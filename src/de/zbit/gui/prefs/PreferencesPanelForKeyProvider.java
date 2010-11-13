/**
 *
 * @author wrzodek
 */
package de.zbit.gui.prefs;

import java.io.IOException;

import de.zbit.util.StringUtil;
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
	private Class<? extends KeyProvider> provider;

	/**
	 * @throws IOException
	 */
	public PreferencesPanelForKeyProvider(Class<? extends KeyProvider> provider) throws IOException {
		super(false); // calls init, before provider is set => many null-pointer-exceptions.
		this.provider = provider;
		initializePrefPanel();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		return preferences.keySetFull().contains(key);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return StringUtil.formatOptionName(provider.getSimpleName());
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#init()
	 */
	@Override
	public void init() {
		if (provider==null) return;
		autoBuildPanel();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#loadPreferences()
	 */
	@Override
	protected SBPreferences loadPreferences() throws IOException {
		if (provider==null) return null;
		return SBPreferences.getPreferencesFor(provider);
	}
	
}
