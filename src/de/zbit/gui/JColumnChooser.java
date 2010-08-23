/**
 *
 * @author wrzodek
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import de.zbit.io.CSVReader;

/**
 * Column Chooser with a label, a columnChooser and a preview.
 * 
 * Very usefull e.g. to assign column headers of a csv file to
 * certain propoerties (e.g. asking the user "in which column
 * are the sequence start coordinates?").
 * 
 * @author wrzodek
 */
public class JColumnChooser extends JPanel {
  private static final long serialVersionUID = -9026612128266336630L;
  
  // Label options
  private String titel;
  private JLabel label;
  
  // Column selector options
  /**
   * Use a JTextField instead of JComboBoxes
   */
  private boolean useJTextField=false;
  private boolean required;
  private String[] headers=null;
  private ComboBoxModel model=null;
  private JComponent colChooser;
  
  // Preview options
  private boolean usePreview=true;
  private String[] previews=null;
  private JLabel preview;
  
  
  
  private static String noOptionChoosen="Not available";
  private static String exampleError="Invalid column";
  private static String noSelection="n/a";
  
  /**
   * Set the static string to use in optional combo boxes if no
   * column has been selected.
   * @param s
   */
  public static void setStringComboBoxNoOptionChoosen(String s) {
    noOptionChoosen = s;
  }
  /**
   * Set the static string to use for the preview in optional combo
   * boxes if no column has been selected.
   * @param s
   */
  public static void setStringExampleNoOptionChoosen(String s) {
    noSelection = s;
  }
  /**
   * Set the static string to use if the user selects a column
   * which is missing in the given preview.
   * @param s
   */
  public static void setStringExampleError(String s) {
    exampleError = s;
  }
  
  /**
   * Creates a new column chooser panel with the given headers.
   * The preview is used to give the user an example of the data
   * he has choosen. If null, no preview will be given.
   * @param title - Label caption for this column chooser
   * @param fieldIsRequired - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   * @param columnHeaders - Column Headers
   * @param preview - First data line to give examples of column data.
   */
  public JColumnChooser(String title, boolean fieldIsRequired, String[] columnHeaders, String[] preview) {
    super();
    initGUI();
    
    setTitle(title);
    setName(title);
    // setHeaders handles this variable.. so set it directly.
    this.required = fieldIsRequired;
    setHeaders(columnHeaders);    
    setPreview(preview);
    
    label.setLabelFor(colChooser);
    this.preview.setLabelFor(colChooser);
  }
  
  /**
   * Creates a new column chooser panel with the given headers.
   * To preview for the selected data will be given.
   * @param title - Label caption for this column chooser
   * @param fieldIsRequired - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   * @param columnHeaders - Column Headers
   */
  public JColumnChooser(String title, boolean fieldIsRequired, String[] columnHeaders) {
    this(title, fieldIsRequired, columnHeaders, null);
  }
  
  /**
   * Creates a new column chooser which let's the user choose
   * columns with JTextFields if no columnHeaders will be given.
   * @param title - Label caption for this column chooser
   * @param fieldIsRequired - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   */
  public JColumnChooser(String title, boolean fieldIsRequired) {
    this(title, fieldIsRequired, null);
  }
  
