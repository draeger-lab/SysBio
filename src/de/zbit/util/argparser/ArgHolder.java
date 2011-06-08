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
package de.zbit.util.argparser;

import java.io.Serializable;

/**
 * A carrier for a certain value of the given generic type.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.1
 * @see ArgParser
 */
public class ArgHolder<V> implements Cloneable, Serializable {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5362889114277649711L;
	
	/**
	 * Value of the {@link Object} reference, set and examined by the application
	 * as needed.
	 */
	private V value;
	
	/**
	 * 
	 */
	public ArgHolder() {
		this((V) null);
	}
	
	/**
	 * 
	 * @param value
	 */
	public ArgHolder(V value) {
		setValue(value);
	}
	
	/**
	 * Clone constructor.
	 * 
	 * @param valueHolder
	 */
	public ArgHolder(ArgHolder<V> valueHolder) {
		this(valueHolder.getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ArgHolder<V> clone() {
		return new ArgHolder<V>(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ArgHolder<?>) {
			ArgHolder<?> v = (ArgHolder<?>) o;
			boolean equal = v.isSetValue() == isSetValue();
			if (equal && isSetValue()) { 
				return v.getValue().equals(getValue()); 
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return the {@link #value}.
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Check if a value has been set.
	 * 
	 * @return <code>true</code> if the {@link #value} in this {@link ArgHolder}
	 *         is not null, <code>false</code> otherwise.
	 */
	public boolean isSetValue() {
		return value != null;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setValue(V value) {
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(),
			getValue() == null ? "null" : getValue().toString());
	}
	
	/**
	 * @see #setValue(Object)
	 */
	public void unsetValue() {
		setValue(null);
	}
	
}
