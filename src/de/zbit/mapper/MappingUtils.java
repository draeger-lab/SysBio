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
package de.zbit.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.zbit.mapper.compounds.CAS2CompoundIDMapper;
import de.zbit.mapper.compounds.ChEBI2CompoundIDMapper;
import de.zbit.mapper.compounds.ChemSpider2CompoundIDMapper;
import de.zbit.mapper.compounds.CompoundSynonym2CompoundIDMapper;
import de.zbit.mapper.compounds.HMDB2CompoundIDMapper;
import de.zbit.mapper.compounds.InChI2CompoundIDMapper;
import de.zbit.mapper.compounds.InChIKey2ComoundIDMapper;
import de.zbit.mapper.compounds.KeggCompound2CompoundIDMapper;
import de.zbit.mapper.compounds.PubChemCompound2CompoundIDMapper;
import de.zbit.mapper.probes.ProbeID2GeneIDMapper;
import de.zbit.mapper.probes.ProbeID2GeneIDMapper.Manufacturer;
import de.zbit.util.DatabaseIdentifiers;
import de.zbit.util.Species;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class contains utilities for {@link AbstractMapper}s.
 * 
 * <p>It mainly contains a list of supported Identifiers ({@link IdentifierType})
 * and can initialize an {@link AbstractMapper}, based on a list element
 * ({@link #initialize2GeneIDMapper(IdentifierType, AbstractProgressBar, String)}).
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class MappingUtils {
  private static final transient Logger log = Logger.getLogger(MappingUtils.class.getName());
  
  
  /**
   * An enumeration of different gene identifiers.
   * <b>All these identifiers should be mappable to either GeneID or CompoundID.</b>
   * <p>Note: If you change this list, please also change the
   * {@link MappingUtils#initialize2GeneIDMapper(IdentifierType, AbstractProgressBar, String)}
   * method.
   * @author Clemens Wrzodek, Lars Rosenbaum
   */
  public static enum IdentifierType {
    // We can not use the "DatabaseIdentifiers" class here, because
    // we need a subset of identifiers that are mappable to geneID or compoundID
    // and have an actual 2GeneID implementation here.
  	// (2GeneID implementation also performs the mapping of compounds)
    Unknown(IdentifierClass.Gene),
    NCBI_GeneID(IdentifierClass.Gene),
    RefSeq(IdentifierClass.Gene),
    Ensembl(IdentifierClass.Gene),
    KeggGenes(IdentifierClass.Gene),
    UniProt(IdentifierClass.Gene),
    GeneSymbol(IdentifierClass.Gene),
    Affymetrix(IdentifierClass.Gene),
    Agilent(IdentifierClass.Gene),
    Illumina(IdentifierClass.Gene),
    
    HMDB(IdentifierClass.Compound),
    KeggCompound(IdentifierClass.Compound),
    InChI(IdentifierClass.Compound),
    InChIKey(IdentifierClass.Compound),
    ChEBI(IdentifierClass.Compound),
    CAS(IdentifierClass.Compound),
    ChemSpider(IdentifierClass.Compound),
    PubChem_compound(IdentifierClass.Compound),
    CompoundSynonym(IdentifierClass.Compound),
    UnknownCompound(IdentifierClass.Compound);
    
    private IdentifierClass identClass;
    
    private IdentifierType(IdentifierClass identClass) {
    	this.identClass = identClass;
    }
    
    public IdentifierClass getIdentifierClass() {
    	return this.identClass;
    }
    
    /**
     * @return all available {@link IdentifierClass#Gene} identifiers.
     */
    public static IdentifierType[] getGeneIdentifierTypes(){
      return getAllIdentifers(IdentifierClass.Gene);
    }
    
    /**
     * @return all available {@link IdentifierClass#Compound} identifiers.
     */
    public static IdentifierType[] getCompoundIdentifierTypes(){
      return getAllIdentifers(IdentifierClass.Compound);
    }
    
    /**
     * Returns all available identifiers with {@link IdentifierClass} {@code clas}.
     * @param clas
     * @return
     */
    public static IdentifierType[] getAllIdentifers(IdentifierClass clas) {
      List<IdentifierType> ret = new ArrayList<IdentifierType>();
      for (IdentifierType v : values()) {
        if (clas==null || v.getIdentifierClass().equals(clas)) {
          ret.add(v);
        }
      }
      return ret.toArray(new IdentifierType[0]);
    }
  }
  
  
  /**
   * An enumeration which lists the class to which an IdentifierType belongs to
   * @author Lars Rosenbaum
   * @version $Rev$
   */
  public static enum IdentifierClass {
  	Gene,Compound;
  }
  
  /**
   * Get a regular expression for each {@link IdentifierType}.
   * @param clas any {@link IdentifierClass} or {@code null} to get all.
   * @return regular expressions for each {@link IdentifierType} in
   * the same ordering as {@link IdentifierType#values()}.
   */
  public static String[] getRegularExpressionsFor(IdentifierClass clas) {
    IdentifierType[] values = IdentifierType.getAllIdentifers(clas);
    String[] ret = new String[values.length];
    
    for (int i=0; i<values.length; i++) {
      ret[i] = getRegularExpressionForIdentifier(values[i]);
    }
    
    return ret;
  }
  
  /**
   * Return a regular expression to identify a certain identifier.
   * @param identifier
   * @return 
   */
  public static String getRegularExpressionForIdentifier(IdentifierType identifier) {
    // Use the new class "DatabaseIdentifiers" for this method.
    switch (identifier) {
      // Gene identifiers
      case NCBI_GeneID:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.EntrezGene, false);
      case RefSeq:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.RefSeq, false);
      case Ensembl:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.Ensembl, false);
      case KeggGenes:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.KEGG_Genes, false);
      case UniProt:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.UniProt_AC, false);
      case GeneSymbol:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.GeneSymbol, false);
      case Affymetrix:
        return "\\d+(_[a-z])?_at";
      case Agilent:
        return "A_\\d{2}_[A-Z0-9]{6,8}";
      case Illumina:
        return "ILMN_\\d+";
        
      // Compound identifiers
      case HMDB:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.HMDB, false);
      case KeggCompound:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.KEGG_Compound, false);
      case InChI:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.InChI, false);
      case InChIKey:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.InChIKey, false);
      case ChEBI:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.ChEBI, false);
      case CAS:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.CAS, false);
      case ChemSpider:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.ChemSpider, false);
      case PubChem_compound:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.PubChem_compound, false);
        
      default:
        // Search for an equally named instance in the "DatabaseIdentifiers.IdentifierDatabases" class.
        DatabaseIdentifiers.IdentifierDatabases dbID = null;
        try {
          dbID = DatabaseIdentifiers.IdentifierDatabases.valueOf(identifier.toString());
        } catch (Exception e) {} // Intentionally ignored.
        if (dbID!=null) {
          return DatabaseIdentifiers.getRegularExpressionForIdentifier(dbID, false);
        } else {
          return null;
        }
    }
  }
  
  
  /**
   * Initializes an X to GeneID mapper.
   * @param sourceIDtype - see {@link IdentifierType}
   * @param progress - Optional progress bar, used for downloading or reading the mapping flatfile. May be null.
   * @param speciesCommonName - For Ensembl or GeneSymbol, the species common name (e.g., "human") is required.
   * @return
   * @throws IOException
   */
  public static AbstractMapper<String, Integer> initialize2GeneIDMapper(IdentifierType sourceIDtype, AbstractProgressBar progress, Species species) throws IOException {
    // Init mapper based on targetIDtype
    AbstractMapper<String, Integer> mapper = null;
    if (sourceIDtype!=null) {
    	if(sourceIDtype.getIdentifierClass().equals(IdentifierClass.Gene)) {
    	  log.info("Initializing 2GeneID mapper...");
	      if (sourceIDtype.equals(IdentifierType.RefSeq)) {
	        mapper = new RefSeq2GeneIDMapper(progress, species.getNCBITaxonID());
	      } else if (sourceIDtype.equals(IdentifierType.Ensembl)) {
	        mapper = new Ensembl2GeneIDMapper(species.getCommonName(), progress);
	      } else if (sourceIDtype.equals(IdentifierType.GeneSymbol)) {
	        mapper = new GeneSymbol2GeneIDMapper(species.getCommonName(), progress);
	      } else if (sourceIDtype.equals(IdentifierType.KeggGenes)) {
	        mapper = new KeggGenesID2GeneID(species.getKeggAbbr(), progress);
	      } else if (sourceIDtype.equals(IdentifierType.UniProt)) {
	        mapper = new UniProt2GeneIDmapper(species, progress);
	      } else if (sourceIDtype.equals(IdentifierType.Affymetrix)) {
	        mapper = new ProbeID2GeneIDMapper(progress, Manufacturer.Affymetrix, species.getCommonName());
	      } else if (sourceIDtype.equals(IdentifierType.Agilent)) {
	        mapper = new ProbeID2GeneIDMapper(progress, Manufacturer.Agilent, species.getCommonName());
	      } else if (sourceIDtype.equals(IdentifierType.Illumina)) {
	        mapper = new ProbeID2GeneIDMapper(progress, Manufacturer.Illumina, species.getCommonName());
	      }
	      
	      
    	} else if(sourceIDtype.getIdentifierClass().equals(IdentifierClass.Compound)) {
    	  log.info("Initializing 2CompoundID mapper...");
        if (sourceIDtype.equals(IdentifierType.HMDB)) {
          mapper = new HMDB2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.KeggCompound)) {
          mapper = new KeggCompound2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.InChI)) {
          mapper = new InChI2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.InChIKey)) {
          mapper = new InChIKey2ComoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.ChEBI)) {
          mapper = new ChEBI2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.CAS)) {
          mapper = new CAS2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.ChemSpider)) {
          mapper = new ChemSpider2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.PubChem_compound)) {
          mapper = new PubChemCompound2CompoundIDMapper(progress);
        } else if (sourceIDtype.equals(IdentifierType.CompoundSynonym)) {
          mapper = new CompoundSynonym2CompoundIDMapper(progress);
        }
    		
    	}
    }
    return mapper;
  }

}
