/**
 *
 * @author wrzodek
 */
package de.zbit.kegg.io;

import de.zbit.kegg.parser.pathway.Pathway;

/**
 * @author wrzodek
 *
 */
public interface KeggConverter {
  public void Convert(Pathway p, String outFile);
  public void Convert(String infile, String outfile);
  
  // Gibt an, ob das letzte geschriebene outFile bereits vorhanden war und deshalb ueberschrieben wurde.
  public boolean lastFileWasOverwritten();
  
}
