package de.zbit.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2007-08-03
 * 
 */
public class SBFileFilter extends FileFilter implements java.io.FileFilter {

	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.4
	 * 
	 */
	public static enum FileType {
		/**
		 * To be selected if CSV files (comma separated files) can be chosen.
		 */
		CSV_FILES,
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
		TEXT_FILES
	}

	/**
	 * A filter for CSV files
	 */
	public static SBFileFilter CSV_FILE_FILTER = new SBFileFilter(
			FileType.CSV_FILES);

	/**
	 * Filter for any kind of image file supported by this class.
	 */
	public static final MultipleFileFilter IMAGE_FILE_FILTER = new MultipleFileFilter(
			"image file (*.jpg, *.png)", SBFileFilter.JPEG_FILE_FILTER,
			SBFileFilter.PNG_FILE_FILTER);

	/**
	 * A filter for joint picture expert group files.
	 */
	public static FileFilter JPEG_FILE_FILTER = new SBFileFilter(
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
	 * Allowable file type.
	 */
	private FileType type;

	/**
	 * Constructs a file filter that accepts or not accepts the following files
	 * (defined by the given parameters).
	 * 
	 * @param type
	 *            One of the short numbers defined in this class.
	 */
	public SBFileFilter(FileType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	// @Override
	public boolean accept(File f) {
		if ((f.isDirectory() || (type == FileType.TEXT_FILES && isTextFile(f)))
				|| (type == FileType.TeX_FILES && isTeXFile(f))
				|| (type == FileType.SBML_FILES && isSBMLFile(f))
				|| (type == FileType.CSV_FILES && isCSVFile(f))
				|| (type == FileType.PNG_FILES && isPNGFile(f))
				|| (type == FileType.JPEG_FILES && isJPEGFile(f))
				|| (type == FileType.PDF_FILES && isPDFFile(f)))
			return true;
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
		default:
			return "";
		}
	}

	/**
	 * @param f
	 * @return
	 */
	public boolean isPDFFile(File file) {
		return file.getName().toLowerCase().endsWith(".pdf");
	}
}
