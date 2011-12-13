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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.zbit.gui.JLabeledComponent;
import de.zbit.gui.LayoutHelper;


/**
 * Column Chooser with a label, a columnChooser and a preview.
 * 
 * Very usefull, e.g., to assign column headers of a csv file to
 * certain propoerties (e.g. asking the user "in which column
 * are the sequence start coordinates?").
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class JColumnChooser extends JLabeledComponent {
  private static final long serialVersionUID = -9026612128266336630L;
  
  // Preview options
  private boolean usePreview=true;
  private String[] previews=null;
  private JLabel preview;
  
  
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
    super(title,fieldIsRequired,columnHeaders);
    usePreview = (preview!=null);
    setPreview(preview);
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
   * Should only be called when the layout changed.
   */
  protected void layoutElements() {
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
  
	
  /**
   * 
   * @param lh
   * @param jc
   * @param addSpace
   */
  public static void addSelectorsToLayout(LayoutHelper lh, JLabeledComponent jc, boolean addSpace) {
    int x = 0;
    lh.ensurePointerIsAtBeginningOfARow();
    
    lh.add(jc.getLabel(), (x++), lh.getRow(), 1, 1, 0d, 0d);
    lh.add(new JPanel(), (x++), lh.getRow(), 1, 1, 0d, 0d);
    if (jc instanceof JColumnChooser) {
      lh.add(jc.getColumnChooser(), (x++), ((JColumnChooser) jc).getUsePreview()?lh.getRow() : 1, 1, 0d, 0d);
    } else {
      lh.add(jc.getColumnChooser(), (x++), 1, 1, 0d, 0d);
    }
    if (jc instanceof JColumnChooser && ((JColumnChooser) jc).getUsePreview()) {
      lh.add(new JPanel(), (x++), lh.getRow(), 1, 1, 0d, 0d);
      lh.add(((JColumnChooser) jc).getPreview(), (x++), 1, 1, 0d, 0d);
    }
    if (addSpace) {
      lh.add(new JPanel(), 0, (x++), 1, 0d, 0d);
    }
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
   * Refresh the whole panel.
   */
  public void refresh() {
    refreshLabel();
    refreshSelector();
    refreshPreview();
    
    validateRepaint();
  }
  
  
  
  protected void refreshSelector() {
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
  
  /* (non-Javadoc)
   * @see de.zbit.gui.JLabeledComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (getUsePreview()) {
      getPreview().setEnabled(enabled);
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

/**
 * @return
 */
public boolean getUsePreview() {
	return usePreview;
}

/**
 * @return
 */
public Component getPreview() {
	return preview;
}
  
  
  
}
