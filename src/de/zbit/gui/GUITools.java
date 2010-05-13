/**
 * 
 */
package de.zbit.gui;

import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * This class contains some methods from the corresponding class in
 * SBMLsqueezer.
 * 
 * @author draeger
 * 
 */
public class GUITools {

	/**
	 * 
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 * @param filter
	 * @return
	 */
	public static JFileChooser createJFileChooser(String dir,
			boolean allFilesAcceptable, boolean multiSelectionAllowed,
			int mode, FileFilter... filter) {
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
		chooser.setMultiSelectionEnabled(multiSelectionAllowed);
		chooser.setFileSelectionMode(mode);
		int i = filter.length - 1;
		while (0 <= i)
			chooser.addChoosableFileFilter(filter[i--]);
		if (i >= 0)
			chooser.setFileFilter(filter[i]);
		return chooser;
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	public static String toHTML(String string) {
		return toHTML(string, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param string
	 * @param lineBreak
	 * @return
	 */
	public static String toHTML(String string, int lineBreak) {
		StringTokenizer st = new StringTokenizer(string != null ? string : "",
				" ");
		StringBuilder sb = new StringBuilder();
		if (st.hasMoreElements())
			sb.append(st.nextElement().toString());
		int length = sb.length();
		sb.insert(0, "<html><body>");
		while (st.hasMoreElements()) {
			if (length >= lineBreak && lineBreak < Integer.MAX_VALUE) {
				sb.append("<br>");
				length = 0;
			} else
				sb.append(' ');
			String tmp = st.nextElement().toString();
			length += tmp.length() + 1;
			sb.append(tmp);
		}
		sb.append("</body></html>");
		return sb.toString();
	}

}
