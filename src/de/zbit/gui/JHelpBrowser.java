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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.zbit.gui.prefs.CommandLineHelp;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.locales.Labels;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * This is a specialized dialog that displays HTML pages and contains a toolbar
 * with two buttons for jumping forward or backward in the history of visited
 * pages. This was part of SBMLsqueezer version 1.0.
 * 
 * @author Andreas Dr&auml;ger
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public class JHelpBrowser extends JDialog implements ActionListener,
		HyperlinkListener, PropertyChangeListener, ChangeListener {
	
	private JButton backButton, nextButton, saveButton, openInBrowserButton;
	
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
	public static JHelpBrowser showOnlineHelp(Frame owner, WindowListener wl,
		String title, URL fileLocation, Class<? extends KeyProvider>... clazz) {
		JHelpBrowser helpBrowser = new JHelpBrowser(owner, title, fileLocation);
		if ((clazz != null) && (clazz.length > 0)) {
			ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
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
	      tabs.addChangeListener(helpBrowser);
				tabs.insertTab(bundle.getString("ONLINE_HELP"), UIManager.getIcon("ICON_HELP_16"),
					helpBrowser.mainPart, bundle.getString("ONLINE_HELP_TOOLTIP"), 0);
				tabs.addTab(bundle.getString("COMMAND_LINE_ARGUMENTS"), component);
				tabs.setSelectedIndex(0);
				helpBrowser.mainPart = tabs;
				helpBrowser.getContentPane().add(tabs, BorderLayout.CENTER);
			}
		}
		if (wl!=null) helpBrowser.addWindowListener(wl);
		helpBrowser.setSize(640, 640);
		helpBrowser.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		helpBrowser.setLocationRelativeTo(owner);
		helpBrowser.setVisible(true);
		return helpBrowser;
	}
	
	/**
	 * The actual browserPane.
	 */
	private JBrowserPane browserPane;
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
	
	/**
	 * Adds a new tab to this {@link JHelpBrowser}.
	 * @param url
	 * @param name
	 */
	public void addTab(URL url, String name) {
	  JBrowserPane browser = new JBrowserPane(url);
	  browser.addHyperlinkListener(this);
	  
	  JScrollPane scroll = new JScrollPane(browser,
	    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  
	  this.addTab(scroll, name);
	}
  
	/**
	 * Adds a new tab to this {@link JHelpBrowser}.
	 * @param component
	 * @param name
	 */
	public void addTab(Component component, String name) {
    // Ensure that there is currently a TabbedPane
    if (!(mainPart instanceof JTabbedPane)) {
      // Create a new TabbedPane
      JTabbedPane tabs = new JTabbedPane();
      tabs.addChangeListener(this);
      tabs.add(mainPart);
      getContentPane().remove(mainPart);
      
      // Set the tabbedPane as root object
      tabs.setSelectedIndex(0);
      mainPart = tabs;
      getContentPane().add(mainPart, BorderLayout.CENTER);
      // this seems to be redundant, remove is nothing unusual happens
//    } else {
//      if (!(mainPart instanceof JTabbedPane)) {
//        mainPart = (JComponent) getContentPane().getComponent(0);
//      }
    }
    
    // Add the new component
    String componentName = name;
    if( componentName == null || componentName.length() < 1 ) {
      componentName = component.getName();
    }
    ((JTabbedPane) mainPart).addTab(componentName, component);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			if (browserPane != null) {
				if (button.equals(backButton)) {
					if (!browserPane.back()) {
						button.setEnabled(false);
						if ((browserPane.getNumPagesVisited() > 1) && !nextButton.isEnabled()) {
							nextButton.setEnabled(true);
						}
					} else if (!nextButton.isEnabled()) nextButton.setEnabled(true);
				} else if (button.equals(nextButton)) {
					if (!browserPane.next()) {
						button.setEnabled(false);
						if ((browserPane.getNumPagesVisited() > 1) && !backButton.isEnabled()) {
							backButton.setEnabled(true);
						}
					} else if (!backButton.isEnabled()) {
						backButton.setEnabled(true);
					}
				} else if (button.equals(saveButton)) {
					SBPreferences prefs = SBPreferences
							.getPreferencesFor(GUIOptions.class);
					File file = GUITools.saveFileDialog(this, prefs
							.get(GUIOptions.OPEN_DIR), false, false, true,
						JFileChooser.FILES_ONLY, SBFileFilter.createHTMLFileFilter());
					if (file != null) {
						JEditorPane editor = null;
						if (mainPart instanceof JScrollPane) {
							editor = browserPane;
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
				} else if (button.equals(openInBrowserButton)) {
				  if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(browserPane.getPage().toURI());
            } catch (Exception e1) {
              GUITools.showErrorMessage(this, e1);
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
		
		browserPane = new JBrowserPane(helpFile);
		browserPane.addHyperlinkListener(this);
		browserPane.addPropertyChangeListener(this);
		JPanel content = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(browserPane,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.mainPart = scroll;
		content.add(scroll, BorderLayout.CENTER);
		
		ResourceBundle bundle = Labels.bundle;
		
		JToolBar toolbar = new JToolBar();
		// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		backButton = new JButton(UIManager.getIcon("ICON_ARROW_LEFT_16"));
		backButton.setToolTipText(bundle.getString(Labels.LAST_PAGE));
		backButton.setName("back");
		backButton.addActionListener(this);
		backButton.setEnabled(false);
		toolbar.add(backButton);
		
		Icon icon = UIManager.getIcon("ICON_ARROW_RIGHT_16");
		if (icon != null) {
			nextButton = new JButton(icon);
		} else {
			nextButton = new JButton(bundle.getString(Labels.NEXT));
		}
		nextButton.setToolTipText(bundle.getString(Labels.NEXT_PAGE));
		nextButton.setName("next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		toolbar.add(nextButton);
		
		icon = UIManager.getIcon("ICON_SAVE_16");
		if (icon != null) {
			saveButton = new JButton(icon);
		} else {
			saveButton = new JButton(bundle.getString(Labels.SAVE));
		}
		saveButton.setToolTipText(bundle.getString(Labels.SAVE_TOOLTIP));
		saveButton.setName("save");
		saveButton.addActionListener(this);
		saveButton.setEnabled(true);
		toolbar.add(saveButton);
		
		openInBrowserButton = new JButton(bundle.getString(Labels.OPEN_IN_BROWSER));
		openInBrowserButton.setToolTipText(bundle.getString(Labels.OPEN_IN_BROWSER_TOOLTIP));
		openInBrowserButton.setName("openInBrowser");
		openInBrowserButton.addActionListener(this);
		openInBrowserButton.setEnabled(false);
		toolbar.add(openInBrowserButton);
		
		content.add(toolbar, BorderLayout.NORTH);
		setContentPane(content);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLocationByPlatform(true);
		
		updateComponents();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	@Override
  public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED
				&& !backButton.isEnabled()) {
		  backButton.setEnabled(true);
		  updateComponents();
		}
	}
	
	/**
	 * Updates the enabled/disabled status of component based on the current state
	 * of the help browser, e.g. the displayed content.
	 */
	public void updateComponents() {
    JEditorPane editor = null;
    if (mainPart instanceof JScrollPane) {
      editor = browserPane;
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
    if (browserPane.equals(editor)) {
      openInBrowserButton.setEnabled( browserPane.getPage().getProtocol().startsWith("http") );
    } else {
      openInBrowserButton.setEnabled(false);
    }
	}

  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // when the browser panel loads a new page, update components
    if ("page".equals( evt.getPropertyName() )) {
      updateComponents();
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    // when another tab is selected, update components
    updateComponents();
  }
}
