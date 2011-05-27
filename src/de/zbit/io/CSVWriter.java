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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-09-02
 * @version $Rev$
 * @since 1.0
 */
public class CSVWriter {

	/**
	 * Helper data structure.
	 * 
	 * @author Andreas Dr&auml;eger
	 * @date 2010-09-02
	 */
	private class Array2DTable implements TableModel {

		/**
		 * Pointer to the data object
		 */
		private Object[][] data;
		/**
		 * Pointer to the table head
		 */
		private Object[] head;

		/**
		 * 
		 * @param data
		 */
		public Array2DTable(Object[][] data) {
			this(data, null);
		}

		/**
		 * 
		 * @param data
		 * @param head
		 */
		public Array2DTable(Object[][] data, Object[] head) {
			this.head = head;
			if (data != null) {
				this.data = data;
			} else {
				throw new IllegalArgumentException(
						"does not accept null values for table content");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableModel#addTableModelListener(javax.swing.event
		 * .TableModelListener)
		 */
		public void addTableModelListener(TableModelListener l) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnClass(int)
		 */
		public Class<String> getColumnClass(int columnIndex) {
			return String.class;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return head != null ? head.length : data[0].length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		public String getColumnName(int columnIndex) {
		  if (head==null) return "";
			return head[columnIndex] != null ? head[columnIndex].toString()
					: "";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return data.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public String getValueAt(int rowIndex, int columnIndex) {
			try {
				return data[rowIndex][columnIndex].toString();
			} catch (Throwable t) {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableModel#removeTableModelListener(javax.swing
		 * .event.TableModelListener)
		 */
		public void removeTableModelListener(TableModelListener l) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int,
		 * int)
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			data[rowIndex][columnIndex] = aValue;
		}
	}

	/**
	 * The default or current symbol to be used as a column separator
	 */
	private char separator;

	/**
	 * The default or current symbol to be used as an indicator for comments
	 */
	private char commentSymbol;

	/**
	 * Creates a new writer for tab-separated files with a comma as column
	 * separator and a sharp symbol to indicate comments.
	 */
	public CSVWriter() {
		separator = '\t';
		commentSymbol = '#';
	}

	/**
	 * 
	 * @param separator
	 */
	public CSVWriter(char separator) {
		this();
		setSeparator(separator);
	}

	/**
	 * 
	 * @param separator
	 * @param commentSymbol
	 */
	public CSVWriter(char separator, char commentSymbol) {
		this(separator);
		setCommentSymbol(commentSymbol);
	}

	/**
	 * Initializes this writer using the separator character from the given
	 * reader.
	 * 
	 * @param reader
	 * @throws IOException
	 */
	public CSVWriter(CSVReader reader) throws IOException {
		this();
		// Set separator char
		if (reader.getSeparatorChar() == '\u0000') {
			// the \u0000 symbol is the undefined character.
			reader.open();
			reader.close();
		}
		if (reader.getSeparatorChar() == '\u0000') {
			throw new IOException("Invalid input file.");
		}
		if (reader.getSeparatorChar() == '\u0001') {
			setSeparator(' ');
		} else {
			setSeparator(reader.getSeparatorChar());
		}
	}
	
	/**
	 * @return the commentSymbol
	 */
	public char getCommentSymbol() {
		return commentSymbol;
	}

	/**
	 * Converts a String to a File object and checks if this
	 * File exists and can be overwritten.
	 * @param pathname
	 * @return
	 * @throws IOException
	 */
	public static File getOrCreateFile(String pathname) throws IOException {
		File file = new File(pathname);
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new FileNotFoundException(String.format(
						"cannot write data into directory %s", pathname));
			}
			if (!file.canWrite()) {
				throw new IOException(String.format("cannot overwrite file %s",
						pathname));
			}
		} else {
			file.createNewFile();
		}
		return file;
	}

	/**
	 * @return the separator
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @param commentSymbol
	 *            the commentSymbol to set
	 */
	public void setCommentSymbol(char commentSymbol) {
		this.commentSymbol = commentSymbol;
	}

	/**
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * 
	 * @param data
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, char separator, File file)
			throws IOException {
		write(new Array2DTable(data), separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, char separator, String pathname)
			throws IOException {
		write(data, separator, getOrCreateFile(pathname));
	}

	/**
	 * 
	 * @param data
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, File file) throws IOException {
		write(data, separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, char separator, File file)
			throws IOException {
		write(new Array2DTable(data, head), separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, char separator,
			String pathname) throws IOException {
		write(data, head, separator, getOrCreateFile(pathname));
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, File file)
			throws IOException {
		write(data, head, separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param comments
	 * @param commentSymbol
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, Object comments,
			char commentSymbol, char separator, File file) throws IOException {
		write(new Array2DTable(data, head), comments, commentSymbol, separator,
				file);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param comments
	 * @param commentSymbol
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, Object comments,
			char commentSymbol, char separator, String pathname)
			throws IOException {
		write(data, head, comments, commentSymbol, separator,
				getOrCreateFile(pathname));
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param comments
	 * @param file
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, Object comments, File file)
			throws IOException {
		write(data, head, comments, commentSymbol, separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param comments
	 * @param pathname
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, Object comments,
			String pathname) throws IOException {
		write(data, head, comments, commentSymbol, separator, pathname);
	}

	/**
	 * 
	 * @param data
	 * @param head
	 * @param pathname
	 * @throws IOException
	 */
	public void write(Object[][] data, Object[] head, String pathname)
			throws IOException {
		write(data, head, separator, pathname);
	}

