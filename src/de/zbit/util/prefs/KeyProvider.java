package de.zbit.util.prefs;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
			sb.append("<html>\n");
			sb.append("  <head>\n");
			sb.append("    <style type=\"text/css\">\n      <!--\n");
			sb.append("        .typewriter-blue {\n");
			sb.append("           font-family:'courier new',courier,monospace;\n");
			sb.append("           color:#0000C0;\n        }\n      -->\n");
			sb.append("    </style>\n");
			sb.append("    <title> ");
			sb.append(keyProvider.getSimpleName());
			sb.append(" </title>\n");
			sb.append("  </head>\n");
			sb.append("  <body>\n");
			
			Set<OptionGroup> groupSet = optionGroupSet(keyProvider);
			if (groupSet.size() > 0) {
				for (OptionGroup<?> group : groupSet) {
					sb.append("    <h1> ");
					sb.append(group.getName());
					sb.append(" </h1>\n      <p>");
					sb.append(StringUtil.insertLineBreaks(group.getToolTip(), 70, "\n      "));
					sb.append("</p>\n      ");
					sb.append("<table cellspacing=\"1\" cellpadding=\"1\" border=\"0\">\n");
					for (Option<?> option : group.getOptions()) {
						sb.append("        <tr>\n");
						sb.append("          <td colspan=\"2\" class=\"typewriter-blue\">");
						sb.append(option.toCommandLineOptionKey());
						sb.append("=&#60;");
						sb.append(option.getRequiredType().getSimpleName());
						sb.append("&#62;</td>\n");
						sb.append("        </tr>\n        <tr><td width=\"6%\"> </td>\n");
						sb.append("        <td>\n          ");
						sb.append(StringUtil.insertLineBreaks(option.getToolTip(), 60, "\n          "));
						sb.append("\n        </td>\n");
						sb.append("      </tr>\n");
					}
					sb.append("    </table>\n");
				}
				// TODO: memorize options that occurred already.
			}
			// TODO: print options that have not been displayed yet.
			
			sb.append("  </body>\n");
			sb.append("</html>\n");
			
			return sb.toString();
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
		public static Set<Option> optionSet(Class<? extends KeyProvider> keyProvider) {
			return set(keyProvider, Option.class);
		}
		
		/**
		 * 
		 * @param keyProvider
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static Set<OptionGroup> optionGroupSet(
			Class<? extends KeyProvider> keyProvider) {
			return set(keyProvider, OptionGroup.class);
		}
		
		/**
		 * 
		 * @param <T>
		 * @param keyProvider
		 * @param clazz
		 * @return
		 */
		public static <T> Set<T> set(Class<? extends KeyProvider> keyProvider,
			Class<T> clazz) {
			Set<T> optionSet = new HashSet<T>();
			for (Iterator<T> iterator = iterator(keyProvider, clazz); iterator
					.hasNext();) {
				optionSet.add(iterator.next());
			}
			return optionSet;
		}
	}
	
}
