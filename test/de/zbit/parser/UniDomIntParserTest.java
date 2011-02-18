/*
 * $Id$
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
package de.zbit.parser;


/**
 * @author Finja B&uml;chel
 * @version $Rev$
 * @since 1.0
 */
public class UniDomIntParserTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String folder = "C:/Dokumente und Einstellungen/buechel/Eigene Dateien/My Dropbox/Uni/UniDomInt/";
    UniDomIntParser up = new UniDomIntParser(folder + "ReferenceSet.txt", folder + "UniDomInt.tsv");
    up.writeUniDomIntFileWithNewPredictionScores(folder + "UniDomInt.tsv", folder + "ddi.txt");
  }
  

}
