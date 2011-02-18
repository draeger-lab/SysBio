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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.zbit.gui.prefs.CommandLineHelp;
import de.zbit.io.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * This is a specialized dialog that displays HTML pages and contains a toolbar
 * with two buttons for jumping forward or backward in the history of visited
 * pages. This was part of SBMLsqueezer version 1.0.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class JHelpBrowser extends JDialog implements ActionListener,
		HyperlinkListener {
	
	private JButton backButton, nextButton, saveButton;
	
	/**
   * 
   */
	private static final long serialVersionUID = 5747595033121404644L;
	
	/**
	 * Shows a dialog window with the online help.
	 * 
	 * @param owner
	 * @param wl
	 * @param title
	 * @param fileLocation
	 * @param component
	 */
	public static void showOnlineHelp(Frame owner, WindowListener wl,
		String title, URL fileLocation, Class<? extends KeyProvider>... clazz) {
		JHelpBrowser helpBrowser = new JHelpBrowser(owner, title, fileLocation);
		if ((clazz != null) && (clazz.length > 0)) {
			ResourceBundle bundle = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
			JComponent component = CommandLineHelp.createHelpComponent(clazz);
			helpBrowser.getLayout().removeLayoutComponent(helpBrowser.mainPart);
			if (component instanceof JTabbedPane) {
				((JTabbedPane) component).insertTab(bundle.getString("ONLINE_HELP"), UIManager
						.getIcon("ICON_HELP_16"), helpBrowser.mainPart,
					bundle.getString("ONLINE_HELP_TOOLTIP"), 0);
				((JTabbedPane) component).setSelectedIndex(0);
				helpBrowser.getContentPane().add(component, BorderLayout.CENTER);
				helpBrowser.mainPart = component;
			} else {
				//helpBrowser.
				JTabbedPane tabs = new JTabbedPane();
				tabs.insertTab(bundle.getString("ONLINE_HELP"), UIManager.getIcon("ICON_HELP_16"),
					helpBrowser.mainPart, bundle.getString("ONLINE_HELP_TOOLTIP"), 0);
				tabs.addTab(bundle.getString("COMMAND_LINE_ARGUMENTS"), component);
				tabs.setSelectedIndex(0);
				helpBrowser.mainPart = tabs;
				helpBrowser.getContentPane().add(tabs, BorderLayout.CENTER);
			}
		}
		helpBrowser.addWindowListener(wl);
		helpBrowser.setSize(640, 640);
		helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		helpBrowser.setLocationRelativeTo(owner);
		helpBrowser.setVisible(true);
	}
	
	/**
	 * The actual browser.
	 */
	private JBrowserPane browser;
	/**
	 * The main element in this dialog.
	 */
	private JComponent mainPart;
	
	/**
	 * Creates a new JDialog that shows a browser and a toolbar to display a help
	 * web site.
	 * 
	 * @param owner
	 *        The owner of this window.
	 * @param title
	 *        The title of this window.
	 * @param startPage
	 * @see javax.swing.JDialog
	 */
	public JHelpBrowser(Dialog owner, String title, URL startPage) {
		super(owner, title);
		init(startPage);
	}
	
	/**
	 * Creates a new JDialog that shows a browser and a toolbar to display a help.
	 * 
	 * @param owner
	 * @param title
	 * @param startPage
	 */
	public JHelpBrowser(Frame owner, String title, URL startPage) {
		super(owner, title);
		init(startPage);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			String name = button.getName();
			if (browser != null) {
				if (name.equals(backButton.getName())) {
					if (!browser.back()) {
						button.setEnabled(false);
						if ((browser.getNumPagesVisited() > 1) && !nextButton.isEnabled()) {
							nextButton.setEnabled(true);
						}
					} else if (!nextButton.isEnabled()) nextButton.setEnabled(true);
				} else if (name.equals(nextButton.getName())) {
					if (!browser.next()) {
						button.setEnabled(false);
						if ((browser.getNumPagesVisited() > 1) && !backButton.isEnabled()) {
							backButton.setEnabled(true);
						}
					} else if (!backButton.isEnabled()) {
						backButton.setEnabled(true);
					}
				} else if (name.equals(saveButton.getName())) {
					SBPreferences prefs = SBPreferences
							.getPreferencesFor(GUIOptions.class);
					File file = GUITools.saveFileDialog(this, prefs
							.get(GUIOptions.OPEN_DIR), false, false, true,
						JFileChooser.FILES_ONLY, SBFileFilter.createHTMLFileFilter());
					if (file != null) {
						JEditorPane editor = null;
						if (mainPart instanceof JScrollPane) {
							editor = browser;
						} else if (mainPart instanceof JTabbedPane) {
							JTabbedPane tabs = (JTabbedPane) mainPart;
							Component component = tabs.getSelectedComponent();
							if (component instanceof JScrollPane) {
								component = ((JScrollPane) component).getViewport()
										.getComponent(0);
								if (component instanceof JEditorPane) {
									editor = (JEditorPane) component;
								}
							}
						}
						if (editor != null) {
							try {
								BufferedWriter bw = new BufferedWriter(new FileWriter(file));
								bw.append(editor.getText());
								bw.close();
								prefs.put(GUIOptions.SAVE_DIR, file.getParent());
								prefs.flush();
							} catch (IOException exc) {
								GUITools.showErrorMessage(this, exc);
							} catch (BackingStoreException exc) {
								prefs.remove(GUIOptions.SAVE_DIR);
							}
						}
					}
				}
			}
		} else {
			dispose();
		}
	}
	
	/**
	 * Initialize this Window.
	 * 
	 * @param helpFile
	 * 
	 */
	private void init(URL helpFile) {
		//		setIconImage((UIManager.getIcon("ICON_HELP_16")));
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(this, stroke,
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		browser = new JBrowserPane(helpFile);
		browser.addHyperlinkListener(this);
		JPanel content = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(browser,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.mainPart = scroll;
		content.add(scroll, BorderLayout.CENTER);
		
		ResourceBundle bundle = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
		
		JToolBar toolbar = new JToolBar();
		// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		backButton = new JButton(UIManager.getIcon("ICON_ARROW_LEFT_16"));
		backButton.setToolTipText(bundle.getString("LAST_PAGE"));
		backButton.setName("back");
		backButton.addActionListener(this);
		backButton.setEnabled(false);
		toolbar.add(backButton);
		
		Icon icon = UIManager.getIcon("ICON_ARROW_RIGHT_16");
		if (icon != null) {
			nextButton = new JButton(icon);
		} else {
			nextButton = new JButton(bundle.getString("NEXT"));
		}
		nextButton.setToolTipText(bundle.getString("NEXT_PAGE"));
		nextButton.setName("next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		toolbar.add(nextButton);
		
		icon = UIManager.getIcon("ICON_SAVE_16");
		if (icon != null) {
			saveButton = new JButton(icon);
		} else {
			saveButton = new JButton(bundle.getString("SAVE"));
		}
		saveButton.setToolTipText(bundle.getString("SAVE_TOOLTIP"));
		saveButton.setName("save");
		saveButton.addActionListener(this);
		saveButton.setEnabled(true);
		toolbar.add(saveButton);
		
		content.add(toolbar, BorderLayout.NORTH);
		setContentPane(content);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLocationByPlatform(true);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event
	 * .HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED
				&& !backButton.isEnabled()) backButton.setEnabled(true);
	}
}
