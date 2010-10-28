package de.zbit.kegg.parser.pathway;


/**
 * Corresponding to the Kegg Graphics class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author wrzodek
 */
public class Graphics {
  /**
   * the label of this graphics object
   */
  private String name = "";
  /**
   * the X axis position of this graphics object
   */
  private int x=0;
  /**
   * the Y axis position of this graphics object
   */
  private int y=0;
  /**
   * the shape of this graphics object
   */
  private GraphicsType type = GraphicsType.rectangle;
  /**
   * the width of this graphics object
   */
  private int width=45;
  /**
   * the height of this graphics object
   */
  private int height=17;
  /**
   * the foreground color used by this graphics object
   */
  private String fgcolor="#000000";
  /**
   * the backgraound color used by this graphics object
   * "=> for gene products"
   */
  private String bgcolor = "#FFFFFF"; // "#BFFFBF"; 
  
  /**
   * Should not be public, because isGeneProduct information is important!
   */
  private Graphics(){
    super();
  }
  
  /**
   * 
   * @param isGeneProduct - (EntryType==EntryType.gene)
   */
  public Graphics( boolean isGeneProduct) {
    this();
    if (isGeneProduct) bgcolor = "#BFFFBF"; // Default for gene product
  }
  
  /**
   * Create a new graphics object, based on the given entry.
   * Does NOT set this graphics as the graphics object of e and
   * does NOT consider any other graphics object that may already
   * belong to e.
   * @param e
   */
  public Graphics(Entry e) {
    this(e.getType()==EntryType.gene || e.getType()==EntryType.genes);
  }
  
  /**
   * 
   * @param name
   * @param x
   * @param y
   * @param type
   * @param width
   * @param height
   * @param fgcolor
   * @param bgcolor
   * @param isGeneProduct - (EntryType==EntryType.gene)
   */
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
  
  /**
   * Returns true if and only if the background color has been set.
   * @return
   */
  public boolean isBGcolorSet() {
    if (bgcolor!=null && bgcolor.length()>0 && !bgcolor.trim().equalsIgnoreCase("none"))
      return true;
    return false;
  }
  
  /**
   * The bgcolor attribute specifies the background color of this object. The default
   * value is "#FFFFFF". The background color for the gene product is "#BFFFBF".
   * @return Background color
   */
  public String getBgcolor() {
    return bgcolor;
  }
  
  /**
   * The coords attribute specifies a set of coordinates, x1,y1,x2,y2,..., for the line object.
   * @return new String( x + "," + y + "," + (x+width)+ "," + (y+height) )
   */
  public String getCoords() {
    return x + "," + y + "," + (x+width)+ "," + (y+height);
  }
  
  /**
   * The fgcolor attribute specifies the foreground color of this object. It applies
   * to the frame and the character string. The default value is "#000000".
   * @return
   */
  public String getFgcolor() {
    return fgcolor;
  }
  
  /**
   * The height attribute specifies the height of this object. The default value is "17".
   * @return
   */
  public int getHeight() {
    return height;
  }
  
  /**
   * The name attribute contains the label that is associated with this graphics object.
   * When two or more name attributes are specified in the same entry element, the first
   * one is taken as the attribute value. When the type attribute value of the entry element
   * is "gene", the gene name is specified for this attribute value. 
   * @return the label of this graphics object (e.g. "1.1.1.43" or "Methane metabolism")
   */
  public String getName() {
    return name;
  }
  
  /**
   * The type attribute specifies the shape of this object. The default value is "rectangle".
   * <table style="" border="1">
  <tbody><tr id="item"> <td id="a1">attribute value</td> <td id="a2">explanation</td> </tr>
  <tr><td>rectangle</td>
  <td>the shape is a rectangle, which is used to represent a gene product and its complex
  (including an ortholog group).</td></tr>
  <tr><td>circle</td>
  <td>the shape is a circle, which is used to specify any other molecule such as a chemical
  compound and a glycan.</td></tr>
  <tr><td>roundrectangle</td>
  <td>the shape is a round rectangle, which is used to represent a linked pathway.</td></tr>
  <tr><td>line</td>
  <td>the shape is a polyline, which is used to represent a reaction or a relation
  (and also a gene or an ortholog group).</td></tr>
  </tbody></table>
   * @return GraphicsType
   */
  public GraphicsType getType() {
    return type;
  }
  
  /**
   * The width attribute specifies the width this object. The default value is "45".
   * @return
   */
  public int getWidth() {
    return width;
  }
  
  /**
   * The x attribute specifies the x-coordinate value of this graphics object
   * in the manually drawn KEGG pathway map.
   * @return
   */
  public int getX() {
    return x;
  }
  
  /**
   * The y attribute specifies the y-coordinate value of this graphics object
   * in the manually drawn KEGG pathway map.
   * @return
   */
  public int getY() {
    return y;
  }
  
  /**
   * @param bgcolor
   */
  public void setBgcolor(String bgcolor) {
    this.bgcolor = bgcolor;
  }
  
  /**
   * 
   * @param fgcolor
   */
  public void setFgcolor(String fgcolor) {
    this.fgcolor = fgcolor;
  }
  
  /**
   * 
   * @param height
   */
  public void setHeight(int height) {
    this.height = height;
  }
  
  /**
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * 
   * @param type
   */
  public void setType(GraphicsType type) {
    this.type = type;
  }
  
  /**
   * 
   * @param width
   */
  public void setWidth(int width) {
    this.width = width;
  }
  
  /**
   * 
   * @param x
   */
  public void setX(int x) {
    this.x = x;
  }
  
  /**
   * 
   * @param y
   */
  public void setY(int y) {
    this.y = y;
  }
  
}
