/**
 * 
 */
package de.zbit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import keggapi.Definition;
import de.zbit.cache.InfoManagement;
import de.zbit.exception.CorruptInputStreamException;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.io.csv.CSVReader;
import de.zbit.io.csv.CSVwriteable;
import de.zbit.io.csv.CSVwriteableIO;
import de.zbit.kegg.api.cache.KeggFunctionManagement;
import de.zbit.kegg.api.cache.KeggQuery;
import de.zbit.resources.Resource;
import de.zbit.util.progressbar.ProgressBar;

/**
 * 
 * This class combines UniProt and KEGG identifiers,
 * i.e. KEGG abbreviation, scientific name, UniProt extension of proteins, common name
 * Example: "hsa", "Homo sapiens", "_HUMAN", "Human"
 * 
 * TODO: Use new speclist txt file and change corresponding methods in this class.
 * TODO: Return most important organisms first (e.g., don't return a rare mouse strain
 * TODO: when user queries mmu).
 * 
 * @author Finja B&uml;chel
 * @author Clemens Wrzodek
 */
public class Species implements Serializable, Comparable<Object>, CSVwriteable, ActionCommand {
  private static final long serialVersionUID = 5900817226349012280L;
  
  public static final Logger log = Logger.getLogger(Species.class.getName());
  
  public final static int SCIENTIFIC_NAME = 0;
  public final static int COMMON_NAME = 1;
  public final static int KEGG_ABBR = 2;
  public final static int UNIPROT_EXTENSION = 3;
  public final static int NCBI_TAX_ID = 4;
  
  private String keggAbbr;
  private String scientificName;
  private String uniprotExtension;
  private String commonName;
  
  private List<String> synonyms;
  
  
  /**
   * Official NCBI Species id (which is an int)
   * http://www.ncbi.nlm.nih.gov/sites/entrez?db=taxonomy
   * tax_id -- the id of node associated with this name
   */
  Integer ncbi_tax_id=null;  
  
  /**
   * 
   * @param scientificName
   * @param uniprotExtension
   * @param commonName
   * @param synonyms
   */
  public Species(String scientificName, String uniprotExtension, String commonName, List<String> synonyms) {
    this(scientificName);
    this.uniprotExtension = uniprotExtension;
    this.commonName = commonName;
    this.synonyms = synonyms;
  }
  
  /**
   * @param keggAbbr
   * @param scientificName
   * @param uniprotExtension
   */
  public Species(String keggAbbr, String scientificName, String uniprotExtension) {
    this(keggAbbr, scientificName, uniprotExtension, null, (String)null);
  }
  
  /**
   * @param keggAbbr
   * @param scientificName
   * @param uniprotExtension
   * @param commonName
   * @param synonym
   */
  public Species(String keggAbbr, String scientificName, String uniprotExtension, String commonName, String synonym) {
    this(scientificName);
    this.keggAbbr = keggAbbr;
    this.uniprotExtension = uniprotExtension;
    this.commonName = commonName;
    addSynonym(synonym);
  }

  /**
   * @param scientificName2
   */
  public Species(String scientificName) {
    super();
    this.scientificName = scientificName;
  }

  /**
   * @param scientific
   * @param uniprot
   * @param common
   * @param syns
   * @param ncbi_taxon_id
   */
  public Species(String scientificName, String uniprotExtension, String commonName, List<String> synonyms, Integer ncbi_taxon_id) {
    this(scientificName, uniprotExtension, commonName, synonyms);
    this.ncbi_tax_id = ncbi_taxon_id;
  }

  /**
   * 
   * @param scientificName
   * @param uniprotExtension
   * @param commonName
   * @param keggAbbr
   * @param ncbi_taxon_id
   */
  public Species(String scientificName, String uniprotExtension, String commonName, String keggAbbr, int ncbi_taxon_id) {
    this(scientificName, uniprotExtension, commonName, null);
    this.keggAbbr = keggAbbr;
    this.ncbi_tax_id = ncbi_taxon_id;
  }

  /**
   * 
   * @param synonym
   * @return as specified by {@link Collection#add(Object)}
   */
  public boolean addSynonym(String synonym) {
    if (synonym!=null && (synonym.trim().length())>0) {
      if (this.synonyms==null) synonyms = new LinkedList<String>();
      if (!synonyms.contains(synonym)) {
        return synonyms.add(synonym);
      }
    }
    return false;
  }
  
