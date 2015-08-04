/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.io.filefilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

import de.zbit.io.FileTools;
import de.zbit.io.OpenFile;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.objectwrapper.ValuePairUncomparable;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @author Florian Mittag
 * @date 2007-08-03
 * @version $Rev$
 * @since 1.0
 */
public class SBFileFilter extends GeneralFileFilter {
  
  /**
   * 
   * @author Andreas Dr&auml;ger
   * @author Clemens Wrzodek
   * 
   */
  /**
   * @author Florian Mittag
   * @version $Rev$
   */
  public static enum FileType {
    /**
     * A filter for assoc files
     */
    ASSOC_FILES,
    /**
     * A filter for bed files
     */
    BED_FILES,
    /**
     * A filter for bim files
     */
    BIM_FILES,
    /**
     * To be selected if BioPAXL files (XML files) can be chosen.
     */
    BioPAX_FILES,
    /**
     * To be selected if BioPAXL files (XML files) of Level 2 can be chosen.
     */
    BioPAX_FILES_L2,
    /**
     * To be selected if BioPAXL files (XML files) of Level 3 can be chosen.
     */
    BioPAX_FILES_L3,
    /**
     * To be selected for CDT (clustered data table) files.
     */
    CDT_FILES,
    /**
     * To be selected if CSV files (comma/character separated files) can be chosen.
     */
    CSV_FILES,
    /**
     * True if this filter accepts directories only (no files).
     */
    DIRECTORIES_ONLY,
    /**
     * A filter for fam files
     */
    FAM_FILES,
    /**
     * True if this filter accepts GIF files.
     */
    GIF_FILES,
    /**
     * True if this filter accepts GML files.
     */
    GML_FILES,
    /**
     * True if this filter accepts GraphML files.
     */
    GRAPHML_FILES,
    /**
     * A file filter type for HTML files.
     */
    HTML_FILES,
    /**
     * Filter for hwe files
     */
    HWE_FILES,
    /**
     * True if this filter accepts JPEG picture files.
     */
    JPEG_FILES,
    /**
     * For the JSON file format (JavaScript Object Notation).
     */
    JSON_FILES,
    /**
     * KEGG Markup Language files.
     */
    KGML_FILES,
    /**
     * Log files for loogers.
     */
    LOG_FILE,
    /**
     * A file filter for map files
     */
    MAP_FILES,
    /**
     * A file filter for Web-Ontology-Language (OWL files).
     */
    OWL_FILES,
    /**
     * A file filter for portable document format files.
     */
    PDF_FILES,
    /**
     * File filter for PED_FILES
     */
    PED_FILES,
    /**
     * PLINK genotype files (.ped and .bed)
     */
    PLINK_GENOTYPE_FILES,
    /**
     * True if this filter accepts portable network graphic files.
     */
    PNG_FILES,
    /**
     * To be selected if SBGN files (XML files) can be chosen.
     */
    SBGN_FILES,
    /**
     * To be selected if SBML files (XML files) can be chosen.
     */
    SBML_FILES,
    /**
     * To be selected if SBML files (XML files) of Level 1 Version 1 can be chosen.
     */
    SBML_FILES_L1V1,
    /**
     * To be selected if SBML files (XML files) of Level 1 Version 2 can be chosen.
     */
    SBML_FILES_L1V2,
    /**
     * To be selected if SBML files (XML files) of Level 2 Version 1 can be chosen.
     */
    SBML_FILES_L2V1,
    /**
     * To be selected if SBML files (XML files) of Level 2 Version 2 can be chosen.
     */
    SBML_FILES_L2V2,
    /**
     * To be selected if SBML files (XML files) of Level 2 Version 3 can be chosen.
     */
    SBML_FILES_L2V3,
    /**
     * To be selected if SBML files (XML files) of Level 2 Version 4 can be chosen.
     */
    SBML_FILES_L2V4,
    /**
     * To be selected if SBML files (XML files) of Level 2 Version 5 can be chosen.
     */
    SBML_FILES_L2V5,
    /**
     * To be selected if SBML files (XML files) of Level 3 Version 1 can be chosen.
     */
    SBML_FILES_L3V1,
    /**
     * To be selected if SBML files (XML files) of Level 3 Version 2 can be chosen.
     */
    SBML_FILES_L3V2,
    /**
     * To be selected if SIF files (pathway exchange, used by Cytoscape) can be chosen.
     */
    SIF_FILES,
    /**
     * To be selected if SVG files (Scalable Vector Graphics) can be chosen.
     */
    SVG_FILES,
    /**
     * True if this filter accepts (La)TeX files.
     */
    TeX_FILES,
    /**
     * True if this filter accepts plain ASCII files
     */
    TEXT_FILES,
    /**
     * True if this filter accepts TGF (trivial graph format) files.
     */
    TGF_FILES,
    /**
     * To be selected if TSV files (tab separated text files) can be chosen.
     */
    TSV_FILES,
    /**
     * If not specified this is the type.
     */
    UNDEFINED,
    /**
     * True if this filter accepts YGF (Y Graph Format) files.
     */
    YGF_FILES;
    
