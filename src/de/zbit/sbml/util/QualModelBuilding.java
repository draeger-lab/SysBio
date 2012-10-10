package de.zbit.sbml.util;

import java.io.FileNotFoundException;
import java.util.Calendar;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.qual.QualitativeModel;

public abstract class QualModelBuilding {

	public static final String QUAL_NS = "http://www.sbml.org/sbml/level3/version1/qual/version1";
	public static final String QUAL_NS_PREFIX = "qual";
	
	public static QualitativeModel qualModel;
	public static Layout layout;
	public static Model model;
	
	
	public static SBMLDocument initializeQualDocument(String modelName, String modelID, String creator) {
	    System.out.println("beginning");

	    SBMLDocument doc = new SBMLDocument(3, 1);
	    doc.addNamespace(QUAL_NS_PREFIX, "xmlns", QUAL_NS);
		
	    model = doc.createModel(modelID);
	    
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
	
	public static void writeSBMlDocument(SBMLDocument doc, String outputFile) throws SBMLException, FileNotFoundException, XMLStreamException {
		SBMLWriter.write(doc, outputFile, "Sysbio-Project", "1");
		System.out.println("ready");
	}

}
