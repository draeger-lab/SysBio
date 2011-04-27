/*
 * $Id$
 * $URL$
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
package de.zbit.util.prefs;

import java.io.File;
import java.util.List;

import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.CharHolder;
import argparser.DoubleHolder;
import argparser.FloatHolder;
import argparser.IntHolder;
import argparser.LongHolder;
import argparser.StringHolder;
import de.zbit.gui.ActionCommand;
import de.zbit.io.GeneralFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * An {@link Option} defines a key in a key-provider class and can also be used
 * to specify command-line options for a program.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-24
 * @version $Rev$
 * @since 1.0
 */
public class Option<Type> implements ActionCommand, Comparable<Option<Type>> {
	
	/**
	 * Just a convenient wrapper method for {@link Range#Range(Class, String)},
	 * that catches the exception.
	 * 
	 * @param <Type>
	 * @param requiredType
	 * @param rangeSpec
	 * @return
	 */
	public static <Type> Range<Type> buildRange(Class<Type> requiredType,
		String rangeSpec) {
		return new Range<Type>(requiredType, rangeSpec);
	}

	/**
	 * Just a convenient wrapper method for {@link Range#Range(Class, List)},
   * that catches the exception.
	 * 
	 * @param <Type>
	 * @param requiredType
	 * @param acceptedObjects
	 * @return
	 */
	public static <Type> Range<Type> buildRange(Class<Type> requiredType,
	          List<Type> acceptedObjects) {
	  return new Range<Type>(requiredType, acceptedObjects);
	}
		