    /**
     * 
     * @return
     */
    public Set<String> getFileExtensions() {
      Set<String> extensions = new TreeSet<String>();
      String string = toString();
      switch (this) {
        case DIRECTORIES_ONLY:
          return extensions;
        case HTML_FILES:
          extensions.add("htm");
          extensions.add("html");
          break;
        case JPEG_FILES:
          extensions.add("jpg");
          break;
        case JSON_FILES:
          extensions.add("json");
          break;
        case KGML_FILES:
          extensions.add("xml");
          return extensions;
        case LOG_FILE:
          extensions.add("log");
          // ---- please keep the following order
        case BioPAX_FILES:
        case BioPAX_FILES_L2:
          extensions.add("bp2");
        case BioPAX_FILES_L3:
          if (!equals(BioPAX_FILES_L2)) {
            extensions.add("bp3");
          }
        case OWL_FILES:
          extensions.add("owl");
          extensions.add("xml");
          return extensions;
          // ----
        case PLINK_GENOTYPE_FILES:
          extensions.add("ped");
          extensions.add("bed");
          return extensions;
        case SBGN_FILES:
        case SBML_FILES:
        case SBML_FILES_L1V1:
        case SBML_FILES_L1V2:
        case SBML_FILES_L2V1:
        case SBML_FILES_L2V2:
        case SBML_FILES_L2V3:
        case SBML_FILES_L2V4:
        case SBML_FILES_L2V5:
        case SBML_FILES_L3V1:
        case SBML_FILES_L3V2:
          extensions.add("xml");
          break;
        case TEXT_FILES:
          extensions.add("txt");
          return extensions;
        case TSV_FILES:
          return extensions;
        default:
          break;
      }
      if (string.contains("_")) {
        extensions.add(toString().substring(0, toString().indexOf("_"))
          .toLowerCase());
      }
      return extensions;
    }
    
    /**
     * @return a pattern for one of the top-most lines to be matched in order to
     *         accept a file of the given type.
     */
    public Pattern getLinePattern() {
      if (this == KGML_FILES) {
        return Pattern.compile("<!DOCTYPE[\\p{ASCII}]*KGML[\\p{ASCII}]*>", Pattern.MULTILINE & Pattern.DOTALL);
      }
      if (toString().startsWith(SBML_FILES.toString())) {
        String anyChar = "[\\s\\w\\p{ASCII}]*";
        String whiteSpace = "[\\s]+";
        String number = "[1-9]+[0-9]*";
        String level = number, version = number;
        String sbmlDef = "<sbml%s%s((level[\\s]*=[\\s]*[\"']%s[\"']%s%sversion[\\s]*=[\\s]*[\"']%s[\"'])|(version[\\s]*=[\\s]*[\"']%s[\"']%s%slevel[\\s]*=[\\s]*[\"']%s[\"']))%s>";
        if (this != SBML_FILES) {
          level = toString().substring(12, 13);
          version = toString().substring(14);
        }
        return Pattern.compile(String.format(sbmlDef, whiteSpace,
          anyChar, level, whiteSpace, anyChar, version, version, whiteSpace,
          anyChar, level, anyChar), Pattern.MULTILINE
          & Pattern.DOTALL);
      }
      
      if (toString().startsWith(BioPAX_FILES.toString())) {
        // Parse a level from file filter string
        Pattern levelPattern = Pattern.compile("BioPAX_FILES_L(\\d)+");
        int level = 0;
        Matcher m = levelPattern.matcher(toString());
        if (m.find()) {
          level = Integer.parseInt(m.group(1));
        }
        
        return Pattern.compile("biopax-level" + (level <= 0 ? "\\d" : level) + ".owl");
      }
      return null;
    }
  }
  
