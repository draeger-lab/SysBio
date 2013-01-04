/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import javax.swing.Icon;

/**
 * The model for the Wizard component, which tracks the text, icons, and enabled
 * state of each of the buttons, as well as the current panel that is displayed.
 * Note that the model, in its current form, is not intended to be subclassed.
 * 
 * 
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class WizardModel {

  /**
   * Identification string for the current panel.
   */
  public static final String CURRENT_PANEL_DESCRIPTOR_PROPERTY = "currentPanelDescriptorProperty";

  /**
   * Property identification String for the Back button's text
   */
  public static final String BACK_BUTTON_TEXT_PROPERTY = "backButtonTextProperty";
  /**
   * Property identification String for the Back button's icon
   */
  public static final String BACK_BUTTON_ICON_PROPERTY = "backButtonIconProperty";
  /**
   * Property identification String for the Back button's enabled state
   */
  public static final String BACK_BUTTON_ENABLED_PROPERTY = "backButtonEnabledProperty";

  /**
   * Property identification String for the Next button's text
   */
  public static final String NEXT_FINISH_BUTTON_TEXT_PROPERTY = "nextButtonTextProperty";
  /**
   * Property identification String for the Next button's icon
   */
  public static final String NEXT_FINISH_BUTTON_ICON_PROPERTY = "nextButtonIconProperty";
  /**
   * Property identification String for the Next button's enabled state
   */
  public static final String NEXT_FINISH_BUTTON_ENABLED_PROPERTY = "nextButtonEnabledProperty";

  /**
   * Property identification String for the Cancel button's text
   */
  public static final String CANCEL_BUTTON_TEXT_PROPERTY = "cancelButtonTextProperty";
  /**
   * Property identification String for the Cancel button's icon
   */
  public static final String CANCEL_BUTTON_ICON_PROPERTY = "cancelButtonIconProperty";
  /**
   * Property identification String for the Cancel button's enabled state
   */
  public static final String CANCEL_BUTTON_ENABLED_PROPERTY = "cancelButtonEnabledProperty";

  /**
   * Property identification String for the Help button's text
   */
  public static final String HELP_BUTTON_TEXT_PROPERTY = "helpButtonTextProperty";
  /**
   * Property identification String for the Help button's icon
   */
  public static final String HELP_BUTTON_ICON_PROPERTY = "helpButtonIconProperty";
  /**
   * Property identification String for the Help button's enabled state
   */
  public static final String HELP_BUTTON_ENABLED_PROPERTY = "helpButtonEnabledProperty";

  /**
   * Property identification String for the warning panel's text
   */
  public static final String WARNING_MESSAGE_TEXT_PROPERTY = "warningMessageTextProperty";
  /**
   * Property identification String for the warning panel's text
   */
  public static final String WARNING_MESSAGE_ICON_PROPERTY = "warningMessageIconProperty";

  private WizardPanelDescriptor currentPanel;

  private HashMap<Object, WizardPanelDescriptor> panelHashmap;

  private HashMap<String, String> buttonTextHashmap;
  private HashMap<String, Icon> buttonIconHashmap;
  private HashMap<String, Boolean> buttonEnabledHashmap;

  private String warningText;
  private Icon warningIcon;

  private PropertyChangeSupport propertyChangeSupport;

  /**
   * Default constructor.
   */
  public WizardModel() {

    panelHashmap = new HashMap<Object, WizardPanelDescriptor>();

    buttonTextHashmap = new HashMap<String, String>();
    buttonIconHashmap = new HashMap<String, Icon>();
    buttonEnabledHashmap = new HashMap<String, Boolean>();

    warningText = "";
    warningIcon = null;

    propertyChangeSupport = new PropertyChangeSupport(this);

  }

  /**
   * Returns the currently displayed WizardPanelDescriptor.
   * 
   * @return The currently displayed WizardPanelDescriptor
   */
  WizardPanelDescriptor getCurrentPanelDescriptor() {
    return currentPanel;
  }

  /**
   * Registers the WizardPanelDescriptor in the model using the
   * Object-identifier specified.
   * 
   * @param id Object-based identifier
   * @param descriptor WizardPanelDescriptor that describes the panel
   */
  void registerPanel(Object id, WizardPanelDescriptor descriptor) {

    //  Place a reference to it in a hashtable so we can access it later
    //  when it is about to be displayed.

    panelHashmap.put(id, descriptor);

  }

  /**
   * Returns the WizardPanelDescriptor with the given Object-identifier as it
   * was registered to this model.
   * 
   * @param id Object-based identifier
   * @return WizardPanelDescriptor with the given identifier
   */
  WizardPanelDescriptor getPanel(Object id) {
    return panelHashmap.get(id);
  }
  
  /**
   * Sets the current panel to that identified by the Object passed in.
   * 
   * @param id Object-based panel identifier
   * @return boolean indicating success or failure
   */
  boolean setCurrentPanel(Object id) {

    //  First, get the hashtable reference to the panel that should
    //  be displayed.

    WizardPanelDescriptor nextPanel = (WizardPanelDescriptor) panelHashmap.get(id);

    //  If we couldn't find the panel that should be displayed, return
    //  false.

    if (nextPanel == null)
      throw new WizardPanelNotFoundException();

    WizardPanelDescriptor oldPanel = currentPanel;
    currentPanel = nextPanel;

    if (oldPanel != currentPanel)
      firePropertyChange(CURRENT_PANEL_DESCRIPTOR_PROPERTY, oldPanel, currentPanel);

    return true;

  }

  /**
   * 
   * @return
   */
  String getBackButtonText() {
    return buttonTextHashmap.get(BACK_BUTTON_TEXT_PROPERTY);
  }

  /**
   * 
   * @param newText
   */
  void setBackButtonText(String newText) {

    String oldText = getBackButtonText();
    if (!newText.equals(oldText)) {
      buttonTextHashmap.put(BACK_BUTTON_TEXT_PROPERTY, newText);
      firePropertyChange(BACK_BUTTON_TEXT_PROPERTY, oldText, newText);
    }
  }

  /**
   * 
   * @return
   */
  String getNextFinishButtonText() {
    return buttonTextHashmap.get(NEXT_FINISH_BUTTON_TEXT_PROPERTY);
  }

  /**
   * 
   * @param newText
   */
  void setNextFinishButtonText(String newText) {
    String oldText = getNextFinishButtonText();
    if (!newText.equals(oldText)) {
      buttonTextHashmap.put(NEXT_FINISH_BUTTON_TEXT_PROPERTY, newText);
      firePropertyChange(NEXT_FINISH_BUTTON_TEXT_PROPERTY, oldText, newText);
    }
  }

  /**
   * 
   * @return
   */
  String getCancelButtonText() {
    return buttonTextHashmap.get(CANCEL_BUTTON_TEXT_PROPERTY);
  }

  /**
   * 
   * @param newText
   */
  void setCancelButtonText(String newText) {
    String oldText = getCancelButtonText();
    if (!newText.equals(oldText)) {
      buttonTextHashmap.put(CANCEL_BUTTON_TEXT_PROPERTY, newText);
      firePropertyChange(CANCEL_BUTTON_TEXT_PROPERTY, oldText, newText);
    }
  }

  /**
   * 
   * @return
   */
  String getHelpButtonText() {
    return buttonTextHashmap.get(HELP_BUTTON_TEXT_PROPERTY);
  }

  /**
   * 
   * @param newText
   */
  void setHelpButtonText(String newText) {
    String oldText = getHelpButtonText();
    if (!newText.equals(oldText)) {
      buttonTextHashmap.put(HELP_BUTTON_TEXT_PROPERTY, newText);
      firePropertyChange(HELP_BUTTON_TEXT_PROPERTY, oldText, newText);
    }
  }

  String getWarningText() {
    return warningText;
  }

  /**
   * 
   * @param newText
   */
  void setWarningText(String newText) {
    String oldText = getWarningText();
    warningText = newText;
    firePropertyChange(WARNING_MESSAGE_TEXT_PROPERTY, oldText, newText);
  }

  /**
   * 
   * @return
   */
  Icon getBackButtonIcon() {
    return (Icon) buttonIconHashmap.get(BACK_BUTTON_ICON_PROPERTY);
  }

  /**
   * 
   * @param newIcon
   */
  void setBackButtonIcon(Icon newIcon) {
    Icon oldIcon = getBackButtonIcon();
    if (!newIcon.equals(oldIcon)) {
      buttonIconHashmap.put(BACK_BUTTON_ICON_PROPERTY, newIcon);
      firePropertyChange(BACK_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
    }
  }

  /**
   * 
   * @return
   */
  Icon getNextFinishButtonIcon() {
    return (Icon) buttonIconHashmap.get(NEXT_FINISH_BUTTON_ICON_PROPERTY);
  }

  /**
   * 
   * @param newIcon
   */
  public void setNextFinishButtonIcon(Icon newIcon) {
    Icon oldIcon = getNextFinishButtonIcon();
    if (!newIcon.equals(oldIcon)) {
      buttonIconHashmap.put(NEXT_FINISH_BUTTON_ICON_PROPERTY, newIcon);
      firePropertyChange(NEXT_FINISH_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
    }
  }

  /**
   * 
   * @return
   */
  Icon getCancelButtonIcon() {
    return (Icon) buttonIconHashmap.get(CANCEL_BUTTON_ICON_PROPERTY);
  }

  /**
   * 
   * @param newIcon
   */
  void setCancelButtonIcon(Icon newIcon) {
    Icon oldIcon = getCancelButtonIcon();
    if (!newIcon.equals(oldIcon)) {
      buttonIconHashmap.put(CANCEL_BUTTON_ICON_PROPERTY, newIcon);
      firePropertyChange(CANCEL_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
    }
  }

  /**
   * 
   * @return
   */
  Icon getHelpButtonIcon() {
    return (Icon) buttonIconHashmap.get(HELP_BUTTON_ICON_PROPERTY);
  }

  /**
   * 
   * @param newIcon
   */
  void setHelpButtonIcon(Icon newIcon) {
    Icon oldIcon = getHelpButtonIcon();
    if (!newIcon.equals(oldIcon)) {
      buttonIconHashmap.put(HELP_BUTTON_ICON_PROPERTY, newIcon);
      firePropertyChange(HELP_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
    }
  }

  /**
   * 
   * @return
   */
  Icon getWarningIcon() {
    return warningIcon;
  }

  /**
   * 
   * @param newIcon
   */
  void setWarningIcon(Icon newIcon) {
    Icon oldIcon = getWarningIcon();
    if (newIcon != oldIcon) {
      warningIcon = newIcon;
      firePropertyChange(WARNING_MESSAGE_ICON_PROPERTY, oldIcon, newIcon);
    }
  }

  /**
   * 
   * @return
   */
  Boolean getBackButtonEnabled() {
    return buttonEnabledHashmap.get(BACK_BUTTON_ENABLED_PROPERTY);
  }

  /**
   * 
   * @param newValue
   */
  void setBackButtonEnabled(Boolean newValue) {
    Boolean oldValue = getBackButtonEnabled();
    if (newValue != oldValue) {
      buttonEnabledHashmap.put(BACK_BUTTON_ENABLED_PROPERTY, newValue);
      firePropertyChange(BACK_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
    }
  }

  /**
   * 
   * @return
   */
  Boolean getNextFinishButtonEnabled() {
    return buttonEnabledHashmap.get(NEXT_FINISH_BUTTON_ENABLED_PROPERTY);
  }

  /**
   * 
   * @param newValue
   */
  void setNextFinishButtonEnabled(Boolean newValue) {
    Boolean oldValue = getNextFinishButtonEnabled();
    if (newValue != oldValue) {
      buttonEnabledHashmap.put(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, newValue);
      firePropertyChange(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
    }
  }

  /**
   * 
   * @return
   */
  Boolean getCancelButtonEnabled() {
    return buttonEnabledHashmap.get(CANCEL_BUTTON_ENABLED_PROPERTY);
  }

  /**
   * 
   * @param newValue
   */
  void setCancelButtonEnabled(Boolean newValue) {
    Boolean oldValue = getCancelButtonEnabled();
    if (newValue != oldValue) {
      buttonEnabledHashmap.put(CANCEL_BUTTON_ENABLED_PROPERTY, newValue);
      firePropertyChange(CANCEL_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
    }
  }
  
  /**
   * 
   * @return
   */
  Boolean getHelpButtonEnabled() {
    return buttonEnabledHashmap.get(HELP_BUTTON_ENABLED_PROPERTY);
  }

  /**
   * 
   * @param newValue
   */
  void setHelpButtonEnabled(Boolean newValue) {
    Boolean oldValue = getHelpButtonEnabled();
    if (newValue != oldValue) {
      buttonEnabledHashmap.put(HELP_BUTTON_ENABLED_PROPERTY, newValue);
      firePropertyChange(HELP_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
    }
  }
  
  /**
   * 
   * @param p
   */
  public void addPropertyChangeListener(PropertyChangeListener p) {
    propertyChangeSupport.addPropertyChangeListener(p);
  }

  /**
   * 
   * @param p
   */
  public void removePropertyChangeListener(PropertyChangeListener p) {
    propertyChangeSupport.removePropertyChangeListener(p);
  }

  /**
   * 
   * @param propertyName
   * @param oldValue
   * @param newValue
   */
  protected void firePropertyChange(String propertyName, Object oldValue,
                                    Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

}
