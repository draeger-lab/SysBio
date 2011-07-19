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
package de.zbit.gui.prefs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;

/**
 * A specialized {@link JDialog} that shows several configuration options in a
 * {@link JTabbedPane}, provides a button for applying the chosen selection and
 * also to restore the default settings. All settings are synchronized with the
 * central configuration of {@link SBMLsqueezer}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-06
 * @version $Rev$
 * @since 1.0
 */
public class PreferencesDialog extends JDialog implements ActionListener,
		ItemListener, ChangeListener, KeyListener {
	
	private static final ResourceBundle resource = ResourceManager
			.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);

	/**
	 * Texts for the {@link JButton}s.
	 */
	private static final String APPLY = "APPLY", CANCEL = "CANCEL",
			DEFAULTS = "DEFAULTS", OK = "OK";
	/**
	 * Return types for the dialog.
	 */
	public static final boolean APPROVE_OPTION = true, CANCEL_OPTION = false;
	/**
	 * 
	 */
	private static final String DEFAULT_TITLE = resource.getString("PREFERENCES");
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;
	
	/**
	 * 
	 * @return
	 */
	public static final boolean showPreferencesDialog() {
		return showPreferencesDialog((PreferencesPanel) null);
	}
	
	/**
	 * 
	 * @param panel
	 * @return
	 */
	public static final boolean showPreferencesDialog(PreferencesPanel panel) {
		PreferencesDialog dialog = new PreferencesDialog();
		return (panel != null) ? dialog.showPrefsDialog(panel) : dialog.showPrefsDialog();
	}
	
	/**
	 * 
	 * @param provider
	 * @return true, if and only if the user approved this dialog.
	 */
	public static final boolean showPreferencesDialog(Class<? extends KeyProvider> provider) {
		PreferencesDialog dialog = new PreferencesDialog();
		boolean exitStatus;
		if (provider != null) {
			exitStatus = dialog.showPrefsDialog(provider);
		} else {
			exitStatus = dialog.showPrefsDialog();
		}
		return exitStatus;
	}
	
	/**
	 * What ever is displayed on this {@link PreferencesDialog}'s content pane
	 * must be available on this {@link PreferencesPanel}.
	 */
	private PreferencesPanel allPrefsPanel;
	
	/**
	 * The buttons for the foot panel of this {@link PreferencesDialog}.
	 */
	private JButton apply, defaults, ok;
	
	/**
	 * This will tell us later what the user selected here.
	 */
	private boolean exitStatus;
	
	/**
	 * 
	 */
	public PreferencesDialog() {
		this(DEFAULT_TITLE);
	}
	
	/**
	 * Creates a new {@link PreferencesDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *        The parent element of this {@link PreferencesDialog}.
	 */
	public PreferencesDialog(Dialog owner) {
		super(owner, DEFAULT_TITLE);
	}
	
	/**
	 * Creates a new {@link PreferencesDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *        The parent element of this {@link PreferencesDialog}.
	 * @param defaultProperties
	 *        The default {@link Properties} to reset all options.
	 */
	public PreferencesDialog(Frame owner) {
		super(owner, DEFAULT_TITLE);
	}
	
	/**
	 * Creates a new {@link PreferencesDialog} without any parent element and the
	 * default {@link Properties}.
	 * 
	 * @param title
	 *        The title of the dialog
	 */
	public PreferencesDialog(String title) {
		super();
		setTitle(title);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if ((ae.getActionCommand() == null) || ae.getActionCommand().equals(CANCEL)) {
			dispose();
		} else if (ae.getActionCommand().equals(DEFAULTS)) {
			allPrefsPanel.restoreDefaults();
			apply.setEnabled(true);
			ok.setEnabled(true);
			defaults.setEnabled(false);
			validate();
		} else if (ae.getActionCommand().equals(APPLY)
				|| ae.getActionCommand().equals(OK)) {
			try {
				allPrefsPanel.persist();
				apply.setEnabled(false);
				exitStatus = APPROVE_OPTION;
			} catch (BackingStoreException exc) {
				GUITools.showErrorMessage(this, exc);
				return; // Do NOT close dialog with invalid values.
			}
			if (ae.getActionCommand().equals(OK)) {
				dispose();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		apply.setEnabled(!allPrefsPanel.isUserConfiguration());
		defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
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
		apply.setEnabled(!allPrefsPanel.isUserConfiguration());
		defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
		ok.setEnabled(true);
	}
	
	/**
	 * Initializes the GUI of this dialog.
	 * 
	 * @param panel
	 *        The element to be put on the content pane.
	 */
	public void setPreferencesPanel(PreferencesPanel panel) {
		getRootPane().registerKeyboardAction(this, CANCEL,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(this, OK,
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		allPrefsPanel = panel;
		getContentPane().removeAll();
		getContentPane().add(allPrefsPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = createButton(DEFAULTS, !panel.isDefaultConfiguration());
		JButton cancel = createButton(CANCEL, true);
		apply = createButton(APPLY, false);
		ok = createButton(OK, true);
		
		GUITools.calculateAndSetMaxWidth(defaults, cancel, apply, ok);
		
		p.add(cancel);
		p.add(defaults);
		p.add(apply);
		p.add(ok);
		JPanel foot = new JPanel(new BorderLayout());
		foot.add(new JSeparator(), BorderLayout.NORTH);
		foot.add(p, BorderLayout.SOUTH);
		getContentPane().add(foot, BorderLayout.SOUTH);
		
		pack();
	}

	/**
	 * 
	 * @param command
	 * @param enabled
	 * @return
	 */
	private JButton createButton(String command, boolean enabled) {
		String separator = ";";
		String labels[] = resource.getString(command).split(separator);
		JButton button = new JButton(labels[0]);
		button.setMnemonic(labels[0].charAt(0));
		button.addActionListener(this);
		button.setActionCommand(command);
		button.setToolTipText(StringUtil.toHTML(labels[1], GUITools.TOOLTIP_LINE_LENGTH));
		button.setEnabled(enabled);
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		if (b) {
			exitStatus = CANCEL_OPTION;
			pack();
			setLocationRelativeTo(getOwner());
			setResizable(false);
			setModal(true);
			allPrefsPanel.addItemListener(this);
			allPrefsPanel.addChangeListener(this);
			allPrefsPanel.addKeyListener(this);
		}
		super.setVisible(b);
	}
	
	/**
	 * Shows this {@link PreferencesDialog} with all available instances of
	 * {@link PreferencesPanel} embedded in a pane with tabs, i.e.,
	 * {@link MultiplePreferencesPanel}. If there is only one such implementation
	 * available, no tabs will be displayed, the element will be directly added to
	 * this element's content pane.
	 * 
	 * @return {@link #APPROVE_OPTION} if the dialog was closed by clicking its OK
	 *         button. In this case it makes sense to call the
	 *         {@link #getProperties()} method to obtain all properties as set by
	 *         the user.
	 */
	public boolean showPrefsDialog() {
		MultiplePreferencesPanel pane;
		try {
			pane = new MultiplePreferencesPanel();
			if (pane.getPreferencesPanelCount() == 1) { return showPrefsDialog(pane
					.getPreferencesPanel(0)); }
			return showPrefsDialog(pane);
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
			return false;
		}
	}
	
	/**
	 * Shows this {@link PreferencesDialog} with the given
	 * {@link PreferencesPanel} on its content pane.
	 * 
	 * @param panel
	 *        The {@link PreferencesPanel} whose options are to be selected by the
	 *        user.
	 * @return {@link #APPROVE_OPTION} if the dialog was closed by clicking its OK
	 *         button. In this case it makes sense to call the
	 *         {@link #getProperties()} method to obtain all properties as set by
	 *         the user.
	 */
	public boolean showPrefsDialog(PreferencesPanel panel) {
		setPreferencesPanel(panel);
		setVisible(true);
		return exitStatus;
	}
	
	public boolean showPrefsDialog(Class<? extends KeyProvider> kp) {
		PreferencesPanel panel;
		try {
			panel = new PreferencesPanelForKeyProvider(kp);
		} catch (IOException e) {
			// May happen only if defaults are loaded from XML
			e.printStackTrace();
			return false;
		}
		setPreferencesPanel(panel);
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
		apply.setEnabled(!allPrefsPanel.isUserConfiguration());
		defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
		ok.setEnabled(true);
	}
}
