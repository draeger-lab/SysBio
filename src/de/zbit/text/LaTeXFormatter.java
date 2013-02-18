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
	
	public String protectedBlank() {
		return "~";
	}

	public String ref(String link) {
		return createLaTeXCommand("vref", link);
	}
	
	/**
	 * @param usingTypewriterFont the usingTypewriterFont to set
	 */
	public void setUsingTypewriterFont(boolean usingTypewriterFont) {
		this.usingTypewriterFont = usingTypewriterFont;
	}

	public String texttt(String text) {
		return isUsingTypewriterFont() ? createLaTeXCommand("texttt", text) : text;
	}
	
}
