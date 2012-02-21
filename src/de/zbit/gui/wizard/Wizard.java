/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * This class implements a basic wizard dialog, where the programmer can insert
 * one or more Components to act as panels. These panels can be navigated
 * through arbitrarily using the 'Next' or 'Back' buttons, or the dialog itself
 * can be closed using the 'Cancel' button. Note that even though the dialog
 * uses a CardLayout manager, the order of the panels is not linear. Each panel
 * determines at runtime what its next and previous panel will be.
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
public class Wizard extends WindowAdapter implements PropertyChangeListener {

  /**
   * Indicates that the 'Finish' button was pressed to close the dialog.
   */
  public static final int FINISH_RETURN_CODE = 0;
  /**
   * Indicates that the 'Cancel' button was pressed to close the dialog, or the
   * user pressed the close box in the corner of the window.
   */
  public static final int CANCEL_RETURN_CODE = 1;
  /**
   * Indicates that the dialog closed due to an internal error.
   */
  public static final int ERROR_RETURN_CODE = 2;

  /**
   * The String-based action command for the 'Next' button.
   */
  public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
  /**
   * The String-based action command for the 'Back' button.
   */
  public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
  /**
   * The String-based action command for the 'Cancel' button.
   */
  public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";

  // The i18n text used for the buttons. Loaded from a property resource file.    

  static String BACK_TEXT;
  static String NEXT_TEXT;
  static String FINISH_TEXT;
  static String CANCEL_TEXT;

  // The image icons used for the buttons. Filenames are loaded from a property resource file.    

  static Icon BACK_ICON;
  static Icon NEXT_ICON;
  static Icon FINISH_ICON;
  static Icon CANCEL_ICON;
  
  static Icon WARNING_ICON;
  static Icon ERROR_ICON;

  private WizardModel wizardModel;
  private WizardController wizardController;
  private JDialog wizardDialog;

  private JPanel cardPanel;
  private CardLayout cardLayout;
  private JButton backButton;
  private JButton nextButton;
  private JButton cancelButton;
  private JPanel warningPanel;
  private JLabel warningLabel;

  private int returnCode;

  /**
   * Default constructor. This method creates a new WizardModel object and
   * passes it into the overloaded constructor.
   */
  public Wizard() {
    this((Frame) null);
  }

  /**
   * This method accepts a java.awt.Dialog object as the javax.swing.JDialog's
   * parent.
   * 
   * @param owner The java.awt.Dialog object that is the owner of this dialog.
   */
  public Wizard(Dialog owner) {
    wizardModel = new WizardModel();
    wizardDialog = new JDialog(owner);
    initComponents();
  }

  /**
   * This method accepts a java.awt.Frame object as the javax.swing.JDialog's
   * parent.
   * 
   * @param owner The java.awt.Frame object that is the owner of the
   *          javax.swing.JDialog.
   */
  public Wizard(Frame owner) {
    wizardModel = new WizardModel();
    wizardDialog = new JDialog(owner);
    initComponents();
  }

  /**
   * Returns an instance of the JDialog that this class created. This is useful
   * in the event that you want to change any of the JDialog parameters
   * manually.
   * 
   * @return The JDialog instance that this class created.
   */
  public JDialog getDialog() {
    return wizardDialog;
  }

  /**
   * Returns the owner of the generated javax.swing.JDialog.
   * 
   * @return The owner (java.awt.Frame or java.awt.Dialog) of the
   *         javax.swing.JDialog generated by this class.
   */
  public Component getOwner() {
    return wizardDialog.getOwner();
  }

  /**
   * Sets the title of the generated javax.swing.JDialog.
   * 
   * @param s The title of the dialog.
   */
  public void setTitle(String s) {
    wizardDialog.setTitle(s);
  }

  /**
   * Returns the current title of the generated dialog.
   * 
   * @return The String-based title of the generated dialog.
   */
  public String getTitle() {
    return wizardDialog.getTitle();
  }

  /**
   * Sets the modality of the generated javax.swing.JDialog.
   * 
   * @param b the modality of the dialog
   */
  public void setModal(boolean b) {
    wizardDialog.setModal(b);
  }

  /**
   * Returns the modality of the dialog.
   * 
   * @return A boolean indicating whether or not the generated
   *         javax.swing.JDialog is modal.
   */
  public boolean isModal() {
    return wizardDialog.isModal();
  }

  /**
   * Convenience method that displays a modal wizard dialog and blocks until the
   * dialog has completed.
   * 
   * @return Indicates how the dialog was closed. Compare this value against the
   *         RETURN_CODE constants at the beginning of the class.
   */
  public int showModalDialog() {

    wizardDialog.setModal(true);
    wizardDialog.pack();
    wizardDialog.setVisible(true);

    return returnCode;
  }

  /**
   * Returns the current model of the wizard dialog.
   * 
   * @return A WizardModel instance, which serves as the model for the wizard
   *         dialog.
   */
  public WizardModel getModel() {
    return wizardModel;
  }

