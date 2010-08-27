/**
 *
 * @author wrzodek
 */
package de.zbit.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import de.zbit.io.CSVReader;

/**
 * Builds a panel to asign columns from the CSVReader to certain subjects.
 * 
 * For example, if you open a CSV file and want to ask the user for the
 * columns for sequenceStart, sequenceEnd, strand and sequenceName,
 * but the sequenceName is optional, you can easily create a dialog with
 * the following command:
 * 
 *    CSVReaderColumnChooser c = new CSVReaderColumnChooser("file.csv.txt");
 *    c.addColumnChooser(new String[]{"sequenceStart","sequenceEnd"}, true, true);
 *    c.addColumnChooser(new String[]{"sequenceName"}, false, true);
 * 
 * @author wrzodek
 */
public class CSVReaderColumnChooser extends JPanel {
  private static final long serialVersionUID = -7944793539696555890L;
  
  private CSVReader r;
  private String[] firstLine=null;
  
  private int prefferedWidth=0;
  
  private JPanel optionalPanel;
  private JPanel requiredPanel;
  
  /**
   * Builds a panel to asign columns from the CSVReader to certain subjects.
   * 
   * The headers are taken from the CSVReader, the first data line of the
   * CSVReader is taken for the Preview.
   * 
   * @param r
   * @throws IOException
   */
  public CSVReaderColumnChooser(CSVReader r) throws IOException {
    super();
    this.r = r;
    init();
  }
  /**
   * See {@link #CSVReaderColumnChooser(CSVReader)}.
   * 
   * Builds a CSVReader from the inFile.
   * 
   * @param inFile
   * @throws IOException
   */
  public CSVReaderColumnChooser(String inFile) throws IOException {
    this(new CSVReader(inFile));
  }
  
  /**
   * Reads first line and headers.
   * Builds ComboBoxModels, based on headers.
   * 
   * Sets default swing components.
   * @throws IOException if file could not be read.
   */
  private void init() throws IOException {
    
    // Open the file and read the first data line.
    firstLine=null;
    if (r.getDataButDoNotReadIfNotAvailable()!=null && r.getDataButDoNotReadIfNotAvailable().length>0) {
      firstLine = r.getDataButDoNotReadIfNotAvailable()[0];
    } else {
        r.open();
        firstLine=r.getNextLine();
        r.close();
    }
    if (firstLine==null) throw new IOException("Invalid CSV file.");
    
    // Init the panel
    // Don't set the preferred size. This breaks the layouts of implementing
    // classes.
    //this.setPreferredSize(new java.awt.Dimension(450, 200));
      
    initGUI();
    setSplitRequiredAndOptional(true);
  }
  private void initGUI() {
    removeAll();
    optionalPanel = new JPanel(new GridLayout(0,1));
    requiredPanel = new JPanel(new GridLayout(0,1));
    
    this.setLayout(new VerticalLayout());
    add(requiredPanel);
    add(optionalPanel);
  }
  
  /**
   * {@inheritDoc}
   */
  public void repaint() {
    if (optionalPanel!=null) optionalPanel.setVisible(optionalPanel.getComponentCount()>0);
    if (requiredPanel!=null) requiredPanel.setVisible(requiredPanel.getComponentCount()>0);
    
    super.repaint();
  }

  /**
   * This function gives both  sub-panels 
   * (optional and required )the same width. 
   * @param width - if <=0, gives the smaller panel the width
   * of the larger one. Else: sets this width.
   */
  public void setPrefferedWidth(int w) {
    prefferedWidth=w;
    setPrefferedSizes(w);
  }
  
