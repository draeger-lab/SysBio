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
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * This class implements a {@link JWindow} object which is shown on the bottom
 * right corner of the screen. It notifies the user that a more recent version
 * of a program is available. The release notes of this version can be shown.
 * 
 * @author Hannes Borch
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @since 1.0 (originates from SBMLsqueezer 1.2 and 1.3)
 * @version $Rev$
 */
public class UpdateMessage extends SwingWorker<Boolean, Void> {

  /**
   * A small yellow {@link JWindow} without regular window decoration that pops
   * up on the right bottom of the screen to display the update message.
   * 
   * @author Hannes Borch
   * @author Andreas Dr&auml;ger
   * @author Clemens Wrzodek
   * 
   */
  private static class UpdateMessageWindow extends JWindow implements
  ActionListener {

    /**
     * Generated serial version uid.
     */
    private static final long serialVersionUID = -6726847840681376184L;

    /**
     * 
     */
    private JPanel contentPanel;

    /**
     * 
     */
    private JButton okButton;

    /**
     * 
     */
    private JButton showHideButton;

    /**
     * Main constructor. Initializes a {@link JWindow} object containing a (at
     * the beginning) invisible {@link JEditorPane} which contains the program's
     * release notes and two {@link JButtons} for showing them and exiting the
     * {@link Window}.
     * 
     * @param urlString - URL of the update.
     * @param applicationName
     * @throws IOException 
     */
    public UpdateMessageWindow(String urlString, String applicationName) throws IOException {
      this(urlString, applicationName, UIManager.getIcon("ICON_GLOBE_16"));
    }

    /**
     * See {@link #UpdateMessage(String, String)} for more information.
     * @param icon - Icon that is displayed, along with the update message.
     * May be null, if no icon is desired.
     */
    public UpdateMessageWindow(String u, String applicationName, Icon icon)
    throws IOException {
      super();
      setBackground(Color.YELLOW);
      setAlwaysOnTop(true);
      URL url = new URL(u);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.setBackground(getBackground());

      ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
      okButton = new JButton(GUITools.getOkButtonText());
      okButton.addActionListener(this);
      showHideButton = new JButton(bundle.getString("SHOW_RELEASE_NOTES"));
      showHideButton.setName("showReleaseNotes");
      showHideButton.setIcon(UIManager.getIcon("ICON_ARROW_RIGHT"));
      showHideButton.setIconTextGap(5);
      showHideButton.setBorderPainted(false);
      showHideButton.setBackground(new Color(buttonPanel.getBackground()
          .getRGB()));
      showHideButton.setSize(150, 20);
      showHideButton.addActionListener(this);
      buttonPanel.add(showHideButton);
      buttonPanel.add(okButton);

      Scanner scanner = new Scanner(url.openStream());
      String s = scanner.useDelimiter("<h1>").next();
      s = "<html><body>" + scanner.useDelimiter("\\Z").next();
      JEditorPane pane = new JEditorPane("text/html", s);
      pane.setEditable(false);
      pane.addHyperlinkListener(new SystemBrowser());
      contentPanel = new JPanel(new BorderLayout());
      JScrollPane scroll = new JScrollPane(pane,
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setPreferredSize(new Dimension(550, 400));
      contentPanel.add(scroll, BorderLayout.CENTER);

      String updateString = String.format(bundle.getString("UPDATE_IS_AVAILABLE"), applicationName);
      contentPanel.setVisible(false);
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.setBackground(getBackground());

      // wrzodek: Unfortunately, the TitledBorder has no influence on the
      // window size. Thus, the text almost never fits into the window.
      // => solution: create a JLabel.
      //mainPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(
      //	Color.BLACK), updateString, TitledBorder.CENTER, TitledBorder.BELOW_TOP));
      mainPanel.setBorder(new LineBorder(Color.BLACK));
      mainPanel.add(new JLabel(" " + updateString + " ", icon, SwingConstants.CENTER), BorderLayout.NORTH);

      mainPanel.add(contentPanel, BorderLayout.CENTER);
      mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
      setContentPane(mainPanel);
      pack();
      adjustLocation();
    }

    /**
     * If the "show release notes" button is hit, the {@link JEditorPane} object
     * containing the release notes is made visible and the button's text is
     * changed to "hide release notes". If "hide release notes" is hit, the
     * {@link JEditorPane} gets invisible again, and the button's text is
     * rechanged to it's default. If "OK" is hit, the window is closed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() instanceof JButton) {
        ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
        String buttonText = ((JButton) e.getSource()).getName();
        if (buttonText==null) buttonText=""; // Simply avoid NullPointerExceptions (=> OK Button)
        if (buttonText.equals("showReleaseNotes")) {
          contentPanel.setVisible(true);
          showHideButton.setName("hideReleaseNotes");
          showHideButton.setText(bundle.getString("HIDE_RELEASE_NOTES"));
        } else if (buttonText.equals("hideReleaseNotes")) {
          contentPanel.setVisible(false);
          showHideButton.setName("showReleaseNotes");
          showHideButton.setText(bundle.getString("SHOW_RELEASE_NOTES"));
        } else { // Ok button (buttonText is null)
          dispose();
        }
      }
      validate();
      pack();
      adjustLocation();
    }

    /**
     * Adjusts the location of the update message window to it's actual size.
     */
    private void adjustLocation() {
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(d.width - this.getWidth(), d.height - this.getHeight() - 30);
    }
  }

  private String applicationName;

  private String dottedVersionNumber;

  /**
   * 
   */
  private String latestVersion;

  /**
   * 
   */
  private URL urlPrefix;

  /**
   * 
   */
  private String url;

  /**
   * 
   */
  private List<WindowListener> listOfListeners;

  /**
   * 
   */
  private boolean gui, hideErrorMessages;


  /**
   * Icon that is displayed, along with the update message.
   * Might be null, if no icon is desired.
   */
  private Icon icon = UIManager.getIcon("ICON_GLOBE_16");

  /**
   * @param gui
   *        If true, a small window is displayed with the update message,
   *        otherwise the message is printed on the console.
   * @param applicationName
   * @param urlPrefix
   * @param dottedVersion
   *        number
   */
  public UpdateMessage(boolean gui, String applicationName, URL urlPrefix,
                       String dottedVersionNumber, boolean hideErrorMessages) {
    super();
    this.gui = gui;
    this.applicationName = applicationName;
    this.urlPrefix = urlPrefix;
    this.dottedVersionNumber = dottedVersionNumber;
    this.hideErrorMessages = hideErrorMessages;
    this.listOfListeners = new LinkedList<WindowListener>();
  }


  /**
   * Icon that is displayed, along with the update message.
   * Set to null, if no icon is desired.
   * @param icon
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Checks if there is an update for a program available.
   * 
   * @return {@code true} if an update is available {@code false}
   *         otherwise.
   * @throws IOException
   *         If the {@link URL} to the update is incorrect or if there is
   *         neither a file with the name
   *         {@code releaseNotes&lt;latestVersion&gt;.htm} or
   *         {@code releaseNotes&lt;latestVersion&gt;.html}.
   */
  public boolean checkForUpdate() throws IOException {
    URL urlLatestVersion = new URL(urlPrefix + "latest.txt");
    latestVersion = (new Scanner(urlLatestVersion.openStream())).next();
    String notes = "releaseNotes" + latestVersion;
    while (notes.endsWith(".0")) {
      notes = notes.substring(0, notes.length() - 2);
    }
    this.url = urlPrefix.toString() + notes;
    if (compareVersionNumbers(dottedVersionNumber, latestVersion)) {
      return true;
    }
    return false;
  }

  /**
   * Compares the version with which this object is initialized and the file
   * "latest.txt" on www.
   * 
   * @param programVersion
   * @param latestVersion
   * @return
   */
  public static boolean compareVersionNumbers(String programVersion, String latestVersion) {
    StringTokenizer progToken = new StringTokenizer(programVersion);
    StringTokenizer latestToken = new StringTokenizer(latestVersion);

    while (progToken.hasMoreElements() && latestToken.hasMoreElements()) {
      int localVers = Integer.parseInt(progToken.nextToken("."));
      int latestVers = Integer.parseInt(latestToken.nextToken("."));

      if (localVers<latestVers) {
        return true;
      } else if (localVers>latestVers) {
        return false;
      }

      if (latestToken.hasMoreElements() && (Integer.parseInt(latestToken.nextToken(".")) > 0)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   */
  private void showUpdateMessage() {
    try {
      showUpdateMessage(gui, url + ".htm");
    } catch (IOException exc) {
      try {
        showUpdateMessage(gui, url + ".html");
      } catch (IOException e) {
        if (gui) {
          GUITools.showErrorMessage(null, e);
        } else {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Show the update message window.
   * 
   * @param showGui
   *        If true the update message is shown in a graphical mode, otherwise
   *        just as text.
   * 
   * @param urlString
   * @throws IOException
   */
  private void showUpdateMessage(boolean showGui, String urlString) throws IOException {
    if (showGui) {
      JWindow umw = new UpdateMessage.UpdateMessageWindow(urlString, applicationName, icon);
      for (WindowListener wl : listOfListeners) {
        umw.addWindowListener(wl);
      }
      umw.setVisible(true);
    } else {
      System.out.printf(ResourceManager.getBundle(
          StringUtil.RESOURCE_LOCATION_FOR_LABELS).getString(
          "COMMAND_LINE_UPDATE_MESSAGE"), applicationName, urlPrefix,
          latestVersion, applicationName, dottedVersionNumber);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Boolean doInBackground() throws Exception {
    return checkForUpdate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    boolean success = false;
    try {
      success = get();
    } catch (Exception e) {
      if (!hideErrorMessages) {
        if (gui) {
          GUITools.showErrorMessage(null, e);
        } else {
          e.printStackTrace();
        }
      }
    }
    if (success) {
      showUpdateMessage();
    } else {
      if (!hideErrorMessages) {
        ResourceBundle resources = ResourceManager
        .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
        String message = String.format(resources
            .getString("NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_MESSAGE"),
            dottedVersionNumber, applicationName);
        if (gui) {
          GUITools.showMessage(message, resources
              .getString("NO_UPDATE_AVAILABLE_FOR_CURRENT_VERSION_TITLE"));
        } else {
          System.out.println(message);
        }
      }
      if (gui) {
        firePropertyChange("onlineUpdateExecuted", Boolean.FALSE, Boolean.TRUE);
      }
    }
  }

  /**
   * 
   * @param wl
   * @return
   */
  public boolean addWindowListener(WindowListener wl) {
    return this.listOfListeners.add(wl);
  }

  /**
   * 
   * @param wl
   * @return
   */
  public boolean removeWindowListener(WindowListener wl) {
    return this.listOfListeners.remove(wl);
  }
}
