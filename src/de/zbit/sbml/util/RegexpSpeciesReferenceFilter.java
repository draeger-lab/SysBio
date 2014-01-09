/*
 * $Id: RegexpSpeciesReferenceFilter.java 708 2012-01-06 14:56:40Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/jsbml/util/filter/RegexpSpeciesReferenceFilter.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

package de.zbit.sbml.util;

import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.filters.Filter;

/**
 * This is a {@link Filter} that allows to search with a regular expression for a
 * {@link SimpleSpeciesReference} that refers to a {@link Species} with the
 * given identifier attribute.
 * 
 * @author Sebastian Nagel
 * @date 2012-01-14
 * @since 1.1
 * @version $Rev: 708 $
 */
public class RegexpSpeciesReferenceFilter implements Filter {

	/**
	 * The desired regexp for SimpleSpeciesReference to be acceptable.
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
	public RegexpSpeciesReferenceFilter(String regexp) {
		this(regexp, true);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 */
	public RegexpSpeciesReferenceFilter(String regexp, boolean caseSensitive) {
		this(regexp, caseSensitive, false);
	}
	
	/**
	 * 
	 * @param regexp
	 * @param caseSensitive
	 * @param invert
	 */
	public RegexpSpeciesReferenceFilter(String regexp, boolean caseSensitive, boolean invert) {
		this.regexp = regexp;
		this.caseSensitive = caseSensitive;
		this.invert = invert;
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.filters.Filter#accepts(java.lang.Object)
	 */
	public boolean accepts(Object o) {
		boolean result = false;
		if (o instanceof SimpleSpeciesReference) {
			SimpleSpeciesReference specRef = (SimpleSpeciesReference) o;
			if (specRef.isSetSpecies() && (regexp != null)) {
				String refName = specRef.getName();
				String refId = specRef.getSpecies();
				String specName = specRef.getSpeciesInstance().getName();
				String specId = specRef.getSpeciesInstance().getId();
				if (!caseSensitive) {
					regexp = regexp.toLowerCase();
					refName = refName.toLowerCase();
					refId = refId.toLowerCase();
					specName = specName.toLowerCase();
					specId = specId.toLowerCase();
				}
				result = (refName.matches(regexp) || refId.matches(regexp) || specName.matches(regexp) || specId.matches(regexp));
			}
		}
		if (invert) {
			return !result;
		}
		return result;
	}

}
