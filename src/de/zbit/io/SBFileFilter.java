package de.zbit.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import de.zbit.util.StringUtil;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2007-08-03
 * 
 */
public class SBFileFilter extends GeneralFileFilter {
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @author wrzodek
	 * 
	 */
	public static enum FileType {
		/**
		 * A filter for assoc files
		 */
	  ASSOC_FILES,
	  /**
		 * To be selected if CSV files (comma separated files) can be chosen.
		 */
		CSV_FILES,
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
	}
	
	/*
	 * TODO: Remove the static constructors. They take memory, even if somebody
	 * does not even use this class! Be careful with initilizaing objects static.
	 */

	/**
	 * 
	 * @param f
	 * @param extension
	 * @return
	 */
	public static boolean checkExtension(File f, String extension) {
		if (!extension.startsWith(".")) extension = "." + extension;
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
		return new MultipleFileFilter("image files (*.jpg, *.png, *.gif)",
			SBFileFilter.createJPEGFileFilter(), SBFileFilter.createPNGFileFilter(),
			SBFileFilter.createGIFFileFilter());
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
   * Returns true if the given file is a hwe file.
   * 
   * @param f
   * @return
   */
  public static boolean isHWEFile(File f) {
    return f.getName().toLowerCase().endsWith(".hwe");
  }
  
  /**
   * Returns true if the given file is a map file.
   * 
   * @param f
   * @return
   */
  private boolean isMAPFile(File f) {
    return f.getName().toLowerCase().endsWith(".map");
  }
  
  /**
   * Returns true if the given file is a assoc file.
   * 
   * @param f
   * @return
   */
  public static boolean isASSOCFile(File f) {
    return f.getName().toLowerCase().endsWith(".assoc");
  }
  
  /**
   * Returns true if the given file is a ped file.
   * 
   * @param f
   * @return
   */
  public static boolean isPEDFile(File f) {
    return f.getName().toLowerCase().endsWith(".ped");
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
		if (type == FileType.UNDEFINED) { throw new IllegalArgumentException(
			"FileType must not be UNDEFINED."); }
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	// @Override
	public boolean accept(File f) {
		if (filter != null) { return filter.accept(f); }
		if ((f.isDirectory() || (type == FileType.TEXT_FILES && isTextFile(f)))
				|| (type == FileType.TeX_FILES && isTeXFile(f))
				|| (type == FileType.SBML_FILES && isSBMLFile(f))
				|| (type == FileType.CSV_FILES && isCSVFile(f))
				|| (type == FileType.HTML_FILES && isHTMLFile(f))
				|| (type == FileType.PNG_FILES && isPNGFile(f))
				|| (type == FileType.JPEG_FILES && isJPEGFile(f))
				|| (type == FileType.PED_FILES && isPEDFile(f))
				|| (type == FileType.MAP_FILES && isMAPFile(f))
				|| (type == FileType.HWE_FILES && isHWEFile(f))
				|| (type == FileType.ASSOC_FILES && isASSOCFile(f))				
				|| (type == FileType.GRAPHML_FILES && checkExtension(f, ".graphml"))
				|| (type == FileType.GML_FILES && checkExtension(f, ".gml"))
				|| (type == FileType.GIF_FILES && checkExtension(f, ".gif"))
				|| (type == FileType.YGF_FILES && checkExtension(f, ".ygf"))
				|| (type == FileType.TGF_FILES && checkExtension(f, ".tgf"))
				|| (type == FileType.PDF_FILES && isPDFFile(f))) return true;
		return false;
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
		if (filter != null) { return filter.getDescription(); }
		switch (type) {
			case TEXT_FILES:
				return "Text files (*.txt)";
			case TeX_FILES:
				return "TeX files (*.tex)";
			case SBML_FILES:
				return "SBML files (*.sbml, *.xml)";
			case CSV_FILES:
				return "Comma separated files (*.csv)";
			case JPEG_FILES:
				return "Joint Photographic Experts Group files (*.jpg, *.jpeg)";
			case PNG_FILES:
				return "Portable Network Graphics files (*.png)";
			case PDF_FILES:
				return "Portable Document Format files (*.pdf)";
			case HTML_FILES:
				return "Hypertext markup language files (*.html, *.htm)";
			case GRAPHML_FILES:
				return "GraphML files (*.GraphML)";
			case GML_FILES:
				return "Graph Modeling Language files (*.gml)";
			case GIF_FILES:
				return "Graphics Interchange Format files (*.gif)";
			case YGF_FILES:
				return "Y Graph Format files (*.ygf)";
			case TGF_FILES:
				return "Trivial graph format files (*.tgf)";
				
			case DIRECTORIES_ONLY:
			default:
				return StringUtil.firstLetterUpperCase(type.toString()
						.replace('_', ' '));
		}
	}
	
	/**
	 * Returns the file extension.
	 */
	public String getExtension() {
		if (type == FileType.JPEG_FILES) {
			return "jpg";
		} else if (type == FileType.SBML_FILES) {
			return "sbml.xml";
		} else if (type == FileType.TEXT_FILES) {
			return "txt";
		} else if (type.toString().contains("_")) {
			return type.toString().substring(0, type.toString().indexOf("_"))
					.toLowerCase();
		} else {
			return "";
		}
	}
}
