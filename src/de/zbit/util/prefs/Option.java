/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import static de.zbit.util.Utils.getMessage;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.argparser.ArgHolder;
import org.argparser.ArgParser;

import de.zbit.gui.JLabeledComponent;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.io.filefilter.GeneralFileFilter;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;
import de.zbit.util.objectwrapper.ValuePairUncomparable;

/**
 * An {@link Option} defines a key in a key-provider class and can also be used
 * to specify command-line options for a program.
 * 
 * <p>TODOS:</p>
 * 
 * <p>TODO: Currently the<ul>
 * <li>{@link #dependencies} are not considered in command-line parsing and
 * all help texts.</li>
 * <li>{@link #buttonGroup} is a kind of an XOR dependency for
 * all options on the same ButtonGroup. Does only seem to make
 * sense for boolean options. This should be implemented as XOR
 * dependency in command-line parsing and help texts.</li>
 * <li>{@link #visible} is not yet considered in command-line parsing.
 * Intention is, that this option is still parsed on the command-line
 * but not displayed in the --help description.</li>
 * </ul>
 * 
 * <p>TODO: If {@link Class} is given as option type, test
 * <ul><li>Command line help text AND auto-generated-gui F1 help text
 * for readability.</li>
 * <li>Test if submitting as command-line argument works correctly</li>
 * <li>Test if default value (if no argument) works correctly</li></ul>
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-24
 * @version $Rev$
 * @since 1.0
 */
