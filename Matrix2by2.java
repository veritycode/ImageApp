public class Matrix2by2
{
  private final double[][] values = new double[2][2];

  public Matrix2by2()
  {
    set(1, 0, 0, 1);
  }

  public Matrix2by2(double m00, double m01, double m10, double m11)
  {
    set(m00, m01, m10, m11);
  }

  public double get(int row, int col)
  {
    checkIndex(row, col);
    return values[row][col];
  }

  public void set(int row, int col, double value)
  {
    checkIndex(row, col);
    values[row][col] = value;
  }

  public double getM00()
  {
    return values[0][0];
  }

  public double getM01()
  {
    return values[0][1];
  }

  public double getM10()
  {
    return values[1][0];
  }

  public double getM11()
  {
    return values[1][1];
  }

  public void setM00(double value)
  {
    values[0][0] = value;
  }

  public void setM01(double value)
  {
    values[0][1] = value;
  }

  public void setM10(double value)
  {
    values[1][0] = value;
  }

  public void setM11(double value)
  {
    values[1][1] = value;
  }

  public void set(double m00, double m01, double m10, double m11)
  {
    values[0][0] = m00;
    values[0][1] = m01;
    values[1][0] = m10;
    values[1][1] = m11;
  }

  public Matrix2by2 add(Matrix2by2 other)
  {
    for (int row = 0; row < 2; row++)
    {
      for (int col = 0; col < 2; col++)
      {
        values[row][col] += other.values[row][col];
      }
    }
    return this;
  }

  public void setRotationMatrix(double angleDegrees)
  {
    double radians = Math.toRadians(angleDegrees);
    double cosine = Math.cos(radians);
    double sine = Math.sin(radians);
    set(cleanZero(cosine), cleanZero(sine),
        cleanZero(-sine), cleanZero(cosine));
  }

  public static Vector1by2 multiply(Vector1by2 vector, Matrix2by2 matrix)
  {
    return new Vector1by2(
      Vector1by2.dot(vector, new Vector1by2(matrix.values[0][0],
                                            matrix.values[1][0])),
      Vector1by2.dot(vector, new Vector1by2(matrix.values[0][1],
                                            matrix.values[1][1])));
  }

  private static double cleanZero(double value)
  {
    return Math.abs(value) < 1e-12 ? 0 : value;
  }

  private static void checkIndex(int row, int col)
  {
    if (row < 0 || row > 1 || col < 0 || col > 1)
    {
      throw new IndexOutOfBoundsException("Matrix indices must be 0 or 1.");
    }
  }
}
