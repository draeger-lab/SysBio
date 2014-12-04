/* $Id$
 * $URL$
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

import java.util.AbstractList;

import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Model;

/**
 * This class makes all instances of {@link CallableSBase} of a {@link Model}
 * accessible in a {@link List}.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class ListOfCallableSBases extends AbstractList<CallableSBase> {
  
  private Model model;
  
  /**
   * 
   * @param model
   */
  public ListOfCallableSBases(Model model) {
    this.model = model;
  }
  
  /* (non-Javadoc)
   * @see java.util.AbstractList#get(int)
   */
  @Override
  public CallableSBase get(int index) {
    if (index < model.getCompartmentCount()) {
      return model.getCompartment(index);
    }
    index -= model.getCompartmentCount();
    if (index < model.getSpeciesCount()) {
      return model.getSpecies(index);
    }
    index -= model.getSpeciesCount();
    if (index < model.getParameterCount()) {
      return model.getParameter(index);
    }
    index -= model.getParameterCount();
    return model.getReaction(index);
  }
  
  /* (non-Javadoc)
   * @see java.util.AbstractCollection#size()
   */
  @Override
  public int size() {
    return model.getSymbolCount() + model.getReactionCount();
  }
  
}
