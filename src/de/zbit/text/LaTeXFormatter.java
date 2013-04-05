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

import de.zbit.text.TableColumn.Align;


/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class LaTeXFormatter {
	
	/**
	 * 
	 */
	private boolean usingTypewriterFont;
	
	/**
	 * 
	 */
	public LaTeXFormatter() {
		usingTypewriterFont = true;
	}

	/**
	 * 
	 * @param cmdName
	 * @param arg
	 * @return
	 */
	private String createLaTeXCommand(String cmdName, String arg) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\");
		sb.append(cmdName);
		sb.append('{');
		sb.append(arg);
		sb.append('}');
		return sb.toString();
	}

	/**
	 * @return the usingTypewriterFont
	 */
	public boolean isUsingTypewriterFont() {
		return usingTypewriterFont;
	}
	
	/**
	 * 
	 * @return
	 */
	public String protectedBlank() {
		return "~";
	}

	/**
	 * 
	 * @param link
	 * @return
	 */
	public String ref(String link) {
		return createLaTeXCommand("vref", link);
	}
	
	/**
	 * @param usingTypewriterFont the usingTypewriterFont to set
	 */
	public void setUsingTypewriterFont(boolean usingTypewriterFont) {
		this.usingTypewriterFont = usingTypewriterFont;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public String texttt(String text) {
		return isUsingTypewriterFont() ? createLaTeXCommand("texttt", text) : text;
	}

	/**
	 * 
	 * @return
	 */
	public String beginLandscape() {
		return "\\begin{landscape}";
	}

	/**
	 * 
	 * @return
	 */
	public String trademark() {
		return "\\texttrademark{}";
	}
	
	/**
	 * 
	 * @param color
	 * @param text
	 * @return
	 */
	public String textcolor(String color, String text) {
		return "\\textcolor{" + color + "}{" + text + "}";
	}

	/**
	 * 
	 * @param url
	 * @param label
	 * @return
	 */
	public String link(String url, String label) {
		return "\\href{" + url + "}{" + label + "}";
	}

	/**
	 * 
	 * @return
	 */
	public String sbml2latex() {
		return "\\SBMLLaTeX";
	}

	/**
	 * 
	 * @return
	 */
	public String emdash() {
		return "---";
	}

	/**
	 * 
	 * @return
	 */
	public String numero() {
		return "\\numero";
	}

	/**
	 * 
	 * @param columnCount
	 * @param align
	 * @param text
	 * @return
	 */
	public String multicolumn(int columnCount, Align align, Object text) {
		return multicolumn(columnCount, align, text, false, false);
	}

	/**
	 * 
	 * @param columnCount
	 * @param align
	 * @param text
	 * @param leftBorder
	 * @param rightBorder
	 * @return
	 */
	public String multicolumn(int columnCount, Align align, Object text, boolean leftBorder,
		boolean rightBorder) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\multicolumn{");
		sb.append(columnCount);
		sb.append("}{");
		if (leftBorder) {
			sb.append('|');
		}
		sb.append(align.toString().charAt(0));
		if (rightBorder) {
			sb.append('|');
		}
		sb.append("}{");
		sb.append(text);
		sb.append('}');
		return sb.toString();
	}

	/**
	 * 
	 * @param label
	 * @return
	 */
	public String labeledItem(String label) {
		return "\\item[" + label + "] ";
	}

	/**
	 * 
	 * @param subject
	 * @return
	 */
	public String documentSubject(String subject) {
		return "\\subject{" + subject + "}";
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public String quote(String text) {
		return "``" + text + "\"";
	}

	/**
	 * 
	 * @param title
	 * @return
	 */
	public String caption(String title) {
		return "\\caption{" + title + "}\n";
	}

	/**
	 * 
	 * @param style
	 * @return
	 */
	public String thisPageStyle(String style) {
		return "\\thispagestyle{" + style + "}";
	}

	/**
	 * 
	 * @return
	 */
	public String today() {
		return "\\today";
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public String date(String date) {
		return "\\date{" + date + "}";
	}

	/**
	 * 
	 * @return
	 */
	public String appendix() {
		return "\\appendix";
	}

	/**
	 * 
	 * @param label
	 * @return
	 */
	public String label(String label) {
		return "\\label{" + label + "}";
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public String mathText(String text) {
		return "\\text{" + text + "}";
	}

	/**
	 * 
	 * @return
	 */
	public String emptySet() {
		return "\\emptyset";
	}
	
}

