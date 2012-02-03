/* $Id$
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
package de.zbit.sbml.gui;


/**
 * A component for a graphical user interface that supports maintaining a
 * {@link EquationRenderer} for equations.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public interface EquationComponent {

	/**
	 * @return the renderer
	 */
	public EquationRenderer getEquationRenderer();

	/**
	 * @param renderer the renderer to set
	 */
	public void setEquationRenderer(EquationRenderer renderer);
	
}
