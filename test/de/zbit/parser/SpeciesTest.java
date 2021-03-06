/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import de.zbit.resources.Resource;
import de.zbit.util.Species;

/**
 * 
 * @version $Rev$
 */
public class SpeciesTest {
  
  public static void main(String[] args) throws IOException {
    System.out.println(Species.getSpeciesWithKEGGIDInList("hsa", new BufferedReader(
      new InputStreamReader(Resource.class.getResourceAsStream("speclist.txt")))).getScientificName());
    
    List<Species> list = Species.generateSpeciesDataStructure(new BufferedReader(
      new InputStreamReader(Resource.class.getResourceAsStream("speclist.txt"))), true);
    
    System.out.println(list.get(list.indexOf(new Species("HoMo sapiEns"))).getKeggAbbr());
    System.out.println(list.get(list.indexOf(new Species("Mus musculus"))).getKeggAbbr());
    System.out.println(list.get(list.indexOf(new Species("Zygosaccharomyces rouxii"))).getKeggAbbr());
  }
  
}