  /**
   * A {@link Logger} for this class.
   */
  public static final Logger log = Logger.getLogger(SBFileFilter.class.getName());
  
  /**
   * The maximal number of characters to check for characteristic identifier in
   * files. If the first {@link #MAX_CHARACTERS_TO_PARSE} do not contain a defined
   * pattern for the given file type, the file cannot be recognized as a valid
   * file of this type.
   */
  private static final int MAX_CHARACTERS_TO_PARSE = 512;
  
  /**
   * 
   * @param file
   * @param extension
   * @return The file extension in lower case with a single dot as its prefix.
   */
  public static boolean checkExtension(File file, String extension) {
    if (!extension.startsWith(".")) {
      extension = "." + extension;
    }
    return file.getName().toLowerCase().endsWith(extension.toLowerCase());
  }
  
  /**
   * This method opens the given file and parses the first
   * {@link #MAX_CHARACTERS_TO_PARSE} characters. If a line pattern for the given expected
   * {@link FileType} is available (see {@link FileType#getLinePattern()}), it
   * then tries to match the String read so far to the pattern. In case it finds the
   * expected pattern, this method will return {@code true}.
   * 
   * @param file
   * @param type
   * @return {@code true} if the given {@link File} matches the line
   *         pattern of the given {@link FileType}
   */
  private static boolean checkFileHead(File file, FileType type) {
    Pattern pattern = type.getLinePattern();
    boolean retVal = pattern == null;
    if (!retVal) {
      boolean oldVerbose = OpenFile.isVerbose();
      OpenFile.setVerbose(false);
      BufferedReader br = OpenFile.openFile(file.getAbsolutePath());
      Matcher matcher;
      try {
        int bytesRead;
        char chunk[] = new char[128];
        StringBuilder line = new StringBuilder();
        for (int i = 0; br.ready() && (i < MAX_CHARACTERS_TO_PARSE) && !retVal; i++) {
          bytesRead = br.read(chunk);
          if (bytesRead > 0) {
            line.append(new String(chunk, 0, bytesRead));
            matcher = pattern.matcher(line.toString());
            retVal = matcher.find();
          }
        }
      } catch (Throwable e) {
        return false;
      } finally {
        try {
          if (br != null) {
            br.close();
          }
        } catch (IOException e) {
          return false;
        }
        OpenFile.setVerbose(oldVerbose);
      }
    }
    return retVal;
  }
  
  /**
   * @return The {@link FileFilter} for all files.
   */
  public final static GeneralFileFilter createAllFileFilter() {
    return new SBAcceptAllFileFilter();
  }
  
  /**
   * @return A filter for association files
   */
  public static SBFileFilter createASSOCFileFilter() {
    return new SBFileFilter(FileType.ASSOC_FILES);
  }
  
  /**
   * @return A filter for bed files
   */
  public static SBFileFilter createBEDFileFilter() {
    return new SBFileFilter(FileType.BED_FILES);
  }
  
  /**
   * @return A filter for bim files
   */
  public static SBFileFilter createBIMFileFilter() {
    return new SBFileFilter(FileType.BIM_FILES);
  }
  
  /**
   * @return A filter for SBML files
   */
  public static final SBFileFilter createBioPAXFileFilter() {
    return new SBFileFilter(FileType.BioPAX_FILES);
  }
  
  /**
   * @return Filter for owl files
   */
  public static SBFileFilter createBioPAXFileFilterL2() {
    return new SBFileFilter(FileType.BioPAX_FILES_L2);
  }
  
  /**
   * @return Filter for owl files
   */
  public static SBFileFilter createBioPAXFileFilterL3() {
    return new SBFileFilter(FileType.BioPAX_FILES_L3);
  }
  
  public static final SBFileFilter createCDTFileFilterList() {
    return new SBFileFilter(FileType.CDT_FILES);
  }
  
  /**
   * @return A filter for CSV files
   */
  public static SBFileFilter createCSVFileFilter() {
    return new SBFileFilter(FileType.CSV_FILES);
  }
  
  /**
   * @return A filter for directories only.
   */
  public static SBFileFilter createDirectoryFilter() {
    return new SBFileFilter(FileType.DIRECTORIES_ONLY);
  }
  
