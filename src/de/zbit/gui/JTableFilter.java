package de.zbit.gui;

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.Option;

/**
 * Creates a panel for a {@link JTable} that lets users choose
 * filters to choose a subset of rows from the underlying table.
 * @author Clemens Wrzodek
 */
public class JTableFilter extends JPanel {
  private static final long serialVersionUID = 6976706059419605006L;

  /** bundle to get the localized "COLUMN" String **/
  protected static ResourceBundle bundle = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
  
  private final static String regexString = "Regular expression";
  
  /**
   * The underlying table
   */
  private JTable table;
  
  /**
   * A thread that counts the number of selected objects.
   */
  private Thread counter = null;

  JComboBox header1;
  JComboBox operator1;
  JTextField text1;
  
  JRadioButton and;
  JRadioButton or;
  
  JComboBox header2;
  JComboBox operator2;
  JTextField text2;
  
  JLabel preview;
  
  /**
   * [COLUMN] [Chooser "=",">=",">",...] [TEXT (DOUBLE-BOX)]<br/>
   * (und) (oder)<br/>
   * [COLUMN] [Chooser "=",">=",">",...] [TEXT (DOUBLE-BOX)]<br/>
   * {string} "x objects in current filter group".<br/>
   * 
   * @param table
   */
  public JTableFilter(JTable table) {
    super();
    this.table = table;
    
    initGUI();
  }
  
