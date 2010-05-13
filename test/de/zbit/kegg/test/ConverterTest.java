/**
 * 
 */
package de.zbit.kegg.test;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import de.zbit.kegg.gui.ConverterUI;
import de.zbit.kegg.io.FileFilterKGML;

/**
 * @author draeger
 * 
 */
public class ConverterTest {

	/**
	 * 
	 * @param path
	 *            The path to a directory that contains KGML files or a single
	 *            file.
	 */
	public ConverterTest(String path) {
		File f = new File(path);
		if (f.exists()) {
			List<File> files;
			if (f.isDirectory()) {
				files = findKGMLFiles(f);
			} else {
				files = new LinkedList<File>();
				files.add(f);
			}
			for (File file : files) {
				System.out.println(file);
				new ConverterUI(file.getAbsolutePath());
			}
		}
	}

	/**
	 * 
	 * @param args
	 *            The first argument must be a directory that contains KGML
	 *            files.
	 */
	public static void main(String args[]) {
		new ConverterTest(args[0]);
	}

	/**
	 * Recursively searches for KGML files in the given directory.
	 * 
	 * @param f
	 * @return
	 */
	private List<File> findKGMLFiles(File f) {
		LinkedList<File> l = new LinkedList<File>();
		FileFilter ff = new FileFilterKGML();
		if (f.exists()) {
			if (f.isFile() && ff.accept(f)) {
				l.add(f);
			} else if (f.isDirectory()) {
				for (File sub : f.listFiles(ff)) {
					l.addAll(findKGMLFiles(sub));
				}
			}
		}
		return l;
	}
}
