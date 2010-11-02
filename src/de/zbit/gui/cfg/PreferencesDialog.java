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
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.StringUtil;

/**
 * A specialized {@link JDialog} that shows several configuration options in a
 * {@link JTabbedPane}, provides a button for applying the chosen selection and
 * also to restore the default settings. All settings are synchronized with the
 * central configuration of {@link SBMLsqueezer}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-06
 * @version
 */
public class PreferencesDialog extends JDialog implements ActionListener,
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
	 * 
	 */
	private static final String DEFAULT_TITLE = "Preferences";
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;
	
	/**
	 * 
	 * @return
	 */
	public static final boolean showPreferencesDialog() {
		return showPreferencesDialog(null);
	}
	
	/**
	 * 
	 * @param panel
	 * @return
	 */
	public static final boolean showPreferencesDialog(PreferencesPanel panel) {
		PreferencesDialog dialog = new PreferencesDialog();
		boolean exitStatus;
		if (panel != null) {
			exitStatus = dialog.showPrefsDialog(panel);
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
			List<String> errors = allPrefsPanel.checkPreferences();
			if (errors.isEmpty()) {
				try {
					allPrefsPanel.persist();
					apply.setEnabled(false);
					exitStatus = APPROVE_OPTION;
				} catch (BackingStoreException exc) {
					GUITools.showErrorMessage(this, exc);
				}
				if (ae.getActionCommand().equals(OK)) {
					dispose();
				}
			} else {
				int lineBreak = 60;
				String lineBreakSymbol = "<br>";
				StringBuilder msg = new StringBuilder();
				msg.append("<html><body>\n");
				if (errors.size() > 1) {
					msg.append("The configuration contains the ");
					msg.append("following errors:\n<ul>");
				}
				for (String error : errors) {
					if (errors.size() > 1) {
						StringUtil.append(msg, "<li>", StringUtil.insertLineBreaks(error,
							lineBreak, lineBreakSymbol), "</li>");
					} else {
						StringUtil.append(msg, StringUtil.insertLineBreaks(error,
							lineBreak, lineBreakSymbol));
					}
					msg.append("\n");
				}
				if (errors.size() > 1) {
					msg.append("</ul>");
				}
				msg.append("</body></html>");
				JOptionPane.showMessageDialog(this, new JLabel(msg.toString()),
					"Please check your configuration", JOptionPane.WARNING_MESSAGE);
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
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(this, stroke,
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		allPrefsPanel = panel;
		getContentPane().removeAll();
		getContentPane().add(allPrefsPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = new JButton("Defaults");
		defaults.addActionListener(this);
		defaults.setActionCommand(DEFAULTS);
		defaults.setEnabled(!panel.isDefaultConfiguration());
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
