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
package de.zbit.gui.prefs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;

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
	private static final ResourceBundle resource = ResourceManager
			.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;
	
	/**
	 * Helper method that initializes the correct constructor given an instance of {@link Window}.
	 * @param parent
	 * @return
	 */
	private static PreferencesDialog createPreferenesDialog(Window parent) {
		if (parent != null) {
			if (parent instanceof Dialog) {
			  return new PreferencesDialog((Dialog) parent);
			} else if (parent instanceof Frame) {
				return new PreferencesDialog((Frame) parent);
			}
		} 
		return new PreferencesDialog();
	}
	
	/**
	 * 
	 * @return
	 */
	public static final boolean showPreferencesDialog() {
		return showPreferencesDialog((Window) null);
	}
	
	/**
	 * 
	 * @param provider
	 * @return {@code true}, if and only if the user approved this dialog.
	 */
	public static final boolean showPreferencesDialog(Class<? extends KeyProvider>... provider) {
		return showPreferencesDialog((Window) null, provider);
	}
	
	/**
	 * 
	 * @param listener
	 * @return
	 */
	public static final boolean showPreferencesDialog(List<PreferenceChangeListener> listeners) {
		return showPreferencesDialog((Window) null, listeners);
	}
	
	/**
	 * 
	 * @param listener
	 *        A {@link PropertyChangeListener} that is notified about any changes
	 *        in the user-preferences.
	 * @param provider
	 * @return {@code true}, if and only if the user approved this dialog.
	 */
	public static final boolean showPreferencesDialog(
		List<PreferenceChangeListener> listeners, Class<? extends KeyProvider>... provider) {
		return showPreferencesDialog(null, listeners, provider);
	}
	
  /**
   * @param listener
   *        A {@link PropertyChangeListener} that is notified about any changes
   *        in the user-preferences.
   * @param provider
   * @return {@code true}, if and only if the user approved this dialog.
   */
	@SuppressWarnings("unchecked")
  public static final boolean showPreferencesDialog(PreferenceChangeListener listener,
    Class<? extends KeyProvider> provider) {
    return showPreferencesDialog(null, new ArrayList<PreferenceChangeListener>(Collections.singleton(listener)), provider);
  }
	
	/**
	 * 
	 * @param panel
	 * @return
	 */
	public static final boolean showPreferencesDialog(PreferencesPanel panel) {
		return showPreferencesDialog(null, panel);
	}
	
	/**
	 * 
	 * @param panel
	 * @param listener
	 * @return
	 */
	public static final boolean showPreferencesDialog(PreferencesPanel panel, List<PreferenceChangeListener>  listeners) {
		return showPreferencesDialog(null, panel, listeners);
	}
	
	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent) {
		return showPreferencesDialog(parent, (List<PreferenceChangeListener>) null);
	}
	
	/**
	 * 
	 * @param parent
	 * @param provider
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent, Class<? extends KeyProvider>... provider) {
		return showPreferencesDialog(parent, null, provider);
	}

	/**
	 * 
	 * @param parent
	 * @param listeners
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent, List<PreferenceChangeListener> listeners) {
		return showPreferencesDialog(parent, (PreferencesPanel) null, listeners);
	}
	
	/**
	 * 
	 * @param parent
	 * @param listeners
	 * @param provider
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent,
		List<PreferenceChangeListener> listeners, Class<? extends KeyProvider>... provider) {
		PreferencesDialog dialog = createPreferenesDialog(parent);
		boolean exitStatus;
		if ((provider != null) && (provider.length > 0)) {
			exitStatus = dialog.showPrefsDialog(listeners, provider);
		} else {
			exitStatus = dialog.showPrefsDialog(listeners);
		}
		return exitStatus;
	}
	
	/**
	 * 
	 * @param parent
	 * @param panel
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent, PreferencesPanel panel) {
		return showPreferencesDialog(parent, panel, null);
	}
	
	/**
	 * 
	 * @param parent
	 * @param panel
	 * @param listeners
	 * @return
	 */
	public static final boolean showPreferencesDialog(Window parent, PreferencesPanel panel, List<PreferenceChangeListener>  listeners) {
		PreferencesDialog dialog = createPreferenesDialog(parent);
		return (panel != null) ? dialog.showPrefsDialog(panel, listeners) : dialog.showPrefsDialog(listeners);
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
		this((Dialog) null);
	}
	
	/**
	 * Creates a new {@link PreferencesDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *        The parent element of this {@link PreferencesDialog}.
	 */
	public PreferencesDialog(Dialog owner) {
		super(owner, resource.getString("PREFERENCES"));
	}
	
	/**
	 * Creates a new {@link PreferencesDialog} with the given parent element and
	 * the default {@link Properties}.
	 * 
	 * @param owner
	 *        The parent element of this {@link PreferencesDialog}.
	 */
	public PreferencesDialog(Frame owner) {
		super(owner, resource.getString("PREFERENCES"));
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
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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
		button.setToolTipText(StringUtil.toHTMLToolTip(labels[1]));
		button.setEnabled(enabled);
		return button;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		apply.setEnabled(!allPrefsPanel.isUserConfiguration());
		defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
		ok.setEnabled(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
    apply.setEnabled(!allPrefsPanel.isUserConfiguration());
    defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
    ok.setEnabled(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	  // Do not update buttons here. The KeyEvent is not yet processed
	  // i.e. the textfield has not yet changed it's value!
	}

	/**
	 * Initializes the GUI of this dialog.
	 * 
	 * @param panel
	 *        The element to be put on the content pane.
	 */
	public void setPreferencesPanel(PreferencesPanel panel) {
    // pressing the ESCAPE button triggers "Cancel"
		getRootPane().registerKeyboardAction(this, CANCEL,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
    // pressing the ENTER button triggers "OK"
		getRootPane().registerKeyboardAction(this, OK,
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		allPrefsPanel = panel;
		getContentPane().removeAll();
		getContentPane().add(allPrefsPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = createButton(DEFAULTS, !allPrefsPanel.isDefaultConfiguration());
		JButton cancel = createButton(CANCEL, true);
		apply = createButton(APPLY, false);
		ok = createButton(OK, true);
		
    // define the OK button as default action
    getRootPane().setDefaultButton(ok);
    
		GUITools.calculateAndSetMaxWidth(defaults, cancel, apply, ok);
		
		p.add(cancel);
		p.add(defaults);
		p.add(apply);
		p.add(ok);
		JPanel foot = new JPanel(new BorderLayout());
		foot.add(new JSeparator(), BorderLayout.NORTH);
		foot.add(p, BorderLayout.SOUTH);
		getContentPane().add(foot, BorderLayout.SOUTH);
    
    // Preferences panels look stupid if they are smaller
    // than the "Ok","Cancel", etc. button panel!
    allPrefsPanel.setPreferredSize(GUITools.getMaxPreferredSize(foot, allPrefsPanel));
    
		pack();
	}

	/* (non-Javadoc)
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
			// FIXME: This is a really dirty solution. You should first check,
			// if this panel is not already registered as listener on allPrefsPanel.
			// Secondly, if allPrefsPanel changes (see setPreferencesPanel()), this
			// panel should not listen anymore to the old panel and should now listen
			// to the new panel!
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
		return showPrefsDialog((List<PreferenceChangeListener>) null);
	}
	
	/**
	 * 
	 * @param kp
	 * @return
	 */
	public boolean showPrefsDialog(Class<? extends KeyProvider>... kp) {
		return showPrefsDialog(null, kp);
	}
	
	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean showPrefsDialog(List<PreferenceChangeListener> listeners) {
		MultiplePreferencesPanel pane;
		try {
			pane = new MultiplePreferencesPanel();
			if (pane.getPreferencesPanelCount() == 1) { 
				return showPrefsDialog(pane.getPreferencesPanel(0), listeners); 
			}
			return showPrefsDialog(pane, listeners);
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
			return false;
		}
	}
	
	/**
	 * 
	 * @param listener
	 * @param kp
	 * @return
	 */
	public boolean showPrefsDialog(List<PreferenceChangeListener> listeners, Class<? extends KeyProvider>... kp) {
		PreferencesPanel panel;
		try {
      if (kp.length > 1) {
        panel = new MultiplePreferencesPanel(kp);
      } else {
        panel = new PreferencesPanelForKeyProvider(kp[0]);
      }
		} catch (Throwable exc) {
			// May happen only if defaults are loaded from XML or kp is null.
			GUITools.showErrorMessage(this, exc);
			return false;
		}
		return showPrefsDialog(panel, listeners);
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
		return showPrefsDialog(panel, null);
	}
	
	/**
	 * 
	 * @param panel
	 * @param listener
	 * @return
	 */
	public boolean showPrefsDialog(PreferencesPanel panel, List<PreferenceChangeListener>  listeners) {
		if ((listeners != null) && (listeners.size() > 0) && (panel != null)) {
			for (PreferenceChangeListener listener : listeners) {
				panel.addPreferenceChangeListener(listener);
			}
		}
		setPreferencesPanel(panel);
		setVisible(true);
		return exitStatus;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		apply.setEnabled(!allPrefsPanel.isUserConfiguration());
		defaults.setEnabled(!allPrefsPanel.isDefaultConfiguration());
		ok.setEnabled(true);
	}

}
