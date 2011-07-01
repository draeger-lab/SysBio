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

import de.zbit.gui.ActionCommand;
import de.zbit.gui.JLabeledComponent;
import de.zbit.io.GeneralFileFilter;
import de.zbit.io.SBFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;
import de.zbit.util.argparser.ArgHolder;
import de.zbit.util.argparser.ArgParser;
import de.zbit.util.argparser.BooleanHolder;
import de.zbit.util.argparser.CharHolder;
import de.zbit.util.argparser.DoubleHolder;
import de.zbit.util.argparser.FloatHolder;
import de.zbit.util.argparser.IntHolder;
import de.zbit.util.argparser.LongHolder;
import de.zbit.util.argparser.StringHolder;

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
	public static <Type> Type parseOrCast(Class<Type> requiredType, Object ret) {
		if (ret == null) { return null; }
		
		if (requiredType.isAssignableFrom(ret.getClass())) {
			return requiredType.cast(ret); 
		}
		
		if (Reflect.containsParser(requiredType)) {
		  try {
			  ret = Reflect.invokeParser(requiredType, ret);
		  } catch (Throwable e) {
		    // Do NOT set to null, e.g., java.awt.Color contains a
		    // decode method for "BLUE" and such, but will fail to decode
		    // any Color.toString(). Thus, below is a special parser for
		    // Colors, but ret must not be null to function correctly!
		    //ret=null;
		  }
		}
		
		// Parse color from string. Alpha is being lost...
    if (requiredType.equals(java.awt.Color.class)) {
      if (ret!=null && !ret.getClass().equals(java.awt.Color.class) && // May be already decoded
          ret.toString().startsWith("java.awt.Color[r=")) {
        String parse = ret.toString();
        int r = Utils.getNumberFromString(parse.indexOf("r=")+2, parse);
        int g = Utils.getNumberFromString(parse.indexOf("g=")+2, parse);
        int b = Utils.getNumberFromString(parse.indexOf("b=")+2, parse);
        return (Type) new java.awt.Color(r,g,b);
      }
	  }
	
		if (requiredType.equals(Character.class)) {
			ret = ((Character) ret.toString().charAt(0));
		}
		
		if (requiredType.equals(File.class)) {
			ret = new File(ret.toString());
		}
		
    if (requiredType.equals(Class.class) && (ret instanceof String)) {
      try {
        ret = Class.forName((String) ret);
      } catch (ClassNotFoundException e) {}
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
	 * This method is special for options of type {@link Class}. It allows to
	 * get the real class from the {@link #range}, by comparing the
	 * {@link Class#getSimpleName()} with the given simple name.
	 * <p>Since {@link JLabeledComponent}s and command-line arguments are string
	 * based, this method is the easiest was to get to the class, represented
	 * by a string.
	 * @param option an option of type {@link Class}, with a restricted {@link #range}.
	 * @param simpleName the {@link Class#getSimpleName()} of the class to
	 * return from the {@link #range}
	 * @return the class for the given simpleName
	 */
  @SuppressWarnings("unchecked")
  public static Class getClassFromRange(Option<Class> option, String simpleName) {
    // For absolute class strings (e.g., "class de.zbit.io.mRNAReader").
    try {
      if (simpleName.startsWith("class ")) simpleName = simpleName.substring(6);
      return Class.forName(simpleName);
    } catch (ClassNotFoundException e) {}
    
    // For simple-name class strings (e.g., "mRNAReader").
    if (option!=null && option.getRange()!=null) {
	    
	    List<Class> l = option.getRange().getAllAcceptableValues();
	    if (l!=null) {
	      for(Class c: l) {
	        if (c.getSimpleName().equals(simpleName)) {
	          return c;
	        }
	      }
	    }
	  }
	  
	  return null;
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
	 * Returns the display name of this {@link Option}.
	 * 
	 * @see #displayName
	 * @return the displayName
	 */
	public final String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name of this {@link Option}.
	 * 
   * @see #displayName
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
		
	  // Ensure that the option name contains no whitespaces.
	  this.optionName = optionName.replaceAll("\\s", "_");
		
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
	 * Creates and returns a new argument holder for the required type of this
	 * {@link Option}.
	 * 
	 * @return an argument holder of the required data type
	 * @see #createArgumentHolder(Object)
	 */
	public ArgHolder<?> createArgumentHolder() {
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
   * Creates and returns a new argument holder for the required type of this
   * {@link Option} with the given object as default value.
	 * 
	 * @param object the default value of this {@link Option}
	 * @return an argument holder of the required data type with given default
	 *         value
	 */
	public ArgHolder<?> createArgumentHolder(Object object) {
		String value = object.toString();
		if (requiredType.equals(Float.class)) {
			return new FloatHolder(Float.valueOf(value));
		} else if (requiredType.equals(Double.class)) {
			return new DoubleHolder(Double.valueOf(value));
		} else if (requiredType.equals(Short.class)) {
			return new IntHolder(Integer.valueOf(value));
		} else if (requiredType.equals(Integer.class)) {
			return new IntHolder(Integer.valueOf(value));
		} else if (requiredType.equals(Long.class)) {
			return new LongHolder(Long.valueOf(value));
		} else if (requiredType.equals(Boolean.class)) {
			return new BooleanHolder(Boolean.valueOf(value));
		} else if (requiredType.equals(Character.class)) {
			if (value.length() != 1) { 
				throw new IllegalArgumentException(
				"Invalid char symbol " + value); 
			}
			return new CharHolder(Character.valueOf(value.charAt(0)));
		} else if (requiredType.equals(String.class)) {
			return new StringHolder(value);
		} else {
			return new StringHolder(value);
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
	 * Returns a description for this {@link Option}. If the {@link Range} of this
	 * {@link Option} is a {@link File} with a {@link GeneralFileFilter}
	 * constraint, the description of the file filter is appended.
	 * 
	 * @see #description
	 * @return the description
	 */
	public String getDescription() {
		if (isSetRangeSpecification() && getRange().isSetConstraints()
				&& (getRange().getConstraints() instanceof GeneralFileFilter)) {
		  GeneralFileFilter gf = (GeneralFileFilter) getRange().getConstraints();
		  // Get a description for the option with correct case
		  // (in the middle of a sentence => lowercase, but keep uppercase if
		  // description is, e.g., "SBML")
		  String desc;
		  if (gf instanceof SBFileFilter) {
		    desc = ((SBFileFilter)gf).getDescription(true);
		  } else {
		    desc = gf.getDescription();
		  }
		  
		  return StringUtil.concat(description," ",
		    ResourceManager.getBundle("de.zbit.locales.Labels").getString(
		    "ACCEPTS")," ", desc, ".").toString(); 
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
		if (requiredType.equals(Boolean.class)
				&& !(Boolean.parseBoolean(defaultValue.toString()) || isSetRangeSpecification())) {
			// Special treatment of boolean arguments whose presents only is already sufficient
			// to switch some feature on.
			sb.append(cmd);
		} else {
			String separators[] = { "=", " ", "" };
			for (int i = 0; i < separators.length; i++) {
				sb.append(cmd);
				sb.append(separators[i]);
				if (i < separators.length - 1) {
					sb.append(',');
				}
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
	@SuppressWarnings("unchecked")
  public Type parseOrCast(Object ret) {
	  if (Class.class.isAssignableFrom(requiredType) &&
	      (ret instanceof String) ) {
	    return (Type) Option.getClassFromRange((Option<Class>)this, ret.toString());
	  }
		return parseOrCast(requiredType, ret);
	}
	
	/**
	 * Does nearly the same as {@link Range#castAndCheckIsInRange(Object)},
	 * but has some enhancements, e.g., when using Class as type.
	 * @param value
	 * @return
	 */
  public boolean castAndCheckIsInRange(Object value) {
    Type value2 = parseOrCast(value);
    if (value2==null) { return false;}
    if (!isSetRangeSpecification()) return true;
    return range.isInRange(value2);
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
