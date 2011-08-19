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

import java.io.File;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

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
     * A filter for fam files
     */
    FAM_FILES,
    /**
     * To be selected if TSV files (tab separated text files) can be chosen.
     */
    TSV_FILES,
    /**
     * To be selected if SVG files (Scalable Vector Graphics) can be chosen.
     */
    SVG_FILES,
		/**
		 * True if this filter accepts directories only (no files).
		 */
		DIRECTORIES_ONLY,
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
	    Set<String> extensions = new HashSet<String>();
	    // TODO: Complete
	    String string = toString();
      switch (this) {
        case HTML_FILES:
          extensions.add("htm");
          break;
        case JPEG_FILES:
          extensions.add("jpg");
          break;
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
    
	}
	


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
	 * The {@link FileFilter} for all files.
	 */
	public final static GeneralFileFilter createAllFileFilter() {
		return new SBAcceptAllFileFilter();
	}
	
	/**
	 * A filter for CSV files
	 */
	public static SBFileFilter createCSVFileFilter() {
		return new SBFileFilter(FileType.CSV_FILES);
	}
	
	/**
	 * A filter for TSV files (tab separated text files)
	 */
	public static SBFileFilter createTSVFileFilter() {
	  return new SBFileFilter(FileType.TSV_FILES);
	}
	
	/**
	 * A filter for SVG files (Scalable Vector Graphics)
	 */
	public static SBFileFilter createSVGFileFilter() {
	  return new SBFileFilter(FileType.SVG_FILES);
	}
	
	/**
   * A filter for association files
   */
  public static SBFileFilter createASSOCFileFilter() {
    return new SBFileFilter(FileType.ASSOC_FILES);
  }
  
  /**
   * A filter for ped files
   */
  public static SBFileFilter createPEDFileFilter() {
    return new SBFileFilter(FileType.PED_FILES);
  }
  
  /**
   * A filter for bed files
   */
  public static SBFileFilter createBEDFileFilter() {
    return new SBFileFilter(FileType.BED_FILES);
  }
  
  /**
   * A filter for bim files
   */
  public static SBFileFilter createBIMFileFilter() {
    return new SBFileFilter(FileType.BIM_FILES);
  }
  
  /**
   * A filter for fam files
   */
  public static SBFileFilter createFAMFileFilter() {
    return new SBFileFilter(FileType.FAM_FILES);
  }
  
  /**
  * Filter for map files
  */
 public static SBFileFilter createMAPFileFilter() {
   return new SBFileFilter(FileType.MAP_FILES);
 }
  
  /**
   * A filter for hwe files
   */
  public static SBFileFilter createHWEFileFilter() {
    return new SBFileFilter(FileType.HWE_FILES);
  }
	
	/**
	 * A filter for directories only.
	 */
	public static SBFileFilter createDirectoryFilter() {
		return new SBFileFilter(FileType.DIRECTORIES_ONLY);
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
	 * 
	 */
	public static final FileFilter createHTMLFileFilter() {
		return new SBFileFilter(FileType.HTML_FILES);
	}
	
	/**
	 * Filter for any kind of image file supported by this class.
	 */
	public static final MultipleFileFilter createImageFileFilter() {
		ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
		return new MultipleFileFilter(String.format("%s (*.jpg, *.png, *.gif)",
			bundle.getString("IMAGE_FILES")), SBFileFilter.createJPEGFileFilter(),
			SBFileFilter.createPNGFileFilter(), SBFileFilter.createGIFFileFilter());
	}
	
	/**
	 * A filter for joint picture expert group files.
	 */
	public static SBFileFilter createJPEGFileFilter() {
		return new SBFileFilter(FileType.JPEG_FILES);
	}
	
	/**
	 * A filter for PDF files.
	 */
	public static final SBFileFilter createPDFFileFilter() {
		return new SBFileFilter(FileType.PDF_FILES);
	}
	
	/**
	 * A filter for portable network graphic files.
	 */
	public static SBFileFilter createPNGFileFilter() {
		return new SBFileFilter(FileType.PNG_FILES);
	}
	
	/**
	 * A filter for SBML files
	 */
	public static final SBFileFilter createSBMLFileFilter() {
		return new SBFileFilter(FileType.SBML_FILES);
	}
	
	/**
	 * A filter for TeX files
	 */
	public static final SBFileFilter createTeXFileFilter() {
		return new SBFileFilter(FileType.TeX_FILES);
	}
	
	/**
	 * A filter for Text files.
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
		String extension = f.getName().toLowerCase();
		return extension.endsWith(".xml") || extension.endsWith(".sbml");
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
	// @Override
  public boolean accept(File f) {
    if (filter != null) {
      return filter.accept(f);
    }
    Set<String> extensions = type.getFileExtensions();
    return (f.isDirectory() || extensions.isEmpty() || extensions
        .contains(getExtension(f)));
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
	 * @return
	 */
	public boolean acceptsCSVFiles() {
		return type == FileType.CSV_FILES;
	}
	
  /**
   * 
   * @return
   */
  public boolean acceptsTSVFiles() {
    return type == FileType.TSV_FILES;
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
	 * @param inTheMiddleOfASentece if true, will return a string that
	 * can be used "in the middle of a sentece". Else, a string that
	 * stands at the start of a sentence or by itself will be returned.
	 * E.g., if true, "directories only" if false "Directories only".
	 * @return
	 */
	public String getDescription(boolean inTheMiddleOfASentece) {
		if (filter != null) { 
			return filter.getDescription(); 
		}
		ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
		switch (type) {
			case TEXT_FILES:
				String s = String.format("%s (*.txt)", bundle.getString("TEXT_FILES"));
				if (inTheMiddleOfASentece) {
		      s = StringUtil.changeFirstLetterCase(s, false, false);
				}
				return s;
			case TeX_FILES:
				return String.format("%s (*.tex)", bundle.getString("TeX_FILES"));
			case SBML_FILES:
				return String.format("%s (*.sbml, *.xml)", bundle.getString("SBML_FILES"));
			case CSV_FILES:
				return String.format("%s (*.csv)", bundle.getString("CSV_FILES"));
      case TSV_FILES:
        return String.format("%s (*.txt)", bundle.getString("TSV_FILES"));
      case SVG_FILES:
        return String.format("%s (*.svg)", bundle.getString("SVG_FILES"));
			case JPEG_FILES:
				return String.format("%s (*.jpg, *.jpeg)", bundle.getString("JPEG_FILES"));
			case PNG_FILES:
				return String.format("%s (*.png)", bundle.getString("PNG_FILES"));
			case PDF_FILES:
				return String.format("%s (*.pdf)", bundle.getString("PDF_FILES"));
			case HTML_FILES:
				return String.format("%s (*.html, *.htm)", bundle.getString("HTML_FILES"));
			case GRAPHML_FILES:
				return String.format("%s (*.GraphML)", bundle.getString("GRAPHML_FILES"));
			case GML_FILES:
				return String.format("%s (*.gml)", bundle.getString("GML_FILES"));
			case GIF_FILES:
				return String.format("%s (*.gif)", bundle.getString("GIF_FILES"));
			case YGF_FILES:
				return String.format("%s (*.ygf)", bundle.getString("YGF_FILES"));
			case TGF_FILES:
				return String.format("%s (*.tgf)", bundle.getString("TGF_FILES"));
			case DIRECTORIES_ONLY:
				String s2 = bundle.getString("DIRECTORIES_ONLY");
        if (inTheMiddleOfASentece) {
          s2 = StringUtil.changeFirstLetterCase(s2, false, false);
        }
        return s2;
			default:
				return StringUtil.firstLetterUpperCase(type.toString()
						.replace('_', ' '));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	  return getDescription();
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
	

}
