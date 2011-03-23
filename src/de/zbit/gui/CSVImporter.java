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
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.zbit.io.CSVReader;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * This {@link CSVImporter} tries to map the head entries in a given CSV file to
 * expected column head values. If this cannot be done automatically, a dialog
 * will be displayed to the user asking for doing or at least checking the
 * guessed column indices manually. To extract the data from the CSV file, the
 * {@link CSVReader} used to generate this mapping can be accessed that grants
 * access to all underlying data (as a two-dimensional array of {@link String}
 * s). Furthermore, this {@link CSVImporter} provides several get-methods to
 * access the selected or determined column index. For instance, the method
 * {@link #getColumnIndex(String)} directly delivers the new column index of the
 * expected table head entry or minus one if no such element exists.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-09-03
 * @version $Rev$
 * @since 1.0
 */
public class CSVImporter {

	/**
	 * For testing only
	 * 
	 * @param args
	 *            a list of {@link String}s for the expected head and the path
	 *            to a file to be parsed.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			String expectedHead[] = new String[args.length - 1];
			System.arraycopy(args, 0, expectedHead, 0, args.length - 1);
			CSVImporter converter = new CSVImporter(args[args.length - 1],
					expectedHead);
			System.out
					.println(Arrays.toString(converter.getColumnAssignment()));
			System.out.println(converter.getNewHead());
		}
	}

	/**
	 * Switch of whether or not to hide those column names that can be
	 * automatically matched in the user interface to be displayed. Defaults to
	 * true.
	 */
	private boolean hideExactColumnNames;

	/**
	 * An array of indices pointing to the position of each column in the new
	 * head, i.e., after the user's assignment.
	 */
	private int assignment[];

	/**
	 * Stores the fields expected to occur in the CSV file. This will be sorted
	 * alphabetically.
	 */
	private String newHead[];

	/**
	 * An alphabetically sorted array of {@link String}s with the given expected
	 * entries in the table head given by the CSV file.
	 */
	private String sortedExpectedHead[];

	/**
	 * Memorizes the {@link CSVReader} object to grant access to this later on.
	 */
	private CSVReader reader;

	/**
	 * Used to directly access the index of a desired column.
	 */
	private Hashtable<String, Integer> expectedNameToColIndex;

	/**
	 * Whether or not the user clicked on cancel while reading the file.
	 */
	private boolean cancelOption;

	/**
	 * 
	 * @param parent
	 * @param hideExactColumnNames
	 * @param pathname
	 * @param expectedHead
	 * @throws IOException
	 */
	public CSVImporter(Component parent, boolean hideExactColumnNames,
			String pathname, String... expectedHead) throws IOException {
		this(parent, hideExactColumnNames, pathname, false, expectedHead);
	}
	
	/**
	 * 
	 * @param parent
	 * @param hideExactColumnNames
	 * @param pathname
	 * @param acceptWithoutEdit
	 * @param expectedHead
	 * @throws IOException
	 */
	public CSVImporter(Component parent, boolean hideExactColumnNames,
			String pathname, boolean acceptWithoutEdit, boolean caseSensitive, String... expectedHead) throws IOException {
		/*
		 * Initialize (field) variables.
		 */
		this.hideExactColumnNames = hideExactColumnNames;
		sortedExpectedHead = expectedHead.clone();
		Arrays.sort(sortedExpectedHead);
		assignment = new int[sortedExpectedHead.length];
		Arrays.fill(assignment, -1);
		reader = new CSVReader(pathname);
		int i, numProblems = 0;
		CSVReaderColumnChooser c = new CSVReaderColumnChooser(reader);

		/*
		 * Read data field and try to automatically map columns from the data
		 * file to expected column header entries.
		 */
		if (acceptWithoutEdit || (reader == CSVReaderOptionPanel.showDialog(parent, reader,
				"Data import"))) {
			c.setSortHeaders(true);
			// if (reader.getContainsHeaders()) {
			for (i = 0; i < sortedExpectedHead.length; i++) {
				int col=-1;
				if(caseSensitive) {
					col=reader.getColumnSensitive(sortedExpectedHead[i]);
				}
				if(col<0) {
					col=reader.getColumn(sortedExpectedHead[i]);
				}
				if (col >= 0) {
					assignment[i] = col;
					c.setHeaderVisible(col, !hideExactColumnNames);
				} else {
					if(caseSensitive) {
						col = reader.getColumnContainingSensitive(sortedExpectedHead[i]);
					}
					if(col<0) {
						col=reader.getColumnContaining(sortedExpectedHead[i]);
					}
					if(acceptWithoutEdit) {
						assignment[i]=col;
					}
					c.addColumnChooser(sortedExpectedHead[i], col, false, true);
					numProblems++;
				}
			}
			// }
		} else {
			// numProblems = reader.getNumberOfColumns();
			cancelOption = true;
		}

		/*
		 * Let the user manually assign columns where it cannot be done
		 * automatically.
		 */
		if (numProblems > 0) {
			JPanel panel = null;
			if (!acceptWithoutEdit) {
				panel = new JPanel(new BorderLayout());
				panel.add(
						new JLabel(StringUtil.toHTML(String.format(
								ResourceManager.getBundle(
										"de.zbit.locales.Labels").getString(
										"COULD_NOT_IDENTIFY_COLUMNS"),
								numProblems), 60)), BorderLayout.NORTH);
				if ((c.getPreferredSize().getWidth() > 450)
						|| (c.getPreferredSize().getHeight() > 450)) {
					JScrollPane scroll = new JScrollPane(c,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					// scroll.setPreferredSize(new Dimension(250, Math.min(
					// 35 * numProblems + 20, 400)));
					scroll.setPreferredSize(new Dimension((int) Math.round(Math
							.min(470, panel.getPreferredSize().getWidth())),
							(int) Math.round(Math.min(c.getPreferredSize()
									.getHeight(), 470))));
					panel.add(scroll);
				} else {
					panel.add(c, BorderLayout.CENTER);
				}
			}
			if(acceptWithoutEdit||(c.getColumnChoosers().size() < 1)
					|| (JOptionPane.showConfirmDialog(parent, panel,
							ResourceManager.getBundle("de.zbit.locales.Labels")
									.getString("COLUMN_ASSIGNMENT"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {
				List<String> newHead = new LinkedList<String>();
				for (i = 0; i < sortedExpectedHead.length; i++) {
					if(assignment[i]<0) {
						assignment[i] = c
						.getSelectedValue(sortedExpectedHead[i]);
					}
					if(assignment[i]>=0) {
						newHead.add(sortedExpectedHead[i]);
					}
					this.newHead = newHead.toArray(new String[0]);
				}
			} else {
				newHead = new String[0];
			}
		}
		else {
			newHead = sortedExpectedHead;
		}

		expectedNameToColIndex = new Hashtable<String, Integer>();
		for (i = 0; i < sortedExpectedHead.length; i++) {
			expectedNameToColIndex.put(sortedExpectedHead[i], Integer
					.valueOf(assignment[i]));
		}
		
		/*If no editing is supposed to be done, we do not allow assignments of more than one element to the same column.
		*/
		if(acceptWithoutEdit) {
			List<Integer> equalElements=new LinkedList<Integer>();
			List<String> elementsToRemove = new LinkedList<String>();
			for(i=0;i!=sortedExpectedHead.length;i++) {
				if(assignment[i]>=0) {
					equalElements.clear();
					equalElements.add(i);
					for(int j=i+1;j!=sortedExpectedHead.length;j++) {
						if(assignment[i]==assignment[j]) {
							equalElements.add(j);
						}
					}
					if(equalElements.size()>1) {
						int max=0;
						int maxElement=-1;
						for(int number:equalElements) {
							int length=sortedExpectedHead[number].length();
							if(length>=max) {
								max=length;
								maxElement=number;
							}
						}
						for(int number:equalElements) {
							if(maxElement!=number) {
								assignment[number]=-1;
								elementsToRemove.add(sortedExpectedHead[number]);
							}
						}
					}
					
				}
				
			}
			if(elementsToRemove.size()>0) {
				List<String> newHeadList = new LinkedList<String>();
				for(String element:newHead) {
					if(!elementsToRemove.contains(element)) {
						newHeadList.add(element);
					}
				}
				this.newHead = newHeadList.toArray(new String[0]);
			}
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @param hideExactColumnNames
	 * @param pathname
	 * @param acceptWithoutEdit
	 * @param expectedHead
	 * @throws IOException
	 */
	public CSVImporter(Component parent, boolean hideExactColumnNames,
			String pathname, boolean acceptWithoutEdit, String... expectedHead) throws IOException {
		this(parent,hideExactColumnNames,pathname,acceptWithoutEdit,false,expectedHead);
	}

	/**
	 * 
	 * @param pathname
	 * @param expectedHead
	 * @throws IOException
	 */
	public CSVImporter(Component parent, String pathname,
			String... expectedHead) throws IOException {
		this(parent, true, pathname, expectedHead);
	}

	/**
	 * 
	 * @param pathname
	 * @param expectedHead
	 * @throws IOException
	 */
	public CSVImporter(String pathname, String... expectedHead)
			throws IOException {
		this(null, pathname, expectedHead);
	}

	/**
	 * 
	 * @return An array whose length is identical to the number of expected
	 *         columns. Each value in this array is the index of the column in
	 *         the CSV file. If no such element exists in the file, the array
	 *         will contain minus one at this position. Note that the expected
	 *         column headers have been alphabetically sorted before generating
	 *         this array. To obtain the sorted array of expected headers, see
	 *         {@link #getSortedExpectedHead}.
	 */
	public int[] getColumnAssignment() {
		return assignment;
	}

	/**
	 * Returns the column index of the given expected table head entry or -1 if
	 * no such element exists in the data file.
	 * 
	 * @param expectedHeadEntry
	 * @return
	 */
	public int getColumnIndex(String expectedHeadEntry) {
		if (expectedNameToColIndex.containsKey(expectedHeadEntry)) {
			return expectedNameToColIndex.get(expectedHeadEntry).intValue();
		}
		return -1;
	}

	/**
	 * Returns the parser used to read the given CSV file.
	 * 
	 * @return
	 */
	public CSVReader getCSVReader() {
		return reader;
	}

	/**
	 * 
	 * @return An alphabetically sorted header only containing those elements
	 *         from the expected table head that could be assigned to elements
	 *         in the CSV file.
	 */
	public String[] getNewHead() {
		return newHead;
	}

	/**
	 * 
	 * @return
	 */
	public String[] getSortedExpectedHead() {
		return sortedExpectedHead;
	}

	/**
	 * Just to check whether or not automatically exactly matched columns were
	 * hided from the user interface for the remaining choices.
	 * 
	 * @return
	 */
	public boolean isHidingExactColumnNames() {
		return hideExactColumnNames;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isCanceled() {
		return cancelOption;
	}
}
