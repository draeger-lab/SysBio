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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;

import de.zbit.kegg.api.KeggInfos;
import de.zbit.util.DatabaseIdentifiers.DatabaseContent;
import de.zbit.util.DatabaseIdentifiers.IdentifierDatabases;

/**
 * This class conatins various Tools for {@link DatabaseIdentifiers}.
 * The tools contained here are distinct to those in {@link DatabaseIdentifiers}
 * because they do not only work on data structures in {@link DatabaseIdentifiers}
 * but also, e.g. on {@link CVTerm}, or other data structures.
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class DatabaseIdentifierTools {
  private static final transient Logger log = Logger.getLogger(DatabaseIdentifierTools.class.getName());
  
  /**
   * 
   * @param ids either a {@link String} (for single identifiers) or an Array of Strings 
   * or any {@link Iterable} instance of strings, for multiple identifiers per
   * database.
   * @param myCVterm
   * @param miriam_URNPrefix
   */
  @SuppressWarnings("rawtypes")
  private static void appendAllIds(Object ids, CVTerm myCVterm, String miriam_URNPrefix) {
    // check if we have a single identifier
    if (ids==null) return;
    
    else if (ids instanceof String) {
      String urn = miriam_URNPrefix + KeggInfos.suffix(ids.toString());
      if (!myCVterm.getResources().contains(urn)) {
        myCVterm.addResource(urn);
      }
    }
    
    else if (ids.getClass().isArray()) {
      ids = Arrays.asList(ids);
      appendAllIds(ids, myCVterm, miriam_URNPrefix);
      
    } else if (ids instanceof Iterable) {
      Iterator it = ((Iterable)ids).iterator();
      while (it.hasNext()) {
        appendAllIds(it.next(), myCVterm, miriam_URNPrefix);
      }
      
    } else {
      log.warning("Can not add an identifier of type " + ids.getClass().getSimpleName());
    }
  }
  
  
  /**
   * Convert a map of various {@link DatabaseIdentifiers} to MRIAM URNs
   * in CVTerms.
   * @param ids a map that points from {@link DatabaseIdentifiers} to either
   * {@link String}s (for single identifiers) or an Array of Strings or
   * any {@link Iterable} instance of strings, for multiple identifiers per
   * database.
   * @return a list of {@link CVTerm}s containing all given
   * identifiers.
   */
  public static List<CVTerm> getCVTerms(Map<DatabaseIdentifiers.IdentifierDatabases, ?> ids) {
    return getCVTerms(ids, null);
  }
  
  /**
   * Convert a map of various {@link DatabaseIdentifiers} to MRIAM URNs
   * in CVTerms.
   * @param ids a map that points from {@link DatabaseIdentifiers} to either
   * {@link String}s (for single identifiers) or an Array of Strings or
   * any {@link Iterable} instance of strings, for multiple identifiers per
   * database.
   * @param pointOfView
   * @return
   */
  public static List<CVTerm> getCVTerms(Map<DatabaseIdentifiers.IdentifierDatabases, ?> ids, String pointOfView) {
    List<CVTerm> ret = new ArrayList<CVTerm>();
    if (ids==null) return ret;
    if (pointOfView==null||pointOfView.length()<1) {
      pointOfView = "protein"; // Default point of view
    }
    
    for (IdentifierDatabases db: ids.keySet()) {
      Object id = ids.get(db);
      
      CVTerm mycv = new CVTerm();
      mycv.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
      mycv.setBiologicalQualifierType(getBQBQualifier(db, pointOfView, getFirstString(id)));
      
      // Append all CV Terms
      String miriam_URNPrefix = DatabaseIdentifiers.getMiriamURN(db);
      appendAllIds(id, mycv, miriam_URNPrefix);
      
      // Set BQB to IS or Has_Version
      if (mycv.getResourceCount() > 1 && 
          mycv.getBiologicalQualifierType().equals(CVTerm.Qualifier.BQB_IS)) {
        // Multiple proteins in one node
        mycv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_VERSION);
      }
      
      // Add to final list
      if (mycv.getResourceCount() > 0) {
        ret.add(mycv);
      }
    }
    
    return ret;
  }
  
  
  /**
   * @param id
   * @return
   */
  @SuppressWarnings("rawtypes")
  private static String getFirstString(Object ids) {
    if (ids==null) return null;
    
    else if (ids instanceof String) {
      return ids.toString();
      
    } else if (ids.getClass().isArray()) {
      ids = Arrays.asList(ids);
      return getFirstString(ids);
      
    } else if (ids instanceof Iterable) {
      Iterator it = ((Iterable)ids).iterator();
      while (it.hasNext()) {
        return it.next().toString();
      }
    }
    
    return ids.toString();
  }


  /**
   * Get a biological {@link Qualifier} describing the interaction of a
   * database identifier and the described object.
   * @param db
   * @param pointOfView should determine if the described object is a "protein"
   * or a "gene" or a "dna", etc.
   * @return
   */
  public static Qualifier getBQBQualifier(IdentifierDatabases db, String pointOfView) {
    return getBQBQualifier(db, pointOfView, null);
  }
  
  /**
   * Get a biological {@link Qualifier} describing the interaction of a
   * database identifier and the described object.
   * @param db
   * @param pointOfView should determine if the described object is a "protein"
   * or a "gene" or a "dna", etc.
   * @param identifier the actual identifier for which you want to
   * determine the BQB qualifier
   * @return
   */
  public static Qualifier getBQBQualifier(IdentifierDatabases db, String pointOfView, String identifier) {
    identifier = identifier.toUpperCase().trim();
    // Get database content
    DatabaseContent c = DatabaseIdentifiers.getDatabaseType(db);
    if (c==null) return Qualifier.BQB_UNKNOWN;
    
    // Catch all gene-gene, protein-protein, etc. cases
    if (DatabaseContent.valueOf(pointOfView)!=null) {
      return Qualifier.BQB_IS;
    }
    
    // Switch content and pointOfView
    switch (c) {
      case RNA:
        break;
      case annotation:
        return Qualifier.BQB_HAS_PROPERTY;
      case description:
        return Qualifier.BQB_IS_DESCRIBED_BY;
      case enzyme:
        // EC-Code is just a functional annotation of an enzyme.
        return Qualifier.BQB_HAS_PROPERTY;
      case gene:
        if (pointOfView.equalsIgnoreCase("rna") || 
            pointOfView.replaceAll("\\s_", "").equalsIgnoreCase("protein")) {
          return Qualifier.BQB_IS_ENCODED_BY;
        }
        break;
      case omics:
        // Try to catch some "IS" cases
        if (identifier!=null) {
          if ((identifier.startsWith("ENSG") && pointOfView.equalsIgnoreCase("gene")) ||
              (identifier.startsWith("ENSP") && pointOfView.equalsIgnoreCase("protein")) ||
              (identifier.startsWith("ENST") && pointOfView.equalsIgnoreCase("transcript"))) {
            return Qualifier.BQB_IS;
          }
        }
        if ((identifier.startsWith("ENSG") && pointOfView.equalsIgnoreCase("protein"))) {
          return Qualifier.BQB_IS_ENCODED_BY;
        }
        if (( (identifier.startsWith("ENSG") || identifier.startsWith("ENST")) 
            && pointOfView.equalsIgnoreCase("protein"))) {
          return Qualifier.BQB_ENCODES;
        }
        break;
      case pathway:
        return Qualifier.BQB_OCCURS_IN;
      case protein:
        if (pointOfView.equalsIgnoreCase("gene") ||
            pointOfView.equalsIgnoreCase("dna")) {
          return Qualifier.BQB_ENCODES;
        }
        break;
      case protein_interaction:
        break;
      case publication:
        break;
      case reaction:
        return Qualifier.BQB_OCCURS_IN; 
      case sequences:
        break;
      case structures:
        return Qualifier.BQB_HAS_PROPERTY;
      case small_molecule:
        if (pointOfView.equalsIgnoreCase("compound") || 
            pointOfView.replaceAll("\\s_", "").equalsIgnoreCase("smallmolecule")) {
          return Qualifier.BQB_IS;
        }
        break;
      case taxonomy:
        return Qualifier.BQB_OCCURS_IN;
    }
    
    return Qualifier.BQB_UNKNOWN;
    
    // TODO: check and remove if correct.
    //cvtKGID.setBiologicalQualifierType(Qualifier.BQB_IS);
    //cvtEntrezID.setBiologicalQualifierType(Qualifier.BQB_IS_ENCODED_BY);
     //cvtOmimID.setBiologicalQualifierType(Qualifier.BQB_HAS_PROPERTY);
    //cvtEnsemblID.setBiologicalQualifierType(Qualifier.BQB_IS_ENCODED_BY);
    
    //cvtUniprotID.setBiologicalQualifierType(Qualifier.BQB_HAS_VERSION);
    //cvtChebiID.setBiologicalQualifierType(Qualifier.BQB_IS);
     //cvtDrugbankID.setBiologicalQualifierType(Qualifier.BQB_IS);
//    cvtGoID.setBiologicalQualifierType(Qualifier.BQB_IS_DESCRIBED_BY);
//    cvtHGNCID.setBiologicalQualifierType(Qualifier.BQB_IS_ENCODED_BY);
    
//    cvtPubchemID.setBiologicalQualifierType()Qualifier.BQB_HAS_PROPERTY;
//     cvt3dmetID.setBiologicalQualifierType(Qualifier.BQB_HAS_PROPERTY);
     
//    cvtReactionID.setBiologicalQualifierType(Qualifier.BQB_OCCURS_IN);
//    cvtTaxonomyID.setBiologicalQualifierType(Qualifier.BQB_OCCURS_IN);
//    // New as of October 2010:
//     PDBeChem.setBiologicalQualifierType(Qualifier.BQB_IS);
//     GlycomeDB.setBiologicalQualifierType(Qualifier.BQB_HAS_PROPERTY);
//    LipidBank.setBiologicalQualifierType(Qualifier.BQB_HAS_PROPERTY);
//     ECNumbers.setBiologicalQualifierType(Qualifier.BQB_HAS_PROPERTY);
     
     /* Some Biological qualifier BQB is set later!
      * - KEGG
      * - Uniprot
      * - Chebi
      * - Drugbank
      * - PDBeChem
      */
    
  }


  /**
   * Get the KEGG database from a KEGG identifier.
   * @param keggID
   * @return
   */
  public static IdentifierDatabases getKEGGdbFromID(String keggID) {
    for (IdentifierDatabases db : DatabaseIdentifiers.IdentifierDatabases.values()) {
      if (db==IdentifierDatabases.KEGG_Genes) continue;
      if (db.toString().toLowerCase().startsWith("kegg")) {
        if (DatabaseIdentifiers.getFormattedID(db, keggID)!=null) {
          return db;
        }
      }
    }
    
    if (DatabaseIdentifiers.getFormattedID(IdentifierDatabases.KEGG_Genes, keggID)!=null) {
      return IdentifierDatabases.KEGG_Genes;
    }

    return null;
  }
  
  
  
}
