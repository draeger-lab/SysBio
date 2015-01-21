/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.gui;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.compilers.HTMLFormula;

import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class GUIToolsForSBML {
	
  /**
	 * Creates a JEditorPane that displays the given UnitDefinition as a HTML.
	 * 
	 * @param ud
	 * @return
	 */
	public static JEditorPane unitPreview(UnitDefinition ud) {
		JEditorPane preview = new JEditorPane("text/html", StringUtil
				.toHTML(ud != null ? HTMLFormula.toHTML(ud) : ""));
		preview.setEditable(false);
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		return preview;
	}

}
