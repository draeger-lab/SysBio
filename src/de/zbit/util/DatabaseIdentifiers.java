/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class contains names and regular expressions to match various
 * identifiers from bioinformatics databases. For example, one can
 * get a regular expression to extract an ensembl id from any string.
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class DatabaseIdentifiers {
  private static final transient Logger log = Logger.getLogger(DatabaseIdentifiers.class.getName());
  
  /**
   * list with all available {@link IdentifierDatabases}
   */
  private static Collection<IdentifierDatabases> identifiers = new HashSet<IdentifierDatabases>();
  /**
   * Contains a map from any {@link IdentifierDatabases}
   * to the corresponding regular expression.
   */
  private static Map<IdentifierDatabases, String> regExMap = new HashMap<IdentifierDatabases, String>();
  
  /**
   * Contains a map from any {@link IdentifierDatabases}
   * to the corresponding miriam URN.
   */
  private static Map<IdentifierDatabases, String> miriamMap = new HashMap<IdentifierDatabases, String>();
  
  /**
   * An enumeration of different gene identifiers in alphabetic order.
   * 
   * @author Clemens Wrzodek
   * @author Finja B&uuml;chel
   */
  public static enum IdentifierDatabases {
    /**
     * Chemical Entities of Biological Interest (ChEBI) is a freely available dictionary 
     * of molecular entities focused on 'small' chemical compounds.
     */
    ChEBI,
    ChemicalAbstracts,
    /**
     * Evidence Code Ontology, Evidence codes can be used to specify the type of supporting 
     * evidence for a piece of knowledge. This allows inference of a 'level of support' between 
     * an entity and an annotation made to an entity.
     */
    /**
     * Kegg compound
     */
    Compound,
    /**
     * ECOGene
     */
    ECO,
    /**
     *  Nucleotide Sequence Database
     *  http://www.ncbi.nlm.nih.gov/Genbank/
     */
    EMBL,
    Ensembl,
    EntrezGene,
    EnzymeConsortium,
    GenBank,
    GeneSymbol, 
    GeneOntology,
    /**
     * Kegg Glycan
     */
    Glycan,
    GO,
    /**
     * Human Metabolome Database
     */
    HMDB,
    HPRD,
    KeggGenes, 
    miRBase,
    NCBI_GeneID,
    NCBI_Taxonomy,
    /**
     *  Synonym of NCBI_Taxonomy, The taxonomy contains the relationships between all living forms 
     *  for which nucleic acid or protein sequence have been determined.
     */
    NEWT,
    Panther,
    PubChem_compound,
    PubMed,
    /**
     * Protein Modification Ontology, The Proteomics Standards Initiative modification ontology 
     * (PSI-MOD) aims to define a concensus nomenclature and ontology reconciling, in a hierarchical 
     * representation, the complementary descriptions of residue modifications.
     */
    PSI_MOD,
    /**
     * Molecular Interactions Ontology
     */
    PSI_MI,
    Reactome,
    Reactome_Database_ID,
    RefSeq,
    /**
     * Synonym for {@link #UniProt}
     */
    SPACC,
    Taxonomy,
    UniProt,
    /**
     * Synonym for {@link #UniProt}
     */
    UniProtKB,
    /**
     * With this entry several web links are referenced
     */
    Website;
  }   
  
  /**
   * Initialize the {@link #identifiers}.
   */
  static {
    // Do NOT append prefixes (^) or suffixes ($) or braces around the regex! 
    identifiers.add(IdentifierDatabases.ChEBI);
    identifiers.add(IdentifierDatabases.ChemicalAbstracts);
    identifiers.add(IdentifierDatabases.Compound);
    identifiers.add(IdentifierDatabases.EMBL);
    identifiers.add(IdentifierDatabases.Ensembl);
    identifiers.add(IdentifierDatabases.ECO);
    identifiers.add(IdentifierDatabases.EntrezGene);
    identifiers.add(IdentifierDatabases.EnzymeConsortium);
    identifiers.add(IdentifierDatabases.GenBank);
    identifiers.add(IdentifierDatabases.GeneOntology);
    identifiers.add(IdentifierDatabases.Glycan);
    identifiers.add(IdentifierDatabases.GO);
    identifiers.add(IdentifierDatabases.HMDB);
    identifiers.add(IdentifierDatabases.HPRD);
    identifiers.add(IdentifierDatabases.KeggGenes);
    identifiers.add(IdentifierDatabases.miRBase);
    identifiers.add(IdentifierDatabases.NCBI_GeneID);
    identifiers.add(IdentifierDatabases.NCBI_Taxonomy);
    identifiers.add(IdentifierDatabases.NEWT);
    identifiers.add(IdentifierDatabases.Panther);
    identifiers.add(IdentifierDatabases.PubChem_compound);
    identifiers.add(IdentifierDatabases.PubMed);
    identifiers.add(IdentifierDatabases.PSI_MI);
    identifiers.add(IdentifierDatabases.PSI_MOD);
    identifiers.add(IdentifierDatabases.Reactome);
    identifiers.add(IdentifierDatabases.Reactome_Database_ID);
    identifiers.add(IdentifierDatabases.RefSeq);
    identifiers.add(IdentifierDatabases.SPACC);
    identifiers.add(IdentifierDatabases.Taxonomy);
    identifiers.add(IdentifierDatabases.UniProt);
    identifiers.add(IdentifierDatabases.UniProtKB);
    identifiers.add(IdentifierDatabases.Website);
  }
  
  /**
   * Initialize the {@link #regExMap}.
   */
  static {
    // Do NOT append prefixes (^) or suffixes ($) or braces around the regex! 
    regExMap.put(IdentifierDatabases.ChEBI,                 "CHEBI:\\d+");
    regExMap.put(IdentifierDatabases.ChemicalAbstracts,     "\\d{1,7}\\-\\d{2}\\-\\d");
    regExMap.put(IdentifierDatabases.Compound,              "C\\d+");
    regExMap.put(IdentifierDatabases.EMBL,                  "\\w+(\\_)?\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.Ensembl,               "ENS[A-Z]*[FPTG]\\d{11}");
    regExMap.put(IdentifierDatabases.ECO,                   "ECO:\\d{7}");
    regExMap.put(IdentifierDatabases.EntrezGene,            "\\d+");
    regExMap.put(IdentifierDatabases.EnzymeConsortium,  
        "\\d+\\.-\\.-\\.-|\\d+\\.\\d+\\.-\\.-|\\d+\\.\\d+\\.\\d+\\.-|\\d+\\.\\d+\\.\\d+\\.(n)?\\d+");
    regExMap.put(IdentifierDatabases.GenBank,               "\\w+(\\_)?\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.GeneOntology,          "GO:\\d{7}");
    regExMap.put(IdentifierDatabases.Glycan,                "G\\d+");
    regExMap.put(IdentifierDatabases.GO,                    "GO:\\d{7}");
    regExMap.put(IdentifierDatabases.HMDB,                  "HMDB\\d{5}");
    regExMap.put(IdentifierDatabases.HPRD,                  "\\d+");
    regExMap.put(IdentifierDatabases.KeggGenes,             "\\w+:[\\w\\d\\.-]*");
    regExMap.put(IdentifierDatabases.miRBase,               "MI\\d{7}");
    regExMap.put(IdentifierDatabases.NCBI_GeneID,           "\\d+");
    regExMap.put(IdentifierDatabases.NCBI_Taxonomy,         "\\d+");
    regExMap.put(IdentifierDatabases.NEWT,                  "\\d+");
    regExMap.put(IdentifierDatabases.Panther,               "PTHR\\d{5}");
    regExMap.put(IdentifierDatabases.PubChem_compound,      "\\d+");
    regExMap.put(IdentifierDatabases.PubMed,                "\\d+");
    regExMap.put(IdentifierDatabases.PSI_MI,                "MI:\\d{4}");
    regExMap.put(IdentifierDatabases.PSI_MOD,               "MOD:\\d{5}");
    regExMap.put(IdentifierDatabases.Reactome,              "REACT_\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.Reactome_Database_ID,  "\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.RefSeq,              
        "(NC|AC|NG|NT|NW|NZ|NM|NR|XM|XR|NP|AP|XP|ZP)_\\d+");
    regExMap.put(IdentifierDatabases.SPACC, 
    "([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])");
    regExMap.put(IdentifierDatabases.Taxonomy,              "\\d+");
    regExMap.put(IdentifierDatabases.UniProt, 
        "([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])");
    regExMap.put(IdentifierDatabases.UniProtKB, 
    "([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])");
    regExMap.put(IdentifierDatabases.Website,       "www.\\..\\."); //TODO: is this correct?
  }
  
  /**
   * Initialize the {@link #miriamMap}.
   */
  static {
    miriamMap.put(IdentifierDatabases.ChEBI,                "urn:miriam:obo.chebi:");
    miriamMap.put(IdentifierDatabases.ChemicalAbstracts,    "urn:miriam:cas:");
    miriamMap.put(IdentifierDatabases.Compound,             "urn:miriam:kegg.compound:");
    miriamMap.put(IdentifierDatabases.EMBL,                 "urn:miriam:insdc:");
    miriamMap.put(IdentifierDatabases.Ensembl,              "urn:miriam:ensembl:");
    miriamMap.put(IdentifierDatabases.ECO,                  "urn:miriam:ecogene:");
    miriamMap.put(IdentifierDatabases.EntrezGene,           "urn:miriam:entrez.gene:");
    miriamMap.put(IdentifierDatabases.EnzymeConsortium,     "urn:miriam:ec-code:");
    miriamMap.put(IdentifierDatabases.GenBank,              "urn:miriam:insdc:");
    miriamMap.put(IdentifierDatabases.GeneOntology,         "urn:miriam:obo.go:");
    miriamMap.put(IdentifierDatabases.Glycan,               "urn:miriam:kegg.glycan:");
    miriamMap.put(IdentifierDatabases.GO,                   "urn:miriam:obo.go:");
    miriamMap.put(IdentifierDatabases.HMDB,                 "urn:miriam:hmdb:");
    miriamMap.put(IdentifierDatabases.HPRD,                 "urn:miriam:irefweb:");
    miriamMap.put(IdentifierDatabases.KeggGenes,            "urn:miriam:kegg.genes:");
    miriamMap.put(IdentifierDatabases.miRBase,              "urn:miriam:mirbase:");
    miriamMap.put(IdentifierDatabases.NCBI_GeneID,          "urn:miriam:entrez.gene:");
    miriamMap.put(IdentifierDatabases.NCBI_Taxonomy,        "urn:miriam:taxonomy:");
    miriamMap.put(IdentifierDatabases.NEWT,                 "urn:miriam:taxonomy:");
    miriamMap.put(IdentifierDatabases.Panther,              "urn:miriam:panther:");
    miriamMap.put(IdentifierDatabases.PubChem_compound,     "urn:miriam:pubchem.compound:");
    miriamMap.put(IdentifierDatabases.PubMed,               "urn:miriam:pubmed:");
    miriamMap.put(IdentifierDatabases.PSI_MI,               "urn:miriam:obo.mi:");
    miriamMap.put(IdentifierDatabases.PSI_MOD,              "urn:miriam:obo.psi-mod:");
    miriamMap.put(IdentifierDatabases.Reactome,             "urn:miriam:reactome:");
    miriamMap.put(IdentifierDatabases.Reactome_Database_ID, "urn:miriam:reactome:");
    miriamMap.put(IdentifierDatabases.RefSeq,               "urn:miriam:refseq:");
    miriamMap.put(IdentifierDatabases.SPACC,                "urn:miriam:uniprot:");
    miriamMap.put(IdentifierDatabases.Taxonomy,             "urn:miriam:taxonomy:");
    miriamMap.put(IdentifierDatabases.UniProt,              "urn:miriam:uniprot:");
    miriamMap.put(IdentifierDatabases.UniProtKB,            "urn:miriam:uniprot:");            
  }
  
  /**
   * Return a regular expression to identify a certain identifier.
   * @param identifier
   * @param contains specify if you want to have a "IS" regex (i.e. any string
   * IS any identifer) or an "CONTAINS" identifier (i.e. any string contains
   * any identifier). If you get a "CONTAINS" identifier, you can extract the
   * identifier from group number 1!
   * @return String with regular expression or null, if none available.
   */
  public static String getRegularExpressionForIdentifier(IdentifierDatabases identifier, boolean contains) {
    String regEx = regExMap.get(identifier);
    if (regEx!=null) {
      if (!contains) {
        // Create a "IS" identifier
        regEx = String.format("^%s$", regEx);
      } else {
        // Create a "contains" identifier
        regEx = String.format(".*?(%s).*?", regEx);
      }
    }
    return regEx;
  }
  
  /**
   * returns the corresponding miriam urn to the enterede database identifier
   * @param identifier
   * @return
   */
  public static String getMiriamURN(IdentifierDatabases identifier) {
    return miriamMap.get(identifier);
  }

  /**
   * search the entered identifier and returns true if it exists
   * @param db
   * @return
   */
  public static IdentifierDatabases getIdentifier(String db) {
    for (IdentifierDatabases identifier : identifiers) {
      if (identifier.toString().equalsIgnoreCase(db) || 
          (identifier.toString().replace("_", " ")).equalsIgnoreCase(db.replace("_", " "))){
            return identifier;
          }
    }
    return null;
  }
  
}
