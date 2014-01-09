/*
 * $Id$
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
package de.zbit.graph.io;

import y.view.Graph2D;

/**
 * An interface that allows other classes to handle the writing
 * of Graph2D files themselves.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface Graph2Dwriteable {
  
  
  /**
   * @param document
   * @param path
   * @param format output file extension, e.g., "gif", "graphml", "gml", "jpg",...
   * @throws Exception 
   * @return {@code true} if everything went fine.
   */
  public boolean writeToFile(Graph2D graph, String outFile, String format) throws Exception;
  
  /**
   * 
   * @param graph
   * @param outFile
   * @return
   */
  public boolean writeToFile(Graph2D graph, String outFile);
  
}
