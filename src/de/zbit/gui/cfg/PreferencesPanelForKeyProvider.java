/**
 *
 * @author wrzodek
 */
package de.zbit.gui.cfg;

import java.io.IOException;
import java.util.List;

import com.sun.net.ssl.internal.ssl.Provider;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * Automatically build an options panel.
 * 
 * @author wrzodek
 */
public class PreferencesPanelForKeyProvider extends PreferencesPanel {
	private static final long serialVersionUID = -3293205475880841303L;
	
	/**
	 * The KeyProvider, that determines this panel.
	 */
	private KeyProvider provider;

	/**
	 * @throws IOException
	 */
	public PreferencesPanelForKeyProvider(KeyProvider provider) throws IOException {
		super();
		this.provider = provider;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#checkPreferences()
	 */
	@Override
	public List<String> checkPreferences() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return PreferencesPanel.formatOptionName(provider.getClass().getSimpleName());
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#init()
	 */
	@Override
	public void init() {
		autoBuildPanel();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#loadPreferences()
	 */
	@Override
	protected SBPreferences loadPreferences() throws IOException {
		return SBPreferences.getPreferencesFor(provider.getClass());
	}
	
}
