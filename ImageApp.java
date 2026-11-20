import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ImageApp
{
  public static void main(String[] args)
  {
    File imageDirectory = getImageDirectory();
    File[] availableImages = getAvailableImages(imageDirectory);
    if (availableImages.length == 0)
    {
      throw new IllegalStateException(
        "No image files were found in " + imageDirectory.getAbsolutePath());
    }

    String initialName = args.length > 0 ? new File(args[0]).getName() : "beach.jpg";
    int initialIndex = findImageIndex(availableImages, initialName);
    String startupMessage = "";
    if (initialIndex < 0)
    {
      initialIndex = 0;
      if (args.length > 0)
      {
        startupMessage = "Requested image was not in the ImageApp folder; loaded "
          + availableImages[initialIndex].getName() + " instead.";
      }
    }

    final int selectedIndex = initialIndex;
    final String message = startupMessage;
    SwingUtilities.invokeLater(() ->
    {
      ImageAppWindow window = new ImageAppWindow(imageDirectory, availableImages,
                                                  selectedIndex, message);
      window.setVisible(true);
    });
  }

  private static File getImageDirectory()
  {
    File workingDirectory = new File(System.getProperty("user.dir")).getAbsoluteFile();
    if (workingDirectory.getName().equalsIgnoreCase("ImageApp"))
    {
      return workingDirectory;
    }

    File projectDirectory = new File(workingDirectory, "ImageApp");
    return projectDirectory.isDirectory() ? projectDirectory : workingDirectory;
  }

  static File[] getAvailableImages(File directory)
  {
    File[] images = directory.listFiles((folder, name) ->
    {
      String lowerName = name.toLowerCase();
      return new File(folder, name).isFile()
        && (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".png") || lowerName.endsWith(".gif")
            || lowerName.endsWith(".bmp"));
    });
    if (images == null)
    {
      throw new IllegalStateException("Cannot read image folder: " + directory.getAbsolutePath());
    }
    Arrays.sort(images, (first, second) -> first.getName().compareToIgnoreCase(second.getName()));
    return images;
  }

  static Picture reloadImage(File source)
  {
    if (source == null || !source.isFile())
    {
      throw new IllegalArgumentException("The source image is not available.");
    }
    return new Picture(source.getAbsolutePath());
  }

  private static int findImageIndex(File[] images, String fileName)
  {
    for (int index = 0; index < images.length; index++)
    {
      if (images[index].getName().equalsIgnoreCase(fileName))
      {
        return index;
      }
    }
    return -1;
  }

  static int[] parseValues(String input, int count)
  {
    if (input == null)
    {
      throw new IllegalArgumentException("Enter the requested values.");
    }

    String[] parts = input.split(",");
    if (parts.length != count)
    {
      throw new IllegalArgumentException(
        "Enter " + count + " comma-separated numbers.");
    }

    int[] values = new int[count];
    try
    {
      for (int index = 0; index < count; index++)
      {
        values[index] = Integer.parseInt(parts[index].trim());
      }
    }
    catch (NumberFormatException exception)
    {
      throw new IllegalArgumentException("Enter whole numbers only.", exception);
    }
    return values;
  }

  static String getPixelDescription(Picture picture, int row, int col)
  {
    if (row < 0 || row >= picture.getHeight() || col < 0 || col >= picture.getWidth())
    {
      throw new IllegalArgumentException("Pixel location is outside the image.");
    }
    Color color = picture.getPixel(col, row).getColor();
    return "Row: " + row + ", Column: " + col + " | R: " + color.getRed()
      + " G: " + color.getGreen() + " B: " + color.getBlue();
  }

  private static final class ImageAppWindow extends JFrame
  {
    private final File imageDirectory;
    private final File[] availableImages;
    private final JComboBox<String> imagePicker;
    private final JComboBox<String> overlayPicker;
    private final JComboBox<String> overlayRotation =
      new JComboBox<String>(new String[] {"None", "90 CCW", "180", "90 CW"});
    private final JTextField sourceRegion = new JTextField("0,0,1,1", 12);
    private final JTextField destination = new JTextField("0,0", 8);
    private final JTextField saveName = new JTextField("result.jpg", 12);
    private final JCheckBox recolorOverlay = new JCheckBox("Recolor inserted image");
    private final JCheckBox removeWhite = new JCheckBox("Remove white background");
    private final JCheckBox blendOverlay = new JCheckBox("Blend with background");
    private final ImageDisplay imageDisplay;
    private final JLabel status = new JLabel("Click the image to inspect a pixel.");
    private Picture picture;
    private File sourceImage;

    private ImageAppWindow(File imageDirectory, File[] availableImages,
                           int initialIndex, String startupMessage)
    {
      super("Image App");
      this.imageDirectory = imageDirectory;
      this.availableImages = availableImages;
      this.sourceImage = availableImages[initialIndex];
      this.picture = reloadImage(sourceImage);

      String[] imageNames = new String[availableImages.length];
      for (int index = 0; index < availableImages.length; index++)
      {
        imageNames[index] = availableImages[index].getName();
      }
      imagePicker = new JComboBox<String>(imageNames);
      imagePicker.setSelectedIndex(initialIndex);
      overlayPicker = new JComboBox<String>(imageNames);
      imageDisplay = new ImageDisplay(picture.getImage());

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(new BorderLayout(8, 8));
      JPanel content = new JPanel(new BorderLayout(8, 8));
      content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      setContentPane(content);

      JPanel controls = new JPanel(new GridLayout(4, 1, 4, 4));
      controls.add(createEffectControls());
      controls.add(createImageControls());
      controls.add(createOverlayControls());
      controls.add(createInsertionControls());
      content.add(controls, BorderLayout.NORTH);

      JScrollPane imageScrollPane = new JScrollPane(imageDisplay);
      content.add(imageScrollPane, BorderLayout.CENTER);
      content.add(status, BorderLayout.SOUTH);

      imageDisplay.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent event)
        {
          int col = event.getX();
          int row = event.getY();
          if (row < picture.getHeight() && col < picture.getWidth())
          {
            imageDisplay.setCurrentX(col);
            imageDisplay.setCurrentY(row);
            status.setText(getPixelDescription(picture, row, col));
          }
        }
      });

      overlayPicker.addActionListener(event -> updateSourceRegion());
      overlayRotation.addActionListener(event -> updateSourceRegion());
      recolorOverlay.addActionListener(event -> updateSourceRegion());
      updateSourceRegion();
      setMinimumSize(new Dimension(900, 650));
      setSize(1200, 850);
      setLocationByPlatform(true);
      if (!startupMessage.isEmpty())
      {
        status.setText(startupMessage);
      }
    }

    private JPanel createEffectControls()
    {
      JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
      addButton(controls, "Recolor", () ->
      {
        recolorSwap(picture);
        refreshImage("Recolored image.");
      });
      addButton(controls, "Negative", () ->
      {
        makeNegative(picture);
        refreshImage("Created negative.");
      });
      addButton(controls, "Grayscale", () ->
      {
        makeGrayscale(picture);
        refreshImage("Created grayscale image.");
      });
      addButton(controls, "Rotate 180", () -> replacePicture(rotate180(picture)));
      addButton(controls, "Rotate 90 CCW", () -> replacePicture(rotate90(picture)));
      addButton(controls, "Rotate 90 CW", () -> replacePicture(rotate270(picture)));
      return controls;
    }

    private JPanel createImageControls()
    {
      JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
      controls.add(new JLabel("Current image:"));
      controls.add(imagePicker);
      addButton(controls, "Load", () ->
      {
        int index = imagePicker.getSelectedIndex();
        if (index >= 0)
        {
          sourceImage = availableImages[index];
          picture = reloadImage(sourceImage);
          refreshImage("Loaded " + availableImages[index].getName() + ".");
        }
      });
      addButton(controls, "Reset", this::resetImage);
      controls.add(new JLabel("Save as:"));
      controls.add(saveName);
      addButton(controls, "Save", this::saveImage);
      return controls;
    }

    private void resetImage()
    {
      picture = reloadImage(sourceImage);
      imagePicker.setSelectedIndex(findImageIndex(availableImages, sourceImage.getName()));
      refreshImage("Reset to " + sourceImage.getName() + ".");
    }

    private JPanel createInsertionControls()
    {
      JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
      controls.add(new JLabel("Source row,col,height,width:"));
      controls.add(sourceRegion);
      controls.add(new JLabel("Destination row,col:"));
      controls.add(destination);
      controls.add(removeWhite);
      controls.add(blendOverlay);
      addButton(controls, "Insert", this::insertSelectedImage);
      return controls;
    }

    private JPanel createOverlayControls()
    {
      JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
      controls.add(new JLabel("Insert image:"));
      controls.add(overlayPicker);
      controls.add(recolorOverlay);
      controls.add(new JLabel("Rotation:"));
      controls.add(overlayRotation);
      return controls;
    }

    private void addButton(JPanel panel, String label, Runnable action)
    {
      JButton button = new JButton(label);
      button.addActionListener(event -> action.run());
      panel.add(button);
    }

    private void updateSourceRegion()
    {
      int index = overlayPicker.getSelectedIndex();
      if (index >= 0)
      {
        Picture overlay = createOverlay(index);
        sourceRegion.setText("0,0," + overlay.getHeight() + "," + overlay.getWidth());
      }
    }

    private void insertSelectedImage()
    {
      int index = overlayPicker.getSelectedIndex();
      if (index < 0)
      {
        status.setText("Choose an image to insert.");
        return;
      }

      try
      {
        int[] source = parseValues(sourceRegion.getText(), 4);
        int[] target = parseValues(destination.getText(), 2);
        Picture overlay = createOverlay(index);
        picture = embedImage(picture, overlay, source[0], source[1],
          source[2], source[3], target[0], target[1],
          removeWhite.isSelected(), blendOverlay.isSelected());
        refreshImage("Inserted " + availableImages[index].getName() + ".");
      }
      catch (IllegalArgumentException exception)
      {
        status.setText(exception.getMessage());
      }
    }

    private Picture createOverlay(int index)
    {
      Picture overlay = new Picture(availableImages[index].getAbsolutePath());
      if (recolorOverlay.isSelected())
      {
        recolorSwap(overlay);
      }

      String rotation = (String) overlayRotation.getSelectedItem();
      if ("90 CCW".equals(rotation))
      {
        overlay = rotate90(overlay);
      }
      else if ("180".equals(rotation))
      {
        overlay = rotate180(overlay);
      }
      else if ("90 CW".equals(rotation))
      {
        overlay = rotate270(overlay);
      }
      return overlay;
    }

    private void saveImage()
    {
      String name = saveName.getText().trim();
      if (name.isEmpty() || !new File(name).getName().equals(name))
      {
        status.setText("Enter a file name in the ImageApp folder.");
        return;
      }
      File output = new File(imageDirectory, name);
      if (picture.write(output.getAbsolutePath()))
      {
        status.setText("Saved " + output.getName() + ".");
      }
      else
      {
        status.setText("Could not save " + output.getName() + ".");
      }
    }

    private void replacePicture(Picture newPicture)
    {
      picture = newPicture;
      refreshImage("Updated image.");
    }

    private void refreshImage(String message)
    {
      imageDisplay.setImage(picture.getImage());
      imageDisplay.setCurrentX(0);
      imageDisplay.setCurrentY(0);
      status.setText(message + " " + getPixelDescription(picture, 0, 0));
      revalidate();
      repaint();
    }
  }

  public static Picture recolorSwap(Picture picture)
  {
    for (Pixel[] row : picture.getPixels2D())
    {
      for (Pixel pixel : row)
      {
        pixel.setColor(new Color(pixel.getBlue(), pixel.getRed(), pixel.getGreen()));
      }
    }
    return picture;
  }

  public static Picture makeNegative(Picture picture)
  {
    for (Pixel[] row : picture.getPixels2D())
    {
      for (Pixel pixel : row)
      {
        pixel.setColor(new Color(255 - pixel.getRed(),
                                 255 - pixel.getGreen(),
                                 255 - pixel.getBlue()));
      }
    }
    return picture;
  }

  public static Picture makeGrayscale(Picture picture)
  {
    for (Pixel[] row : picture.getPixels2D())
    {
      for (Pixel pixel : row)
      {
        int average = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
        pixel.setColor(new Color(average, average, average));
      }
    }
    return picture;
  }

  public static Picture rotate180(Picture picture)
  {
    return rotate(picture, 180);
  }

  public static Picture rotate90(Picture picture)
  {
    return rotate(picture, 90);
  }

  public static Picture rotate270(Picture picture)
  {
    return rotate(picture, -90);
  }

  public static Picture rotate(Picture picture, int angle)
  {
    int normalizedAngle = ((angle % 360) + 360) % 360;
    if (normalizedAngle != 0 && normalizedAngle != 90
        && normalizedAngle != 180 && normalizedAngle != 270)
    {
      throw new IllegalArgumentException("Rotation must be a multiple of 90 degrees.");
    }

    Pixel[][] sourcePixels = picture.getPixels2D();
    int sourceHeight = sourcePixels.length;
    int sourceWidth = sourcePixels[0].length;
    boolean quarterTurn = normalizedAngle == 90 || normalizedAngle == 270;
    int outputHeight = quarterTurn ? sourceWidth : sourceHeight;
    int outputWidth = quarterTurn ? sourceHeight : sourceWidth;
    Picture rotated = new Picture(outputHeight, outputWidth);
    Pixel[][] outputPixels = rotated.getPixels2D();
    Matrix2by2 rotation = new Matrix2by2();
    rotation.setRotationMatrix(normalizedAngle == 270 ? -90 : normalizedAngle);
    double sourceCenterRow = (sourceHeight - 1) / 2.0;
    double sourceCenterCol = (sourceWidth - 1) / 2.0;
    double outputCenterRow = (outputHeight - 1) / 2.0;
    double outputCenterCol = (outputWidth - 1) / 2.0;

    for (int row = 0; row < sourceHeight; row++)
    {
      for (int col = 0; col < sourceWidth; col++)
      {
        Vector1by2 offset = new Vector1by2(row - sourceCenterRow,
                                           col - sourceCenterCol);
        Vector1by2 transformed = Matrix2by2.multiply(offset, rotation);
        int outputRow = (int) Math.round(outputCenterRow + transformed.getRow());
        int outputCol = (int) Math.round(outputCenterCol + transformed.getCol());
        outputPixels[outputRow][outputCol].setColor(sourcePixels[row][col].getColor());
      }
    }
    return rotated;
  }

  public static Picture embedImage(Picture background, Picture overlay,
                                   int startRow, int startCol)
  {
    return embedImage(background, overlay, startRow, startCol, false);
  }

  public static Picture embedImage(Picture background, Picture overlay,
                                   int startRow, int startCol,
                                   boolean removeWhiteBackground)
  {
    return embedImage(background, overlay, 0, 0, overlay.getHeight(),
                      overlay.getWidth(), startRow, startCol,
                      removeWhiteBackground, false);
  }

  public static Picture embedImage(Picture background, Picture overlay,
                                   int startRow, int startCol,
                                   boolean removeWhiteBackground,
                                   boolean blendColors)
  {
    return embedImage(background, overlay, 0, 0, overlay.getHeight(),
                      overlay.getWidth(), startRow, startCol,
                      removeWhiteBackground, blendColors);
  }

  public static Picture embedImage(Picture background, Picture overlay,
                                   int sourceRow, int sourceCol,
                                   int sourceHeight, int sourceWidth,
                                   int startRow, int startCol,
                                   boolean removeWhiteBackground)
  {
    return embedImage(background, overlay, sourceRow, sourceCol, sourceHeight,
                      sourceWidth, startRow, startCol, removeWhiteBackground, false);
  }

  public static Picture embedImage(Picture background, Picture overlay,
                                   int sourceRow, int sourceCol,
                                   int sourceHeight, int sourceWidth,
                                   int startRow, int startCol,
                                   boolean removeWhiteBackground,
                                   boolean blendColors)
  {
    if (sourceRow < 0 || sourceCol < 0 || sourceHeight <= 0 || sourceWidth <= 0
        || sourceRow > overlay.getHeight() - sourceHeight
        || sourceCol > overlay.getWidth() - sourceWidth)
    {
      throw new IllegalArgumentException("The selected image portion is out of bounds.");
    }

    Picture embedded = new Picture(background);
    Pixel[][] basePixels = embedded.getPixels2D();
    Pixel[][] overlayPixels = overlay.getPixels2D();
    for (int row = 0; row < sourceHeight; row++)
    {
      long targetRow = (long) startRow + row;
      if (targetRow < 0 || targetRow >= basePixels.length)
      {
        continue;
      }
      int targetRowIndex = (int) targetRow;
      for (int col = 0; col < sourceWidth; col++)
      {
        long targetCol = (long) startCol + col;
        if (targetCol < 0 || targetCol >= basePixels[0].length)
        {
          continue;
        }

        int targetColIndex = (int) targetCol;
        Color color = overlayPixels[sourceRow + row][sourceCol + col].getColor();
        if (!removeWhiteBackground || !Color.WHITE.equals(color))
        {
          if (blendColors)
          {
            Color backgroundColor = basePixels[targetRowIndex][targetColIndex].getColor();
            color = new Color((backgroundColor.getRed() + color.getRed()) / 2,
                              (backgroundColor.getGreen() + color.getGreen()) / 2,
                              (backgroundColor.getBlue() + color.getBlue()) / 2);
          }
          basePixels[targetRowIndex][targetColIndex].setColor(color);
        }
      }
    }
    return embedded;
  }

}
