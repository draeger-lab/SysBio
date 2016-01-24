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
package de.zbit.gui;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.zbit.gui.layout.LayoutHelper;
import de.zbit.gui.prefs.JComponentForOption;
import de.zbit.gui.prefs.PreferencesPanel;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.Option;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class JFontChooserPanel extends JPanel implements JComponentForOption {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -8081168247712008139L;
  
  /**
   * For demonstration purposes.
   * 
   * @param args
   *        nothing required.
   */
  public static void main(String args[]) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(new JFontChooserPanel());
    f.pack();
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }
  
  /**
   * Localization support.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
  
  /**
   * 
   */
  private JToggleButton buttonBold;
  /**
   * 
   */
  private JToggleButton buttonItalic;
  /**
   * 
   */
  private JFontChooserComboBox combo;
  /**
   * 
   */
  private JSpinner size;
  
  /**
   * 
   */
  private Option<?> option;
  
  /**
   * 
   */
  public JFontChooserPanel() {
    this(new Font("Arial", Font.PLAIN, 12));
  }
  
  /**
   * 
   * @param initial pre-selected font. must not be {@code null}.
   */
  public JFontChooserPanel(Font initial) {
    super(new GridBagLayout());
    changeListeners = new LinkedList<ChangeListener>();
    
    combo = new JFontChooserComboBox();
    combo.setSelectedItem(initial.getFontName());
    size = new JSpinner(new SpinnerNumberModel(12f, 0f, 1260f, 1f));
    size.setValue(Integer.valueOf(initial.getSize()));
    size.addChangeListener(new ChangeListener() {
      /* (non-Javadoc)
       * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
       */
      @Override
      public void stateChanged(ChangeEvent e) {
        fireStateChangedEvent();
      }
    });
    buttonBold = new JToggleButton(bundle.getString("BUTTON_BOLD_FONT"));
    buttonBold.setToolTipText(bundle.getString("BUTTON_BOLD_FONT_TOOLTIP"));
    buttonItalic = new JToggleButton(bundle.getString("BUTTON_ITALIC_FONT"));
    buttonItalic.setToolTipText(bundle.getString("BUTTON_ITALIC_FONT_TOOLTIP"));
    buttonBold.setFont(buttonBold.getFont().deriveFont(Font.BOLD));
    buttonItalic.setFont(buttonItalic.getFont().deriveFont(Font.ITALIC));
    buttonBold.setSelected(initial.isBold());
    buttonItalic.setSelected(initial.isItalic());
    buttonBold.addItemListener(new ItemListener() {
      /* (non-Javadoc)
       * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fireStateChangedEvent();
        }
      }});
    buttonItalic.addChangeListener(new ChangeListener() {
      /* (non-Javadoc)
       * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
       */
      @Override
      public void stateChanged(ChangeEvent e) {
        fireStateChangedEvent();
      }
    });
    
    buttonBold.setBorderPainted(false);
    buttonItalic.setBorderPainted(false);
    buttonBold.addMouseListener(new MouseOverListener(buttonBold));
    buttonItalic.addMouseListener(new MouseOverListener(buttonItalic));
    combo.setBorder(BorderFactory.createTitledBorder(" " + bundle.getString("NAME") + " "));
    combo.addItemListener(new ItemListener() {
      /* (non-Javadoc)
       * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fireStateChangedEvent();
        }
      }
    });
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.add(buttonBold);
    p.add(buttonItalic);
    p.setBorder(BorderFactory.createTitledBorder(" " + bundle.getString("STYLE") + " "));
    size.setBorder(BorderFactory.createTitledBorder(" " + bundle.getString("SIZE") + " "));
    
    LayoutHelper lh = new LayoutHelper(this);
    lh.add(combo, 0, 0, 1, 1, 1d, 0d, 0, 0, 0, 0, 0, 5);
    lh.add(p,     1, 0, 1, 1, 1d, 0d, 0, 0, 0, 0, 5, 5);
    lh.add(size,  2, 0, 1, 1, 1d, 0d, 0, 0, 0, 0, 5, 0);
  }
  
  /**
   * 
   */
  private void fireStateChangedEvent() {
    ChangeEvent e = new ChangeEvent(this);
    for (ChangeListener c: changeListeners) {
      c.stateChanged(e);
    }
  }
  
  /**
   * 
   * @return the selected font with style and size.
   */
  public Font getSelectedFont() {
    int style = Font.BOLD + Font.ITALIC;
    if (!buttonBold.isSelected()) {
      style -= Font.BOLD;
    }
    if (!buttonItalic.isSelected()) {
      style -= Font.ITALIC;
    }
    Font font = combo.getSelectedFont();
    return font.deriveFont(style, Float.parseFloat(size.getValue().toString()));
  }
  
  /**
   * 
   * @author Andreas Dr&auml;ger
   * @version $Rev$
   */
  private static class MouseOverListener implements MouseListener {
    
    /**
     * 
     */
    private AbstractButton b;
    
    /**
     * @param button the button for which this listener is registered.
     */
    public MouseOverListener(AbstractButton button) {
      b = button;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
      b.setBorderPainted(b.isSelected());
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
      b.setBorderPainted(true);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getOption()
   */
  @Override
  public Option<?> getOption() {
    return option;
  }
  
  /**
   * A list of {@link ChangeListener}s to be notified in case that values change
   * on this {@link PreferencesPanel}.
   */
  private List<ChangeListener> changeListeners;
  
  /**
   * 
   * @param listener
   */
  public void addChangeListener(ChangeListener listener) {
    changeListeners.add(listener);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#isSetOption()
   */
  @Override
  public boolean isSetOption() {
    return option != null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#setOption(de.zbit.util.prefs.Option)
   */
  @Override
  public void setOption(Option<?> option) {
    this.option = option;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getCurrentValue()
   */
  @Override
  public Font getCurrentValue() {
    return getSelectedFont();
  }
  
}
