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
package de.zbit.kegg;

import java.io.Serializable;

/**
 * This class is used by KeggFunctionManagement. It determines which function
 * should be executed and what are the parameters for this function.
 * 
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KeggQuery implements Comparable<KeggQuery>, Serializable {
	private static final long serialVersionUID = -2970366298298913439L;

	/**
	 * Input: Organism id (e.g. "hsa")
	 * Returns: Definition[]
	 */
	public final static int getPathways = 0; // returns: Definition[] (alt:
	// ArrayList<String>)
	/**
	 * Input: Pathway id (e.g. "path:hsa04010")
	 */
	public final static int getGenesByPathway = 1; // returns: String[]
	/**
	 * Input: KG-Gene-ids, separated by space (e.g. "hsa:123 hsa:142")
	 */
	public final static int getIdentifier = 2; // returns: String (each entry
	// separated by new line)
	
	/**
	 * Input: String gene, String species
	 * Returns: ArrayList<String> kegg identifiers in an arrayList e.g. "hsa:7529"
	 */
	public final static int getKEGGIdentifierForAGeneSymbol = 3;
	
	/**
	 * Input: none.
	 * Returns: Definition[]
	 */
	public final static int getOrganisms = 4;

	/**
	 * 
	 */
	private int jobToDo; // Required
	/**
	 * 
	 */
	private String query; // Required

	/**
	 * 
	 * @param jobToDo
	 * @param query
	 */
	public KeggQuery(int jobToDo, String query) {
		this.jobToDo = jobToDo;
		this.query = query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public KeggQuery clone() {
		KeggQuery clone = new KeggQuery(this.jobToDo, query);
		return clone;
	}

	/**
	 * 
	 */
	public int compareTo(KeggQuery o) {
		if (jobToDo < o.getJobToDo())
			return -1;
		else if (jobToDo > o.getJobToDo())
			return 1;
		else { // Same job to do
		  if (query==null && o.getQuery()==null)
		    return 0;
		  else if (query==null || o.getQuery()==null)
		    return -1;
		  else
			  return query.compareTo(o.getQuery());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof KeggQuery) {
			KeggQuery e = (KeggQuery) o;
			equal &= e.jobToDo == this.jobToDo && this.query.equals(e.query);
		}
		return equal;
	}

	/**
	 * 
	 * @return
	 */
	public int getJobToDo() {
		return jobToDo;
	}

	/**
	 * 
	 * @return
	 */
	public String getQuery() {
		return query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hc = (int) (jobToDo + query==null?0:query.hashCode());
		return (hc);
	}

	/**
	 * 
	 * @param jobToDo
	 */
	public void setJobToDo(int jobToDo) {
		this.jobToDo = jobToDo;
	}

	/**
	 * 
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Job:" + jobToDo + " Query:" + query;
	}

}
