/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.util.Utils;

/**
 * Simplifies parsing KeggInfos from the Adaptor. Stores and handles all
 * KEGG infos very conveniently.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KeggInfos implements Serializable {
  private static final long serialVersionUID = 3436209252181758056L;
  public static final transient Logger log = Logger.getLogger(KeggInfos.class.getName());
  
  /**
   * 
   */
	private String Kegg_ID;
	/**
   * The complete info-block from kegg.
   */
	private String informationFromKeggAdaptor;
	/**
   * 
   */
	private String taxonomy = null;
	/**
   * 
   */
	private String definition = null;
	/**
   * 
   */
	private String description = null;
  /**
   * 
   */
	private String orthology = null;
	/**
	 * multiple synonyms separated by ;
	 */
	private String names = null;
	/**
	 * Last entry (mostly the most meaningful) in names.
	 */
	private String name = null;
	/**
	 * Be careful: multiple numbers without "GO:". e.g., "0006096 0006094"
	 * instead of "GO:0006096"
	 */
	private String go_id = null;
	/**
	 * Be careful: multiple numbers without "RN:", e.g., "R05966 R05967"
	 */
	private String reaction_id = null;
	/**
   * 
   */
	private String ensembl_id = null;
	/**
   * 
   */
	private String uniprot_id = null;
	/**
   * 
   */
	private String hgnc_id = null;
	/**
   * 
   */
	private String omim_id = null;
	/**
   * 
   */
	private String entrez_id = null;
	/**
   * 
   */
	private String formula = null;
	/**
   * 
   */
	private String mass = null;
	/**
	 * PDB-CCD
   * PDBeChem - Dictionary of chemical components (ligands, small molecules and monomers) referred in PDB entries and maintained by the wwPDB
   * http://www.ebi.ac.uk/msd-srv/msdchem/cgi-bin/cgi.pl?APPLICATION=1 
	 */
	private String PDBeChem=null;
	/**
	 * urn:miriam:glycomedb
	 */
	private String GlycomeDB=null;
	/**
	 * urn:miriam:lipidbank
	 */
	private String LipidBank=null;
	/**
	 * <ul>
	 * <li>urn:miriam:pubchem.compound</li>
	 * <li>urn:miriam:pubchem.substance</li>
	 * <li>urn:miriam:pubchem.bioassay</li>
	 * </ul>
	 */
	private String pubchem = null;
	/**
	 * urn:miriam:obo.chebi, be careful! E.g., CHEBI:36927
	 */
	private String chebi = null;
	/**
	 * urn:miriam:3dmet
	 */
	private String three_dmet = null;
	/**
	 * NOT IN MIRIAM :(
	 * "CAS registry number, unique numerical identifiers for chemical substances"
	 */
	private String cas = null;
	/**
	 * urn:miriam:drugbank
	 */
	private String drugbank = null;
	/**
	 * e.g., "G10609 + G00094 <=> G10619 + G00097"
	 */
	private String equation = null;
	/**
	 * Referenced "PATHWAY" ids, without PATH: and comma separated.
	 */
	private String pathways = null;
	/**
	 * Description of referenced "PATHWAY" ids, comma separated. Comments behind
	 * URNs are examples.
	 */
	private String pathwayDescs = null;
	/**
	 * Enzymes for the reaction
	 * E.g. "5.4.2.1         5.4.2.4"
	 */
	private String enzymes=null;
	/**
	 * Genes occuring in a pathway. Example:
	 * <br>774  CACNA1B; calcium channel, voltage-dependent, N type, alpha 1B subunit [KO:K04849]
	 * <br>775  CACNA1C; calcium channel, voltage-dependent, L type, alpha 1C subunit [KO:K04850]
	 * <br>[...]
	 */
  private String[] gene_entry;
	
	
	
	
	/**
	 * 9606
	 */
	public static final String miriam_urn_taxonomy = "urn:miriam:taxonomy:";
	/**
	 * GO:0006915
	 */
	public static final String miriam_urn_geneOntology = "urn:miriam:obo.go:";
	/**
	 * hsa00620
	 */
	public static final String miriam_urn_kgPathway = "urn:miriam:kegg.pathway:";
	/**
	 * C12345
	 */
	public static final String miriam_urn_kgCompound = "urn:miriam:kegg.compound:";
	/**
	 * G00123
	 */
	public static final String miriam_urn_kgGlycan = "urn:miriam:kegg.glycan:";
  /**
   * G00123
   */
  public static final String miriam_urn_kgOrthology = "urn:miriam:kegg.orthology:";
	/**
	 * D00123
	 */
	public static final String miriam_urn_kgDrug = "urn:miriam:kegg.drug:";
	/**
	 * syn:ssr3451
	 */
	public static final String miriam_urn_kgGenes = "urn:miriam:kegg.genes:";
	/**
	 * R00100
	 */
	public static final String miriam_urn_kgReaction = "urn:miriam:kegg.reaction:";
	/**
	 * 1.1.1.1
	 */
	public static final String miriam_urn_ezymeECcode = "urn:miriam:ec-code:";
	/**
	 * ENSG00000139618
	 */
	public static final String miriam_urn_ensembl = "urn:miriam:ensembl:";
	/**
	 * P62158
	 */
	public static final String miriam_urn_uniprot = "urn:miriam:uniprot:";
	/**
	 * HGNC:2674
	 */
	public static final String miriam_urn_hgnc = "urn:miriam:hgnc:";
	/**
	 * 603903
	 */
	public static final String miriam_urn_omim = "urn:miriam:omim:";
	/**
	 * 100010
	 */
	public static final String miriam_urn_entrezGene = "urn:miriam:entrez.gene:";
	/**
	 * 100101
	 */
	public static final String miriam_urn_PubChem_Compound = "urn:miriam:pubchem.compound:";
	/**
	 * 100101
	 */
	public static final String miriam_urn_PubChem_Substance = "urn:miriam:pubchem.substance:";
	/**
	 * 1018
	 */
	public static final String miriam_urn_PubChem_Bioassay = "urn:miriam:pubchem.bioassay:";
	/**
	 * PDBeChem/pdb-ccd ID e.g. "2PM"
	 */
	public static final String miriam_urn_PDBeChem = "urn:miriam:pdb-ccd:";
  /**
   * Example: 1
   */
  public static final String miriam_urn_GlycomeDB = "urn:miriam:glycomedb:";
  /**
   * Example: BBA0001
   */
  public static final String miriam_urn_LipidBank = "urn:miriam:lipidbank:";
	/**
	 * CHEBI:36927
	 */
	public static final String miriam_urn_chebi = "urn:miriam:obo.chebi:";
	/**
	 * B00162
	 */
	public static final String miriam_urn_3dmet = "urn:miriam:3dmet:";
	/**
	 * DB00001
	 */
	public static final String miriam_urn_drugbank = "urn:miriam:drugbank:";
	
	/**
	 * Evidence Code Ontology, e.g. "ECO:0000313"
	 */
  public static final String miriam_urn_eco = "urn:miriam:obo.eco:";
  
  /**
   * MIRIAM urn for SBO-terms
   */
  public static final String miriam_urn_sbo = "urn:miriam:obo.sbo:"; 
  
  /**
   * RegEx Pattern for extracting EC enzyme codes
   */
  private final static Pattern ECcodes = Pattern.compile("(\\d+\\.-\\.-\\.-|\\d+\\.\\d+\\.-\\.-|\\d+\\.\\d+\\.\\d+\\.-|\\d+\\.\\d+\\.\\d+\\.(n)?\\d+)");
  

	// GO, kgGenes, hgnc, chebi - require extra prefixes!

	/**
	 * Same as 'getMiriamURIforKeggID(keggId, null)'. Please use other
	 * method and submit {@link EntryType} if available.
	 * 
	 * @param keggId
   * @return Complete MIRIAM URN Including the given ID. Or <code>NULL</code>
   * if no such MIRIAM URN is available!
	 */
	public static String getMiriamURIforKeggID(String keggId) {
		return getMiriamURIforKeggID(keggId, null);
	}

	/**
	 * Please use {@link #KeggInfos(String, KeggAdaptor)} if
	 * possible.
	 */
	public KeggInfos(String Kegg_ID) {
		this(Kegg_ID, new KeggAdaptor());
	}


	/**
	 * 
	 * @param Kegg_ID
	 * @param adap
	 */
	public KeggInfos(String Kegg_ID, KeggAdaptor adap) {
	  this(Kegg_ID, adap.get(Kegg_ID));
	}
	
	/**
	 * @param Kegg_ID
	 * @param informationFromKeggAdaptor already fetched info from the KEGG API.
	 */
	public KeggInfos(String Kegg_ID, String informationFromKeggAdaptor) {
	  super();
	  this.Kegg_ID = Kegg_ID;
	  this.informationFromKeggAdaptor = informationFromKeggAdaptor;
	  parseInfos();
	  // Clear to save ram, do not set to null to not break queryWasSucessfull().
	  if (this.informationFromKeggAdaptor!=null) {
	    this.informationFromKeggAdaptor="";
	  }
	}

	/**
	 * 
	 * @param ko_id
	 * @param manager
	 * @return a non-null {@link KeggInfos} instance.
	 */
  public static KeggInfos get(String ko_id, KeggInfoManagement manager) {
    KeggInfos ret = manager.getInformation(ko_id);
    if (ret==null) return new KeggInfos(ko_id,(String)null);
    else return ret;
  }

  /**
	 * 
	 * @return
	 */
	public boolean containsMultipleNames() {
		return names != null ? names.contains(";") : false;
	}

	/**
	 * 
	 * @return
	 */
	public String getCas() {
		return cas;
	}

	/**
	 * 
	 * @return
	 */
	public String getChebi() {
		return chebi;
	}

	/**
	 * 
	 * @return
	 */
	public String getChebi_with_MiriamURN() {
		return miriam_urn_chebi
				+ (chebi.contains(":") ? chebi.trim() : "CHEBI:" + chebi.trim());
	}

	/**
	 * 
	 * @return
	 */
	public String getDefinition() {
		return definition;
	}
	
  /**
   * @return
   */
  private String getOrthology() {
    return orthology;
  }

	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @return
	 */
	public String getDrugbank() {
		return drugbank;
	}

	/**
	 * 
	 * @return
	 */
	public String getDrugbank_with_MiriamURN() {
		return miriam_urn_drugbank + suffix(drugbank);
	}

	/**
	 * 
	 * @return
	 */
	public String getEnsembl_id() {
		return ensembl_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getEnsembl_id_with_MiriamURN() {
		return miriam_urn_ensembl + suffix(ensembl_id);
	}

	/**
	 * 
	 * @return
	 */
	public String getEntrez_id() {
		return entrez_id;
	}

	/**
	 * Be careful, fails to return the correct information if this class
	 * holds a string with multiple entrez ids!
	 * @return
	 */
	public String getEntrez_id_with_MiriamURN() {
		return miriam_urn_entrezGene + suffix(entrez_id);
	}

	/**
	 * 
	 * @return e.g. "G10609 + G00094 <=> G10619 + G00097"
	 */
	public String getEquation() {
		return equation;
	}
	
	/**
	 * Returns the parsed "ENZYME" entry.
   * <p>This method is mainly for reactions, 
   * and NOT for genes, compounds, etc. use
   * {@link #getECcodes()} for genes and such.
   * @see #getECcodes()
	 * @return e.g. "5.4.2.1         5.4.2.4"
	 */
	public String getEnzymes() {
	  return enzymes;
	}
	
  /**
   * Genes occuring in a pathway. Example:
   * <br>774  CACNA1B; calcium channel, voltage-dependent, N type, alpha 1B subunit [KO:K04849]
   * <br>775  CACNA1C; calcium channel, voltage-dependent, L type, alpha 1C subunit [KO:K04850]
   * <br>[...]
   * @return the gene_entry
   */
  public String[] getGeneEntry() {
    return gene_entry;
  }

	/**
	 * 
	 * @return
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * Maybe multiple values separated by space!
	 */
	public String getGo_id() {
		return go_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getHgnc_id() {
		return hgnc_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getHgnc_id_with_MiriamURN() {
		return miriam_urn_hgnc
				+ (hgnc_id.contains(":") ? hgnc_id.trim() : "HGNC:"
						+ hgnc_id.trim());
	}

	/**
	 * Deprecated because info is erased after parsing.
	 * @return
	 */
	@Deprecated
	public String getInformationFromKeggAdaptor() {
		return informationFromKeggAdaptor;
	}
	
  /**
   * @return
   */
  public String getPDBeChem() {
    return PDBeChem;
  }

  /**
   * @return
   */
  public String getGlycomeDB() {
    return GlycomeDB;
  }
  
  /**
   * @return
   */
  public String getLipidBank() {
    return LipidBank;
  }
  
	/**
	 * 
	 * @return
	 */
	public String getKegg_ID() {
		return Kegg_ID;
	}

	/**
	 * 
	 * @return may return <code>NULL</code> if no MIRIAM URN
	 * is available for the current KEGG ID.
	 */
	public String getKegg_ID_with_MiriamURN() {
		return getMiriamURIforKeggID(this.Kegg_ID);
	}

	/**
	 * 
	 * @return
	 */
	public String getMass() {
		return mass;
	}

	/**
	 * Hopefully the most meaningfull name (last in the ";"-dividedlist of getNames()).
	 * Does NOT remove synonyms, only multiples.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * All names, separated by ;
	 * 
	 * @return
	 */
	public String getNames() {
		return names;
	}
	
	/**
	 * Get EC Numbers (e.g. "EC:2.1.2.10") matching
	 * to this {@link #Kegg_ID}. Parses the kegg_id itself,
	 * the definition and the orthology. Does NOT parse the "ENZYME" entry.
	 * <p>Thus, this method is rather for genes, compounds, etc.
	 * and NOT for queried reactions. 
	 * @return non-<code>NULL</code> (but maybe empty) list
	 * of EC numbers (without ec: prefix) describing this object.
	 * @see #getEnzymes()
	 */
	public Collection<String> getECcodes() {
	  Set<String> ecCodes = new HashSet<String>();
	  
	  // Extract from kegg id (IS an enzyme)
	  Collection<String> ec = extractECNumbers(getKegg_ID());
	  if (ec!=null) ecCodes.addAll(ec);
	  
	  // Extract from definition (Encodes for an enzyme)
	  ec = extractECNumbers(getDefinition());
	  if (ec!=null) ecCodes.addAll(ec);
	  
    // Extract from orthology (Encodes for an enzyme)
	  // e.g. "efa:EF2928" and reaction "rn:R04241"
    ec = extractECNumbers(getOrthology());
    if (ec!=null) ecCodes.addAll(ec);
	  
	  return ecCodes;
	}

  /**
	 * Extract enzyme codes (e.g. "EC:2.1.2.10") from any string.
   * @param ids any string
   * @return EC Numbers if found (without ec: prefix) or <code>NULL</code>
   */
	public static Set<String> extractECNumbers(String ids) {
	  Set<String> ecCodes = new HashSet<String>();
	  
	  // Extract via regex.
	  if (ids!=null) {
	    Matcher matcher = ECcodes.matcher(ids);
	    int i=-1;
	    while (matcher.find()) {
	      i++;
	      String string = matcher.group(1);
	      ecCodes.add(string.trim());
	    }
	  }
	  return ecCodes.size()<1?null:ecCodes;
	}

  /**
	 * 
	 * @return
	 */
	public String getOmim_id() {
		return omim_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getOmim_id_with_MiriamURN() {
		return miriam_urn_omim + suffix(omim_id);
	}

	/**
	 * Of REFENRECED pathways. not the actual queried one (if one queries a PW)
	 * @return
	 */
	public String getPathwayDescriptions() {
		return pathwayDescs;
	}

	/**
	 * 
	 * @return
	 */
	public String getPathways() {
		return pathways;
	}

	/**
	 * 
	 * @return
	 */
	public String getPubchem() {
		return pubchem;
	}

	/**
	 * 
	 * @return
	 */
	public String getPubchem_with_MiriamURN() {
		// Pubchem occurs in compounds and drugs, according to
		// http://www.genome.jp/kegg/kegg3.html
		// Even Kegg Compounds refer to Pubchem Substance unnits. NOT to Pubchem
		// compounds. Strange... but true.
		return miriam_urn_PubChem_Substance + suffix(pubchem);
	}

	/**
	 * 
	 * @return
	 */
	public String getReaction_id() {
		return reaction_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getReaction_id_with_MiriamURN() {
		return miriam_urn_kgReaction + suffix(reaction_id);
	}

	/**
	 * 
	 * @return
	 */
	public String getTaxonomy() {
		return taxonomy;
	}

	/**
	 * 
	 * @return
	 */
	public String getTaxonomy_with_MiriamURN() {
		return miriam_urn_taxonomy + suffix(taxonomy);
	}

	/**
	 * 
	 * @return
	 */
	public String getThree_dmet() {
		return three_dmet;
	}

	/**
	 * 
	 * @return
	 */
	public String getThree_dmet_with_MiriamURN() {
		return miriam_urn_3dmet + suffix(three_dmet);
	}

	/**
	 * 
	 * @return
	 */
	public String getUniprot_id() {
		return uniprot_id;
	}

	/**
	 * 
	 * @return
	 */
	public String getUniprot_id_with_MiriamURN() {
		return miriam_urn_uniprot + suffix(uniprot_id);
	}
	

	/**
   * 
   */
	private void parseInfos() {
		String infos = informationFromKeggAdaptor; // Create a shorter variable name ;-)
		if (infos != null && infos.trim().length() == 0)
			infos = null;
		if (infos == null) {
			informationFromKeggAdaptor = null;
			return;
		}
		String uInfos = infos.toUpperCase();

		// General
		names = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "NAME", null);
		if (names != null && names.length() > 0) {
			int pos = names.lastIndexOf(";");
			if (pos > 0 && pos < (names.length() - 1))
				name = names.substring(pos + 1, names.length()).replace("\n","").trim();
			else
				name = names;
		}
		definition = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "DEFINITION", null);
		description = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "DESCRIPTION", null);
		orthology = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "ORTHOLOGY", null);

		// Mainly Pathway specific (eg. "path:map00603")
		go_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, " GO:", "\n"); // DBLINKS GO:
		// 0006096
		// 0006094

		// Mainly Organism specific (eg. "GN:hsa")
		taxonomy = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "TAXONOMY", "\n"); // e.g.
		// "TAXONOMY    TAX:9606"
		// =>
		// "TAX:9606".

		// Mainly Gene specific (eg. "hsa:12313")
		ensembl_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "ENSEMBL:", "\n");
		uniprot_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "UNIPROT:", "\n");
		hgnc_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "HGNC:", "\n");
		omim_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "OMIM:", "\n");
		entrez_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "NCBI-GENEID:", "\n");
		
		// For KO orthologous, parse entrez ids of all organisms from "GENES"
		if (entrez_id==null || entrez_id.length()<1) {
		  try {
		    String temp = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "\nGENES ", null);
		    if (temp!=null && temp.length()>0) {
		      StringBuffer eId = new StringBuffer();
		      for (int num: Utils.getNumbersFromString(temp, ": ", null)) {
		        if (eId.length()>0) eId.append(' ');
		        eId.append(num);
		      }
		      entrez_id = eId.toString();
		    }
		  } catch (Throwable t) {
		    t.printStackTrace();
		  }
		}
		
    // For pathways, parse genes occurring in this pathway
		try {
		  String temp = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "\nGENE ", null);
		  if (temp!=null && temp.length()>0) {
		    gene_entry = temp.split("\n");
		    for (int i=0; i<gene_entry.length; i++) {
		      gene_entry[i] = gene_entry[i].trim();
		    }
		  }
		} catch (Throwable t) {
		  t.printStackTrace();
		}

		// Mainly Glycan specific (eg. "glycan:G00181")
		/*
		 * Sadly non of the following has a miriam URN: CCSD (CarbBank) : 1303
		 * GlycomeDB: 12885 JCGGDB: JCGG-STR003128 LipidBank: GSG1005
		 */

		// Mainly Enzyme specific (eg. "ec:2.4.1.-  ")
		/*
		 * Mostly uninteresting... Product, Substrate, REACTION, Class
		 */

		// Ortholog (e.g. "ko:K01204")
		// DBLINKS (RN, GO); GENES (actual orthologous genes)
		// //urn:miriam:kegg.reaction (R00100)
		reaction_id = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, " RN:", "\n"); // DBLINKS
		// RN:
		// R05966

		// in small molecules (compound eg. "cpd:C00031")
		// KNApSAcK, NIKKAJI, (CAS) missing
		formula = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "FORMULA", null); // FORMULA C6H12O6
		mass = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "MASS", null); // MASS 180.0634

		pubchem = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "PUBCHEM:", "\n");
		PDBeChem = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "PDB-CCD:", "\n");
		chebi = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "CHEBI:", "\n");
		three_dmet = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "3DMET:", "\n");
		cas = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, " CAS:", "\n");
		
		GlycomeDB = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "GLYCOMEDB:", "\n");
		LipidBank = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "LIPIDBANK:", "\n");

		// Mainly drg (eg. "dr:D00694")
		// missing: NIKKAJI, LigandBox (CAS)
		drugbank = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "DRUGBANK:", "\n");

		// in reactions:
		equation = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "EQUATION", "\n");
		String pathwaysTemp = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "PATHWAY", null);
		if ((pathwaysTemp != null) && (pathwaysTemp.trim().length() != 0)) {
			pathwaysTemp = pathwaysTemp.replace("PATH:", "");
			String[] splitt = pathwaysTemp.split("\n");
			pathways = "";
			pathwayDescs = "";
			for (String s : splitt) {
				if (s.startsWith(" ") && (pathwayDescs.length() != 0)) {
					/*
					 * Continuation of last line.
					 */
					pathwayDescs += " " + s;
					continue;
				}
				s = s.trim();
				pathways += "," + s.substring(0, s.indexOf(" "));
				pathwayDescs += ","
						+ s.substring(s.indexOf(" ")).trim().replace(",", "");
			}
		}
		if ((pathways != null) && pathways.startsWith(",")) {
			pathways = pathways.substring(1);
		}
		if ((pathwayDescs != null) && pathwayDescs.startsWith(",")) {
			pathwayDescs = pathwayDescs.substring(1);
		}
		enzymes = KeggAdaptor.extractInfoCaseSensitive(infos, uInfos, "ENZYME", "\n");

		// Free Memory instead of storing empty Strings.
		if (taxonomy != null && taxonomy.trim().length() == 0)
			taxonomy = null;
		if (equation != null && equation.trim().length() == 0)
			equation = null;
    if (enzymes != null && enzymes.trim().length() == 0)
      enzymes = null;		
		if (pathways != null && pathways.trim().length() == 0)
			pathways = null;
		if (pathwayDescs != null && pathwayDescs.trim().length() == 0)
			pathwayDescs = null;
		if (definition != null && definition.trim().length() == 0)
			definition = null;
		if (description != null && description.trim().length() == 0)
			description = null;
    if (orthology != null && orthology.trim().length() == 0)
      orthology = null;
		if (names != null && names.trim().length() == 0)
			names = null;
		if (name != null && name.trim().length() == 0)
			name = null;
		if (go_id != null && go_id.trim().length() == 0)
			go_id = null;
		if (reaction_id != null && reaction_id.trim().length() == 0)
			reaction_id = null;
		if (ensembl_id != null && ensembl_id.trim().length() == 0)
			ensembl_id = null;
		if (uniprot_id != null && uniprot_id.trim().length() == 0)
			uniprot_id = null;
		if (hgnc_id != null && hgnc_id.trim().length() == 0)
			hgnc_id = null;
		if (omim_id != null && omim_id.trim().length() == 0)
			omim_id = null;
		if (entrez_id != null && entrez_id.trim().length() == 0)
			entrez_id = null;
		if (formula != null && formula.trim().length() == 0)
			formula = null;
		if (mass != null && mass.trim().length() == 0)
			mass = null;
		if (chebi != null && chebi.trim().length() == 0)
			chebi = null;
		if (three_dmet != null && three_dmet.trim().length() == 0)
			three_dmet = null;
		if (cas != null && cas.trim().length() == 0)
			cas = null;
		if (drugbank != null && drugbank.trim().length() == 0)
			drugbank = null;
		if (pubchem != null && pubchem.trim().length() == 0)
			pubchem = null;
    if (PDBeChem != null && PDBeChem.trim().length() == 0)
      PDBeChem = null;
    if (GlycomeDB != null && GlycomeDB.trim().length() == 0)
      GlycomeDB = null;
    if (LipidBank != null && LipidBank.trim().length() == 0)
      LipidBank = null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean queryWasSuccessfull() {
		return (informationFromKeggAdaptor != null);
	}
	
	 /**
   * 
   * @param go_id
   * @return
   */
  public static String getGo_id_with_MiriamURN(String go_id) {
    // So aufgebaut, da GO_id mehrere enthaelt! Eine funktion muss also
    // drueber iterieren und diese aufrugen.
    return miriam_urn_geneOntology
        + (go_id.contains(":") ? go_id.trim() : "GO:" + go_id.trim());
  }

  /**
   * @param keggId
   * @return Complete MIRIAM URN Including the given ID. Or <code>NULL</code>
   * if no such MIRIAM URN is available!
   */
  public static String getMiriamURIforKeggID(String keggId, EntryType et) {
    int pos = keggId.indexOf(':');
    if (pos <= 0) {
      // Try to infere prefix from id.
      String newKeggId = appendPrefix(keggId);
      pos = newKeggId.indexOf(':');
      if (pos>0) {
        log.info(String.format("Inferred prefix for partial KEGG ID. Was: '%s', is now: '%s'.", keggId, newKeggId));
        keggId=newKeggId;
      } else {
        log.warning(String.format("Invalid Kegg ID submitted. Please submit the full id e.g. 'cpd:12345'. You submitted: '%s'.", keggId));
        return null;
      }
    }
    String prefix = keggId.toLowerCase().trim();
    String suffix = keggId.substring(pos + 1).trim();
    String ret = "";

    // Add Kegg-id Miriam identifier
    if (prefix.startsWith("cpd:")) {
      ret = miriam_urn_kgCompound + suffix;
    } else if (prefix.startsWith("glycan:") || prefix.startsWith("gl:")) {
      ret = miriam_urn_kgGlycan + suffix;
    } else if (prefix.startsWith("ec:")) {
      ret = miriam_urn_ezymeECcode + suffix;
    } else if (prefix.startsWith("dr:")) {
      ret = miriam_urn_kgDrug + suffix;
    } else if (prefix.startsWith("rn:")) {
      ret = miriam_urn_kgReaction + suffix;
    } else if (prefix.startsWith("path:")) { // Link to another pathway
      ret = miriam_urn_kgPathway + suffix;
    } else if (prefix.startsWith("ko:")) {
      ret = miriam_urn_kgOrthology + suffix;
    } else if (prefix.startsWith("br:")) {
      // KEGG BRITE - no official MIRIAM URN available as of 2011-11-07
      // if it matches "^K\d+$", we can go with KEGG orthology, else
      // we will have to skip it.
      if (suffix.length()>2 && Character.toUpperCase(suffix.charAt(0))=='K' && 
          Character.isDigit(suffix.charAt(1))) {
        ret = miriam_urn_kgOrthology + suffix;
      } else {
        ret = null;
      }
    } else if (et == null || et != null
        && (et.equals(EntryType.gene) || et.equals(EntryType.ortholog))) {// z.B. hsa:00123, ko:00123
      ret = miriam_urn_kgGenes + keggId.trim().replace(":", "%3A"); // Be careful here: Don't trim to ':'! (Don't use suffix)
    } else {
      log.warning("Please implement MIRIAM urn for: '" + keggId
          + ((et != null) ? "' (" + et.toString() + ")." : "."));
      ret = null;
    }
    return ret;
  }
  
  /**
   * If s contains ":" => return values is all chars behind the ":". Else =>
   * return value is s.
   * 
   * @param s any String.
   * @return see above. Result is always trimmed.
   */
  public static String suffix(String s) {
    if (!s.contains(":")){
      return s.trim();
    }
    return (s.substring(s.indexOf(':') + 1)).trim();
  }
  
  /**
   * Prepends the KEGG prefix (e.g. "C00123" => "cpd:C00123").
   * Does not work for organism specific genes.
   * <p>Note that this is case sensitive and s should NOT be
   * uppercased or lowercased before usage!
   * @param s
   * @return
   */
  public static String appendPrefix(String s) {
    if (s==null) return s;
    
    int pos = s.indexOf(':');
    if (pos>0){
      return s.trim();
    } else if (pos==0) {
      return appendPrefix(s.substring(1));
    }
    
    if (s.length()>2) {
      char firstChar = s.charAt(0);
      if (Character.isDigit(s.charAt(1))) {
        firstChar = Character.toUpperCase(firstChar);
        
        if (firstChar=='C') {
          return "cpd:" + s;
        } else if (firstChar=='D') {
          return "dr:" + s;
        } else if (firstChar=='G') {
          return "gl:" + s;
        } else if (firstChar=='K' || firstChar=='E') {
          // "ko:E3.1.4.11" is also possible
          return "ko:" + s;
        } else if (firstChar=='R') {
          return "rn:" + s;
        }
        
        
      } else //if (Character.isLetter(firstChar) && Character.isLowerCase(firstChar) 
          //&& Character.isLetter(s.charAt(1)) && Character.isLetter(s.charAt(2))) {
        if (Pattern.matches("^\\w{2,3}\\d{5}$", s)) {
        // e.g. ("path:hsa00010")
        return "path:" + s;
      } else if (ECcodes.matcher(s).matches()){
        return "ec:"+s; // Enzyme codes
      } else {
        // genes, impossible without knowing the organism ("^\w+:[\w\d\.-]*$")
        // e.g. "hsa:1738"; also impossible is KEGG brite ("br:*" ids)
        log.warning(String.format("Warning: can not prepend unknown organism on possibly gene-id %s", s));
        return s;
      }
    } else {
      log.warning(String.format("Warning: can not prepend prefix to unknown id %s", s));
    }
    
    return s;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("[KEGGinfos for '%s' name:'%s' ...]", Kegg_ID, names);
  }

  
  
  /**
   * Examples for all different kegg databases
   * @param args
   * @throws TimeoutException 
   */
  public static void main(String[] args) {
    KeggAdaptor manag = new KeggAdaptor();
    
    System.out.println("=========KEGG BRITE==========");
    System.out.println(new KeggInfos("br:efa00194", manag).getNames());
    System.out.println(new KeggInfos("br:br08003", manag).getNames());
    System.out.println("=============================");
    
//    System.out.println("=========SPECIAL TESTS=======");
//    System.out.println(new KeggInfos("hsa:223", manag).getECcodes()); // \n between EC codes
//    System.out.println(new KeggInfos("efa:EF2928", manag).getECcodes()); // EC codes in orthology
    
    System.out.println("=========KEGG COMPOUNDS======");
    System.out.println(new KeggInfos("cpd:C00719", manag).getNames());
    System.out.println("=============================");
    
    System.out.println("=========KEGG DRUG===========");
    System.out.println(new KeggInfos("dr:D00123", manag).getNames());
    System.out.println("=============================");
    
    System.out.println("=========KEGG PATHWAY=========");
    System.out.println(new KeggInfos("path:hsa00010", manag).getNames());
    System.out.println("=============================");

    
    System.out.println("=========KEGG GENES==========");
    System.out.println(new KeggInfos("hsa:1738", manag).getNames());
    System.out.println("=============================");

    
    System.out.println("=========KEGG GLYCAN=========");
    System.out.println(new KeggInfos("gl:G02511", manag).getNames());
    System.out.println("=========KEGG GLYCAN-2=======");
    System.out.println(new KeggInfos("gl:G00123", manag).getNames());
    System.out.println("=============================");

    
    System.out.println("=========KEGG Orthology======");
    System.out.println(new KeggInfos("ko:K00001", manag).getNames());
    System.out.println("=============================");

    
    System.out.println("=========KEGG Reaction=======");
    System.out.println(new KeggInfos("rn:R00100", manag).getNames());
    System.out.println("=============================");

    
  }

}
