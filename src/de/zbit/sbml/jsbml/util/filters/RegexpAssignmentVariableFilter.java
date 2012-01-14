/*
 * $Id: RegexpAssignmentVariableFilter.java 708 2012-01-06 14:56:40Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/jsbml/util/filter/RegexpAssignmentVariableFilter.java $
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

import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.util.filters.Filter;

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class RegexpAssignmentVariableFilter implements Filter {

	/**
	 * 
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
	public RegexpAssignmentVariableFilter(String regexp) {
		this(regexp, true);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 */
	public RegexpAssignmentVariableFilter(String regexp, boolean caseSensitive) {
		this(regexp, caseSensitive, false);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 * @param invert
	 */
	public RegexpAssignmentVariableFilter(String regexp, boolean caseSensitive, boolean invert) {
		this.regexp = regexp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.Filter#fulfilsProperty(java.lang.Object)
	 */
	public boolean accepts(Object o) {
		boolean result = false;
		if (o instanceof Assignment) {
			Assignment er = (Assignment) o;
			if (er.isSetVariable() && (regexp != null)) {
				String assId = er.getVariable();
				String varId = er.getVariableInstance().getId();
				String varName = er.getVariableInstance().getName();
				if (!caseSensitive){
					regexp = regexp.toLowerCase();
					assId = assId.toLowerCase();
					varId = varId.toLowerCase();
					varName = varName.toLowerCase();
				}
				result = (assId.matches(regexp) || varId.matches(regexp) || varName.matches(regexp));
			}
		}
		if (invert) {
			return !result;
		}
		return result;
	}

}
