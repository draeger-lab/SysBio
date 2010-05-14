/**
 *
 */
package de.zbit.kegg.io;

import javax.xml.stream.XMLStreamException;

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
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws XMLStreamException
	 */
	public void Convert(Pathway p, String outFile) throws XMLStreamException,
			InstantiationException, IllegalAccessException;

	/**
	 * 
	 * @param infile
	 * @param outfile
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws XMLStreamException
	 */
	public void Convert(String infile, String outfile)
			throws XMLStreamException, InstantiationException,
			IllegalAccessException;

	/**
	 * Gibt an, ob das letzte geschriebene outFile bereits vorhanden war und
	 * deshalb ueberschrieben wurde.
	 * 
	 * @return
	 */
	public boolean lastFileWasOverwritten();

}
