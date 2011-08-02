/*
 * $Id:  PreferencesPanelDependencies.java 12:34:45 wrzodek $
 * $URL: PreferencesPanelDependencies.java $
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

import java.awt.Component;
import java.awt.Container;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import de.zbit.gui.JLabeledComponent;
import de.zbit.util.Reflect;
import de.zbit.util.ValuePairUncomparable;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.Range;

/**
 * This class adds automated dependencies to {@link PreferencesPanel}s.
 * <p><i>Note:<br/>The components in the {@link PreferencesPanel} must implement the
 * {@link PreferencesPanelForKeyProvider} interface (i.e., must be
 * created with {@link PreferencesPanel#getJComponentForOption(de.zbit.util.prefs.Option)})
 * and the dependencies must be configured in the {@link Option}s
 * (i.e., with {@link Option#addDependency(Option, Object)}).
 * </i></p>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class PreferencesPanelDependencies {
  
  /**
   * The {@link PreferencesPanel} for which all dependencies are being configured.
   */
  PreferencesPanel parent;
  
  public PreferencesPanelDependencies(PreferencesPanel panel) {
    parent = panel;
    processDependencies();
  }
  
  /**
   * Processes all dependencies and adds corresponding listeners to
   * {@link JComponent}s that enable or disable other components, 
   * based on the dependencies.
   * @param panel auto-build {@link PreferencesPanel}.
   */
  public static void configureDependencies(PreferencesPanel panel) {
    new PreferencesPanelDependencies(panel);
  }
  
  /**
   * A listener class the implements all required interface to react
   * on changes to any {@link JComponent}.
   * 
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public class DependencyListener implements EventListener, ActionListener, ItemListener, ChangeListener, DocumentListener, KeyListener {
    private Iterable<ValuePairUncomparable<JComponentForOption, Range<?>>> toCheck;
    private Component dependant;
    public DependencyListener(Iterable<ValuePairUncomparable<JComponentForOption, Range<?>>> toCheck, Component dependant) {
      this.toCheck = toCheck;
      this.dependant = dependant;
    }
    public void checkAndProcessConditions() {
      // Check all conditions and enable or disable
      boolean enabled=true;
      for (ValuePairUncomparable<JComponentForOption, Range<?>> valuePairUncomparable : toCheck) {
        //if (!valuePairUncomparable.getA().getCurrentValue().equals(valuePairUncomparable.getB())) {
        JComponentForOption jc = valuePairUncomparable.getA();
        if (!jc.getOption().castAndCheckRange(jc.getCurrentValue(), valuePairUncomparable.getB())) {
          enabled = false;
          break;
        }
      }
      ((Container)dependant).setEnabled(enabled);
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {checkAndProcessConditions();}
    @Override
    public void removeUpdate(DocumentEvent e) {checkAndProcessConditions();}
    @Override
    public void changedUpdate(DocumentEvent e) {checkAndProcessConditions();}
    @Override
    public void stateChanged(ChangeEvent e) {checkAndProcessConditions();}
    @Override
    public void itemStateChanged(ItemEvent e) {checkAndProcessConditions();}
    @Override
    public void actionPerformed(ActionEvent e) {checkAndProcessConditions();}
    @Override
    public void keyTyped(KeyEvent e) {
      // Do not update buttons here. The KeyEvent is not yet processed
      // i.e. the textfield has not yet changed it's value!
    }
    @Override
    public void keyPressed(KeyEvent e) {} // INTENTIONALLY LEFT BLANK
    @Override
    public void keyReleased(KeyEvent e) {checkAndProcessConditions();}
  };
  
  
  /**
   * Processes all dependencies and adds corresponding listeners to
   * {@link JComponent}s that enable or disable other components, 
   * based on the dependencies.
   */
  private void processDependencies() {
//    Class<? extends KeyProvider> keyProvider = parent.getKeyProvider();
//    List<Option> allOptions = (KeyProvider.Tools.optionList(keyProvider));
    
    // Collect all JComponents for all options
    Set<JComponentForOption> jcs = new HashSet<JComponentForOption>();
    for (JComponent jc : parent.option2component.values()) {
      if (jc instanceof JComponentForOption) {
        // Should actually hold true for all items in the source collection.
        jcs.add((JComponentForOption) jc);
      }
    }
   
    // Set dependency listeners
    for (JComponentForOption jco : jcs) {
      if (jco.getOption().hasDependencies()) {
        createDependencyActionListeners(jcs, jco);
      }
    }
  }
  
  /**
   * Get all {@link JComponent} that have been created for {@link Option}s.
   * 
   * Unfortunately, the {@link JLabeledComponent}s and {@link FileSelector}s
   * are added in {@link PreferencesPanel#addOptions(de.zbit.gui.LayoutHelper, Iterable, Map)}
   * with their components only (not as whole objects). Thus, this method
   * can not identify these components.
   * 
   * @param c any {@link Container}
   * @return a set of all {@link JComponent}s, implementing the
   * {@link JComponentForOption} interface that are in the given
   * {@link Container}<code>c</code> or any of its sub-containers.
   */
  public static Set<JComponentForOption> getAllJComponentsForOptions(Container c) {
    Set<JComponentForOption> list = new HashSet<JComponentForOption>();
    if (c == null) return list;
    
    // Iterate over all components and collect all JComponentForOption
    for (Component co: c.getComponents()) {
      if (co instanceof JComponentForOption) {
        list.add((JComponentForOption)co);
      }
      
      if (co instanceof Container) {
        list.addAll(getAllJComponentsForOptions((Container)co));
      }
    }
    return list;
  }
  
  /**
   * Processes the dependencies of the <code>dependant</code> and adds
   * listeners to all dependent JComponents from the given list, that
   * automatically enable or disable the <code>dependant</code>, based
   * on the current value of the dependent JComponents.
   * @param jcomponents
   * @param dependant
   */
  private void createDependencyActionListeners(Iterable<? extends JComponentForOption> jcomponents, final JComponentForOption dependant) {
    if (!dependant.getOption().hasDependencies()) return;
    
    // Summarize all dependent JComponents and their conditions
    final List<ValuePairUncomparable<JComponentForOption, Range<?>>> toCheck = 
      new ArrayList<ValuePairUncomparable<JComponentForOption, Range<?>>>();
    
    Map<Option<?>, Range<?>> dependsOn = dependant.getOption().getDependencies();
    for (JComponentForOption jc : jcomponents) {
      Option<?> cur = jc.getOption();
      if (dependsOn.containsKey(cur)) {
        Range<?> condition = dependsOn.get(cur);
        toCheck.add(new ValuePairUncomparable<JComponentForOption, Range<?>>(jc, condition));
      }
    }
    
    
    // Create a listener that enables and disabled components
    DependencyListener listener  = new DependencyListener(toCheck, (Component) dependant);
    
    // Add action listener to all dependents
    for (ValuePairUncomparable<JComponentForOption, Range<?>> valuePairUncomparable : toCheck) {
      JComponentForOption dependent = valuePairUncomparable.getA();
      // Add all listener interfaces 
      // For JComboBoxes
      if (dependent instanceof ItemSelectable) {
        ((ItemSelectable)dependent).addItemListener(listener);
      }
      // For any textfields and similar
      if (dependent instanceof JTextComponent) {
        ((JTextComponent)dependent).getDocument().addDocumentListener(listener);
      }
      // Others (e.g, for buttons)
      Reflect.invokeIfContains(dependent, "addChangeListener", ChangeListener.class, listener);
      Reflect.invokeIfContains(dependent, "addActionListener", ActionListener.class, listener);
      ((Component)dependent).addKeyListener(listener);
    }
    
    // Perform an initial check
    listener.checkAndProcessConditions();
    
  }
  
  
  
}
