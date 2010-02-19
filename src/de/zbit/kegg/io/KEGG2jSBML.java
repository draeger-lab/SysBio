package de.zbit.kegg.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.History;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.CVTerm.Qualifier;

import y.view.Graph2D;

import de.zbit.kegg.KeggAdaptor;
import de.zbit.kegg.parser.KeggParser;
import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Graphics;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.util.ProgressBar;

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
  
  public static SBMLDocument Kegg2jSBML(String filepath) {
    // TODO: implement me.
    return null;
  }

  /*
   * MIRIAM Kegg IDs:
   * urn:miriam:kegg.pathway (hsa00620)
   * urn:miriam:kegg.compound (C12345)
   * urn:miriam:kegg.reaction (R00100)
   * urn:miriam:kegg.drug (D00123)
   * urn:miriam:kegg.glycan (G00123)
   * urn:miriam:kegg.genes (syn:ssr3451)
   */
  
  private static void KEGG2SBML(Pathway p, String outfile) {
    int level=2; int version=4;
    SBMLDocument doc = new SBMLDocument(level,version);
    //ArrayList<String> PWReferenceNodeTexts = new ArrayList<String>(); 
    KeggAdaptor adap = null;
    if (retrieveKeggAnnots) adap = new KeggAdaptor();
    
    // Initialize a progress bar.
    int aufrufeGesamt=p.getEntries().size(); //+p.getRelations().size(); // Relations gehen sehr schnell.
    //if (adap==null) aufrufeGesamt+=p.getRelations().size(); // TODO: noch ausloten wann klasse aufgerufen wird.
    ProgressBar progress = new ProgressBar(aufrufeGesamt+1);
    progress.DisplayBar();
    
    // new Model with Kegg id as id.
    Model model = doc.createModel(p.getName());
    model.setMetaId("meta_" + model.getId());
    model.setName(p.getTitle());
    Compartment compartment = model.createCompartment(); // Create neccessary default compartment
    
    // Create Model History
    History hist = new History();
    Creator creator = new Creator();
    creator.setOrganisation("ZBIT, University of T\u00fcbingen, WSI-RA");
    hist.addCreator(creator);
    hist.addModifiedDate(Calendar.getInstance().getTime());
    model.setModelHistory(hist);
    
    // Parse Kegg Pathway information
    CVTerm mtPwID = new CVTerm();
    mtPwID.setModelQualifierType(Qualifier.BQM_IS);
    mtPwID.addResource("urn:miriam:kegg.pathway" + p.getName().substring(p.getName().indexOf(":")));
    model.addCVTerm(mtPwID);
    
    // TODO: Implement list, which saves last queried items, sorts by queried last and manages to keep iNet usage low.
    if (adap!=null) { // Retrieve further information via Kegg Adaptor
      String orgInfos = adap.get("GN:" + p.getOrg()); // Retrieve all organism information via KeggAdaptor
      String tax = KeggAdaptor.extractInfo(orgInfos, "TAXONOMY", "\n").trim(); // e.g. "TAXONOMY    TAX:9606" => "TAX:9606". 
      CVTerm mtOrgID = new CVTerm();
      mtOrgID.setBiologicalQualifierType(Qualifier.BQB_OCCURS_IN);
      mtOrgID.addResource("urn:miriam:taxonomy" + tax.substring(tax.indexOf(':')));
      model.addCVTerm(mtOrgID);
      
      model.appendNotes(String.format("<h1>Model of %s in %s</h1>\n", p.getTitle(), KeggAdaptor.extractInfo(orgInfos, "DEFINITION", "\n").trim()  ));
      
      // Get PW infos from KEGG Api for Description and GO ids.
      String pwInfos = adap.get(p.getName()); // NAME, DESCRIPTION, DBLINKS verwertbar
      model.appendNotes(String.format("%s<br>\n", KeggAdaptor.extractInfo(pwInfos, "DESCRIPTION").trim()));
      
      // GO IDs
      String goIDs = KeggAdaptor.extractInfo(pwInfos, "GO:", "\n").trim();
      CVTerm mtGoID = new CVTerm();
      mtGoID.setBiologicalQualifierType(Qualifier.BQB_IS_VERSION_OF); // TODO: richtiger Qulifier für GO? (aus neustem Biomodel kopoiert).
      for (String go_id:goIDs.split(" ")) {
        if (go_id.length()!=7 || !containsOnlyDigits(go_id)) continue; // Invalid GO id.
        mtGoID.addResource("urn:miriam:obo.go:GO:" + go_id);
      }
      if (mtGoID.getNumResources()>0) model.addCVTerm(mtGoID);
    }
    model.appendNotes(String.format("<a href=\"%s\"><img src=\"%s\" alt=\"%s\"/></a><br>\n", p.getImage(), p.getImage(), p.getImage()));
    model.appendNotes(String.format("<a href=\"%s\">Original Entry</a><br>\n", p.getLink()));
    
    // Create species
    ArrayList<Entry> entries = p.getEntries();
    for (Entry entry: entries) {
      progress.DisplayBar();
      /*
       *<entry id="1" name="ko:K00128" type="ortholog" reaction="rn:R00710"
             link="http://www.genome.jp/dbget-bin/www_bget?ko+K00128">
          <graphics name="K00128" fgcolor="#000000" bgcolor="#BFBFFF"
             type="rectangle" x="170" y="1018" width="45" height="17"/>
        </entry>
       */
      
      // Initialize species object
      Species spec = new Species(level,version); // id?, level, version
      spec.initDefaults();
      spec.setCompartment(compartment); //spec.setId("s_" + entry.getId());
      spec.appendNotes(String.format("<a href=\"%s\">Description</a><br>\n", entry.getLink()));
      
      // Set SBO Term
      if (treatEntrysWithReactionDifferent && entry.getReaction()!=null && !entry.getReaction().trim().isEmpty()) {
        spec.setSBOTerm(ET_SpecialReactionCase2SBO);
        // TODO: ...
        Reaction r = new Reaction(level, version);
        r.setId("xyz");
        ModifierSpeciesReference modifier = new ModifierSpeciesReference(spec);
        modifier.setSBOTerm(461);
        r.addModifier(modifier);
       
        // TODO: Obiges nicht richtig.
      } else {
        spec.setSBOTerm(getSBOTerm(entry.getType()));
      }
      
      boolean isPathwayReference=false;
      String name = entry.getName().trim();
      if (name.toLowerCase().startsWith("path:") ||entry.getType().equals(EntryType.map)) isPathwayReference=true;
      
      boolean hasMultipleIDs = false;
      if (entry.getName().trim().contains(" ")) hasMultipleIDs = true;
      
      // Process graphics information
      if (entry.hasGraphics()) {
         /*    <entry id="16" name="ko:K04467 ko:K07209 ko:K07210" type="ortholog">
                 <graphics name="IKBKA..." fgcolor="#000000" bgcolor="#FFFFFF"
                   type="rectangle" x="785" y="141" width="45" height="17"/>
                   
         *   ... is actually a compund!?!?
         */
        
        // Get name, description and other annotations via api (organism specific) possible!!
        Graphics g = entry.getGraphics();
        if (!g.getName().isEmpty())
          name = g.getName(); // + " (" + name + ")"; // Append ko Id(s) possible!
        
        // TODO: CellDesignerAnnotation
        spec.getAnnotation().appendNoRDFAnnotation("");
      }

      // Parse every gene/object in this node.
      CVTerm cvtKGID = new CVTerm();     
      cvtKGID.setBiologicalQualifierType(Qualifier.BQB_IS);
      for (String ko_id:entry.getName().split(" ")) {
        if (ko_id.trim().equalsIgnoreCase("undefined")) continue;
        
        // Add Kegg-id Miriam identifier
        if (ko_id.trim().toLowerCase().startsWith("cpd:")) {
          cvtKGID.addResource("urn:miriam:kegg.compound" + ko_id.substring(ko_id.indexOf(':')).trim());
        } else if (ko_id.trim().toLowerCase().startsWith("glycan:")) {
          cvtKGID.addResource("urn:miriam:kegg.glycan" + ko_id.substring(ko_id.indexOf(':')).trim());
        } else if (ko_id.trim().toLowerCase().startsWith("ec:")) {
          cvtKGID.addResource("urn:miriam:ec-code" + ko_id.substring(ko_id.indexOf(':')).trim());
        } else if (ko_id.trim().toLowerCase().startsWith("dr:")) {
          cvtKGID.addResource("urn:miriam:kegg.drug" + ko_id.substring(ko_id.indexOf(':')).trim());
        } else if (ko_id.trim().toLowerCase().startsWith("path:")) { // Link to another pathway
          cvtKGID.addResource("urn:miriam:kegg.pathway" + ko_id.substring(ko_id.indexOf(':')).trim());
        } else if ((entry.getType().equals(EntryType.gene) || entry.getType().equals(EntryType.ortholog)) ) {// z.B. hsa:00123, ko:00123
          cvtKGID.addResource("urn:miriam:kegg.genes:" + ko_id.trim());
        } else {
          System.err.println("Please implement MIRIAM urn for: '" + ko_id + "' (" + entry.getType().toString() + ").");
        }
        
        // Retrieve further information via Kegg API
        CVTerm cvtEgID = new CVTerm(); cvtEgID.setBiologicalQualifierType(Qualifier.BQB_IS);
        CVTerm cvtOmimID = new CVTerm(); cvtOmimID.setBiologicalQualifierType(Qualifier.BQB_IS);
        CVTerm cvtHgncID = new CVTerm(); cvtHgncID.setBiologicalQualifierType(Qualifier.BQB_IS);
        CVTerm cvtEnsemblID = new CVTerm(); cvtEnsemblID.setBiologicalQualifierType(Qualifier.BQB_IS);
        CVTerm cvtUniprotID = new CVTerm(); cvtUniprotID.setBiologicalQualifierType(Qualifier.BQB_IS);
        if (adap!=null) { // Be careful: very slow!
          String infos = adap.get(ko_id);
          
          // Set name to real and human-readable name.
          if (!hasMultipleIDs) {
            name = KeggAdaptor.extractInfo(infos, "NAME", "\n").trim();
          }
          
          spec.getAnnotation().appendNoRDFAnnotation(String.format("Description for %s: %s\n", KeggAdaptor.extractInfo(infos, "NAME"),KeggAdaptor.extractInfo(infos, "DEFINITION")));
          
          // Parse "NCBI-GeneID:","UniProt:", "Ensembl:", ...
          String text;
          if (infos!=null && infos.length()>0) {
            text = KeggAdaptor.extractInfo(infos, "Ensembl:", "\n");
            if (text!=null && !text.isEmpty()) cvtEnsemblID.addResource("urn:miriam:ensembl:" + text.trim());
            text = KeggAdaptor.extractInfo(infos, "UniProt:", "\n");
            if (text!=null && !text.isEmpty()) cvtUniprotID.addResource("urn:miriam:ensembl:" + text.trim());
            text = KeggAdaptor.extractInfo(infos, "HGNC:", "\n");
            if (text!=null && !text.isEmpty()) cvtHgncID.addResource("urn:miriam:ensembl:" + text.trim());
            text = KeggAdaptor.extractInfo(infos, "OMIM:", "\n");
            if (text!=null && !text.isEmpty()) cvtOmimID.addResource("urn:miriam:ensembl:" + text.trim());
            text = KeggAdaptor.extractInfo(infos, "NCBI-GeneID:", "\n");
            if (text!=null && !text.isEmpty()) cvtEgID.addResource("urn:miriam:ensembl:" + text.trim());
          }
        }
        if (cvtEgID.getNumResources()>0) spec.addCVTerm(cvtEgID);
        if (cvtOmimID.getNumResources()>0) spec.addCVTerm(cvtOmimID);
        if (cvtHgncID.getNumResources()>0) spec.addCVTerm(cvtHgncID);
        if (cvtEnsemblID.getNumResources()>0) spec.addCVTerm(cvtEnsemblID);
        if (cvtUniprotID.getNumResources()>0) spec.addCVTerm(cvtUniprotID);
      }
      if (cvtKGID.getNumResources()>0) spec.addCVTerm(cvtKGID);

      // Finally, add the fully configured species.
      spec.setName(name);
      spec.setId(NameToSId(name));
      model.addSpecies(spec);
      entry.setCustom(spec); // Remember node in KEGG Structure for further references.
    }
    
    
    
    
    
  }

  private static boolean containsOnlyDigits(String myString) {
    char[] ch = myString.toCharArray();
    for (char c:ch)
      if (!Character.isDigit(c)) return false;
    return true;
  }

  private static String NameToSId(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public static int ET_SpecialReactionCase2SBO = 461; // 461="enzymatic catalyst"
  public static boolean treatEntrysWithReactionDifferent=true;
  
  public static int ET_Ortholog2SBO = 243; // 243="gene", 404="unit of genetic information"
  public static int ET_Enzyme2SBO = 14; // 14="Enzyme", 252="polypeptide chain"
  public static int ET_Gene2SBO = 243; // 243="gene"
  public static int ET_Group2SBO = 253; // 253="non-covalent complex"
  public static int ET_Compound2SBO = 247; // 247="Simple Molecule"
  public static int ET_Map2SBO = 291; // 291="Empty set"
  public static int ET_Other2SBO = 285; // 285="material entity of unspecified nature"
  private static int getSBOTerm(EntryType type) {
    if (type.equals(EntryType.compound)) return ET_Compound2SBO;
    if (type.equals(EntryType.enzyme)) return ET_Enzyme2SBO;
    if (type.equals(EntryType.gene)) return ET_Gene2SBO;
    if (type.equals(EntryType.group)) return ET_Group2SBO;
    if (type.equals(EntryType.map)) return ET_Map2SBO;
    if (type.equals(EntryType.ortholog)) return ET_Ortholog2SBO;
    
    if (type.equals(EntryType.other)) return ET_Compound2SBO;
    return ET_Compound2SBO;
  }
  
}
