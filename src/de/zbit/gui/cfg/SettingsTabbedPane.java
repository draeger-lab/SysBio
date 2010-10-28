package de.zbit.gui.cfg;

import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;
import de.zbit.util.SBPreferences;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of the {@link Properties} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @since This was part of SBMLsqueezer 1.3
 * @date 2009-09-22
 */
public class SettingsTabbedPane extends SettingsPanel {

    /**
     * Load all available {@link SettingsPanel}s. This array is constructed by
     * querying the current project and the calling project as well.
     */
    private static Class<SettingsPanel> classes[] = Reflect
	    .getAllClassesInPackage(SettingsTabbedPane.class.getPackage()
		    .getName(), true, true, SettingsPanel.class, System
		    .getProperty("user.dir")
		    + File.separatorChar, true);

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = 3189416350182046246L;;

    /**
     * Needed to structure this {@link SettingsPanel}.
     */
    private JTabbedPane tab;

    /**
     * @param properties
     * @param defaultProperties
     * @throws IOException
     */
    public SettingsTabbedPane() throws IOException {
	super();
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#accepts(java.lang.Object)
     */
    @Override
    public boolean accepts(Object key) {
	return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.sbml.squeezer.gui.SettingsPanel#addChangeListener(javax.swing.event
     * .ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
	for (int i = 0; i < tab.getComponentCount(); i++) {
	    getSettingsPanelAt(i).addChangeListener(listener);
	}
    }

    /*
     * (non-Javadoc)
     * @seeorg.sbml.squeezer.gui.SettingsPanel#addItemListener(java.awt.event.
     * ItemListener)
     */
    @Override
    public void addItemListener(ItemListener listener) {
	for (int i = 0; i < tab.getComponentCount(); i++) {
	    getSettingsPanelAt(i).addItemListener(listener);
	}
    }

    /*
     * (non-Javadoc)
     * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
     */
    @Override
    public void addKeyListener(KeyListener listener) {
	for (int i = 0; i < tab.getComponentCount(); i++) {
	    getSettingsPanelAt(i).addKeyListener(listener);
	}
    }

    /**
     * @return
     */
    public int getSelectedIndex() {
	return tab.getSelectedIndex();
    }

    /**
     * @param index
     * @return
     */
    public SettingsPanel getSettingsPanelAt(int index) {
	return (SettingsPanel) ((JScrollPane) tab.getComponentAt(index))
		.getViewport().getComponent(0);
    }

    /**
     * Gives the number of {@link SettingsPanel}s displayed on this element.
     * 
     * @return 0 if there is no panel or if this element has not yet been
     *         initialized properly.
     */
    public int getSettingsPanelCount() {
	return tab == null ? 0 : tab.getTabCount();
    }

    /*
     * (non-Javadoc)
     * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
     */
    @Override
    public String getTitle() {
	return "User preferences";
    }

    /*
     * (non-Javadoc)
     * @see org.sbml.squeezer.gui.SettingsPanel#init()
     */
    @Override
    public void init() {
	tab = new JTabbedPane();
	SettingsPanel settingsPanel;
	for (int i = 0; i < classes.length; i++) {
	    if (!classes[i].equals(getClass())) {
		try {
		    Class<SettingsPanel> c = classes[i];
		    Constructor<SettingsPanel> con = c.getConstructor();
		    settingsPanel = con.newInstance();
		    tab.addTab(settingsPanel.getTitle(), new JScrollPane(
			settingsPanel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		} catch (Exception exc) {
		    GUITools.showErrorMessage(this, exc);
		}
	    }
	}
	this.add(tab);
	addItemListener(this);
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#isDefaultConfiguration()
     */
    @Override
    public boolean isDefaultConfiguration() {
	for (int i = 0; i < getSettingsPanelCount(); i++) {
	    if (!getSettingsPanelAt(i).isDefaultConfiguration()) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#isUserConfiguration()
     */
    @Override
    public boolean isUserConfiguration() {
	for (int i = 0; i < getSettingsPanelCount(); i++) {
	    if (!getSettingsPanelAt(i).isUserConfiguration()) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#loadPreferences()
     */
    @Override
    protected SBPreferences loadPreferences() throws IOException {
	return null;
    }

    /*
     * (non-Javadoc)
     * @see de.zbit.gui.cfg.SettingsPanel#persist()
     */
    @Override
    public void persist() throws BackingStoreException {
	for (int i = 0; i < tab.getComponentCount(); i++) {
	    getSettingsPanelAt(i).persist();
	}
    }

    /*
     * (non-Javadoc)
     * @see org.sbml.squeezer.gui.SettingsPanel#restoreDefaults()
     */
    @Override
    public void restoreDefaults() {
	for (int i = 0; i < tab.getComponentCount(); i++) {
	    getSettingsPanelAt(i).restoreDefaults();
	}
	validate();
    }

    /**
     * @param tab
     */
    public void setSelectedIndex(int tab) {
	this.tab.setSelectedIndex(tab);
    }
}