  private void initGUI() {
    LayoutHelper lh = new LayoutHelper(this);
    
    header1 = new JComboBox(getHeaders(table));
    operator1 = new JComboBox(getOperators());
    text1 = new JTextField(15);
    lh.add(header1, operator1, text1);
    
    and = new JRadioButton(
        StringUtil.changeFirstLetterCase(bundle.getString("AND"), true, true));
    or = new JRadioButton(
        StringUtil.changeFirstLetterCase(bundle.getString("OR"), true, true));
    ButtonGroup group = new ButtonGroup();
    and.setSelected(true);
    group.add(and);
    group.add(or);
    lh.add(and, or);
    
    header2 = new JComboBox(getHeaders(table));
    operator2 = new JComboBox(getOperators());
    text2 = new JTextField(15);
    lh.add(header2, operator2, text2);
    
    preview = new JLabel(" ");
    preview.setName("");
    lh.add(preview);
    
    // Add listeners to refresh the preview
    ItemListener il = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        queueCounter();
      }
    };
    DocumentListener dl = new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        queueCounter(); 
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        queueCounter();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        queueCounter();
      }
    };
    
    for (Component c: getComponents()) {
      if (c instanceof ItemSelectable) {
        // Buttons ands JComboBoxes
        ((ItemSelectable)c).addItemListener(il);
      } else if (c instanceof JTextComponent) {
        // JTextFields
        ((JTextComponent)c).getDocument().addDocumentListener(dl);
      }
    }
    
    queueCounter();
  }
  
  /**
   * A value that represents the state of all
   * items on this panel.
   * @return 101 if nothing is selected or entered.
   */
  private int currentStateIdentifier() {
    int id = 0;
    id += and.isSelected()?1:0;
    id += text1.getText().hashCode();
    id += text2.getText().hashCode();
    id += header1.getSelectedIndex()+10;
    id += header2.getSelectedIndex()+20;
    id += operator1.getSelectedIndex()+30;
    id += operator2.getSelectedIndex()+40;
    return id;
  }
  
  private void queueCounter() {
    // Do we have to recalculate ?
    int id = currentStateIdentifier();
    if (preview.getName().equals(Integer.toString(id))) return;
    preview.setName(Integer.toString(id));
    
    // Are we already recalculating?
    if (counter!=null) counter.interrupt();
    
    // Queue the refresh.
    counter = new Thread() {
      public void run() {
        int hits = getSelectedRowCount();
        if (Thread.currentThread().isInterrupted()) return;
        preview.setText(String.format("%s objects in current filter group.", hits));
      }
    };
    preview.setText("Calculating number of objects in current filter group.");
    counter.start();
          
  }
  
  public int getSelectedRowCount() {
    String c1 = operator1.getSelectedItem().toString();
    String c2 = operator2.getSelectedItem().toString();
    if (c1=="" && c2=="") {
      return table.getRowCount();
    } else {
      List<Integer> l = getSelectedRows();
      return l!=null?l.size():0;
    }
  }
  
  public List<Integer> getSelectedRows() {
    LinkedList<Integer> ret = new LinkedList<Integer>();

    // Get selected values
    String c1 = operator1.getSelectedItem().toString();
    String c2 = operator2.getSelectedItem().toString();
    int col1 = header1.getSelectedIndex();
    int col2 = header2.getSelectedIndex();
    String filter1 = text1.getText();
    String filter2 = text2.getText();
    boolean or = this.or.isSelected();

    // Prepare filter, parse if numeric.
    Object f1=filter1;
    Object f2=filter2;
    try {
      if (c1.length()>0 && !c1.equals(regexString)) {
        if (table.getRowCount()>0) {
          Object v = table.getValueAt(0, col1);
          if (Number.class.isAssignableFrom(v.getClass())) {
            // Create same datatype as column contains!
            f1=Option.parseOrCast(v.getClass(), f1);
            if (!Number.class.isAssignableFrom(f1.getClass())) {
              // Reset operator if number could not be parsed.
              c1="";
            }
          }
        }
      } else if (c1.equals(regexString)){
        f1 = Pattern.compile(filter1);
      }
      if (c2.length()>0 && !c2.equals(regexString)) {
        if (table.getRowCount()>0) {
          Object v = table.getValueAt(0, col2);
          if (Number.class.isAssignableFrom(v.getClass())) {
            // Create same datatype as column contains!
            f2=Option.parseOrCast(v.getClass(), f2);
            if (!Number.class.isAssignableFrom(f2.getClass())) {
              // Reset operator if number could not be parsed.
              c2="";
            }
          }
        }
      } else if (c2.equals(regexString)){
        f2 = Pattern.compile(filter2);
      }
    } catch (Exception e) {
      // Column contains integers and user types e.g. > "A".
      return ret;
    }
    
    // Preprocess: Always c1 must be selected
    if (c1.equals("") && !c2.equals("")) {
      c1 = c2;
      f1 = f2;
      col1 = col2;
      c2="";
    }

    // Match all rows and store matcheds rows in list.
    for (int i=0; i<table.getRowCount(); i++) {
      if (Thread.currentThread().isInterrupted()) return null;
      if (!c1.equals("")) {
        Object val = table.getValueAt(i, col1);
        if (matchOperator(c1, val, f1)) {
          if (or) {
            ret.add(i);
            continue;
          }
        } else if (!or) {
          // "and" and first condition does not match.
          continue;
        }
        
        if (c2.equals("") || matchOperator(c2, table.getValueAt(i, col2), f2)) {
          ret.add(i);
        }
        
      } else {
        ret.add(i);
      }
    }
    
    return ret;
  }

  public static String[] getOperators() {
    // TODO: Sch�ner w�re ala excep "entspricht", "entspricht nicht",...
    // erm�glicht auch "beginnt mit", etc.
    // �nderungen hier unbedingt mit matchOperator() abgleichen!
    return new String[]{"","=", "!=", ">=","<=",">","<"
        , "|>=|","|<=|","|>|","|<|", regexString};
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean matchOperator(String operator, Object val1, Object val2) {
    // Assumes Strings already pared to numbers, if desired.
    // Assumes a compiled pattern for regex strings
    
    if (operator.equals("")) {
      return true;
    } else if (operator.equals(regexString)) {
      return ((Pattern)val2).matcher(val1.toString()).matches();
    }
    
    // Comparables coming... ensure comparable from here on!
    if (!(val1 instanceof Comparable)) {
      val1=val1.toString();
      val2=val2.toString();
    }
    

    if (operator.equals("=")) {
      return (((Comparable) val1).compareTo(val2)==0);
    } else if (operator.equals("!=")) {
      return (((Comparable) val1).compareTo(val2)!=0);
    } else if (operator.equals(">=")) {
      return (((Comparable) val1).compareTo(val2)>=0);
    } else if (operator.equals("<=")) {
      return (((Comparable) val1).compareTo(val2)<=0);
    } else if (operator.equals(">")) {
      return (((Comparable) val1).compareTo(val2)>0);
    } else if (operator.equals("<")) {
      return (((Comparable) val1).compareTo(val2)<0);
    }
    
    // Absoluate values coming...
    if (val1 instanceof Number) {
      // should always be true
      val1 = Math.abs(((Number)val1).doubleValue());
      val2 = Math.abs(((Number)val2).doubleValue());
    }

    if (operator.equals("|>=|")) {
      return (((Comparable) val1).compareTo(val2)>=0);
    } else if (operator.equals("|<=|")) {
      return (((Comparable) val1).compareTo(val2)<=0);
    } else if (operator.equals("|>|")) {
      return (((Comparable) val1).compareTo(val2)>0);
    } else if (operator.equals("|<|")) {
      return (((Comparable) val1).compareTo(val2)<0);
    }

    return false;
  }

  /**
   * Returns the headers of the table as array. If the table
   * has no headers, a list of Strings "Column X" will be
   * returned.
   * @param table
   * @return
   */
  public static Object[] getHeaders(JTable table) {
    String column = bundle.getString("COLUMN");
    if (table.getTableHeader()==null ||
        table.getTableHeader().getColumnModel()==null) {
      String[] ret = new String[table.getColumnCount()];
      for (int i=1;i<=ret.length; i++) {
        ret[i-1] = String.format("%s %s", column, i);
      }
      return ret;
    } else {
      TableColumnModel m = table.getTableHeader().getColumnModel();
      int max = Math.max(m.getColumnCount(), table.getColumnCount());
      Object[] ret = new Object[max];
      for (int i=0;i<max; i++) {
        Object s = null;
        if (i<m.getColumnCount()) s = m.getColumn(i).getHeaderValue();
        if (s!=null && s.toString().length()>0) {
          ret[i]=s;
        } else {
          ret[i] = String.format("(%s %s)", column, (i+1));
        }
      }
      return ret;
    }
    
  }
  
  
  public static JTableFilter showDialog(Component parent, JTable r) {
    return showDialog(parent, r, null);
  }
  public static JTableFilter showDialog(Component parent, JTable r, String title) {
    if (title==null) title = UIManager.getString("OptionPane.titleText");
    
    final JTableFilter c = new JTableFilter(r);
    int ret = JOptionPane.showConfirmDialog(parent, c, title, JOptionPane.OK_CANCEL_OPTION);
    if (ret==JOptionPane.OK_OPTION) {
      return c;
    } else {
      return null;
    }
  }
  
  
  public static void main(String[] args) {
    Object[][] row = new Object[][]{
        {-1,"Hallo"},
        {-2,"allo"},
        {-3,"llo"},
        {-4,"lo"},
        {-5,"o"},
        
        {1,"Hallo"},
        {2,"allo"},
        {3,"llo"},
        {4,"lo"},
        {5,"o"}};
    
    JTable demo = new JTable(row, new String[]{"", "Name"});
    showDialog(null, demo);
  }
  

}
