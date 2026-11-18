/*
  ImageApp:
 */
import java.awt.Color;

public class ImageApp
{
  public static void main(String[] args)
  {
    String pictureFile = "lib/beach.jpg";

    // Image #1: reorder the RGB values in each pixel
    Picture recoloredImg = recolorSwap(new Picture(pictureFile));
    recoloredImg.show();

    // Image #2: create a photographic negative
    Picture negImg = makeNegative(new Picture(pictureFile));
    negImg.show();

    // Image #3: create a grayscale version
    Picture grayscaleImg = makeGrayscale(new Picture(pictureFile));
    grayscaleImg.show();

    // Image #4: rotate 180 degrees
    Picture upsideDownImage = rotate180(new Picture(pictureFile));

    // Image #5: rotate 90 degrees
    Picture rotateImg = rotate90(new Picture(pictureFile));
    rotateImg.show();

    // Image #6: rotate -90 degrees
    Picture rotateImg2 = rotate270(new Picture(pictureFile));
    rotateImg2.show();

    // Final Image: embed a smaller picture into a larger one
    Picture background = new Picture(pictureFile);
    Picture overlay = new Picture("lib/flower1.jpg");
    Picture combinedImg = embedImage(background, overlay, background.getHeight() / 4, overlay.getWidth() / 4);
    combinedImg.show();

  }

  public static Picture recolorSwap(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    for (int row = 0; row < pixels.length; row++)
    {
      for (int col = 0; col < pixels[0].length; col++)
      {
        Pixel pixel = pixels[row][col];
        int red = pixel.getRed();
        int green = pixel.getGreen();
        int blue = pixel.getBlue();
        pixel.setColor(new Color(blue, red, green));
      }
    }
    return picture;
  }

  public static Picture makeNegative(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    for (int row = 0; row < pixels.length; row++)
    {
      for (int col = 0; col < pixels[0].length; col++)
      {
        Pixel pixel = pixels[row][col];
        pixel.setColor(new Color(255 - pixel.getRed(),
                                 255 - pixel.getGreen(),
                                 255 - pixel.getBlue()));
      }
    }
    return picture;
  }

  public static Picture makeGrayscale(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    for (int row = 0; row < pixels.length; row++)
    {
      for (int col = 0; col < pixels[0].length; col++)
      {
        Pixel pixel = pixels[row][col];
        int avg = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
        pixel.setColor(new Color(avg, avg, avg));
      }
    }
    return picture;
  }

  public static Picture rotate180(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    int height = pixels.length;
    int width = pixels[0].length;
    Picture rotated = new Picture(height, width);
    Pixel[][] rotatedPixels = rotated.getPixels2D();

    for (int row = 0; row < height; row++)
    {
      for (int col = 0; col < width; col++)
      {
        rotatedPixels[height - 1 - row][width - 1 - col].setColor(pixels[row][col].getColor());
      }
    }
    return rotated;
  }

  public static Picture rotate90(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    int height = pixels.length;
    int width = pixels[0].length;
    Picture rotated = new Picture(width, height);
    Pixel[][] rotatedPixels = rotated.getPixels2D();

    for (int row = 0; row < height; row++)
    {
      for (int col = 0; col < width; col++)
      {
        rotatedPixels[col][height - 1 - row].setColor(pixels[row][col].getColor());
      }
    }
    return rotated;
  }

  public static Picture rotate270(Picture picture)
  {
    Pixel[][] pixels = picture.getPixels2D();
    int height = pixels.length;
    int width = pixels[0].length;
    Picture rotated = new Picture(width, height);
    Pixel[][] rotatedPixels = rotated.getPixels2D();

    for (int row = 0; row < height; row++)
    {
      for (int col = 0; col < width; col++)
      {
        rotatedPixels[width - 1 - col][row].setColor(pixels[row][col].getColor());
      }
    }
    return rotated;
  }

  public static Picture embedImage(Picture background, Picture overlay, int startRow, int startCol)
  {
    Picture embedded = new Picture(background);
    Pixel[][] basePixels = embedded.getPixels2D();
    Pixel[][] overlayPixels = overlay.getPixels2D();

    for (int row = 0; row < overlayPixels.length; row++)
    {
      for (int col = 0; col < overlayPixels[0].length; col++)
      {
        int targetRow = startRow + row;
        int targetCol = startCol + col;

        if (targetRow < basePixels.length && targetCol < basePixels[0].length)
        {
          basePixels[targetRow][targetCol].setColor(overlayPixels[row][col].getColor());
        }
      }
    }
    return embedded;
  }
}
