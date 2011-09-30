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
package de.zbit.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

import de.zbit.util.FileTools;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
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
		 * KEGG Markup Language files.
		 */
		KGML_FILES,
		/**
		 * A file filter for map files
		 */
		MAP_FILES,
		/**
		 * A file filter for portable document format files.
		 */
		PDF_FILES,
		/**
		 * File filter for PED_FILES
		 */
		PED_FILES,
		/**
		 * True if this filter accepts portable network graphic files.
		 */
		PNG_FILES,
		/**
		 * To be selected if SBML files (XML files) can be chosen.
		 */
		SBML_FILES, 
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
        case HTML_FILES:
          extensions.add("htm");
          break;
        case JPEG_FILES:
          extensions.add("jpg");
          break;
        case KGML_FILES:
          extensions.add("xml");
          return extensions;
        case SBML_FILES:
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
    public String getLinePattern() {
      String linePattern = null;
      switch (this) {
        case KGML_FILES:
          return "<!DOCTYPE[\\p{ASCII}]*KGML[\\p{ASCII}]*>";
        case SBML_FILES:
          return "<sbml[\\p{ASCII}]*level=\"[1-3]\"[\\p{ASCII}]*version=\"[1-4]\"[\\p{ASCII}]*>";
        default:
          break;
      }
      return linePattern;
    } 
	}

	/**
   * The maximal number of lines to check for characteristic identifier in
   * files. If the first {@link #MAX_LINES_TO_PARSE} do not contain a defined
   * pattern for the given file type, the file cannot be recognized as a valid
   * file of this type.
   */
  private static final int MAX_LINES_TO_PARSE = 20;
	
	/**
	 * 
	 * @param f
	 * @param extension
	 * @return
	 */
	public static boolean checkExtension(File f, String extension) {
		if (!extension.startsWith(".")) {
			extension = "." + extension;
		}
		return f.getName().toLowerCase().endsWith(extension.toLowerCase());
	}
	
	/**
	 * 
	 * @param file
	 * @param type
	 * @return
	 */
  private static boolean checkFileHead(File file, FileType type) {
    String linePattern = type.getLinePattern();
    boolean retVal = linePattern == null;
    if (!retVal) {
      BufferedReader br = OpenFile.openFile(file.getAbsolutePath());
      try {
        String line;
        for (int i = 0; br.ready() && (i < MAX_LINES_TO_PARSE) && !retVal; i++) {
          line = br.readLine();
          retVal = Pattern.matches(linePattern, line);
        }
      } catch (Throwable e) {
        return false;
      } finally {
        try {
          br.close();
        } catch (IOException e) {
          return false;
        }
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
	 * @return A filter for KGML files (KEGG Markup Language).
	 */
	public static final SBFileFilter createKGMLFileFilter() {
	  return new SBFileFilter(FileType.KGML_FILES);
	}
	
	/**
  * @return Filter for map files
  */
 public static SBFileFilter createMAPFileFilter() {
   return new SBFileFilter(FileType.MAP_FILES);
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
	 * @return A filter for portable network graphic files.
	 */
	public static SBFileFilter createPNGFileFilter() {
		return new SBFileFilter(FileType.PNG_FILES);
	}
	
	/**
	 * @return A filter for SBML files
	 */
	public static final SBFileFilter createSBMLFileFilter() {
		return new SBFileFilter(FileType.SBML_FILES);
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
   * @param f
   * @return
   */
  public static String getExtension(File f) {
    return getExtension(f.getName());
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
	 * @param f
	 * @return
	 */
	public static boolean isCSVFile(File f) {
		return f.getName().toLowerCase().endsWith(".csv");
	}
  
  /**
	 * @param file
	 * @return
	 */
	public static boolean isHTMLFile(File file) {
		String name = file.getName().toLowerCase();
		return name.endsWith(".html") || name.endsWith(".htm");
	}
  
  /**
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isJPEGFile(File f) {
		String extension = f.getName().toLowerCase();
		return extension.endsWith(".jpg") || extension.endsWith(".jpeg");
	}
	
	/**
   * Checks a) if the file endswith XML and b) if the doctype is KGML.
   * @param f
   * @return true if and only if the file is a KGML formatted file.
   */
  public static boolean isKGML(File f) {
    FileType type = FileType.KGML_FILES;
    if (type.getFileExtensions().contains(getExtension(f))) {
      return checkFileHead(f, type);
    }
    return false;
  }
	
	/**
	 * @param f
	 * @return
	 */
	public static boolean isPDFFile(File file) {
		return file.getName().toLowerCase().endsWith(".pdf");
	}

  /**
	 * Returns true if the given file is a portable network graphics file.
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isPNGFile(File f) {
		return f.getName().toLowerCase().endsWith(".png");
	}
	
	/**
	 * Returns true if the given file is an SBML file.
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isSBMLFile(File f) {
	  FileType type = FileType.SBML_FILES; 
	  if (type.getFileExtensions().contains(getExtension(f))) {
	    return checkFileHead(f, type);
	  }
	  return false;
	}
	
	/**
	 * Returns true if the given file is a TeX file.
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isTeXFile(File f) {
		return f.getName().toLowerCase().endsWith(".tex");
	}
	
	/**
	 * Returns true if the given file is a text file.
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isTextFile(File f) {
		return f.getName().toLowerCase().endsWith(".txt");
	}
	
	/**
	 * Allows users to initialize this {@link GeneralFileFilter} with another
	 * {@link FileFilter}.
	 */
	private FileFilter filter;
	
  /**
	 * Allowable file type.
	 */
	private FileType type;
	
	/**
	 * 
	 * @param filter
	 */
	public SBFileFilter(FileFilter filter) {
		this.filter = filter;
		this.type = FileType.UNDEFINED;
	}
  
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

  /*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
  public boolean accept(File f) {
    if (filter != null) {
      return filter.accept(f);
    }
    Set<String> extensions = type.getFileExtensions();
    return (f.isDirectory() || ((extensions.isEmpty() || extensions
        .contains(getExtension(f))) && checkFileHead(f, type)));
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
	public boolean acceptsPNGFiles() {
		return type == FileType.PNG_FILES;
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
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
	  return getDescription(false);
	}
  
  /**
   * @see #getDescription()
   * @param inTheMiddleOfASentece
   *        if true, will return a string that can be used
   *        "in the middle of a sentece". Else, a string that stands at the
   *        start of a sentence or by itself will be returned. E.g., if true,
   *        "directories only" if false "Directories only".
   * @return
   */
  public String getDescription(boolean inTheMiddleOfASentece) {
    if (filter != null) {
      return filter.getDescription();
    }
    ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
    Set<String> extensions = type.getFileExtensions();
    StringBuilder sb = new StringBuilder();
    sb.append(bundle.getString(type.toString()));
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
    if (inTheMiddleOfASentece) {
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
    if (extensions!=null && extensions.size()>0) {
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	  return getDescription();
	}	

}
