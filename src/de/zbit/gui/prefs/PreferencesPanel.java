package de.zbit.gui.prefs;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import de.zbit.gui.JColumnChooser;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.prefs.FileSelector.Type;
import de.zbit.io.GeneralFileFilter;
import de.zbit.io.SBFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * Abstract super class for any {@link PreferencesPanel}s, i.e., a GUI element
 * on which the user may manipulate some preferences. All these are gathered in
 * a {@link Properties} object. There is one such {@link Properties} element for
 * the default settings and one that only contains those key-value pairs of
 * interest here. All key-value pairs that are not required by this class are
 * removed from the current {@link Properties} element before initialization.
 * The {@link #init()} method creates the layout of this {@link Panel}. You may
 * consider to override the method {@link #initConstantFields(Properties)} to
 * extract other key-value pairs than those that are directly needed for
 * manipulation. The {@link #getProperties()} method may also be overridden in
 * case that not all {@link Properties} can be updated while this object is
 * still visible. In some cases this method may have to gather information from
 * other GUI elements. Furthermore, this element already implements several
 * methods to notify listeners about changes. This is absolutely required, but
 * you may want to override these methods, such as {@link #keyPressed(KeyEvent)}
 * or {@link #itemStateChanged(ItemEvent)}. Please make sure not to forget to
 * call these methods from the super class also.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-04-13
 */
public abstract class PreferencesPanel extends JPanel implements KeyListener,
		ItemListener, ChangeListener {
	
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1852850798328875230L;

	/**
	 * A list of {@link ChangeListener}s to be notified in case that values
	 * change on this {@link PreferencesPanel}.
	 */
	private List<ChangeListener> changeListeners;

	/**
	 * A list of {@link ItemListener}s to be notified when switching items on
	 * this {@link PreferencesPanel}.
	 */
	private List<ItemListener> itemListeners;
	/**
	 * Stores a sorted mapping between {@link Option}s and corresponding {@link OptionGroup}s.
	 */
	SortedMap<Option<?>, OptionGroup<?>> option2group;
	/**
	 * Stores those options that do not belong to any {@link OptionGroup}.
	 */
	@SuppressWarnings("unchecked")
	SortedSet<Option> ungroupedOptions;

	/**
	 * Stores a (sorted) {@link List} of all {@link OptionGroup}s belonging to this class.
	 */
	@SuppressWarnings("unchecked")
  List<OptionGroup> optionGroups;

	/**
	 * These are the persistently saved user-preferences of which some ore all
	 * elements are possibly to be changed in this panel. But only if the user
	 * wants. Hence, we have to first manipulate the field {@link #properties}
	 * and can maybe persist these changes.
	 */
	protected SBPreferences preferences;
	
	/**
	 * The settings to be changed by the user including default settings as a
	 * backup.
	 */
	protected SBProperties properties;

	/**
	 * Creates a new {@link PreferencesPanel}.
	 * 
	 * @param properties
	 *            The current user properties. These will be filtered to contain
	 *            only accepted elements. Access to other elements is possible
	 *            by overriding {@link #initConstantFields(Properties)}.
	 * @throws IOException
	 * @see #accepts(Object)
	 */
	public PreferencesPanel() throws IOException {
		this(true);
	}

	/**
	 * If you decide not to initialize the panel imideately, you HAVE TO
	 * call {@link #initializePrefPanel()} in the calling constructor.
	 * @param init_Panel
	 * @throws IOException
	 */
	protected PreferencesPanel(boolean init_Panel) throws IOException {
		super();
		/*
		 * We have to move this into a separate method, because it calls
		 * abstract functions and they may require an initilization first,
		 * by extending methds (e.g. see PreferencesPanelForKeyProvider).
		 */
		if (init_Panel) {
		  initializePrefPanel();
		}
	}

	/**
	 * This method decides whether or not this {@link PreferencesPanel} accepts
	 * the given key parameter as a valid key for which an option can be shown
	 * on this panel. This method is necessary to filter those settings that are
	 * not supported to avoid later write conflicts.
	 * 
	 * @param key
	 *            A key parameter for which it is to be decided whether it
	 *            constitutes a valid option here.
	 * @return True if the given key corresponds to a valid option, false
	 *         otherwise.
	 */
	public abstract boolean accepts(Object key);

	/**
	 * Adds the given {@link ChangeListener} to this element's list of this kind
	 * of listeners.
	 * 
	 * @param listener
	 *            the element to be added.
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/**
	 * Adds the given {@link ItemListener} to this element's list of this kind
	 * of listeners.
	 * 
	 * @param listener
	 *            the element to be added.
	 */
	public void addItemListener(ItemListener listener) {
		itemListeners.add(listener);
	}
	/**
	 * 
	 * @param lh
	 * @param options
	 * @param deleteFromHere Processed options will be deleted from this {@link Map}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	List<Option<?>> addOptions(LayoutHelper lh,
		Iterable<? extends Option> options,
		Map<Option<?>, OptionGroup<?>> deleteFromHere) {
		List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
		for (Option<?> option : options) {
			// Create swing option based on field type
			JComponent jc = properties.containsKey(option) ? getJComponentForOption(option, properties, this) : null;
			if (jc != null) {
				if (jc instanceof FileSelector) {
					FileSelector.addSelectorsToLayout(lh, (FileSelector) jc);
				} else if (jc instanceof JColumnChooser) {
				  JColumnChooser.addSelectorsToLayout(lh, (JColumnChooser) jc);
				} else {
					lh.add(jc);
				}
				if (deleteFromHere != null) {
					deleteFromHere.remove(option);
				}
			} else {
				// Remember unprocessed options
				unprocessedOptions.add(option);
			}
		}
		return unprocessedOptions;
	}

	/**
	 * Automatically builds an option panel, based on the static Option fields
	 * of the preferences.keyProvider.
	 * 
	 * @return all options that could not automatically be converted into a
	 *         JComponent.
	 */
	public List<Option<?>> autoBuildPanel() {
		List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
		LayoutHelper lh = new LayoutHelper(this);

		// search for OptionGroups first
		searchForOptionGroups();
		
		// First we create GUI elements for all groups
		for (OptionGroup<?> optGrp : optionGroups) {
			lh.add(createGroup(optGrp, unprocessedOptions));
		}
		
		// Now we consider what is left
		unprocessedOptions.addAll(addOptions(lh, ungroupedOptions, null));
		
		return unprocessedOptions;
	}

	/**
	 * 
	 * @param optGrp
	 * @param unprocessedOptions
	 *        where to put those options that could not be added to the groups
	 *        panel.
	 * @return
	 */
	Component createGroup(OptionGroup<?> optGrp,
		List<Option<?>> unprocessedOptions) {
		JPanel groupPanel;
		String title;
		groupPanel = new JPanel();
		LayoutHelper groupsLayout = new LayoutHelper(groupPanel);
		unprocessedOptions.addAll(addOptions(groupsLayout, optGrp.getOptions(),
			option2group));
		if (optGrp.isSetName()) {
			title = StringUtil.concat(" ", optGrp.getName().trim(), " ").toString();
			groupPanel.setBorder(BorderFactory.createTitledBorder(title));
		} else {
			groupPanel.setBorder(BorderFactory.createEtchedBorder());
		}
		if (optGrp.isSetToolTip()) {
			groupPanel.setToolTipText(StringUtil.toHTML(optGrp.getToolTip(), 60));
		}
		return groupPanel;
	}

	/**
	 * The default {@link Properties} are the standard values to be used if the
	 * user wants to re-initialize this object. With this method you can access
	 * these elements. Note that the default properties are never filtered and
	 * may therefore contain many additional elements in comparison to the
	 * current properties of this {@link PreferencesPanel}
	 * 
	 * @return The {@link Properties} object containing the default key-value
	 *         pairs of user settings.
	 * @see #getProperties()
	 */
	public Properties getDefaultProperties() {
		return properties != null ? properties.getDefaults() : new Properties();
	}
	
	/**
	 * 
	 * @param <T>
	 * @param option
	 * @return
	 */
	public <T> T getProperty(Option<T> option) {
	  return option.parseOrCast(properties.getProperty(option));
	}
	
	/**
	 * Initializes a Option type specific JComponent without any listeners and default
	 * values.
	 * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener, KeyListener)
	 * @param option
	 * @return
	 */
	public JComponent getJComponentForOption(Option<?> option) {
	  return getJComponentForOption(option, properties, this);
	}
	
	/**
	 * With this method it is possible to create a {@link JComponent} based on the persistently saved user 
	 * preferences, i.e., here an instance of {@link SBPreferences}. This is in contrast to {@link SBProperties},
	 * i.e., a current in-memory user-configuration.
	 * 
	 * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener, KeyListener)
	 * @param option
	 * @param prefs
	 * @param l
	 * @return
	 */
	public static JComponent getJComponentForOption(Option<?> option, SBPreferences prefs, EventListener l) {
	  Object def = prefs != null ? option.getValue(prefs) : option.getDefaultValue();
	  return getJComponentForOption(option, def, l);
	}
	
	/**
	 * With this method it is possible to create a {@link JComponent} based on the current in-memory 
	 * preferences, i.e., here an instance of {@link SBProperties}. This is in contrast to {@link SBPreferences},
	 * i.e., a persistently saved user-configuration.
	 * 
	 * @param option
	 * @param probs
	 * @param l
	 * @return
	 * @see #getJComponentForOption(Option, SBPreferences, EventListener)
	 */
	public static JComponent getJComponentForOption(Option<?> option, SBProperties probs, EventListener l) {
	  Object def = probs != null ? probs.get(option) : option.getDefaultValue();
	  return getJComponentForOption(option, def, l);
	}
	
	/**
	 * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener, KeyListener)
	 * @param option
	 * @param def
	 * @param l
	 * @return
	 */
	public static JComponent getJComponentForOption(Option<?> option, Object def, EventListener l) {
	  return getJComponentForOption(option, def, 
	    (l instanceof ItemListener?(ItemListener)l:null),
	    (l instanceof ChangeListener?(ChangeListener)l:null),
	    (l instanceof KeyListener?(KeyListener)l:null)  );
	}
	
	
	/**
	 * Automatically generates a JComponent for the given option. This includes:
	 * <ul>
	 * <li>Setting the text</li>
	 * <li>Setting the name</li>
   * <li>Setting the tool tip text</li>
   * <li>Setting the default value</li>
   * <li>Adding this panel as item-listener</li>
   * </ul>
   * 
   * @param option
   *            - option to build the JComponent for.
   * @param defaultValue - default value (should be same class as the "?" in Option<?> (optional)
   * @param itemListener - ItemListener (optional)
   * @param changeListener - ChangeListener (optional)
   * @param keyListener - KeyListener (optional)
   * @return JComponent or NULL if the getRequiredType() is unknown.
   */
	public static JComponent getJComponentForOption(Option<?> option, Object defaultValue,
            ItemListener itemListener, ChangeListener changeListener,
            KeyListener keyListener) {
		// Create swing option based on field type
		JComponent component = null;
		String optionTitle = option.formatOptionName();
		
		// If a range is specified, get all possible values.
		String[] values = null;
		
		if (option.getRange() != null) {
			List<?> val = option.getRange().getAllAcceptableValues();
			if (val != null) {
				values = new String[val.size()];
				for (int i = 0; i < val.size(); i++) {
					values[i] = val.get(i).toString();
				}
			}
		}
		
    // Get default value
    String defPath = null;
    //Object def = preferences!=null?option.getValue(preferences):null;
    
		// TODO: Group, test, and accept automatically
		Class<?> clazz = option.getRequiredType();
		if (Boolean.class.isAssignableFrom(clazz)) {
			component = new JCheckBox();
			if (defaultValue!=null) {
				((AbstractButton) component).setSelected(Boolean.parseBoolean(defaultValue.toString()));
			}
			
			//((AbstractButton) jc).setSelected(Boolean.parseBoolean(properties
			//		.get(o.getOptionName()).toString()));
		} else if (File.class.isAssignableFrom(clazz)) {
			// Infere type
			Type ty = Type.SAVE;
			String check = optionTitle.toLowerCase();
			if (check.contains("open")  || check.contains("load")
					|| check.contains("input")) {
				ty = Type.OPEN;
			}
			
      // Get default value
			if (defaultValue == null) {
				defPath = null;
			} else if (defaultValue instanceof File) {
				defPath = ((File) defaultValue).getPath();
			} else {
				defPath = defaultValue.toString();
			}
			
			boolean isDirectory = false;
			if (option.isSetRangeSpecification()
					&& option.getRange().isSetConstraints()
					&& (option.getRange().getConstraints() instanceof GeneralFileFilter)) {
				GeneralFileFilter filter = (GeneralFileFilter) option.getRange()
						.getConstraints();
				if (filter == SBFileFilter.createDirectoryFilter()) {
					isDirectory = true;
				}
				component = new FileSelector(ty, defPath, isDirectory, new GeneralFileFilter[] {filter});
			} else {
				component = new FileSelector(ty, defPath, isDirectory, (FileFilter[]) null);
			}
			((FileSelector)component).setLabelText(optionTitle);
			
		} else if (Character.class.isAssignableFrom(clazz)) {
			component = new JColumnChooser(optionTitle, true, values);
			((JColumnChooser)component).setAcceptOnlyIntegers(false);
			//JComponent cs = ((JColumnChooser)jc).getColumnChooser();
			// TODO: Limit maximum size to one (don't accept inputs after that).
			
		} else if (String.class.isAssignableFrom(clazz) ||
				(Enum.class.isAssignableFrom(clazz))) {
			component = new JColumnChooser(optionTitle, true, values);
			((JColumnChooser)component).setAcceptOnlyIntegers(false);
			
		} else if (Number.class.isAssignableFrom(clazz)) {
			component = new JColumnChooser(optionTitle, true, values);
			if (!Utils.isInteger(option.getRequiredType())) {
   			// TODO: implement Box for doubles.
				// For doulbes, we need to allow ',' and '.'.
				((JColumnChooser)component).setAcceptOnlyIntegers(false);
			}
		}

		// Check if the option could be converted to a JComponent
		if (component != null) {
			if (component instanceof AbstractButton) {
				((AbstractButton) component).setText(optionTitle);
			} else if (component instanceof JColumnChooser) {
				((JColumnChooser) component).setTitle(optionTitle);
				if (defaultValue!=null) ((JColumnChooser)component).setDefaultValue(defaultValue.toString());
				// Remove preview and reset predefined JColumnChooser layout.
				((JColumnChooser)component).hidePreview();
				((JColumnChooser)component).setPreferredSize(null);
				((JColumnChooser)component).setLayout(new FlowLayout());
			}
			
			if ((itemListener!=null) && Reflect.contains(component, "addItemListener", ItemListener.class)) {
				Reflect.invokeIfContains(component, "addItemListener", ItemListener.class, itemListener);
			} else if (changeListener!=null) {
				Reflect.invokeIfContains(component, "addChangeListener", ChangeListener.class, changeListener);
			}
			component.setName(option.getOptionName());
			component.setToolTipText(StringUtil.toHTML(option.getDescription(), 60));
			if (keyListener != null) {
        component.addKeyListener(keyListener);
      }
			
			//jc.setBorder(new TitledBorder("test"));
		}

		return component;
	}

	/**
	 * A derived class may override this method because it might be necessary to
	 * gather information from some GUI elements in the properties field
	 * variable. By default, this method returns a pointer to the properties
	 * field assuming that during the manipulation of all fields by the user the
	 * entries within this {@link Properties} object have been updated already.
	 * 
	 * @return A pointer to the currently set properties of this class.
	 */
	public SBProperties getProperties() {
		return properties;
	}

	/**
	 * Returns a meaningful human-readable title for this
	 * {@link PreferencesPanel}.
	 * 
	 * @return A representative title for this element.
	 */
	public abstract String getTitle();

	/**
	 * Initializes the layout and GUI of this {@link PreferencesPanel}. This
	 * method should use the values stored in the field variable
	 * {@link #properties}. Please note, although the {@link #accepts(Object)}
	 * method is used to filter possible values from the given
	 * {@link Properties}, there is no guarantee that a desired key-value pair
	 * is set. You can also access the {@link #defaultProperties} field here.
	 * Please make sure, all GUI elements used on this {@link PreferencesPanel}
	 * provide some method to update the {@link #properties} field as this is
	 * the interesting value to be returned by the {@link #getProperties()}
	 * method.
	 */
	public abstract void init();

	/**
	 * The main initialization method, that must be called by
	 * every constructor.
	 * @throws IOException
	 */
	protected void initializePrefPanel() throws IOException {
		changeListeners = new LinkedList<ChangeListener>();
		itemListeners = new LinkedList<ItemListener>();
		properties = new SBProperties(new SBProperties());
		preferences = loadPreferences();
		if (preferences != null) {
			String k;
			for (Object key : preferences.keySetFull()) {
				if (accepts(key)) {
					k = key.toString();
					properties.put(k, preferences.get(k));
					properties.getDefaults().put(k, 
						preferences.getDefault(k));
				}
			}
		}
		init();
	}

	/**
	 * Method to test whether the current properties equal the default
	 * configuration.
	 * 
	 * @return true if the values of all current properties equal the values of
	 *         the defaults.
	 */
	public boolean isDefaultConfiguration() {
		for (Entry<Object, Object> e : properties.entrySet()) {
			if (!e.getValue().equals(properties.getDefaults().get(e.getKey()))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to test whether the current properties equal the user's current
	 * configuration.
	 * 
	 * @return true if the values of all current properties equal the current
	 *         persistent user configuration.
	 */
	public boolean isUserConfiguration() {
		for (Entry<Object, Object> e : properties.entrySet()) {
			if (!e.getValue().toString().equals(
					preferences.get(e.getKey().toString()))) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		setProperty(properties, e.getSource());
		for (ItemListener i : itemListeners) {
			i.itemStateChanged(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		setProperty(properties, e.getSource());
		for (KeyListener i : getKeyListeners()) {
			i.keyPressed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		setProperty(properties, e.getSource());
		for (KeyListener kl : getKeyListeners()) {
			kl.keyReleased(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		setProperty(properties, e.getSource());
		for (KeyListener kl : getKeyListeners()) {
			kl.keyTyped(e);
		}
	}

	/**
	 * This method loads an {@link SBPreferences} object whose key-value pairs
	 * are to be manipulated by the user. In this way, this method tells this
	 * class where these preferences are located.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract SBPreferences loadPreferences() throws IOException;

	/**
	 * Persistently stores the currently set options, i.e., key-value pairs in
	 * the user's configuration. Depending on the operating system, the way how
	 * this information is actually stored can vary.
	 * 
	 * @throws BackingStoreException
	 */
	public void persist() throws BackingStoreException {
		preferences.putAll(properties);
		preferences.flush();
	}

	/**
	 * Removes the given {@link ChangeListener} from the list of this kind of
	 * listeners in this object.
	 * 
	 * @param listener
	 *            the element to be removed.
	 * @return true if the list contained the specified element.
	 */
	public boolean removeChangeListener(ChangeListener listener) {
		return changeListeners.remove(listener);
	}

	/**
	 * Removes the given {@link ItemListener} from the list of this kind of
	 * listeners in this object.
	 * 
	 * @param listener
	 *            the element to be removed.
	 * @return true if the list contained the specified element.
	 */
	public boolean removeItemListener(ItemListener listener) {
		return itemListeners.remove(listener);
	}

	/**
	 * Switches all properties back to the given default values and
	 * re-initializes the graphical user interface.
	 */
	public void restoreDefaults() {
		setProperties(preferences != null ? preferences.getDefaults()
				: new SBProperties());
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	void searchForOptionGroups() {
		option2group = new TreeMap<Option<?>, OptionGroup<?>>();
		ungroupedOptions = new TreeSet<Option>();
		Class<? extends KeyProvider> keyProvider = preferences.getKeyProvider();
		ungroupedOptions.addAll(KeyProvider.Tools.optionList(keyProvider));
		optionGroups = KeyProvider.Tools.optionGroupList(keyProvider);
		for (OptionGroup<?> group : optionGroups) {
		  for (Option<?> option : group.getOptions()) {
		    option2group.put(option, group);
		    ungroupedOptions.remove(option);
		  }
		}
	}

	/**
	 * Filters the given properties and only keeps those key-value pairs that
	 * are accepted by this class. Then it initializes the layout of this GUI
	 * element and also allows to select non-modifiable but important key-value
	 * pairs from the given {@link Properties} by overriding the method
	 * {@link #initConstantFields(Properties)}.
	 * 
	 * @param map
	 *            All available current properties of the user.
	 * @see #accepts(Object)
	 */
	public void setProperties(Map<Object, Object> map) {
		for (Object key : map.keySet()) {
			if (accepts(key)) {
				this.properties.setProperty(key.toString(), map.get(key)
						.toString());
			}
		}
		removeAll();
		init();
	}


	/**
	 * Attempts to set the new value of the changed element in this panel's
	 * properties. To this end, it is required that the name property is set for
	 * each graphical component that may change.
	 * 
	 * This static method may be used by all methods using elements from
	 * {@link #getJComponentForOption(Option)} (and similar) to change the
	 * respective properties.
	 * 
	 * @param properties
	 *        - should be either {@link SBPreferences} or {@link SBProperties}.
	 * @param source
	 *        The element whose value has been changed (e.g., in events, this
	 *        should be e.getSource()).
	 * 
	 */
	public static void setProperty(Map<Object, Object> properties, Object source) {
		if (source instanceof Component) {
			Component c = (Component) source;
			String name = c.getName();
			//System.out.print("DEBUG - try to change property of "+ name);
			/*
			 * Properties is build in initializePrefPanel() -> loadPreferences -> accep(key).
			 * If a key is missing in properties, it is very likely that one of the above mentioned
			 * methods doesn't work correctly.
			 */
			if ((name != null) && (properties.containsKey(name))) { 
				// XXX: Q: Don't we have to use preferences here?
				String value = null;
				if (c instanceof AbstractButton) {
					value = Boolean.toString(((AbstractButton) c).isSelected());
				} else if (c instanceof JColorChooser) {
					value = ((JColorChooser) c).getColor().toString();
				} else if (c instanceof JComboBox) {
					value = ((JComboBox) c).getSelectedItem().toString();
				} else if (c instanceof JFileChooser) {
					value = ((JFileChooser) c).getSelectedFile()
							.getAbsolutePath();
				} else if (c instanceof JList) {
					value = ((JList) c).getSelectedValue().toString();
				} else if (c instanceof JSlider) {
					value = Integer.toString(((JSlider) c).getValue());
				} else if (c instanceof JSpinner) {
					value = ((JSpinner) c).getValue().toString();
				} else if (c instanceof FileSelector) {
					try {
						value = ((FileSelector)c).getSelectedFile().getPath();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (c instanceof JTable) {
					JTable tab = (JTable) c;
					value = tab.getModel().getValueAt(tab.getSelectedRow(),
							tab.getSelectedColumn()).toString();
				} else if (c instanceof JTextComponent) {
					value = ((JTextComponent) c).getText();
				}
				
				properties.put(name, value);
				//System.out.println(" - " + "changed to '" + value.toString() + "'.");
			} else {
				//System.out.println(" - " + "failed: properties contains no key with that name.");
			}
		}
	}
	
	/**
	 * Sets the property for the given {@link Option}.
	 * 
	 * @param <T>
	 * @param option
	 * @param value
	 */
	public <T> void setProperty(Option<T> option, T value) {
		properties.setProperty(option.toString(), value.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}
}