public class Option<Type> implements ActionCommand, Comparable<Option<Type>>,
Serializable {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(Option.class.getName());
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 1799289265354101320L;
  
  /**
   * Just a convenient wrapper method for {@link Range#Range(Class, List)}.
   * 
   * @param <Type>
   * @param acceptedObjects
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <Type> Range<Type> buildRange(List<Type> acceptedObjects) {
    if ((acceptedObjects == null) || (acceptedObjects.size() < 1)) {
      throw new IllegalArgumentException("Can not create empty range.");
    }
    return new Range<Type>((Class<Type>) acceptedObjects.get(0).getClass(),
        acceptedObjects);
  }
  
  /**
   * Just a convenient wrapper method for {@link Range#Range(Class, List)}.
   * 
   * @param <Type>
   * @param acceptedObjects
   * @return
   */
  public static <Type> Range<Type> buildRange(Type... acceptedObjects) {
    return buildRange(Arrays.asList(acceptedObjects));
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
  @SuppressWarnings("rawtypes")
  // Do NOT add a Type parameter here! see below!
  public static Class getClassFromRange(Option<Class> option, String simpleName) {
    // Please DO NOT add a TYPE PARAMETER to class. Option<Class<?>> can not be
    // initialized with a valid range. Thus, adding a type parameter (even a T or
    // a "?" to this method, renders it useless for many Option<Class>'es!!!
    
    // For absolute class strings (e.g., "class de.zbit.io.mRNAReader").
    try {
      if (simpleName.startsWith("class ")) {
        simpleName = simpleName.substring(6);
      }
      return Class.forName(simpleName);
    } catch (ClassNotFoundException exc) {
      logger.finest(getMessage(exc));
    }
    
    // For simple-name class strings (e.g., "mRNAReader").
    if ((option != null) && (option.getRange() != null)) {
      // Really take the simple name!
      int pos = simpleName.lastIndexOf('.');
      if (pos >= 0) {
        simpleName = simpleName.substring(pos + 1, simpleName.length());
      }
      
      // Search simple name in current range
      List<Class> listOfClasses = option.getRange().getAllAcceptableValues();
      if (listOfClasses != null) {
        for(Class<?> c: listOfClasses) {
          if (c.getSimpleName().equals(simpleName)) {
            return c;
          }
        }
      }
    }
    
    return null;
  }
  
  /**
   * Convert 'ret' to {@link #requiredType} by parsing it (e.g.
   * Integer.parseInt), or casting it to the desired type.
   * 
   * @param <Type> Type
   * @param requiredType Type.class
   * @param ret Object to convert
   * @return Type instance of {@code ret}.
   */
  @SuppressWarnings("unchecked")
  public static <Type> Type parseOrCast(Class<Type> requiredType, Object ret) {
    if (ret == null) {
      return null;
    }
    if (requiredType.isAssignableFrom(ret.getClass())) {
      return requiredType.cast(ret);
    }
    
    if (Reflect.containsParser(requiredType)) {
      try {
        ret = Reflect.invokeParser(requiredType, ret);
      } catch (Throwable exc) {
        // Do NOT set to null, e.g., java.awt.Color contains a
        // decode method for "BLUE" and such, but will fail to decode
        // any Color.toString(). Thus, below is a special parser for
        // Colors, but ret must not be null to function correctly!
        //ret=null;
        logger.finest(getMessage(exc));
      }
    }
    
    // Parse color from string. Alpha is being lost...
    if (requiredType.equals(java.awt.Color.class)) {
      if ((ret != null) && !ret.getClass().equals(java.awt.Color.class) && // May be already decoded
          ret.toString().startsWith("java.awt.Color[r=")) {
        String parse = ret.toString();
        int r = Utils.getNumberFromString(parse.indexOf("r=")+2, parse);
        int g = Utils.getNumberFromString(parse.indexOf("g=")+2, parse);
        int b = Utils.getNumberFromString(parse.indexOf("b=")+2, parse);
        return (Type) new java.awt.Color(r,g,b);
      }
    } else if (requiredType.equals(Character.class)) {
      if ((ret == null) || (ret.toString().length() < 1)) {
        return null;
      }
      ret = (ret.toString().charAt(0));
    } else if (requiredType.equals(File.class)) {
      ret = new File(ret.toString());
    } else if (requiredType.equals(Class.class) && (ret instanceof String)) {
      try {
        ret = Class.forName((String) ret);
      } catch (ClassNotFoundException exc) {
        logger.finest(getMessage(exc));
      }
    } else if (Enum.class.isAssignableFrom(requiredType)) {
      // Empty strings are never contained in enums
      if ((ret == null) || (ret.toString().length() < 1)) {
        return null;
      }
      try {
        ret = Reflect.invokeIfContains(requiredType, "valueOf",
          new Object[] { ret.toString() });
      } catch (Throwable exc) {
        // ret should be, but is not in enum
        logger.finest(getMessage(exc));
        return null;
      }
    } else if (Date.class.isAssignableFrom(requiredType)) {
      try {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US);
        ret = df.parse(ret.toString());
      } catch (ParseException exc) {
        logger.finest(getMessage(exc));
        return null;
      }
    }
    
    try {
      return (Type) ret;
    } catch (Throwable exc) {
      logger.fine(getMessage(exc));
      return null;
    }
  }
  
  
  /**
   * This group allows to create a group of buttons. This does only
   * make sense with {@link Boolean} options. All options on this
   * group will automatically be converted into a {@link JRadioButton},
   * when translated into a JComponent.
   */
  private ButtonGroup buttonGroup = null;
  
  /**
   * The default value for this option. May be null, if it is going to be read
   * from the XML-file later.
   */
  private Type defaultValue;
  /**
   * This allows to configure dependencies for this option. Only if
   * for each entry in the map, the value of the option fulfills
   * the condition, this Option is enabled (e.g., in GUIs).
   */
  private Map<Option<?>, SortedSet<Range<?>>> dependencies = null;
  
  /**
   * A short description what the purpose of this option is.
   */
  private String description;
  
  /**
   * A short human-readable representation of the purpose of this {@link Option}
   * . This {@link String} is intended to be displayed in help texts and
   * graphical user interfaces.
   */
  private String displayName;
  
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
   * Allows to set a visibility for this option. If this is false,
   * the option should be hidden from every GUI, command-line,
   * help, etc.
   */
  private boolean visible = true;
  
  /**
   * Decide whether the value of this option is allowed to be displayed to users,
   * e.g., passwords etc.
   */
  private boolean secret = false;
  
  /**
   * @return the secret
   */
  public boolean isSecret() {
    return secret;
  }
  
  /**
   * @param secret the secret to set
   */
  public void setSecret(boolean secret) {
    this.secret = secret;
  }
  
  /**
   * 
   * @param optionName
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param bundle
   *        This {@link ResourceBundle} looks for the optionName as key for a
   *        human-readable display name. It also looks for the key
   *        {@code optionName + "_TOOLTIP"} in order to obtain a more
   *        detailed description of this option. If no such description can be
   *        found, it tries to split the human-readable name connected with the
   *        optionName using the character ';' (semicolon). If the
   *        human-readable name contains this symbol it assumes that the part
   *        before the semicolon is intended to be a short name and everything
   *        written after it is assumed to be a tooltip.
   * @param defaultValue
   * @param range
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Option(String optionName, Class<Type> requiredType,
    ResourceBundle bundle, Range<Type> range, Type defaultValue) {
    this(optionName, requiredType, bundle, range, defaultValue,
      (ValuePairUncomparable) null);
  }
  
  /**
   * 
   * @param <E>
   * @param optionName
   * @param requriedType Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param bundle This {@link ResourceBundle} looks for the optionName as key for a
   *        human-readable display name. It also looks for the key
   *        {@code optionName + "_TOOLTIP"} in order to obtain a more
   *        detailed description of this option. If no such description can be
   *        found, it tries to split the human-readable name connected with the
   *        optionName using the character ';' (semicolon). If the
   *        human-readable name contains this symbol it assumes that the part
   *        before the semicolon is intended to be a short name and everything
   *        written after it is assumed to be a tooltip.
   * @param defaultValue
   * @param range see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requriedType,
    ResourceBundle bundle, Range<Type> range, Type defaultValue,
    ValuePairUncomparable<Option<E>, Range<E>>... dependencies) {
    this(optionName, requriedType, bundle.getString(optionName), range,
      defaultValue);
    loadDisplayNameAndDescription(optionName, bundle);
    if ((dependencies != null) && (dependencies.length > 0)) {
      this.dependencies = new HashMap<Option<?>, SortedSet<Range<?>>>();
      for (ValuePairUncomparable<Option<E>, Range<E>> pair : dependencies) {
        // this might happen if this method was called with null as last argument.
        if ((pair != null) && pair.isSetA() && pair.isSetB()) {
          addDependency(pair.getA(), pair.getB());
        }
      }
      if (this.dependencies.size() == 0) {
        this.dependencies = null;
      }
    }
  }
  
  /**
   * load the display name and description ("_TOOLTIP" or the string after ";")
   * 
   * @param optionName
   * @param bundle
   */
  private void loadDisplayNameAndDescription(String optionName, ResourceBundle bundle) {
    String key = optionName + "_TOOLTIP";
    if (bundle.containsKey(key)) {
      setDisplayName(bundle.getString(optionName));
      this.description = bundle.getString(key);
    } else if (this.description.contains(";")) {
      String names[] = this.description.split(";");
      this.displayName = names[0];
      this.description = names[1];
    }
  }
  
  /**
   * 
   * @param optionName
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param bundle
   *        This {@link ResourceBundle} looks for the optionName as key for a
   *        human-readable display name. It also looks for the key
   *        {@code optionName + "_TOOLTIP"} in order to obtain a more
   *        detailed description of this option. If no such description can be
   *        found, it tries to split the human-readable name connected with the
   *        optionName using the character ';' (semicolon). If the
   *        human-readable name contains this symbol it assumes that the part
   *        before the semicolon is intended to be a short name and everything
   *        written after it is assumed to be a tooltip.
   * @param defaultValue
   */
  public Option(String optionName, Class<Type> requiredType,
    ResourceBundle bundle, Type defaultValue) {
    this(optionName, requiredType, bundle, null, defaultValue);
  }
  
  /**
   * 
   * @param <E>
   * @param optionName
   * @param requiredType
   * @param bundle
   * @param defaultValue
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    ResourceBundle bundle, Type defaultValue,
    ValuePairUncomparable<Option<E>, Range<E>>... dependencies) {
    this(optionName, requiredType, bundle, null, defaultValue, dependencies);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   */
  public Option(String optionName, Class<Type> requiredType, String description) {
    this(optionName, requiredType, description, null, (Type) null);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        - see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range) {
    this(optionName, requiredType, description, range, (short) 2);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
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
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        - see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param numLeadingMinus
   * @param shortCmdName
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, short numLeadingMinus,
    String shortCmdName) {
    this(optionName, requiredType, description, range, numLeadingMinus, shortCmdName,
      (Type) null);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param numLeadingMinus
   * @param shortCmdName
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, short numLeadingMinus,
    String shortCmdName, Type defaultValue) {
    this(optionName, requiredType, description, range, numLeadingMinus,
      shortCmdName, defaultValue, null);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param numLeadingMinus
   * @param shortCmdName
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  @SuppressWarnings("unchecked")
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, short numLeadingMinus,
    String shortCmdName, Type defaultValue, String displayName) {
    super();
    
    // Ensure that the option name contains no white spaces.
    this.optionName = optionName.replaceAll("\\s", "_");
    
    // If declaring for Enums, always set a Range that accepts only
    // values from this Enum !
    if ((range == null) && Enum.class.isAssignableFrom(requiredType)) {
      range = new Range<Type>(requiredType,
          Range.toRangeString((Class<? extends Enum<?>>) requiredType));
    }
    
    this.requiredType = requiredType;
    this.description = description;
    this.range = range;
    this.shortCmdName = shortCmdName;
    if (requiredType.isAssignableFrom(Class.class) && (defaultValue != null)) {
      this.defaultValue = (Type) ((Class<Type>) defaultValue).getName();
    } else {
      this.defaultValue = defaultValue;
    }
    if (numLeadingMinus < 0) {
      throw new IllegalArgumentException(String.format(
        ResourceManager.getBundle("de.zbit.loales.Warnings").getString(
            "VALUE_MUST_BE_POSITIVE"), "numLeadingMinus"));
    }
    this.numLeadingMinus = numLeadingMinus;
    this.displayName = displayName;
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        - see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param numLeadingMinus
   *        the number of leading '-' symbols of the command-line argument
   *        corresponding to this option.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, short numLeadingMinus,
    Type defaultValue) {
    this(optionName, requiredType, description, range, numLeadingMinus, null,
      defaultValue);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param range
   * @param numLeadingMinus
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, short numLeadingMinus,
    Type defaultValue, String displayName) {
    this(optionName, requiredType, description, range, numLeadingMinus, null,
      defaultValue, displayName);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param bundle This {@link ResourceBundle} looks for the optionName as key for a
   *        human-readable display name. It also looks for the key
   *        {@code optionName + "_TOOLTIP"} in order to obtain a more
   *        detailed description of this option. If no such description can be
   *        found, it tries to split the human-readable name connected with the
   *        optionName using the character ';' (semicolon). If the
   *        human-readable name contains this symbol it assumes that the part
   *        before the semicolon is intended to be a short name and everything
   *        written after it is assumed to be a tooltip.
   * @param range
   * @param numLeadingMinus
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   */
  public Option(String optionName, Class<Type> requiredType,
    ResourceBundle bundle, Range<Type> range, short numLeadingMinus,
    Type defaultValue) {
    this(optionName, requiredType, bundle.getString(optionName), range, numLeadingMinus, null,
      defaultValue, bundle.getString(optionName));
    loadDisplayNameAndDescription(optionName, bundle);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue) {
    this(optionName, requiredType, description, range, (short) 2, defaultValue);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param Range
   *        see {@link Range#Range(Class, String)} or
   *        {@link #buildRange(Class, String)}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param visibility
   *        allows to hide this option from auto-generated
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue,
    boolean visibility) {
    this(optionName, requiredType, description, range, (short) 2, defaultValue);
    setVisible(visibility);
  }
  
  /**
   * 
   * @param <E>
   * @param optionName
   * @param requiredType
   * @param description
   * @param range
   * @param defaultValue
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue,
    Map<Option<?>, SortedSet<Range<?>>> dependencies) {
    this(optionName, requiredType, description, range, defaultValue, null,
      dependencies);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type. This
   * constructor adds a dependency to the created option. The given
   * {@code dependency} must fulfill the given {@code condition} that
   * this option is considered enabled.
   * 
   * @param <E>
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param range
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param dependency
   * @param condition
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue, Option<E> dependency, Range<E> condition) {
    this(optionName, requiredType, description, range, defaultValue);
    addDependency(dependency, condition);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param range
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue, String displayName) {
    this(optionName, requiredType, description, range, (short) 2, defaultValue,
      displayName);
  }
  
  /**
   * 
   * @param <E>
   * @param optionName
   * @param requiredType
   * @param description
   * @param range
   * @param defaultValue
   * @param displayName
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue,
    String displayName, Map<Option<?>, SortedSet<Range<?>>> dependencies) {
    this(optionName, requiredType, description, range, defaultValue,
      displayName);
    this.dependencies = dependencies;
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param range
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param dependency
   * @param condition
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Range<Type> range, Type defaultValue,
    String displayName, Option<E> dependency, Range<E> condition) {
    this(optionName, requiredType, description, range, (short) 2, defaultValue,
      displayName);
    addDependency(dependency, condition);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param numLeadingMinus
   * @param shortCmdName
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, short numLeadingMinus, String shortCmdName) {
    this(optionName, requiredType, description, null, numLeadingMinus,
      shortCmdName);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param numLeadingMinus
   * @param shortCmdName
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, short numLeadingMinus, String shortCmdName,
    Type defaultValue) {
    this(optionName, requiredType, description, null, numLeadingMinus,
      shortCmdName, defaultValue);
  }
  
  /**
   * Same as {@link #Option(String, Class, String, short, String, Object))}, but with a
   * default visibility attribute.
   * @param optionName
   * @param requiredType
   * @param description
   * @param numLeadingMinus
   * @param shortCmdName
   * @param defaultValue
   * @param visibility
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, short numLeadingMinus, String shortCmdName,
    Type defaultValue, boolean visibility) {
    this(optionName, requiredType, description, numLeadingMinus,
      shortCmdName, defaultValue);
    setVisible(visibility);
  }
  
  
  /**
   * 
   * @param <E>
   * @param optionName
   * @param requiredType
   * @param description
   * @param numLeadingMinus
   * @param shortCmdName
   * @param defaultValue
   * @param dependency
   * @param condition
   * @see #Option(String, Class, String, short, String, Object)
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, short numLeadingMinus, String shortCmdName,
    Type defaultValue, Option<E> dependency, Range<E> condition) {
    this(optionName, requiredType, description, numLeadingMinus, shortCmdName,
      defaultValue);
    addDependency(dependency, condition);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue) {
    this(optionName, requiredType, description, null, defaultValue);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param visibility
   *        allows to hide this option from auto-generated GUIs, HELPs,
   *        command-lines, etc.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, boolean visibility) {
    this(optionName, requiredType, description, defaultValue, null, visibility);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type. This
   * constructor adds all given dependencies to the created option. This is
   * especially usefull for setting the dependencies of this option to the same
   * dependencies as other options.
   * 
   * @see #getDependencies()
   * @param <E>
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue,
    Map<Option<?>, SortedSet<Range<?>>> dependencies) {
    this(optionName, requiredType, description, defaultValue, null,
      dependencies);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type. This
   * constructor adds a dependency to the created option. The given
   * {@code dependency} must fulfill the given {@code condition} that
   * this option is considered enabled.
   * 
   * @param <E>
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param dependency
   * @param condition
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, Option<E> dependency,
    Range<E> condition) {
    this(optionName, requiredType, description, defaultValue, (String) null,
      dependency, condition);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName) {
    this(optionName, requiredType, description, null, defaultValue, displayName);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param visibility
   *        allows to hide this option from auto-generated GUIs, HELPs,
   *        command-lines, etc.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName, boolean visibility) {
    this(optionName, requiredType, description, null, defaultValue, displayName);
    setVisible(visibility);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param group
   *        allows to create a group of buttons. This does only make sense with
   *        {@link Boolean} options. All options on this group will
   *        automatically be converted into a {@link JRadioButton}, when
   *        translated into a JComponent!
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName, ButtonGroup group) {
    this(optionName, requiredType, description, null, defaultValue, displayName);
    setButtonGroup(group);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type.
   * 
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param group
   *        allows to create a group of buttons. This does only make sense with
   *        {@link Boolean} options. All options on this group will
   *        automatically be converted into a {@link JRadioButton}, when
   *        translated into a JComponent!
   * @param visibility
   *        allows to hide this option from auto-generated GUIs, HELPs,
   *        command-lines, etc.
   */
  public Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName,
    ButtonGroup group, boolean visibility) {
    this(optionName, requiredType, description, null, defaultValue, displayName);
    setButtonGroup(group);
    setVisible(visibility);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type. This
   * constructor adds all given dependencies to the created option. This is
   * especially useful for setting the dependencies of this option to the same
   * dependencies as other options.
   * 
   * @param <E>
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param dependencies
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName,
    Map<Option<?>, SortedSet<Range<?>>> dependencies) {
    this(optionName, requiredType, description, null, defaultValue,
      displayName, dependencies);
  }
  
  /**
   * Creates a new {@link Option}, that accepts an input of the given Type. This
   * constructor adds a dependency to the created option. The given
   * {@code dependency} must fulfill the given {@code condition} that
   * this option is considered enabled.
   * 
   * @param <E>
   * @param optionName
   *        This {@link String} must be the identical to the name of the
   *        variable that stores this {@link Option}.
   * @param requiredType
   *        Since it is not possible in Java to access the generic type
   *        attribute at run time, each {@link Option} also requires its type
   *        attribute in form of a {@link Class} object.
   * @param description
   *        A human-readable description of this {@link Option}. Note that the
   *        identical description may serve as the explanation of the
   *        corresponding command-line option or as a tool tip within a
   *        graphical user interface. Hence, this text must be expressive enough
   *        to specify the purpose of this {@link Option}, i.e., how it helps
   *        the user to influence the program without explaining details of how
   *        to enter this {@link Option}.
   * @param defaultValue
   *        The value for this {@link Option} to be used in case that there is
   *        no user-defined value at the moment.
   * @param displayName
   *        A better human-readable name to be shown in graphical user
   *        interfaces in order to give a brief description of this
   *        {@link Option}.
   * @param dependency
   * @param condition
   */
  public <E> Option(String optionName, Class<Type> requiredType,
    String description, Type defaultValue, String displayName,
    Option<E> dependency, Range<E> condition) {
    this(optionName, requiredType, description, defaultValue, displayName);
    addDependency(dependency, condition);
  }
  
  /**
   * 
   * @param optionName
   * @param requiredType
   * @param bundle
   * @param range
   */
  public Option(String optionName, Class<Type> requiredType, ResourceBundle bundle,
    Range<Type> range) {
    this(optionName, requiredType, bundle, range, (Type) null);
  }
  
  /**
   * This allows to add dependencies for this option. Only if
   * for all added dependencies, the value of the {@code option}
   * is equal to the {@code condition}, this {@link Option}
   * is enabled (e.g., in GUIs).
   * <p>Remarks:<br/><ul>
   * <li>Only one {@code condition} is allowed for each
   * option.</li>
   * <li>Multiple dependencies are connected with an
   * {@code AND} operator.</li></ul>
   * @param <E>
   * @param option another option, this option depends on
   * @param condition only if this condition is equal to the
   * {@code option}s value, this option is considered enabled.
   */
  @SuppressWarnings("unchecked")
  public <E> void addDependency(Option<E> option, E condition) {
    // Create a range with a single element.
    addDependency(option, new Range<E>((Class<E>)condition.getClass(),
        Arrays.asList(condition)));
  }
  
  /**
   * 
   * @param <E>
   * @param option
   * @param condition
   */
  public <E> void addDependency(Option<E> option, Range<E> condition) {
    if (dependencies == null) {
      dependencies = new HashMap<Option<?>, SortedSet<Range<?>>>();
    }
    if (!dependencies.containsKey(option)) {
      dependencies.put(option, new TreeSet<Range<?>>());
    }
    dependencies.get(option).add(condition);
  }
  
  /**
   * Does nearly the same as {@link Range#castAndCheckIsInRange(Object)}, but
   * has some enhancements, e.g., when using Class as type.
   * 
   * @param value
   * @param props the current properties of all options.
   * @return
   */
  public boolean castAndCheckIsInRange(Object value, SBProperties props) {
    Type value2 = parseOrCast(value);
    if (value2 == null) {
      return false;
    }
    if (!isSetRangeSpecification()) {
      return true;
    }
    return range.isInRange(value2, props);
  }
  
  /**
   * Cast or parse {@code value} to {@code Type}
   * and check with the given range constraints.
   * 
   * @param value
   * @param r
   * @param props the current properties of all options.
   * @return {@code true} if {@code value} is in {@link Range} {@code r}.
   */
  @SuppressWarnings("unchecked")
  public boolean castAndCheckRange(Object value, Range<?> r, SBProperties props) {
    Type value2 = parseOrCast(value);
    if (value2 == null) {
      return false;
    }
    if ((r == null) || (r.getRangeSpecString() == null)) {
      return true;
    }
    // I made the method a bit more generic, expecting a Range<?>
    // instead a Range<Type>. This improves the usability of the method.
    try {
      return ((Range<Type>) r).isInRange(value2, props);
    } catch (Throwable exc) {
      logger.finest(getMessage(exc));
      return false;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
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
      return new ArgHolder<Float>(Float.class);
    } else if (requiredType.equals(Double.class)) {
      return new ArgHolder<Double>(Double.class);
    } else if (requiredType.equals(Short.class)
        || requiredType.equals(Integer.class)) {
      return new ArgHolder<Integer>(Integer.class);
    } else if (requiredType.equals(Long.class)) {
      return new ArgHolder<Long>(Long.class);
    } else if (requiredType.equals(Boolean.class)) {
      return new ArgHolder<Boolean>(Boolean.class);
    } else if (requiredType.equals(Character.class)) {
      return new ArgHolder<Character>(Character.class);
    } else /*if (requiredType.equals(String.class))*/ {
      return new ArgHolder<String>(String.class);
    }
  }
  
  /**
   * Creates and returns a new argument holder for the required type of this
   * {@link Option} with the given object as default value.
   * 
   * @param object
   *        the default value of this {@link Option}
   * @return an argument holder of the required data type with given default
   *         value
   */
  public ArgHolder<?> createArgumentHolder(Object object) {
    String value = object.toString();
    if (requiredType.equals(Float.class)) {
      return new ArgHolder<Float>(Float.valueOf(value));
    } else if (requiredType.equals(Double.class)) {
      return new ArgHolder<Double>(Double.valueOf(value));
    } else if (requiredType.equals(Short.class)
        || requiredType.equals(Integer.class)) {
      return new ArgHolder<Integer>(Integer.valueOf(value));
    } else if (requiredType.equals(Long.class)) {
      return new ArgHolder<Long>(Long.valueOf(value));
    } else if (requiredType.equals(Boolean.class)) {
      return new ArgHolder<Boolean>(Boolean.valueOf(value));
    } else if (requiredType.equals(Character.class)) {
      if (value.length() != 1) {
        throw new IllegalArgumentException(
          "Invalid char symbol " + value);
      }
      return new ArgHolder<Character>(Character.valueOf(value.charAt(0)));
    } else /*if (requiredType.equals(String.class))*/ {
      return new ArgHolder<String>(value);
    }
  }
  
  /* (non-Javadoc)
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
   * @see #setButtonGroup(ButtonGroup)
   * @return the buttonGroup
   */
  public ButtonGroup getButtonGroup() {
    return buttonGroup;
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
   * Remark: Please be careful with this method, as it
   * returns a raw internal data structure.
   * @return the configured dependencies for this option.
   */
  public Map<Option<?>, SortedSet<Range<?>>> getDependencies() {
    if (dependencies == null) {
      dependencies = new HashMap<Option<?>, SortedSet<Range<?>>>();
    }
    return dependencies;
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
  
  /**
   * Returns the display name of this {@link Option}.
   * 
   * @see #displayName
   * @return the displayName
   */
  public final String getDisplayName() {
    return displayName;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getName()
   */
  @Override
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
    //		if (requiredType.equals(Boolean.class)
    //				&& !(Boolean.parseBoolean(defaultValue.toString()) || isSetRangeSpecification())) {
    //			// Special treatment of boolean arguments whose presents only is already sufficient
    //			// to switch some feature on.
    //			sb.append(cmd);
    //		} else {
    // Removed. Explanation see below
    String separators[] = { "=", " ", "" };
    for (int i = 0; i < separators.length; i++) {
      sb.append(cmd);
      sb.append(separators[i]);
      if (i < separators.length - 1) {
        sb.append(',');
      }
    }
    //		}
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
      /* Changed by Wrzodek: since we make thse options persistent, we might
       * fave a default value of false, but a persitent value of true in memory.
       * If we make the option with %v, it is impossible to change this option
       * to be false. Thus, always make an option that expects an argument.
       */
      //		  if (Boolean.parseBoolean(defaultValue.toString()) || isSetRangeSpecification()) {
      //		    sb.append('b');
      //		  } else {
      //		    sb.append('v');
      //		  }
      sb.append('b');
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
  
  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getToolTip()
   */
  @Override
  public String getToolTip() {
    return getDescription();
  }
  
  /**
   * Returns the value for this {@link Option}, which must be contained in the
   * given {@link SBPreferences}.
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
   * Returns the value for this {@link Option}, which must be contained in the
   * given {@link SBProperties}.
   * 
   * @param parentPreferences
   * @return
   */
  public Type getValue(SBProperties parentProperties) {
    // Returns a string.
    Object ret = parentProperties.getProperty(this.toString());
    
    return parseOrCast(requiredType, ret);
  }
  
  /**
   * @return true if and only if this Option depends
   * on other options.
   */
  public boolean hasDependencies() {
    return (dependencies!=null && dependencies.size()>0);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getOptionName().hashCode();
  }
  
  
  /**
   * 
   * @return
   */
  public boolean isSetDefault() {
    return defaultValue != null;
  }
  
  /**
   * @return
   */
  public final boolean isSetDescription() {
    return description != null;
  }
  
  /**
   * Checks if a display name for this option has been set.
   * 
   * @return {@code true} if the display name has been set,
   *         {@code false} otherwise.
   */
  public final boolean isSetDisplayName() {
    return displayName != null;
  }
  
  /**
   * @return
   */
  public boolean isSetRangeSpecification() {
    return (range != null) && (range.getRangeSpecString() != null);
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
   * @return true if this options should be visible to the user
   */
  public boolean isVisible() {
    return visible;
  }
  
  /**
   * @return
   */
  public final boolean optionName() {
    return optionName != null;
  }
  
  /**
   * 
   * @param ret
   * @return
   * @see #parseOrCast(Class, Object)
   */
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public Type parseOrCast(Object ret) {
    if (Class.class.isAssignableFrom(requiredType) && (ret instanceof String)) {
      return (Type) Option.getClassFromRange((Option<Class>) this, ret
        .toString());
    }
    return parseOrCast(requiredType, ret);
  }
  
  /**
   * Remove an option from the list of dependencies.
   * @param <E>
   * @param option
   */
  public <E> void removeDependency(Option<E> option) {
    if (dependencies==null) {
      return;
    }
    dependencies.remove(option);
  }
  
  /**
   * @param group
   *        allows to create a group of buttons. This does only make sense with
   *        {@link Boolean} options. All options on this group will
   *        automatically be converted into a {@link JRadioButton}, when
   *        translated into a JComponent.
   */
  public void setButtonGroup(ButtonGroup group) {
    this.buttonGroup = group;
  }
  
  /**
   * Change the default value for this option. Actually, you should do this only
   * once right at the start of your main class. This possibility has just been
   * added to use the same options with different default values in different
   * projects.
   * 
   * @param def
   */
  public void setDefaultValue(Type def) {
    this.defaultValue=def;
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
   * @param visible allows to change the desired visibility for
   * this option.
   * @see #visible
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
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
  
  /**
   * Returns the {@link #optionName}.
   */
  @Override
  public String toString() {
    return optionName;
  }
}
