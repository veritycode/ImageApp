import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageAppTest
{
  private interface TestCase
  {
    void run() throws Exception;
  }

  public static void main(String[] args)
  {
    int total = 11;
    int passed = 0;
    passed += runTest("Vector constructors, accessors, and mutators", ImageAppTest::testVectorApi);
    passed += runTest("Vector dot product", ImageAppTest::testVectorDotProduct);
    passed += runTest("Matrix constructors, accessors, and mutators", ImageAppTest::testMatrixApi);
    passed += runTest("Matrix rotation and vector multiplication", ImageAppTest::testMatrixMultiplication);
    passed += runTest("Image color effects", ImageAppTest::testColorEffects);
    passed += runTest("Image rotations", ImageAppTest::testImageRotations);
    passed += runTest("Image insertion, background removal, and blending", ImageAppTest::testImageInsertion);
    passed += runTest("Image folder selection", ImageAppTest::testImageFolderSelection);
    passed += runTest("Reset image", ImageAppTest::testResetImage);
    passed += runTest("Pixel inspection", ImageAppTest::testPixelInspection);
    passed += runTest("Image display updates and input validation", ImageAppTest::testDisplayAndInput);
    System.out.println(passed + "/" + total + " tests passed.");
    if (passed != total)
    {
      System.exit(1);
    }
  }

  private static int runTest(String name, TestCase test)
  {
    try
    {
      test.run();
      System.out.println(name + ": PASS");
      return 1;
    }
    catch (Exception | AssertionError failure)
    {
      System.out.println(name + ": FAIL (" + failure.getMessage() + ")");
      return 0;
    }
  }

  private static void testVectorApi()
  {
    Vector1by2 vector = new Vector1by2(2, 3);
    assertEquals(2, vector.getRow());
    assertEquals(3, vector.getCol());
    vector.setRow(4);
    vector.setCol(5);
    vector.add(new Vector1by2(1, -2));
    assertEquals(5, vector.getRow());
    assertEquals(3, vector.getCol());
    Vector1by2 copy = new Vector1by2(vector);
    copy.set(6, 7);
    assertEquals(6, copy.getRow());
    assertEquals(7, copy.getCol());
    Vector1by2 zero = new Vector1by2();
    assertEquals(0, zero.getRow());
    assertEquals(0, zero.getCol());
  }

  private static void testVectorDotProduct()
  {
    assertEquals(13, Vector1by2.dot(new Vector1by2(5, 3),
                                    new Vector1by2(2, 1)));
    assertEquals(0, Vector1by2.dot(new Vector1by2(4, -4),
                                   new Vector1by2(1, 1)));
  }

  private static void testMatrixApi()
  {
    Matrix2by2 matrix = new Matrix2by2(1, 2, 3, 4);
    assertEquals(3, matrix.get(1, 0));
    matrix.set(0, 1, 5);
    matrix.add(new Matrix2by2(1, 1, 1, 1));
    assertEquals(2, matrix.getM00());
    assertEquals(6, matrix.getM01());
    assertEquals(4, matrix.getM10());
    assertEquals(5, matrix.getM11());
    matrix.setM00(2);
    matrix.setM01(8);
    matrix.setM10(9);
    matrix.setM11(10);
    assertEquals(8, matrix.getM01());
    assertEquals(9, matrix.getM10());
    assertEquals(10, matrix.getM11());
    assertThrowsIndexOutOfBounds(() -> matrix.get(2, 0));
    Matrix2by2 identity = new Matrix2by2();
    assertEquals(1, identity.getM00());
    assertEquals(1, identity.getM11());
    assertEquals(0, identity.getM01());
  }

  private static void testMatrixMultiplication()
  {
    Matrix2by2 matrix = new Matrix2by2(2, 6, 4, 5);
    Vector1by2 result = Matrix2by2.multiply(new Vector1by2(2, 3), matrix);
    assertEquals(16, result.getRow());
    assertEquals(27, result.getCol());
    matrix.setRotationMatrix(90);
    result = Matrix2by2.multiply(new Vector1by2(2, 3), matrix);
    assertEquals(-3, result.getRow());
    assertEquals(2, result.getCol());
  }

  private static void testColorEffects()
  {
    Picture picture = singlePixel(new Color(10, 20, 30));
    assertColor(new Color(30, 10, 20), ImageApp.recolorSwap(picture), 0, 0);
    picture = singlePixel(new Color(10, 20, 30));
    assertColor(new Color(245, 235, 225), ImageApp.makeNegative(picture), 0, 0);
    picture = singlePixel(new Color(10, 20, 30));
    assertColor(new Color(20, 20, 20), ImageApp.makeGrayscale(picture), 0, 0);
  }

  private static void testImageRotations()
  {
    Picture source = new Picture(2, 3);
    Color[][] colors = {
      {Color.RED, Color.GREEN, Color.BLUE},
      {Color.YELLOW, Color.CYAN, Color.MAGENTA}
    };
    Pixel[][] pixels = source.getPixels2D();
    for (int row = 0; row < pixels.length; row++)
    {
      for (int col = 0; col < pixels[row].length; col++)
      {
        pixels[row][col].setColor(colors[row][col]);
      }
    }

    Picture counterclockwise = ImageApp.rotate90(source);
    assertEquals(3, counterclockwise.getHeight());
    assertEquals(2, counterclockwise.getWidth());
    assertColor(Color.BLUE, counterclockwise, 0, 0);
    assertColor(Color.MAGENTA, counterclockwise, 0, 1);
    assertColor(Color.RED, counterclockwise, 2, 0);

    Picture clockwise = ImageApp.rotate270(source);
    assertColor(Color.YELLOW, clockwise, 0, 0);
    assertColor(Color.RED, clockwise, 0, 1);
    assertColor(Color.BLUE, clockwise, 2, 1);

    Picture flipped = ImageApp.rotate180(source);
    assertColor(Color.MAGENTA, flipped, 0, 0);
    assertColor(Color.RED, flipped, 1, 2);
  }

  private static void testImageInsertion()
  {
    Picture background = new Picture(2, 2);
    background.getPixels2D()[0][0].setColor(Color.RED);
    background.getPixels2D()[0][1].setColor(Color.RED);
    Picture overlay = new Picture(2, 2);
    overlay.getPixels2D()[0][0].setColor(Color.WHITE);
    overlay.getPixels2D()[0][1].setColor(Color.BLUE);
    overlay.getPixels2D()[1][0].setColor(Color.GREEN);
    Picture inserted = ImageApp.embedImage(background, overlay, 0, 0, true);
    assertColor(Color.RED, inserted, 0, 0);
    assertColor(Color.BLUE, inserted, 0, 1);
    assertColor(Color.GREEN, inserted, 1, 0);

    Picture cropped = ImageApp.embedImage(background, overlay,
      1, 0, 1, 1, 1, 1, false);
    assertColor(Color.GREEN, cropped, 1, 1);

    Picture blended = ImageApp.embedImage(background, overlay,
      0, 0, 1, 2, 0, 0, true, true);
    assertColor(Color.RED, blended, 0, 0);
    assertColor(new Color(127, 0, 127), blended, 0, 1);
    assertColor(Color.RED, background, 0, 0);

    Picture clipped = ImageApp.embedImage(background, overlay, -1, -1, true);
    assertColor(Color.RED, clipped, 0, 0);
    assertColor(Color.WHITE, clipped, 1, 1);
    assertThrowsIllegalArgument(() ->
      ImageApp.embedImage(background, overlay, 1, 1, 2, 2, 0, 0, false));
  }

  private static void testImageFolderSelection() throws IOException
  {
    File directory = Files.createTempDirectory("image-app-test").toFile();
    File nestedDirectory = new File(directory, "nested");
    if (!nestedDirectory.mkdir())
    {
      throw new AssertionError("Could not create test image directory.");
    }

    try
    {
      new File(directory, "zebra.PNG").createNewFile();
      new File(directory, "apple.jpg").createNewFile();
      new File(directory, "notes.txt").createNewFile();
      new File(nestedDirectory, "nested.png").createNewFile();

      File[] images = ImageApp.getAvailableImages(directory);
      if (images.length != 2 || !images[0].getName().equals("apple.jpg")
          || !images[1].getName().equals("zebra.PNG"))
      {
        throw new AssertionError("Image choices must include only sorted images from the app folder.");
      }
    }
    finally
    {
      File[] contents = directory.listFiles();
      if (contents != null)
      {
        for (File file : contents)
        {
          if (file.isDirectory())
          {
            File[] nestedFiles = file.listFiles();
            if (nestedFiles != null)
            {
              for (File nestedFile : nestedFiles)
              {
                Files.deleteIfExists(nestedFile.toPath());
              }
            }
          }
          Files.deleteIfExists(file.toPath());
        }
      }
      Files.deleteIfExists(directory.toPath());
    }
  }

  private static void testPixelInspection()
  {
    Picture picture = new Picture(2, 3);
    picture.getPixel(1, 0).setColor(new Color(12, 34, 56));
    String description = ImageApp.getPixelDescription(picture, 0, 1);
    if (!description.equals("Row: 0, Column: 1 | R: 12 G: 34 B: 56"))
    {
      throw new AssertionError("Pixel inspection must report the clicked pixel's coordinates and color.");
    }
    assertThrowsIllegalArgument(() -> ImageApp.getPixelDescription(picture, 2, 0));
  }

  private static void testResetImage() throws IOException
  {
    File source = Files.createTempFile("image-app-reset", ".png").toFile();
    Color originalColor = new Color(25, 100, 200);
    try
    {
      if (!singlePixel(originalColor).write(source.getAbsolutePath()))
      {
        throw new AssertionError("Could not create the reset test image.");
      }

      Picture edited = ImageApp.reloadImage(source);
      edited.getPixel(0, 0).setColor(Color.BLACK);
      Picture reset = ImageApp.reloadImage(source);
      assertColor(originalColor, reset, 0, 0);
    }
    finally
    {
      Files.deleteIfExists(source.toPath());
    }
  }

  private static void testDisplayAndInput()
  {
    Picture picture = new Picture(2, 3);
    ImageDisplay display = new ImageDisplay(picture.getImage());
    assertEquals(3, display.getPreferredScrollableViewportSize().width);
    assertEquals(2, display.getPreferredScrollableViewportSize().height);

    Picture rotated = ImageApp.rotate90(picture);
    display.setImage(rotated.getImage());
    assertEquals(2, display.getPreferredScrollableViewportSize().width);
    assertEquals(3, display.getPreferredScrollableViewportSize().height);

    int[] region = ImageApp.parseValues("1, 2, 3, 4", 4);
    assertEquals(1, region[0]);
    assertEquals(4, region[3]);
    assertThrowsIllegalArgument(() -> ImageApp.parseValues("1,2", 4));
    assertThrowsIllegalArgument(() -> ImageApp.parseValues("1,x,3,4", 4));
  }

  private static Picture singlePixel(Color color)
  {
    Picture picture = new Picture(1, 1);
    picture.getPixel(0, 0).setColor(color);
    return picture;
  }

  private static void assertThrowsIllegalArgument(Runnable action)
  {
    try
    {
      action.run();
    }
    catch (IllegalArgumentException expected)
    {
      return;
    }
    throw new AssertionError("Expected an IllegalArgumentException.");
  }

  private static void assertThrowsIndexOutOfBounds(Runnable action)
  {
    try
    {
      action.run();
    }
    catch (IndexOutOfBoundsException expected)
    {
      return;
    }
    throw new AssertionError("Expected an IndexOutOfBoundsException.");
  }

  private static void assertColor(Color expected, Picture picture, int row, int col)
  {
    if (!expected.equals(picture.getPixels2D()[row][col].getColor()))
    {
      throw new AssertionError("Unexpected color at (" + row + ", " + col + ").");
    }
  }

  private static void assertEquals(double expected, double actual)
  {
    if (Math.abs(expected - actual) > 1e-9)
    {
      throw new AssertionError("Expected " + expected + " but was " + actual + ".");
    }
  }
}