	/**
	 * Convert 'ret' to {@link #requiredType} by parsing it (e.g.
	 * Integer.parseInt), or casting it to the desired type.
	 * 
	 * @param <Type>
	 *        - Type
	 * @param requiredType
	 *        - Type.class
	 * @param ret
	 *        - Object to convert
	 * @return Type instance of ret.
	 */
	@SuppressWarnings("unchecked")
	protected static <Type> Type parseOrCast(Class<Type> requiredType, Object ret) {
		if (ret == null) { return null; }
		
		if (requiredType.isAssignableFrom(ret.getClass())) {
			return requiredType.cast(ret); 
		}
		
		if (Reflect.containsParser(requiredType)) {
			ret = Reflect.invokeParser(requiredType, ret);
		}
		
		if (requiredType.equals(Character.class)) {
			ret = ((Character) ret.toString().charAt(0));
		}
		
		if (requiredType.equals(File.class)) {
			ret = new File(ret.toString());
		}
		
		if (Enum.class.isAssignableFrom(requiredType)) {
			ret = Reflect.invokeIfContains(requiredType, "valueOf", new Object[]{ret.toString()});
		}
		
		try {
			return (Type) ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * The default value for this option. May be null, if it is going to be read
	 * from the XML-file later.
	 */
	private Type defaultValue;
	
	/**
	 * A short description what the purpose of this option is.
	 */
	private final String description;
	/**
	 * Gives the number of leading '-' symbols when converting this {@link Option}
	 * instance's name into a command line key.
	 */
	private final short numLeadingMinus;
	
	/**
	 * The name of this option.
	 */
	private final String optionName;
	
	/**
	 * An optional range specification string to limit the allowable values when
	 * using this {@link Option} on the command line.
	 */
	private final Range<Type> range;
	
	/**
	 * The data type that is expected as an associated value in a key-value pair
	 * of this {@link Option}'s name and a value. For instance, Boolean, Integer,
	 * String etc.
	 */
	private final Class<Type> requiredType;
	
	/**
	 * A shorter name for the command line, for instance, in addition to --file
	 * one might want the option -f.
	 */
	private final String shortCmdName;
	
	/**
	 * A short human-readable representation of the purpose of this {@link Option}
	 * . This {@link String} is intended to be displayed in help texts and
	 * graphical user interfaces.
	 */
	private String displayName;
	
	/**
	 * Checks if a display name for this option has been set.
	 * 
	 * @return <code>true</code> if the display name has been set,
	 *         <code>false</code> otherwise.
	 */
	public final boolean isSetDisplayName() {
		return displayName != null;
	}
	
	/**
	 * @return the displayName
	 */
	public final String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public final void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @param optionName
	 * @param requiredType
	 * @param description
	 */
	public Option(String optionName, Class<Type> requiredType, String description) {
		this(optionName, requiredType, description, null, (Type) null);
	}
	
	/**
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range) {
		this(optionName, requiredType, description, range, (short) 2);
	}
	
	/**
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param numLeadingMinus
	 *        the number of leading '-' symbols of the command-line argument
	 *        corresponding to this option.
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus) {
		this(optionName, requiredType, description, range, numLeadingMinus, null,
			(Type) null);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param numLeadingMinus
	 * @param shortCmdName
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus,
		String shortCmdName) {
		this(optionName, requiredType, description, range, numLeadingMinus, null,
			(Type) null);
	}

	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param numLeadingMinus
	 * @param shortCmdName
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus,
		String shortCmdName, Type defaultValue) {
		this(optionName, requiredType, description, range, numLeadingMinus,
			shortCmdName, defaultValue, null);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param numLeadingMinus
	 * @param shortCmdName
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus,
		String shortCmdName, Type defaultValue, String displayName) {
		this.optionName = optionName;
		this.requiredType = requiredType;
		this.description = description;
		this.range = range;
		this.shortCmdName = shortCmdName;
		this.defaultValue = defaultValue;
		if (numLeadingMinus < 0) { 
			throw new IllegalArgumentException(String
				.format(ResourceManager.getBundle("de.zbit.loales.Warnings").getString(
					"VALUE_MUST_BE_POSITIVE"), "numLeadingMinus")); 
		}
		this.numLeadingMinus = numLeadingMinus;
		this.displayName = displayName;
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param numLeadingMinus
	 *        the number of leading '-' symbols of the command-line argument
	 *        corresponding to this option.
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus,
		Type defaultValue) {
		this(optionName, requiredType, description, range, numLeadingMinus, null,
			defaultValue);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param range
	 * @param numLeadingMinus
	 * @param defaultValue
	 * @param displayName
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, short numLeadingMinus,
		Type defaultValue, String displayName) {
		this(optionName, requiredType, description, range, numLeadingMinus, null,
			defaultValue, displayName);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param Range
	 *        - see {@link Range#Range(Class, String)} or
	 *        {@link #buildRange(Class, String)}.
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, Type defaultValue) {
		this(optionName, requiredType, description, range, (short) 2, defaultValue);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param range
	 * @param defaultValue
	 * @param displayName
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Range<Type> range, Type defaultValue, String displayName) {
		this(optionName, requiredType, description, range, (short) 2, defaultValue,
			displayName);
	}
	
	/**
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param numLeadingMinus
	 * @param shortCmdName
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, short numLeadingMinus, String shortCmdName) {
		this(optionName, requiredType, description, null, numLeadingMinus,
			shortCmdName);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param numLeadingMinus
	 * @param shortCmdName
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, short numLeadingMinus, String shortCmdName,
		Type defaultValue) {
		this(optionName, requiredType, description, null, numLeadingMinus,
			shortCmdName, defaultValue);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param defaultValue
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Type defaultValue) {
		this(optionName, requiredType, description, null, defaultValue);
	}
	
	/**
	 * 
	 * @param optionName
	 * @param requiredType
	 * @param description
	 * @param defaultValue
	 * @param displayName
	 */
	public Option(String optionName, Class<Type> requiredType,
		String description, Type defaultValue, String displayName) {
		this(optionName, requiredType, description, null, defaultValue, displayName);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Option<Type> option) {
		return toString().compareTo(option.toString());
	}
	
	/**
	 * @return
	 * @see #createArgumentHolder(Object)
	 */
	public Object createArgumentHolder() {
		if (requiredType.equals(Float.class)) {
			return new FloatHolder();
		} else if (requiredType.equals(Double.class)) {
			return new DoubleHolder();
		} else if (requiredType.equals(Short.class)) {
			return new IntHolder();
		} else if (requiredType.equals(Integer.class)) {
			return new IntHolder();
		} else if (requiredType.equals(Long.class)) {
			return new LongHolder();
		} else if (requiredType.equals(Boolean.class)) {
			return new BooleanHolder();
		} else if (requiredType.equals(Character.class)) {
			return new CharHolder();
		} else if (requiredType.equals(String.class)) {
			return new StringHolder();
		} else {
			return new StringHolder();
		}
	}
	
	/**
	 * @param defaultValue
	 * @return
	 */
	public Object createArgumentHolder(Object defaultValue) {
		String value = defaultValue.toString();
		if (requiredType.equals(Float.class)) {
			return new FloatHolder(Float.parseFloat(value));
		} else if (requiredType.equals(Double.class)) {
			return new DoubleHolder(Double.parseDouble(value));
		} else if (requiredType.equals(Short.class)) {
			return new IntHolder(Short.parseShort(value));
		} else if (requiredType.equals(Integer.class)) {
			return new IntHolder(Integer.parseInt(value));
		} else if (requiredType.equals(Long.class)) {
			return new LongHolder(Long.parseLong(value));
		} else if (requiredType.equals(Boolean.class)) {
			return new BooleanHolder(Boolean.parseBoolean(value));
		} else if (requiredType.equals(Character.class)) {
			if (value.length() != 1) { throw new IllegalArgumentException(
				"Invalid char symbol " + value); }
			return new CharHolder(value.charAt(0));
		} else if (requiredType.equals(String.class)) {
			return new StringHolder(value);
		} else {
			return new StringHolder(defaultValue.toString());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return o.toString().equals(toString());
	}
	
	/**
	 * @see StringUtil#formatOptionName(String)
	 * @return
	 */
	public String formatOptionName() {
		return StringUtil.formatOptionName(getOptionName());
	}
	
	/**
	 * The default value for this option. If it is null, the cfg packet tries to
	 * read it from an config.xml.
	 * 
	 * If this fails, an exception is thrown.
	 * 
	 * @return
	 */
	public Type getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Change the default value for this option. Actually, you should do this only
	 * once right at the start of your main class. This possibility has just been
	 * added to use the same options with different default values in different
	 * projects.
	 * @param def
	 */
	public void setDefaultValue(Type def) {
	  this.defaultValue=def;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		if (isSetRangeSpecification() && getRange().isSetConstraints()
				&& (getRange().getConstraints() instanceof GeneralFileFilter)) { 
        return StringUtil.concat(description," ",
					ResourceManager.getBundle("de.zbit.locales.Labels").getString(
						"ACCEPTS")," ",
					StringUtil.changeFirstLetterCase(((GeneralFileFilter) getRange()
							.getConstraints()).getDescription(), false, false), ".").toString(); 
		}
		return description;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.ActionCommand#getName()
	 */
	public String getName() {
		return isSetDisplayName() ? getDisplayName() : getOptionName();
	}
	
	/**
	 * @return the numLeadingMinus
	 */
	public short getNumLeadingMinus() {
		return numLeadingMinus;
	}
	
	/**
	 * @return this {@link Option}'s name
	 */
	public String getOptionName() {
		return optionName;
	}
	
	/**
	 * @return the range, or null if no range is set.
	 */
	public Range<Type> getRange() {
		return range;
	}
	
	/**
	 * Returns the Range specification String for the {@link #range}. More
	 * specific: {@link Range#getRangeSpecString()} is returned.
	 * 
	 * @return
	 */
	public String getRangeSpecification() {
		return range == null ? null : range.getRangeSpecString();
	}
	
	/**
	 * @return the type
	 */
	public Class<Type> getRequiredType() {
		return requiredType;
	}
	
	/**
	 * @return the shortCmdName
	 */
	public String getShortCmdName() {
		return shortCmdName;
	}
	
	/**
	 * A {@link String} to be parsed by an {@link ArgParser} to specify the
	 * command line option corresponding to this {@link Option}. If a short
	 * version of this option is set, this will have to be used without any
	 * separators between the short option name and the command line value. For
	 * instance, if the short option's name is '-v', the {@link ArgParser} will
	 * require key value pairs such as '-vMyValue'. For the long option name,
	 * multiple versions are generated to separate the option's name from the
	 * value.
	 * 
	 * @return
	 */
	public String getSpecification() {
		StringBuilder sb = new StringBuilder();
		if (isSetShortCmdName()) {
			sb.append(shortCmdName);
			sb.append(',');
		}
		String cmd = toCommandLineOptionKey();
		String separators[] = { "=", " ", "" };
		for (int i = 0; i < separators.length; i++) {
			sb.append(cmd);
			sb.append(separators[i]);
			if (i < separators.length - 1) {
				sb.append(',');
			}
		}
		sb.append('%');
		if (requiredType.equals(Float.class)) {
			sb.append('f');
		} else if (requiredType.equals(Double.class)) {
			sb.append('f');
		} else if (requiredType.equals(Short.class)) {
			sb.append('d');
		} else if (requiredType.equals(Integer.class)) {
			sb.append('d');
		} else if (requiredType.equals(Long.class)) {
			sb.append('d');
		} else if (requiredType.equals(Boolean.class)) {
		  /* special handling for boolean parameters, because ArgParser has two ways
		   * of handling this:
		   * 1) if the default value is "true" or a range was set, it expects the
		   *    option plus "true" or "false" (%b) 
		   * 2) otherwise it only uses the presence or absence of the option (%v)
		   */
		  if (Boolean.parseBoolean(defaultValue.toString()) || isSetRangeSpecification()) {
		    sb.append('b');
		  } else {
		    sb.append('v');
		  }
		} else if (requiredType.equals(Character.class)) {
			sb.append('c');
		} else if (requiredType.equals(String.class)) {
			sb.append('s');
		} else {
			// some other Object
			sb.append('s');
		}
		if (isSetRangeSpecification() && !requiredType.equals(Boolean.class)) {
			sb.append(' ');
			sb.append(range.getRangeSpecString());
		}
		sb.append(" #");
		sb.append(getDescription());
		return sb.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.ActionCommand#getToolTip()
	 */
	public String getToolTip() {
		return getDescription();
	}
	
	/**
	 * Returns the value for this Option, which must be contained in the given
	 * SBPreferences.
	 * 
	 * @param parentPreferences
	 * @return
	 */
	public Type getValue(SBPreferences parentPreferences) {
		// Returns a string.
		Object ret = parentPreferences.get(this.toString());
		
		return parseOrCast(requiredType, ret);
	}
	
	/**
	 * Returns the value for this Option, which must be contained in the given
	 * SBProperties.
	 * 
	 * @param parentPreferences
	 * @return
	 */
	public Type getValue(SBProperties parentProperties) {
		// Returns a string.
		Object ret = parentProperties.getProperty(this.toString());
		
		return parseOrCast(requiredType, ret);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getOptionName().hashCode();
	}
	
	/**
	 * @return
	 */
	public final boolean isSetDescription() {
		return description != null;
	}
	
	/**
	 * @return
	 */
	public boolean isSetRangeSpecification() {
		return range != null && range.getRangeSpecString() != null;
	}
	
	/**
	 * @return
	 */
	public final boolean isSetRequiredType() {
		return requiredType != null;
	}
	
	/**
	 * 
	 */
	public final boolean isSetShortCmdName() {
		return shortCmdName != null;
	}
	
	/**
	 * @return
	 */
	public final boolean optionName() {
		return optionName != null;
	}
	
	/**
	 * See {@link #parseOrCast(Class, Object)}.
	 * 
	 * @param ret
	 * @return
	 */
	public Type parseOrCast(Object ret) {
		return parseOrCast(requiredType, ret);
	}
	
	/**
	 * This creates a command-line argument name from this {@link Option}'s name
	 * by adding {@link #numLeadingMinus} '-' symbols, converting the name to
	 * lower case and by replacing all underscores by '-' symbols.
	 * 
	 * @return
	 */
	public String toCommandLineOptionKey() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numLeadingMinus; i++) {
			sb.append('-');
		}
		sb.append(optionName.toLowerCase().replace('_', '-'));
		return sb.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return optionName;
	}
}
