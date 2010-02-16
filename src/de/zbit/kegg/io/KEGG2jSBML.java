package de.zbit.kegg.io;

import java.io.File;
import java.util.ArrayList;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.CVTerm.Qualifier;

import y.view.Graph2D;

import de.zbit.kegg.KeggAdaptor;
import de.zbit.kegg.parser.KeggParser;
import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.Pathway;

public class KEGG2jSBML {
  public static boolean retrieveKeggAnnots=true; // Retrieve annotations from Kegg or use purely information available in the document.
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args!=null && args.length >0) {
      File f = new File(args[0]);
      if (f.isDirectory()) BatchConvertKegg.main(args); // TODO: Change to Batch for SBML
      else {
        String outfile = args[0].substring(0, args[0].contains(".")?args[0].lastIndexOf("."):args[0].length())+".sbml.xml";
        if (args.length>1) outfile = args[1];
        Pathway p = KeggParser.parse(args[0]).get(0);
        KEGG2SBML(p, outfile);
      }
      return;
    }
    
    //KeggParser.silent=false;
    System.out.println("Reading kegg pathway...");
    Pathway p = KeggParser.parse("_ko00010.xml").get(0); //04115 ko02010
    //p = KeggParser.parse("http://kaas.genome.jp/kegg/KGML/KGML_v0.6.1/ko/ko00010.xml").get(0);
    
    System.out.println("Converting to SBML");
    KEGG2SBML(p, "test.sbml.xml");
  }

  private static void KEGG2SBML(Pathway p, String outfile) {
    SBMLDocument doc = new SBMLDocument(2,4);
    //ArrayList<String> PWReferenceNodeTexts = new ArrayList<String>(); 
    KeggAdaptor adap = null;
    if (retrieveKeggAnnots) adap = new KeggAdaptor();
    
    // new Model with Kegg id as id.
    Model sbmlModel = doc.createModel(p.getName());
    sbmlModel.setMetaId("meta_" + sbmlModel.getId());
    sbmlModel.setName(p.getTitle());
    Compartment sbmlCompartment = sbmlModel.createCompartment(); // Create neccessary default compartment
    
    // Parse Kegg Pathway information
    CVTerm mtPwID = new CVTerm();
    mtPwID.setModelQualifierType(Qualifier.BQM_IS);
    mtPwID.addResource("urn:miriam:kegg.pathway" + p.getName().substring(p.getName().indexOf(":")));
    sbmlModel.addCVTerm(mtPwID);
    
    // TODO: Implement list, which saves last queried items, sorts by queried last and manages to keep iNet usage low.
    if (adap!=null) { // Retrieve further information via Kegg Adaptor
      String orgInfos = adap.get("GN:" + p.getOrg()); // Retrieve all organism information via KeggAdaptor
      String tax = KeggAdaptor.extractInfo(orgInfos, "TAXONOMY", "\n").trim(); // e.g. "TAXONOMY    TAX:9606" => "TAX:9606". 
      CVTerm mtOrgID = new CVTerm();     
      mtOrgID.setModelQualifierType(Qualifier.BQB_OCCURS_IN); // TODO: Richtiger Qaulifier? BQB?
      mtOrgID.addResource("urn:miriam:taxonomy" + tax.substring(tax.indexOf(":")));
      sbmlModel.addCVTerm(mtOrgID);
    }
    // TODO: Organismus im klartext reinschreiben? "Homo Sapiens"
    
    sbmlModel.appendNotes(String.format("<a href=\"%s\"><img src=\"%s\" alt=\"%s\"/></a><br>\n", p.getImage(), p.getImage(), p.getImage()));
    sbmlModel.appendNotes(String.format("<a href=\"%s\">Description</a><br>\n", p.getLink()));
    
    // Create species
    ArrayList<Entry> entries = p.getEntries();
    for (Entry entry: entries) {
      
    }
    
    
    
    
    
  }
  
}
