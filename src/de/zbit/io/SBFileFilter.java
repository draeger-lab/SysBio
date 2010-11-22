package de.zbit.io;

import java.io.File;

import javax.swing.JFileChooser;
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
	 * @since 1.4
	 * 
	 */
	public static enum FileType {
		/**
		 * To be selected if CSV files (comma separated files) can be chosen.
		 */
		CSV_FILES,
		/**
		 * True if this filter accepts directories only (no files).
		 */
		DIRECTORIES_ONLY,
		/**
		 * True if this filter accepts JPEG picture files.
		 */
		JPEG_FILES,
		/**
		 * A file filter for portable document format files.
		 */
		PDF_FILES,
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
		 * True if this filter accepts GraphML files.
		 */
		GRAPHML_FILES,
		/**
		 * True if this filter accepts GML files.
		 */
		GML_FILES,
		/**
		 * True if this filter accepts GIF files.
		 */
		GIF_FILES,
		/**
		 * True if this filter accepts YGF (Y Graph Format) files.
		 */
		YGF_FILES,
		/**
		 * True if this filter accepts TGF (trivial graph format) files.
		 */
		TGF_FILES,
		
		/**
		 * If not specified this is the type.
		 */
		UNDEFINED;
	}
	
	/*
	 * TODO: Remove the static constructors. They take memory, even if somebody
	 * does not even use this class! Be careful with initilizaing objects static.
	 */
	
	/**
	 * The {@link FileFilter} for all files.
	 */
	public final static GeneralFileFilter ALL_FILE_FILTER = new SBFileFilter(
		(new JFileChooser()).getAcceptAllFileFilter());
	
	/**
	 * A filter for CSV files
	 */
	public static SBFileFilter CSV_FILE_FILTER = new SBFileFilter(
		FileType.CSV_FILES);
	
	/**
	 * A filter for directories only.
	 */
	public static SBFileFilter DIRECTORY_FILTER = new SBFileFilter(
		FileType.DIRECTORIES_ONLY);
		
	/**
	 * A filter for joint picture expert group files.
	 */
	public static SBFileFilter JPEG_FILE_FILTER = new SBFileFilter(
		FileType.JPEG_FILES);
	
	/**
	 * A filter for PDF files.
	 */
	public static final SBFileFilter PDF_FILE_FILTER = new SBFileFilter(
		FileType.PDF_FILES);
	
	/**
	 * A filter for portable network graphic files.
	 */
	public static SBFileFilter PNG_FILE_FILTER = new SBFileFilter(
		FileType.PNG_FILES);
	
	/**
	 * A filter for SBML files
	 */
	public static final SBFileFilter SBML_FILE_FILTER = new SBFileFilter(
		FileType.SBML_FILES);
	
	/**
	 * A filter for TeX files
	 */
	public static final SBFileFilter TeX_FILE_FILTER = new SBFileFilter(
		FileType.TeX_FILES);
	
	/**
	 * A filter for Text files.
	 */
	public static final SBFileFilter TEXT_FILE_FILTER = new SBFileFilter(
	  FileType.TEXT_FILES);
	
	public static final SBFileFilter GRAPHML_FILE_FILTER = new SBFileFilter(
	  FileType.GRAPHML_FILES);
	
	public static final SBFileFilter GML_FILE_FILTER = new SBFileFilter(
	  FileType.GML_FILES);
	public static final SBFileFilter GIF_FILE_FILTER = new SBFileFilter(
	  FileType.GIF_FILES);
	public static final SBFileFilter YGF_FILE_FILTER = new SBFileFilter(
	  FileType.YGF_FILES);
	public static final SBFileFilter TGF_FILE_FILTER = new SBFileFilter(
	  FileType.TGF_FILES);
	
  
	/**
	 * Filter for any kind of image file supported by this class.
	 */
	public static final MultipleFileFilter IMAGE_FILE_FILTER = new MultipleFileFilter(
		"image files (*.jpg, *.png, *.gif)", SBFileFilter.JPEG_FILE_FILTER,
		SBFileFilter.PNG_FILE_FILTER, SBFileFilter.GIF_FILE_FILTER);
	
	/**
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isCSVFile(File f) {
		return f.getName().toLowerCase().endsWith(".csv");
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
			throw new IllegalArgumentException("FileType must not be UNDEFINED.");
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
		if ((f.isDirectory() || (type == FileType.TEXT_FILES && isTextFile(f)))
				|| (type == FileType.TeX_FILES && isTeXFile(f))
				|| (type == FileType.SBML_FILES && isSBMLFile(f))
				|| (type == FileType.CSV_FILES && isCSVFile(f))
				|| (type == FileType.PNG_FILES && isPNGFile(f))
				|| (type == FileType.JPEG_FILES && isJPEGFile(f))
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
		if (filter != null) {
			return filter.getDescription();
		}
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
}