  private void initGUI() {
    this.setPreferredSize(new java.awt.Dimension(400, 25));
    //layout = new GridBagLayout();
    GridLayout layout = new GridLayout(1,3,10,2);
    this.setLayout(layout);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setLayout(LayoutManager manager) {
    super.setLayout(manager);
    layoutElements();
  }
  
  /**
   * {@inheritDoc}
   */
  public void setToolTipText(String s) {
    super.setToolTipText(s);
    if (colChooser!=null) colChooser.setToolTipText(s);
  }
  
  /**
   * @return if this is a required column chooser. If not required
   * this class will add a NoOptionChoosen String at the start of the box.
   */
  public boolean isRequired() {
    return this.required;
  }
  
  /**
   * Should only be called when the layout changed.
   */
  private void layoutElements() {
    LayoutManager l = getLayout();
    
    if (label!=null) l.removeLayoutComponent(label);
    if (colChooser!=null) l.removeLayoutComponent(colChooser);
    if (preview!=null) l.removeLayoutComponent(preview);
    
    if (l instanceof BorderLayout) {
      BorderLayout c = (BorderLayout) l;
      if (label!=null) c.addLayoutComponent(label, BorderLayout.WEST);
      if (colChooser!=null) c.addLayoutComponent(colChooser, BorderLayout.CENTER);
      if (preview!=null) c.addLayoutComponent(preview, BorderLayout.EAST);
      
    } else if (l instanceof GridBagLayout) {
      GridBagLayout c = (GridBagLayout) l;
      
      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0);
      
      if (label!=null) {
        gbc.gridx=0;
        c.addLayoutComponent(label, gbc);
      }
      if (colChooser!=null) {
        gbc.gridx=1;
        c.addLayoutComponent(colChooser, gbc);
      }
      if (preview!=null) {
        gbc.gridx=2;
        c.addLayoutComponent(preview, gbc);      
      }
    } else {
      if (label!=null) l.addLayoutComponent("Titel", label);
      if (colChooser!=null) l.addLayoutComponent("ColChooser", colChooser);
      if (preview!=null) l.addLayoutComponent("Preview", preview);
    }
  }
  
  
  /**
   * Set the headers to display in the combo box.
   * This function will fill the combo box with
   * "Column 1", "Column 2",...
   * @param numberOfColumns
   */
  public void setHeaders(int numberOfColumns) {
    setHeaders (null, numberOfColumns);
  }
  /**
   * Set the headers to display in the combo box.
   * This function will fill empty fields in the combo box
   * with "(Column i)".
   * if header is null, behaves like {@link #setHeaders(int)}.
   * @param header
   */
  public void setHeaders(String[] header) {
    int l = 0;
    if (header==null || header.length<1) {
      if (previews!=null) l = previews.length;
    }
    setHeaders(header, l);
  }
  /**
   * Set the headers to display in the combo box.
   * This function will fill empty fields in the combo box
   * with "(Column i)".
   * If numberOfColumns is greater than header.length, it will
   * extend the header to the numberOfColumns filling it wil
   * "(Column i)".
   * @param header
   * @param numberOfColumns
   */
  public void setHeaders(String[] header, int numberOfColumns) {
    
    // Set the header
    String[] newHeader=header;
    int maxSize = newHeader!=null?newHeader.length:previews!=null?previews.length:numberOfColumns;
    maxSize = Math.max(numberOfColumns, maxSize);
    newHeader = new String[maxSize];
    if (header!=null) System.arraycopy(header, 0, newHeader, 0, header.length);      
    for (int i=0; i<newHeader.length; i++) {
      // Completely empty array
      if (header==null) newHeader[i] = "Column " + (i+1);
      // Just fill missing gaps
      else if (newHeader[i]==null || newHeader[i].trim().length()<1) newHeader[i]= "(Column " + (i+1)+")";
    }
    
    // Remember required headers and build model
    headers = newHeader;
    model = buildComboBoxModel(newHeader);
    
    refreshSelector();
    validateRepaint();
  }
  
  private ComboBoxModel buildComboBoxModel(String[] newHeader) {
    // If not required, add noOptionChoosen
    if (!required) {
      String [] optional=null;
      optional = new String[newHeader.length+1];
      optional[0] = noOptionChoosen;
      System.arraycopy(newHeader, 0, optional, 1, newHeader.length);
      newHeader = optional;
    }
    
    // Build the model
    return new DefaultComboBoxModel(newHeader);
  }
  
  /**
   * Set the preview to display. If column i (header[i]) is
   * choosen, this will display preview[i] as preview. Set
   * to null to disable.
   *  
   * @param preview
   */
  public void setPreview(String[] preview) {
    this.previews = preview;
    refreshPreview();
    validateRepaint();
  }
  
