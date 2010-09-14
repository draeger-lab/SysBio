/**
 * 
 */
package de.zbit.parser;


/**
 * @author Finja B&uml;chel
 * 
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
