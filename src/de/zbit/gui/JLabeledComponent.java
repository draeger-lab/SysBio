/*
 * $Id:  JLabeledComponent.java 16:17:34 keller $
 * $URL: JLabeledComponent.java $
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import de.zbit.gui.csv.CSVReaderOptionPanel;
import de.zbit.gui.csv.JColumnChooser;
import de.zbit.gui.prefs.JComponentForOption;
import de.zbit.io.CSVReader;
import de.zbit.util.ArrayUtils;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * A generic component with a label.
 * See {@link JColumnChooser} for the original implementation.
 * 
 * @author Roland Keller
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JLabeledComponent extends JPanel implements JComponentForOption, ItemSelectable {
  public static final transient Logger log = Logger.getLogger(JLabeledComponent.class.getName());
	protected static final long serialVersionUID = -9026612128266336630L;
	  
	  // Label options
	  protected String titel;
	  protected JLabel label;
	  
	  // Column selector options
	  /**
	   * Use a JTextField instead of JComboBoxes
	   */
	  protected boolean useJTextField=false;
	  protected boolean required;
	  protected Object[] headers=null;
	  protected ComboBoxModel model=null;
	  protected JComponent colChooser;
	  
	  
	  /**
	   * Only necessary for using this class in Combination with
	   * {@link SBPreferences} and {@link Option}s.
	   */
	  protected Option<?> option=null;
	  
	  /**
	   * This should always be true. Just if you want to use this
	   * class not for "Choosing columns" but other stuff, you may
	   * want to change this behaviour.
	   */
	  protected boolean acceptOnlyIntegers=true;
	  
	  
	  /**
	   * Every integer added here corresponds to one column number. If an
	   * integer is added, the column will be hidden in all ColumnChoosers.
	   */
	  protected ArrayList<Integer> hideColumns = new ArrayList<Integer>();
	  
	  /**
	   * If unsorted, the columns appear as they appear in the file. Else,
	   * they are sorted alphabetically.
	   */
	  protected boolean sortHeaders=false;

	  /**
	   * If true, the user may edit the given header strings.
	   */
    private boolean editHeaderAlllowed=false;
	  
	  protected static ResourceBundle bundle = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
	  protected static String noOptionChoosen=bundle.getString("NOT_AVAILABLE");
	  protected static String exampleError=bundle.getString("INVALID_COLUMN");
	  protected static String noSelection="n/a";
	  
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
	   * @param title - Label caption for this column chooser
	   * @param fieldIsRequired - If not required, this class will add
	   * a NoOptionChoosen String at the start of the box.
	   * @param columnHeaders - Column Headers
	   */
	  public JLabeledComponent(String title, boolean fieldIsRequired, Object[] columnHeaders) {
	    super();
	    initGUI();
	    
	    setTitle(title);
	    setName(title);
	    // setHeaders handles this variable.. so set it directly.
	    this.required = fieldIsRequired;
	    setHeaders(columnHeaders);
	    
	    label.setLabelFor(colChooser);
	  }
	  
	  
	  /**
	   * Creates a new column chooser which let's the user choose
	   * columns with JTextFields if no columnHeaders will be given.
	   * @param title - Label caption for this column chooser
	   * @param fieldIsRequired - If not required, this class will add
	   * a NoOptionChoosen String at the start of the box.
	   */
	  public JLabeledComponent(String title, boolean fieldIsRequired) {
	    this(title, fieldIsRequired, (Object[])null);
	  }
	  
	  /**
     * @param title
     * @param fieldIsRequired
     * @param organisms
     */
    @SuppressWarnings({ "rawtypes" })
    public JLabeledComponent(String title, boolean fieldIsRequired,
      Collection columnHeaders) {
      this(title, fieldIsRequired, columnHeaders.toArray());
    }
    /**
     * @param optionTitle
     * @param fieldIsRequired
     * @param colorChooserWithPreview
     */
    public JLabeledComponent(String optionTitle, boolean fieldIsRequired,
      JComponent c) {
      this (optionTitle, fieldIsRequired, new Object[]{c});
      GUITools.replaceComponent(colChooser, c);
      colChooser = c;
      label.setLabelFor(c);
    }
    
    
    /**
	   * 
	   */
	  protected void initGUI() {
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
		
    /* (non-Javadoc)
     * @see java.awt.ItemSelectable#getSelectedObjects()
     */
    @Override
    public Object[] getSelectedObjects() {
      return ArrayUtils.toArray(getSelectedItem());
    }
    /* (non-Javadoc)
     * @see java.awt.ItemSelectable#removeItemListener(java.awt.event.ItemListener)
     */
    @Override
    public void removeItemListener(ItemListener l) {
      JComponent comp = getColumnChooser();
      if (comp instanceof JComboBox) {
        ((JComboBox) comp).removeItemListener(l);
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
	  protected void layoutElements() {
	    LayoutManager l = getLayout();
	    
	    if (label!=null) l.removeLayoutComponent(label);
	    if (colChooser!=null) l.removeLayoutComponent(colChooser);
	    if (l instanceof BorderLayout) {
	      BorderLayout c = (BorderLayout) l;
	      if (label!=null) c.addLayoutComponent(label, BorderLayout.WEST);
	      if (colChooser!=null) c.addLayoutComponent(colChooser, BorderLayout.CENTER);
	      
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
	      
	    } else {
	      if (label!=null) l.addLayoutComponent("Titel", label);
	      if (colChooser!=null) l.addLayoutComponent("ColChooser", colChooser);
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
		public void setHeaders(Object[] header) {
			setHeaders(header, 0);
		}
		
		/**
		 * @see #setHeaders(Object[])
		 * @param header
		 */
    public void setHeaders(Collection<?> header) {
      setHeaders(header.toArray(), 0);
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
	  public void setHeaders(Object[] header, int numberOfColumns) {
	    // Set the header
	    Object[] newHeader=header;
	    int maxSize = newHeader!=null?newHeader.length:numberOfColumns;
	    maxSize = Math.max(numberOfColumns, maxSize);
	    newHeader = new Object[maxSize];
	    if (header!=null) System.arraycopy(header, 0, newHeader, 0, header.length);
			String column = bundle.getString("COLUMN");
			for (int i = 0; i < newHeader.length; i++) {
				// Completely empty array
				if (header == null)
					newHeader[i] = column + (i + 1);
				// Just fill missing gaps
				else if (newHeader[i] == null || newHeader[i].toString().trim().length() < 1)
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
	  protected void refreshAndRepaint() {
	    model = buildComboBoxModel(headers);
	    
	    refreshSelector();
	    validateRepaint();
	  }
	  
	  protected ComboBoxModel buildComboBoxModel(Object[] newHeader) {
	  	if (newHeader==null || newHeader.length<1) return null;
	    
	    // Create a list, hiding all unwanted elements
	  	Vector<Object> modelHeaders = new Vector<Object>();
	    Map<String, Object> headersToDisplay = new HashMap<String, Object>();
	    for (int i=0; i<newHeader.length; i++) {
	      if (hideColumns.contains(i)) continue;
	      headersToDisplay.put(newHeader[i].toString(), newHeader[i]);
	      modelHeaders.add(newHeader[i]);
	    }
	    
	    // Sort eventually
	    if (sortHeaders) {
	      ArrayList<String> keys = new ArrayList<String> (headersToDisplay.keySet());
	      Collections.sort(keys);
	      for (int i=0; i<keys.size(); i++)
	        modelHeaders.set(i, headersToDisplay.get(keys.get(i)));
	    }
	    
	    // If not required, add noOptionChoosen
	    if (!required) {
	      modelHeaders.add(0, noOptionChoosen);
	    }
	    
	    // Build the model
	    return new DefaultComboBoxModel(modelHeaders);
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
	          break;
	        }
	      }
	    }
	    setSelectedValue(i);
	  }
	  
	  /**
	   * Set the default value of the column chooser.
	   * @param s set the default index to this value's index.
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
     * Set the default value of the column chooser.
     * @param s set the default index to this value's index.
     */
    public void setDefaultValue(Object defaultV) {
      if (model!=null) {
        model.setSelectedItem(defaultV);
      } else {
        setDefaultValue(defaultV.toString());
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
	  
	  protected void refreshSelector() {
	    int id = -1;
	    if (colChooser!=null) {
	      id = getSelectedValue();;
	      remove(colChooser);
	    }
	    
	    // Column chooser
	    colChooser = getColumnChooser(useJTextField?null:model, -1, required, null, acceptOnlyIntegers);
      if (colChooser instanceof JComboBox) {
        ((JComboBox)colChooser).setEditable(this.editHeaderAlllowed);
      }
	    colChooser.setToolTipText(getToolTipText());
	    if (getLayout() instanceof GridBagLayout) {
	      addComponent(this, colChooser, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
	    } else if (getLayout() instanceof BorderLayout) {
	      add(colChooser, BorderLayout.CENTER);
	    } else {
	      add(colChooser); // e.g. GridLayout
	    } 
	    
	    if (id>=0) setSelectedValue(id);
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
	    } else if (colChooser instanceof JTextComponent) {
	      String s = ((JTextComponent)colChooser).getText().trim();
	      
	      if (CSVReader.isNumber(s, true)) return Integer.parseInt(s);
	      else return -1;
	    } else {
	      return -1; // Unknown
	    }
	  }
	  
	  
	  /**
	   * Returns the selected item (Usually a header string).
	   * @return
	   */
	  public Object getSelectedItem() {
	    if (colChooser instanceof JComboBox) {
	      return ((JComboBox)colChooser).getSelectedItem();
	    } else if (colChooser instanceof JTextComponent) {
	      String s = ((JTextComponent)colChooser).getText();
	      return s;
	    } else if (colChooser instanceof ColorChooserWithPreview) {
	      return colChooser.getBackground();
	    } else {
	      log.severe("Please implement getSelectedItem() for " + colChooser.getClass() + " in JLabeledComponent.");
	      return null;
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
	    } else if (colChooser instanceof JTextComponent) {
	      ((JTextComponent)colChooser).setText(Integer.toString(i));
	    } else {
	      log.warning("Cannot set selected integer value on " + colChooser.getClass());
	    }
	  }
	  
	  /**
	   * Set the selected item to the given one.
	   * @param string
	   */
	  public void setSelectedItem(Object string) {
	    if (colChooser instanceof JComboBox) {
	      ((JComboBox)colChooser).setSelectedItem(string);
	    } else if (colChooser instanceof JTextComponent) {
	      int pos = indexOf(headers, string.toString());
	      if (pos<0) {
	        if (CSVReader.isNumber(string.toString(), false)) {
	          ((JTextComponent)colChooser).setText(string.toString());
	        }
	      } else {
	        ((JTextComponent)colChooser).setText(Integer.toString(pos));
	      }
	    } else if (colChooser instanceof ColorChooserWithPreview) {
	      if (string instanceof java.awt.Color) {
	      ((ColorChooserWithPreview) colChooser).setColor((Color)string);
	      }
	    } else {
	      log.warning("Please implement setSelectedItem for" + colChooser.getClass());
	    }
	  }
	  
	  
	  
	  protected void refreshLabel() {
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
	  
	  protected void validateRepaint() {
	    validate();
	    repaint();
	  }
	  
	  protected static JComponent getColumnChooser(ComboBoxModel model) {
	    return getColumnChooser(model, -1, false, null, true);
	  }
	  protected static JComponent getColumnChooser(ComboBoxModel model, int defaultValue, boolean required, ActionListener l, boolean acceptOnlyIntegers) {
	    JComponent ret;
	    
	    if (model!=null && model.getSize()>0) {
	      JComboBox cb = new JComboBox(model);
	      // XXX: Feature possibility: remove the selected element in all other ComboBoxes.
	      if (l!=null) cb.addActionListener(l);
	      
	      int def = defaultValue+(required?0:1);
	      if (def<0 || def>=model.getSize()) def=0;
	      cb.setSelectedIndex(def);
	      // Configure an improved renderer that allows more customization.
	      cb.setRenderer(new ActionCommandRenderer());
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
	  protected static int indexOf(Object[] array, Object toSearch) {
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

	  public static void addSelectorsToLayout(LayoutHelper lh, JLabeledComponent jc) {
	    addSelectorsToLayout(lh, jc, false);
	  }
	  public static void addSelectorsToLayout(LayoutHelper lh, JLabeledComponent jc, boolean addSpace) {
	    int i=0;
	    lh.add(jc.label, (i++), lh.getRow(), 1, 1, 0d, 0d);
	    lh.add(new JPanel(), (i++), lh.getRow(), 1, 1, 0d, 0d);
	    if(jc instanceof JColumnChooser) {
	    	lh.add(jc.colChooser, (i++), ((JColumnChooser)jc).getUsePreview()?lh.getRow():1, 1, 0d, 0d);
	    }
	    else {
	    	lh.add(jc.colChooser, (i++), 1, 1, 0d, 0d);
	    }
	    if (jc instanceof JColumnChooser && ((JColumnChooser)jc).getUsePreview()) {
	        lh.add(new JPanel(), (i++), lh.getRow(), 1, 1, 0d, 0d);
	        lh.add(((JColumnChooser)jc).getPreview(), (i++), 1, 1, 0d, 0d);
	    }
	    if (addSpace) {
	      lh.add(new JPanel(), 0, (i++), 1, 0d, 0d);
	    }
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
    /* (non-Javadoc)
     * @see de.zbit.gui.prefs.JComponentForOption#getCurrentValue()
     */
    public Object getCurrentValue() {
      return getSelectedItem();
    }

    /**
     * Show a dialog with multiple {@link JLabeledComponent}s. Each component
     * has the label given in <code>fields</code> and the selections from the
     * <code>suggestions</code> variable.
     * @param parent the parent to which this dialog is modal
     * @param title  title for this dialog
     * @param fields labels for {@link JLabeledComponent}s
     * @param suggestions available choices for {@link JLabeledComponent}s (same indices as fields)
     * @param fixedSuggestions if true, only choices from the given selections arrays are allowed.
     * If false, users may enter custom strings.
     * @return array with selected values for each field
     */
    public static String[] showDialog(Component parent, String title, String[] fields, String[][] suggestions, boolean fixedSuggestions) {

      // Initialize the panel
      final JPanel c = new JPanel(new VerticalLayout());
      for (int i=0; i<fields.length; i++) {
        JLabeledComponent l = new JLabeledComponent(fields[i],true, suggestions[i]);
        if (!fixedSuggestions) l.setEditHeaderAllowed(true);
        // Make a flexible layout
        l.setLayout(new FlowLayout());
        l.setPreferredSize(null);
        c.add(l);
      }
      c.setBorder(new TitledBorder(title));
      
      // Show as Dialog
      int button = GUITools.showAsDialog(parent, c, title, true);
      
      // OK Pressed?
      boolean okPressed = (button==JOptionPane.OK_OPTION);
      if (!okPressed) return null;
      else {
        String[] ret = new String[fields.length];
        for (int i=0; i<fields.length; i++) {
          ret[i] = ((JLabeledComponent)c.getComponent(i)).getSelectedItem().toString();
        }
        return ret;
      }
    }
    
    /**
     * @param b if true, the given {@link #headers} may be edited
     * and customized by the user!
     */
    public void setEditHeaderAllowed(boolean b) {
      this.editHeaderAlllowed=true;
      if (colChooser instanceof JComboBox) {
        ((JComboBox)colChooser).setEditable(b);
      }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      if (label!=null) label.setEnabled(enabled);
      if (colChooser!=null) colChooser.setEnabled(enabled);
    }
    
    /**
     * Just for testing purposes.
     */
    public static void main(String[] args) {
      String[][] sug = new String[][]{{"A","B","C"}, {"A","D","E"}, {"A","F","G"}};
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame parent = new JFrame();
        parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] ret = showDialog(parent,"My title", new String[]{"1.", "2.", "3."}, sug, false);
        if (ret==null) System.out.println("Cancelled");
        else System.out.println(Arrays.deepToString(ret));
        System.exit(0);
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      /*JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JLabeledComponent c = new JLabeledComponent("test", false, new String[]{"header1", "header2"});
        frame.getContentPane().add(c);
      } catch (Exception e) {
        e.printStackTrace();
      }
      frame.pack();
      frame.setVisible(true);*/
    }
    
	}