  /**
   * Add a Component as a panel for the wizard dialog by registering its
   * WizardPanelDescriptor object. Each panel is identified by a unique
   * Object-based identifier (often a String), which can be used by the
   * setCurrentPanel() method to display the panel at runtime.
   * 
   * @param id An Object-based identifier used to identify the
   *          WizardPanelDescriptor object.
   * @param panel The WizardPanelDescriptor object which contains helpful
   *          information about the panel.
   */
  public void registerWizardPanel(Object id, WizardPanelDescriptor panel) {

    //  Add the incoming panel to our JPanel display that is managed by
    //  the CardLayout layout manager.

    cardPanel.add(panel.getPanelComponent(), id);

    //  Set a callback to the current wizard.

    panel.setWizard(this);

    //  Place a reference to it in the model. 

    wizardModel.registerPanel(id, panel);

  }

  /**
   * Returns the WizardPanelDescriptor with the given Object-identifier as it
   * was registered to the model of this wizard.
   * 
   * @param id Object-based identifier
   * @return WizardPanelDescriptor with the given identifier
   */
  public WizardPanelDescriptor getPanel(Object id) {
    return wizardModel.getPanel(id);
  }
  
  /**
   * Displays the panel identified by the object passed in. This is the same
   * Object-based identified used when registering the panel.
   * 
   * @param id The Object-based identifier of the panel to be displayed.
   */
  public void setCurrentPanel(Object id) {

    //  Get the hashtable reference to the panel that should
    //  be displayed. If the identifier passed in is null, then close
    //  the dialog.

    if (id == null)
      close(ERROR_RETURN_CODE);

    WizardPanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();
    if (oldPanelDescriptor != null)
      oldPanelDescriptor.aboutToHidePanel();

    wizardModel.setCurrentPanel(id);
    wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();

    //  Show the panel in the dialog.

    cardLayout.show(cardPanel, id.toString());
    wizardModel.getCurrentPanelDescriptor().displayingPanel();

  }