  /**
   * @param required - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   */
  public void setRequired(boolean required) {
    this.required = required;
    model = buildComboBoxModel(this.headers);
    refreshSelector();
    validateRepaint();
  }
  
  /**
   * Sets the title (the label caption) for this column chooser.
   * @param title
   */
  public void setTitle(String title) {
    this.titel = title;
    refreshLabel();
    validateRepaint();
  }
  
  
  /**
   * If false, no preview field will be displayed.
   * @param b
   */
  public void setUsePreview(boolean b) {
    if (this.usePreview != b) {
      this.usePreview = b;
      preview.setVisible(usePreview);
      validateRepaint();
    }
  }
  
  /**
   * Set the default value of the column chooser.
   * Does account for required or optional settings
   * (adds 1 for optional).
   * 
   * @param i - index number
   */
  public void setDefaultValue(int i) {
    if (!required) i+=1;
    if (i<0) i=0;
    if (model!=null && i>=model.getSize()) i = model.getSize()-1;
    
    if (model!=null && i<model.getSize()) {
      model.setSelectedItem(model.getElementAt(i));
    }
    setSelectedValue(i);
  }
  
  /**
   * Set the default value of the column chooser.
   * @param s - set the default index to this value's index.
   */
  public void setDefaultValue(String s) {
    if (model!=null) {
      model.setSelectedItem(s);
    } else {
      if (CSVReader.isNumber(s, false)) {
        setDefaultValue(Integer.parseInt(s));
      }
    }
  }
  
  /**
   * Do you want to use a JTextField (true) of a JComboBox (false)
   * to choose the column? Default: JComboBox(false).
   * @param useTextField
   */
  public void setUseJTextField(boolean useTextField) {
    this.useJTextField = useTextField;
    refreshSelector();
    validateRepaint();
  }
  
  /**
   * Refresh the whole panel.
   */
  public void refresh() {
    refreshLabel();
    refreshSelector();
    refreshPreview();
    
    validateRepaint();
  }
  
  /**
   * Add any action Listener to the combo box.
   * Be careful: The action listeners are erased when the selector
   * changes (by setting new headers or refreshing).
   * @param l
   */
  public void addActionListener(ActionListener l) {
    if (colChooser instanceof JComboBox) {
      ((JComboBox)colChooser).addActionListener(l);
    } else if (colChooser instanceof JTextField) {
      ((JTextField)colChooser).addActionListener(l);
    }
  }
  
  private void refreshSelector() {
    int id = -1;
    if (colChooser!=null) {
      id = getSelectedValue();;
      remove(colChooser);
    }
    
    // Column chooser
    ActionListener al = createActionListener();
    colChooser = getColumnChooser(useJTextField?null:model, -1, required, al);
    colChooser.setToolTipText(getToolTipText());
    if (getLayout() instanceof GridBagLayout) {
      addComponent(this, colChooser, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    } else if (getLayout() instanceof BorderLayout) {
      add(colChooser, BorderLayout.CENTER);
    } else {
      // Remove and add preview to keep ordering (e.g. in gridlayout).
      if (preview!=null) remove(preview);
      add(colChooser); // e.g. GridLayout
      if (preview!=null) add(preview);
    } 
    
    if (id>=0) setSelectedValue(id);
    al.actionPerformed(null); // Set preview field
  }
  
  /**
   * Returns the selected index. Accounts automatically for required or optional.
   * @return integer between -1 and headers.length.
   */
  public int getSelectedValue() {
    if (colChooser==null) return -1;
    if (colChooser instanceof JComboBox) {
      if (required) return ((JComboBox)colChooser).getSelectedIndex();
      else return ((JComboBox)colChooser).getSelectedIndex()-1;
    } else {
      String s = ((JTextComponent)colChooser).getText().trim();
      
      if (CSVReader.isNumber(s, true)) return Integer.parseInt(s);
      else return -1;
    }
  }
  
  /**
   * Set the selected index to i. Does not account for required or
   * optional (add 1 for optional). Use {@link #setDefaultValue(String)}
   * or {@link #setDefaultValue(int))} to account for that.
   * @param i
   */
  public void setSelectedValue(int i) {
    if (colChooser instanceof JComboBox) {
      if (i>=0 && i<((JComboBox)colChooser).getModel().getSize())
        ((JComboBox)colChooser).setSelectedIndex(i);
    } else {
      ((JTextComponent)colChooser).setText(Integer.toString(i));
    }
  }
  
  private ActionListener createActionListener() {
    // Create action listener to refresh preview
    ActionListener previewAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (colChooser!=null && usePreview) {
          refreshPreview();
        }
      }      
    };
    