	/**
	 * 
	 * @param data
	 * @param pathname
	 * @throws IOException
	 */
	public void write(Object[][] data, String pathname) throws IOException {
		write(data, separator, pathname);
	}

	/**
	 * 
	 * @param comments
	 * @param commentSymbol
	 * @param writer
	 * @throws IOException
	 */
	private Writer write(String comments, char commentSymbol,
			Writer writer) throws IOException {
		// setCommentSymbol(commentSymbol);
		StringTokenizer st = new StringTokenizer(comments.replace("\r", ""),
				"\n");
		while (st.hasMoreElements()) {
			writer.append(commentSymbol);
			writer.append(' ');
			writer.write(st.nextElement().toString());
      writer.write(StringUtil.newLine());
		}
		writer.flush();
		return writer;
	}

	/**
	 * 
	 * @param data
	 * @param separator
	 * @param writer
	 * @throws IOException
	 */
	private void write(TableModel data, char separator, Writer writer)
			throws IOException {
	  String lineSep = StringUtil.newLine();
	  
		// setSeparator(separator);
		int i, j;
		Object value;

		boolean allHeadEntriesNullOrEmpty = true;
		String name;
		for (i = 0; i < data.getColumnCount() && allHeadEntriesNullOrEmpty; i++) {
			name = data.getColumnName(i);
			if ((name != null) && (name.length() > 0)) {
				allHeadEntriesNullOrEmpty = false;
			}
		}

		if (!allHeadEntriesNullOrEmpty) {
			i = 0;
			// write table head
			while (i < data.getColumnCount()) {
				value = data.getColumnName(i++);
				writer.append(value != null ? value.toString() : "");
				if (i < data.getColumnCount()) {
					writer.append(separator);
				}
			}
			writer.write(lineSep);
		}

		// write table body
		for (i = 0; i < data.getRowCount(); i++) {
			for (j = 0; j < data.getColumnCount(); j++) {
				value = data.getValueAt(i, j);
				writer.append(value != null ? value.toString() : "");
				if (j < data.getColumnCount() - 1) {
					writer.append(separator);
				}
			}
			writer.write(lineSep);
		}
		writer.close();
	}

	/**
	 * 
	 * @param data
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(TableModel data, char separator, File file)
			throws IOException {
		write(data, separator, initializeWriter(file));
	}

	/**
	 * Create a writer from a file.
	 * @param file
	 * @return
	 * @throws IOException
	 */
  private static Writer initializeWriter(File file) throws IOException {
    return new BufferedWriter(new FileWriter(file));
  }

	/**
	 * 
	 * @param data
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(TableModel data, char separator, String pathname)
			throws IOException {
		write(data, separator, getOrCreateFile(pathname));
	}

	/**
	 * 
	 * @param data
	 * @param file
	 * @throws IOException
	 */
	public void write(TableModel data, File file) throws IOException {
		write(data, separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param comments
	 * @param commentSymbol
	 * @param separator
	 * @param file
	 * @throws IOException
	 */
	public void write(TableModel data, Object comments, char commentSymbol,
			char separator, File file) throws IOException {
		write(data, separator, write(comments.toString(), commentSymbol,
				initializeWriter(file)));
	}

	/**
	 * 
	 * @param data
	 * @param comments
	 * @param commentSymbol
	 * @param separator
	 * @param pathname
	 * @throws IOException
	 */
	public void write(TableModel data, Object comments, char commentSymbol,
			char separator, String pathname) throws IOException {
		write(data, comments, commentSymbol, separator,
				getOrCreateFile(pathname));
	}

	/**
	 * 
	 * @param data
	 * @param comments
	 * @param file
	 * @throws IOException
	 */
	public void write(TableModel data, Object comments, File file)
			throws IOException {
		write(data, comments, commentSymbol, separator, file);
	}

	/**
	 * 
	 * @param data
	 * @param comments
	 * @param pathname
	 * @throws IOException
	 */
	public void write(TableModel data, Object comments, String pathname)
			throws IOException {
		write(data, comments, commentSymbol, separator, pathname);
	}

	/**
	 * 
	 * @param data
	 * @param pathname
	 * @throws IOException
	 */
	public void write(TableModel data, String pathname) throws IOException {
		write(data, separator, pathname);
	}
}
