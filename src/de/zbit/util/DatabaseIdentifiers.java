/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
   * This map is used to describe the content of each database.
   * See {@link DatabaseContent}.
   */
  private static Map<IdentifierDatabases, DatabaseContent> describedType = new HashMap<IdentifierDatabases, DatabaseContent>();
  
  /**
   * Maps an official name (e.g., "Enzyme Nomenclature") to the
   * corresponding {@link IdentifierDatabases} (e.g., {@link IdentifierDatabases#EC_code}).
   * See {@link DatabaseContent}.
   */
  private static Map<String, IdentifierDatabases> officialNames = new HashMap<String, IdentifierDatabases>();
  
  
  /**
   * An enumeration of different databases with identifiers in aplhabetical order.
   * 
   * @author Clemens Wrzodek
   * @author Finja B&uuml;chel
   */
  public static enum IdentifierDatabases {
    /*
     * PLEASE KEEP THE ALPHABETICAL ORDER!
     */
    /**
     * CAS (Chemical Abstracts Service) is a division of the American Chemical
     * Society and is the producer of comprehensive databases of chemical information.
     */
    CAS,
    /**
     * Chemical Entities of Biological Interest (ChEBI) is a freely available dictionary 
     * of molecular entities focused on 'small' chemical compounds.
     */
    ChEBI,
    ChemicalAbstracts,
    /**
     * ChemSpider is a free chemical structure database providing fast text and
     * structure search access to over 28 million structures from hundreds of data sources.
     * http://www.chemspider.com/
     */
    ChemSpider,
    /**
     * The DrugBank database is a bioinformatics and chemoinformatics resource
     * that combines detailed drug (i.e. chemical, pharmacological and
     * pharmaceutical) data with comprehensive drug target (i.e. sequence,
     * structure, and pathway) information.
     */
    DrugBank,
    EC_code,
    /**
     * Evidence Code Ontology, Evidence codes can be used to specify the type of supporting 
     * evidence for a piece of knowledge. This allows inference of a 'level of support' between 
     * an entity and an annotation made to an entity.
     */
    ECO,
    /**
     * EMBL-Bank
     */
    ENA,
    Ensembl,
    EntrezGene,
    /**
     * FlyBase is the database of the Drosophila Genome Projects and 
     * of associated literature.
     */
    FlyBase,
    /**
     * The International Nucleotide Sequence Database Collaboration (INSDC)
     * consists of a joint effort to collect and disseminate databases
     * containing DNA and RNA sequences.
     */
    GenBank, 
    GeneOntology,
    /**
     * GlycomeDB is the result of a systematic data integration effort, and
     * provides an overview of all carbohydrate structures available in public
     * databases, as well as cross-links.
     */
    GlycomeDB,
    /**
     * Human Metabolome Database - The Human Metabolome Database (HMDB) is a
     * database containing detailed information about small molecule metabolites
     * found in the human body.It contains or links 1) chemical 2) clinical and
     * 3) molecular biology/biochemistry data.
     */

    HMDB,
    /**
     * Human genome nomenclature (ID format: HGNC:\d{1,5}).
     */
    HGNC,
    /**
     * Actually not a real database identifier... 
     */
    GeneSymbol,
    /**
     * The IUPAC International Chemical Identifier (InChI)
     * www.iupac.org/inchi/
     */
    InChI,
    /**
     * The IUPAC International Chemical Identifier as hashed key(InChIKey)
     * www.iupac.org/inchi/
     */
    InChIKey,
    /**
     * The International Protein Index (IPI) provides complete nonredundant data
     * sets representing the human, mouse and rat proteomes, built from the
     * Swiss-Prot, TrEMBL, Ensembl and RefSeq databases.
     */
    IPI,
    /**
     * iRefWeb is an interface to a relational database containing the latest
     * build of the interaction Reference Index (iRefIndex) which integrates
     * protein interaction data from ten different interaction databases:
     * BioGRID, BIND, CORUM, DIP, HPRD, INTACT, MINT, MPPI, MPACT and OPHID. In
     * addition, iRefWeb associates interactions with the PubMed record from
     * which they are derived.
     */
    iRefWeb,
    KEGG_Genes, 
    KEGG_Compound,
    KEGG_Glycan,
    KEGG_Reaction,
    KEGG_Drug,
    KEGG_Pathway,
    KEGG_Orthology,
    KEGG_Genome,
    KEGG_Metagenome,    
    /**
     * LipidBank is an open, publicly free database of natural lipids including
     * fatty acids, glycerolipids, sphingolipids, steroids, and various
     * vitamins.
     */
    LipidBank,
    /**
     * Molecular Interactions Ontology (PSI-MI)
     */
    MI,
    /**
     * Protein Modification Ontology (PSI-MOD)
     */
    MOD,
    /**
     * 
     */
    miRBase,
    NCBI_Taxonomy,
    /**
     * The Protein database is a collection of sequences from several sources, 
     * including translations from annotated coding regions in GenBank, RefSeq 
     * and TPA, as well as records from SwissProt, PIR, PRF, and PDB.
     * Restrictions: This restriction is associated to data collections which 
     * are an aggregated set of different types of data. For example, they could 
     * allow identification of protein, DNA and RNA within the same collection. 
     * One should therefore not expect each record within data collections with 
     * this restriction to refer to directly comparable entities.
     */
    NCBI_Protein,
    /**
     * Online Mendelian Inheritance in Man is a catalog of human genes and
     * genetic disorders. Synonym: MIM
     */
    OMIM,
    /**
     * The PANTHER (Protein ANalysis THrough Evolutionary Relationships)
     * Classification System is a unique resource that classifies genes by their
     * functions, using published scientific experimental evidence and
     * evolutionary relationships to predict function even in the absence of
     * direct experimental evidence.
     */
    Panther,
    /**
     * 	The Protein Data Bank is the single worldwide archive of structural data 
     *  of biological macromolecules.
     */
    PDB,
    /**
     * Chemical Component Dictionary (also called PDB-CCD, PDBeChem) -- The
     * Chemical Component Dictionary is as an external reference file describing
     * all residue and small molecule components found in Protein Data Bank
     * entries.
     */
    PDBeChem,
    /**
     * PubChem provides information on the biological activities of small
     * molecules. It is a component of NIH's Molecular Libraries Roadmap
     * Initiative. PubChem Substance archives chemical substance records.
     */
    PubChem_substance,
    PubChem_compound,
    PubMed,
    /**
     * Protein Modification Ontology, The Proteomics Standards Initiative
     * modification ontology (PSI-MOD) aims to define a concensus nomenclature
     * and ontology reconciling, in a hierarchical representation, the
     * complementary descriptions of residue modifications.
     */
    PSI_MOD,
    /**
     * Molecular Interactions Ontology -- The Molecular Interactions (MI)
     * ontology forms a structured controlled vocabulary for the annotation of
     * experiments concerned with protein-protein interactions. MI is developed
     * by the HUPO Proteomics Standards Initiative.
     */
    PSI_MI,
    /**
     * The Reactome project is a collaboration to develop a curated resource of
     * core pathways and reactions in human biology.
     */
    Reactome,
    RefSeq,
    /**
     * Systems Biology Ontology (SBO)
     */
    SBO,
    /**
     * Actually 3DMET. 3DMET is a database collecting three-dimensional
     * structures of natural metabolites.
     */
    ThreeDMET,
    /**
     * A UniGene entry is a set of transcript sequences that appear to come 
     * from the same transcription locus (gene or expressed pseudogene), 
     * together with information on protein similarities, gene expression, 
     * cDNA clone reagents, and genomic location.
     * Restrictions: The way this data collection is distributed prevents 
     * linking to one specific entity. For example, access may require users 
     * authentication or the data might be distributed as a whole set and 
     * not individually.
     */
    UniGene,
    UniProt_AC;
    
    /**
     * Tries to return the official database name as given by MIRIAM.
     * @return the official name of the database.
     */
    public String getOfficialName() {
      if (this==GeneOntology) {
        return "Gene Ontology";
      } else if (this==PubChem_substance) {
        return "PubChem-substance";
      } else if (this==UniProt_AC) {
        return "UniProt";
      } else if (this==EC_code) {
        return "Enzyme Nomenclature";
      } else if (this==EntrezGene) {
        return "Entrez gene";
      } else if (this==PDBeChem) {
        return "Chemical Component Dictionary";
      } else if (this==NCBI_Taxonomy) {
        return "Taxonomy";
        
      } else {
        
        String s = toString();
        s = s.replace("_", " ");
        s = StringUtil.replaceIgnoreCase(s, "Three", Integer.toString(3));
        s = StringUtil.replaceIgnoreCase(s, "Two", Integer.toString(2));
        s = StringUtil.replaceIgnoreCase(s, "One", Integer.toString(1));
      
        return s;
      }
    }
  }
  
  /**
   * Use this enumeration to further specify the content of a particular database!
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public static enum DatabaseContent {
    /**
     * The database contains information about smll molecules (also called compounds).
     * Example: KEGG compound
     */
    small_molecule,
    /**
     * DB information about gnes, proteins, transcripts, etc. all kinds of omics data.
     * Example: Ensembl.
     */
    omics,
    /**
     * DB contains info about genes.
     * Example: EntrezGene
     */
    gene,
    /**
     * DB contains info about enzymes.
     * Example: EC-Code
     */
    enzyme,
    /**
     * DB contains info about proteins.
     * Example: UniProt
     */
    protein,
    /**
     * No concreate protein instances, bot orthology information
     * classifing rather the function of a group of proteins,
     * available in many organisms.
     * Example: KEGG Orthology
     */
    ortholog,
    /**
     * DB contains reactions
     * Example: KEGG reactions.
     */
    reaction,
    /**
     * DB contains info specific desriptions or terms
     * Example: Gene onotology
     */
    description,
    /**
     * DB contains info various annotations
     * Example: ECO
     */
    annotation,
    /**
     * DB contains (DNA-, RNA-, Protein-) sequences.
     * Example: RefSeq
     */
    sequences,
    /**
     * Database contains structures (chemical or 3D) of things.
     * Example: 3DMet, PubChem
     */
    structures,
    /**
     * DB contains protein interactions.
     * Example: String, Stitch
     */
    protein_interaction,
    /**
     * DB contains RNAs.
     * Example: miRBase (microRNAs)
     */
    RNA,
    /**
     * Example: NCBI Taxonomy
     */
    taxonomy,
    /**
     * Example: Pubmed
     */
    publication,
    /**
     * Example: KEGG Pathway
     */
    pathway
  }
  
  /**
   * Search the entered identifier and returns the instance if it exists.
   * @param dbIdentifier
   * @return
   */
  public static IdentifierDatabases getDatabase(String dbIdentifier) {
    IdentifierDatabases id = null;
    try {
      id = IdentifierDatabases.valueOf(dbIdentifier);
    } catch (Exception e) {
      // Doesn't matter, is null then
    }
    if (id==null) {
      // Check against each database
      String dbIdentifier2 = dbIdentifier.toLowerCase().replace("_", "").replace("-", "").replace(" ", "");
      for (IdentifierDatabases identifier : IdentifierDatabases.values()) {
        if (identifier.toString().replace("_", "").replace("-", "").replace(" ", "").equalsIgnoreCase(dbIdentifier2)) {
          return identifier;
        }
        // TODO: _ID suffix. E.g. "KeggGenes" should also catch "KeggGene", KeggGeneID", etc.
      }
      
      // Many databases have synonyms! Catch them here.
      if (dbIdentifier2.equalsIgnoreCase("UniProtKB") ||
          dbIdentifier2.equalsIgnoreCase("UniProt") ||
          dbIdentifier2.equalsIgnoreCase("SPACC")){
        return IdentifierDatabases.UniProt_AC;
        
      } else if (dbIdentifier2.equalsIgnoreCase("Taxonomy") ||
          dbIdentifier2.equalsIgnoreCase("NEWT")) {
        return IdentifierDatabases.NCBI_Taxonomy;
        
      } else if (dbIdentifier2.equalsIgnoreCase("NCBIGeneID") ||
          dbIdentifier2.equalsIgnoreCase("GeneID") ||
          dbIdentifier2.equalsIgnoreCase("LocusLink") ||
          dbIdentifier2.equalsIgnoreCase("LL")) {
        return IdentifierDatabases.EntrezGene;
        
      } else if (dbIdentifier2.equalsIgnoreCase("GO") || 
          dbIdentifier2.equalsIgnoreCase("Gene Ontology")) {        
        return IdentifierDatabases.GeneOntology;
        
      } else if (dbIdentifier2.equalsIgnoreCase("INSDC")) {
        return IdentifierDatabases.GenBank;
        
      } else if (dbIdentifier2.equalsIgnoreCase("3DMET")) {
        return IdentifierDatabases.ThreeDMET;

      } else if (dbIdentifier2.equalsIgnoreCase("Chemical Abstracts Service") ||
          dbIdentifier2.startsWith("CAS ") || dbIdentifier2.equalsIgnoreCase("CASRN")) {
        return IdentifierDatabases.CAS;
        
      } else if (dbIdentifier2.equalsIgnoreCase("PDBCCD")) {
        return IdentifierDatabases.PDBeChem;

      } else if (dbIdentifier2.equalsIgnoreCase("EnzymeConsortium")) {
        return IdentifierDatabases.EC_code;
        
      } else if (dbIdentifier2.startsWith("reactome")) {
        return IdentifierDatabases.Reactome;
        
      } else if (dbIdentifier2.equalsIgnoreCase("BioGRID") ||
          dbIdentifier2.equalsIgnoreCase("BIND") ||
          dbIdentifier2.equalsIgnoreCase("CORUM") ||
          dbIdentifier2.equalsIgnoreCase("DIP") ||
          dbIdentifier2.equalsIgnoreCase("HPRD") ||
          dbIdentifier2.equalsIgnoreCase("INTACT") ||
          dbIdentifier2.equalsIgnoreCase("MINT") ||
          dbIdentifier2.equalsIgnoreCase("MPPI") ||
          dbIdentifier2.equalsIgnoreCase("MPACT ") ||
          dbIdentifier2.equalsIgnoreCase("OPHID")) {
        return IdentifierDatabases.iRefWeb;
        
      } else if (dbIdentifier2.equalsIgnoreCase("Symbol")) {
        return IdentifierDatabases.GeneSymbol;

      }
      
      // Check the official names list
      for (String officialName : officialNames.keySet()) {
        if (officialName.equalsIgnoreCase(dbIdentifier)) {
          return officialNames.get(officialName);
        }
      }
      
      log.warning(String.format("Could not get database identifier for '%s'.", dbIdentifier));
    }
    return id;
  }
  
  
  /**
   * Initialize all maps.
   */
  static {
    /*
     * Initialize the {@link #regExMap}.
     */
    // Do NOT append prefixes (^) or suffixes ($) or braces around the regex!
    regExMap.put(IdentifierDatabases.CAS,                   "\\d{1,7}\\-\\d{2}\\-\\d");
    regExMap.put(IdentifierDatabases.ChEBI,                 "CHEBI:\\d+");
    regExMap.put(IdentifierDatabases.ChemicalAbstracts,     "\\d{1,7}\\-\\d{2}\\-\\d");
    regExMap.put(IdentifierDatabases.KEGG_Compound,         "C\\d{5}");
    regExMap.put(IdentifierDatabases.ENA,                   "[A-Z]+[0-9]+");
    regExMap.put(IdentifierDatabases.UniGene,             	"\\d+");
    regExMap.put(IdentifierDatabases.FlyBase,               "FB\\w{2}\\d{7}");
    regExMap.put(IdentifierDatabases.Ensembl,               "ENS[A-Z]*[FPTG]\\d{11}");
    regExMap.put(IdentifierDatabases.ECO,                   "ECO:\\d{7}");
    regExMap.put(IdentifierDatabases.EntrezGene,            "\\d+");
    regExMap.put(IdentifierDatabases.EC_code,  
    "\\d+\\.-\\.-\\.-|\\d+\\.\\d+\\.-\\.-|\\d+\\.\\d+\\.\\d+\\.-|\\d+\\.\\d+\\.\\d+\\.(n)?\\d+");
    regExMap.put(IdentifierDatabases.GenBank,               "\\w+(\\_)?\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.GeneOntology,          "GO:\\d{7}");
    regExMap.put(IdentifierDatabases.KEGG_Glycan,           "G\\d{5}");
    regExMap.put(IdentifierDatabases.HMDB,                  "HMDB\\d{5}");
    regExMap.put(IdentifierDatabases.iRefWeb,               "\\d+");
    regExMap.put(IdentifierDatabases.KEGG_Genes,            "\\w+:[\\w\\d\\.-]*");
    regExMap.put(IdentifierDatabases.miRBase,               "MI\\d{7}");
    regExMap.put(IdentifierDatabases.NCBI_Taxonomy,         "\\d+");
    regExMap.put(IdentifierDatabases.PDB,                   "[0-9][A-Za-z0-9]{3}");
    regExMap.put(IdentifierDatabases.Panther,               "PTHR\\d{5}");
    regExMap.put(IdentifierDatabases.PubChem_substance,     "\\d+");
    regExMap.put(IdentifierDatabases.PubMed,                "\\d+");
    regExMap.put(IdentifierDatabases.PSI_MI,                "MI:\\d{4}");
    regExMap.put(IdentifierDatabases.PSI_MOD,               "MOD:\\d{5}");
    regExMap.put(IdentifierDatabases.Reactome,              "REACT_\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.RefSeq,                
    "(NC|AC|NG|NT|NW|NZ|NM|NR|XM|XR|NP|AP|XP|ZP)_\\d+");
    regExMap.put(IdentifierDatabases.UniProt_AC, 
    "([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])");
    regExMap.put(IdentifierDatabases.IPI,                   "IPI\\d{8}");    
    regExMap.put(IdentifierDatabases.KEGG_Reaction,         "R\\d{5}");
    regExMap.put(IdentifierDatabases.KEGG_Drug,             "D\\d{5}");
    regExMap.put(IdentifierDatabases.KEGG_Pathway,          "[a-zA-Z]{2,4}\\d{5}");
    regExMap.put(IdentifierDatabases.KEGG_Orthology,        "K\\d{5}");
    regExMap.put(IdentifierDatabases.NCBI_Protein,          "\\w+\\d+(\\.\\d+)?");
    regExMap.put(IdentifierDatabases.OMIM,                  "[*#+%^]?\\d{6}");
    regExMap.put(IdentifierDatabases.DrugBank,              "DB\\d{5}");
    regExMap.put(IdentifierDatabases.ThreeDMET,             "B\\d{5}");
    regExMap.put(IdentifierDatabases.PDBeChem,              "\\w{3}");
    regExMap.put(IdentifierDatabases.GlycomeDB,             "\\d+");
    regExMap.put(IdentifierDatabases.LipidBank,             "\\w+\\d+");
    regExMap.put(IdentifierDatabases.KEGG_Genome,           "(T0\\d+|\\w{3,4})");
    regExMap.put(IdentifierDatabases.KEGG_Metagenome,       "T3\\d+");
    regExMap.put(IdentifierDatabases.HGNC,                  "HGNC:\\d{1,5}");
    regExMap.put(IdentifierDatabases.GeneSymbol,            "\\w+");
    regExMap.put(IdentifierDatabases.SBO,                   "SBO:\\d{7}");
    regExMap.put(IdentifierDatabases.MI,                    "MI:\\d{4}");
    regExMap.put(IdentifierDatabases.MOD,                    "MOD:\\d{5}");
    
    
    
    /* 
     * Initialize the {@link #miriamMap}.
     */
    miriamMap.put(IdentifierDatabases.CAS,                  "urn:miriam:cas:");
    miriamMap.put(IdentifierDatabases.ChEBI,                "urn:miriam:obo.chebi:");
    miriamMap.put(IdentifierDatabases.ChemicalAbstracts,    "urn:miriam:cas:");
    miriamMap.put(IdentifierDatabases.KEGG_Compound,        "urn:miriam:kegg.compound:");
    miriamMap.put(IdentifierDatabases.Ensembl,              "urn:miriam:ensembl:");
    miriamMap.put(IdentifierDatabases.ECO,                  "urn:miriam:ecogene:");
    miriamMap.put(IdentifierDatabases.EntrezGene,           "urn:miriam:entrez.gene:");
    miriamMap.put(IdentifierDatabases.EC_code,              "urn:miriam:ec-code:");
    miriamMap.put(IdentifierDatabases.GenBank,              "urn:miriam:insdc:");
    miriamMap.put(IdentifierDatabases.GeneOntology,         "urn:miriam:obo.go:");
    miriamMap.put(IdentifierDatabases.KEGG_Glycan,          "urn:miriam:kegg.glycan:");
    miriamMap.put(IdentifierDatabases.HMDB,                 "urn:miriam:hmdb:");
    miriamMap.put(IdentifierDatabases.iRefWeb,              "urn:miriam:irefweb:");
    miriamMap.put(IdentifierDatabases.KEGG_Genes,           "urn:miriam:kegg.genes:");
    miriamMap.put(IdentifierDatabases.miRBase,              "urn:miriam:mirbase:");
    miriamMap.put(IdentifierDatabases.NCBI_Taxonomy,        "urn:miriam:taxonomy:");
    miriamMap.put(IdentifierDatabases.ENA,                  "urn:miriam:ena.embl:");
    miriamMap.put(IdentifierDatabases.UniGene,              "urn:miriam:unigene:");
    miriamMap.put(IdentifierDatabases.FlyBase,              "urn:miriam:flybase:");
    miriamMap.put(IdentifierDatabases.PDB,	                "urn:miriam:pdb:");
    miriamMap.put(IdentifierDatabases.Panther,              "urn:miriam:panther:");
    miriamMap.put(IdentifierDatabases.PubChem_substance,    "urn:miriam:pubchem.substance:");
    miriamMap.put(IdentifierDatabases.PubMed,               "urn:miriam:pubmed:");
    miriamMap.put(IdentifierDatabases.PSI_MI,               "urn:miriam:obo.mi:");
    miriamMap.put(IdentifierDatabases.PSI_MOD,              "urn:miriam:obo.psi-mod:");
    miriamMap.put(IdentifierDatabases.Reactome,             "urn:miriam:reactome:");
    miriamMap.put(IdentifierDatabases.RefSeq,               "urn:miriam:refseq:");
    miriamMap.put(IdentifierDatabases.UniProt_AC,           "urn:miriam:uniprot:");
    miriamMap.put(IdentifierDatabases.IPI,                  "urn:miriam:ipi:");
    miriamMap.put(IdentifierDatabases.KEGG_Reaction,        "urn:miriam:kegg.reaction:");
    miriamMap.put(IdentifierDatabases.KEGG_Drug,            "urn:miriam:kegg.drug:");
    miriamMap.put(IdentifierDatabases.KEGG_Pathway,         "urn:miriam:kegg.pathway:");
    miriamMap.put(IdentifierDatabases.KEGG_Orthology,       "urn:miriam:kegg.orthology:");
    miriamMap.put(IdentifierDatabases.NCBI_Protein,         "urn:miriam:ncbiprotein:");
    miriamMap.put(IdentifierDatabases.OMIM,                 "urn:miriam:omim:");
    miriamMap.put(IdentifierDatabases.DrugBank,             "urn:miriam:drugbank:");
    miriamMap.put(IdentifierDatabases.ThreeDMET,            "urn:miriam:3dmet:");
    miriamMap.put(IdentifierDatabases.PDBeChem,             "urn:miriam:pdb-ccd:");
    miriamMap.put(IdentifierDatabases.GlycomeDB,            "urn:miriam:glycomedb:");
    miriamMap.put(IdentifierDatabases.LipidBank,            "urn:miriam:lipidbank:");
    miriamMap.put(IdentifierDatabases.KEGG_Genome,          "urn:miriam:kegg.genome:");
    miriamMap.put(IdentifierDatabases.KEGG_Metagenome,      "urn:miriam:kegg.metagenome:");
    miriamMap.put(IdentifierDatabases.HGNC,                 "urn:miriam:hgnc:");
    miriamMap.put(IdentifierDatabases.SBO,                  "urn:miriam:biomodels.sbo:");
    miriamMap.put(IdentifierDatabases.MI,                   "urn:miriam:obo.mi:");
    miriamMap.put(IdentifierDatabases.MOD,                  "urn:miriam:obo.psi-mod:");
    //miriamMap.put(IdentifierDatabases.GeneSymbol,         ); // None available!
    
    
    /* 
     * Initialize the {@link #describedType} map.
     */
    describedType.put(IdentifierDatabases.CAS,                   DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.ChEBI,                 DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.ChemicalAbstracts,     DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.KEGG_Compound,         DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.Ensembl,               DatabaseContent.omics);
    describedType.put(IdentifierDatabases.ECO,                   DatabaseContent.annotation);
    describedType.put(IdentifierDatabases.EntrezGene,            DatabaseContent.gene);
    describedType.put(IdentifierDatabases.EC_code,               DatabaseContent.enzyme);
    describedType.put(IdentifierDatabases.GenBank,               DatabaseContent.sequences); // Actually DNA & RNA
    describedType.put(IdentifierDatabases.GeneOntology,          DatabaseContent.description);
    describedType.put(IdentifierDatabases.KEGG_Glycan,           DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.HMDB,                  DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.iRefWeb,               DatabaseContent.protein_interaction); // protein interactions
    describedType.put(IdentifierDatabases.KEGG_Genes,            DatabaseContent.gene);
    describedType.put(IdentifierDatabases.ENA,                   DatabaseContent.sequences);
    describedType.put(IdentifierDatabases.UniGene,            	 DatabaseContent.sequences);
    describedType.put(IdentifierDatabases.FlyBase,               DatabaseContent.sequences);
    describedType.put(IdentifierDatabases.miRBase,               DatabaseContent.RNA);
    describedType.put(IdentifierDatabases.NCBI_Taxonomy,         DatabaseContent.taxonomy);
    describedType.put(IdentifierDatabases.PDB,	                 DatabaseContent.structures);
    describedType.put(IdentifierDatabases.Panther,               DatabaseContent.description);
    describedType.put(IdentifierDatabases.PubChem_substance,     DatabaseContent.structures);
    describedType.put(IdentifierDatabases.PubMed,                DatabaseContent.publication);
    describedType.put(IdentifierDatabases.PSI_MI,                DatabaseContent.annotation); // Actually molecular interaction
    describedType.put(IdentifierDatabases.PSI_MOD,               DatabaseContent.protein);
    describedType.put(IdentifierDatabases.Reactome,              DatabaseContent.pathway); // Actually pathways and reactions
    describedType.put(IdentifierDatabases.RefSeq,                DatabaseContent.sequences); // DNA, RNA and protein
    describedType.put(IdentifierDatabases.UniProt_AC,            DatabaseContent.protein);
    describedType.put(IdentifierDatabases.IPI,                   DatabaseContent.protein);
    describedType.put(IdentifierDatabases.KEGG_Reaction,         DatabaseContent.reaction); // Actually Reaction
    describedType.put(IdentifierDatabases.KEGG_Drug,             DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.KEGG_Pathway,          DatabaseContent.pathway);
    describedType.put(IdentifierDatabases.KEGG_Orthology,        DatabaseContent.ortholog);
    describedType.put(IdentifierDatabases.NCBI_Protein,          DatabaseContent.protein);
    describedType.put(IdentifierDatabases.OMIM,                  DatabaseContent.annotation);
    describedType.put(IdentifierDatabases.DrugBank,              DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.ThreeDMET,             DatabaseContent.structures); // Actually protein structure
    describedType.put(IdentifierDatabases.PDBeChem,              DatabaseContent.small_molecule);
    describedType.put(IdentifierDatabases.GlycomeDB,             DatabaseContent.structures);
    describedType.put(IdentifierDatabases.LipidBank,             DatabaseContent.structures);
    describedType.put(IdentifierDatabases.KEGG_Genome,           DatabaseContent.annotation);
    describedType.put(IdentifierDatabases.KEGG_Metagenome,       DatabaseContent.annotation);
    describedType.put(IdentifierDatabases.HGNC,                  DatabaseContent.omics); // Gene symbols are not only for genes...
    describedType.put(IdentifierDatabases.GeneSymbol,            DatabaseContent.omics); // Gene symbols are not only for genes...
    describedType.put(IdentifierDatabases.SBO,                   DatabaseContent.description);
    describedType.put(IdentifierDatabases.MI,                    DatabaseContent.description);
    describedType.put(IdentifierDatabases.MOD,                   DatabaseContent.description);
    
    
    for (IdentifierDatabases db : IdentifierDatabases.values()) {
      officialNames.put(db.getOfficialName(), db);
    }
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
   * The same as {@link #getRegularExpressionForIdentifier(IdentifierDatabases, boolean)} but does not
   * modify the regEx with anything.
   * @param identifier
   * @return
   */
  public static String getPlainRegularExpressionForIdentifier(IdentifierDatabases identifier) {
    return regExMap.get(identifier);
  }
  
  /**
   * Returns the corresponding miriam urn to the enterede database identifier.
   * <i>Note: this is just the prefix, to which the real identifier still need
   * to be attached. Use {@link #getMiriamURN(IdentifierDatabases, String)} to
   * get a final URN.</i>
   * @param identifier
   * @return
   */
  public static String getMiriamURN(IdentifierDatabases identifier) {
    return miriamMap.get(identifier);
  }
  
  /**
   * Get a MIRIAM URN for a specific database and identifier.
   * @param db
   * @param identifier
   * @return correctly formatted miriam urn or {@code null}
   * if this failed.
   */
  public static String getMiriamURN(IdentifierDatabases db, String identifier) {
    if (!checkID(db, identifier)) {
      identifier = getFormattedID(db, identifier);
      if (identifier==null) return null;
      if (!DatabaseIdentifiers.checkID(db, identifier)) {
        log.warning("Skipping invalid database entry " + identifier);
        return null;
      }
    }
    
    String miriam = miriamMap.get(db);
    if (miriam==null) {
      // No need to issue a warning for gene symbol
      Level l = Level.WARNING;
      if (db.equals(IdentifierDatabases.GeneSymbol)) {
        l = Level.FINE;
      }
      log.log(l, String.format("Missing MIRIAM identifier for database '%s'.", db));
      return null;
    }
    
    identifier = identifier.replace(":", "%3A");
    return miriam + identifier;
  }
  
  /**
   * Get a MIRIAM URI for a specific database and identifier.
   * @param db
   * @param identifier
   * @return correctly formatted miriam uri or {@code null}
   * if this failed.
   */
  public static String getMiriamURI(IdentifierDatabases db, String identifier) {
    String urn = getMiriamURN(db, identifier);
    if (urn==null) return null;
    
    // Make the conversion to URI
    Matcher m = Pattern.compile("urn:miriam:(.+?):(.+)").matcher(urn);
    if (!m.find()) return null;
    // As a side-note: In contrast to the URNs, URIs are allowed to contain a ':'. 
    return String.format("http://identifiers.org/%s/%s", m.group(1), m.group(2).replace("%3A", ":").replace("%3a", ":"));
  }
  
  /**
   * Returns the {@link DatabaseContent} of the given database.
   * @param identifier
   * @return
   */
  public static DatabaseContent getDatabaseType(IdentifierDatabases identifier) {
    return describedType.get(identifier);
  }
  
  /**
   * Check if any identifier matches the regular expression of the
   * corresponding database.
   * @param database
   * @param id
   * @return {@code true} if the {@code id} matches the regular
   * expression of the {@code database}.
   */
  public static boolean checkID(IdentifierDatabases database, String id) {
    if (id==null) {
      return false;
    }
    
    // Get RegEx
    String regEx = getRegularExpressionForIdentifier(database, false);
    if (regEx == null) {
      // unknown => in doubt, return true.
      log.warning(String.format("Missing regular expression for database '%s'.", database));
      return true;
    }
    
    // Check if id is correct
    return (Pattern.matches(regEx, id));
  }
  
  /**
   * Formats the identifier as requested by the database.
   * <p>Sometimes, databases have a prefix, but their identifiers don't.
   * Or identifiers have prefixes that must be removed to get a real
   * database identifier.
   * <p>A good example are the KEGG databases. This method removes
   * or adds the prefixes (e.g. "ko:") as required. Furthermore,
   * the "ECO:\\d{7}" is a good example. if you just give a number,
   * the 0's and ECO prefix will be prepended.
   * 
   * @param database
   * @param id
   * @return
   */
  public static String getFormattedID(IdentifierDatabases database, String id) {
    // Get RegEx
    String regEx = getRegularExpressionForIdentifier(database, false);
    if (regEx == null) {
      // can't do it.
      log.warning(String.format("Missing regular expression for database '%s'.", database));
      return id;
    }
    
    // Check if id is correct
    if (Pattern.matches(regEx, id)) {
      // Perfect match.
      return id;
    }
    
    /* Many identifiers end with a number that must have n digits.
     * We can try to prepend zeros to fix issues here.
     * Example: regex is "^ECO:\\d{7}$" and id is "253" => will create "0000253"
    */
    if (Utils.isNumber(id, true)) {
      Matcher m = Pattern.compile(".*?\\\\d\\{(\\d+)\\}[$]$").matcher(regEx);
      if (m.matches()) {
        // add leading zeros
        id = String.format("%0"+m.group(1)+"d", Integer.parseInt(id));
      }
    }
    
    // Many databases have prefixes before numbers. This can be auto-corrected.
    int posDid = id.indexOf(':');
    int posDdb = regEx.indexOf(':');
    char PrefixAndNumberDivisor=':';
    if (posDdb<0 && posDid<0) {
      // Some others (e.g., reactome) use an underscore
      posDid = id.indexOf('_');
      posDdb = regEx.indexOf('_');
      PrefixAndNumberDivisor='_';
    }
    
    if (posDdb>1 && posDid<0) {
      // database has a prefix that is missing in id (start at 1 to trim ^).
      String prefix = regEx.substring(1, posDdb);
      if (Pattern.matches("\\w+", prefix)) {
        String newId = String.format("%s%s%s", prefix, PrefixAndNumberDivisor, id);
        if (Pattern.matches(regEx, newId)) {
          return newId;
        }
      }
    } else if (posDdb<0 && posDid>1) {
      // database has no prefix but the id has one
      // (This is very often the case for all KEGG identifiers).
      String newId = id.substring(posDid+1);
      if (Pattern.matches(regEx, newId)) {
        // Trim prefix and check id id is now ok.
        return newId;
      }
    }
    
    // Prefixes without a ":"
    Matcher mDB = Pattern.compile("[\\^]([a-zA-Z]+)\\\\d[\\+\\{\\}0-9]+[$]").matcher(regEx);
    Matcher mID = Pattern.compile("([a-zA-Z]+)([0-9]+)").matcher(id);
    if (mDB.matches() && !mID.matches()) {
      String prefix = mDB.group(1);
      String newId = String.format("%s%s", prefix, id);
      if (Pattern.matches(regEx, newId)) {
        return newId;
      }
    }
    if (mID.matches() && !mDB.matches()) {
      if (Pattern.matches(regEx, mID.group(2))) {
        // Trim prefix and check id id is now ok.
        return mID.group(2);
      }
    }
    
    // Doesn't match and could not be fixed.
    return null;
  }
  
}
