/*
 * SBMLsqueezer creates rate equations for reactions in SBML files
 * (http://sbml.org). Copyright (C) 2009 ZBIT, University of Tübingen, Andreas
 * Dräger
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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

/**
 * This is a specialized dialog that displays HTML pages and contains a toolbar
 * with two buttons for jumping forward or backward in the history of visited
 * pages.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public class JHelpBrowser extends JDialog implements ActionListener,
		HyperlinkListener {
	
	private JButton backButton, nextButton;
	
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
	 */
	public static void showOnlineHelp(Frame owner, WindowListener wl,
		String title, URL fileLocation) {
		showOnlineHelp(owner, wl, title, fileLocation, null);
	}
	
	/**
	 * 
	 * @param owner
	 * @param wl
	 * @param title
	 * @param fileLocation
	 * @param component
	 */
	public static void showOnlineHelp(Frame owner, WindowListener wl,
		String title, URL fileLocation, JComponent component) {
		JHelpBrowser helpBrowser = new JHelpBrowser(owner, title, fileLocation);
		if (component != null) {
			helpBrowser.getLayout().removeLayoutComponent(helpBrowser.scroll);
			if (component instanceof JTabbedPane) {
				((JTabbedPane) component).insertTab("Online Help", UIManager
						.getIcon("ICON_HELP_16"), helpBrowser.scroll,
					"This is the main online help.", 0);
				((JTabbedPane) component).setSelectedIndex(0);
				helpBrowser.getContentPane().add(component, BorderLayout.CENTER);
			} else {
				//helpBrowser.
				JTabbedPane tabs = new JTabbedPane();
				tabs.insertTab("Online Help", UIManager.getIcon("ICON_HELP_16"),
					helpBrowser.scroll, "This is the main online help.", 0);
				tabs.addTab("Command line arguments", component);
				tabs.setSelectedIndex(0);
				helpBrowser.getContentPane().add(tabs, BorderLayout.CENTER);
			}
		}
		helpBrowser.addWindowListener(wl);
		helpBrowser.setLocationRelativeTo(owner);
		helpBrowser.setSize(640, 640);
		helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		helpBrowser.setVisible(true);
	}
	
	/**
	 * The actual browser.
	 */
	private JBrowserPane browser;
	/**
	 * The {@link JScrollPane} that contains the actual browser.
	 */
	private JScrollPane scroll;
	
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
			if (name.equals("back") && (browser != null)) {
				if (!browser.back()) {
					button.setEnabled(false);
					if (browser.getNumPagesVisited() > 1 && !nextButton.isEnabled())
						nextButton.setEnabled(true);
				} else if (!nextButton.isEnabled()) nextButton.setEnabled(true);
			} else if (name.equals("next") && (browser != null)) {
				if (!browser.next()) {
					button.setEnabled(false);
					if (browser.getNumPagesVisited() > 1 && !backButton.isEnabled())
						backButton.setEnabled(true);
				} else if (!backButton.isEnabled()) backButton.setEnabled(true);
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
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(this, stroke,
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		browser = new JBrowserPane(helpFile);
		browser.addHyperlinkListener(this);
		JPanel content = new JPanel(new BorderLayout());
		scroll = new JScrollPane(browser,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.add(scroll, BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		backButton = new JButton(UIManager.getIcon("ICON_ARROW_LEFT_16"));
		backButton.setToolTipText("Last Page");
		backButton.setName("back");
		backButton.addActionListener(this);
		backButton.setEnabled(false);
		toolbar.add(backButton);
		
		Icon icon = UIManager.getIcon("ICON_ARROW_RIGHT_16");
		if (icon != null) {
			nextButton = new JButton(icon);
		} else {
			nextButton = new JButton("Next");
		}
		nextButton.setToolTipText("Next Page");
		nextButton.setName("next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		toolbar.add(nextButton);
		content.add(toolbar, BorderLayout.NORTH);
		setContentPane(content);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
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