  public List<String> getSynonyms() {
    return this.synonyms;
  }



  /**
   * @return the kegg abbreviation
   */
  public String getKeggAbbr() {
    return keggAbbr;
  }
  
  /**
   * @return the ncbi_taxon_id or null if it is known / not set.
   */
  public Integer getNCBITaxonID() {
    return ncbi_tax_id;
  }



  /**
   * @param shortName the shortName to set
   */
  public void setShortName(String keggAbbr) {
    this.keggAbbr = keggAbbr;
  }



  /**
   * @return the scientificName
   */
  public String getScientificName() {
    return scientificName;
  }

  /**
   * @param scientificName the scientificName to set
   */
  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }
  
  /**
   * @return the uniprotExtension
   */
  public String getUniprotExtension() {
    return uniprotExtension;
  }

  /**
   * @param uniprotExtension the uniprotExtension to set
   */
  public void setUniprotExtension(String uniprotExtension) {
    this.uniprotExtension = uniprotExtension;
  }

  /**
   * @return the commonName
   */
  public String getCommonName() {
    return commonName;
  }
  
  /**
   * The name that is used by ensembl flat files and
   * biomart queries.
   * <p>For "Homo sapiens" this is "Hsapiens".
   * Analogue for other species.</p>
   * @return
   */
  public String getEnsemblName() {
    int pos = scientificName.indexOf(' ');
    if (pos<0) return scientificName;
    else {
      String ret = scientificName.charAt(0) + scientificName.substring(pos+1).trim();
      if (ret.contains(" ")) {
        ret = ret.substring(0, ret.indexOf(' '));
      }
      return ret;
    }
  }

  /**
   * @param commonName the commonName to set
   */
  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public int compareTo(Object o) {
    if (o instanceof Species){
      return scientificName.toLowerCase().compareTo(((Species)o).scientificName.toLowerCase());
    } else
      return scientificName.toLowerCase().compareTo(o.toString().toLowerCase());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    Species newSpecies = new Species(this.scientificName);
    
    newSpecies.keggAbbr = keggAbbr;
    newSpecies.uniprotExtension = uniprotExtension;
    newSpecies.commonName = commonName;
    newSpecies.ncbi_tax_id = ncbi_tax_id;
    newSpecies.keggAbbr = keggAbbr;
    newSpecies.keggAbbr = keggAbbr;
    if (synonyms==null) {
      newSpecies.synonyms = null;
    } else {
      for (String sym: synonyms)
        newSpecies.addSynonym(sym);
    }

    return newSpecies;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Species) {
      if (((Species) o).isSetTaxonomyId() && isSetTaxonomyId()) {
        // most precise is tax id.
        return ((Species) o).ncbi_tax_id.equals(ncbi_tax_id); 
      }
      if (((Species) o).scientificName.toLowerCase().equals(scientificName.toLowerCase()))
        return true;
      else
        return false;
    }
    else {
      return false;
    }
  }
  
  /**
   * @return true if a valid {@link #ncbi_tax_id} is set.
   */
  public boolean isSetTaxonomyId() {
    return ncbi_tax_id!=null && ncbi_tax_id>0;
  }

  public static List<String> getListOfNames(List<Species> list, int type) {
    List<String> retval = new LinkedList<String>();
    for (int i=0; i<list.size(); i++) {
      retval.add(list.get(i).getName(type));
    }
    return retval;
  }

  /**
   * @param list
   * @param biopaxRDFid
   * @param type
   */
  public String getName(int type) {
    if (type==SCIENTIFIC_NAME) {
      return getScientificName();
    } else if (type==COMMON_NAME) {
      return getCommonName();
    } else if (type==KEGG_ABBR) {
      return keggAbbr;
    } else if (type==UNIPROT_EXTENSION) {
      return uniprotExtension;
    } else {
      System.err.println("Unknown species name type: " + type);
      return null;
    }
  }
  
  /**
   *   
   * @param kegg identifier, i.e. "hsa"
   * @param br UniProtList uniprotSpeciesFile from the UniProt homepage under <a href="http://www.uniprot.org/docs/speclist">http://www.uniprot.org/docs/speclist</a>
   * @return species, if the species is not found it returns null
   */
  public static Species getSpeciesWithKEGGIDInList(String kegg, BufferedReader br){
    List<Species> speciesList = null;
    try {
      speciesList = Species.generateSpeciesDataStructure(br, true);
    } catch (IOException e) {
      System.err.printf("Error while generating species list", e);
      System.exit(0);
    }
    
    Species sp = null;
    for (Species species : speciesList) {
      if(species.getKeggAbbr().equals(kegg.toLowerCase())){
        sp = species;
        break;
      }
    }
    
    return sp;
  }
  
  /**
   *   
   * @param kegg identifier, i.e. "hsa"
   * @param br UniProtList uniprotSpeciesFile from the UniProt homepage under <a href="http://www.uniprot.org/docs/speclist">http://www.uniprot.org/docs/speclist</a>
   * @return species, if the species is not found it returns null
   */
  public static Species getSpeciesWithTaxonomyIDInList(Integer tax, BufferedReader br, 
      boolean keggNecessary){
    List<Species> speciesList = null;
    try {
      speciesList = Species.generateSpeciesDataStructure(br, keggNecessary);
    } catch (IOException e) {
      System.err.printf("Error while generating species list", e);
      System.exit(0);
    }   
            
    return Species.search(speciesList, tax.toString(), NCBI_TAX_ID);
  }
  
  /**
   * 
   * @return a list with the species 
   *  
   */
  public static List<Species> generateSpeciesDataStructure() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(Resource.class.getResourceAsStream("speclist.txt")));
    List<Species> a = generateSpeciesDataStructure(in, true);
    return a;
  }
  
  /**
   * 
   * @param uniprotSpeciesFile
   * @return a list with the species 
   *  
   *  catches the IOException
   */
  public static List<Species> generateSpeciesDataStructureExcToSto(BufferedReader uniprotSpeciesFile) {
    try {
      return generateSpeciesDataStructure(uniprotSpeciesFile, true);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);// TODO: Is this good or exist something more convenient?
    }
    return new LinkedList<Species>();
  }
  
  /**
   * 
   * @param uniprotSpeciesFile from the UniProt homepage under <a href="http://www.uniprot.org/docs/speclist">http://www.uniprot.org/docs/speclist</a>
   * @return
   * @throws IOException
   */
  public static List<Species> generateSpeciesDataStructure(BufferedReader in,
      boolean keggNecessary) throws IOException {
    List<Species> allSpec = new SortedArrayList<Species>();
    
    
    if (in!=null) { // Read uniprot species taxon file.
      boolean startReading=false;
      String line;
      String uniprot = "";
      String scientificName = "";
      String commonName = "";
      Integer tax = null;
      List<String> syns = new ArrayList<String>();
      
      while ((line=in.readLine())!=null) {
        if (line.trim().length()<1) continue;
        else if (startReading) {          
          if (line.contains("====================================")) {
            startReading=false;
            break; // done reading.
          }
          
          //          Code    Taxon   N=Official (scientific) name
          //          Node    C=Common name
          //                  S=Synonym
          //          _____ _ _______ _____________________________________________________________
          //          AADNV V 648330: N=Aedes albopictus densovirus (isolate Boublik/1994)
          //                          C=AalDNV
          //          AAV2  V  10804: N=Adeno-associated virus 2
          //                          C=AAV-2
          //          AAV2S V 648242: N=Adeno-associated virus 2 (isolate Srivastava/1982)
          //                          C=AAV-2
          
          // Create the current species object, if a new entry starts here.
          uniprot = "_" + line.substring(0,6).trim();
          if(!uniprot.equals("_")) {                        
            // Add to list here, because the scientific name is required.
            if(!uniprot.isEmpty() && tax!=null && 
                !scientificName.isEmpty() &&!commonName.isEmpty()){
              Species s = new Species(scientificName, uniprot, commonName, null, tax.intValue());
              allSpec.add(s);
              
              uniprot = "";
              scientificName = "";
              commonName = "";
              tax = null;
              syns = new ArrayList<String>();
            }
          }

          // Taxon ID is always in line with uniprot species name
          String taxon_id = line.substring(7, 14).trim();
          if (taxon_id.length()>0) {
            tax = Integer.parseInt(taxon_id);
          }
          
          // Start at position 16 are Common-, Scientific Name and Synonyms.
          String help = line.substring(16, line.length());
          String[] split = help.split("=");
          if (split.length>=2){
            if(split[0].equals("N") && split[1]!=null && !split[1].isEmpty())
              scientificName = split[1].trim();
            else if (split[0].equals("C") && split[1]!=null && !split[1].isEmpty()){              
              commonName = split[1].trim();
            }
            else if (split[0].equals("S") && split[1]!=null && !split[1].isEmpty())
              syns.add(split[1].trim());  
          }           
        } else if (line.contains("____________________________________")) {
          startReading = true;
        }
      }
      in.close();
    }
    
   
    //KeggQuery.getOrganisms
    KeggFunctionManagement manag = null;      
    Definition[] keggOrgs = null;
    try {
      boolean fileExists = new File("kgFct.dat").exists();
      log.config("File kgFct.dat exists: " + fileExists);
      if (fileExists){
        manag = (KeggFunctionManagement) KeggFunctionManagement.loadFromFilesystem("kgFct.dat");
      }
    } catch (Throwable e) {
      log.log(Level.WARNING, "Error reading kgFct.dat", e);
    }
    if (manag==null) manag = new KeggFunctionManagement();
    
    KeggQuery q = new KeggQuery(KeggQuery.getOrganisms, null);
    keggOrgs = (Definition[]) manag.getInformation(q).getObject();
    
    if (keggOrgs!=null){
      for (Definition definition : keggOrgs) {
        // getDefinition() = e.g "Homo sapiens (human)"
        String scientificName  = definition.getDefinition();
        String commonName = null;
        int pos = scientificName.indexOf("(");
        if(pos>-1) {
          int pos2 = scientificName.indexOf(")",pos);
          if (pos2<0) pos2=scientificName.length();
          commonName = scientificName.substring(pos+1, pos2);
          
          scientificName = scientificName.substring(0,pos).trim();
        }
        
        // Search all species in allSpec for the current KEGG species.
        String keggAbbr = definition.getEntry_id();
        boolean contained = false;
        for(int i=0; i<allSpec.size(); i++){
          String scName = allSpec.get(i).getScientificName();
          
          // Search for the current species
          if (allSpec.get(i).getScientificName().equalsIgnoreCase(scientificName)) {
            contained=true;
          }
          
          // Complete missing kegg abbreviations
          if(scName.startsWith(scientificName)) {
            if (allSpec.get(i).getKeggAbbr()==null || allSpec.get(i).getKeggAbbr().length()<1) {
              /*
               * Sometimes leads to wrong annotations! E.g. 
               * [1] zmo - Zymomonas mobilis _ZYMMO    542 
               * [2] zmo - Zymomonas mobilis subsp. mobilis (strain NCIB 11163)  _ZYMMN    622759  
               * [3] zmn - Zymomonas mobilis subsp. mobilis NCIMB 11163
               * 2 & 3 are equal. Thus, the KeggAbbr. should be zmn not zmo!        
               */
              allSpec.get(i).setShortName(keggAbbr);
              contained=true;
            }
          }          
        }
        
        
        // Add not contained species.
        if (!contained) {
          String uniprot_ext = null;
          if (commonName!=null && commonName.trim().length()>0) uniprot_ext = '_'+commonName.trim().toUpperCase().replace(" ", "");
          Species new_spec = new Species(keggAbbr, scientificName, uniprot_ext, commonName, null);
          allSpec.add(new_spec);
        }
      }
    }
    
    
    // Sort and Filter list. Return only Species with KEGG Abbr.
    int counter = 0;
    List<Species> speciesWithoutKeggAbbr = new SortedArrayList<Species>();
    List<Species> speciesWithKeggAbbr = new SortedArrayList<Species>();
    if (keggNecessary){      
      for (Species species : allSpec) {
        System.out.println(species.getNCBITaxonID());
        if(species.getKeggAbbr()!=null){
          counter++;
          speciesWithKeggAbbr.add(species);
        }
        else{
          if (species.getNCBITaxonID().equals((Integer)6706))System.out.println("FUCK");
          speciesWithoutKeggAbbr.add(species);
        }
      }  
    } else {
      speciesWithKeggAbbr = allSpec;
    }
    
     if (manag.isCacheChangedSinceLastLoading()) {
      InfoManagement.saveToFilesystem("kgFct.dat", manag);
    }
    
    return speciesWithKeggAbbr;
  }
  
  @Override
  public int hashCode(){
    return scientificName.hashCode();
  }
  
  @Override
  public String toString() {
    return "[Species: " + scientificName + ']';
  }

  /**
   * Searches for a specific species in a list of species.
   * @param all
   * @param species
   * @param nameTypeToSearch - set to -1 to search in all types, else, one
   * of the included final static integers.
   * @return
   */
  public static Species search(List<Species> all, String species, int nameTypeToSearch) {
    if (all==null || species==null) return null;
    for (Species s: all) {
      if (s.matchesIdentifier(species, nameTypeToSearch)) return s;
    }
    return null;
  }

  /**
   * Searches for a specific species and retrieves a complete list
   * of species from KEGG.
   * @param species
   * @return
   * @throws IOException 
   */
  public static Species search(String species) throws IOException {
    return search(Species.generateSpeciesDataStructure(), species, -1);
  }
  
  /**
   * Get the given species
   * @see #search(List, String, int)
   * @param identifier String identifying the species (e.g. "human" or "homo sapiens" or "hsa)
   * @param nameTypeToSearch Identifier type to search (-1 for all, else one of the included
   * final static ints (e.g., {@link #COMMON_NAME}).
   * @return the species object representing the species, or null if not found.
   * @throws IOException
   */
  public static Species get(String identifier, int nameTypeToSearch) throws IOException {
    return search(Species.generateSpeciesDataStructure(), identifier, nameTypeToSearch);
  }

  /**
   * Checks all identifiers and returns true if one of them
   * equals (ignore case) the 'species' string.
   * @param species
   * @return
   */
  public boolean matchesIdentifier(String species) {
    return matchesIdentifier(species, -1);
  }
  
  /**
   * @param species
   * @param nameTypeToSearch - -1 to search all identifiers, else the name type
   * to search.
   * @return
   * @see #matchesIdentifier(String)
   */
  private boolean matchesIdentifier(String species, int nameTypeToSearch) {
    // Integer? => NCBI Taxonomy ID
    if (ncbi_tax_id!=null && Utils.isNumber(species, true)) {
      if (ncbi_tax_id.equals(Integer.parseInt(species))) {
        return true;
      } else {
        return false;
      }
    }
    // String identifiers
    for (int i=0; i<2; i++) {
      if (i==1) {
        /* First try with exact string. Second try with replacing
         * spaces and dots. (e.g. "C. elegans" => "Celegans"
         * => Ensembl name matches). */
        species = species.replaceAll("\\W", ""); // All no-word-chars
      }
      
      if (nameTypeToSearch==UNIPROT_EXTENSION || nameTypeToSearch==-1) {
        if (getUniprotExtension()!=null && getUniprotExtension().equalsIgnoreCase(species)) return true;
      }
      // TODO: Implement checks for nameTypeToSearch for other name types.
      
      if (getScientificName()!=null && getScientificName().equalsIgnoreCase(species)) return true;
      if (getCommonName()!=null && getCommonName().equalsIgnoreCase(species)) return true;
      if (getEnsemblName()!=null && getEnsemblName().equalsIgnoreCase(species)) return true;
      if (getKeggAbbr()!=null && getKeggAbbr().equalsIgnoreCase(species)) return true;
      if (getSynonyms()!=null) {
        for (String s1: getSynonyms()) {
          if (s1!=null && s1.equalsIgnoreCase(species)) return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Annotate a list of species with NCBI Taxonomy Identifiers.
   * @param all - List of species to annotate with NCBI Tax IDs
   * @param namesDMPfile -
   * Download "taxdmp.zip" from "ftp://ftp.ncbi.nih.gov/pub/taxonomy/"
   * and extract "names.dmp". This is the required filepath.
   * @throws IOException 
   */
  public static void addNCBItaxonomyIdentifier(List<Species> all, String namesDMPfile) throws IOException {
    System.out.println("Reading NCBI Taxonomy file...");    
    HashMap<String, Integer> taxFile = new HashMap<String, Integer>(10000);
    
    // Fast but memory intensive method. Could be rewritten to slow but memory efficient.
    
    // Read file
    CSVReader in = new CSVReader(namesDMPfile);
    in.setDisplayProgress(true);
    String[] line;
    while ((line=in.getNextLine())!=null) {
      //line[0] = Taxonomy ID, line[2] = Name, (line[4] = unique name if [Name] not unique), line[6] = Name type
      String name = line[2].trim();
      Integer ncbiID = Integer.parseInt(line[0].trim());
      taxFile.put(name.toUpperCase(), ncbiID);
    }
    in.close();
    
    // Match species list
    List<Species> unmatched = new ArrayList<Species>();
    unmatched.addAll(all);
    for (int i=0; i<unmatched.size(); i++) {
      Species spec = unmatched.get(i);
      
      Integer r = taxFile.get(spec.getScientificName().toUpperCase());
      if (r==null && spec.getCommonName()!=null) r = taxFile.get(spec.getCommonName().toUpperCase());
      if (r!=null) {
        spec.ncbi_tax_id = r;
        unmatched.remove(i);
        i--;
        System.out.println("Annotate " + spec.ncbi_tax_id  + " => " + spec + " Remaining: " + unmatched.size());
      }
    }
    
    // Optional: Extensively try every combination.
    ProgressBar prog = new ProgressBar(unmatched.size());
    for (int i=0; i<unmatched.size(); i++) {
      prog.DisplayBar();
      Species spec = unmatched.get(i);
      for (String s: taxFile.keySet()) {
        if (spec.matchesIdentifier(s)) {
          Integer r = taxFile.get(s);
          spec.ncbi_tax_id = r;
          unmatched.remove(i);
          i--;
          System.out.println("XAnnotate " + spec.ncbi_tax_id  + " => " + spec + " Remaining: " + unmatched.size());
        }        
      }

    }
    
    System.out.println("DONE.");
    /*
     *   /**
     *    * if {@link #generateSpeciesDataStructure()} is called (WITHOUT ARGUMENT ONLY)
     *    * and this (serialized) file exists, it will be loaded and returned.
     *    * /
     *   private final static String speciesCacheFile = "resources/" + Species.class.getPackage().getName().replace('.', '/') + "/speciesCache.dat";
     */
    //Utils.saveGZippedObject(speciesCacheFile, all);
    
  }

  /* (non-Javadoc)
   * @see de.zbit.io.CSVwriteable#fromCSV(java.lang.String[], int, int)
   */
  public void fromCSV(String[] elements, int elementNumber, int CSVversionNumber)
    throws CorruptInputStreamException {
    
    keggAbbr = elements[0];
    scientificName = elements[1].trim();
    uniprotExtension = elements[2];
    commonName = elements[3];
    ncbi_tax_id = elements[4].length()>0?Integer.parseInt(elements[4]):null;
    synonyms = null;
    if (elements.length>5 && elements[5].length()>0) {
      synonyms = Arrays.asList(elements[5].split(Pattern.quote("|")));
    }
    
  }

  /* (non-Javadoc)
   * @see de.zbit.io.CSVwriteable#getCSVOutputVersionNumber()
   */
  public int getCSVOutputVersionNumber() {
    return 0;
  }

  /* (non-Javadoc)
   * @see de.zbit.io.CSVwriteable#toCSV(int)
   */
  public String toCSV(int elementNumber) {
    if (elementNumber==0) {
      StringBuffer csv = new StringBuffer();
      appendCSVString(csv,keggAbbr);
      appendCSVString(csv,scientificName);
      appendCSVString(csv,uniprotExtension);
      appendCSVString(csv,commonName);
      appendCSVString(csv,ncbi_tax_id);
      
      if (synonyms!=null && synonyms.size()>0) {
        csv.append(ArrayUtils.implode(synonyms.toArray(new String[0]), "|"));
      }
      return csv.toString();
    } else return null;
  }
  
  private static void appendCSVString(StringBuffer csv, Object toAppend) {
    if (toAppend==null) {
      csv.append("");
    } else {
      csv.append(toAppend.toString());
    }
    csv.append('\t');
  }
  
  /**
   * Saves a {@link List}, {@link Collection} or Array of {@link Species}
   * as CSV file.
   * @param speciesListOrArray
   * @param outFile
   * @throws IOException
   */
  public static void saveAsCSV(Object speciesListOrArray, String outFile) throws IOException {
    CSVwriteableIO.write(speciesListOrArray, outFile);
  }
  
  /**
   * Loads a CSV file, previously saved with {@link #saveAsCSV(Object, String)}.
   * Returns the source data structure (which should be either a {@link List} or
   * an Array).
   * @param file
   * @return
   * @throws IOException
   */
  public static Object loadFromCSV(String file) throws IOException {
    return CSVwriteableIO.read(new Species(null), file);
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getName()
   */
  public String getName() {
    // For a better readability in options and such
    return getScientificName();
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getToolTip()
   */
  public String getToolTip() {
    // no tooltip.
    return null;
  }

  /**
   * @return
   */
  public boolean isSetScientificName() {
    return scientificName!=null && scientificName.length()>0;
  }
  
}
