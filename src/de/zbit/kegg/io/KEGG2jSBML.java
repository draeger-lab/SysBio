package de.zbit.kegg.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.sbml.jsbml.Annotation;
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
import org.sbml.jsbml.CVTerm.Type;

import y.view.Graph2D;

import de.zbit.kegg.KeggAdaptor;
import de.zbit.kegg.KeggInfoManagement;
import de.zbit.kegg.KeggInfos;
import de.zbit.kegg.parser.KeggParser;
import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Graphics;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.util.InfoManagement;
import de.zbit.util.ProgressBar;

public class KEGG2jSBML {
  public static boolean retrieveKeggAnnots=true; // Retrieve annotations from Kegg or use purely information available in the document.
  
  private KeggInfoManagement manager;
  private ArrayList<String> SIds = new ArrayList<String>();
  
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
        // TODO: Load info manager from fileSystem and give the manager to KEGG2jSBML as argument.
        //Example: InfoManagement<String, String> manager = (InfoManagement<String, String>) KeggInfoManagement.loadFromFilesystem(filepath);
        KEGG2jSBML k2s = new KEGG2jSBML();
        k2s.KEGG2SBML(p, outfile);
        // TODO: Save info manager to fileSystem. Example: InfoManagement.saveToFilesystem(filepath, manager);
      }
      return;
    }
    
    KEGG2jSBML k2s = new KEGG2jSBML(); // TODO: s.o.
    k2s.KEGG2SBML("src/de/zbit/kegg/samplefiles/hsa00010.xml", "src/de/zbit/kegg/samplefiles/hsa00010.sbml.xml");
  }
  
  public KeggInfoManagement getKeggInfoManager(){
    return manager;
  }
  
  public KEGG2jSBML(KeggInfoManagement manager) {
    this.manager = manager;
  }
  public KEGG2jSBML() {
    this(new KeggInfoManagement(1000,new KeggAdaptor()));
  }
  
  public SBMLDocument Kegg2jSBML(String filepath) {
    //System.out.println("Reading kegg pathway...");
    Pathway p = KeggParser.parse(filepath).get(0);
    
    //System.out.println("Converting to SBML");
    SBMLDocument doc = Kegg2jSBML(p);
    
    return doc;
  }
  
  public void KEGG2SBML(Pathway p, String outfile) {
    SBMLDocument doc = Kegg2jSBML(p);
    
    // TODO: JSBML IO => write doc to outfile.
  }
  
  public void KEGG2SBML(String infile, String outfile) {
    SBMLDocument doc = Kegg2jSBML(infile);
    
    // TODO: JSBML IO => write doc to outfile.
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
  
  public SBMLDocument Kegg2jSBML(Pathway p) {
    int level=2; int version=4;
    SBMLDocument doc = new SBMLDocument(level,version);
    //ArrayList<String> PWReferenceNodeTexts = new ArrayList<String>(); 
    if (!retrieveKeggAnnots) KeggInfoManagement.offlineMode=true; else KeggInfoManagement.offlineMode=false;
    SIds = new ArrayList<String>(); // Reset list of given SIDs. These are being remembered to avoid double ids.
      
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
    model.setAnnotation(new Annotation());
    model.setModelHistory(hist);
    
    // Parse Kegg Pathway information
    CVTerm mtPwID = new CVTerm(); mtPwID.setQualifierType(Type.MODEL_QUALIFIER);
    mtPwID.setModelQualifierType(Qualifier.BQM_IS);
    mtPwID.addResource(KeggInfos.getMiriamURIforKeggID(p.getName())); // same as "urn:miriam:kegg.pathway" + p.getName().substring(p.getName().indexOf(":"))
    
    model.addCVTerm(mtPwID);
    
    // Retrieve further information via Kegg Adaptor
    KeggInfos orgInfos = new KeggInfos("GN:" + p.getOrg(), manager); // Retrieve all organism information via KeggAdaptor
    if (orgInfos.queryWasSuccessfull()) {
      CVTerm mtOrgID = new CVTerm();  mtOrgID.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
      mtOrgID.setBiologicalQualifierType(Qualifier.BQB_OCCURS_IN);
      appendAllIds(orgInfos.getTaxonomy(), mtOrgID, KeggInfos.miriam_urn_taxonomy);
      model.addCVTerm(mtOrgID);
      
      model.appendNotes(String.format("<h1>Model of %s in %s</h1>\n", p.getTitle(), orgInfos.getDefinition() ));
    } else {
      model.appendNotes(String.format("<h1>Model of %s</h1>\n", p.getTitle() ));
    }
    
    // Get PW infos from KEGG Api for Description and GO ids.
    KeggInfos pwInfos =  new KeggInfos(p.getName(), manager); // NAME, DESCRIPTION, DBLINKS verwertbar
    if (pwInfos.queryWasSuccessfull()) {
      model.appendNotes(String.format("%s<br>\n", pwInfos.getDescription() ));
      
      // GO IDs
      if (pwInfos.getGo_id()!=null) {
        CVTerm mtGoID = new CVTerm(); mtGoID.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
        mtGoID.setBiologicalQualifierType(Qualifier.BQB_IS_VERSION_OF);
        appendAllGOids(pwInfos.getGo_id(), mtGoID);
        if (mtGoID.getNumResources()>0) model.addCVTerm(mtGoID);
      }
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
      spec.setAnnotation(new Annotation("")); // manchmal ist jSBML schon bescheurt...
      spec.appendNotes(String.format("<a href=\"%s\">Original Kegg Entry</a><br>\n", entry.getLink()));
      
      // Set SBO Term
      if (treatEntrysWithReactionDifferent && entry.getReaction()!=null && !entry.getReaction().trim().isEmpty()) {
        spec.setSBOTerm(ET_SpecialReactionCase2SBO);
        // TODO: ... Beispiel um zu verdeutlich wie das mit reaktionen gehen soll. Muss natürlich gelöscht, gemerkt und später realisiert werden.
        /*Reaction r = new Reaction(level, version);
        r.setId("xyz");
        ModifierSpeciesReference modifier = new ModifierSpeciesReference(spec);
        modifier.setSBOTerm(461);
        r.addModifier(modifier);*/
       
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
        spec.getAnnotation().appendNoRDFAnnotation("SpecisAnnotationForCellDesigner.");
      }
      
      CVTerm cvtKGID = new CVTerm(); cvtKGID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtKGID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtEntrezID = new CVTerm(); cvtEntrezID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtEntrezID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtOmimID = new CVTerm(); cvtOmimID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtOmimID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtHgncID = new CVTerm(); cvtHgncID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtHgncID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtEnsemblID = new CVTerm(); cvtEnsemblID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtEnsemblID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtUniprotID = new CVTerm(); cvtUniprotID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtUniprotID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtChebiID = new CVTerm(); cvtChebiID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtChebiID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtDrugbankID = new CVTerm(); cvtDrugbankID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtDrugbankID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtGoID = new CVTerm(); cvtGoID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtGoID.setBiologicalQualifierType(Qualifier.BQB_IS_VERSION_OF);
      CVTerm cvtHGNCID = new CVTerm(); cvtHGNCID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtHGNCID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtPubchemID = new CVTerm(); cvtPubchemID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtPubchemID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvt3dmetID = new CVTerm(); cvt3dmetID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvt3dmetID.setBiologicalQualifierType(Qualifier.BQB_IS);
      CVTerm cvtReactionID = new CVTerm(); cvtReactionID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtReactionID.setBiologicalQualifierType(Qualifier.BQB_IS_DESCRIBED_BY);
      CVTerm cvtTaxonomyID = new CVTerm(); cvtTaxonomyID.setQualifierType(Type.BIOLOGICAL_QUALIFIER); cvtTaxonomyID.setBiologicalQualifierType(Qualifier.BQB_OCCURS_IN);
      
      // Parse every gene/object in this node.
      for (String ko_id:entry.getName().split(" ")) {
        if (ko_id.trim().equalsIgnoreCase("undefined")) continue;
        
        // Add Kegg-id Miriam identifier
        cvtKGID.addResource(KeggInfos.getMiriamURIforKeggID(ko_id, entry.getType()));
        
        // Retrieve further information via Kegg API -- Be careful: very slow!
        KeggInfos infos = new KeggInfos(ko_id, manager);
        if (infos.queryWasSuccessfull()) {
          
          // Set name to real and human-readable name.
          if (!hasMultipleIDs) {
            name = infos.getName();
          }
          
          // HTML Information
          spec.appendNotes(String.format("<p><b>Description for %s:</b> %s</p>\n", infos.getName(),infos.getDefinition()));
          if (infos.containsMultipleNames()) spec.appendNotes(String.format("<p><b>All given names:</b> %s</p>\n", infos.getNames()));
          if (infos.getCas()!=null) spec.appendNotes(String.format("<p><b>CAS number:</b> %s</p>\n", infos.getCas()));
          if (infos.getFormula()!=null) spec.appendNotes(String.format("<p><b>Formula:</b> %s</p>\n", infos.getFormula()));
          if (infos.getMass()!=null) spec.appendNotes(String.format("<p><b>Mass:</b> %s</p>\n", infos.getMass()));
          if (infos.containsMultipleNames()) spec.appendNotes(String.format("<p><b>All given names:</b> %s</p>\n", infos.getNames()));
          
          if (infos.getCas()!=null) spec.getAnnotation().appendNoRDFAnnotation(String.format(    "CAS number: %s\n", infos.getCas()));
          if (infos.getFormula()!=null) spec.getAnnotation().appendNoRDFAnnotation(String.format("Formula:    %s\n", infos.getFormula()));
          if (infos.getMass()!=null) spec.getAnnotation().appendNoRDFAnnotation(String.format(   "Mass:       %s\n", infos.getMass()));
          
          // Parse "NCBI-GeneID:","UniProt:", "Ensembl:", ...
          if (infos.getEnsembl_id()!=null) appendAllIds(infos.getEnsembl_id(), cvtEnsemblID, KeggInfos.miriam_urn_ensembl);
          if (infos.getChebi()!=null) appendAllIds(infos.getChebi(), cvtChebiID, KeggInfos.miriam_urn_chebi, "CHEBI:");
          if (infos.getDrugbank()!=null) appendAllIds(infos.getDrugbank(), cvtDrugbankID, KeggInfos.miriam_urn_drugbank);
          if (infos.getEntrez_id()!=null) appendAllIds(infos.getEntrez_id(), cvtEntrezID, KeggInfos.miriam_urn_entrezGene);
          if (infos.getGo_id()!=null) appendAllGOids(infos.getGo_id(), cvtGoID);
          if (infos.getHgnc_id()!=null) appendAllIds(infos.getHgnc_id(), cvtHGNCID, KeggInfos.miriam_urn_hgnc, "HGNC:");
          
          if (infos.getOmim_id()!=null) appendAllIds(infos.getOmim_id(), cvtOmimID, KeggInfos.miriam_urn_omim);
          if (infos.getPubchem()!=null) appendAllIds(infos.getPubchem(), cvtPubchemID, KeggInfos.miriam_urn_PubChem_Substance);
          
          if (infos.getThree_dmet()!=null) appendAllIds(infos.getThree_dmet(), cvt3dmetID, KeggInfos.miriam_urn_3dmet);
          if (infos.getUniprot_id()!=null) appendAllIds(infos.getUniprot_id(), cvtUniprotID, KeggInfos.miriam_urn_uniprot);
          
          if (infos.getReaction_id()!=null) appendAllIds(infos.getReaction_id(), cvtReactionID, KeggInfos.miriam_urn_kgReaction);
          if (infos.getTaxonomy()!=null) appendAllIds(infos.getTaxonomy(), cvtTaxonomyID, KeggInfos.miriam_urn_taxonomy);
        }
        
        
      }
      // Add all non-empty ressources.
      if (cvtKGID.getNumResources()>0) spec.addCVTerm(cvtKGID);
      if (cvtEntrezID.getNumResources()>0) spec.addCVTerm(cvtEntrezID);
      if (cvtOmimID.getNumResources()>0) spec.addCVTerm(cvtOmimID);
      if (cvtHgncID.getNumResources()>0) spec.addCVTerm(cvtHgncID);
      if (cvtEnsemblID.getNumResources()>0) spec.addCVTerm(cvtEnsemblID);
      if (cvtUniprotID.getNumResources()>0) spec.addCVTerm(cvtUniprotID);
      if (cvtChebiID.getNumResources()>0) spec.addCVTerm(cvtChebiID);
      if (cvtDrugbankID.getNumResources()>0) spec.addCVTerm(cvtDrugbankID);
      if (cvtGoID.getNumResources()>0) spec.addCVTerm(cvtGoID);
      if (cvtHGNCID.getNumResources()>0) spec.addCVTerm(cvtHGNCID);
      if (cvtPubchemID.getNumResources()>0) spec.addCVTerm(cvtPubchemID);
      if (cvt3dmetID.getNumResources()>0) spec.addCVTerm(cvt3dmetID);
      if (cvtReactionID.getNumResources()>0) spec.addCVTerm(cvtReactionID);
      if (cvtTaxonomyID.getNumResources()>0) spec.addCVTerm(cvtTaxonomyID);
      
      
      // Finally, add the fully configured species.
      spec.setName(name);
      spec.setId(NameToSId(name));
      model.addSpecies(spec);
      entry.setCustom(spec); // Remember node in KEGG Structure for further references.
    }
    
    
    return doc;
  }

  private static boolean containsOnlyDigits(String myString) {
    char[] ch = myString.toCharArray();
    for (char c:ch)
      if (!Character.isDigit(c)) return false;
    return true;
  }
  
  private static void appendAllGOids(String goIDs, CVTerm mtGoID) {
    for (String go_id:goIDs.split(" ")) {
      if (go_id.length()!=7 || !containsOnlyDigits(go_id)) continue; // Invalid GO id.
      mtGoID.addResource(KeggInfos.getGo_id_with_MiriamURN(go_id));
    }
  }
  
  /**
   * Append all IDs with Miriam URNs to a CV term. Multiple IDs are separated by a space.
   * Only the part behind the ":" will be added (if an ID contains a ":").
   * @param IDs
   * @param myCVterm
   * @param miriam_URNPrefix
   */
  private static void appendAllIds(String IDs, CVTerm myCVterm, String miriam_URNPrefix) {
    for (String id:IDs.split(" ")) {
      myCVterm.addResource(miriam_URNPrefix + KeggInfos.suffix(id));
    }
  }
  
  /**
   * Append all IDs with Miriam URNs to a CV term. Multiple IDs are separated by a space.
   * All ids are required to contain a ":". If not,  mayContainDoublePointButAppendThisStringIfNot
   *  will be used. E.g. "[mayContainDoublePointButAppendThisStringIfNot]:[ID]" or [ID] if it contains ":".
   * @param IDs
   * @param myCVterm
   * @param miriam_URNPrefix
   * @param mayContainDoublePointButAppendThisStringIfNot
   */
  private static void appendAllIds(String IDs, CVTerm myCVterm, String miriam_URNPrefix, String mayContainDoublePointButAppendThisStringIfNot) {
    for (String id:IDs.split(" ")) {
      myCVterm.addResource( miriam_URNPrefix + (miriam_URNPrefix.contains(":")?miriam_URNPrefix.trim():mayContainDoublePointButAppendThisStringIfNot+":"+miriam_URNPrefix.trim()) );
    }
  }

  /**
   * Generates a valid SId from a given name. If the name already is a valid SId, the name is returned.
   * If the SId already exists in this document, "_<number>" will be appended and the next free number is
   * being assigned.
   * => See SBML L2V4 document for the Definition of SId. (Page 12/13)
   * @param name
   * @return SId
   */
  private String NameToSId(String name) {
    /* letter ::= ’a’..’z’,’A’..’Z’
     * digit ::= ’0’..’9’
     * idChar ::= letter | digit | ’_’
     * SId ::= ( letter | ’_’ ) idChar*
     */
    String ret = "";
    if (name==null || name.trim().isEmpty()) {
      ret = incrementSIdSuffix("SId");
      SIds.add(ret);
    } else {
      name = name.trim();
      char c = name.charAt(0);
      if (!(Character.isLetter(c) || c=='_')) ret = "SId_"; else ret = Character.toString(c);
      for (int i=1; i<name.length(); i++) {
        c = name.charAt(i);
        if (Character.isLetter(c) || Character.isDigit(c) || c=='_') ret+=Character.toString(c);
      }
      if (SIds.contains(ret)) ret = incrementSIdSuffix(ret);
    }
    
    return ret;
  }
  
  /**
   * Appends "_<Number>" to a given String. <Number> is being set to the next free number, so that
   * this sID is unique in this sbml document. Should only be called from "NameToSId".
   * @return
   */
  private String incrementSIdSuffix(String prefix) {
    int i=1;
    String aktString = prefix + "_" + i;
    while (SIds.contains(aktString)) {
      aktString = prefix + "_" + (++i);
    }
    return aktString;
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
