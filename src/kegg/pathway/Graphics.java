package kegg.pathway;


public class Graphics {
  String name = "";
  int x=0;
  int y=0;
  String coords="";
  GraphicsType type = GraphicsType.rectangle;
  int width=0;
  int height=0;
  String fgcolor="#000000";
  String bgcolor = "#FFFFFF"; // "#BFFFBF"; for gene products
  
  
  public Graphics(){
    super();
  }
  public Graphics( boolean isGeneProduct) {
    this();
    if (isGeneProduct) bgcolor = "#BFFFBF"; // Default for gene product
  }
  
  
  public Graphics(String name, int x, int y, GraphicsType type, int width, int height, String fgcolor, String bgcolor, boolean isGeneProduct) {
    this(isGeneProduct);
    this.name = name;
    this.x = x;
    this.y = y;
    this.type = type;
    this.width = width;
    this.height = height;
    this.fgcolor = fgcolor;
    this.bgcolor = bgcolor;
  }
  
  
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getX() {
    return x;
  }
  public void setX(int x) {
    this.x = x;
  }
  public int getY() {
    return y;
  }
  public void setY(int y) {
    this.y = y;
  }
  public String getCoords() {
    return coords;
  }
  public void setCoords(String coords) {
    this.coords = coords;
  }
  public GraphicsType getType() {
    return type;
  }
  public void setType(GraphicsType type) {
    this.type = type;
  }
  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }
  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }
  public String getFgcolor() {
    return fgcolor;
  }
  public void setFgcolor(String fgcolor) {
    this.fgcolor = fgcolor;
  }
  public String getBgcolor() {
    return bgcolor;
  }
  public void setBgcolor(String bgcolor) {
    this.bgcolor = bgcolor;
  }
  
  
  
}
