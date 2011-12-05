/*
 * $Id$ $URL:
 * svn://rarepos/SysBio/trunk/src/de/zbit/gui/prefs/PreferencesPanel.java $
 * --------------------------------------------------------------------- This
 * file is part of the SysBio API library.
 * 
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.prefs;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
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

import de.zbit.gui.ColorChooserWithPreview;
import de.zbit.gui.ExpandablePanel;
import de.zbit.gui.GUITools;
import de.zbit.gui.JLabeledComponent;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.csv.CSVReaderOptionPanel;
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
 * @version $Rev$
 * @since 1.0
 */
public abstract class PreferencesPanel extends JPanel implements KeyListener,
    ItemListener, ChangeListener {
	
	/**
	 * A {@link Logger} for this class.
	 */
  public static final transient Logger log = Logger
      .getLogger(PreferencesPanel.class.getName());
  
  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = 1852850798328875230L;
  
  /**
   * A list of {@link ChangeListener}s to be notified in case that values change
   * on this {@link PreferencesPanel}.
   */
  private List<ChangeListener> changeListeners;
  
  /**
   * A list of {@link ItemListener}s to be notified when switching items on this
   * {@link PreferencesPanel}.
   */
  private List<ItemListener> itemListeners;
  /**
   * Stores a sorted mapping between {@link Option}s and corresponding
   * {@link OptionGroup}s.
   */
  SortedMap<Option<?>, OptionGroup<?>> option2group;
  
  /**
   * Stores a sorted mapping between {@link Option}s and corresponding
   * {@link JComponent}s.
   */
  SortedMap<Option<?>, JComponent> option2component = new TreeMap<Option<?>, JComponent>();
  
  /**
   * Stores those options that do not belong to any {@link OptionGroup}.
   */
  @SuppressWarnings("rawtypes")
  SortedSet<Option> ungroupedOptions;
  
  /**
   * Stores a (sorted) {@link List} of all {@link OptionGroup}s belonging to
   * this class.
   */
  @SuppressWarnings("rawtypes")
  SortedMap<String, List<OptionGroup>> optionGroups;
  
  /**
   * Memorizes how many {@link OptionGroup}s contain {@link FileSelector}s in
   * each {@link KeyProvider} of interest.
   */
  Map<String, Integer> keyProvider2fileGroups;
    
  /**
   * These are the persistently saved user-preferences of which some ore all
   * elements are possibly to be changed in this panel. But only if the user
   * wants. Hence, we have to first manipulate the field {@link #properties} and
   * can maybe persist these changes.
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
   *        The current user properties. These will be filtered to contain only
   *        accepted elements. Access to other elements is possible by
   *        overriding {@link #initConstantFields(Properties)}.
   * @throws IOException
   * @see #accepts(Object)
   */
  public PreferencesPanel() throws IOException {
    this(true);
  }
  
  /**
   * If you decide not to initialize the panel immediately, you HAVE TO call
   * {@link #initializePrefPanel()} in the calling constructor.
   * 
   * @param initPanel
   * @throws IOException
   */
  protected PreferencesPanel(boolean initPanel) throws IOException {
    super();
    /*
     * We have to move this into a separate method, because it calls abstract
     * functions and they may require an initialization first, by extending
     * methods (e.g., see PreferencesPanelForKeyProvider).
     */
    if (initPanel) {
      initializePrefPanel();
    }
  }
  
  /**
   * This method decides whether or not this {@link PreferencesPanel} accepts
   * the given key parameter as a valid key for which an option can be shown on
   * this panel. This method is necessary to filter those settings that are not
   * supported to avoid later write conflicts.
   * 
   * @param key
   *        A key parameter for which it is to be decided whether it constitutes
   *        a valid option here.
   * @return True if the given key corresponds to a valid option, false
   *         otherwise.
   */
  public abstract boolean accepts(Object key);
  
  /**
   * Adds the given {@link ChangeListener} to this element's list of this kind
   * of listeners.
   * 
   * @param listener
   *        the element to be added.
   */
  public void addChangeListener(ChangeListener listener) {
    changeListeners.add(listener);
  }
  
  /**
   * Adds the given {@link ItemListener} to this element's list of this kind of
   * listeners.
   * 
   * @param listener
   *        the element to be added.
   */
  public void addItemListener(ItemListener listener) {
    itemListeners.add(listener);
  }
  
  /**
   * 
   * @param lh
   * @param options
   * @param deleteFromHere
   *        Processed options will be deleted from this {@link Map}.
   * @return options, for which no {@link JComponent} could be created automatically
   */
  @SuppressWarnings("rawtypes")
  List<Option<?>> addOptions(LayoutHelper lh, Iterable<? extends Option> options, Map<Option<?>, OptionGroup<?>> deleteFromHere) {
    List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
    for (Option<?> option : options) {
      // Hide options that should not be visible.
      if (!option.isVisible()) {
        continue;
      }
      
      // Create swing option based on field type
      JComponent jc = null;
      if (properties.containsKey(option)) {
        // Check if we already have an element for this option
        // Unfortunately is no good choice: this renderse the
        // "reset defaults" button useless...
        //jc = option2component.get(option);
        
        if (jc == null) {
          jc = getJComponentForOption(option, properties, this);
        }
      }
      
      // Add to layout component
      if (jc != null) {
        if (jc instanceof FileSelector) {
          FileSelector.addSelectorsToLayout(lh, (FileSelector) jc);
        } else if (jc instanceof JLabeledComponent) {
          JLabeledComponent.addSelectorsToLayout(lh, (JLabeledComponent) jc);
        } else {
          // Most options have 3 columns (2 real + a spacer).
          lh.addWithWidth(jc, 3);
        }
        if (deleteFromHere != null) {
          deleteFromHere.remove(option);
        }
        if (option2component != null) {
          option2component.put(option, jc);
        }
        
      } else {
        // Remember unprocessed options
        unprocessedOptions.add(option);
      }
      
    }
    return unprocessedOptions;
  }
  
  /**
   * Automatically builds an option panel, based on the static Option fields of
   * the {@link #preferences#getKeyProvider()}.
   * 
   * @return all options that could not automatically be converted into a
   *         JComponent.
   */
  @SuppressWarnings("unchecked")
  public List<Option<?>> autoBuildPanel() {
    return autoBuildPanel(preferences.getKeyProvider());
  }
  
  /**
   * Automatically builds an option panel, based on the given fields in the
   * {@link KeyProvider}s.
   * 
   * @param keyProviders
   * @return all options that could not automatically be converted into a
   *         JComponent.
   */
  @SuppressWarnings("rawtypes")
  protected List<Option<?>> autoBuildPanel(Class<? extends KeyProvider>... keyProviders) {
    List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
        
    // search for OptionGroups first
    searchForOptionGroups(keyProviders);
    LayoutHelper lh = new LayoutHelper(this), helper;
    boolean multipleKeyProviders = keyProviders.length > 1;
    int elemCount;
    
    for (Class<? extends KeyProvider> keyProvider : keyProviders) {
      helper = multipleKeyProviders ? new LayoutHelper(new JPanel()) : lh;
      elemCount = insertOptionGroups(optionGroups.get(keyProvider.getName()),
        keyProvider2fileGroups.get(keyProvider.getName()).intValue(),
        unprocessedOptions, helper);
      if (multipleKeyProviders && (elemCount > 0)) {
        lh.add(helper.getContainer());
      }
    }
    
    // Now we consider what is left
    if (ungroupedOptions.size() > 0) {
      /* We only have one column in the layout so far (only grouped panels).
       * Thus, we should keep the one column layout and create a new group for
       * ungrouped options.
       */
      elemCount = 0;
      for (Option<?> option : ungroupedOptions) {
        if (option.isVisible()) {
          elemCount++;
        }
      }
      if (elemCount > 0) {
        lh.add(createGroup((Collection<Option>) ungroupedOptions,
          unprocessedOptions));
      }
    }
    
    // And finally we create the dependencies
    PreferencesPanelDependencies.configureDependencies(this);
    
    return unprocessedOptions;
  }
  
  /**
   * Helper method to arrange {@link OptionGroup}s for just one
   * {@link KeyProvider} on a {@link LayoutHelper}.
   * 
   * @param groupList
   * @param fileSelectors
   * @param unprocessedOptions
   * @param lh
   * @return the number of elements that are visibly added to the given
   *         {@link LayoutHelper}.
   */
  @SuppressWarnings("rawtypes")
  private int insertOptionGroups(List<OptionGroup> groupList,
    int fileSelectors, List<Option<?>> unprocessedOptions, LayoutHelper lh) {
    boolean twoColumn = ((groupList.size() - fileSelectors) % 2 == 0)
        && (ungroupedOptions.size() == 0);
    boolean oneColumn = false;
    
    // First we create GUI elements for all groups
    int column = 0;
    int row = 0;
    int elemCount = 0;
    Component c;
    for (OptionGroup<?> optGrp : groupList) {
      if (optGrp.isVisible()) {
        // Group is visible => Generate components.
        c = createGroup(optGrp, unprocessedOptions);
        if (twoColumn) {
          for (Option<?> opt : optGrp) {
            if (opt.getRequiredType().equals(File.class)) {
              oneColumn = true;
              break;
            }
          }
          if (oneColumn) {
            column = 0;
            lh.add(c, column, row++, 2, 1);
            elemCount++;
            oneColumn = false;
          } else {
            lh.add(c, column++, row, 1, 1);
            elemCount++;
            if (column == 2) {
              column = 0;
              row++;
            }
          }
        } else {
          lh.add(c);
          elemCount++;
        }
      } else {
        // Remove from internal "to-do" list.
        for (Option<?> o: optGrp) {
          option2group.remove(o);
        }
      }
    }
    return elemCount;
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
    
    // Create a new panel for the group
    JPanel groupPanel = new JPanel();
    LayoutHelper groupsLayout = new LayoutHelper(groupPanel);
    // Add all child-options to this panel 
    unprocessedOptions.addAll(addOptions(groupsLayout, optGrp.getOptions(), option2group));
    
    // Set Border, Name and ToolTip
    String title = (optGrp.isSetName()) ? StringUtil.concat(" ", optGrp.getName().trim(), " ").toString() : null;
    String toolTip = (optGrp.isSetToolTip()) ? StringUtil.toHTML(optGrp.getToolTip(), GUITools.TOOLTIP_LINE_LENGTH) : null;
    groupPanel.setToolTipText(toolTip);
    if (optGrp.isCollapsable()) {
      // Create a collapsible panel
      ExpandablePanel parentPanel = new ExpandablePanel(title, groupPanel, optGrp.isInitiallyCollapsed(), true);
      parentPanel.setToolTipText(toolTip);
      return parentPanel;
      
    } else {
      // Set border and return simple panel
      if (title != null) {
        groupPanel.setBorder(BorderFactory.createTitledBorder(title));
      } else {
        groupPanel.setBorder(BorderFactory.createEtchedBorder());
      }
      return groupPanel;
    }
  }
  
  /**
   * @param optGrp
   * @param unprocessedOptions
   *        where to put those options that could not be added to the groups
   *        panel.
   * @return
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  Component createGroup(Collection<Option> optGrp,
    List<Option<?>> unprocessedOptions) {
    OptionGroup group = new OptionGroup();
    group.addAll(optGrp);
    return createGroup(group, unprocessedOptions);
  }
  
  /**
   * The default {@link Properties} are the standard values to be used if the
   * user wants to re-initialize this object. With this method you can access
   * these elements. Note that the default properties are never filtered and may
   * therefore contain many additional elements in comparison to the current
   * properties of this {@link PreferencesPanel}
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
   * Initializes a Option type specific JComponent without any listeners and
   * default values.
   * 
   * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener,
   *      KeyListener)
   * @param option
   * @return
   */
  public JComponent getJComponentForOption(Option<?> option) {
    return getJComponentForOption(option, properties, this);
  }
  
  /**
   * With this method it is possible to create a {@link JComponent} based on the
   * persistently saved user preferences, i.e., here an instance of
   * {@link SBPreferences}. This is in contrast to {@link SBProperties}, i.e., a
   * current in-memory user-configuration.
   * 
   * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener,
   *      KeyListener)
   * @param option
   * @param prefs
   * @param l
   * @return
   */
  public static JComponent getJComponentForOption(Option<?> option,
    SBPreferences prefs, EventListener l) {
    Object def = prefs != null ? option.getValue(prefs) : option
        .getDefaultValue();
    return getJComponentForOption(option, def, l);
  }
  
  /**
   * With this method it is possible to create a {@link JComponent} based on the
   * current in-memory preferences, i.e., here an instance of
   * {@link SBProperties}. This is in contrast to {@link SBPreferences}, i.e., a
   * persistently saved user-configuration.
   * 
   * <p>
   * NOTE: the returned Element is ALWAYS a {@link JComponent} that implements
   * the {@link JComponentForOption} interface.
   * </p>
   * 
   * @param option
   * @param probs
   * @param l
   * @return
   * @see #getJComponentForOption(Option, SBPreferences, EventListener)
   */
  public static JComponent getJComponentForOption(Option<?> option, SBProperties probs, EventListener l) {
    // Get default value
    Object def = (probs != null)? probs.get(option) : option.getDefaultValue();
    if (def != null) {
      try {
        // Try to get the real default value
        def = option.parseOrCast(def);
      } catch (Throwable t) {
        // doesn't matter, try to continue with string representation.
      }
    }
    return getJComponentForOption(option, def, l);
  }
  
  /**
   * This Method will generate a {@link JComponent}, based on the given option.
   * 
   * May be called as getJComponentForOption(Option, null, null).
   * 
   * @see #getJComponentForOption(Option, Object, ItemListener, ChangeListener,
   *      KeyListener)
   * @param option
   * @param def
   *        - default value. May be null.
   * @param l
   *        - Listener to listen for changes. One of ItemListener,
   *        ChangeListener, KeyListener or null.
   * @return
   */
  public static JComponent getJComponentForOption(Option<?> option, Object def,
    EventListener l) {
    return getJComponentForOption(option, def,
      (l instanceof ItemListener ? (ItemListener) l : null),
      (l instanceof ChangeListener ? (ChangeListener) l : null),
      (l instanceof KeyListener ? (KeyListener) l : null));
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
   * <p>
   * NOTE: the returned Element is ALWAYS a {@link JComponent} that implements
   * the {@link JComponentForOption} interface.
   * </p>
   * 
   * @param option
   *        {@link Option} to build the {@link JComponent} for.
   * @param defaultValue
   *        default value (should be same class as the "?" in Option<?>
   *        (optional)
   * @param itemListener
   *        ItemListener (optional)
   * @param changeListener
   *        ChangeListener (optional)
   * @param keyListener
   *        KeyListener (optional)
   * @return JComponent or <code>NULL</code> if {@link Option#getRequiredType()}
   *         is unknown.
   */
  @SuppressWarnings("unchecked")
  public static JComponent getJComponentForOption(Option<?> option,
    Object defaultValue, ItemListener itemListener,
    ChangeListener changeListener, KeyListener keyListener) {
    // Create swing option based on field type
    JComponent component = null;
    String optionTitle = option.isSetDisplayName() ? option.getDisplayName()
        : option.formatOptionName();
    
    // If a range is specified, get all possible values.
    Object[] values = null;
    
    if (option.getRange() != null) {
      List<?> val = option.getRange().getAllAcceptableValues();
      if (val != null) {
        values = new Object[val.size()];
        for (int i = 0; i < val.size(); i++) {
          values[i] = val.get(i);//.toString();
        }
      }
    }
    
    // Get default value
    String defPath = null;
    //Object def = preferences!=null?option.getValue(preferences):null;
    
    // TODO: Group, test, and accept automatically
    Class<?> clazz = option.getRequiredType();
    if (Boolean.class.isAssignableFrom(clazz)) {
      boolean boolDefault = false;
      if (defaultValue != null) {
        boolDefault = Boolean.parseBoolean(defaultValue.toString());
      }
      
      if (option.getButtonGroup() == null) {
        component = new OptionCheckBox();
        ((AbstractButton) component).setSelected(boolDefault);
      } else {
        component = new OptionRadioButton();
        // Only set true. Setting false may deselect others.
        if (boolDefault) {
          ((AbstractButton) component).setSelected(boolDefault);
        }
      }
      
      //((AbstractButton) jc).setSelected(Boolean.parseBoolean(properties.get(o.getOptionName()).toString()));
    } else if (File.class.isAssignableFrom(clazz)) {
      // Infere type
      Type ty = Type.OPEN;
      String check = option.getOptionName().toLowerCase();
			if (check.contains("save") || check.contains("store")
					|| check.contains("output")) {
				ty = Type.SAVE;
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
        component = new FileSelector(ty, defPath, isDirectory,
          new GeneralFileFilter[] { filter });
      } else {
        component = new FileSelector(ty, defPath, isDirectory,
          (FileFilter[]) null);
      }
      ((FileSelector) component).setLabelText(optionTitle);
      
    } else if (Character.class.isAssignableFrom(clazz)) {
      component = new JLabeledComponent(optionTitle, true, values);
      ((JLabeledComponent) component).setAcceptOnlyIntegers(false);
      // TODO: Replace certain Strings by others, e.g.
      // '\t' with "[Tab]", as already done CSVReaderOptionPanel.java
      CSVReaderOptionPanel.createCharacterBox(
        ((JLabeledComponent) component).getJTextComponent());      
      
    } else if (String.class.isAssignableFrom(clazz)
        || (Enum.class.isAssignableFrom(clazz))) {
      component = new JLabeledComponent(optionTitle, true, values);
      ((JLabeledComponent) component).setAcceptOnlyIntegers(false);
      
    } else if (Number.class.isAssignableFrom(clazz)) {
      // Try to make a spinner
      if ((values == null) || (values.length > 15)) {
        // we do not need a spinner if only 15 different values are possible.
        try {
          // Get Numeric option and Numeric default value
          Option<Number> o2 = (Option<Number>) option;
          Object defaultV = o2.parseOrCast(defaultValue);
          if ((defaultV != null)
              && !Number.class.isAssignableFrom(defaultV.getClass())) {
            defaultV = null;
          }
          // init component with a spinner.
          component = new JLabeledComponent(optionTitle, true,
            JLabeledComponent.buildJSpinner(o2, (Number) defaultV));
        } catch (Throwable t) {
          component=null;
          // Might occur when dealing with strange Number instances.
        }
      }
      
      // Fall back with regular JTextField or JComboBox
      if (component == null) {
        component = new JLabeledComponent(optionTitle, true, values);
        if (!Utils.isInteger(option.getRequiredType())) {
          ((JLabeledComponent) component).setAcceptOnlyIntegers(false);
        }
      }
      
    } else if (java.awt.Color.class.isAssignableFrom(clazz)) {
      // Create color chooser with defaultValue or white as initial color.
      Color initial = null;
      if (defaultValue instanceof Color) {
        initial = (Color) defaultValue;
      } else if (defaultValue instanceof String) {
        initial = Option.parseOrCast(Color.class, defaultValue);
      }
      if (initial == null) {
				log.warning(String.format("Invalid default value for color %s: %s",
					defaultValue.getClass(), defaultValue));
        initial = Color.WHITE;
      }
      ColorChooserWithPreview colChooser = new ColorChooserWithPreview(initial);
      component = new JLabeledComponent(optionTitle, true, colChooser);
      
    } else if ((values != null) && (values.length > 0)) {
      component = new JLabeledComponent(optionTitle, true, values);
      ((JLabeledComponent) component).setAcceptOnlyIntegers(false);
      
    } else {
			log.severe(String.format("Please implement JComponent for %s.", clazz));
    }
    
    // Check if the option could be converted to a JComponent
    if (component != null) {
      if (component instanceof AbstractButton) {
        ((AbstractButton) component).setText(optionTitle);
        // Assign button group eventually and keep selection
        if (option.getButtonGroup() != null) {
          boolean restoreSelection = ((AbstractButton) component).isSelected();
          option.getButtonGroup().add(((AbstractButton) component));
          if (restoreSelection) {
            option.getButtonGroup().setSelected(((AbstractButton) component).getModel(), true);
          }
        }
        
      } else if (component instanceof JLabeledComponent) {
        ((JLabeledComponent) component).setTitle(optionTitle);
        if (defaultValue != null) {
          // Set default value
          if (defaultValue.toString().startsWith("class ")) {
            int count = -1;
            for (Object value : option.getRange().getAllAcceptableValues()) {
              count++;
              if (defaultValue.toString().contains(value.toString())) {
                /*
                 * Note: a renderer might have changed the display-value for the
                 * component. In this case, if you directly call
                 * setDefaultValue(String) you might override the value given by
                 * the renderer (althought this would be correct as well).
                 */
                ((JLabeledComponent) component).setDefaultValue(count);
                break;
              }
            }
          } else {
            ((JLabeledComponent) component).setDefaultValue(defaultValue);
          }
        }
        // Remove preview and reset predefined JLabeledComponent layout.
        ((JLabeledComponent) component).setPreferredSize(null);
        ((JLabeledComponent) component).setLayout(new FlowLayout());
      }
      
      /*
       * Add listeners to component and set generic options (name, tooltip,
       * etc.)
       */
      if (itemListener != null) {
        Reflect.invokeIfContains(component, "addItemListener", ItemListener.class, itemListener);
      } // do NOT MAKE ELSE IF (e.g. JLabeledComponent contains both, but actual colChooser must not contain both!) !!! 
      if (changeListener != null) {
        Reflect.invokeIfContains(component, "addChangeListener", ChangeListener.class, changeListener);
      }
      component.setName(option.getOptionName());
      component.setToolTipText(StringUtil.toHTML(option.getDescription(), 60));
      if (keyListener != null) {
        component.addKeyListener(keyListener);
      }
      if (component instanceof JComponentForOption) {
        // MUST always be true.
        ((JComponentForOption) component).setOption(option);
      } else {
        // Issue a warning. Programmers should watch that each returned
        // JComponent implements the JComponentForOption interface!!!
				log.warning(String.format("%s IS NO %s!", component.getClass()
						.getName(), JComponentForOption.class.getName()));
      }      
    }
    
    return component;
  }
  
  /**
   * A derived class may override this method because it might be necessary to
   * gather information from some GUI elements in the properties field variable.
   * By default, this method returns a pointer to the properties field assuming
   * that during the manipulation of all fields by the user the entries within
   * this {@link Properties} object have been updated already.
   * 
   * @return A pointer to the currently set properties of this class.
   */
  public SBProperties getProperties() {
    return properties;
  }
  
  /**
   * Returns a meaningful human-readable title for this {@link PreferencesPanel}
   * .
   * 
   * @return A representative title for this element.
   */
  public abstract String getTitle();
  
  /**
   * Initializes the layout and GUI of this {@link PreferencesPanel}. This
   * method should use the values stored in the field variable
   * {@link #properties}. Please note, although the {@link #accepts(Object)}
   * method is used to filter possible values from the given {@link Properties},
   * there is no guarantee that a desired key-value pair is set. You can also
   * access the {@link #defaultProperties} field here. Please make sure, all GUI
   * elements used on this {@link PreferencesPanel} provide some method to
   * update the {@link #properties} field as this is the interesting value to be
   * returned by the {@link #getProperties()} method.
   */
  public abstract void init();
  
  /**
   * The main initialization method, that must be called by every constructor.
   * 
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
          // Accept only key from KeyProvider!
          // Don't put all keys in properties (also not in preferences) here!
          k = key.toString();
          properties.put(k, preferences.get(k));
          properties.getDefaults().put(k, preferences.getDefault(k));
        } else {
          log.fine(String.format("Rejecting key: %s", key));
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
      if (!e.getValue().toString()
          .equals(preferences.get(e.getKey().toString()))) { 
        return false; 
      }
    }
    return true;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
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
   * This method loads an {@link SBPreferences} object whose key-value pairs are
   * to be manipulated by the user. In this way, this method tells this class
   * where these preferences are located.
   * 
   * @return
   * @throws IOException
   */
  protected abstract SBPreferences loadPreferences() throws IOException;
  
  /**
   * Persistently stores the currently set options, i.e., key-value pairs in the
   * user's configuration. Depending on the operating system, the way how this
   * information is actually stored can vary.
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
   *        the element to be removed.
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
   *        the element to be removed.
   * @return true if the list contained the specified element.
   */
  public boolean removeItemListener(ItemListener listener) {
    return itemListeners.remove(listener);
  }
  
  /**
   * Switches all properties back to the given default values and re-initializes
   * the graphical user interface.
   */
  public void restoreDefaults() {
    setProperties(preferences != null ? preferences.getDefaults() : new SBProperties());
  }
  
  /**
	 * 
	 */
  @SuppressWarnings("unchecked")
  void searchForOptionGroups() {
    searchForOptionGroups(preferences.getKeyProvider());
  }
  
  /**
   * 
   * @param keyProviders
   */
  @SuppressWarnings("rawtypes")
  private void searchForOptionGroups(
    Class<? extends KeyProvider>... keyProviders) {
    option2group = new TreeMap<Option<?>, OptionGroup<?>>();
    ungroupedOptions = new TreeSet<Option>();
    optionGroups = new TreeMap<String, List<OptionGroup>>();
    keyProvider2fileGroups = new HashMap<String, Integer>();
    for (Class<? extends KeyProvider> keyProvider : keyProviders) {
      ungroupedOptions.addAll(KeyProvider.Tools.optionList(keyProvider));
      optionGroups.put(keyProvider.getName(), KeyProvider.Tools
          .optionGroupList(keyProvider));
    }
    int fileCount;
    Integer fileGroupCount;
    for (Class<? extends KeyProvider> keyProvider : keyProviders) {
      fileGroupCount = Integer.valueOf(0);
      keyProvider2fileGroups.put(keyProvider.getName(), fileGroupCount);
      for (OptionGroup<?> group : optionGroups.get(keyProvider.getName())) {
        fileCount = 0;
        for (Option<?> option : group) {
          option2group.put(option, group);
          ungroupedOptions.remove(option);
          if (option.getRequiredType().equals(File.class)) {
            fileCount++;
          }
        }
        if (fileCount > 0) {
          fileGroupCount = Integer.valueOf(keyProvider2fileGroups.get(
            keyProvider.getName()).intValue() + 1);
          keyProvider2fileGroups.put(keyProvider.getName(),
            fileGroupCount);
        }
      }
    }
  }
  
  /**
   * @return the {@link #preferences}.
   */
  public SBPreferences getPreferences() {
    return preferences;
  }
  
  /**
   * @return the {@link KeyProvider} of the {@link #preferences}.
   */
  public Class<? extends KeyProvider> getKeyProvider() {
    return preferences != null ? preferences.getKeyProvider() : null;
  }
  
  /**
   * Filters the given properties and only keeps those key-value pairs that are
   * accepted by this class. Then it initializes the layout of this GUI element
   * and also allows to select non-modifiable but important key-value pairs from
   * the given {@link Properties} by overriding the method
   * {@link #initConstantFields(Properties)}.
   * 
   * @param map
   *        All available current properties of the user.
   * @see #accepts(Object)
   */
  public void setProperties(Map<Object, Object> map) {
    for (Object key : map.keySet()) {
      if (accepts(key)) {
        this.properties.setProperty(key.toString(), map.get(key).toString());
      }
    }
    
    // Reset panel
    removeAll();
    init();
  }
  
  /**
   * Calls {@link #setProperty(Map, Object, boolean)} WITHOUT performing range
   * checks (to keep compatibility).
   * 
   * @see #setProperty(Map, Object, boolean)
   * @param properties
   * @param source
   */
  public static void setProperty(Map<Object, Object> properties, Object source) {
    setProperty(properties, source, false);
  }
  
  /**
   * Attempts to set the new value of the changed element in this panel's
   * properties. To this end, it is required that the name property is set for
   * each graphical component that may change.
   * 
   * This static method may be used by all methods using elements from
   * {@link #getJComponentForOption(Option)} (and similar) to change the
   * respective properties.
   * <p>
   * Remark: This method performs automatic Range checks, if and only if source,
   * or the parent of source is an instance of {@link JComponentForOption}. The
   * value will only be stored if the Range check returns true. <br/>
   * If source is NOT an instance of {@link JComponentForOption} this method
   * does NOT perform any Range check. This has to be done before calling this
   * method. Else, you may end up having invalid properties in the HashSet.
   * </p>
   * 
   * @param properties
   *        Should be either {@link SBPreferences} or {@link SBProperties}.
   * @param source
   *        The element whose value has been changed (e.g., in events, this
   *        should be e.getSource()).
   * @param checkRange
   *        Decides, wether the method should try to perform a range check
   *        before storing the property. If false, the property will be stored
   *        without any further check. This should always be true, when working
   *        with {@link Preferences}.
   */
  public static void setProperty(Map<Object, Object> properties, Object source,
    boolean checkRange) {
    if (source instanceof Component) {
      Component c = (Component) source;
      String name = c.getName();
//      System.out.print("DEBUG - try to change property of "+ name);
      /*
       * Properties is build in initializePrefPanel() -> loadPreferences ->
       * accept(key). If a key is missing in properties, it is very likely that
       * one of the above mentioned methods doesn't work correctly.
       */
      if ((name != null) && (properties.containsKey(name))) {
        String value = null;
        if (c instanceof AbstractButton) {
          value = Boolean.toString(((AbstractButton) c).isSelected());
        } else if (c instanceof JColorChooser) {
          value = ((JColorChooser) c).getColor().toString();
        } else if (c instanceof ColorChooserWithPreview) {
          value = ((ColorChooserWithPreview) c).getColor().toString();
        } else if (c instanceof JComboBox) {
          value = ((JComboBox) c).getSelectedItem().toString();
        } else if (c instanceof JFileChooser) {
          value = ((JFileChooser) c).getSelectedFile().getAbsolutePath();
        } else if (c instanceof JList) {
          value = ((JList) c).getSelectedValue().toString();
        } else if (c instanceof JSlider) {
          value = Integer.toString(((JSlider) c).getValue());
        } else if (c instanceof JSpinner) {
          value = ((JSpinner) c).getValue().toString();
        } else if (c instanceof FileSelector) {
          try {
            value = ((FileSelector) c).getSelectedFile().getPath();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else if (c instanceof JTable) {
          JTable tab = (JTable) c;
          value = tab.getModel()
              .getValueAt(tab.getSelectedRow(), tab.getSelectedColumn())
              .toString();
        } else if (c instanceof JTextComponent) {
          value = ((JTextComponent) c).getText();
        } else if (c instanceof JComponentForOption) {
          value = ((JComponentForOption)c).getCurrentValue().toString();
        }
        
        // Check before saving
        boolean isInRange = true;
        if (checkRange) {
          Option<?> o = null;
          if (c instanceof JComponent) {
            o = getOptionForJComponent((JComponent) c);
          }
          if (o != null && o.isSetRangeSpecification()) {
            isInRange = o.castAndCheckIsInRange(value);
          } else if (o == null) {
//            System.out.println("Could not get option for JComponent.");
          }
        }
        
        // When dialog is displayed and changed, error messages with invalid values
        // are displayed later! Write all options to properties first!
        if (!checkRange || isInRange) {
          properties.put(name, value);
//          System.out.println(" - " + "changed to '" + value.toString() + "'.");
        } else {
//          System.out.println(" - " + "out of Range.");
        }
      } else {
//        System.out.println(" - " + "failed: properties contains no key with that name.");
      }
    }
  }
  
  /**
   * @param c any {@link Component}
   * @return the option, associated with this component or any parent of this
   *         component.
   */
  public Option<?> getOptionForAutoGeneratedJComponent(JComponent c) {
    // Try to search in internal mapping
    if (option2component != null) {
      for (Option<?> key : option2component.keySet()) {
        if (option2component.get(key).equals(c)) {
          return key;
        }
      }
    }
    return null;
  }
  
  /**
   * @param c any {@link Component}
   * @return the option, associated with this component or any parent of this
   *         component.
   */
  public static Option<?> getOptionForJComponent(JComponent c) {
    return getOptionForJComponent(c, c);
  }
  
  private static Option<?> getOptionForJComponent(JComponent toCompare,
    Component currentRecurse) {
    
    /*
     * This does NOT work for JLabeledComponents and FileSelectors. FIXED: =>
     * Avoided by adding the getOptionForAutoGeneratedJComponent() method.
     * 
     * Unfortunately, the {@link JLabeledComponent}s and {@link FileSelector}s
     * are added in {@link PreferencesPanel#addOptions(de.zbit.gui.LayoutHelper,
     * Iterable, Map)} with their components only (not as whole objects). Thus,
     * this method can not identify these components.
     */
    if (currentRecurse instanceof JComponentForOption
        || JComponentForOption.class
            .isAssignableFrom(currentRecurse.getClass())) {
      return ((JComponentForOption) currentRecurse).getOption();
    } else if (currentRecurse instanceof PreferencesPanel
        || PreferencesPanel.class.isAssignableFrom(currentRecurse.getClass())) {
      Option<?> ret = ((PreferencesPanel) currentRecurse)
          .getOptionForAutoGeneratedJComponent(toCompare);
      if (ret != null) return ret;
    }
    if (currentRecurse.getParent() != null
        && (!currentRecurse.getParent().equals(currentRecurse))) { 
      return getOptionForJComponent(toCompare, currentRecurse.getParent()); 
    }
    return null;
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
    setProperty(properties, e.getSource());
    for (ChangeListener cl : changeListeners) {
      cl.stateChanged(e);
    }
  }

  /**
   * @param option
   * @return {@link JComponent} representing this option
   */
  public JComponent getComponentForOption(Option<?> option) {
    return option2component.get(option);
  }
  
  
}