  /**
   * This function gives both  sub-panels 
   * (optional and required )the same width. 
   * @param width - if <=0, gives the smaller panel the width
   * of the larger one. Else: sets this width.
   */
  private void setPrefferedSizes(int width) {
    // Give both panels the same width
    if (optionalPanel!=null && requiredPanel!=null && optionalPanel.isVisible() &&
        requiredPanel.isVisible()) {
      
      optionalPanel.setPreferredSize(null);
      requiredPanel.setPreferredSize(null);
      this.doLayout();
      
      int maxWidth=width<=0?Math.max(optionalPanel.getWidth(), requiredPanel.getWidth()):width;
      maxWidth = Math.max(prefferedWidth, maxWidth);
      if (maxWidth>0) {
        
        optionalPanel.setPreferredSize(new Dimension(maxWidth, optionalPanel.getHeight()));
        requiredPanel.setPreferredSize(new Dimension(maxWidth, requiredPanel.getHeight()));
        
        
        optionalPanel.repaint();
        requiredPanel.repaint();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void remove(Component comp) {
    optionalPanel.remove(comp);
    requiredPanel.remove(comp);
    super.remove(comp);
    setPrefferedSizes(0);
  }
  
  /**
   * Returns the CSVReader associated with this instance.
   * @return
   */
  public CSVReader getCSVReader() {
    return r;
  }

  /**
   * Returns the JPanel with all required column choosers.
   * @return
   */
  public JPanel getRequiredPanel() {
    return requiredPanel;
  }
  
  /**
   * Returns the JPanel with all optional column choosers.
   * @return
   */
  public JPanel getOptionalPanel() {
    return optionalPanel;
  }

  
  /**
   * Returns the selected value for the given title.
   * 
   * @param title - Must be the same as given in addColumnChooser().
   * @return column index (or -1 if no column is choosen).
   * @throws Exception - if the title does not exist.
   */
  public int getSelectedValue(String title) throws Exception {
    return  getColumnChooser(title).getSelectedValue();
  }
  
  /**
   * Returns the JColumnChooser for the given title.
   * @param title - Must be the same as given in addColumnChooser().
   * @return JColumnChooser for the given title.
   * @throws Exception - if the title does not exist.
   */
  public JColumnChooser getColumnChooser(String title) throws Exception {
    for (int j=0; j<2; j++) {
      JPanel p;
      if (j==0) p=requiredPanel; else p=optionalPanel;
      
      if (p!=null) {
        for (int i=0; i<p.getComponentCount(); i++) {
          if (p.getComponent(i) == null || p.getComponent(i).getName() == null) continue;
          if (p.getComponent(i).getName().equals(title) &&
              p.getComponent(i) instanceof JColumnChooser) {
            return  ((JColumnChooser)p.getComponent(i));
          }
        }
      }
    }
    
    throw new Exception("No column chooser named '" + title + "'.");
  }
  
  /**
   * Checks if all JColumnChoosers are set to distinct columns. Only
   * multiple optional Column Choosers are allowed to be "unselected".
   * @return true if only distinct columns have been selected. Else: false.
   */
  public boolean isAllSelectedColumnsAreDistinct() {
    // ArrayList is used to check if all selected columns are distinct.
    ArrayList<Integer> alreadySelectedColumns = new ArrayList<Integer>();
    
    for (int j=0; j<2; j++) {
      JPanel p;
      if (j==0) p=requiredPanel; else p=optionalPanel;
      
      if (p!=null) {
        for (int i=0; i<p.getComponentCount(); i++) {
          if (p.getComponent(i) == null || 
            !(p.getComponent(i) instanceof JColumnChooser)) {
            continue;
          } else {
            int sel = ((JColumnChooser)p.getComponent(i)).getSelectedValue();
            if (sel>=0) { // Multiple optional columns may be -1
              if (alreadySelectedColumns.contains(sel)) return false;
              alreadySelectedColumns.add(sel);
            }
          }
        }
      }
    }
    return true;
  }
  
  /**
   * Returns the index on the panel of the given column chooser.
   * @param jc - the column chooser to search for.
   * @return - the index of the given column chooser on it's parent
   * panel (0 is the first element, 1 the second and so on).
   */
  public int getIndexOfColumnChooser(JColumnChooser jc) {
    for (int j=0; j<2; j++) {
      JPanel p;
      if (j==0) p=requiredPanel; else p=optionalPanel;
      
      if (p!=null) {
        for (int i=0; i<p.getComponentCount(); i++) {
          if (p.getComponent(i).equals(jc)) return  i;
        }
      }
    }
    
    return -1;
  }
  
  /**
   * Split between required and optional columns and set
   * a titled border.
   * @param b
   */
  public void setSplitRequiredAndOptional(boolean b) {
    if (b) {
      setBorder(requiredPanel, "Required columns ");
      setBorder(optionalPanel, "Optional columns ");
    } else {
      requiredPanel.setBorder(null);
      optionalPanel.setBorder(null);
    }
  }
  private void setBorder(JComponent c, String title) {
    TitledBorder tb2 = new TitledBorder(title);
    c.setBorder(tb2);
  }
  
  /**
   * Add multiple column choosers.
   * @param titles of the JColumnChooser and caption of the describing label.
   * @param required - if it is required to select a value.
   * @param showExamples - shows a preview, based on the first data row in the CSV file.
   */
  public void addColumnChooser(String[] titles, boolean required, boolean showExamples) {
    for (String s: titles) {
      addColumnChooser(s, required, showExamples);
    }
  }
  /**
   * Add a single column chooser
   * @param title of the JColumnChooser and caption of the describing label.
   * @param required - if it is required to select a value.
   * @param showExamples - shows a preview, based on the first data row in the CSV file.
   * @return the JColumnChooser that has just been created.
   */
  public JColumnChooser addColumnChooser(String title, boolean required, boolean showExamples) {
    
    JColumnChooser jc = createColumnChooser(title, required, showExamples);
    addColumnChooser(jc);
    
    return jc;
  }
  
  /**
   * Adds the given column chooser to the corresponding panel.
   * @param jc - The JColumnChooser to add.
   * @param required - add it to the required or optional panel.
   */
  public void addColumnChooser(JColumnChooser jc) {
    if (jc==null) return;
    if (requiredPanel==null || optionalPanel==null) initGUI();
    JPanel target = jc.isRequired()?requiredPanel:optionalPanel;
    target.add(jc);
    setPrefferedSizes(0);
  }
  
  /**
   * Adds the given column chooser to the corresponding panel at
   * the specified index.
   * @param jc - The JColumnChooser to add.
   * @param index - The position where the column chooser should be added
   * (from top to bottom, 0 is the first element, 1 the second,...).
   */
  public void addColumnChooser(JColumnChooser jc, int index) {
    if (jc==null) return;
    if (requiredPanel==null || optionalPanel==null) initGUI();
    JPanel target = jc.isRequired()?requiredPanel:optionalPanel;
    target.add(jc, index);
    setPrefferedSizes(0);
  }
  
  /**
   * Creates a new Column Chooser WITHOUT adding it to any panel.
   * @param title of the JColumnChooser and caption of the describing label.
   * @param required - if it is required to select a value.
   * @param showExamples - shows a preview, based on the first data row in the CSV file.
   * @return the JColumnChooser that has just been created.
   */
  public JColumnChooser createColumnChooser(String title, boolean required, boolean showExamples) {
    JColumnChooser jc = new JColumnChooser(title, required, r.getHeader(), firstLine);
    jc.setName(title);
    jc.setHeaders(r.getHeader(), r.getNumberOfColumns());
    jc.setUsePreview(showExamples);
    return jc;
  }
  
  /**
   * Add a single column chooser
   * @param title of the JColumnChooser and caption of the describing label.
   * @param defaultValue - default column number for this column chooser.
   * @param required - if it is required to select a value.
   * @param showExamples - shows a preview, based on the first data row in the CSV file.
   * @return the JColumnChooser that has just been created.
   */
  public JColumnChooser addColumnChooser(String title, int defaultValue, boolean required, boolean showExamples) {
    JColumnChooser jc  = addColumnChooser(title, required, showExamples);
    jc.setDefaultValue(defaultValue);
    return jc;
  }
  
  /**
   * Replaces an existing column chooser with a new one.
   * Automatically repaints the panel.
   * @param oldOne - column chooser to remove
   * @param newOne - column chooser to add
   */
  public void replaceColumnChooser(JColumnChooser oldOne, JColumnChooser newOne) {    
    GUITools.replaceComponent(oldOne, newOne);
    
    newOne.getParent().validate();
    newOne.getParent().repaint();
  }
  
  
  
  /**
   * Just for testing purposes.
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      CSVReaderColumnChooser c = new CSVReaderColumnChooser("files/sample.csv.txt");
      c.addColumnChooser(new String[]{"sequenceStart","sequenceEnd"}, true, true);
      c.addColumnChooser("sequenceName",1, false, true);
      c.addColumnChooser("test",1, true, true);
      frame.getContentPane().add(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
    frame.pack();
    frame.setVisible(true);
  }
  
}
