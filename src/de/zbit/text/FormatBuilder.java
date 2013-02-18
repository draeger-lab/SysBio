/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.text;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public interface FormatBuilder {
	
	/**
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public abstract void close(BufferedWriter bw) throws IOException;
	
	/**
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public abstract void closeTable(BufferedWriter bw) throws IOException;
	
	/**
	 * 
	 * @param bw
	 * @param location
	 * @param text
	 * @throws IOException
	 */
	public abstract void createLink(BufferedWriter bw, String location,
		String text) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	public String createProtectedBlank();
	
	/**
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public abstract void createTable(BufferedWriter bw) throws IOException;
	
	/**
	 * 
	 * @param bw
	 * @param entry
	 * @throws IOException
	 */
	public abstract void createTableHead(BufferedWriter bw, String... entry)
		throws IOException;
	
	/**
	 * 
	 * @param bw
	 * @param columns
	 * @throws IOException
	 */
	public abstract void createTableRow(BufferedWriter bw, String... columns)
		throws IOException;
	
	/**
	 * 
	 * @param bw
	 * @param title
	 * @throws IOException
	 */
	public abstract void init(BufferedWriter bw, String title) throws IOException;
	
}
