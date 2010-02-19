package de.zbit.kegg;

/**
 * Erleichert das parsen der KeggInfos vom Adapter.
 * @author wrzodek
 */
public class KeggInfos {
  private KeggAdaptor adap;
  private String Kegg_ID;
  
  private String informationFromKeggAdaptor;
  
  private String taxonomy=null;
  private String definition=null;
  private String description=null;
  private String names = null; // multiple synonyms separated by ;
  private String name = null; // Last entry (mostly the most meaningful) in names.
  private String go_id = null; // Be careful: multiple numbers without "GO:". eg "0006096 0006094" instead of "GO:0006096"
  private String reaction_id = null; // Be careful: multiple numbers without "RN:". eg "R05966 R05967"
  private String ensembl_id=null;
  private String uniprot_id=null;
  private String hgnc_id=null;
  private String omim_id=null;
  private String entrez_id=null;
  private String formula=null;
  private String mass=null;
  private String pubchem=null; //urn:miriam:pubchem.compound urn:miriam:pubchem.substance urn:miriam:pubchem.bioassay
  private String chebi=null; //urn:miriam:obo.chebi // be careful! E.g. CHEBI:36927
  private String three_dmet=null; //urn:miriam:3dmet
  private String cas=null; // NOT IN MIRIAM :( "CAS registry number, unique numerical identifiers for chemical substances"
  private String drugbank=null; //urn:miriam:drugbank
  
  //TODO: Getters and setter and isAvailable and GetMiriam and InfoRetrivalManagement.
  
  
  public KeggInfos(String Kegg_ID) {
    this(Kegg_ID, new KeggAdaptor());
  }
  public KeggInfos(String Kegg_ID, KeggAdaptor adap) {
    this.Kegg_ID = Kegg_ID;
    this.adap = adap;
    
    informationFromKeggAdaptor = adap.get(Kegg_ID);
    parseInfos();
  }
  
  private void parseInfos() {
    String infos = informationFromKeggAdaptor; // Create a shorter variable name ;-)
    if (infos.trim().isEmpty()) infos=null;
    if (infos==null) {
      informationFromKeggAdaptor = null;
      return;
    }
    
    // General
    names = KeggAdaptor.extractInfo(infos, "NAME");
    if (names!=null && !names.isEmpty()) {
      int pos = names.lastIndexOf(";");
      if (pos>0 && pos<(names.length()-1)) name = names.substring(pos+1, names.length()).replace("\n", "").trim();
      else name = names;
    }
    definition = KeggAdaptor.extractInfo(infos, "DEFINITION");
    description=KeggAdaptor.extractInfo(infos, "DESCRIPTION");
    
    // Mainly Pathway specific (eg. "path:map00603")
    go_id=KeggAdaptor.extractInfo(infos, " GO:", "\n"); // DBLINKS     GO: 0006096 0006094
    
    // Mainly Organism specific (eg. "GN:hsa")
    taxonomy = KeggAdaptor.extractInfo(infos, "TAXONOMY", "\n"); // e.g. "TAXONOMY    TAX:9606" => "TAX:9606".
    
    // Mainly Gene specific (eg. "hsa:12313")
    ensembl_id=KeggAdaptor.extractInfo(infos, "Ensembl:", "\n");
    uniprot_id=KeggAdaptor.extractInfo(infos, "UniProt:", "\n");
    hgnc_id=KeggAdaptor.extractInfo(infos, "HGNC:", "\n");
    omim_id=KeggAdaptor.extractInfo(infos, "OMIM:", "\n");
    entrez_id=KeggAdaptor.extractInfo(infos, "NCBI-GeneID:", "\n");
    
    // Mainly Glycan specific (eg. "glycan:G00181")
    /*
     * Sadly non of the following has a miriam URN:
     *  CCSD (CarbBank) : 1303
            GlycomeDB: 12885
            JCGGDB: JCGG-STR003128
            LipidBank: GSG1005
     */
    
    // Mainly Enzyme specific (eg. "ec:2.4.1.-  ")
    /* Mostly uninteresting...
     * Product, Substrate, REACTION, Class
     */
    
    // Ortholog (e.g. "ko:K01204")
    // DBLINKS (RN, GO); GENES (actual orthologous genes) //urn:miriam:kegg.reaction (R00100)
    reaction_id=KeggAdaptor.extractInfo(infos, " RN:", "\n"); // DBLINKS     RN: R05966
    
    // in small molecules (compound eg. "cpd:C00031")
    // KNApSAcK, NIKKAJI, (CAS) missing
    formula = KeggAdaptor.extractInfo(infos, "FORMULA"); //FORMULA     C6H12O6
    mass=KeggAdaptor.extractInfo(infos, "MASS"); // MASS        180.0634
    
    pubchem=KeggAdaptor.extractInfo(infos, "PubChem:", "\n");
    chebi=KeggAdaptor.extractInfo(infos, "ChEBI:", "\n");
    three_dmet=KeggAdaptor.extractInfo(infos, "3DMET:", "\n");
    cas=KeggAdaptor.extractInfo(infos, " CAS:", "\n");
    
    // Mainly drg (eg. "dr:D00694")
    // missing: NIKKAJI, LigandBox (CAS)
    drugbank=KeggAdaptor.extractInfo(infos, "DrugBank:", "\n");
    
    // Free Memory instead of storing empty Strings.
    if (taxonomy.trim().isEmpty()) taxonomy=null;
    if (definition.trim().isEmpty()) definition=null;
    if (description.trim().isEmpty()) description=null;
    if (names.trim().isEmpty()) names=null;
    if (name.trim().isEmpty()) name=null;
    if (go_id.trim().isEmpty()) go_id=null;
    if (reaction_id.trim().isEmpty()) reaction_id=null;
    if (ensembl_id.trim().isEmpty()) ensembl_id=null;
    if (uniprot_id.trim().isEmpty()) uniprot_id=null;
    if (hgnc_id.trim().isEmpty()) hgnc_id=null;
    if (omim_id.trim().isEmpty()) omim_id=null;
    if (entrez_id.trim().isEmpty()) entrez_id=null;
    if (formula.trim().isEmpty()) formula=null;
    if (mass.trim().isEmpty()) mass=null;
    if (chebi.trim().isEmpty()) chebi=null;
    if (three_dmet.trim().isEmpty()) three_dmet=null;
    if (cas.trim().isEmpty()) cas=null;
    if (drugbank.trim().isEmpty()) drugbank=null;
  }
  
  
  
}
