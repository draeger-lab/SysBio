/**
 * 
 */
package de.zbit.gui.prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * This interface provides one single {@link Option} to memorize a {@link List}
 * of {@link File} objects that can be accessed in a graphical user interface
 * and used to open {@link File}s that have been used earlier by the user.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-12-10
 */
public interface FileHistory extends KeyProvider {
	
	
	/**
	 * The separator symbol within lists of files.
	 */
	public static final String FILE_SEPARATOR = System
			.getProperty("path.separator");
	
	/**
	 * An empty {@link List}.
	 */
	public static final List<File> emptyList = new ArrayList<File>(0);
	
	@SuppressWarnings("unchecked")
	public static Option<List<File>> LAST_OPENED = new Option<List<File>>(
		"LAST_OPENED", (Class<List<File>>) emptyList.getClass(),
		"This memorizes the last files that have been opened by the program.",
		emptyList);
	
	/**
	 * A collection of tools to facilitate working with the values associated
	 * with a {@link FileHistory}.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-12-11
	 */
	public static class Tools {
		
		/**
		 * 
		 * @param listOfFiles
		 * @return
		 */
		public static String toString(List<File> listOfFiles) {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			int i = 0;
			for (File file : listOfFiles) {
				if (i > 0) {
					sb.append(FILE_SEPARATOR);
				}
				sb.append(file.getAbsolutePath());
				i++;
			}
			sb.append(']');
			return sb.toString();
		}
		
		/**
		 * 
		 * @param fileList can be empty but never null.
		 * @return
		 */
		public static List<File> parseList(String fileList) {
			List<File> listOfFiles = new LinkedList<File>();
			if ((fileList != null) && (fileList.length() > 0)) {
				if (fileList.startsWith("[")) {
					fileList = fileList.substring(1);
				}
				if (fileList.endsWith("]")) {
					fileList = fileList.substring(0, fileList.length() - 1);
				}
				String files[] = fileList.split(FileHistory.FILE_SEPARATOR);
				File file;
				for (String filePath : files) {
					file = new File(filePath);
					if (!listOfFiles.contains(file)) {
						listOfFiles.add(file);
					}
				}
			}
			return listOfFiles;
		}
	}
	
}
