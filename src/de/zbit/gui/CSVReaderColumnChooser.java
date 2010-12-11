/**
 *
 * @author wrzodek
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import de.zbit.io.CSVReader;
import de.zbit.util.StringUtil;

/**
 * Builds a panel to asign columns from the CSVReader to certain subjects.
 * 
 * For example, if you open a CSV file and want to ask the user for the
 * columns for sequenceStart, sequenceEnd, strand and sequenceName,
 * but the sequenceName is optional, you can easily create a dialog with
 * the following command:
 * 
 * <pre>
 *    CSVReaderColumnChooser c = new CSVReaderColumnChooser("file.csv.txt");
 *    c.addColumnChooser(new String[]{"sequenceStart","sequenceEnd"}, true, true);
 *    c.addColumnChooser(new String[]{"sequenceName"}, false, true);
 * </pre>
 * 
 * @author wrzodek
 */
public class CSVReaderColumnChooser extends JPanel {
  private static final long serialVersionUID = -7944793539696555890L;
  
  private CSVReader r;
  
  /**
   * This String array is used for the previews.
   */
  private String[] firstLine=null;
  
  private int prefferedWidth=0;
  /**
   * If unsorted, the columns appear as they appear in the file. Else,
   * they are sorted alphabetically.
   */
  private boolean sortHeaders=false;
  
  /**
   * Every integer added here corresponds to one column number. If an
   * integer is added, the column will be hidden in all ColumnChoosers.
   */
  private ArrayList<Integer> hideColumns = new ArrayList<Integer>();
  
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
    if (firstLine==null) throw new IOException("Invalid or empty CSV file.");
    
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
  
  /*
   * (non-Javadoc)
   * @see java.awt.Component#repaint()
   */
  @Override
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
    if (optionalPanel!=null) optionalPanel.setVisible(optionalPanel.getComponentCount()>0);
    if (requiredPanel!=null) requiredPanel.setVisible(requiredPanel.getComponentCount()>0);
    
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
   * Decide, wether you want the headers to appear sorted, or not.
   * @param sort - True: headers appear sorted. False: headers appear
   * in the same ordering as they appear in the file.
   */
  public void setSortHeaders(boolean sort) {
    if (sortHeaders != sort) {
      sortHeaders = sort;
      
      for (JColumnChooser jc: getColumnChoosers()) {
        jc.setSortHeaders(sort);
      }
      // Repaint is done by column chooser itself.
    }
  }
  
  /**
   * Set the visibility of certain columns. This will hide the
   * header of the column with the given number, so the user can't
   * choose that column.
   * Try to set multiple columns at once, because calling this class
   * will also lead to refreshing and repainting the column chooser.
   * @param columnNumbers - from 0 to n. 
   * @param visible - true or false. Default: true (visible).
   */
  public void setHeaderVisible(int columnNumber, boolean visible) {
    setHeaderVisible(new int[]{columnNumber}, visible);
  }
  
