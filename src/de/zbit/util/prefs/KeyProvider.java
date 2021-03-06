/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * <p>
 * Base class for all those elements that provide a collection of {@link Option}
 * instances to be used in key-value pairs, e.g., in {@link SBPreferences} or
 * {@link SBProperties}. Note that there must not be more than one class that is
 * derived from {@link KeyProvider} in the same package because the package name
 * is the path to the position where user options are stored in the local
 * configuration.
 * <p>
 * In case that your {@link KeyProvider} contains some instance of
 * {@link ResourceBundle} in form of a static variable, which in turn contains
 * your {@link KeyProvider}'s simple class name (i.e.,
 * {@link Class#getSimpleName()}) as one of its keys, and maybe this simple name
 * plus the suffix "_TOOLTIP", the automatic HTML documentation tool within
 * {@link Tools} can generate a customized head line and a description of your
 * particular {@link KeyProvider}. One example: Your class that implements
 * {@link KeyProvider} is named "MyKeyProvider". Now, there is a public static
 * final variable of type {@link ResourceBundle}, e.g., "myBundle" in
 * "MyKeyProvider". The file, to which "myBundle" points, contains two keys and
 * corresponding descriptions: "MyKeyProvider" (description: a human readable
 * name) and "MyKeyProvider_TOOLTIP" (description: a longer text explaining the
 * purpose of the class).
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 * @version $Rev$
 * @since 1.0
 */
public interface KeyProvider {
  
  /**
   * Stores an element of a certain type together with an index.
   * 
   * @author Andreas Dr&auml;ger
   * @date 2010-11-23
   * @since 1.0
   * @param <T>
   */
  public static class Entry<T> {
    /**
     * 
     */
    private T element;
    /**
     * 
     */
    private int index;
    
    /**
     * 
     */
    public Entry() {
      this(-1, null);
    }
    
    /**
     * 
     * @param index
     * @param element
     */
    public Entry(int index, T element) {
      this.setIndex(index);
      this.setElement(element);
    }
    
    /**
     * @return the element
     */
    public T getElement() {
      return element;
    }
    
    /**
     * @return the index
     */
    public int getIndex() {
      return index;
    }
    
    /**
     * @param element
     *        the element to set
     */
    public void setElement(T element) {
      this.element = element;
    }
    
    /**
     * @param index
     *        the index to set
     */
    public void setIndex(int index) {
      this.index = index;
    }
  }
  
  /**
   * A collection of useful tools for working with {@link KeyProvider}
   * instances.
   * 
   * @author Andreas Dr&auml;ger
   * @date 2010-11-13
   * @since 1.0
   */
  public static class Tools {
    
    /**
     * A {@link Logger} for this class.
     */
    private static final transient Logger logger = Logger.getLogger(Tools.class.getName());
    
    /**
     * 
     * @param keyProvider
     * @param headerRank
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static StringBuilder createDocumantationContent(
      Class<? extends KeyProvider> keyProvider, int headerRank) {
      StringBuilder sb = new StringBuilder();
      ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
      List<OptionGroup> groupList = optionGroupList(keyProvider);
      List<Option> optionList = optionList(keyProvider);
      boolean atLeastOneOptionVisible = false;
      // if there is at least on group for that KeyProvider
      if (groupList.size() > 0) {
        // for each group
        for (OptionGroup<?> group : groupList) {
          // if this group contains options and is visible
          if (group.getOptions().size() > 0) {
            if (group.isAnyOptionVisible()) {
              // add a header with the groups name
              sb.append(createHeadline(headerRank, group.getName()));
              String tooltip = group.getToolTip();
              if ((tooltip != null) && (tooltip.trim().length() > 0)) {
                sb.append("      <p>");
                sb.append(StringUtil.insertLineBreaks(tooltip, 70, "\n      "));
                sb.append("</p>\n");
              }
              // create the options
              atLeastOneOptionVisible |= writeOptionsToHTMLTable(sb, group.getOptions(), optionList);
            }
          }
        }
      }
      if (optionList.size() > 0) {
        if (OptionGroup.isAnyOptionVisible(optionList)) {
          if (groupList.size() > 0) {
            // If at least one option was displayed, add an "Additional options"
            // header. Otherwise, don't add a header at all.
            if (atLeastOneOptionVisible) {
              sb.append(createHeadline(headerRank, bundle.getString("ADDITIONAL_OPTIONS")));
            }
          }
          writeOptionsToHTMLTable(sb, optionList, null);
        }
      }
      return sb;
    }
    
    /**
     * Creates a headline with the given title and the given rank.
     * 
     * @param headerRank
     * @param string
     * @return
     */
    private static Object createHeadline(int headerRank, String string) {
      return String.format("<h%d> %s </h%d>\n", headerRank, string, headerRank);
    }
    
    /**
     * 
     * @return
     */
    private static StringBuilder createDocumantationFooter() {
      return new StringBuilder("  </body>\n</html>\n");
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    public static String createDocumentation(
      Class<? extends KeyProvider> keyProvider) {
      StringBuilder sb = new StringBuilder();
      sb.append(createDocumentationHeader(createTitle(keyProvider)));
      sb.append(createProgramUsage(2, ""));
      sb.append(createDocumantationContent(keyProvider, 2));
      sb.append(createDocumantationFooter());
      return sb.toString();
    }
    
    
    /**
     * Create a LaTeX documentation string.
     * 
     * XXX: This method is work-in-progress!
     * 
     * @param keyProvider
     * @param headerRank whether you want to start with chapter (=0) or section (=1).
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static String createLaTeXDocumentation(
      Class<? extends KeyProvider> keyProvider, int headerRank) {
      
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%s{%s}\n", getLaTeXSection(headerRank), createTitle(keyProvider)));
      //sb.append(createProgramUsage(2, "")); // TODO: LaTeX program usage
      
      ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
      List<OptionGroup> groupList = optionGroupList(keyProvider);
      List<Option> optionList = optionList(keyProvider);
      if (groupList.size() > 0) {
        for (OptionGroup<?> group : groupList) {
          if (group.getOptions().size() > 0) {
            if (group.isAnyOptionVisible()) {
              // a headline
              sb.append(String.format("%s{%s}\n", getLaTeXSection(headerRank+1), group.getName()));
              
              // display all options in an description environment
              sb.append("\\begin{description}\n");
              writeOptionsToLaTeXItems(sb, group.getOptions(), optionList); // TODO: LaTeX method
              sb.append("\\end{description}\n");
            }
          }
        }
      }
      if (optionList.size() > 0) {
        if (OptionGroup.isAnyOptionVisible(optionList)) {
          if (groupList.size() > 0) {
            sb.append(String.format("%s{%s}\n", getLaTeXSection(headerRank+1), bundle.getString("ADDITIONAL_OPTIONS")));
          }
          sb.append("\\begin{description}\n");
          writeOptionsToLaTeXItems(sb, optionList, null);  // TODO: LaTeX method
          sb.append("\\end{description}\n");
        }
      }
      
      return sb.toString();
    }
    
    /**
     * Returns a latex section definition, based on the {@code headerRank}.
     * 
     * @param headerRank 0 will return "chapter", 1 is "section", 2 is "subsection", etc.
     * @return
     */
    private static String getLaTeXSection(int headerRank) {
      if (headerRank<=0) {
        return "\\chapter";
      }
      
      StringBuilder sb = new StringBuilder("\\");
      for (int i=1; i<headerRank; i++) {
        sb.append("sub");
      }
      sb.append("section");
      return sb.toString();
    }
    
    /**
     * 
     * @param headerRank
     * @return
     */
    private static StringBuilder createProgramUsage(int headerRank, String programName) {
      ResourceBundle bundle = ResourceManager
          .getBundle("de.zbit.locales.Labels");
      String explanation = String.format(bundle.getString("STARTS_PROGRAM"),
        programName != null ? programName : "");
      
      if (explanation.endsWith(" .")) {
        explanation = explanation.substring(0, explanation.length() - 2) + '.';
      }
      int indentation = 2;
      StringBuilder sb = new StringBuilder();
      sb.append(createHeadline(headerRank, bundle.getString("PROGRAM_USAGE")));
      sb.append("<table cellspacing=\"1\" cellpadding=\"1\" border=\"0\" width=\"100%\">\n");
      sb.append(createHTMLTableLine(SBPreferences.generateUsageString(), explanation, indentation));
      sb.append(
        createHTMLTableLine("--help, -?",
          String.format(bundle.getString("COMMAND_LINE_HELP"), bundle.getString("OPTIONS")), indentation));
      sb.append("</table>\n\n");
      return sb;
    }
    
    /**
     * Creates a pair of a type writer formatted text with some explanation.
     * 
     * @return
     */
    private static String createHTMLTableLine(String typeWriterText,
      String explanation, int indent) {
      StringBuilder sb = new StringBuilder();
      String indentation1 = StringUtil.fill("", indent, ' ', false);
      String indentation2 = StringUtil.fill("", indent + 2, ' ', false);
      sb.append(indentation1);
      sb.append("<tr>\n");
      sb.append(indentation2);
      sb.append("<td colspan=\"2\" class=\"typewriter-highlighted\"> ");
      sb.append(typeWriterText);
      sb.append(" </td>\n");
      sb.append(indentation1);
      sb.append("</tr>\n");
      sb.append(indentation1);
      sb.append("<tr>\n");
      sb.append(indentation2);
      sb.append("<td width=\"6%\"> </td>\n");
      sb.append(indentation2);
      sb.append("<td> ");
      sb.append(explanation);
      sb.append(" </td>\n");
      sb.append(indentation1);
      sb.append("</tr>\n");
      return sb.toString();
    }
    
    /**
     * 
     * @param keyProviders
     * @return
     */
    public static String createDocumentation(
      Class<? extends KeyProvider>... keyProviders) {
      String appName = System.getProperty("app.name");
      // appName must be set manually and is null if not set.
      return createDocumentation(appName, keyProviders);
    }
    
    /**
     * 
     * @param keyProviders
     * @return
     */
    public static String createDocumentation(String applicationName,
      Class<? extends KeyProvider>... keyProviders) {
      StringBuilder sb = new StringBuilder();
      ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
      String cmdArgs = bundle.getString("COMMAND_LINE_ARGUMENTS");
      if ((applicationName != null) && (applicationName.length() > 0)) {
        sb.append(createDocumentationHeader(StringUtil.concat(applicationName,
          " - ", cmdArgs).toString()));
      } else {
        sb.append(createDocumentationHeader(cmdArgs));
      }
      sb.append('\n');
      sb.append(createProgramUsage(2, applicationName));
      for (Class<? extends KeyProvider> keyProvider : keyProviders) {
        sb.append(String.format("    <h2> %s </h2>\n\n", createTitle(keyProvider)));
        String description = createDescription(keyProvider);
        if ((description != null) && (description.trim().length() > 0)) {
          sb.append("    <p>\n");
          sb.append(description);
          sb.append("    </p>\n\n");
        }
        sb.append(createDocumantationContent(keyProvider, 3));
        sb.append('\n');
      }
      sb.append(createDocumantationFooter());
      return sb.toString();
    }
    
    /**
     * Writes the complete command line documentation for an application into a
     * {@link File}.
     * 
     * @param applicationName
     * @param targetFile
     * @param keyProviders
     * @throws IOException
     */
    public static void createDocumentation(String applicationName,
      File targetFile, Class<? extends KeyProvider>... keyProviders)
          throws IOException {
      BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile));
      createDocumentation(applicationName, bw, keyProviders);
      bw.close();
    }
    
    /**
     * Doesn't close the writer.
     * 
     * @param file
     * @throws IOException
     */
    public static void createDocumentation(String applicationName,
      Writer writer, Class<? extends KeyProvider>... keyProviders)
          throws IOException {
      writer.append(KeyProvider.Tools.createDocumentation(applicationName,
        keyProviders));
    }
    
    /**
     * 
     * @param title
     * @return
     */
    private static StringBuilder createDocumentationHeader(String title) {
      StringBuilder sb = new StringBuilder();
      String lang = Locale.getDefault().getLanguage();
      sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
      sb.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
      sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
      sb.append(lang);
      sb.append("\" lang=\"");
      sb.append(lang);
      sb.append("\">\n");
      sb.append("  <head>\n");
      sb.append("    <style type=\"text/css\">\n      <!--\n");
      sb.append("        .typewriter {\n");
      sb.append("           font-family:'courier new',courier,monospace;\n");
      sb.append("        }\n");
      sb.append("        .typewriter-highlighted {\n");
      sb.append("           font-family:'courier new',courier,monospace;\n");
      sb.append("           font-weight: bold;\n");
      sb.append("           color:#a51e37;\n        }\n      -->\n");
      sb.append("    </style>\n");
      sb.append("    <title> ");
      sb.append(title);
      sb.append(" </title>\n");
      sb.append("  </head>\n");
      sb.append("  <body>\n");
      sb.append("    <h1> ");
      sb.append(title);
      sb.append(" </h1>\n");
      return sb;
    }
    
    /**
     * Creates a human-readable title from the class name of some {@link Class}.
     * 
     * @param clazz
     * @return
     */
    public static String createTitle(Class<?> clazz) {
      String title = clazz.getSimpleName();
      ResourceBundle bundle = findResourceBundleFor(clazz);
      if (bundle.containsKey(title)) {
        return bundle.getString(title);
      }
      StringBuilder headLine = new StringBuilder();
      headLine.append(title.charAt(0));
      char prev, curr;
      for (int i = 1; i < title.length(); i++) {
        prev = title.charAt(i - 1);
        curr = title.charAt(i);
        if (((Character.isLowerCase(prev) && Character.isUpperCase(curr))
            || (Character.isLetter(prev) && Character.isDigit(curr)) || (Character
                .isDigit(prev) && Character.isLetter(curr)))
                || (title.substring(i).startsWith("Option"))) {
          headLine.append(' ');
        }
        if (title.charAt(i) == '_') {
          headLine.append(' ');
        } else {
          headLine.append(title.charAt(i));
        }
      }
      return headLine.toString();
    }
    
    /**
     * 
     * @param clazz
     * @return
     */
    public static ResourceBundle findResourceBundleFor(Class<?> clazz) {
      ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
      // Check if the given class contains an own ResourceBundle:
      for (Field field : clazz.getFields()) {
        try {
          Object b = field.get(clazz);
          if ((b != null) && (b instanceof ResourceBundle)) {
            bundle = (ResourceBundle) b;
            logger.fine("Found ResourceBundle " + bundle.toString());
            break;
          }
        } catch (Throwable exc) {
          // ignore
          logger.finest(getMessage(exc));
        }
      }
      return bundle;
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    public static String createDescription(Class<? extends KeyProvider> clazz) {
      ResourceBundle bundle = findResourceBundleFor(clazz);
      if (bundle != null) {
        String key = clazz.getSimpleName() + "_TOOLTIP";
        return bundle.containsKey(key) ? bundle.getString(key) : null;
      }
      return null;
    }
    
    /**
     * This tries to obtain the next element of the desired {@link Class}.
     * 
     * @param <T>
     *        The type of the desired element
     * @param sourceClass
     *        The {@link KeyProvider} holding the keys
     * @param clazz
     *        The class of the desired element.
     * @param n
     *        The index of the element to get.
     * @return null if no such element exists or the desired element.
     */
    @SuppressWarnings("unchecked")
    public static <T> Entry<T> getField(Class<?> sourceClass, Class<T> clazz, int n) {
      Field fields[] = sourceClass.getFields();
      Object fieldValue;
      logger.fine("Loading class " + sourceClass.getName());
      while (n < fields.length) {
        try {
          logger.fine("Processing field " + fields[n].getName());
          fieldValue = fields[n].get(sourceClass);
          if ((fieldValue != null) && fieldValue.getClass().isAssignableFrom(clazz)) {
            return new Entry<T>(n, (T) fieldValue);
          }
        } catch (Exception exc) {
          logger.log(Level.FINE, getMessage(exc), exc);
        }
        n++;
      }
      return null;
    }
    
    /**
     * @param keyProvider
     * @param optionName
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> T getField(Class<?> keyProvider, String name, Class<T> clazz) {
      try {
        Field field = keyProvider.getField(name);
        if (field != null) {
          Object fieldValue = field.get(keyProvider);
          if (fieldValue.getClass().isAssignableFrom(clazz)) {
            return (T) fieldValue;
          }
        }
      } catch (Exception exc) {
        logger.log(Level.FINER, getMessage(exc), exc);
      }
      return null;
    }
    
    /**
     * Checks the given {@link KeyProvider} for a {@link Field} of the given
     * name and if such a {@link Field} exists, it tries to get the associated
     * value. If this is successful it then checks whether this {@link Field}'s
     * value is an instance of {@link Option}. If so, it will return this
     * {@link Option}. Otherwise null is returned.
     * 
     * @param keyProvider
     *        The {@link KeyProvider} in which an {@link Option} is searched.
     * @param optionName
     *        The name of the desired {@link Option}, i.e., the name of a
     *        {@link Field} variable that is an instance of {@link Option}.
     * @return an instance of {@link Option} with the given name from the given
     *         {@link KeyProvider} class or null if no such {@link Field} exists
     *         in this {@link KeyProvider}.
     */
    public static Option<?> getOption(Class<? extends KeyProvider> keyProvider,
      String optionName) {
      //return getField(keyProvider, optionName, Option.class);
      
      // Wrzodek, 9.6.2011 - Removed the constraint the options must have the same
      // names as the variable containing the options.
      for (Field f : keyProvider.getFields()) {
        Object fieldValue;
        try {
          fieldValue = f.get(keyProvider);
          if (fieldValue!=null && fieldValue.getClass().isAssignableFrom(Option.class)) {
            if (((Option<?>) fieldValue).getOptionName().equals(optionName)) {
              return (Option<?>) fieldValue;
            }
          }
        } catch (Exception e) {
          logger.log(Level.FINE, e.getLocalizedMessage(), e);
        }
      }
      return null;
    }
    
    
    /**
     * 
     * @param keyProvider
     * @param optionGroupName
     * @return
     * @see #getOption(Class, String)
     */
    public static OptionGroup<?> getOptionGroup(
      Class<? extends KeyProvider> keyProvider, String optionGroupName) {
      return getField(keyProvider, optionGroupName, OptionGroup.class);
    }
    
    /**
     * Returns an {@link Iterator} over all {@link Option} instances defined by
     * the given {@link KeyProvider}.
     * 
     * @param keyProvider
     * @return
     */
    public static <T> Iterator<T> iterator(
      final Class<? extends KeyProvider> keyProvider,
      final Class<? extends T> clazz) {
      return new Iterator<T>() {
        
        private int i = -1;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
          try {
            return getField(keyProvider, clazz, i + 1) != null;
          } catch (ArrayIndexOutOfBoundsException exc) {
            return false;
          }
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
          Entry<? extends T> entry = getField(keyProvider, clazz, ++i);
          if (entry == null) {
            i = keyProvider.getFields().length;
            return null;
          }
          i = entry.getIndex();
          return entry.getElement();
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
          throw new IllegalAccessError();
        }
      };
    }
    
    /**
     * 
     * @param <T>
     * @param keyProvider
     * @param clazz
     * @return
     */
    public static <T> List<T> list(Class<? extends KeyProvider> keyProvider,
      Class<T> clazz) {
      List<T> optionList = new LinkedList<T>();
      for (Iterator<T> iterator = iterator(keyProvider, clazz); iterator
          .hasNext();) {
        optionList.add(iterator.next());
      }
      return optionList;
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Iterator<OptionGroup> optionGroupIterator(
      final Class<? extends KeyProvider> keyProvider) {
      return iterator(keyProvider, OptionGroup.class);
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<OptionGroup> optionGroupList(
      Class<? extends KeyProvider> keyProvider) {
      return list(keyProvider, OptionGroup.class);
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Iterator<Option> optionIterator(
      final Class<? extends KeyProvider> keyProvider) {
      return iterator(keyProvider, Option.class);
    }
    
    /**
     * 
     * @param keyProvider
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<Option> optionList(
      Class<? extends KeyProvider> keyProvider) {
      return list(keyProvider, Option.class);
    }
    
    /**
     * Checks whether or not the given {@link KeyProvider} contains a field with
     * the given key name of type {@link Option}.
     * 
     * @param keyProvider
     * @param key
     *        the name of the {@link Option} to check.
     * @return
     */
    public static boolean providesOption(
      Class<? extends KeyProvider> keyProvider, String key) {
      return getField(keyProvider, key, Option.class) != null;
    }
    
    /**
     * TODO: Quick and dirty solution: Just a copy of the HTML function!!! That's
     * terrible! We need a formatter instance and just one method that actually
     * creates the text. The formatter knows what to do when writing LaTeX or HTML...
     * 
     * @param sb
     * @param options
     * @param removeFromHere
     * @return {@code true}, if at least one option is written to the HTML
     *         table, {@code false} otherwise
     */
    @SuppressWarnings("rawtypes")
    private static boolean writeOptionsToLaTeXItems(StringBuilder sb,
      List<?> options,
      List<Option> removeFromHere) {
      boolean atLeastOneOptionVisible = false;
      
      for (Object o : options) {
        if (!(o instanceof Option<?>)) {
          continue;
        }
        Option<?> option = (Option<?>) o;
        // Hide options that should not be visible, i.e., show only visible options.
        if (option.isVisible()) {
          atLeastOneOptionVisible = true;
          sb.append("\\item[");
          String shortName = option.getShortCmdName();
          String requiredType = String.format("<%s>", option.getRequiredType().getSimpleName());
          /*
           * Special treatment of boolean arguments whose presents only is
           * already sufficient to switch some feature on.
           */
          boolean switchOnOnlyOption = option.getRequiredType().equals(
            Boolean.class)
            && !(Boolean.parseBoolean(option.getDefaultValue().toString()) || option
                .isSetRangeSpecification());
          if (shortName != null) {
            sb.append(shortName);
            if (!switchOnOnlyOption) {
              sb.append(requiredType);
            }
            sb.append(", ");
          }
          sb.append(option.toCommandLineOptionKey());
          if (!switchOnOnlyOption) {
            sb.append("[ |=]");
            sb.append(requiredType);
          }
          sb.append("] ");
          sb.append(StringUtil.insertLineBreaks(option.getToolTip(),
            StringUtil.TOOLTIP_LINE_LENGTH, "\n          "));
          Range<?> range = option.getRange();
          ResourceBundle bundle = ResourceManager
              .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
          if (range != null) {
            List<?> list = range.getAllAcceptableValues();
            String value;
            int lineLength = 0;
            if ((list != null) && (list.size() > 0)) {
              sb.append("\n          ");
              sb.append(bundle.getString("ALL_POSSIBLE_VALUES_FOR_TYPE"));
              sb.append(" \\texttt{");
              sb.append(requiredType);
              sb.append("} ");
              sb.append(bundle.getString("ARE"));
              sb.append(":\n          ");
              Object element;
              for (int i = 0; i < list.size(); i++) {
                if ((i > 0) && (list.size() > 2)) {
                  sb.append(',');
                  if (lineLength > StringUtil.TOOLTIP_LINE_LENGTH) {
                    sb.append("\n          ");
                    lineLength = 0;
                  } else {
                    sb.append(' ');
                  }
                }
                if (i == list.size() - 1) {
                  sb.append(' ');
                  sb.append(bundle.getString("AND"));
                  sb.append(' ');
                }
                element = list.get(i);
                if (element instanceof Class<?>) {
                  value = ((Class<?>) element).getName();
                } else {
                  value = element.toString();
                }
                sb.append("\\texttt{");
                sb.append(value);
                sb.append("}");
                lineLength += value.length() + 30;
              }
              sb.append('.');
            } else if ((range.getRangeSpecString() != null)
                && !range.isSetConstraints()) {
              sb.append("\n          ");
              sb.append(String.format(
                bundle.getString("ARGS_MUST_FIT_INTO_RANGE"),
                range.getRangeSpecString()));
            }
          }
          Object defaultValue = option.getDefaultValue();
          if (defaultValue != null) {
            sb.append("\n          ");
            sb.append(bundle.getString("DEFAULT_VALUE"));
            sb.append(": \\texttt{");
            if (defaultValue instanceof Class<?>) {
              sb.append(((Class<?>) defaultValue).getSimpleName());
            } else {
              sb.append(defaultValue);
            }
            sb.append("}");
          }
          sb.append("\n\n");
        }
        if (removeFromHere != null) {
          removeFromHere.remove(option);
        }
      }
      sb.append("\n");
      return atLeastOneOptionVisible;
    }
    
    /**
     * 
     * @param sb
     * @param options
     * @param removeFromHere
     * @return {@code true}, if at least one option is written to the HTML
     *         table, {@code false} otherwise
     */
    @SuppressWarnings("rawtypes")
    private static boolean writeOptionsToHTMLTable(StringBuilder sb,
      List<?> options,
      List<Option> removeFromHere) {
      boolean atLeastOneOptionVisible = false;
      
      sb.append("      <table cellspacing=\"1\" cellpadding=\"1\" border=\"0\">\n");
      for (Object o : options) {
        if (!(o instanceof Option<?>)) {
          continue;
        }
        Option<?> option = (Option<?>) o;
        // Hide options that should not be visible, i.e., show only visible options.
        if (option.isVisible()) {
          atLeastOneOptionVisible = true;
          sb.append("        <tr>\n          ");
          sb.append("<td colspan=\"2\" class=\"typewriter-highlighted\">");
          String shortName = option.getShortCmdName();
          String requiredType = String.format("&#60;%s&#62;", option.getRequiredType().getSimpleName());
          /*
           * Special treatment of boolean arguments whose presents only is
           * already sufficient to switch some feature on.
           */
          boolean switchOnOnlyOption = option.getRequiredType().equals(
            Boolean.class)
            && !(Boolean.parseBoolean(option.getDefaultValue().toString()) || option
                .isSetRangeSpecification());
          if (shortName != null) {
            sb.append(shortName);
            if (!switchOnOnlyOption) {
              sb.append(requiredType);
            }
            sb.append(", ");
          }
          sb.append(option.toCommandLineOptionKey());
          if (!switchOnOnlyOption) {
            sb.append("[ |=]");
            sb.append(requiredType);
          }
          sb.append("</td>\n        ");
          sb.append("</tr>\n        <tr><td width=\"6%\"> </td>\n");
          sb.append("        <td>\n          ");
          sb.append(StringUtil.insertLineBreaks(option.getToolTip(),
            StringUtil.TOOLTIP_LINE_LENGTH, "\n          "));
          Range<?> range = option.getRange();
          ResourceBundle bundle = ResourceManager
              .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
          if (range != null) {
            List<?> list = range.getAllAcceptableValues();
            String value;
            int lineLength = 0;
            if ((list != null) && (list.size() > 0)) {
              sb.append("<br/>\n          ");
              sb.append(bundle.getString("ALL_POSSIBLE_VALUES_FOR_TYPE"));
              sb.append(" <span class=\"typewriter\">");
              sb.append(requiredType);
              sb.append("</span> ");
              sb.append(bundle.getString("ARE"));
              sb.append(":\n          ");
              Object element;
              for (int i = 0; i < list.size(); i++) {
                if ((i > 0) && (list.size() > 2)) {
                  sb.append(',');
                  if (lineLength > StringUtil.TOOLTIP_LINE_LENGTH) {
                    sb.append("\n          ");
                    lineLength = 0;
                  } else {
                    sb.append(' ');
                  }
                }
                if (i == list.size() - 1) {
                  sb.append(' ');
                  sb.append(bundle.getString("AND"));
                  sb.append(' ');
                }
                element = list.get(i);
                if (element instanceof Class<?>) {
                  value = ((Class<?>) element).getName();
                } else {
                  value = element.toString();
                }
                sb.append("<span class=\"typewriter\">");
                sb.append(value);
                sb.append("</span>");
                lineLength += value.length() + 30;
              }
              sb.append('.');
            } else if ((range.getRangeSpecString() != null)
                && !range.isSetConstraints()) {
              sb.append("<br/>\n          ");
              sb.append(String.format(
                bundle.getString("ARGS_MUST_FIT_INTO_RANGE"),
                range.getRangeSpecString()));
            }
          }
          Object defaultValue = option.getDefaultValue();
          if (defaultValue != null) {
            sb.append("<br/>\n          ");
            sb.append(bundle.getString("DEFAULT_VALUE"));
            sb.append(": <span class=\"typewriter\"> ");
            if (defaultValue instanceof Class<?>) {
              sb.append(((Class<?>) defaultValue).getSimpleName());
            } else {
              sb.append(defaultValue);
            }
            sb.append(" </span>");
          }
          sb.append("\n        </td>\n");
          sb.append("      </tr>\n");
        }
        if (removeFromHere != null) {
          removeFromHere.remove(option);
        }
      }
      sb.append("    </table>\n");
      return atLeastOneOptionVisible;
    }
  }
  
}
