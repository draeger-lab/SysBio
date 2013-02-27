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
package de.zbit.sbml.layout.y;

import java.awt.Color;

import y.layout.DiscreteEdgeLabelModel;
import y.view.EdgeLabel;

/**
 * EdgeLabel subclass for stoichiometry values on reaction edges.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class StoichiometryLabel extends EdgeLabel {

	/**
	 * @param value the stoichiometry value
	 */
	public StoichiometryLabel(String value) {
		super(value);
		
		DiscreteEdgeLabelModel elModel = new DiscreteEdgeLabelModel();  
		elModel.setDistance(0.0);
		setLabelModel(elModel);
		setModelParameter(DiscreteEdgeLabelModel.createPositionParameter(DiscreteEdgeLabelModel.TTAIL));
		
		setLineColor(Color.BLACK);
	}

}
