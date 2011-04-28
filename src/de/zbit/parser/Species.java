/**
 * 
 */
package de.zbit.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import keggapi.Definition;
import de.zbit.kegg.KeggFunctionManagement;
import de.zbit.kegg.KeggQuery;
import de.zbit.util.InfoManagement;
import de.zbit.util.SortedArrayList;

/**
 * 
 * This class combines UniProt and KEGG identifiers,
 * i.e. KEGG abbreviation, scientific name, UniProt extension of proteins, common name
 * Example: "hsa", "Homo sapiens", "_HUMAN", "Human"
 * 
 * @author Finja B&uml;chel
 * @author Clemens Wrzodek
 */
public class Species implements Serializable, Comparable<Object> {
  private static final long serialVersionUID = 5900817226349012280L;
  
  public static final Logger log = Logger.getLogger(Species.class.getName());
  
  private String keggAbbr;
  private String scientificName;
  private String uniprotExtension;
  private String commonName;
  
  private List<String> synonyms;
  
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
    this(keggAbbr, scientificName, uniprotExtension, null, null);
  }
  
  /**
   * @param keggAbbr
   * @param scientificName
   * @param uniprotExtension
   * @param commonName
   * @param synonym
   */
  public Species(String keggAbbr, String scientificName,
            String uniprotExtension, String commonName, String synonym) {
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
    this.scientificName = scientificName;
  }

  /**
   * 
   * @param synonym
   * @return as specified by {@link Collection#add(Object)}
   */
  public boolean addSynonym(String synonym) {
    if (synonym!=null && (synonym.trim().length())>0) {
      if (this.synonyms==null) synonyms = new SortedArrayList<String>();
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
    if (o instanceof Species)
      return scientificName.toLowerCase().compareTo(((Species)o).scientificName.toLowerCase());
    else
      return scientificName.toLowerCase().compareTo(o.toString().toLowerCase());
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Species) {
      if (((Species) o).scientificName.toLowerCase().equals(scientificName.toLowerCase()))
        return true;
      else
        return false;
    }
    else {
      return false;
    }
  }
  
  public final static int SCIENTIFIC_NAME = 0;
  public final static int COMMON_NAME = 1;
  public final static int KEGG_ABBR = 2;
  public final static int UNIPROT_EXTENSION = 3;
  
  public static List<String> getListOfNames(List<Species> list, int type) {
    List<String> retval = new LinkedList<String>();
    for (int i=0; i<list.size(); i++) {
      retval.add(list.get(i).getName(type));
    }
    return retval;
  }

  /**
   * @param list
   * @param name
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
      speciesList = Species.generateSpeciesDataStructure(br);
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
   * @return a list with the species 
   *  
   */
  public static List<Species> generateSpeciesDataStructure() throws IOException {
    return generateSpeciesDataStructure(null);
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
      return generateSpeciesDataStructure(uniprotSpeciesFile);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);// TODO: Will ich das so oder gibt's noch was geschickteres?
    }
    return new LinkedList<Species>();
  }
  
  /**
   * 
   * @param uniprotSpeciesFile from the UniProt homepage under <a href="http://www.uniprot.org/docs/speclist">http://www.uniprot.org/docs/speclist</a>
   * @return
   * @throws IOException
   */
  public static List<Species> generateSpeciesDataStructure(BufferedReader in) throws IOException {
    List<Species> spec = new SortedArrayList<Species>();
    String common = "", scientific = "",  uniprot = "_";
    List<String> syns = new ArrayList<String>();
    
    if (in!=null) {
      boolean startReading=false;
      String line;
      while ((line=in.readLine())!=null) {
        if (line.equalsIgnoreCase("_____ _ _______ _____________________________________________________________")) {
          startReading = true;
        } else if (startReading) {
          if (line.contains("=======================================================================")) break; // done reading.
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
          if(!line.isEmpty()){
            if(!uniprot.equals("_"))
              spec.add(new Species(scientific, uniprot, common, syns));
            common = "";
            scientific = ""; 
            uniprot = "_" + line.substring(0,6).trim();
            syns = new ArrayList<String>();
          }  

          if(!line.isEmpty()){
            String help = line.substring(16, line.length());
            String[] split = help.split("=");
            if(split[0].equals("N"))
              scientific = split[1];
            else if (split[0].equals("C"))
              common = split[1];
            else if (split[0].equals("S"))
              syns.add(split[1]);
          }
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
    
    if (keggOrgs!=null)
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
        String keggAbbr = definition.getEntry_id();
        boolean contained = false;
        for(int i=0; i<spec.size(); i++){
          String scName = spec.get(i).getScientificName();
          if (spec.get(i).getScientificName().equalsIgnoreCase(scientificName)) contained=true;
          if(scName.startsWith(scientificName)) {
            if (spec.get(i).getKeggAbbr()==null || spec.get(i).getKeggAbbr().length()<1) {
              spec.get(i).setShortName(keggAbbr);
            }
          }
        }
        // Add not contained species.
        if (!contained) {
          String uniprot_ext = null;
          if (commonName!=null && commonName.trim().length()>0) uniprot_ext = '_'+commonName.trim().toUpperCase();
          Species new_spec = new Species(keggAbbr, scientificName, uniprot_ext, commonName, null);
          spec.add(new_spec);
        }
      }
      
    int counter = 0;
    List<Species> speciesWithoutKeggAbbr = new SortedArrayList<Species>();
    List<Species> speciesWithKeggAbbr = new SortedArrayList<Species>();
    for (Species species : spec) {
      if(species.getKeggAbbr()!=null){
        counter++;
        speciesWithKeggAbbr.add(species);
      }
      else
        speciesWithoutKeggAbbr.add(species);
    }
    
//    System.out.println("KeggOrgs.length: " + keggOrgs.length + ", speciesWithKeggAbbr: " + speciesWithKeggAbbr.size() + 
//                       ", speciesWithoutKeggAbbr: " + speciesWithoutKeggAbbr.size());
//    for (int i = 0; i < speciesWithoutKeggAbbr.size(); i++) {
//    Species species = speciesWithoutKeggAbbr.get(i);
//      System.out.println(i + "\t" + species.getScientificName() + "\t" + species.getKeggAbbr());
//    }
    
//    for (int i = 0; i < speciesWithKeggAbbr.size(); i++) {
//      Species species = speciesWithKeggAbbr.get(i);
//      System.out.println(i + "\t" + species.getScientificName() + "\t" + species.getKeggAbbr());
//    }
    
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
   * @return
   */
  public static Species search(List<Species> all, String species) {
    if (all==null || species==null) return null;
    for (Species s: all) {
      if (s.matchesIdentifier(species)) return s;
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
    return search(Species.generateSpeciesDataStructure(), species);
  }

  /**
   * Checks all identifiers and returns true if one of them
   * equals (ignore case) the 'species' string.
   * @param species
   * @return
   */
  public boolean matchesIdentifier(String species) {
    if (getScientificName()!=null && getScientificName().equalsIgnoreCase(species)) return true;
    if (getCommonName()!=null && getCommonName().equalsIgnoreCase(species)) return true;
    if (getEnsemblName()!=null && getEnsemblName().equalsIgnoreCase(species)) return true;
    if (getKeggAbbr()!=null && getKeggAbbr().equalsIgnoreCase(species)) return true;
    if (getSynonyms()!=null) {
      for (String s1: getSynonyms()) {
        if (s1!=null && s1.equalsIgnoreCase(species)) return true;
      }
    }
    return false;
  }
  
}
