package de.zbit.util.prefs;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 */
public interface KeyProvider {
	
	/**
	 * Stores an element of a certain type together with an index.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-11-23
	 * 
	 * @param <T>
	 */
	public static class Entry<T> {
		/**
		 * 
		 */
		private int index;
		/**
		 * 
		 */
		private T element;
		
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
		 * @param index
		 *        the index to set
		 */
		public void setIndex(int index) {
			this.index = index;
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
		 * @return the element
		 */
		public T getElement() {
			return element;
		}
	}
	
	/**
	 * A collection of useful tools for working with {@link KeyProvider}
	 * instances.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-11-13
	 */
	public static class Tools {
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static String createDocumentation(
			Class<? extends KeyProvider> keyProvider) {
			StringBuilder sb = new StringBuilder();
			String title = createTitle(keyProvider);
			
			sb.append("<!DOCTYPE html");
			sb.append(" PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
			sb
					.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
			sb
					.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
			sb.append("  <head>\n");
			sb.append("    <style type=\"text/css\">\n      <!--\n");
			sb.append("        .typewriter {\n");
			sb.append("           font-family:'courier new',courier,monospace;\n");
			sb.append("        }\n");
			sb.append("        .typewriter-blue {\n");
			sb.append("           font-family:'courier new',courier,monospace;\n");
			sb.append("           color:#0000C0;\n        }\n      -->\n");
			sb.append("    </style>\n");
			sb.append("    <title> ");
			sb.append(title);
			sb.append(" </title>\n");
			sb.append("  </head>\n");
			sb.append("  <body>\n");
			sb.append("    <h1> ");
			sb.append(title);
			sb.append(" </h1>\n");
			
			List<OptionGroup> groupList = optionGroupList(keyProvider);
			List<Option> optionList = optionList(keyProvider);
			if (groupList.size() > 0) {
				for (OptionGroup<?> group : groupList) {
					sb.append("    <h2> ");
					sb.append(group.getName());
					sb.append(" </h2>\n      <p>");
					sb.append(StringUtil.insertLineBreaks(group.getToolTip(), 70,
						"\n      "));
					sb.append("</p>\n");
					writeOptionsToHTMLTable(sb, group.getOptions(), optionList);
				}
			}
			if (optionList.size() > 0) {
				sb.append("    <h2> Additional options </h2>\n");
				writeOptionsToHTMLTable(sb, optionList, null);
			}
			
			sb.append("  </body>\n");
			sb.append("</html>\n");
			
			return sb.toString();
		}
		
		/**
		 * Creates a human-readable title from the class name of some {@link Class}.
		 * 
		 * @param clazz
		 * @return
		 */
		public static String createTitle(Class<?> clazz) {
			String title = clazz.getSimpleName();
			StringBuilder headLine = new StringBuilder();
			headLine.append(title.charAt(0));
			for (int i = 1; i < title.length(); i++) {
				if ((Character.isLowerCase(title.charAt(i - 1)) && Character
						.isUpperCase(title.charAt(i)))
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
		 * @param sb
		 * @param options
		 * @param removeFromHere
		 */
		@SuppressWarnings("unchecked")
		private static void writeOptionsToHTMLTable(StringBuilder sb,
			List<?> options, List<Option> removeFromHere) {
			sb
					.append("      <table cellspacing=\"1\" cellpadding=\"1\" border=\"0\">\n");
			for (Object o : options) {
				if (!(o instanceof Object)) {
					continue;
				}
				Option<?> option = (Option<?>) o;
				sb.append("        <tr>\n");
				sb.append("          <td colspan=\"2\" class=\"typewriter-blue\">");
				String shortName = option.getShortCmdName();
				String requiredType = StringUtil.concat("&#60;",
					option.getRequiredType().getSimpleName(), "&#62;").toString();
				if (shortName != null) {
					sb.append(shortName);
					sb.append(requiredType);
					sb.append(", ");
				}
				sb.append(option.toCommandLineOptionKey());
				sb.append("[ |=]");
				sb.append(requiredType);
				sb.append("</td>\n");
				sb.append("        </tr>\n        <tr><td width=\"6%\"> </td>\n");
				sb.append("        <td>\n          ");
				sb.append(StringUtil.insertLineBreaks(option.getToolTip(), 60,
					"\n          "));
				Range range = option.getRange();
				if (range != null) {
					List<?> list = range.getAllAcceptableValues();
					String value;
					int lineLength = 0;
					if ((list != null) && (list.size() > 0)) {
						sb.append("<br>\n          All possible values for type ");
						sb.append("<span class=typewriter>");
						sb.append(requiredType);
						sb.append("</span> are:\n          ");
						for (int i = 0; i < list.size(); i++) {
							if ((i > 0) && (list.size() > 2)) {
								sb.append(',');
								if (lineLength > 60) {
									sb.append("\n          ");
									lineLength = 0;
								} else {
									sb.append(' ');
								}
							}
							if (i == list.size() - 1) {
								sb.append("and ");
							}
							value = list.get(i).toString();
							sb.append("<span class=typewriter>");
							sb.append(value);
							sb.append("</span>");
							lineLength += value.length() + 30;
						}
						sb.append('.');
					} else if ((range.getRangeSpecString() != null)
							&& !range.isSetConstraints()) {
						sb.append("<br>\n          Arguments must fit into the range ");
						sb.append(range.getRangeSpecString());
						sb.append('.');
					}
				}
				Object defaultValue = option.getDefaultValue();
				if (defaultValue != null) {
					sb.append("<br>\n          Default value: <span class=typewriter> ");
					sb.append(defaultValue);
					sb.append(" </span>");
				}
				sb.append("\n        </td>\n");
				sb.append("      </tr>\n");
				if (removeFromHere != null) {
					removeFromHere.remove(option);
				}
			}
			sb.append("    </table>\n");
		}
		
		/**
		 * This tries to obtain the next element of the desired {@link Class}.
		 * 
		 * @param <T>
		 *        The type of the desired element
		 * @param keyProvider
		 *        The {@link KeyProvider} holding the keys
		 * @param clazz
		 *        The class of the desired element.
		 * @param n
		 *        The index of the element to get.
		 * @return null if no such element exists or the desired element.
		 */
		@SuppressWarnings("unchecked")
		public static <T> Entry<T> getField(
			Class<? extends KeyProvider> keyProvider, Class<T> clazz, int n) {
			Field fields[] = keyProvider.getFields();
			Object fieldValue;
			for (; n < fields.length; n++) {
				try {
					fieldValue = fields[n].get(keyProvider);
					if (fieldValue.getClass().isAssignableFrom(clazz)) { return new Entry<T>(
						n, (T) fieldValue); }
				} catch (Exception exc) {
				}
			}
			return null;
		}
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static Iterator<Option> optionIterator(
			final Class<? extends KeyProvider> keyProvider) {
			return iterator(keyProvider, Option.class);
		}
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static Iterator<OptionGroup> optionGroupIterator(
			final Class<? extends KeyProvider> keyProvider) {
			return iterator(keyProvider, OptionGroup.class);
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
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Iterator#hasNext()
				 */
				public boolean hasNext() {
					try {
						return getField(keyProvider, clazz, i + 1) != null;
					} catch (ArrayIndexOutOfBoundsException exc) {
						return false;
					}
				}
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Iterator#next()
				 */
				public T next() {
					Entry<? extends T> entry = getField(keyProvider, clazz, ++i);
					if (entry == null) {
						i = keyProvider.getFields().length;
						return null;
					}
					i = entry.getIndex();
					return entry.getElement();
				}
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Iterator#remove()
				 */
				public void remove() {
					throw new IllegalAccessError();
				}
			};
		}
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static List<Option> optionList(
			Class<? extends KeyProvider> keyProvider) {
			return list(keyProvider, Option.class);
		}
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static List<OptionGroup> optionGroupList(
			Class<? extends KeyProvider> keyProvider) {
			return list(keyProvider, OptionGroup.class);
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
	}
	
}
