package de.zbit.util.prefs;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 */
public interface KeyProvider {
	
	/**
	 * A collection of useful tools for working with {@link KeyProvider}
	 * instances.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-11-13
	 */
	public static class Tools {
		/**
		 * Returns an {@link Iterator} over all {@link Option} instances defined by
		 * the given {@link KeyProvider}.
		 * 
		 * @param keyProvider
		 * @return
		 */
		public static Iterator<Option<?>> optionIterator(
			final Class<? extends KeyProvider> keyProvider) {
			return new Iterator<Option<?>>() {
				
				private int i = 0;
				
				/**
				 * This tries to obtain the next {@link Option}.
				 * 
				 * @param j
				 * @return
				 */
				private Option<?> getOption(int j) {
					Field field = keyProvider.getFields()[j];
					Object fieldValue;
					for (; j < keyProvider.getFields().length; j++) {
						try {
							fieldValue = field.get(keyProvider);
							if (fieldValue instanceof Option<?>) { return (Option<?>) fieldValue; }
						} catch (Exception exc) {
						}
					}
					return null;
				}
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Iterator#hasNext()
				 */
				public boolean hasNext() {
					try {
						return getOption(i + 1) != null;
					} catch (ArrayIndexOutOfBoundsException exc) {
						return false;
					}
				}
				
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Iterator#next()
				 */
				public Option<?> next() {
					return getOption(i++);
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
		public static Set<Option<?>> optionSet(Class<? extends KeyProvider> keyProvider) {
			Set<Option<?>> optionSet = new HashSet<Option<?>>();
			for (Iterator<Option<?>> iterator = optionIterator(keyProvider); iterator
					.hasNext();) {
				optionSet.add(iterator.next());
			}
			return optionSet;
		}
	}
	
}
