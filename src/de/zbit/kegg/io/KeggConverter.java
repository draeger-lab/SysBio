/**
 *
 */
package de.zbit.kegg.io;

import de.zbit.kegg.parser.pathway.Pathway;

/**
 * @author wrzodek
 * 
 */
public interface KeggConverter {
	
	/**
	 * 
	 * @param p
	 * @param outFile
	 */
	public void Convert(Pathway p, String outFile);
	
	/**
	 * 
	 * @param infile
	 * @param outfile
	 */
	public void Convert(String infile, String outfile);

	/**
	 * Gibt an, ob das letzte geschriebene outFile bereits vorhanden war und
	 * deshalb ueberschrieben wurde.
	 * 
	 * @return
	 */
	public boolean lastFileWasOverwritten();

}
