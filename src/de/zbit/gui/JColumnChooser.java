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
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;

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

import de.zbit.gui.prefs.JComponentForOption;
import de.zbit.io.CSVReader;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * Column Chooser with a label, a columnChooser and a preview.
 * 
 * Very usefull, e.g., to assign column headers of a csv file to
 * certain propoerties (e.g. asking the user "in which column
 * are the sequence start coordinates?").
 * 
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class JColumnChooser extends JPanel implements JComponentForOption {
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
  
  /**
   * Only necessary for using this class in Combination with
   * {@link SBPreferences} and {@link Option}s.
   */
  private Option<?> option=null;
  
  /**
   * This should always be true. Just if you want to use this
   * class not for "Choosing columns" but other stuff, you may
   * want to change this behaviour.
   */
  private boolean acceptOnlyIntegers=true;
  
  
  /**
   * Every integer added here corresponds to one column number. If an
   * integer is added, the column will be hidden in all ColumnChoosers.
   */
  private ArrayList<Integer> hideColumns = new ArrayList<Integer>();
  
  /**
   * If unsorted, the columns appear as they appear in the file. Else,
   * they are sorted alphabetically.
   */
  private boolean sortHeaders=false;
  
  private static ResourceBundle bundle = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
  private static String noOptionChoosen=bundle.getString("NOT_AVAILABLE");
  private static String exampleError=bundle.getString("INVALID_COLUMN");
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
   * he has chosen. If null, no preview will be given.
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
    
    usePreview = (preview!=null);
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
  
  /**
   * 
   */
  private void initGUI() {
    this.setPreferredSize(new Dimension(400, 25));
    //layout = new GridBagLayout();
    GridLayout layout = new GridLayout(1,3,10,2);
    this.setLayout(layout);
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Container#setLayout(java.awt.LayoutManager)
   */
  @Override
  public void setLayout(LayoutManager manager) {
    super.setLayout(manager);
    layoutElements();
  }
  
  
  /**
	 * @return acceptOnlyIntegers
	 */
	public boolean isAcceptOnlyIntegers() {
		return acceptOnlyIntegers;
	}
	
	/**
   * This should always be true. Just if you want to use this
   * class not for "Choosing columns" but other stuff, you may
   * want to change this behaviour.
   * 
	 * @param acceptOnlyIntegers
	 */
	public void setAcceptOnlyIntegers(boolean acceptOnlyIntegers) {
		if (this.acceptOnlyIntegers != acceptOnlyIntegers) {
		  this.acceptOnlyIntegers = acceptOnlyIntegers;
		  refreshSelector();
		}
	}
	
	/**
	 * Returns the actual column chooser object, which is either
	 * a {@link JComboBox} or a {@link JTextField}.
	 * @return
	 */
	public JComponent getColumnChooser() {
		return colChooser;
	}
	
	/**
	 * 
	 * @param il
	 */
	public synchronized void addItemListener(ItemListener il) {
		JComponent comp = getColumnChooser();
		if (comp instanceof JComboBox) {
			((JComboBox) comp).addItemListener(il);
		} else {
			// otherwise not possible!
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */
	@Override
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
    if (preview!=null && usePreview) l.removeLayoutComponent(preview);
    
    if (l instanceof BorderLayout) {
      BorderLayout c = (BorderLayout) l;
      if (label!=null) c.addLayoutComponent(label, BorderLayout.WEST);
      if (colChooser!=null) c.addLayoutComponent(colChooser, BorderLayout.CENTER);
      if (preview!=null && usePreview) c.addLayoutComponent(preview, BorderLayout.EAST);
      
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
      if (preview!=null && usePreview) {
        gbc.gridx=2;
        c.addLayoutComponent(preview, gbc);      
      }
    } else {
      if (label!=null) l.addLayoutComponent("Titel", label);
      if (colChooser!=null) l.addLayoutComponent("ColChooser", colChooser);
      if (preview!=null && usePreview) l.addLayoutComponent("Preview", preview);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		if (colChooser != null) {
			colChooser.addKeyListener(l);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		if (colChooser != null) colChooser.setName(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#getName()
	 */
	@Override
	public String getName() {
		return super.getName();
	}
  
  /**
   * Set the visibility of a column. This will hide the
   * header of the column with the given number, so the user can't
   * choose that column.
   * Try to set multiple columns at once, because calling this class
   * will also lead to refreshing and repainting the column chooser.
   * @param columnNumber - from 0 to n. 
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
    
    // Internally, there is just one array with columns to hide.
    // The array is changed here to reflect the desired visibilities.
    boolean performedChanges=false;
    for (int i=0; i<columnNumbers.length; i++) {
      int pos = hideColumns.indexOf(columnNumbers[i]);
      if (pos>=0) {
        if (visible) {
          hideColumns.remove(pos);
          performedChanges=true;
        }
      } else {
        if (!visible) {
          hideColumns.add(columnNumbers[i]);
          performedChanges=true;
        }
      }
    }
    
    // If changed, change active ColumnChoosers
    if (performedChanges) {
      refreshAndRepaint();
    }
  }
  
  /**
   * Decide, wether you want the headers to appear sorted, or not.
   * @param sort - True: headers appear sorted. False: headers appear
   * in the same ordering as they appear in the file.
   */
  public void setSortHeaders(boolean sort) {
    if (sort!=this.sortHeaders) {
      // Keep the selection
      Object item = getSelectedItem();
      
      this.sortHeaders = sort;
      refreshAndRepaint();
      
      // Restore selection.
      setSelectedItem(item);
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
	 * Set the headers to display in the combo box. This function will fill empty
	 * fields in the combo box with "(Column i)". if header is null, behaves like
	 * {@link #setHeaders(int)}.
	 * 
	 * @param header
	 */
	public void setHeaders(String[] header) {
		int l = 0;
		if ((header == null) || (header.length < 1)) {
			if (previews != null) l = previews.length;
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
		String column = bundle.getString("COLUMN");
		for (int i = 0; i < newHeader.length; i++) {
			// Completely empty array
			if (header == null)
				newHeader[i] = column + (i + 1);
			// Just fill missing gaps
			else if (newHeader[i] == null || newHeader[i].trim().length() < 1)
				newHeader[i] = "(" + column + (i + 1) + ")";
		}
    
    // Remember required headers and build model
    headers = newHeader;
    refreshAndRepaint();
  }
  
  /**
   * Builds the ComboBoxModel based on the current header,
   * refreshs the selector with this model and validates/
   * repaints the panel.
   */
  private void refreshAndRepaint() {
    model = buildComboBoxModel(headers);
    
    refreshSelector();
    validateRepaint();
  }
  
  private ComboBoxModel buildComboBoxModel(String[] newHeader) {
  	if (newHeader==null || newHeader.length<1) return null;
    
    // Create a list, hiding all unwanted elements
    Vector<String> headersToDisplay = new Vector<String>();
    for (int i=0; i<newHeader.length; i++) {
      if (hideColumns.contains(i)) continue;
      headersToDisplay.add(newHeader[i]);
    }
    
    // Sort eventually
    if (sortHeaders) Collections.sort(headersToDisplay);
    
    // If not required, add noOptionChoosen
    if (!required) {
      headersToDisplay.add(0, noOptionChoosen);
    }
    
    // Build the model
    return new DefaultComboBoxModel(headersToDisplay);
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
    if (this.required != required) {
      this.required = required;
      model = buildComboBoxModel(this.headers);
      refreshSelector();
      validateRepaint();
    }
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
      if (preview!=null) {
        preview.setVisible(usePreview);
        validateRepaint();
      }
    }
  }
  
  /**
   * Hide the preview label. Wrapper for 
   * <pre>
   * setUsePreview(false);
   * </pre> 
   */
  public void hidePreview() {
  	setUsePreview(false);
  }
  
  /**
   * Show the preview label. Wrapper for 
   * <pre>
   * setUsePreview(true);
   * </pre> 
   */
  public void showPreview() {
  	setUsePreview(true);
  }
  
  /**
   * Set the default value of the column chooser.
   * Does account for required or optional settings
   * (adds 1 for optional).
   * Also automaticaly accounts for sorting.
   * 
   * @param i - index number
   */
  public void setDefaultValue(int i) {
    if (!sortHeaders) {
      if (!required) i+=1;
      if (i<0) i=0;
      if (model!=null && i>=model.getSize()) i = model.getSize()-1;
      
      if (model!=null && i<model.getSize()) {
        model.setSelectedItem(model.getElementAt(i));
      }
    } else {
      // Search for position of the given item.
      if (i<0 || i>=headers.length) return;
      for (int j=0; j<model.getSize(); j++) {
        if (model.getElementAt(j).equals(headers[i])) {
          i=j;
          System.out.println(i);
          break;
        }
      }
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
      if (CSVReader.isNumber(s, true)) {
        setDefaultValue(Integer.parseInt(s));
      } else if (colChooser instanceof JTextField){
      	((JTextField) colChooser).setText(s);
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
    colChooser = getColumnChooser(useJTextField?null:model, -1, required, al, acceptOnlyIntegers);
    colChooser.setToolTipText(getToolTipText());
    if (getLayout() instanceof GridBagLayout) {
      addComponent(this, colChooser, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
    } else if (getLayout() instanceof BorderLayout) {
      add(colChooser, BorderLayout.CENTER);
    } else {
      // Remove and add preview to keep ordering (e.g. in gridlayout).
      if (preview!=null) remove(preview);
      add(colChooser); // e.g. GridLayout
      if (preview!=null && usePreview) add(preview);
    } 
    
    if (id>=0) setSelectedValue(id);
    al.actionPerformed(null); // Set preview field
  }
  
  /**
   * Returns the selected index. Accounts automatically for sorting and
   * required or optional.
   * 
   * @return integer between -1 and headers.length.
   */
  public int getSelectedValue() {
    if (colChooser==null) return -1;
    if (colChooser instanceof JComboBox) {
      if (!sortHeaders) {
        if (required) return ((JComboBox)colChooser).getSelectedIndex();
        else return ((JComboBox)colChooser).getSelectedIndex()-1;
      } else {
        return indexOf(headers, getSelectedItem().toString());
      }
    } else {
      String s = ((JTextComponent)colChooser).getText().trim();
      
      if (CSVReader.isNumber(s, true)) return Integer.parseInt(s);
      else return -1;
    }
  }
  
  
  /**
   * Returns the selected item (Usually a header string).
   * @return
   */
  public Object getSelectedItem() {
    if (colChooser instanceof JComboBox) {
      return ((JComboBox)colChooser).getSelectedItem();
    } else {
      String s = ((JTextComponent)colChooser).getText();
      return s;
    }
  }
  
  /**
   * Set the selected index to i. Does NOT account for required or
   * optional (add 1 for optional) or sortation. Use {@link #setDefaultValue(String)}
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
  
  /**
   * Set the selected item to the given one.
   * @param string
   */
  public void setSelectedItem(Object string) {
    if (colChooser instanceof JComboBox) {
      ((JComboBox)colChooser).setSelectedItem(string);
    } else {
      int pos = indexOf(headers, string.toString());
      if (pos<0) {
        if (CSVReader.isNumber(string.toString(), false)) {
          ((JTextComponent)colChooser).setText(string.toString());
        }
      } else {
        ((JTextComponent)colChooser).setText(Integer.toString(pos));
      }
    }
  }
  
  /** Create action listener to refresh preview */
  private ActionListener createActionListener() {
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
    if (this.preview==null && usePreview) {
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
			preview.setText(bundle.getString("EG")
					+ "\"" + previews[defaultValue] + "\"");
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
    return getColumnChooser(model, -1, false, null, true);
  }
  private static JComponent getColumnChooser(ComboBoxModel model, int defaultValue, boolean required, ActionListener l, boolean acceptOnlyIntegers) {
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
      JTextField tf;
      if (acceptOnlyIntegers) {
        tf =CSVReaderOptionPanel.buildIntegerBox(Integer.toString(defaultValue), l);
      } else {
      	tf = new JTextField(defaultValue);
      	tf.setColumns(30);
      	tf.addActionListener(l);
      }
      ret = tf;
    }
    
    return ret;
  }
  
  /**
   * Sequentially searches a string in an unsorted array.
   * @param array
   * @param toSearch
   * @return index if found, -1 else.
   */
  private static int indexOf(String[] array, String toSearch) {
    for (int i=0; i<array.length; i++) {
      if (array[i].equals(toSearch)) return i;
    }
    return-1;
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

  public static void addSelectorsToLayout(LayoutHelper lh, JColumnChooser jc) {
    addSelectorsToLayout(lh, jc, false);
  }
  public static void addSelectorsToLayout(LayoutHelper lh, JColumnChooser jc,
    boolean addSpace) {
    int i=0;
    lh.add(jc.label, (i++), lh.getRow(), 1, 1, 0d, 0d);
    lh.add(new JPanel(), (i++), lh.getRow(), 1, 1, 0d, 0d);
    lh.add(jc.colChooser, (i++), jc.usePreview?lh.getRow():1, 1, 0d, 0d);
    
    if (jc.usePreview) {
      lh.add(new JPanel(), (i++), lh.getRow(), 1, 1, 0d, 0d);
      lh.add(jc.preview, (i++), 1, 1, 0d, 0d);
    }
    
    if (addSpace) {
      lh.add(new JPanel(), 0, (i++), 1, 0d, 0d);
    }
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
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getOption()
   */
  public Option<?> getOption() {
    return option;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#isSetOption()
   */
  public boolean isSetOption() {
    return option!=null;
  }
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#setOption(de.zbit.util.prefs.Option)
   */
  public void setOption(Option<?> option) {
    this.option=option;
  }
}
