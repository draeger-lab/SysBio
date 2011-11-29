/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.csv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.zbit.gui.ExpandablePanel;
import de.zbit.gui.GUITools;
import de.zbit.gui.JLabeledComponent;
import de.zbit.gui.table.JComponentTableModel;
import de.zbit.gui.table.JComponentTableRenderer;
import de.zbit.gui.table.JTableRowBased;
import de.zbit.io.CSVReader;
import de.zbit.util.ArrayUtils;
import de.zbit.util.FileTools;
import de.zbit.util.ResourceManager;

/**
 * A graphical CSV Importer with very flexible column choosers.
 * <p>Please see the {@link #main(String[])} method for an example of
 * how to use this class.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CSVImporterV2 extends CSVReaderOptionPanel implements ActionListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -1351897174228755331L;

  /**
   * The file to import.
   */
  String inFile;
  
  /**
   * A collection, describing all expected columns.
   */
  List<ExpectedColumn> exCols;
  
  /**
   * If true, will hide all columns that can automatically be identified
   * with 100% certainty (exact column header match, or 100% of entries
   * match the given RegEx pattern (if
   * {@link ExpectedColumn#regExPatternForInitialSuggestion} is not null)).
   */
  boolean hideAutoIdentifiedColumns = false;

  /**
   * A field that is added to each combo box to allow ignoring columns.
   */
  private Object ignoreItem = "Ignore column";
  
  /**
   * This array holds the selections from {@link #exCols} for the column indices.
   */
  private Integer[] exColSelections = null;
  
  /**
   * This array holds the selected type for the column in the indices.
   */
  private Integer[] exColTypeSelections = null;
  
  /**
   * The {@link TableCellRenderer} and {@link ListCellRenderer} that is used
   * to draw {@link JComponent}s and display strings in {@link JLabel}s.
   */
  private JComponentTableRenderer rend = new JComponentTableRenderer();

  /**
   * A button that allows to rename expected column headers
   */
  private String renameButtonCaption = "Edit names";
  
  /**
   * 
   * @param reader
   * @throws IOException
   */
  private CSVImporterV2(CSVReader reader) throws IOException {
    super(reader, false);
    this.inFile = reader.getFilename();
  }
  
  /**
   * 
   * @param filepath
   * @throws IOException
   */
  private CSVImporterV2(String filepath) throws IOException {
    this(new CSVReader(filepath));
    this.inFile = filepath;
  }
  
  /**
   * 
   * @param filepath
   * @param expectedHeaders
   * @throws IOException
   */
  public CSVImporterV2(String filepath, String... expectedHeaders) throws IOException {
    this(filepath);
    exCols = new ArrayList<ExpectedColumn>(expectedHeaders.length);
    for(String s: expectedHeaders) {
      exCols.add(new ExpectedColumn(s));
    }
    init(exCols);
  }
  
  /**
   * 
   * @param filepath
   * @param expectedColumns
   * @throws IOException
   */
  public CSVImporterV2(String filepath, ExpectedColumn... expectedColumns) throws IOException {
    this(filepath, Arrays.asList(expectedColumns));
  }
  
  /**
   * 
   * @param reader
   * @param expectedColumns
   * @throws IOException
   */
  public CSVImporterV2(CSVReader reader, ExpectedColumn... expectedColumns) throws IOException {
    this(reader, Arrays.asList(expectedColumns));
  }
  
  /**
   * 
   * @param filepath
   * @param expectedColumns
   * @throws IOException
   */
  public CSVImporterV2(String filepath, Collection<ExpectedColumn> expectedColumns) throws IOException {
    this(filepath);
    init(expectedColumns);
  }
  
  /**
   * 
   * @param reader
   * @param expectedColumns
   * @throws IOException
   */
  public CSVImporterV2(CSVReader reader, Collection<ExpectedColumn> expectedColumns) throws IOException {
    this(reader);
    init(expectedColumns);
  }
  
  /**
   * 
   * @param expectedColumns
   * @throws IOException
   */
  private void init(Collection<ExpectedColumn> expectedColumns) throws IOException {
    if (expectedColumns instanceof List<?>) {
      exCols = (List<ExpectedColumn>) expectedColumns;
    } else {
      exCols = new ArrayList<ExpectedColumn>(expectedColumns);
    }
    
    // Prepare initial suggestions for columns
    int nc = getCSVReader().getNumberOfColumns();
    if (nc > 0) {
      exColSelections = new Integer[nc];
      Arrays.fill(exColSelections, 0);
      exColTypeSelections = new Integer[nc];
      Arrays.fill(exColTypeSelections, 0);
      
      // Put selections to lists
      /*for (int i=0; i<exColSelections.length; i++) {
        int selectedItem = exColSelections[i]-1;
        int selectedType = exColTypeSelections[i];
        if (selectedItem>=0) {
          exCols.get(selectedItem).assignedColumns.add(i);
          exCols.get(selectedItem).assignedTypeForEachColumn.add(selectedType);
        }
      }*/
      
      
       // Read eventual initial suggestions from exCols
      int i = 0;
      for (ExpectedColumn expectedColumn : expectedColumns) {
        // Old manual selections
        if (expectedColumn.hasAssignedColumns()) {
          for (int j=0; j<expectedColumn.getAssignedColumns().size(); j++) {
            int idx = expectedColumn.getAssignedColumns().get(j);
            if (idx>=0 && idx<exColSelections.length) {
              exColSelections[idx]=i+1;
              if (j<expectedColumn.getAssignedTypeForEachColumn().size()) {
                exColTypeSelections[idx]=expectedColumn.getAssignedTypeForEachColumn().get(j);
              }
            }
          }
          
        } else {
          // Auto-inference based on file content patterns
          int sug = expectedColumn.getInitialSuggestion(getCSVReader());
          if (sug >= 0) {
            exColSelections[sug] = i + 1; // +1 because "Ignore Column"
          }
        }
        i++;
      }
    }
    
    // display the GUI
    setPreferredSize(new Dimension(650, 400));
    initGUI();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.csv.CSVReaderOptionPanel#buildCSVOptionsPanel()
   */
  @Override
  public JPanel buildCSVOptionsPanel() {
    return new ExpandablePanel(getCSVOptionsString(), super.buildCSVOptionsPanel(),true,true);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.CSVReaderOptionPanel#buildPreview(int)
   */
  @Override
  public JComponent buildPreview(int numDataLines) throws IOException {
    JComponent base = super.buildPreview(numDataLines);
    JPanel p = new JPanel(new BorderLayout());
    p.add(base, BorderLayout.CENTER);
    if (isAColumnRequired()) {
      p.add(new JLabel("* = "
          + ResourceManager.getBundle("de.zbit.locales.Labels").getString(
            "REQUIRED_COLUMNS")), BorderLayout.SOUTH);
    }
    if (isRenamingAllowed()) {
      JButton but = new JButton(getRenameButtonCaption());
      but.setActionCommand("edit_names");
      but.addActionListener(this);
      p.add(but, BorderLayout.NORTH);
    }
    return p;
  }
  
  /**
   * @return
   */
  public String getRenameButtonCaption() {
    return renameButtonCaption;
  }
  
  /**
   * 
   * @param caption
   */
  public void setRenameButtonCaption(String caption) {
    renameButtonCaption = caption;
    refreshPreviewPanel();
  }

  /**
   * @return
   */
  private boolean isRenamingAllowed() {
    for (ExpectedColumn exCol : exCols) {
      if (exCol.renameAllowed) return true;
    }
    return false;
  }
  
  /**
   * @return current list of {@link ExpectedColumn}s.
   */
  public List<ExpectedColumn> getExpectedColumns() {
    return exCols;
  }

  /**
   * @return true if any ExpectedColumn from {@link #exCols}
   * has set the required attribute to true
   */
  private boolean isAColumnRequired() {
    for (ExpectedColumn exCol : exCols) {
      if (exCol.required) return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.CSVReaderOptionPanel#getFilePreviewCaption()
   */
  @Override
  protected String getFilePreviewCaption() {
    return "Please specify the content of each column";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.CSVReaderOptionPanel#buildPreviewTable(java.lang.String[][], java.lang.String[])
   */
  @Override
  protected JTable buildPreviewTable(String[][] data, String[] header) {
    Object[][] dataNew = prependColumnSelectors(data);
    final JTableRowBased table = new JTableRowBased(new JComponentTableModel(dataNew, header));
    rend.setDefaultForegroundColorForJLabels(Color.GRAY);
    int maxHeadRow = (isATypeSelectorRequired() ? 2 : 1);
    
    // Set an appropriate editor for the expected column and type selectors
    for (int row=0; row<maxHeadRow; row++) {
      for (int col=0; col<dataNew[row].length; col++) {
        Object cur = dataNew[row][col];
        
        TableCellEditor cellEditor;
        if (cur instanceof JCheckBox) {
          cellEditor = new DefaultCellEditor((JCheckBox)cur);
        }else if (cur instanceof JComboBox) {
          cellEditor = new DefaultCellEditor((JComboBox)cur);
        }else if (cur instanceof JTextField) {
          cellEditor = new DefaultCellEditor((JTextField)cur);
        } else {
          cellEditor = table.getCellEditor(0,col);
        }
        
        table.setCellEditor(row, col, cellEditor);      
      }
    }
    
    // Draw JComponents inside the JTable
    for (int i=0; i<table.getColumnCount(); i++) {
      table.getColumnModel().getColumn(i).setCellRenderer(rend);
    }
    
    // Set additional attributes
    if (defaultPreviewFont!=null) table.setFont(defaultPreviewFont);
    table.setPreferredScrollableViewportSize(new Dimension(500, 100));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    for (int i=0; i<maxHeadRow+1;i++)
      table.setRowHeight(i, (int) (table.getRowHeight(i)*1.3));
    
    // Disallow dragging columns
    table.getTableHeader().setReorderingAllowed(false);
    
    // Resize columns to a reasonable width
    if (table.getColumnModel().getColumnCount()>0)  {
      int width = table.getColumnModel().getColumn(0).getWidth();
      width = Math.max(width, 120);
      for (int i=0; i<table.getColumnModel().getColumnCount(); i++)
        table.getColumnModel().getColumn(i).setPreferredWidth(width); 
    }
    
    return table;
  }
  
  /**
   * @param data
   * @return
   */
  private Object[][] prependColumnSelectors(String[][] data) {
    if (data.length<1) return new Object[][]{{"Invalid settings."}};
    
    // Create Column Selectors
    JComponent[][] colSel=buildColumnSelectors(data[0].length);
    
    // Merge ColumnSelectors with table content
    Object[][] d = new Object[data.length+colSel.length][];
    System.arraycopy(colSel, 0, d, 0, colSel.length);
    System.arraycopy(data, 0, d, colSel.length, data.length);
    
    return d;
  }

  /**
   * Builds the {@link JComboBox}es that let the user assign an
   * expected column and a type to each column. 
   * @param numCols
   * @return
   */
  private JComponent[][] buildColumnSelectors(int numCols) {
    boolean ts_required = isATypeSelectorRequired();
    exColSelections = (Integer[])ArrayUtils.resize(exColSelections, numCols, 0, true);
    exColTypeSelections = (Integer[])ArrayUtils.resize(exColTypeSelections, numCols, 0, true);
    
    // Build JComboBoxes for each column to let the user select the content.
    JComponent[][] ret = new JComponent[ts_required?2:1][numCols];
    for (int i=0; i<numCols; i++) {
      if (exColSelections[i]==null)exColSelections[i] = 0;
      if (exColTypeSelections[i]==null)exColTypeSelections[i] = 0;
      
      final int i_copy = i;
      final JComboBox box = new JComboBox(getExpectedColsComboBoxModel());
      if (defaultPreviewFont!=null) box.setFont(defaultPreviewFont);
      box.setSelectedIndex(exColSelections[i]);
      //box.setRenderer(rend);
      ret[0][i] = box;
      
      // Type selector required?
      final JComboBox typeBox;
      if (ts_required) {
        typeBox = new JComboBox();
        if (defaultPreviewFont!=null) typeBox.setFont(defaultPreviewFont);
        typeBox.setEnabled(false);
        //typeBox.setRenderer(rend);
        
        // Set initial value
        if (box.getSelectedItem() !=null && box.getSelectedItem() instanceof ExpectedColumn) {
          ExpectedColumn ec = ((ExpectedColumn)box.getSelectedItem());
          if (ec.isSetTypeSelection()) {
            typeBox.setModel(ec.getTypeComboBoxModel());
            typeBox.setEnabled(true);
          }
        }
        if (exColTypeSelections[i]<typeBox.getItemCount()) {
          typeBox.setSelectedIndex(exColTypeSelections[i]);
        }
        exColTypeSelections[i] = typeBox.getSelectedIndex(); //Keep in Sync!
        ret[1][i] = typeBox;
        
        // Store selected value in an array.
        typeBox.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            exColTypeSelections[i_copy] = ((JComboBox)e.getSource()).getSelectedIndex();
          }
        });

      } else {
        typeBox=null;
      }
      
      // Add an action listener to the columnSelector that controls the type selector
      // and stores the selected item.
      box.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          exColSelections[i_copy] = ((JComboBox)e.getSource()).getSelectedIndex();
          if (typeBox==null) return;
          Object item = ((JComboBox)e.getSource()).getSelectedItem();
          if (item.equals(ignoreItem)) {
            // Remove the type chooser
            if (typeBox!=null) typeBox.setEnabled(false);
          } else {
            // Set the appropriate typeSelector
            ExpectedColumn col = (ExpectedColumn) item;
            if (col.isSetTypeSelection()) {
              if (typeBox!=null) {
                typeBox.setEnabled(true);
                typeBox.setModel(col.getTypeComboBoxModel());
                exColTypeSelections[i_copy] = typeBox.getSelectedIndex(); //Keep in Sync!
              }
            } else {
              typeBox.setEnabled(false);
            }
          }
        }
      });
    }
    
    return ret;
  }

  /**
   * @return true if and only if at least one Expected column
   * in {@link #exCols} requires a type selection
   * (i.e., {@link ExpectedColumn#isSetTypeSelection()} = true).
   */
  private boolean isATypeSelectorRequired() {
    for (ExpectedColumn exCol : exCols) {
      if (exCol.isSetTypeSelection()) return true;
    }
    return false;
  }

  /**
   * @return a {@link ComboBoxModel} for all expected Columns ({@link #exCols}).
   */
  private ComboBoxModel getExpectedColsComboBoxModel() {
    if (exCols==null) return null;
    Object[] eca = exCols.toArray();
    Object[] choices = new Object[eca.length+1];
    choices[0] = ignoreItem;
    System.arraycopy(eca, 0, choices, 1, eca.length);
    return new DefaultComboBoxModel(choices);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.CSVReaderOptionPanel#isSelectionValid()
   */
  @Override
  protected boolean isSelectionValid() {
    writeSelectionsToObjects();
    
    for (ExpectedColumn e: exCols) {
      if (e.required && e.getAssignedColumns().size()<1) {
        GUITools.showMessage("Please assign a column to '" + e.name.toString() + "'.", getName());
        return false;
      }
      if (!e.multiSelectionAllowed && e.getAssignedColumns().size()>1) {
        GUITools.showMessage("Please assign only one column to '" + e.name.toString() + "'.", getName());
        return false;
      }
      if (e.multiSelectionAllowed && e.multiSelectionOnlyWithDifferentType) {
        Set<Integer> unique = new HashSet<Integer>();
        for (Integer i: e.getAssignedTypeForEachColumn()) {
          if (!unique.add(i)) {
            GUITools.showMessage("Please assign a unique type to each '" + e.name.toString() + "' column.", getName());
            return false;
          }
        }
      }
    }
    return true;
  }


  /**
   * Processes the {@link #exColSelections} and {@link #exColTypeSelections} arrays
   * and writes the respective values to the {@link ExpectedColumn#assignedColumns}
   * and {@link ExpectedColumn#assignedTypeForEachColumn} lists.
   * <p>Note: this method simply processes the selections. It does NOT check if
   * they are valid!
   * @see #isSelectionValid()
   */
  private void writeSelectionsToObjects() {
    removeSelectionsFromObjects();
    
    // Put selections to lists
    for (int i=0; i<exColSelections.length; i++) {
      int selectedItem = exColSelections[i]-1;
      int selectedType = exColTypeSelections[i];
      if (selectedItem>=0) {
        exCols.get(selectedItem).assignedColumns.add(i);
        exCols.get(selectedItem).assignedTypeForEachColumn.add(selectedType);
      }
    }
  }

  /**
   * Resets all <code>assignedColumns</code> and 
   * <code>assignedTypeForEachColumn</code> attributes of
   * all {@link ExpectedColumn}s.
   */
  private void removeSelectionsFromObjects() {
    // Erase all previous selections
    for (ExpectedColumn e : exCols) {
      e.assignedColumns.clear();
      e.assignedTypeForEachColumn.clear();
    }
  }

  /**
   * @see #showDialog(Component, String, CSVImporterV2)
   * @param parent
   * @param c
   * @return
   * @throws IOException
   */
  public static boolean showDialog(Component parent, final CSVImporterV2 c) throws IOException {
    return showDialog(parent, String.format("Data import for '%s'", FileTools.getFilename(c.getCSVReader().getFilename())), c);
  }
  
  /**
   * Show a dialog for the {@link CSVImporterV2} panel.
   * @param parent - the parent to which this dialog is modal. 
   * @param title - title for this dialog
   * @param c - {@link CSVImporterV2} panel to display as dialog
   * @return true if and only if the dialog has been confirmed by the user.
   * @throws IOException - if input file is not readable or invalid. 
   */
  public static boolean showDialog(Component parent, String title, final CSVImporterV2 c) throws IOException {

    // Initialize the dialog
    final JDialog jd;
    if (parent!=null && parent instanceof Frame) {
      jd = new JDialog((Frame)parent, title, true);
    } else if (parent!=null && parent instanceof Dialog) {
      jd = new JDialog((Dialog)parent, title, true);
    } else {
      jd = new JDialog();
      jd.setTitle(title);
      jd.setModal(true);
    }
    
    // Initialize the panel
    c.setName(title);
    jd.add(c);
    // Close dialog with ESC button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        c.removeSelectionsFromObjects();
        c.buttonPressed = JOptionPane.CANCEL_OPTION;
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    // Close dialog with ENTER button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (c.isSelectionValid()) {
          c.buttonPressed = JOptionPane.OK_OPTION;
          jd.dispose();
        }
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    // Set close operations
    jd.addWindowListener(new WindowAdapter() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(WindowEvent e) {
        jd.setVisible(false);
      }
    });
    c.addComponentListener(new ComponentListener() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
       */
      public void componentHidden(ComponentEvent e) {
        jd.setVisible(false);
      }
      /*
       * (non-Javadoc)
       * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
       */
      public void componentMoved(ComponentEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
       */
      public void componentResized(ComponentEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
       */
      public void componentShown(ComponentEvent e) {}
    });
    
    // Set size
    jd.setPreferredSize(c.getPreferredSize());
    jd.setSize(c.getPreferredSize());
    //jd.pack();
    jd.setLocationRelativeTo(parent);
    
    // Set visible and wait until invisible
    jd.setVisible(true);
    
    // Dispose and return if dialog has been confirmed.
    jd.dispose();
    
    return (c.getButtonPressed()==JOptionPane.OK_OPTION);
  }
  
  /**
   * Show a dialog to choose the main CSVReader options AND to let the user
   * assign columns for the {@link ExpectedColumn}s.
   * @param parent - the parent to which this dialog is modal. 
   * @param r - the current CSV Reader
   * @param title - title for this dialog
   * @return copy of original (cancel button pressed) or modified (ok) reader.
   * Ok has been pressed if and only if (returnedReader==sourceReader).
   * @throws IOException - if input file is not readable or invalid.
   */
  public static CSVImporterV2 showDialog(Component parent, String inFile, String title, ExpectedColumn... cols) throws IOException {
    final CSVImporterV2 c = new CSVImporterV2(inFile, cols);
    showDialog(parent, title, c);
    return c;
  }
  
  // TODO: Override invalid static methods coming from CSVReaderOptionPanel
  
  /**
   * Just for DEMO and testing purposes.
   */
  public static void main(String[] args) {
    // Define the columns we expect to read from the file
    ExpectedColumn[] exp = new ExpectedColumn[3];
    
    exp[0] = new ExpectedColumn("Signal");
    exp[0].type=new String[]{"Pval","Fold change"};
    exp[0].regExPatternForInitialSuggestion="\\d+";
    exp[0].renameAllowed=true;
    exp[0].multiSelectionAllowed=true;
    exp[0].multiSelectionOnlyWithDifferentType=true;
    
    exp[1] = new ExpectedColumn("Score");
    exp[1].type=new String[]{"a1","a2","a3"};
    
    exp[2] = new ExpectedColumn("Chromosome");
    exp[2].regExPatternForInitialSuggestion="chr.*";
    
    // Graphically import the file and let the user assign the columns
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame parent = new JFrame();
      parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      CSVImporterV2 importer = showDialog(parent,"files/sample2.csv.txt","Data import",exp);
      System.out.println("Button pressed    : " + importer.getButtonPressed());
      System.out.println("Dialog approved?  : " + (importer.getButtonPressed()==JOptionPane.OK_OPTION) );
      System.out.println("Approved CSVReader: " + importer.getApprovedCSVReader());
      if ((importer.getButtonPressed()==JOptionPane.OK_OPTION)) {
        System.out.println("Column assignments: ");
        for (ExpectedColumn e: exp) {
          System.out.println("  " + e.getName() + ": " + Arrays.deepToString(e.getAssignedColumns().toArray()));
          if (e.isSetTypeSelection()) {
            System.out.println("    Types: " + Arrays.deepToString(e.getAssignedTypeForEachColumn().toArray()));
          }
        }
      }
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    // Show a dialog that let's the user rename the expected column captions
    if (cmd.equals("edit_names")) {
      // Write initial selections to object for better proposals
      writeSelectionsToObjects();
      
      // get CSV file headers for initial suggestions
      String[] headers = getCSVReader().getHeader();
      if (headers==null) headers = new String[0];
      else {
        // Trim to maximum 40 chars
        for (int i=0; i<headers.length; i++) {
          if (headers[i].length()>40)headers[i] = headers[i].substring(0, 40);
        }
      }
      
      // Create expected array with fields and suggestions
      List<String> fields = new ArrayList<String>();
      List<ExpectedColumn> field_ecs = new ArrayList<ExpectedColumn>();
      List<String[]> suggestions = new ArrayList<String[]>();
      int i=0;
      for (ExpectedColumn ec: exCols) {
        if (ec.renameAllowed) {
          //fields.add((++i)+".");
          fields.add(ec.getOriginalName().toString()+": ");
          field_ecs.add(ec);
          
          // Propose name of selected column for already assigned columns
          String currentName = ec.name.toString();
          if (ec.isOriginalName() && ec.getAssignedColumn()>=0 && r.getContainsHeaders()
              && ec.getAssignedColumn()<r.getHeader().length) {
            currentName = r.getHeader()[ec.getAssignedColumn()];
          }
          // Suggest: currentName/colHeader, originalName, <all other col headers>
          String[] curSuggestions = headers.clone();
          if (!currentName.equals(ec.getOriginalName().toString())) {
            curSuggestions = ArrayUtils.merge(ec.getOriginalName().toString(), curSuggestions);
          }
          curSuggestions = ArrayUtils.merge(currentName, curSuggestions);
          suggestions.add(curSuggestions);
        }
      }
      
      
      // Show dialog and let the user enter new labels
      String[] newLabels = JLabeledComponent.showDialog(this, getRenameButtonCaption(),
        fields.toArray(new String[0]), suggestions.toArray(new String[0][]), false);
      if (newLabels!=null) {
        for(i=0; i<newLabels.length; i++) {
          field_ecs.get(i).name = newLabels[i];
        }
        refreshPreviewPanel();
      }
    }
  }

}
