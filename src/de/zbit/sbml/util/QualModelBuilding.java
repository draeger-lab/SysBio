package de.zbit.sbml.util;

import java.io.FileNotFoundException;
import java.util.Calendar;

import javax.xml.stream.XMLStreamException;

import org.sbgn.bindings.SBGNBase.Notes;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.qual.QualitativeModel;

import de.zbit.util.DatabaseIdentifierTools;
import de.zbit.util.EscapeChars;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;

public abstract class QualModelBuilding {

	public static final String QUAL_NS = "http://www.sbml.org/sbml/level3/version1/qual/version1";
	public static final String QUAL_NS_PREFIX = "qual";
	
	public static QualitativeModel qualModel;
	public static Layout layout;
	public static Model model;
	
	protected final static String notesStartString = "<notes><body xmlns=\"http://www.w3.org/1999/xhtml\">";
	protected final static String notesEndString = "</body></notes>";
	
	/**
	 * 
	 * @param modelName
	 * @param modelID
	 * @param creator
	 * @param taxon
	 * @param organism
	 * @return
	 */
	public static SBMLDocument initializeQualDocument(String modelName, String modelID, String creator, String taxon, String organism) {
	    System.out.println("beginning");

	    SBMLDocument doc = new SBMLDocument(3, 1);
	    doc.addNamespace(QUAL_NS_PREFIX, "xmlns", QUAL_NS);
		
	    model = doc.createModel(modelID);
	    CVTerm term = DatabaseIdentifierTools.getCVTerm(IdentifierDatabases.NCBI_Taxonomy, null, taxon);
	    model.addCVTerm(term);
	    
	    
	    StringBuffer notes = new StringBuffer(notesStartString);
	    notes.append(String.format("<h1>%s (%s)</h1>\n", formatTextForHTMLnotes(modelName), organism));
	    model.setName(String.format("%s (%s)", modelName, organism)); 
	    
	    notes.append(notesEndString);
	    model.setNotes(notes.toString());
	    if (!model.getHistory().isSetCreatedDate()) {
	    	model.getHistory().setCreatedDate(Calendar.getInstance().getTime());
	    }
	    else {
	    	model.getHistory().addModifiedDate(Calendar.getInstance().getTime());
	    }
	    
	    qualModel = new QualitativeModel(model);
	    model.addExtension(QUAL_NS, QualModelBuilding.qualModel);
		
	    ExtendedLayoutModel layoutExt = new ExtendedLayoutModel(model);
		model.addExtension(LayoutConstants.namespaceURI, layoutExt);
		layout = layoutExt.createLayout();
	    
		return doc;
	}
	
	  /**
	   * Escapes all HTML-tags in the given string and
	   * replaces new lines with a space. 
	   * @param text
	   * @return
	   */
	  public static String formatTextForHTMLnotes(String text) {
	    if (text==null) return "";
	    return EscapeChars.forHTML(text.replace('\n', ' '));
	  }
	
	/**
	 * 
	 * @param doc
	 * @param outputFile
	 * @throws SBMLException
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public static void writeSBMlDocument(SBMLDocument doc, String outputFile) throws SBMLException, FileNotFoundException, XMLStreamException {
		SBMLWriter.write(doc, outputFile, "Sysbio-Project", "1");
		System.out.println("ready");
	}

}
