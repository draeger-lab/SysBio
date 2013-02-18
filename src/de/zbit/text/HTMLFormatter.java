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
 * @author Andreas Dr&auml;ger
 * @date 14:37:37
 * @since 1.1
 * @version $Rev$
 */
public class HTMLFormatter implements FormatBuilder {

	public HTMLFormatter() {
		super();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#init(java.io.BufferedWriter, java.lang.String)
	 */
	@Override
	public void init(BufferedWriter bw, String title) throws IOException {
		bw.append("<html>");
		bw.newLine();
		bw.append("  <head>");
		bw.newLine();
		bw.append("    <title>" + title + "</title>");
		bw.newLine();
		bw.append("  </head>");
		bw.newLine();
		bw.append("  <body>");
		bw.newLine();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#close(java.io.BufferedWriter)
	 */
	@Override
	public void close(BufferedWriter bw) throws IOException {
		bw.append("  </body>");
		bw.newLine();
		bw.append("</html>");
		bw.newLine();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#createTableHead(java.io.BufferedWriter, java.lang.String)
	 */
	@Override
	public void createTableHead(BufferedWriter bw, String... entry) throws IOException {
		bw.append("      <tr>");
		bw.newLine();
		for (String e : entry) {
			bw.append("        <th>");
			bw.append(e);
			bw.append("</th>");
			bw.newLine();
		}
		bw.append("      </tr>");
		bw.newLine();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#closeTable(java.io.BufferedWriter)
	 */
	@Override
	public void closeTable(BufferedWriter bw) throws IOException {
		bw.append("    </table>");
		bw.newLine();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#createTableRow(java.io.BufferedWriter, java.lang.String)
	 */
	@Override
	public void createTableRow(BufferedWriter bw, String... columns) throws IOException {
		bw.append("      <tr>");
		bw.newLine();
		for (String c : columns) {
			bw.append("        <td>");
			bw.append(c);
			bw.append("</td>");
			bw.newLine();
		}
		bw.append("      </tr>");
		bw.newLine();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#createTable(java.io.BufferedWriter)
	 */
	@Override
	public void createTable(BufferedWriter bw) throws IOException {
		bw.append("    <table>");
		bw.newLine();
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#createLink(java.io.BufferedWriter, java.lang.String, java.lang.String)
	 */
	@Override
	public void createLink(BufferedWriter bw, String location, String text) throws IOException {
		bw.append("<a href=\"");
		bw.append(location);
		bw.append("\">");
		bw.append(text);
		bw.append("</a>");
	}

	/* (non-Javadoc)
	 * @see de.zbit.text.FormatBuilder#createProtectedBlank()
	 */
	@Override
	public String createProtectedBlank() {
		return "&nbsp;";
	}
	
}
