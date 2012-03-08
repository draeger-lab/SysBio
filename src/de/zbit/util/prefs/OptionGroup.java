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
package de.zbit.util.prefs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;

import de.zbit.gui.ActionCommand;

/**
 * Gathers multiple instances of {@link Option} in a group together with a
 * meaningful tool tip and a label.
 * 
 * Remark by wrzodek: By using "? extends T" one can have an OptionGroup of
 * mixed type options.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 * @version $Rev$
 * @since 1.0
 */
public class OptionGroup<T> implements ActionCommand,
    Comparable<OptionGroup<T>>, Iterable<Option<? extends T>> {
  
  /**
   * If this is set to true, this {@link OptionGroup} should
   * be visualized collapsable in GUIs.
   */
  private boolean collapsable = false;
  /**
   * Defines the initial state, if this group is {@link #collapsable}.
   */
  private boolean isCollapsed = false;
  
  /**
   * 
   */
  private String name, toolTip;
  
  /**
   * 
   */
  private List<Option<? extends T>> options;
  
  /**
   * Allows to hide this option group (and subsequently all contained
   * options) in GUIs, help, command-line, etc.
   */
  private boolean visible = true;
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
	public OptionGroup() {
    this(null, (String) null);
  }
  
  /**
   * 
   * @param optionGroupId
   * @param bundle
   *        This {@link ResourceBundle} looks for the optionGroupId as key for a
	 *        human-readable display name. It also looks for the key
	 *        <code>optionGroupId + "_TOOLTIP"</code> in order to obtain a more
	 *        detailed description of this option. If no such description can be
	 *        found, it tries to split the human-readable name connected with the
	 *        optionGroupId using the character ';' (semicolon). If the
	 *        human-readable name contains this symbol it assumes that the part
	 *        before the semicolon is intended to be a short name and everything
	 *        written after it is assumed to be a tooltip.
   * @param option
   */
	public OptionGroup(String optionGroupId, ResourceBundle bundle,
		Option<? extends T>... option) {
		this(bundle.getString(optionGroupId), bundle.getString(optionGroupId),
			option);
		String key = optionGroupId + "_TOOLTIP";
		if (bundle.containsKey(key)) {
			setToolTip(bundle.getString(key));
		} else if (getName().contains(";")) {
			String names[] = getName().split(";");
			setName(names[0]);
			setToolTip(names[1]);
		}
	}
	
	/**
	   * 
	   * @param optionGroupId
	   * @param bundle
	   *        This {@link ResourceBundle} looks for the optionGroupId as key for a
	   *        human-readable display name. It also looks for the key
	   *        <code>optionGroupId + "_TOOLTIP"</code> in order to obtain a more
	   *        detailed description of this option. If no such description can be
	   *        found, it tries to split the human-readable name connected with the
	   *        optionGroupId using the character ';' (semicolon). If the
	   *        human-readable name contains this symbol it assumes that the part
	   *        before the semicolon is intended to be a short name and everything
	   *        written after it is assumed to be a tooltip.
	   * @param asRadioButtons
	   * 		handle all options as radio buttons
	   * @param option
	   */
		public OptionGroup(String optionGroupId, ResourceBundle bundle, boolean asRadioButtons,
			Option<? extends T>... option) {
			this(optionGroupId, bundle, option);
			
			if (asRadioButtons) {
				ButtonGroup group = new ButtonGroup();
				for (Option opt : option) {
					opt.setButtonGroup(group);
				}
			}
		}
  
  /**
   * Creates a (eventually collapsible) {@link OptionGroup}.
   * @param name
   * @param toolTip
   * @param collapsable
   * @param isCollapsed initial state
   * @param option
   */
  public OptionGroup(String name, String toolTip, boolean collapsable, boolean isCollapsed, Option<? extends T>... option) {
    super();
    this.name = name;
    this.toolTip = toolTip;
    this.collapsable=collapsable;
    this.isCollapsed = isCollapsed;
    this.options = new LinkedList<Option<? extends T>>();
    setOptions(option);
  }
  
  /**
   * Creates a (eventually invisible) {@link OptionGroup}.
   * @param name
   * @param toolTip
   * @param visibility see {@link #setVisible(boolean)}
   * @param option
   */
  public OptionGroup(String name, String toolTip, boolean visibility, Option<? extends T>... option) {
    this(name, toolTip, false, false, option);
    setVisible(visibility);
  }
  
  /**
   * 
   * @param name
   * @param toolTip
   * @param option
   */
  public OptionGroup(String name, String toolTip, Option<? extends T>... option) {
    this(name, toolTip, false, false, option);
  }
  
	/**
   * @param option
   * @return
   * @see List#add(Object)
   */
  public boolean add(Option<? extends T> option) {
    return options.add(option);
  }
  
  /**
   * @param options
   * @return
   * @see List#add(Object)
   */
  public boolean addAll(Collection<Option<? extends T>> options) {
    return this.options.addAll(options);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(OptionGroup<T> optionGroup) {
    return Integer.valueOf(getOptions().hashCode()).compareTo(
      Integer.valueOf(optionGroup.getOptions().hashCode()));
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getName()
   */
  public String getName() {
    return isSetName() ? name : getClass().getSimpleName();
  }
  
  /**
   * @return the options
   */
  public List<Option<? extends T>> getOptions() {
    return options;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getToolTip()
   */
  public String getToolTip() {
    return toolTip;
  }
  
  /**
   * @return the collapsable
   */
  public boolean isCollapsable() {
    return collapsable;
  }
  
  /**
   * @return the isCollapsed
   */
  public boolean isInitiallyCollapsed() {
    return isCollapsed;
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetName() {
    return (name != null) && (name.length() > 0);
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetToolTip() {
    return (toolTip != null) && (toolTip.length() > 0);
  }
  
  /**
   * @return visibility state of this option
   */
  public boolean isVisible() {
    return visible;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<Option<? extends T>> iterator() {
    return getOptions().iterator();
  }

  /**
   * 
   * @param option
   * @return
   * @see List#remove(Object)
   */
  public boolean remove(Option<T> option) {
    return options.remove(option);
  }

  /**
   * @param collapsable the collapsable to set
   */
  public void setCollapsable(boolean collapsable) {
    this.collapsable = collapsable;
  }

  /**
   * @param isCollapsed the isCollapsed to set
   */
  public void setInitiallyCollapsed(boolean isCollapsed) {
    this.isCollapsed = isCollapsed;
  }

  /**
   * @param name
   *        the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  
  
  
  /**
   * @param options
   *        the options to set
   */
  public void setOptions(List<Option<? extends T>> options) {
    this.options = options;
  }

  /**
   * 
   * @param option
   */
  public void setOptions(Option<? extends T>... option) {
    options.clear();
    if (option != null) {
      for (Option<? extends T> opt : option) {
        add(opt);
      }
    }
  }

  /**
   * @param toolTip
   *        the toolTip to set
   */
  public void setToolTip(String toolTip) {
    this.toolTip = toolTip;
  }
  
  /**
   * Allows to hide this option group (and subsequently all contained
   * options) in GUIs, help, command-line, etc.
   * @param visible visibility to set
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (isSetName()) {
      sb.append(name);
      sb.append(": ");
    }
    sb.append(options.toString());
    return sb.toString();
  }

  /**
   * Check if any {@link Option} on this group is visible.
   * @return <code>TRUE</code> if all {@link #options} on this
   * group are invisible.
   */
  public boolean isAllOptionsInvisible() {
    return isAllOptionsInvisible(options);
  }
  
  /**
   * Check if any of the given {@link Option}s is visible.
   * 
   * @param <T>
   * @param options
   * @return <code>TRUE</code> if all {@link #options} are invisible.
   */
  public static <T extends Option<?>> boolean isAllOptionsInvisible(Iterable<T> options) {
    for (Option<?> option : options) {
      if (option.isVisible()) {
        return false;
      }
    }
    return true;
  }
  
}
