/**
 *
 * @author wrzodek
 */
package de.zbit.gui.cfg;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
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
	 * @see de.zbit.gui.cfg.PreferencesPanel#checkPreferences()
	 */
	@Override
	public List<String> checkPreferences() {
		List<String> errors = new LinkedList<String>();
		Object fieldValue;
		Option<?> o;
		
		for (Field field : provider.getDeclaredFields()) {
			try {
				fieldValue = field.get(provider);
				if (fieldValue instanceof Option<?>) {
					o = (Option<?>) fieldValue;
					Object val = o.getValue(preferences);
					if (val==null) {
						errors.add("Could not determine value of " + o);
					} else {
						if (o.isSetRangeSpecification()) {
							if (!o.getRange().castAndCheckIsInRange(val)) {
								errors.add(o + " is out of range. Please select one out of " + o.getRangeSpecifiaction() + ".");
							}
						} else {
							if (o.parseOrCast(val)==null) {
								errors.add(o + " is of invalid type. Please specify an instance of " + o.getRequiredType().getSimpleName());
							}
							// TODO: Additional checks( e.g. file.canRead, etc.).
						}
					}
				}
			} catch (Exception exc) {
				// ignore non-static fields
			}
		}
		return errors;
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
