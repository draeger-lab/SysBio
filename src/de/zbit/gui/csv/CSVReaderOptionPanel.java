/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import de.zbit.gui.GUITools;
import de.zbit.gui.panels.ExpandablePanel;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.ResourceManager;

/**
 * 
 * Swing panel (or dialog) to choose and validate CSVReader options.
 * It displays a preview of the file with the current settings and
 * allows to change some settings.
 * 
 * <p>
 * Use Case 1 (Dialog): <pre>showDialog(parent, "files.csv.txt", "My title")</pre>
 * Use Case 2 (Panel) :
 * <pre>
 * CSVReaderOptionPanel c = new CSVReaderOptionPanel("files.csv.txt")
 * setShowButtons(false); // Hide ok and cancel
 * </pre>
 * </p>
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class CSVReaderOptionPanel extends JPanel {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -3459654748893049315L;
  
  /**
   * A {@link Logger} for this class.
   */
  public static final transient Logger logger = Logger.getLogger(CSVReaderOptionPanel.class.getName());

  // Save original and modified CSV Reader (for the cancel button)
  private final CSVReader original;
  protected final CSVReader r;
  private int numDataLinesForPreview=5;
  protected int buttonPressed=-1; // Tracks if ok or cancel has been pressed
  private boolean showButtons = true;
  
  private JComponent currentOptions;
  private JComponent currentPreview;
  private JComponent currentButtons;
  private Font defaultFont = null;
  private Font defaultTitleFont = null;
  protected Font defaultPreviewFont = null;
  
  private String[] seperators = new String[]{"[Auto detect]", "[Any whitespace character]", "[Space]", "[Tab]", ",", ";", "|", "/"};
  private boolean separatorAutoDetect=true;
  /**
   * This thread is refreshing the preview in a given time.
   * Use this.queueRefreshPreviewPanel(refreshInMiliseconds)
   */
  private Thread waitingForRefresh=null;
  
  // Strings for finding and resetting fields
  private final static String FIELD_CONTAINS_HEADERS = "containsHeaders";
  private final static String FIELD_SEPARATOR_CHAR = "separatorChar";
  private final static String FIELD_TREAT_MULTI_AS_ONE = "treatMultiAsOne";
  private final static String FIELD_TO_SKIP = "toSkip";
  
  /**
   * 
   * @param r
   * @throws IOException
   */
  public CSVReaderOptionPanel(CSVReader r) throws IOException {
    this(r,true);
  }
  protected CSVReaderOptionPanel(CSVReader r, boolean initGUI) throws IOException {
    super();
    // Look if the user has previously set a custom separator.
    separatorAutoDetect = r.isAutoDetectSeparatorChar();
    this.r = r;
    init();
    
    // Create a copy of the CSV Reader
    CSVReader org = null;
    try {
      org = (CSVReader) r.clone();
    } catch (CloneNotSupportedException e) {e.printStackTrace();}
    original = org;
    
    if (initGUI) {
      initGUI();
    }
  }
  public CSVReaderOptionPanel(String inFile) throws IOException {
    this(new CSVReader(inFile));
  }
  
  
  
  /**
   * See {@link #setNumDataLinesForPreview(int)}
   * @return
   */
  public int getNumDataLinesForPreview() {
    return numDataLinesForPreview;
  }
  /**
   * Sets the number of data lines to display in the preview panel.
   * 
   * @param numDataLinesForPreview
   */
  public void setNumDataLinesForPreview(int numDataLinesForPreview) {
    this.numDataLinesForPreview = numDataLinesForPreview;
    refreshPreviewPanel();
  }
  
  /**
   * See {@link #setDefaultFont(Font)}
   * @return
   */
  public Font getDefaultFont() {
    return defaultFont;
  }
  /**
   * The default font to use for the OptionPanel and Buttons.
   * @param defaultFont
   */
  public void setDefaultFont(Font defaultFont) {
    this.defaultFont = defaultFont;
    refreshOptionsPanel();
  }
  
  /**
   * See {@link #setDefaultTitleFont(Font)}
   * @return
   */
  public Font getDefaultTitleFont() {
    return defaultTitleFont;
  }
  /**
   * The default font to use for the Titles in the Borders
   * @param defaultTitleFont
   */
  public void setDefaultTitleFont(Font defaultTitleFont) {
    this.defaultTitleFont = defaultTitleFont;
    setBorder(currentOptions, getCSVOptionsString());
    setBorder(currentPreview, ResourceManager.getBundle(
        "de.zbit.locales.Labels").getString("FILE_PREVIEW"));
  }
  
  /**
   * See {@link #setDefaultPreviewFont(Font)}
   * @return
   */
  public Font getDefaultPreviewFont() {
    return defaultPreviewFont;
  }
  /**
   * The default font to use for the preview panel
   * (mainly the table).
   * 
   * @param defaultPreviewFont
   */
  public void setDefaultPreviewFont(Font defaultPreviewFont) {
    this.defaultPreviewFont = defaultPreviewFont;
    refreshPreviewPanel();
  }
  
  /**
   * Do you want to display ok and cancel buttons on this panel?
   * @param showButtons
   */
  public void setShowButtons(boolean showButtons) {
    boolean rebuild = false;
    if (this.showButtons != showButtons) {
      rebuild = true;
    }
    
    this.showButtons = showButtons;
    
    if (rebuild) {
      if (currentButtons!=null) {
        remove(currentButtons);
      }
      if (showButtons) {
        if (currentButtons==null) {
          currentButtons = buildButtons();
        }
        add(currentButtons, BorderLayout.SOUTH);
      }
    }
  }
  /**
   * Returns the approved and resetted CSVReader.
   * 
   * @return if ok has been pressed: the modified CSV Reader.
   * Else: the original CSV Reader.
   */
  public CSVReader getApprovedCSVReader() {
    if (isOkPressed()) {
      return getCSVReader();
    } else {
      return original;
    }
  }
  
  /**
   * Has the OK-button been pressed or not. If false,
   * either cancel has been pressed or the dialog has
   * been closed.
   * @return
   */
  public boolean isOkPressed() {
    return (buttonPressed == JOptionPane.OK_OPTION);
  }
  
  /**
   * @return the button pressed on this dialog as given by
   * JOptionPane. -1 if it has just been closed,
   * JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION else.
   */
  public int getButtonPressed() {
    return buttonPressed;
  }
  
  /**
   * Returns the resetted, eventually modified CSVReader.
   * @return
   */
  public CSVReader getCSVReader() {
    // Reset reading position before returning the reader
    try {
      r.open();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return r;
  }
  
  /**
   * Builds the whole panel.
   * This function is called automatically in the constructor.
   * @throws IOException
   */
  protected void initGUI() throws IOException{
    resetPanel();
    
    // Get Panels
    currentOptions = buildCSVOptionsPanel();
    currentPreview = buildPreview(numDataLinesForPreview);
    
    // Add borders
    if (!(currentOptions instanceof ExpandablePanel)) {
      setBorder(currentOptions, getCSVOptionsString());
    }
    setBorder(currentPreview, getFilePreviewCaption());
    
    // Create Panel
    setLayout(new BorderLayout());
    add(currentOptions, BorderLayout.NORTH);
    add(currentPreview, BorderLayout.CENTER);
    
    // Add Ok and Cancel buttons
    if (showButtons) {
      currentButtons = buildButtons();
      add(currentButtons, BorderLayout.SOUTH);
    }
    
  }
  
  /**
   * @return localized String for "CSV Options"
   */
  protected static String getCSVOptionsString() {
    return ResourceManager.getBundle(
        "de.zbit.locales.Labels").getString("CSV_OPTIONS");
  }
  
  /**
   * @return a caption for the file preview.
   */
  protected String getFilePreviewCaption() {
    return ResourceManager.getBundle(
        "de.zbit.locales.Labels").getString("FILE_PREVIEW");
  }
  /**
   * Build a panel with cancel and ok buttons.
   * When any button is pressed, it will trigger setVisible(false).
   * If ok is pressed, it will also trigger okPressed = true
   * @return
   */
  public JPanel buildButtons() {
    JPanel southPanel = new JPanel(new BorderLayout());
    
    // Ok Button
    FlowLayout fr = new FlowLayout();
    fr.setAlignment(FlowLayout.RIGHT);
    JPanel se = new JPanel(fr);
    String text = GUITools.getOkButtonText();
    JButton ok = new JButton(text);
    ok.setName("ok");
    if (defaultFont != null) {
      ok.setFont(defaultFont);
    }
    ok.setSelected(true);
    se.add(ok);
    southPanel.add(se, BorderLayout.EAST);
    
    // Cancel Button
    FlowLayout fl = new FlowLayout();
    fl.setAlignment(FlowLayout.LEFT);
    JPanel sw = new JPanel(fl);
    text = GUITools.getCancelButtonText();
    JButton cancel = new JButton(text);
    if (defaultFont!=null) {
      cancel.setFont(defaultFont);
    }
    sw.add(cancel);
    southPanel.add(sw, BorderLayout.WEST);
    
    // Set common size
    ok.setPreferredSize(cancel.getPreferredSize()); // new Dimension(75,25)
    //    cancel.setPreferredSize(new Dimension(75,25));
    
    // Add listeners
    ok.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (isSelectionValid()) {
          buttonPressed = JOptionPane.OK_OPTION;
          setVisible(false);
        }
      }
    });
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        buttonPressed = JOptionPane.CANCEL_OPTION;
        setVisible(false);
      }
    });
    
    return southPanel;
  }
  
  /**
   * The dialog will only be closeable via the ok button if
   * this method returns true.
   * @return
   */
  protected boolean isSelectionValid() {
    return true;
  }
  
  private void setBorder(JComponent c, String title) {
    TitledBorder tb2 = new TitledBorder(title);
    if (defaultTitleFont!=null) {
      tb2.setTitleFont(defaultTitleFont);
    }
    c.setBorder(tb2);
  }
  
  /**
   * Initializes the CSV Reader
   * @throws IOException
   */
  private void init() throws IOException {
    setPreferredSize(getDefaultPreferredSize());
    
    // Open the file and read the first data line.
    if (r.getDataButDoNotReadIfNotAvailable()==null) {
      r.open();
    }
    
  }
  
  protected Dimension getDefaultPreferredSize() {
    return new java.awt.Dimension(650, 350);
  }
  
  /**
   * Creates the CSV Options panel.
   * @return
   */
  public JPanel buildCSVOptionsPanel() {
    JPanel currentPanel = new JPanel(new GridBagLayout());
    
    // Do NOT change the names. They are used in the buildPreviewPanel function.
    
    // Contains headers
    final JCheckBox head = new JCheckBox(ResourceManager.getBundle(
        "de.zbit.locales.Labels").getString("FILE_CONTAINS_HEADERS"));
    if (defaultFont!=null) {
      head.setFont(defaultFont);
    }
    head.setToolTipText(ResourceManager.getBundle("de.zbit.locales.Labels")
      .getString("INDICATE_IF_COLUMN_DESCRIPTIONS_ARE_GIVEN"));
    head.setSelected(r.getContainsHeaders());
    head.setName(FIELD_CONTAINS_HEADERS);
    head.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        r.setContainsHeaders(head.isSelected());
        queueRefreshPreviewPanel(0);
      }
    });
    addComponent(currentPanel, head, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    
    // Separator char
    JLabel d0 = new JLabel(ResourceManager.getBundle("de.zbit.locales.Labels")
      .getString("SEPARATOR_CHAR"));
    if (defaultFont!=null) {
      d0.setFont(defaultFont);
    }
    final JComboBox sep = buildSeparatorCharChooser();
    sep.setName(FIELD_SEPARATOR_CHAR);
    d0.setLabelFor(sep);
    addComponent(currentPanel, d0, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    addComponent(currentPanel, sep, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    // Separator Char END
    
    
    // Treat multi as one
    final JCheckBox mult = new JCheckBox(ResourceManager.getBundle("de.zbit.locales.Labels").getString("TREAT_CONSECUTIVE_SEPARATORS_AS_ONE"));
    if (defaultFont!=null) {
      mult.setFont(defaultFont);
    }
    mult.setToolTipText(ResourceManager.getBundle("de.zbit.locales.Labels").getString("TREAT_CONSECUTIVE_SEPARATORS_AS_ONE_TOOLTIP"));
    mult.setSelected(r.getTreatMultipleConsecutiveSeparatorsAsOne());
    mult.setName(FIELD_TREAT_MULTI_AS_ONE);
    mult.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        r.setTreatMultipleConsecutiveSeparatorsAsOne(mult.isSelected());
        queueRefreshPreviewPanel(0);
      }
    });
    addComponent(currentPanel, mult, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    
    
    // Skip lines (=> set via Skip)
    String contentStartTT = ResourceManager.getBundle("de.zbit.locales.Labels").getString("SKIP_THIS_NUMBER_OF_LEADING_LINES");
    JLabel d1 = new JLabel(ResourceManager.getBundle("de.zbit.locales.Labels").getString("SKIP_LINES"));
    if (defaultFont!=null) {
      d1.setFont(defaultFont);
    }
    d1.setToolTipText(contentStartTT);
    
    final ActionListener skipLinesAction = new ActionListener() {
      public int lastQueuedSkip = r.getContentStartLine();
      @Override
      public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand().trim();
        if (s==null || s.length()<1) {
          return;
        } else if (CSVReader.isNumber(s, true)) {
          int skip = Integer.parseInt(s);
          if (lastQueuedSkip!= skip) {
            lastQueuedSkip = skip;
            r.setSkipLines(skip);
            r.setAutoDetectContentStart(false);
            queueRefreshPreviewPanel(1000);
          }
        }
      }
    };
    
    JTextField tf = buildIntegerBox(Integer.toString(r.getContentStartLine()), skipLinesAction);
    if (defaultFont!=null) {
      tf.setFont(defaultFont);
    }
    tf.setToolTipText(contentStartTT);
    tf.setName(FIELD_TO_SKIP);
    d1.setLabelFor(tf);
    addComponent(currentPanel, d1, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    addComponent(currentPanel, tf, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    
    return currentPanel;
  }
  
  /**
   * Build the separator char JComboBox
   * @return JComboBox to select a separator char.
   */
  private JComboBox buildSeparatorCharChooser() {
    // Build separator model - eventually add current separator to default ones.
    // The ordering and subject of the first four elements is important and
    // should not be touched (Auto, RegexWS,Space,Tab).
    char c = r.getSeparatorChar();
    if (c!='\u0000' && c!='\u0001' && c!=' ' && c!='\t' &&
        !arrayContains(seperators, Character.toString(c))) {
      
      String[] temp  = new String[seperators.length+1];
      System.arraycopy(seperators, 0, temp, 0, seperators.length);
      temp[temp.length-1] = Character.toString(c);
      seperators = temp;
    }
    
    final DefaultComboBoxModel m = new DefaultComboBoxModel(seperators);
    final JComboBox sep = new JComboBox(m);
    if (defaultFont!=null) {
      sep.setFont(defaultFont);
    }
    sep.setEditable(true);
    sep.setToolTipText(ResourceManager.getBundle("de.zbit.locales.Labels").getString("SEPARATOR_CHAR_TOOLTIP"));
    
    // Set currently selected char
    setCurrentSeparator(c, sep);
    
    // Define action to perform
    final ActionListener seperatorAction = new ActionListener() {
      private char lastQueuedChar = r.getSeparatorChar();
      @Override
      public void actionPerformed(ActionEvent e) {
        
        String s = ((JTextComponent)sep.getEditor().getEditorComponent()).getText();
        if (s.length()<1) {
          return;
        }
        separatorAutoDetect = false;
        if (s.equalsIgnoreCase(m.getElementAt(0).toString())) {
          r.setSeparatorChar('\u0000');
          separatorAutoDetect = true;
        } else if (s.equalsIgnoreCase(m.getElementAt(1).toString())) {
          r.setSeparatorChar('\u0001');
        } else if (s.equalsIgnoreCase(m.getElementAt(2).toString())) {
          r.setSeparatorChar(' ');
        } else if (s.equalsIgnoreCase(m.getElementAt(3).toString())) {
          r.setSeparatorChar('\t');
        } else {
          if (s.length()>1) {
            return;
          }
          r.setSeparatorChar(s.charAt(0));
        }
        
        if (r.getSeparatorChar()!=lastQueuedChar) {
          lastQueuedChar = r.getSeparatorChar();
          queueRefreshPreviewPanel(500);
        }
      }
    };
    sep.addActionListener(seperatorAction);
    
    // Restrict box to characters only
    createCharacterBox((JTextComponent)sep.getEditor().getEditorComponent(), seperatorAction);
    
    return sep;
  }
  
  public static void createCharacterBox(final JComboBox sep) {
    createCharacterBox( (JTextField) sep.getEditor().getEditorComponent() );
  }
  public static void createCharacterBox(final JTextComponent editorcomp) {
    createCharacterBox(editorcomp, null);
  }
  /**
   * Adds a {@link KeyListener} to the {@code editorcomp} that ensures
   * a length of one.
   * 
   * <p>Please note, that paste-actions are not covered by the KeyListener. For
   * those, a {@link DocumentListener} should be added to
   * {@link JTextComponent#getDocument()}.
   * @param editorcomp
   * @param fireOnKeyRelease an additional actionlistener (may be {@code null})
   * that will be fired upon key release.
   */
  public static void createCharacterBox(final JTextComponent editorcomp, final ActionListener fireOnKeyRelease) {
    
    // Create a new document with maximum size of 1
    // This will really disable all texts with length>1 in the box => no "[Tab]" selection, etc.
    //    editorcomp.setDocument(new PlainDocument() {
    //      private static final long serialVersionUID = 1L;
    //      private int limit=1;
    //      public void insertString (int offset, String str, AttributeSet attr) throws BadLocationException {
    //        if (str == null) return;
    //
    //        if ((getLength() + str.length()) <= limit) {
    //          super.insertString(offset, str, attr);
    //        }
    //      }
    //    });
    
    
    // Ensure that the text field length is always 1
    editorcomp.addKeyListener(new KeyListener() {
      public boolean isValid(char keyChar) {
        String selText=editorcomp.getSelectedText();
        if (selText==null) {
          selText="";
        }
        // Accept backspace or delete or everything if length < 1
        if ((keyChar == 8 )  || (keyChar == 127) || // BS, Entf
            Character.getType(keyChar) == Character.UNASSIGNED || // left or right and such
            selText.equals(editorcomp.getText()) || // text is selected
            editorcomp.getText().length()<1) {
          return true;
        }
        return false;
      }
      @Override
      public void keyPressed(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        }
      }
      @Override
      public void keyReleased(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        }
        // Fire an action event to change the separator accordingly.
        if (fireOnKeyRelease!=null) {
          fireOnKeyRelease.actionPerformed(new ActionEvent(editorcomp,ActionEvent.ACTION_PERFORMED, "textBoxChanged"));
        }
      }
      @Override
      public void keyTyped(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        }
      }
    });
    
    
  }
  
  /**
   * This will set the separator comboBox to the given separator char.
   * It will leave the separatorAutoDetect variable untouched.
   * 
   * Use this function to set the combo box to the auto detected separator.
   * Do not use it for user actions (the action listener is for that purpose).
   * @param c
   * @param sep
   */
  private void setCurrentSeparator(char c, final JComboBox sep) {
    boolean backupAutoDetec = separatorAutoDetect;
    if (c=='\u0000') {
      sep.setSelectedIndex(0);
    } else if (c=='\u0001') {
      sep.setSelectedIndex(1);
    } else if (c==' ') {
      sep.setSelectedIndex(2);
    } else if (c=='\t') {
      sep.setSelectedIndex(3);
    } else {
      int id = arrayIndexOf(seperators, Character.toString(c));
      if (id>=0) {
        sep.setSelectedIndex(id);
      } else {
        sep.setSelectedItem(Character.toString(c));
      }
    }
    separatorAutoDetect = backupAutoDetec;
  }
  
  /**
   * Build a JTextfield that accepts only integer values.
   * Should be PUBLIC because the method is also used by other classes.
   * @param defaultValue
   * @return
   */
  public static JTextField buildIntegerBox(String defaultValue) {
    return buildIntegerBox(defaultValue, null);
  }
  /**
   * Build a JTextfield that accepts only integer values.
   * Should be PUBLIC because the method is also used by other classes.
   * @param defaultValue
   * @param ac - actionListener that will get an action fired each time
   * a valid key has been released. The current textfield content is passed
   * as ActionCommand to the ActionListener.
   * @return
   */
  public static JTextField buildIntegerBox(String defaultValue, final ActionListener ac) {
    if (defaultValue==null) {
      defaultValue="";
    }
    final JTextField tf = new JTextField(defaultValue);
    
    // Ensure that only numbers come in this box
    tf.addKeyListener(new KeyListener() {
      public boolean isValid(char keyChar) {
        
        // Extract relevant text, Check if modification would lead to a valid number.
        boolean validNumber=false;
        if (Character.isDigit(keyChar) || keyChar=='-') {
          String t = tf.getText();
          if (tf.getSelectionStart()>=0) {
            t = t.substring(0, tf.getSelectionStart())+ keyChar + t.substring(tf.getSelectionEnd(), t.length());
          } else {
            t = t.substring(0, tf.getCaretPosition())+ keyChar + t.substring(tf.getCaretPosition(), t.length());
          }
          if (t.equals("-") || CSVReader.isIntegerNumber(t)) {
            validNumber=true;
          }
        }
        
        // Accept backspace or delete or everything if length < 1
        if ((keyChar == 8 )  || (keyChar == 127) ||
            Character.getType(keyChar) == Character.UNASSIGNED || // left or right and such
            validNumber) {
          return true;
        }
        return false;
      }
      @Override
      public void keyPressed(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        }
      }
      @Override
      public void keyReleased(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        } else if (ac!=null) {
          ac.actionPerformed(new ActionEvent(tf,ActionEvent.ACTION_PERFORMED, tf.getText()));
        }
      }
      @Override
      public void keyTyped(KeyEvent e) {
        if (!isValid(e.getKeyChar())) {
          e.consume();
        }
      }
    });
    return tf;
  }
  
  /**
   * Builds the Preview-panel, containing a JScrollPanel with a
   * table on it. The table corresponds to the first lines of the file
   * which were read with the current CSVReader settings.
   * 
   * @param numDataLines - number of data lines to display.
   * @return JScrollPane with the preview table.
   * @throws IOException
   */
  public JComponent buildPreview(int numDataLines) throws IOException {
    
    // Get Preview content and number of columns
    String[][] previewContent = getSampleData(numDataLines);
    int maxColCount = 0;
    if (previewContent!=null && previewContent.length>0) {
      maxColCount = previewContent[0].length;
    }
    
    // Set the header
    // For correct sizing, at least a space is required in each header column.
    String[] header=null;
    if (r.getContainsHeaders()) {
      header = r.getHeader();
    }
    String[] newHeader=header;
    newHeader = new String[maxColCount];
    if (header != null) {
      System.arraycopy(header, 0, newHeader, 0, Math.min(newHeader.length,header.length));
    }
    String column = ResourceManager.getBundle("de.zbit.locales.Labels").getString("COLUMN");
    for (int i = 0; i < newHeader.length; i++) {
      if (!r.getContainsHeaders()) {
        newHeader[i] = column + (i + 1);
      } else if (newHeader[i] == null || newHeader[i].length() < 1) {
        newHeader[i] = " ";
      }
    }
    
    // Build the table
    final JTable table = buildPreviewTable(previewContent, newHeader);
    
    // Put all on a scroll  pane
    final JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    // When resizing, try to optimize table size.
    final int numberOfcols = newHeader.length;
    final int defaultWidth;
    if (table.getColumnModel().getColumnCount()>0) {
      defaultWidth = table.getColumnModel().getColumn(0).getWidth();
    } else {
      defaultWidth = 75;
    }
    
    scrollPane.addComponentListener(new ComponentListener() {
      @Override
      public void componentHidden(ComponentEvent e) {}
      @Override
      public void componentMoved(ComponentEvent e) {}
      @Override
      public void componentShown(ComponentEvent e) {}
      
      @Override
      public void componentResized(ComponentEvent e) {
        if (numberOfcols<5 && scrollPane.getWidth()>numberOfcols*defaultWidth) {
          table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
      }
    });
    
    return scrollPane;
  }
  
  /**
   * 
   * @param header
   * @return
   */
  protected JTable buildPreviewTable(String[][] data, String[] header) {
    final JTable table = new JTable(data, header);
    table.setPreferredScrollableViewportSize(new Dimension(500, 100));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    if (defaultPreviewFont!=null) {
      table.setFont(defaultPreviewFont);
    }
    
    // Make cells not editable
    table.setEnabled(false);
    // Disallow dragging columns
    table.getTableHeader().setReorderingAllowed(false);
    
    return table;
  }
  
  /**
   * Return sample content from the file
   * @param numDataLines number of lines to read from the file
   * @return a uniform 2D array with sample content
   * @throws IOException
   */
  private String[][] getSampleData(int numDataLines) throws IOException {
    // If the user has not definitely set a separator, allow
    // to change the current selection, based on best-guess.
    if (separatorAutoDetect) {
      r.setSeparatorChar('\u0000');
    }
    
    // Get Data
    ArrayList<String[]> firstLines = new ArrayList<String[]>(numDataLines);
    int maxColCount=0;
    r.open();
    
    String[] line;
    int i=0;
    while((line=r.getNextLine())!=null) {
      firstLines.add(line);
      maxColCount = Math.max(maxColCount, line.length);
      if ((++i)==numDataLines) {
        break;
      }
    }
    r.close();
    // ---
    String[][] data = firstLines.toArray(new String[0][0]);
    
    // Bring them all to the same column count
    for (i=0; i<data.length; i++) {
      if (data[i]!=null && data[i].length<maxColCount) {
        String[] newData = new String[maxColCount];
        System.arraycopy(data[i], 0, newData, 0, data[i].length);
        data[i] = newData;
      }
    }
    
    // Reset re-inferred values
    if (currentOptions!=null) {
      for (i=0; i<currentOptions.getComponentCount(); i++) {
        Component c = currentOptions.getComponent(i);
        if (c==null || c.getName()==null) {
          continue;
        }
        if (c.getName().equals(FIELD_CONTAINS_HEADERS)) {
          if (c instanceof JCheckBox) {
            ((JCheckBox)c).setSelected(r.getContainsHeaders());
          }
        } else if (c.getName().equals(FIELD_SEPARATOR_CHAR)) {
          if (c instanceof JComboBox) {
            setCurrentSeparator(r.getSeparatorChar(), ((JComboBox)c));
          }
        } else if (c.getName().equals(FIELD_TREAT_MULTI_AS_ONE)) {
          if (c instanceof JCheckBox) {
            ((JCheckBox)c).setSelected(r.getTreatMultipleConsecutiveSeparatorsAsOne());
          }
        } else if (c.getName().equals(FIELD_TO_SKIP)) {
          if (c instanceof JTextComponent) {
            ((JTextComponent)c).setText(Integer.toString(Math.max(0, r.getContentStartLine())));
          }
        }
      }
    }
    
    return data;
  }
  
  /**
   * Queue a refresh operation to occur in the given time.
   * This will cancel all other refresh operations currently in the queue.
   * @param refreshInMiliseconds - time to wait until refresh.
   */
  private void queueRefreshPreviewPanel(final int refreshInMiliseconds) {
    if (waitingForRefresh!=null) {
      waitingForRefresh.interrupt();
    }
    
    // Queue the refresh.
    waitingForRefresh = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(refreshInMiliseconds);
        } catch (InterruptedException e) {
          return; // Without refreshing!
        }
        refreshPreviewPanel();
        return;
      }
    };
    waitingForRefresh.start();
    
  }
  
  /**
   * Recreates the preview panel and replaces the old one.
   */
  public void refreshPreviewPanel() {
    if (currentPreview!=null) {
      remove(currentPreview);
    }
    try {
      currentPreview = buildPreview(numDataLinesForPreview);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception while refreshing the preview table.", e);
    }
    
    setBorder(currentPreview, getFilePreviewCaption());
    add(currentPreview, BorderLayout.CENTER);
    
    validate();
    repaint();
  }
  
  /**
   * Recreates the option panel and replaces the old one.
   */
  public void refreshOptionsPanel() {
    if (currentOptions!=null) {
      JPanel newOptions = buildCSVOptionsPanel();
      setBorder(newOptions, getCSVOptionsString());
      GUITools.replaceComponent(currentOptions, newOptions);
      currentOptions = newOptions;
      
      validate();
      repaint();
    }
  }
  
  /**
   * Use this method with caution, as it returns unchecked internal
   * data structures!
   * @return the current options panel.
   */
  public JComponent getOptionalPanel() {
    return currentOptions;
  }
  
  /**
   * Use this method with caution, as it returns unchecked internal
   * data structures!
   * @return the current preview panel.
   */
  public JComponent getPreviewPanel() {
    return currentPreview;
  }
  
  
  private static boolean arrayContains(String[] arr, String element) {
    if (arrayIndexOf(arr, element)>=0) {
      return true;
    }
    return false;
  }
  
  
  private static int arrayIndexOf(String[] arr, String element) {
    for (int i=0; i<arr.length; i++) {
      if (arr[i].equalsIgnoreCase(element)) {
        return i;
      }
    }
    return -1;
  }
  
  
  
  /**
   * Removes all objects from this panel.
   */
  public void resetPanel() {
    removeAll();
  }
  
  /**
   * Completely rebuilds the whole panel.
   */
  public void rebuildPanel() {
    try {
      initGUI();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Exception while rebuilding the preview table.", e);
    }
    
    validate();
    repaint();
  }
  
  
  private static final Insets insets = new Insets(0, 0, 0, 0);
  /**
   * Helper Method for GridBagConstrains.
   */
  private static void addComponent(Container container, Component component, int gridx, int gridy,
    int gridwidth, int gridheight, int anchor, int fill) {
    GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0,
      anchor, fill, insets, 0, 0);
    container.add(component, gbc);
  }
  
  
  /**
   * Show a dialog to choose the CSVReader options.
   * @param parent - the parent to which this dialog is modal. Either a frame or a dialog!
   * @param inFile - file to build a CSVReader around ( new CSVReader(inFile) ).
   * @param title - title for this dialog.
   * @return default (cancel button pressed) or modified (ok) reader.
   * @throws IOException - if input file is not readable or invalid.
   */
  public static CSVReader showDialog(Window parent, String inFile, String title) throws IOException {
    return showDialog(parent, new CSVReader(inFile), title);
  }
  
  /**
   * Show a dialog to choose the CSVReader options.
   * @param parent - the parent to which this dialog is modal.
   * @param inFile - file to build a CSVReader around ( new CSVReader(inFile) ).
   * @param title - title for this dialog.
   * @return default (cancel button pressed) or modified (ok) reader.
   * @throws IOException - if input file is not readable or invalid.
   */
  public static CSVReader showDialog(Container parent, String inFile, String title) throws IOException {
    return showDialog(parent, new CSVReader(inFile), title);
  }
  
  /**
   * Show a dialog to choose the main CSVReader options.
   * @param inFile - file to build a CSVReader around ( new CSVReader(inFile) ).
   * @param title - title for this dialog.
   * @return default (cancel button pressed) or modified (ok) reader.
   * @throws IOException - if input file is not readable or invalid.
   */
  public static CSVReader showDialog(String inFile, String title) throws IOException {
    return showDialog(null, new CSVReader(inFile), title);
  }
  
  /**
   * Show a dialog to choose the main CSVReader options.
   * @param parent - the parent to which this dialog is modal.
   * @param r - the current CSV Reader
   * @param title - title for this dialog
   * @return copy of original (cancel button pressed) or modified (ok) reader.
   * Ok has been pressed if and only if (returnedReader==sourceReader).
   * @throws IOException - if input file is not readable or invalid.
   */
  public static CSVReader showDialog(Component parent, CSVReader r, String title) throws IOException {
    
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
    final CSVReaderOptionPanel c = new CSVReaderOptionPanel(r);
    jd.add(c);
    // Close dialog with ESC button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        c.buttonPressed = JOptionPane.CANCEL_OPTION;
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
    JComponent.WHEN_IN_FOCUSED_WINDOW);
    // Close dialog with ENTER button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        if (c.isSelectionValid()) {
          c.buttonPressed = JOptionPane.OK_OPTION;
          jd.dispose();
        }
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
    JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    // Set close operations
    jd.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        jd.setVisible(false);
      }
    });
    c.addComponentListener(new ComponentListener() {
      @Override
      public void componentHidden(ComponentEvent e) {
        jd.setVisible(false);
      }
      @Override
      public void componentMoved(ComponentEvent e) {}
      @Override
      public void componentResized(ComponentEvent e) {}
      @Override
      public void componentShown(ComponentEvent e) {}
    });
    
    // Set size
    jd.setPreferredSize(c.getPreferredSize());
    jd.setSize(c.getPreferredSize());
    jd.setLocationRelativeTo(parent);
    
    // Set visible and wait until invisible
    jd.setVisible(true);
    
    // Dispose and return reader.
    r = c.getApprovedCSVReader();
    
    jd.dispose();
    return (r);
  }
  
}
