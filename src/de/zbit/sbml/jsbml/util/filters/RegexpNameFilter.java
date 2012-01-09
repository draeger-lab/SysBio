/*
 * $Id: RegexpNameFilter.java 708 2012-01-06 14:56:40Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/gui/RegexpNameFilter.java $
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

package de.zbit.sbml.jsbml.util.filters;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.util.filters.Filter;

/**
 * This filter only accepts instances of {@link NamedSBase} with the name as
 * given in the constructor of this object.
 * 
 * @author Sebastian Nagel
 * @date 2012-01-04
 * @since 0.8
 * @version $Rev: 708 $
 */
public class RegexpNameFilter implements Filter {

	/**
	 * The desired regexp for NamedSBases to be acceptable.
	 */
	private String regexp;
	
	/**
	 * @return the regexp
	 */
	public String getRegexp() {
		return regexp;
	}

	/**
	 * 
	 */
	private boolean caseSensitive;
	
	/**
	 * 
	 */
	private boolean invert;

	/**
	 * 
	 * @param regexp
	 */
	public RegexpNameFilter(String regexp) {
		this(regexp, true);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 */
	public RegexpNameFilter(String regexp, boolean caseSensitive) {
		this(regexp, caseSensitive, false);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 * @param invert
	 */
	public RegexpNameFilter(String regexp, boolean caseSensitive, boolean invert) {
		this.regexp = regexp;
		this.caseSensitive = caseSensitive;
		this.invert = invert;
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.filters.Filter#accepts(java.lang.Object)
	 */
	public boolean accepts(Object o) {
		boolean result = false;
		if (o instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) o;
			if ((nsb.isSetName() || nsb.isSetId()) && (regexp != null)) {
				String name = nsb.getName();
				String id = nsb.getId();
				if (!caseSensitive){
					regexp = regexp.toLowerCase();
					name = name.toLowerCase();
					id = id.toLowerCase();
				}
				result = (name.matches(regexp) || id.matches(regexp));
			}
		}
		if (invert) {
			return !result;
		}
		return result;
	}

}