  /**
   * Set the visibility of certain columns. This will hide the
   * header of the column with the given number, so the user can't
   * choose that column.
   * Try to set multiple columns at once, because calling this class
   * will also lead to refreshing and repainting the column chooser.
   * @param columnNumbers - from 0 to n. 
   * @param visible - true or false. Default: true (visible).
   */
  public void setHeaderVisible(int[] columnNumbers, boolean visible) {
    
    // Change local list (for further column choosers)
    for (int i=0; i<columnNumbers.length; i++) {
      int pos = hideColumns.indexOf(columnNumbers[i]);
      if (pos>=0) {
        if (visible) hideColumns.remove(pos);
      } else {
        if (!visible) hideColumns.add(columnNumbers[i]);
      }
    }
    
    // Propagate changes to current column choosers
    for (JColumnChooser jc: getColumnChoosers()) {
      jc.setHeaderVisible(columnNumbers, visible);
    }
    // Repaint is done by column chooser itself.
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
   * @throws NoSuchElementException - if the title does not exist.
   */
  public int getSelectedValue(String title) throws NoSuchElementException {
    return  getColumnChooser(title).getSelectedValue();
  }
  
  /**
   * Returns the selected item (Usually a header string)
   * for the given title.
   * 
   * @param title - Must be the same as given in addColumnChooser().
   * @return selected item (header string).
   * @throws NoSuchElementException - if the title does not exist.
   */
  public Object getSelectedItem(String title) throws NoSuchElementException {
    return  getColumnChooser(title).getSelectedItem();
  }
  
  /**
   * Returns the JColumnChooser for the given title.
   * @param title - Must be the same as given in addColumnChooser().
   * @return JColumnChooser for the given title.
   * @throws NoSuchElementException - if the title does not exist.
   */
  public JColumnChooser getColumnChooser(String title) throws NoSuchElementException {
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
    throw new NoSuchElementException("No column chooser named '" + title + "'.");
  }
  
  /**
   * Get all JColumnChoosers.
   * @return ArrayList<JColumnChooser> containing all JColumnChoosers on this panel.
   */
  public Collection<JColumnChooser> getColumnChoosers() {
    ArrayList<JColumnChooser> ret = new ArrayList<JColumnChooser>();
    
    // Get all column choosers from both panels.
    for (int j=0; j<2; j++) {
      JPanel p;
      if (j==0) p=requiredPanel; else p=optionalPanel;
      
      if (p!=null) {
        for (int i=0; i<p.getComponentCount(); i++) {
          if (p.getComponent(i) == null) continue;
          if (p.getComponent(i) instanceof JColumnChooser) {
            ret.add ((JColumnChooser)p.getComponent(i));
          }
        }
      }
    }
    return ret;
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
   * Add multiple column choosers.
   * @param titles of the JColumnChooser and caption of the describing label.
   * @param defaults - integer array of the same size as titles, containing default columns.
   * @param required - if it is required to select a value.
   * @param showExamples - shows a preview, based on the first data row in the CSV file.
   */
  public void addColumnChooser(String[] titles, int[] defaults, boolean required, boolean showExamples) {
    for (int i=0; i<titles.length; i++) {
      addColumnChooser(titles[i], defaults[i], required, showExamples);
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
    configureColumnChooser(jc);
    
    // Add to panel
    JPanel target = jc.isRequired()?requiredPanel:optionalPanel;
    target.add(jc);
    
    setPrefferedSizes(0);
  }
  
  /**
   * Applys the current configuration (hidden headers, headers sorted)
   * to the given JColumnChooser.
   * @param jc
   */
  private void configureColumnChooser(JColumnChooser jc) {
    
    // Eventually hide columns
    int[] toHide = new int[hideColumns.size()];
    for (int i=0; i<hideColumns.size(); i++)
      toHide[i] = hideColumns.get(i);
    
    jc.setHeaderVisible(toHide, false);
    
    // Eventually sort columns
    jc.setSortHeaders(sortHeaders);
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
    configureColumnChooser(jc);
    
    // Add to panel
    JPanel target = jc.isRequired()?requiredPanel:optionalPanel;
    target.add(jc, index);
    
    setPrefferedSizes(0);
  }
  
  /**
   * Creates a new Column Chooser WITHOUT adding it to any panel.
   * Uses all settings from this class.
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
    configureColumnChooser(jc);
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
    
    
    if (newOne.getParent()!=null) {
      newOne.getParent().validate();
      newOne.getParent().repaint();
    }
  }
  
  /**
   * Convenient method to display a given ColumnChooser
   * on a JOptionPane.
   * @param c - CSVReaderColumnChooser to show.
   * @return JOptionPane.OK_OPTION || JOptionPane.CANCEL_OPTION
   */
	public static int showAsDialog(CSVReaderColumnChooser c) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(StringUtil.toHTML(String
				.format("Please assign the following columns:"), 60)),
				BorderLayout.NORTH);
		panel.add(c, BorderLayout.CENTER);

		if (c.getColumnChoosers().size() > 0) {
			return JOptionPane.showConfirmDialog(null, panel,
					"Column assignment", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		} else {
			return JOptionPane.OK_OPTION;
		}
	}


  /**
   * Just for testing purposes.
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      
      // Create the column chooser, based on a CSV file.
      CSVReaderColumnChooser c = new CSVReaderColumnChooser("files/sample2.csv.txt");
      
      
      // Add multiple columns to choose. Required=true, showPreview=true.
      c.addColumnChooser(new String[]{"sequenceStart","sequenceEnd"}, true, true);
      
      // Add a single column with a default selection. Required=false.
      c.addColumnChooser("sequenceName",3, false, true);
      
      // Add a single column, that is not required and show no preview.
      c.addColumnChooser("SBMLFile", false, false);
      
      // Try a few options
      c.setSortHeaders(true);
      c.setHeaderVisible(new int[]{5,6,7}, false);
      
      
      frame.getContentPane().add(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
    frame.pack();
    frame.setVisible(true);
  }
  
}
