public class Vector1by2
{
  private double row;
  private double col;

  public Vector1by2()
  {
    this(0, 0);
  }

  public Vector1by2(double row, double col)
  {
    this.row = row;
    this.col = col;
  }

  public Vector1by2(Vector1by2 other)
  {
    this(other.row, other.col);
  }

  public double getRow()
  {
    return row;
  }

  public double getCol()
  {
    return col;
  }

  public void setRow(double row)
  {
    this.row = row;
  }

  public void setCol(double col)
  {
    this.col = col;
  }

  public void set(double row, double col)
  {
    this.row = row;
    this.col = col;
  }

  public Vector1by2 add(Vector1by2 other)
  {
    row += other.row;
    col += other.col;
    return this;
  }

  public static double dot(Vector1by2 first, Vector1by2 second)
  {
    return first.row * second.row + first.col * second.col;
  }
}
