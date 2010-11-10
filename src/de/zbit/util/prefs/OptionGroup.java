/**
 * 
 */
package de.zbit.util.prefs;

import java.util.LinkedList;
import java.util.List;

import de.zbit.gui.ActionCommand;

/**
 * Gathers multiple instances of {@link Option} in a group together with a
 * meaningful tool tip and a label.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 */
public class OptionGroup<T> implements ActionCommand,
		Comparable<OptionGroup<T>> {
	
	/**
	 * 
	 */
	private String name, toolTip;
	/**
	 * 
	 */
	private List<Option<T>> options;
	
	/**
	 * 
	 */
	public OptionGroup() {
		this(null, null);
	}
	
	/**
	 * 
	 * @param name
	 * @param toolTip
	 * @param option
	 */
	public OptionGroup(String name, String toolTip, Option<T>... option) {
		this.name = name;
		this.toolTip = toolTip;
		this.options = new LinkedList<Option<T>>();
		setOptions(option);
	}
	
	/**
	 * 
	 * @param option
	 * @return
	 * @see List#add(Object)
	 */
	public boolean add(Option<T> option) {
		return options.add(option);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.ActionCommand#getName()
	 */
	public String getName() {
		return isSetName() ? name : getClass().getSimpleName();
	}
	
	/**
	 * @return the options
	 */
	public List<Option<T>> getOptions() {
		return options;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.ActionCommand#getToolTip()
	 */
	public String getToolTip() {
		return toolTip;
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
	 * 
	 * @param option
	 * @return
	 * @see List#remove(Object)
	 */
	public boolean remove(Option<T> option) {
		return options.remove(option);
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
	public void setOptions(List<Option<T>> options) {
		this.options = options;
	}
	
	/**
	 * 
	 * @param option
	 */
	public void setOptions(Option<T>... option) {
		options.clear();
		if (option != null) {
			for (Option<T> opt : option) {
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
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(OptionGroup<T> optionGroup) {
		return Integer.valueOf(getOptions().hashCode()).compareTo(
			Integer.valueOf(optionGroup.getOptions().hashCode()));
	}
	
}