  /**
   * Method used to listen for property change events from the model and update
   * the dialog's graphical components as necessary.
   * 
   * @param evt PropertyChangeEvent passed from the model to signal that one of
   *          its properties has changed value.
   */
  public void propertyChange(PropertyChangeEvent evt) {

    if (evt.getPropertyName().equals(WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY)) {
      wizardController.resetButtonsToPanelRules();
    } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY)) {
      nextButton.setText(evt.getNewValue().toString());
    } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_TEXT_PROPERTY)) {
      backButton.setText(evt.getNewValue().toString());
    } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_TEXT_PROPERTY)) {
      cancelButton.setText(evt.getNewValue().toString());
    } else if (evt.getPropertyName().equals(WizardModel.WARNING_MESSAGE_TEXT_PROPERTY)) {
      warningLabel.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY)) {
      nextButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
    } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ENABLED_PROPERTY)) {
      backButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
    } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY)) {
      cancelButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
    } else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY)) {
      nextButton.setIcon((Icon) evt.getNewValue());
    } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ICON_PROPERTY)) {
      backButton.setIcon((Icon) evt.getNewValue());
    } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ICON_PROPERTY)) {
      cancelButton.setIcon((Icon) evt.getNewValue());
    } else if (evt.getPropertyName().equals(WizardModel.WARNING_MESSAGE_ICON_PROPERTY)) {
      warningLabel.setIcon((Icon) evt.getNewValue());
    }

  }

  /**
   * Retrieves the last return code set by the dialog.
   * 
   * @return An integer that identifies how the dialog was closed. See the
   *         *_RETURN_CODE constants of this class for possible values.
   */
  public int getReturnCode() {
    return returnCode;
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @return A boolean indicating if the button is enabled.
   */
  public boolean getBackButtonEnabled() {
    return wizardModel.getBackButtonEnabled().booleanValue();
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @param boolean newValue The new enabled status of the button.
   */
  public void setBackButtonEnabled(boolean newValue) {
    wizardModel.setBackButtonEnabled(new Boolean(newValue));
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @return A boolean indicating if the button is enabled.
   */
  public boolean getNextFinishButtonEnabled() {
    return wizardModel.getNextFinishButtonEnabled().booleanValue();
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @param boolean newValue The new enabled status of the button.
   */
  public void setNextFinishButtonEnabled(boolean newValue) {
    wizardModel.setNextFinishButtonEnabled(new Boolean(newValue));
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @return A boolean indicating if the button is enabled.
   */
  public boolean getCancelButtonEnabled() {
    return wizardModel.getCancelButtonEnabled().booleanValue();
  }

  /**
   * Mirrors the WizardModel method of the same name.
   * 
   * @param boolean newValue The new enabled status of the button.
   */
  public void setCancelButtonEnabled(boolean newValue) {
    wizardModel.setCancelButtonEnabled(new Boolean(newValue));
  }
  
  public void setWarningText(String message) {
    wizardModel.setWarningText(message);
  }
  
  public void setWarningIcon(Icon icon) {
    wizardModel.setWarningIcon(icon);
  }
  
  public void clearWarningMessage() {
    setWarningIcon(null);
    setWarningText("");
  }


  /**
   * Sets the visibility of the warning panel.
   * 
   * @param visible the new visibility status of the warning panel
   */
  public void setWarningVisible(boolean visible) {
    warningPanel.setVisible(visible);
  }
  
  /**
   * Returns the visibility of the warning panel.
   * 
   * @return the visibility status of the warning panel
   */
  public boolean isWarningVisible() {
    return warningPanel.isVisible();
  }

  /**
   * Changes the Next/Finish button availability and the warning/error text and
   * icon. If the Next/Finish button is enabled and a message, the message along
   * with a warning icon is displayed. If the Next/Finish button is disabled,
   * the message is displayed with an error icon.
   * 
   * @param enableNext the availability of the next/finish button
   * @param message the message to be displayed (or <code>null</code> if there
   *                is no warning or error)
   */
  public void setNextButtonAndWarningMessage(boolean enableNext, String message) {
    setNextFinishButtonEnabled(enableNext);
    setWarningText(message);
    if( enableNext ) {
      setWarningIcon(message != null ? WARNING_ICON : null);
    } else {
      setWarningIcon(ERROR_ICON);
    }
  }

  /**
   * Closes the dialog and sets the return code to the integer parameter.
   * 
   * @param code The return code.
   */
  void close(int code) {
    returnCode = code;
    wizardDialog.dispose();
  }

  /**
   * This method initializes the components for the wizard dialog: it creates a
   * JDialog as a CardLayout panel surrounded by a small amount of space on each
   * side, as well as three buttons at the bottom.
   */

  private void initComponents() {

    ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);

    BACK_TEXT = bundle.getString("WIZARD_BACK");
    NEXT_TEXT = bundle.getString("WIZARD_NEXT");
    CANCEL_TEXT = bundle.getString("WIZARD_CANCEL");
    FINISH_TEXT = bundle.getString("WIZARD_FINISH");

    BACK_ICON = UIManager.getIcon("ICON_ARROW_LEFT_16");
    NEXT_ICON = UIManager.getIcon("ICON_ARROW_RIGHT_16");
    CANCEL_ICON = UIManager.getIcon("ICON_EXIT_16");
    FINISH_ICON = UIManager.getIcon("ICON_TICK_16");
    
    WARNING_ICON = UIManager.getIcon("ICON_WARNING_16");
    ERROR_ICON = UIManager.getIcon("ICON_EXIT_16");

    wizardModel.addPropertyChangeListener(this);
    wizardController = new WizardController(this);

    wizardDialog.getContentPane().setLayout(new BorderLayout());
    wizardDialog.addWindowListener(this);

    //  Create the outer wizard panel, which is responsible for three buttons:
    //  Next, Back, and Cancel. It is also responsible a JPanel above them that
    //  uses a CardLayout layout manager to display multiple panels in the 
    //  same spot.

    JPanel buttonPanel = new JPanel();
    JSeparator separator = new JSeparator();
    Box buttonBox = new Box(BoxLayout.X_AXIS);

    cardPanel = new JPanel();
    cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

    cardLayout = new CardLayout();
    cardPanel.setLayout(cardLayout);

    backButton = new JButton(new ImageIcon("com/nexes/wizard/backIcon.gif"));
    nextButton = new JButton();
    cancelButton = new JButton();

    backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
    nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
    cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

    backButton.addActionListener(wizardController);
    nextButton.addActionListener(wizardController);
    cancelButton.addActionListener(wizardController);

    warningPanel = new JPanel();
    warningPanel.setLayout(new BorderLayout());
    
    warningLabel = new JLabel("");
    warningLabel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
    
    //  Create the buttons with a separator above them, then place them
    //  on the east side of the panel with a small amount of space between
    //  the back and the next button, and a larger amount of space between
    //  the next button and the cancel button.

    buttonPanel.setLayout(new BorderLayout());
    buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

    buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
    buttonBox.add(backButton);
    buttonBox.add(Box.createHorizontalStrut(10));
    buttonBox.add(nextButton);
    buttonBox.add(Box.createHorizontalStrut(30));
    buttonBox.add(cancelButton);

    buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

    
    warningPanel.setLayout(new BorderLayout());
    warningPanel.add(warningLabel, BorderLayout.WEST);
    warningPanel.add(separator, BorderLayout.SOUTH);

    wizardDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    wizardDialog.getContentPane().add(cardPanel, BorderLayout.CENTER);
    wizardDialog.getContentPane().add(warningPanel, BorderLayout.NORTH);
  }

  /**
   * If the user presses the close box on the dialog's window, treat it as a
   * cancel.
   * 
   * @param WindowEvent The event passed in from AWT.
   */

  public void windowClosing(WindowEvent e) {
    returnCode = CANCEL_RETURN_CODE;
  }

}
