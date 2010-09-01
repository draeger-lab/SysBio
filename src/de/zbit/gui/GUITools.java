/**
 * 
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
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
	 * Path to the image resources in the resource bundle (in this case, .gif
	 * and .jpg files)
	 */
	private static String imagePath = null;
	
	/**
	 * The resource bundle for the default locale
	 */
	private static ResourceBundle resources = null;

	/**
	 * Loads locale-specific resources: strings, images, et cetera
	 */
	static {
		Locale locale = Locale.getDefault();
		resources = ResourceBundle.getBundle(
				"samples.resources.bundles.MetalEditResources", locale);
		imagePath = resources.getString("images.path");
	}

	/**
	 * Creates a JButton with the given properties. The tool tip becomes an HTML
	 * formatted string with a line break after 40 symbols.
	 * 
	 * @param icon
	 * @param listener
	 * @param com
	 * @param toolTip
	 * @return
	 */
	public static JButton createButton(Icon icon, ActionListener listener,
			Object command, String toolTip) {
		JButton button = new JButton();
		if (icon != null)
			button.setIcon(icon);
		if (listener != null)
			button.addActionListener(listener);
		if (command != null)
			button.setActionCommand(command.toString());
		if (toolTip != null)
			button.setToolTipText(toHTML(toolTip, 40));
		return button;
	}

	/**
	 * 
	 * @param text
	 * @param icon
	 * @param listener
	 * @param command
	 * @param toolTip
	 * @return
	 */
	public static JButton createButton(String text, Icon icon,
			ActionListener listener, Object command, String toolTip) {
		JButton button = createButton(icon, listener, command, toolTip);
		if (text != null)
			button.setText(text);
		return button;
	}

	/**
	 * Creates and returns a JCheckBox with all the given properties.
	 * 
	 * @param label
	 * @param selected
	 * @param name
	 *            The name for the component to be identifiable by the
	 *            ItemListener
	 * @param listener
	 * @param toolTip
	 * @return
	 */
	public static JCheckBox createJCheckBox(String label, boolean selected,
			String name, ItemListener listener, String toolTip) {
		JCheckBox chkbx = new JCheckBox(label, selected);
		chkbx.setName(name);
		chkbx.addItemListener(listener);
		chkbx.setToolTipText(toHTML(toolTip, 40));
		return chkbx;
	}

	/**
	 * 
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 * @return
	 */
	public static JFileChooser createJFileChooser(String dir,
			boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode) {
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
		chooser.setMultiSelectionEnabled(multiSelectionAllowed);
		chooser.setFileSelectionMode(mode);
		return chooser;
	}

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
		JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
				multiSelectionAllowed, mode);
		if (filter != null) {
			int i = filter.length - 1;
			while (0 <= i)
				chooser.addChoosableFileFilter(filter[i--]);
			if (i >= 0)
				chooser.setFileFilter(filter[i]);
		}
		return chooser;
	}

	/**
	 * 
	 * @return
	 */
	public static ImageIcon getIconOpen() {
		return getImageIcon("imageOpen");
	}

	/**
	 * 
	 * @return
	 */
	public static ImageIcon getIconSave() {
		return getImageIcon("imageSave");
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	private static ImageIcon getImageIcon(String string) {
		return new ImageIcon(GUITools.class.getResource(imagePath
				+ resources.getString(string)));
	}

	public static Font incrementFontSize(Font g, int incrementBy) {
		return g.deriveFont((float) (g.getSize() + incrementBy));
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
	 * Replaces two components. Tries to preserve the layout while replacing the
	 * two components on the parent of the oldOne.
	 * 
	 * @param oldOne
	 * @param newOne
	 */
	public static void replaceComponent(JComponent oldOne, JComponent newOne) {
		if (oldOne == null || oldOne.getParent() == null) {
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
			for (int i = 0; i < target.getComponents().length; i++) {
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

	/**
	 * Shows a generic Save file Dialog.
	 * 
	 * @param parentComp
	 * @return
	 */
	public static File saveFileDialog(final Component parentComp) {
		final JFileChooser fc = new JFileChooser(new File("."));
		fc.setDialogTitle("Select a target");

		if (fc.showSaveDialog(parentComp) == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists()) {
				if (!GUITools.overwriteExistingFile(parentComp, fc
						.getSelectedFile())) {
					return null;
				}
			}
			return fc.getSelectedFile();
		}
		return null;
	}

	/**
	 * 
	 * @param c
	 * @param enabled
	 */
	public static void setAllEnabled(Container c, boolean enabled) {
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container)
				setAllEnabled((Container) children[i], enabled);
			children[i].setEnabled(enabled);
		}
	}

	/**
	 * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
	 * all menu items and buttons that are associated with the given actions are
	 * enabled or disabled.
	 * 
	 * @param state
	 *            if true buttons, items etc. are enabled, otherwise disabled.
	 * @param menuBar
	 * @param toolbar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar,
			JToolBar toolbar, Object... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (Object command : commands)
			setOfCommands.add(command.toString());
		if (menuBar != null)
			for (i = 0; i < menuBar.getMenuCount(); i++) {
				JMenu menu = menuBar.getMenu(i);
				for (j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (item instanceof JMenu) {
						JMenu m = (JMenu) item;
						boolean containsCommand = false;
						for (int k = 0; k < m.getItemCount(); k++) {
							JMenuItem it = m.getItem(k);
							if (it != null
									&& it.getActionCommand() != null
									&& setOfCommands.contains(it
											.getActionCommand())) {
								it.setEnabled(state);
								containsCommand = true;
							}
						}
						if (containsCommand)
							m.setEnabled(state);
					}
					if (item != null && item.getActionCommand() != null
							&& setOfCommands.contains(item.getActionCommand()))
						item.setEnabled(state);
				}
			}
		if (toolbar != null)
			for (i = 0; i < toolbar.getComponentCount(); i++) {
				Object o = toolbar.getComponent(i);
				if (o instanceof JButton) {
					JButton b = (JButton) o;
					if (setOfCommands.contains(b.getActionCommand())) {
						b.setEnabled(state);
						// if (b.getIcon() != null
						// && b.getIcon() instanceof CloseIcon)
						// ((CloseIcon) b.getIcon())
						// .setColor(state ? Color.BLACK : Color.GRAY);
					}
				}
			}
	}

	/**
	 * 
	 * @param state
	 * @param toolbar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JToolBar toolbar,
			Object... commands) {
		setEnabled(state, null, toolbar, commands);
	}

	/**
	 * Displayes the error message on a {@link JOptionPane}.
	 * 
	 * @param exc
	 */
	public static void showErrorMessage(Component parent, Throwable exc) {
		exc.printStackTrace();
		JOptionPane.showMessageDialog(parent, exc.getMessage(), exc.getClass()
				.getSimpleName(), JOptionPane.ERROR_MESSAGE);
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
