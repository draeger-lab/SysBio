/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.util.Species;
import de.zbit.util.StringUtil;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressBar;

/**
 * A mapper to map Nimblegen Probe Identifiers (currently from DNA methylation chips
 * for mouse and rat) to Ensembl Gene Identifiers.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class NimblegenProbeID2EnsemblIDMapper extends AbstractMapper<String, String> {
  private static final long serialVersionUID = 5831049646066089602L;

  public static final Logger log = Logger.getLogger(NimblegenProbeID2EnsemblIDMapper.class.getName());

  /**
   * The species to map ids for.
   */
  private Species species = null;

  /**
   * If true, uses the extended file (-10,000/2000 bps around each tss).
   * If false, uses a limited file (-2000/+500 bps around each tss).
   */
  private boolean extended;
  
  /**
   * If true, skips all probes that match defined regions
   * (see {@link #extended}) of multiple genes. If false,
   * reads all available mappings.
   */
  private boolean onlyTakeUniqueMappings;
  
  
  public NimblegenProbeID2EnsemblIDMapper(Species species) throws IOException {
    this(species, null);
  }
  
  public NimblegenProbeID2EnsemblIDMapper (Species species, AbstractProgressBar progress) throws IOException {
    this(species, progress, false, true);
  }
  
  /**
   * 
   * @param species only mapping data for this species is read. Currently mouse or rat is supported.
   * @param progress any {@link AbstractProgressBar} or {@code null}.
   * @param readExtendedFile see {@link #extended}!
   * @param onlyTakeUniqueMappings see {@link #onlyTakeUniqueMappings}!
   * @throws IOException
   */
  public NimblegenProbeID2EnsemblIDMapper(Species species, AbstractProgressBar progress,
    boolean readExtendedFile, boolean onlyTakeUniqueMappings) throws IOException {
    // This constructor is called from every other!
    super(String.class, String.class, progress);
    this.species = species;
    this.extended = readExtendedFile;
    this.onlyTakeUniqueMappings = onlyTakeUniqueMappings;
    init();
  }
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    NimblegenProbeID2EnsemblIDMapper mapper = new NimblegenProbeID2EnsemblIDMapper(new Species("mmu", "Mus musculus", "_MOUSE"), new ProgressBar(0));
    System.out.println(mapper.map("CHR10FS100050969")); // CYP2B20
    System.out.println(mapper.map("CHR09FS061876008")); // ENSMUSG00000032278
    
    // TEMP: add a new ensemblID column to an existing file.
    mapper.addEnsemblGeneIDColumnToFile("Y:/Marcar-Daten-Kopie/EKUTA_tumors_Liver_Tumor/NimblegenRawData/Raw_Data_Files/Result_Ctnnb1/mmu_Ctnnb1.txt");
  }
  
  /**
   * Takes any character separated value (CSV) input file that contains
   * a column with Nimblegen Probe IDs (is detected automatically) and appends
   * a column with corresponding Ensembl Gene ID.
   * @param inputFile
   * @throws Exception
   */
  public void addEnsemblGeneIDColumnToFile(String inputFile) throws Exception {
    // Open input file and get source col
    CSVReader in = new CSVReader(inputFile);
    in.setProgressBar(progress);
    int sourceCol = in.getColumnByMatchingContent("CHR.+FS.+");
    
    // open target file
    inputFile = new File(inputFile).getName(); // XXX: redirect to current working directory
    BufferedWriter out = new BufferedWriter(new FileWriter(removeFileExtension(inputFile) + ".plusEns.txt"));
    // write preamble
    String preamble = in.getPreamble();
    if (preamble!=null && preamble.length()>0) {
      out.write(in.getPreamble());
      out.write('\n');
    }
    // write header line
    out.write(StringUtil.implode(in.getHeader(), "\t"));
    out.write("\tEnsemblGeneID");
    out.write('\n');
    
    // Process input file and apped new column
    String[] line=null;
    while ((line=in.getNextLine())!=null) {
      // write old cols
      out.write(StringUtil.implode(line, "\t"));
      // write new col
      String mapped = map(line[sourceCol]);
      out.write('\t');
      out.write(mapped==null?"n/a":mapped);
      out.write('\n');
    }
    out.close();
    
  }
  
  /**
   * If the input has a file extension, it is removed. else, the input is
   * returned.
   * 
   * @param input
   * @return
   */
  private static String removeFileExtension(String input) {
    int pos = input.lastIndexOf('.');
    if (pos > 0) { return input.substring(0, pos); }
    return input;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    String prefix = null;
    if (species.getKeggAbbr().equalsIgnoreCase("mmu")) {
      prefix = "mm9";
    } else if (species.getKeggAbbr().equalsIgnoreCase("rno")) {
      prefix = "rn34";
    } else {
      log.severe(String.format("No %s file known for species %s.", getMappingName(), species));
    }
    
    return "res/"+prefix+"-probes-near-tss" + (extended ?"Ext":"") + ".txt";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "NimblegenProbeID2EnsemblGeneID";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return null; // Not available.
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 1;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#skipLine(java.lang.String[])
   */
  @Override
  protected boolean skipLine(String[] line) {
    if (onlyTakeUniqueMappings) {
      boolean duplicated = false;
      try {
        duplicated = Boolean.parseBoolean(line[2]);
      } catch (Exception e) {
        log.log(Level.WARNING, "", e);
      }
      return duplicated;
    }
    
    return false;
  }
  

}
