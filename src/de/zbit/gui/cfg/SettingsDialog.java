/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.gui.cfg;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A specialized {@link JDialog} that shows several configuration options in a
 * {@link JTabbedPane}, provides a button for applying the chosen selection and
 * also to restore the default settings. All settings are synchronized with the
 * central configuration of {@link SBMLsqueezer}.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @date 2009-09-06
 * @version
 */
public class SettingsDialog extends JDialog implements ActionListener,
		ItemListener, ChangeListener, KeyListener {

	/**
	 * Texts for the {@link JButton}s.
	 */
	private static final String APPLY = "Apply", CANCEL = "Cancel",
			DEFAULTS = "Defaults", OK = "OK";
	/**
	 * Return types for the dialog.
	 */
	public static final boolean APPROVE_OPTION = true, CANCEL_OPTION = false;
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;

	/**
	 * Method to demonstrate how to use this element.
	 * 
	 * @param args
	 *            nothing.
	 */
	public static void main(String args[]) {
		SettingsDialog d = new SettingsDialog("Properties", new Properties());
		if (d.showSettingsDialog(new Properties()) == APPROVE_OPTION) {
			System.out.printf("Approve:\t%s\n", d.getProperties());
		} else {
			System.out.println("Cancel");
		}
	}

	/**
	 * The buttons for the foot panel of this {@link SettingsDialog}.
	 */
	private JButton apply, defaults, ok;

	/**
	 * This will tell us later what the user selected here.
	 */
	private boolean exitStatus;

	/**
	 * What ever is displayed on this {@link SettingsDialog}'s content pane must
	 * be available on this {@link SettingsPanel}.
	 */
	private SettingsPanel panelAllSettings;

	/**
	 * The current and the default {@link Properties}. The latter one is
	 * necessary to re-set the values to the default configuration.
	 */
	private Properties properties, defaultProperties;

	/**
	 * Creates a new {@link SettingsDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *            The parent element of this {@link SettingsDialog}.
	 * @param defaultProperties
	 *            The default {@link Properties} to reset all options.
	 */
	public SettingsDialog(Dialog owner, Properties defaultProperties) {
		super(owner, "Preferences");
		this.defaultProperties = defaultProperties;
	}

	/**
	 * Creates a new {@link SettingsDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *            The parent element of this {@link SettingsDialog}.
	 * @param defaultProperties
	 *            The default {@link Properties} to reset all options.
	 */
	public SettingsDialog(Frame owner, Properties defaultProperties) {
		super(owner, "Preferences");
		this.defaultProperties = defaultProperties;
	}

	/**
	 * Creates a new {@link SettingsDialog} without any parent element and the
	 * default {@link Properties}.
	 * 
	 * @param title
	 *            The title of the dialog
	 * @param defaultSettings
	 *            The default {@link Properties} to reset all options.
	 */
	public SettingsDialog(String title, Properties defaultSettings) {
		super();
		setTitle(title);
		this.defaultProperties = defaultSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if ((ae.getActionCommand() == null)
				|| ae.getActionCommand().equals(CANCEL)) {
			dispose();
		} else if (ae.getActionCommand().equals(DEFAULTS)) {
			Properties p = (Properties) properties.clone();
			properties = defaultProperties;
			panelAllSettings.setProperties(properties);
			properties = p;
			apply.setEnabled(true);
			ok.setEnabled(true);
			defaults.setEnabled(false);
			panelAllSettings.addChangeListener(this);
			panelAllSettings.addItemListener(this);
			panelAllSettings.addKeyListener(this);
			validate();
		} else if (ae.getActionCommand().equals(APPLY)
				|| ae.getActionCommand().equals(OK)) {
			properties.putAll(panelAllSettings.getProperties());
			apply.setEnabled(false);
			exitStatus = APPROVE_OPTION;
			if (ae.getActionCommand().equals(OK)) {
				dispose();
			}
		}
	}

	/**
	 * Method to retrieve all current properties of this element, i.e., after
	 * interaction with the user.
	 * 
	 * @return A pointer to this {@link SettingsDialog}'s current
	 *         {@link Properties}.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Initializes the GUI of this dialog.
	 * 
	 * @param panel
	 *            The element to be put on the content pane.
	 */
	private void init(SettingsPanel panel) {

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(this, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		panelAllSettings = panel;
		getContentPane().add(panelAllSettings, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = new JButton("Defaults");
		defaults.addActionListener(this);
		defaults.setActionCommand(DEFAULTS);
		defaults.setEnabled(!properties.equals(defaultProperties));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand(CANCEL);
		cancel.setSize(defaults.getSize());
		apply = new JButton("Apply");
		apply.setSize(defaults.getSize());
		apply.addActionListener(this);
		apply.setActionCommand(APPLY);
		apply.setEnabled(false);
		ok = new JButton("OK");
		ok.addActionListener(this);
		ok.setActionCommand(OK);
		ok.setSize(defaults.getSize());
		ok.setEnabled(false);

		cancel.setPreferredSize(defaults.getPreferredSize());
		apply.setPreferredSize(defaults.getPreferredSize());
		ok.setPreferredSize(defaults.getPreferredSize());

		p.add(cancel);
		p.add(defaults);
		p.add(apply);
		p.add(ok);
		add(p, BorderLayout.SOUTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/**
	 * Shows this {@link SettingsDialog} with all available instances of
	 * {@link SettingsPanel} embedded in a pane with tabs, i.e.,
	 * {@link SettingsTabbedPane}. If there is only one such implementation
	 * available, no tabs will be displayed, the element will be directly added
	 * to this element's content pane.
	 * 
	 * @param properties
	 *            The current properties.
	 * @return {@link #APPROVE_OPTION} if the dialog was closed by clicking its
	 *         OK button. In this case it makes sense to call the
	 *         {@link #getProperties()} method to obtain all properties as set
	 *         by the user.
	 */
	public boolean showSettingsDialog(Properties properties) {
		SettingsTabbedPane pane = new SettingsTabbedPane(properties,
				defaultProperties);
		if (pane.getSettingsPanelCount() == 1) {
			return showSettingsDialog(pane.getSettingsPanelAt(0));
		}
		return showSettingsDialog(pane);
	}

	/**
	 * Shows this {@link SettingsDialog} with the given {@link SettingsPanel} on
	 * its content pane.
	 * 
	 * @param panel
	 *            The {@link SettingsPanel} whose options are to be selected by
	 *            the user.
	 * @return {@link #APPROVE_OPTION} if the dialog was closed by clicking its
	 *         OK button. In this case it makes sense to call the
	 *         {@link #getProperties()} method to obtain all properties as set
	 *         by the user.
	 */
	public boolean showSettingsDialog(SettingsPanel panel) {
		this.properties = panel.getProperties();
		this.exitStatus = CANCEL_OPTION;
		init(panel);
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setModal(true);
		panelAllSettings.addItemListener(this);
		panelAllSettings.addChangeListener(this);
		panelAllSettings.addKeyListener(this);
		setVisible(true);
		return exitStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}
}
