/* $Id$
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
package de.zbit.util.objectwrapper;

/**
 * This entity stores three objects.
 * 
 * @author Andreas Dr&auml;ger
 * @date 17:54:01
 * @since 1.1
 * @version $Rev$
 */
public class ValueTripletUncomparable<A, B, C> extends ValuePairUncomparable<A, B> {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -9184607987991797415L;
	
	/**
	 * The third object.
	 */
	private C c;

	/**
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public ValueTripletUncomparable(A a, B b, C c) {
		super(a, b);
		this.c = c;
	}
	
	/**
	 * @param ValuePairUncomparable
	 */
	public ValueTripletUncomparable(ValueTripletUncomparable<A, B, C> vt) {
		super(vt);
		this.c = vt.getC();
	}

	/* (non-Javadoc)
	 * @see de.zbit.util.ValuePairUncomparable#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o.getClass().isAssignableFrom(getClass())) {
			boolean equal = super.equals(o);
			@SuppressWarnings("unchecked")
			ValueTripletUncomparable<A, B, C> tp = (ValueTripletUncomparable<A, B, C>) o;
			equal &= tp.isSetC() == isSetC();
			if (equal && isSetC()) {
				equal &= tp.getC().equals(getC());
			}
			return equal;
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public C getC() {
		return c;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.util.ValuePairUncomparable#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if (isSetC()) {
			hashCode += 7 * getC().hashCode();
		}
		return hashCode;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSetC() {
		return c != null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.util.ValuePairUncomparable#toString()
	 */
	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", getA(), getB(), getC());
	}
	
}
