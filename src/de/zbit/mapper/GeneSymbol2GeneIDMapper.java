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
package de.zbit.mapper;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.ArrayUtils;
import de.zbit.util.logging.LogUtil;

/**
 * Maps Gene Symbols to NCBI Gene IDs (Entrez).
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GeneSymbol2GeneIDMapper extends Ensembl2GeneIDMapper {
  private static final long serialVersionUID = -3081619125111063947L;
  
  public static final Logger log = Logger.getLogger(GeneSymbol2GeneIDMapper.class.getName());
  
  /*
   * TODO: Implement second alternative if useNCBI_gene2ensembl_file=true.
   * File "ftp://ftp.ncbi.nih.gov/gene/DATA/gene_info.gz" contains mapping
   * of gene symbols the geneIDs. But take care of reading the symbols for
   * the correct organisms (not so easy to implement because of organism
   * diversity).
   */
  
  /**
   * @param organism in non-scientific format ("human", "mouse" or "rat").
   * @throws IOException
   */
  public GeneSymbol2GeneIDMapper(String organism) throws IOException {
    super(organism);
    useNCBI_gene2ensembl_file=false;
  }
  
  /**
   * @param organism in non-scientific format ("human", "mouse" or "rat").
   * @param progress
   * @throws IOException
   */
  public GeneSymbol2GeneIDMapper(String organism, AbstractProgressBar progress) throws IOException {
    super(organism, progress);
    useNCBI_gene2ensembl_file=false;
  }
  
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    GeneSymbol2GeneIDMapper mapper = new GeneSymbol2GeneIDMapper("human");
    System.out.println(mapper.map("Cyp3a4"));
    System.out.println(mapper.map("CYP3A4"));
    System.out.println(mapper.map("CYP3A3"));
    System.out.println(mapper.map("CYP3A4-0001"));
    
    System.out.println(mapper.map("SLC6A3")); // DAT: // http://www.genenames.org/data/hgnc_data.php?hgnc_id=11049
    System.out.println(mapper.map("ComplexI"));
    System.out.println(mapper.map("PLD2"));
    System.out.println(mapper.map("CaseinkinaseI"));
    System.out.println(mapper.map("Casein kinaseII"));
    System.out.println(mapper.map("GRK"));
    System.out.println(mapper.map("20S proteasome"));
    System.out.println(mapper.map("Parkin"));
    System.out.println(mapper.map("_alpha_-Synuclein"));
    System.out.println(mapper.map("UCH-L1"));
    System.out.println(mapper.map("Synphilin"));
    System.out.println(mapper.map("1433"));
    System.out.println(mapper.map("Fynkinase"));
    System.out.println(mapper.map("Srckinase"));
    System.out.println(mapper.map("19Sproteasome"));
    System.out.println(mapper.map("Elk1"));
    System.out.println(mapper.map("Hsp70"));
    System.out.println(mapper.map("betaSynuclein"));
    System.out.println(mapper.map("gammaSynuclein"));
    System.out.println(mapper.map("TorsinA"));
    System.out.println(mapper.map("UbcH7"));
    System.out.println(mapper.map("UbcH8"));
    System.out.println(mapper.map("ERK"));
    System.out.println(mapper.map("SAPK"));
    System.out.println(mapper.map("p38MAPK"));
    System.out.println(mapper.map("aelR"));
    System.out.println(mapper.map("UBC6"));
    System.out.println(mapper.map("Ubc7"));
    System.out.println(mapper.map("CyclinE"));
    System.out.println(mapper.map("hSel10"));
    System.out.println(mapper.map("Cul1"));
    System.out.println(mapper.map("CASK"));
    System.out.println(mapper.map("TH"));
    System.out.println(mapper.map("CDCrel1"));
    System.out.println(mapper.map("Syntaxin"));
    System.out.println(mapper.map("CHIP"));
    
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "GeneSymbol2GeneID";
  }
  
  public int[] getMultiSourceColumn(CSVReader r) {
    Collection<Integer> sourceCols = new LinkedList<Integer>();
    int c;
    c = r.getColumnContaining("Gene", "Name");
    if (c>=0) sourceCols.add(c);
    c = r.getColumnContaining("Gene", "Symbol");
    if (c>=0) sourceCols.add(c);
    
    return ArrayUtils.toIntArray(sourceCols.toArray(new Integer[0]));
  }
  
}
