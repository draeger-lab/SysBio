/*
 * $Id$ $URL$ Copyright John E. Lloyd, 2004. All rights reserved. Permission to
 * use, copy, modify and redistribute is granted, provided that this copyright
 * notice is retained and the author is given credit whenever appropriate.
 * 
 * This software is distributed "as is", without any warranty, including any
 * implied warranty of merchantability or fitness for a particular use. The
 * author assumes no responsibility for, and shall not be liable for, any
 * special, indirect, or consequential damages, or any damages whatsoever,
 * arising out of or in connection with the use of this software.
 */
package de.zbit.util.argparser;

/**
 * Wrapper class which ``holds'' an Object reference, enabling methods to return
 * Object references through arguments.
 * 
 * @author John E. Lloyd
 * @author Andreas Dr&auml;ger
 * @since Fall 2004
 * @version $Rev$
 * @see ArgParser
 */
public class ObjectHolder extends ArgHolder<Object> {
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5975643311817849520L;
		
	/**
	 * Constructs a new <code>ObjectHolder</code> with an initial value of
	 * <code>null</code>.
	 */
	public ObjectHolder() {
		super((Object) null);
	}
	
	/**
	 * Constructs a new <code>ObjectHolder</code> with a specific initial value.
	 * 
	 * @param o
	 *        Initial Object reference.
	 */
	public ObjectHolder(Object o) {
		super(o);
	}
	
}