    return previewAction;
  }
  
  
  private void refreshPreview() {
    // Create it
    if (this.preview==null) {
      preview = new JLabel();
      if (getLayout() instanceof GridBagLayout) {
        addComponent(this, preview, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
      } else if (getLayout() instanceof BorderLayout) {
        add(preview, BorderLayout.EAST);
      } else {
        add(preview); // e.g. GridLayout
      }
    }
    if (usePreview!=preview.isVisible()) preview.setVisible(usePreview);
    if (!usePreview) return;
    
    // If TextBox then parse action command, else pase combo selection.
    int defaultValue = getSelectedValue();
    //if (colChooser instanceof JComboBox && !required) defaultValue-=1;
    //if (colChooser==null) defaultValue=0;
    if (previews==null || previews.length<1) {
      preview.setText("");
    } else if (defaultValue>=0 && defaultValue<previews.length) {
      preview.setText("E.g. \""+previews[defaultValue]+"\"");
    } else if (defaultValue<0) {
      preview.setText(noSelection);
    } else {
      preview.setText(exampleError);
    }
  }
  
  
  private void refreshLabel() {
    // Create it
    if (this.label==null) {
      label = new JLabel(this.titel);
      if (getLayout() instanceof GridBagLayout) {
        addComponent(this, label, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
      } else if (getLayout() instanceof BorderLayout) {
        add(label, BorderLayout.WEST);
      } else {
        add(label); // e.g. GridLayout
      }
    } else {
      label.setText(titel);
    }
  }
  
  private void validateRepaint() {
    validate();
    repaint();
  }
  
  @SuppressWarnings("unused")
  private static JComponent getColumnChooser(ComboBoxModel model) {
    return getColumnChooser(model, -1, false, null);
  }
  private static JComponent getColumnChooser(ComboBoxModel model, int defaultValue, boolean required, ActionListener l) {
    JComponent ret;
    
    if (model!=null && model.getSize()>0) {
      JComboBox cb = new JComboBox(model);
      // XXX: Feature possibility: remove the selected element in all other ComboBoxes.
      if (l!=null) cb.addActionListener(l);
      
      int def = defaultValue+(required?0:1);
      if (def<0 || def>=model.getSize()) def=0;
      cb.setSelectedIndex(def);
      ret = cb;
    } else {
      if (defaultValue<0) defaultValue=0;
      JTextField tf =CSVReaderOptionPanel.buildIntegerBox(Integer.toString(defaultValue), l);
      ret = tf;
    }
    
    return ret;
  }
  
  public static final Insets insets = new Insets(1,3,1,3);
  /**
   * Helper Method for GridBagConstrains.
   */
  public static void addComponent(Container container, Component component, int gridx, int gridy,
      int gridwidth, int gridheight, int anchor, int fill) {
    GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0,
        anchor, fill, insets, 0, 0);
    container.add(component, gbc);
  }
  
  /**
   * Just for testing purposes.
   */
  public static void main (String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JColumnChooser c = new JColumnChooser("test", false, new String[]{"header1", "header2"}, new String[]{"dataPreview1", "dataPreview2"});
      frame.getContentPane().add(c);
    } catch (Exception e) {
      e.printStackTrace();
    }
    frame.pack();
    frame.setVisible(true);
  }
  
  
}
