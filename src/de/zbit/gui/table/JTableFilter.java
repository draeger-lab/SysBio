/*
 * $Id: package-info.java 994 2012-08-31 13:56:13Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/util/package-info.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.table;

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

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
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

  /** Bundle to get localized Strings. **/
  protected static ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
  
  /**
   * String and identifier for arbitrary regular expressions.
   */
  private final static String regexString = bundle.getString("REGULAR_EXPRESSION");
  
  /**
   * The underlying table
   */
  private JTable table;
  
  /**
   * A thread that counts the number of selected objects.
   */
  private Thread counter = null;

  JLabel description;
  
  JComboBox header1;
  JComboBox operator1;
  JTextField text1;
  
  JRadioButton and;
  JRadioButton or;
  JRadioButton andNot;
  
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
    
    description = new JLabel();
    lh.add(description, 3, true);
    
    header1 = new JComboBox(getHeaders(table));
    operator1 = new JComboBox(getOperators());
    text1 = new JTextField(15);
    lh.add(header1, operator1, text1);
    
    and = new JRadioButton(
        StringUtil.changeFirstLetterCase(bundle.getString("AND"), true, true));
    or = new JRadioButton(
        StringUtil.changeFirstLetterCase(bundle.getString("OR"), true, true));
    andNot = new JRadioButton(StringUtil.changeFirstLetterCase(
      String.format("%s %s", bundle.getString("AND"), bundle.getString("NOT")), true, true));
    ButtonGroup group = new ButtonGroup();
    and.setSelected(true);
    group.add(and);
    group.add(or);
    group.add(andNot);
    lh.add(and, or, andNot);
    
    header2 = new JComboBox(getHeaders(table));
    operator2 = new JComboBox(getOperators());
    text2 = new JTextField(15);
    lh.add(header2, operator2, text2);
    
    preview = new JLabel(" ");
    preview.setName("");
    lh.add(preview,3);
    
    // Add listeners to refresh the preview
    ItemListener il = new ItemListener() {
    /*
     * 	(non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
      public void itemStateChanged(ItemEvent e) {
        queueCounter();
      }
    };
    DocumentListener dl = new DocumentListener() {
      /*
       * (non-Javadoc)
       * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
       */
      public void removeUpdate(DocumentEvent e) {
        queueCounter(); 
      }
      /*
       * (non-Javadoc)
       * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
       */
      public void insertUpdate(DocumentEvent e) {
        queueCounter();
      }
      /*
       * (non-Javadoc)
       * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
       */
      public void changedUpdate(DocumentEvent e) {
        queueCounter();
      }
    };
    
    // Add listeners to every item
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
    if (and.isSelected()) {
      id+=1;
    } else if (andNot.isSelected()) {
      id+=2;
    } else { // or
      //id+=0; //... not necessary ;-)
    }
    
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
    	/* (non-Javadoc)
    	 * @see java.lang.Thread#run()
    	 */
    	@Override
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
    boolean andNot = this.andNot.isSelected();

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

      
      if (!c1.equals("")) { // Any operator selected
        Object val = table.getValueAt(i, col1);
        
        // Some columns contain rows in the order "N/A", "0.1, "0.2",...
        // then, initially f1 is a String, because the first row ("N/A")
        // is a string. Hence, eventually cast to Number later on.
        if (val!=null && !Number.class.isAssignableFrom(f1.getClass()) && 
            Number.class.isAssignableFrom(val.getClass())) {
          f1=Option.parseOrCast(val.getClass(), f1);
        }
        
        if (matchOperator(c1, val, f1)) { // 1st condition ok
          if (or || c2.equals("")) {
            ret.add(i);
            continue; // don't have to check 2nd condition
          }
        } else if (!or) {
          // "and" and first condition does not match.
          // => don't even look at 2nd one.
          continue;
        }
        
        // Check 2nd condition
        boolean secondConditionOk = !c2.equals("");
        if (secondConditionOk) { // if anything selected
          
          Object val2 = table.getValueAt(i, col2);
          
          // Some columns contain rows in the order "N/A", "0.1, "0.2",...
          // then, initially f1 is a String, because the first row ("N/A")
          // is a string. Hence, eventually cast to Number later on.
          if (val2!=null && !Number.class.isAssignableFrom(f2.getClass()) && 
              Number.class.isAssignableFrom(val2.getClass())) {
            f2=Option.parseOrCast(val2.getClass(), f2);
          }
          
          secondConditionOk = matchOperator(c2, val2, f2);
          if (andNot) secondConditionOk = !secondConditionOk;
        }
        if (secondConditionOk) {
          // either nothing selected or really ok.
          ret.add(i);
        }
        
      } else {
        // No condition selected in 1st condition => add all
        ret.add(i);
      }
    }
    
    return ret;
  }

  /**
   * Currently:
   * "","=", "!=", ">=","<=",">","<"
        , "|>=|","|<=|","|>|","|<|", regexString
   * @return
   */
  public static String[] getOperators() {
    // TODO: Schöner wäre ala excel "entspricht", "entspricht nicht",...
    // ermöglicht auch "beginnt mit", etc.
    // Änderungen hier unbedingt mit matchOperator() abgleichen!
    return new String[]{"","=", "!=", ">=","<=",">","<"
        , "|>=|","|<=|","|>|","|<|", regexString};
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean matchOperator(String operator, Object val1, Object val2) {
    // Assumes Strings already pared to numbers, if desired.
    // Assumes a compiled pattern for regex strings
    
    if (operator.equals("")) {
      return false;
    } else if (operator.equals(regexString)) {
      return ((Pattern)val2).matcher(val1.toString()).matches();
    }
    
    // Comparables coming... ensure comparable from here on!
    boolean bothToString = false;
    if (!(val1 instanceof Comparable) ||
        !(val2 instanceof Comparable)) {
      bothToString = true;
    }
    // Both must be the same comparable (if one is number, the
    // other one can't be a string!)
    if (val1 instanceof Number) {
      if (!(val2 instanceof Number)) {
        Object Nval2 = toNumber(val2);
        if (Nval2==null) {
          bothToString = true;
        } else {
          val2 = Nval2;
        }
      }
    } else if (val2 instanceof Number) {
      if (!(val1 instanceof Number)) {
        Object Nval1 = toNumber(val1);
        if (Nval1==null) {
          bothToString = true;
        } else {
          val1 = Nval1;
        }
      }
    }
    if (!val1.getClass().isAssignableFrom(val2.getClass())) {
      bothToString = true;
    }
    
    // Avoid comparisons with NaN's
    if (val2 instanceof Number) {
      if (Double.isNaN(((Number)val2).doubleValue())) {
        return false;
      }
    } if (val1 instanceof Number) {
      if (Double.isNaN(((Number)val1).doubleValue())) {
        return false;
      }
    }
      
    // If classes are incompatible, make string comparisons.
    if (bothToString) {
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
    
    // ABSOLUTE VALUES coming...
    if (val1 instanceof Number || val2 instanceof Number) {
      // should always be true
      val1 = toAbsoluteNumber(val1);
      val2 = toAbsoluteNumber(val2);
      if (val1==null || val2==null || 
          Double.isNaN(((Number)val1).doubleValue()) || Double.isNaN(((Number)val2).doubleValue()) ) {
        // incompatible data types.
        return false;
      }
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
   * @param val1
   * @return
   */
  private Object toNumber(Object val1) {
    if (val1 instanceof Number) {
      return ((Number) val1).doubleValue();
    }
    
    try {
      val1 = ((Number)val1).doubleValue();
    } catch (Exception e) {
      try {
        val1 = Double.parseDouble(val1.toString());
      } catch (Exception e2) {
        //e2.printStackTrace(); //Mostly 'empty string'
        //return false;
        val1 = null;
      }
    }
    return val1;
  }
  
  private Object toAbsoluteNumber(Object val1) {
    if (val1 instanceof Number) {
      return Math.abs(((Number) val1).doubleValue());
    }
    
    try {
      val1 = Math.abs(((Number) val1).doubleValue());
    } catch (Exception e) {
      try {
        val1 = Math.abs(Double.parseDouble(val1.toString()));
      } catch (Exception e2) {
        //e2.printStackTrace(); //Mostly 'empty string'
        //return false;
        val1 = null;
      }
    }
    return val1;
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
    final JTableFilter c = new JTableFilter(r);
    return showDialog(parent, c, title);
  }
  public static JTableFilter showDialog(Component parent, final JTableFilter c, String title) {
    if (title==null) title = UIManager.getString("OptionPane.titleText");

    boolean allOk = false;
    int ret=JOptionPane.OK_OPTION;
    while(!allOk) {
      ret = JOptionPane.showConfirmDialog(parent, c, title, JOptionPane.OK_CANCEL_OPTION);
      allOk = ret!=JOptionPane.OK_OPTION || (ret==JOptionPane.OK_OPTION && c.checkSelectionAndRaiseWarnings());
    }
    
    if (ret==JOptionPane.OK_OPTION) {
      return c;
    } else {
      return null;
    }
  }
  
  /**
   * Set an initial selection
   * @param col1Header initial selected column header
   * @param condition1 see {@link #getOperators()}
   * @param filter1 a filter string
   */
  public void setInitialSelection(String col1Header, String condition1, String filter1) {
    header1.setSelectedItem(col1Header);
    operator1.setSelectedItem(condition1);
    text1.setText(filter1);
    queueCounter();
  }
  
  /**
   * Show a description on top of the selectors.
   * @param text
   */
  public void setDescribingLabel(String text) {
    description.setText(text);
  }
  
  /**
   * Set an initial selection
   * @param col1Header initial selected column header
   * @param condition1 see {@link #getOperators()}
   * @param filter1 a filter string
   * @param conjunction 0 for and, 1 for or and 2 for andNot
   * @param col2Header
   * @param condition2
   * @param filter2
   */
  public void setInitialSelection(String col1Header, String condition1, String filter1, int conjunction, String col2Header, String condition2, String filter2) {
    header1.setSelectedItem(col1Header);
    operator1.setSelectedItem(condition1);
    text1.setText(filter1);    
    
    and.setSelected(conjunction<=0);
    or.setSelected(conjunction==1);
    andNot.setSelected(conjunction>1);
    
    header2.setSelectedItem(col2Header);
    operator2.setSelectedItem(condition2);
    text2.setText(filter2);
    queueCounter();
  }
  
  
  /**
   * Checks the user selection and shows warning messages if something is wrong.
   * @return true if everything is fine. False else.
   */
  public boolean checkSelectionAndRaiseWarnings() {
    String c1 = operator1.getSelectedItem().toString();
    String c2 = operator2.getSelectedItem().toString();
    String filter1 = text1.getText();
    String filter2 = text2.getText();
    
    String message=null;
    if (c1.length()<1 && filter1.trim().length()>0 ) {
      message = "Please select an operator (=, >, etc.) for the first condition";
    } else if (c1.length()>0 && filter1.trim().length()<1) {
      message = "Please enter a filter string for the first condition.";
    } else if (filter2.trim().length()>0 && c2.length()<1 ) {
      message = "Please select an operator (=, >, etc.) for the second condition";
    } else if (c2.length()>1 &&  filter2.trim().length()<1) {
      message = "Please enter a filter string for the second condition.";
    }
    if (message!=null) {
      GUITools.showErrorMessage(this, message);
      return false;
    }
    
    return true;
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