  /**
   * @return A filter for fam files
   */
  public static SBFileFilter createFAMFileFilter() {
    return new SBFileFilter(FileType.FAM_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter createGIFFileFilter() {
    return new SBFileFilter(FileType.GIF_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter createGMLFileFilter() {
    return new SBFileFilter(FileType.GML_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter createGraphMLFileFilter() {
    return new SBFileFilter(FileType.GRAPHML_FILES);
  }
  
  /**
   * @return
   */
  public static final FileFilter createHTMLFileFilter() {
    return new SBFileFilter(FileType.HTML_FILES);
  }
  
  /**
   * @return A filter for hwe files
   */
  public static SBFileFilter createHWEFileFilter() {
    return new SBFileFilter(FileType.HWE_FILES);
  }
  
  /**
   * @return Filter for any kind of image file supported by this class.
   */
  public static final MultipleFileFilter createImageFileFilter() {
    ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
    return new MultipleFileFilter(String.format("%s (*.jpg, *.png, *.gif)",
      bundle.getString("IMAGE_FILES")), SBFileFilter.createJPEGFileFilter(),
      SBFileFilter.createPNGFileFilter(), SBFileFilter.createGIFFileFilter());
  }
  
  /**
   * @return A filter for joint picture expert group files.
   */
  public static SBFileFilter createJPEGFileFilter() {
    return new SBFileFilter(FileType.JPEG_FILES);
  }
  
  /**
   * @return A filter for JavaScript Object Notation (JSON) files.
   */
  public static GeneralFileFilter createJSONFileFilter() {
    return new SBFileFilter(FileType.JSON_FILES);
  }
  
  /**
   * @return A filter for KGML files (KEGG Markup Language).
   */
  public static final SBFileFilter createKGMLFileFilter() {
    return new SBFileFilter(FileType.KGML_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static GeneralFileFilter createLogFileFilter() {
    return new SBFileFilter(FileType.LOG_FILE);
  }
  
  /**
   * @return Filter for map files
   */
  public static SBFileFilter createMAPFileFilter() {
    return new SBFileFilter(FileType.MAP_FILES);
  }
  
  /**
   * @return Filter for owl files
   */
  public static SBFileFilter createOWLFileFilter() {
    return new SBFileFilter(FileType.OWL_FILES);
  }
  
  /**
   * @return A filter for PDF files.
   */
  public static final SBFileFilter createPDFFileFilter() {
    return new SBFileFilter(FileType.PDF_FILES);
  }
  
  /**
   * @return A filter for ped files
   */
  public static SBFileFilter createPEDFileFilter() {
    return new SBFileFilter(FileType.PED_FILES);
  }
  
  /**
   * @return A filter for PLINK genotype files
   */
  public static SBFileFilter createPlinkGenotypeFileFilter() {
    return new SBFileFilter(FileType.PLINK_GENOTYPE_FILES);
  }
  
  /**
   * @return A filter for portable network graphic files.
   */
  public static SBFileFilter createPNGFileFilter() {
    return new SBFileFilter(FileType.PNG_FILES);
  }
  
  /**
   * @return A filter for SBGN files
   */
  public static final SBFileFilter createSBGNFileFilter() {
    return new SBFileFilter(FileType.SBGN_FILES);
  }
  
  /**
   * @return A filter for SBML files
   */
  public static final SBFileFilter createSBMLFileFilter() {
    return new SBFileFilter(FileType.SBML_FILES);
  }
  
  /**
   * @return A filter for SBML files in level 1 version 1
   */
  public static final SBFileFilter createSBMLFileFilterL1V1() {
    return new SBFileFilter(FileType.SBML_FILES_L1V1);
  }
  
  /**
   * @return A filter for SBML files in level 1 version 2
   */
  public static final SBFileFilter createSBMLFileFilterL1V2() {
    return new SBFileFilter(FileType.SBML_FILES_L1V2);
  }
  
  /**
   * @return A filter for SBML files in level 2 version 1
   */
  public static final SBFileFilter createSBMLFileFilterL2V1() {
    return new SBFileFilter(FileType.SBML_FILES_L2V1);
  }
  
  /**
   * @return A filter for SBML files in level 2 version 2
   */
  public static final SBFileFilter createSBMLFileFilterL2V2() {
    return new SBFileFilter(FileType.SBML_FILES_L2V2);
  }
  
  /**
   * @return A filter for SBML files in level 2 version 3
   */
  public static final SBFileFilter createSBMLFileFilterL2V3() {
    return new SBFileFilter(FileType.SBML_FILES_L2V3);
  }
  
  /**
   * @return A filter for SBML files in level 2 version 4
   */
  public static final SBFileFilter createSBMLFileFilterL2V4() {
    return new SBFileFilter(FileType.SBML_FILES_L2V4);
  }
  
  /**
   * @return A filter for SBML files in level 2 version 4
   */
  public static final SBFileFilter createSBMLFileFilterL2V5() {
    return new SBFileFilter(FileType.SBML_FILES_L2V5);
  }
  
  /**
   * @return A filter for SBML files in level 3 version 1
   */
  public static final SBFileFilter createSBMLFileFilterL3V1() {
    return new SBFileFilter(FileType.SBML_FILES_L3V1);
  }
  
  /**
   * @return A filter for SBML files in level 3 version 2
   */
  public static final SBFileFilter createSBMLFileFilterL3V2() {
    return new SBFileFilter(FileType.SBML_FILES_L3V2);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter[] createSBMLFileFilterList() {
    FileType types[] = { FileType.SBML_FILES, FileType.SBML_FILES_L1V1,
        FileType.SBML_FILES_L1V2, FileType.SBML_FILES_L2V1,
        FileType.SBML_FILES_L2V2, FileType.SBML_FILES_L2V3,
        FileType.SBML_FILES_L2V4, FileType.SBML_FILES_L2V5,
        FileType.SBML_FILES_L3V1, FileType.SBML_FILES_L2V2 };
    SBFileFilter filters[] = new SBFileFilter[types.length];
    int i = 0;
    for (FileType type : types) {
      filters[i++] = new SBFileFilter(type);
    }
    return filters;
  }
  
  /**
   * @return A filter for SIF files
   */
  public static final SBFileFilter createSIFFileFilter() {
    return new SBFileFilter(FileType.SIF_FILES);
  }
  
  /**
   * @return A filter for SVG files (Scalable Vector Graphics)
   */
  public static SBFileFilter createSVGFileFilter() {
    return new SBFileFilter(FileType.SVG_FILES);
  }
  
  /**
   * @return A filter for TeX files
   */
  public static final SBFileFilter createTeXFileFilter() {
    return new SBFileFilter(FileType.TeX_FILES);
  }
  
  /**
   * @return A filter for Text files.
   */
  public static final SBFileFilter createTextFileFilter() {
    return new SBFileFilter(FileType.TEXT_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter createTGFFileFilter() {
    return new SBFileFilter(FileType.TGF_FILES);
  }
  
  /**
   * @return A filter for TSV files (tab separated text files)
   */
  public static SBFileFilter createTSVFileFilter() {
    return new SBFileFilter(FileType.TSV_FILES);
  }
  
  /**
   * 
   * @return
   */
  public static final SBFileFilter createYGFFileFilter() {
    return new SBFileFilter(FileType.YGF_FILES);
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static String getExtension(File file) {
    return getExtension(file.getName());
  }
  
  /**
   * @param name
   * @return
   */
  public static String getExtension(String name) {
    return FileTools.getExtension(name);
  }
  
  /**
   * 
   * @param file
   * @param type
   * @return
   */
  public static boolean hasFileType(File file, FileType type) {
    return hasFileType(file, type, false);
  }
  
  /**
   * 
   * @param file
   * @param type
   * @param caseSensitive
   * @return
   */
  public static boolean hasFileType(File file, FileType type, boolean caseSensitive) {
    String extension = getExtension(file);
    if (!caseSensitive) {
      extension = extension.toLowerCase();
    }
    return type.getFileExtensions().contains(extension);
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static boolean isCSVFile(File file) {
    return hasFileType(file, FileType.CSV_FILES);
  }
  
  /**
   * @param file
   * @return
   */
  public static boolean isHTMLFile(File file) {
    return hasFileType(file, FileType.HTML_FILES);
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static boolean isJPEGFile(File file) {
    return hasFileType(file, FileType.JPEG_FILES);
  }
  
  /**
   * 
   * @param file
   * @return
   */
  public static boolean isJSONFile(File file) {
    return hasFileType(file, FileType.JSON_FILES);
  }
  
  /**
   * Checks a) if the file endswith XML and b) if the doctype is KGML.
   * @param file
   * @return true if and only if the file is a KGML formatted file.
   */
  public static boolean isKGML(File file) {
    FileType type = FileType.KGML_FILES;
    if (hasFileType(file, type)) {
      return checkFileHead(file, type);
    }
    return false;
  }
  
  public static boolean isLogFile(File file) {
    return hasFileType(file, FileType.LOG_FILE);
  }
  
  /**
   * Returns true if the given file is an OWL file.
   * 
   * @param file
   * @return
   */
  public static boolean isOWLFile(File file) {
    return hasFileType(file, FileType.OWL_FILES);
  }
  
  /**
   * @param f
   * @return
   */
  public static boolean isPDFFile(File file) {
    return hasFileType(file, FileType.PDF_FILES);
  }
  
  /**
   * Returns true if the given file is a portable network graphics file.
   * 
   * @param file
   * @return
   */
  public static boolean isPNGFile(File file) {
    return hasFileType(file, FileType.PNG_FILES);
  }
  
  /**
   * Returns true if the given file is a SBGN file.
   * 
   * @param file
   * @return
   */
  public static boolean isSBGNFile(File file) {
    return hasFileType(file, FileType.SBGN_FILES);
  }
  
  /**
   * Returns true if the given file is an SBML file.
   * 
   * @param file
   * @return
   */
  public static boolean isSBMLFile(File file) {
    FileType type = FileType.SBML_FILES;
    if (hasFileType(file, type)) {
      return checkFileHead(file, type);
    }
    return false;
  }
  
  /**
   * Returns true if the given file is a TeX file.
   * 
   * @param file
   * @return
   */
  public static boolean isTeXFile(File file) {
    return hasFileType(file, FileType.TeX_FILES);
  }
  
  /**
   * Returns true if the given file is a text file.
   * 
   * @param file
   * @return
   */
  public static boolean isTextFile(File file) {
    return hasFileType(file, FileType.TEXT_FILES);
  }
  
  /**
   * Allows users to initialize this {@link GeneralFileFilter} with another
   * {@link FileFilter}. In this way, the {@link SBFileFilter} can work as
   * an adapter/wrapper.
   */
  private GeneralFileFilter filter;
  
  /**
   * Allowable file type.
   */
  private FileType type;
  
  /**
   * Constructs a file filter that accepts or not accepts the following files
   * (defined by the given parameters).
   * 
   * @param type
   *        One of the short numbers defined in this class.
   */
  public SBFileFilter(FileType type) {
    this.type = type;
    if (type == FileType.UNDEFINED) {
      throw new IllegalArgumentException(
        ResourceManager.getBundle("de.zbit.locales.Warnings").getString(
            "FILE_TYPE_MUST_NOT_BE_UNDEFINED"));
    }
  }
  
  /**
   * A constructor that allows using this {@link SBFileFilter} as a wrapper
   * for another {@link GeneralFileFilter}.
   * 
   * @param filter
   */
  public SBFileFilter(GeneralFileFilter filter) {
    this.filter = filter;
    type = FileType.UNDEFINED;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File f) {
    if (f == null) {
      return false;
    }
    if (filter != null) {
      return filter.accept(f);
    }
    Set<String> extensions = type.getFileExtensions();
    return f.isDirectory()
        || (((extensions == null) || extensions.isEmpty() || extensions.contains(getExtension(f)))
            && (!f.exists() || (checkFileHead(f, type))));
  }
  
  /**
   * Returns true if this file filter accepts SBML files.
   * 
   * @return
   */
  public boolean acceptsBioPAXFiles() {
    return type == FileType.BioPAX_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsCDTFiles() {
    return type == FileType.CDT_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsCSVFiles() {
    return type == FileType.CSV_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsJPEGFiles() {
    return type == FileType.JPEG_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsOWLFiles() {
    return type == FileType.OWL_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsPNGFiles() {
    return type == FileType.PNG_FILES;
  }
  
  /**
   * 
   * @return true if this file filter accepts SBGN files.
   */
  public boolean acceptsSBGNFiles() {
    return type == FileType.SBGN_FILES;
  }
  
  /**
   * Returns true if this file filter accepts SBML files.
   * 
   * @return
   */
  public boolean acceptsSBMLFiles() {
    return type == FileType.SBML_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsSVGFiles() {
    return type == FileType.SVG_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsTeXFiles() {
    return type == FileType.TeX_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsTextFiles() {
    return type == FileType.TEXT_FILES;
  }
  
  /**
   * 
   * @return
   */
  public boolean acceptsTSVFiles() {
    return type == FileType.TSV_FILES;
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.io.GeneralFileFilter#clone()
   */
  @Override
  protected GeneralFileFilter clone() throws CloneNotSupportedException {
    if (isSetFileFilter()) {
      return new SBFileFilter(filter);
    }
    return new SBFileFilter(type);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    // if object is of the same class
    if (obj.getClass().equals(getClass())) {
      SBFileFilter sbff = (SBFileFilter)obj;
      boolean equal = true;
      if( filter == null ) {
        equal &= (sbff.filter == null);
      } else {
        equal &= filter.equals(sbff.filter);
      }
      if( type == null ) {
        equal &= (sbff.type == null);
      } else {
        equal &= type.equals(sbff.type);
      }
      return equal;
    }
    // otherwise false
    return false;
  }
  
  /**
   * Filters the given files for those acceptable by this
   * {@link java.io.FileFilter}.
   * 
   * @param files
   * @return a {@link List} containing only acceptable {@link File} objects.
   *         This {@link List} may be empty.
   */
  public List<File> filter(File... files) {
    return separate(files).getA();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    return getDescription(false);
  }
  
  /**
   * 
   * @param inTheMiddleOfASentence
   *        if {@code true}, will return a {@link String} that can be used
   *        "in the middle of a sentence". Else, a {@link String} that stands at
   *        the start of a sentence or by itself will be returned. E.g., if
   *        {@code true}, "directories only" if false "Directories only".
   * @return
   * @see #getDescription()
   */
  public String getDescription(boolean inTheMiddleOfASentence) {
    if (filter != null) {
      return filter.getDescription();
    }
    ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
    Set<String> extensions = type.getFileExtensions();
    StringBuilder sb = new StringBuilder();
    try {
      sb.append(bundle.getString(type.toString()));
    } catch(MissingResourceException e) {
      log.warning("No label found for this file type " + type.toString());
    }
    if (extensions.iterator().hasNext()) {
      sb.append(" (");
      Iterator<String> iterator = extensions.iterator();
      while (iterator.hasNext()) {
        sb.append("*.");
        sb.append(iterator.next());
        if (iterator.hasNext()) {
          sb.append(", ");
        }
      }
      sb.append(")");
    }
    if (inTheMiddleOfASentence) {
      return StringUtil.changeFirstLetterCase(sb.toString(), false, false);
    }
    return sb.toString();
  }
  
  /**
   * @see {@link #getExtensions()}
   * @return the first file extension of all acceptable extensions.
   */
  public String getExtension() {
    Set<String> extensions = getExtensions();
    if ((extensions != null) && (extensions.size() > 0)) {
      return extensions.iterator().next();
    }
    return null;
  }
  
  /**
   * @return all acceptable file extensions.
   */
  public Set<String> getExtensions() {
    return type.getFileExtensions();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.io.GeneralFileFilter#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 919;
    int hashCode = super.hashCode();
    if (isSetFileFilter()) {
      hashCode += prime * filter.hashCode();
    }
    if (isSetFileType()) {
      hashCode += prime * type.hashCode();
    }
    return hashCode;
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetFileType() {
    return type != null;
  }
  
  /**
   * Sorts the given {@link File} objects into two {@link List}s:
   * <ol>
   *   <li>A {@link List} of accepted {@link File}s</li>
   *   <li>The remaining {@link List} of elements that are not accepted by this {@link java.io.FileFilter}</li>
   * </ol>
   * @param files
   * @return Can be two empty {@link List}s
   */
  public ValuePairUncomparable<List<File>, List<File>> separate(File... files) {
    if ((files == null) || (files.length == 0)) {
      return new ValuePairUncomparable<List<File>, List<File>>(
          new ArrayList<File>(0), new ArrayList<File>(0));
    }
    ValuePairUncomparable<List<File>, List<File>> separation = new ValuePairUncomparable<List<File>, List<File>>(
        new ArrayList<File>(files.length), new ArrayList<File>(files.length));
    for (File f : files) {
      if (accept(f)) {
        separation.getA().add(f);
      } else {
        separation.getB().add(f);
      }
    }
    return separation;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getDescription();
  }
  
}
