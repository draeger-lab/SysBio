/**
 *
 * @author wrzodek
 */
package de.zbit.gui.prefs;

import java.io.IOException;

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

	@Override
	public boolean accepts(Object key) {
		return preferences.keySetFull().contains(key);
	}
	
	@Override
	public String getTitle() {
		return KeyProvider.Tools.createTitle(provider);
		//StringUtil.formatOptionName(provider.getSimpleName());
	}
	
	@Override
	public void init() {
		if (provider != null) {
			autoBuildPanel();
		}
	}
	
	@Override
	protected SBPreferences loadPreferences() throws IOException {
		if (provider==null) return null;
		return SBPreferences.getPreferencesFor(provider);
	}
	
}
