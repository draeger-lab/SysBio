/**
 * 
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * This class contains various GUI tools.
 * 
 * @author draeger
 * @author wrzodek
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
	 * @param parent
	 * @param out
	 * @return
	 */
	public static boolean overwriteExistingFile(Component parent, File out) {
		return GUITools.overwriteExistingFileDialog(parent, out) == JOptionPane.YES_OPTION;
	}

	/**
	 * Shows a dialog that asks whether or not to overwrite an existing file and
	 * returns the answer from JOptionPane constants.
	 * 
	 * @param parent
	 * @param out
	 * @return An integer representing the user's choice.
	 */
	public static int overwriteExistingFileDialog(Component parent, File out) {
		return JOptionPane.showConfirmDialog(parent, toHTML(out.getName()
				+ " already exists. Do you really want to over write it?", 40),
				"Over write existing file?", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
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
	
	
	
	/**
   * Replaces two components.
   * Tries to preserve the layout while replacing the two components on
   * the parent of the oldOne.
   * @param oldOne
   * @param newOne
   */
  public static void replaceComponent(JComponent oldOne, JComponent newOne) {
    if (oldOne==null || oldOne.getParent()==null) {
      // All I can do here is replacing the variables...
      oldOne = newOne;
      return;
    }
    
    Container target = oldOne.getParent();
    LayoutManager lm = target.getLayout();
    
    // Try to replace by setting same layout as old component
    if (lm instanceof BorderLayout) {
      Object c = ((BorderLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((BorderLayout) lm).addLayoutComponent(newOne, c);
      
    } else if (lm instanceof GridBagLayout) {
      Object c = ((GridBagLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((GridBagLayout) lm).addLayoutComponent(newOne, c);
      
    } else {
      // Layouts have no contstraints. Just set the correct index.
      boolean replaced = false;
      for (int i=0; i<target.getComponents().length; i++) {
        if (target.getComponents()[i].equals(oldOne)) {
          target.remove(oldOne);
          target.add(newOne, i);
          replaced = true;
          break;
        }
      }
      
      // element not found? still add the new one.
      if (!replaced) {
        target.remove(oldOne);
        target.add(newOne);
      }
        
    }
  }

}
