/**
 * 
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;

import de.zbit.io.OpenFile;
import de.zbit.util.StringUtil;

/**
 * This class contains various GUI tools.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author wrzodek
 */
public class GUITools {
	
	static {
		ImageTools.initImages(GUITools.class.getResource("img"));
	}
	
	/**
	 * Checks whether the first container contains the second one.
	 * 
	 * @param c
	 * @param insight
	 * @return True if c contains insight.
	 */
	public static boolean contains(Component c, Component insight) {
		boolean contains = c.equals(insight);
		if ((c instanceof Container) && !contains)
			for (Component c1 : ((Container) c).getComponents()) {
				if (c1.equals(insight))
					return true;
				else contains |= contains(c1, insight);
			}
		return contains;
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
		if (icon != null) {
			button.setIcon(icon);
		}
		if (listener != null) {
			button.addActionListener(listener);
		}
		if (command != null) {
			button.setActionCommand(command.toString());
		}
		if (toolTip != null) {
			button.setToolTipText(StringUtil.toHTML(toolTip, 40));
		}
		return button;
	}
	
	/**
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
		if (text != null) {
			button.setText(text);
		}
		return button;
	}
	
	/**
	 * Creates and returns a JCheckBox with all the given properties.
	 * 
	 * @param label
	 * @param selected
	 * @param name
	 *        The name for the component to be identifiable by the ItemListener
	 * @param toolTip
	 * @param listener
	 * @return
	 */
	public static JCheckBox createJCheckBox(String label, boolean selected,
		Object command, String toolTip, ItemListener... listener) {
		JCheckBox chkbx = new JCheckBox(label, selected);
		chkbx.setActionCommand(command.toString());
		for (ItemListener l : listener) {
			chkbx.addItemListener(l);
		}
		chkbx.setToolTipText(StringUtil.toHTML(toolTip, 40));
		return chkbx;
	}
	
	/**
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 *        - e.g. JFileChooser.FILES_ONLY
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
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 *        - e.g. JFileChooser.FILES_ONLY
	 * @param filter
	 * @return
	 */
	public static JFileChooser createJFileChooser(String dir,
		boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
		FileFilter... filter) {
		JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
			multiSelectionAllowed, mode);
		if (filter != null) {
			int i = filter.length - 1;
			while (0 <= i) {
				chooser.addChoosableFileFilter(filter[i--]);
			}
			if (i >= 0) {
				chooser.setFileFilter(filter[i]);
			}
		}
		return chooser;
	}
	
	/**
	 * Creates a new JMenuItem with the given text as label and the mnemonic. All
	 * given menu items are added to the menu.
	 * 
	 * @param text
	 * @param mnemonic
	 * @param menuItems
	 *        instances of {@link JMenuItem} or {@link JSeparator}. Other
	 *        {@link Object}s are ignored.
	 * @return
	 */
	public static JMenu createJMenu(String text, char mnemonic,
		Object... menuItems) {
		JMenu menu = new JMenu(text);
		menu.setMnemonic(mnemonic);
		for (Object item : menuItems) {
			if (item instanceof JMenuItem) {
				menu.add((JMenuItem) item);
			} else if (item instanceof JSeparator) {
				menu.add((JSeparator) item);
			}
		}
		return menu;
	}
	
	/**
	 * Creates a new {@link JMenu} with the given menu items and sets the first
	 * letter in the menu's name as mnemonic.
	 * 
	 * @param text
	 * @param menuItems
	 * @return
	 */
	public static JMenu createJMenu(String text, Object... menuItems) {
		return createJMenu(text, text.charAt(0), menuItems);
	}
	
	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param listener
	 * @param command
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command) {
		return createJMenuItem(listener, command, (Icon) null);
	}
	
	/**
	 * @param listener
	 * @param command
	 * @param icon
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon) {
		return createJMenuItem(listener, command, icon, null);
	}
	
	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param listener
	 * @param command
	 * @param icon
	 * @param mnemonic
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon, char mnemonic) {
		return createJMenuItem(listener, command, icon, null, Character
				.valueOf(mnemonic));
	}
	
	/**
	 * Creates a new item for a {@link JMenu}.
	 * 
	 * @param listener
	 *        the ActionListener to be added
	 * @param command
	 *        the action command for this button, i.e., the item in the menu. This
	 *        will be converted to a {@link String} using the {@link
	 *        String.#toString()} method.
	 * @param icon
	 *        the icon of the JMenuItem (can be null)
	 * @param keyStroke
	 *        the KeyStroke which will serve as an accelerator
	 * @return A new {@link JMenuItem} with the given features.
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon, KeyStroke keyStroke) {
		return createJMenuItem(listener, command, icon, keyStroke, null);
	}
	
	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param listener
	 * @param command
	 * @param icon
	 * @param ks
	 * @param mnemonic
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic) {
		JMenuItem item = new JMenuItem();
		if (ks != null) {
			item.setAccelerator(ks);
		}
		if (listener != null) {
			item.addActionListener(listener);
		}
		if (mnemonic != null) {
			item.setMnemonic(mnemonic.charValue());
		}
		if (command != null) {
			item.setText(command.getName());
			item.setToolTipText(StringUtil.toHTML(command.getToolTip(), 60));
			item.setActionCommand(command.toString());
		}
		if (icon != null) {
			item.setIcon(icon);
		}
		return item;
	}
	
	/**
	 * @param listener
	 * @param command
	 * @param icon
	 * @param ks
	 * @param mnemonic
	 * @param enabled
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic,
		boolean enabled) {
		JMenuItem item = createJMenuItem(listener, command, icon, ks, mnemonic);
		item.setEnabled(enabled);
		return item;
	}
	
	/**
	 * @param listener
	 * @param command
	 * @param keyStroke
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, KeyStroke keyStroke) {
		return createJMenuItem(listener, command, null, keyStroke);
	}
	
	/**
	 * Computes and returns the dimension, i.e., the size of a given icon.
	 * 
	 * @param icon
	 *        an icon whose dimension is required.
	 * @return The dimension of the given icon.
	 */
	public static Dimension getDimension(Icon icon) {
		return icon == null ? new Dimension(0, 0) : new Dimension(icon
				.getIconWidth(), icon.getIconHeight());
	}
	
	/**
	 * @param g
	 * @param incrementBy
	 * @return
	 */
	public static Font incrementFontSize(Font g, int incrementBy) {
		return g.deriveFont((float) (g.getSize() + incrementBy));
	}
	
	/**
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
		return JOptionPane.showConfirmDialog(parent, StringUtil.toHTML(out
				.getName()
				+ " already exists. Do you really want to overwrite it?", 40),
			"Overwrite existing file?", JOptionPane.YES_NO_OPTION,
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
	 * @return null if no {@link File} has been selected for any reason or the
	 *         selected {@link File}.
	 */
	public static File saveFileDialog(Component parentComp) {
		return saveFileDialog(parentComp, System.getProperty("user.dir"), true,
			false, JFileChooser.FILES_ONLY);
	}
	
	/**
	 * @param parent
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 * @param filter
	 * @return null if no {@link File} has been selected for any reason or the
	 *         selected {@link File}.
	 */
	public static File saveFileDialog(Component parent, String dir,
		boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
		FileFilter... filter) {
		return saveFileDialog(parent, dir, allFilesAcceptable,
			multiSelectionAllowed, true, mode, filter);
	}
	
	/**
	 * 
	 * @param parent
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param checkFile
	 *        decides whether to check if the file is writable and if existing
	 *        files are to be overwritten. If false, no such check will be
	 *        performed.
	 * @param mode
	 * @param filter
	 * @return
	 */
	public static File saveFileDialog(Component parent, String dir,
		boolean allFilesAcceptable, boolean multiSelectionAllowed,
		boolean checkFile, int mode, FileFilter... filter) {
		JFileChooser fc = createJFileChooser(dir, allFilesAcceptable,
			multiSelectionAllowed, mode, filter);
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			if (f.exists()) {
				if (checkFile && !f.canWrite()) {
					JOptionPane.showMessageDialog(parent, StringUtil.toHTML(
						"Cannot write to file " + f.getAbsolutePath() + ".", 60),
						"No writing access", JOptionPane.WARNING_MESSAGE);
				} else if (f.isDirectory()
						|| (checkFile && GUITools.overwriteExistingFile(parent, f))) {
					return f;
				} else if (!checkFile) { return f; }
			} else {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @param parent
	 * @param dir
	 * @param allFilesAcceptable
	 * @param multiSelectionAllowed
	 * @param mode
	 * @param filter
	 * @return null if for some reason the no {@link File} has been selected or
	 *         the {@link File} cannot be read, else the selected {@link File}.
	 */
	public static File openFileDialog(final Component parent, String dir,
		boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
		FileFilter... filter) {
		JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
			multiSelectionAllowed, mode, filter);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.canRead()) {
				JOptionPane.showMessageDialog(parent, StringUtil.toHTML(
					"Cannot read file " + f.getAbsolutePath() + ".", 60),
					"Unable to read file", JOptionPane.WARNING_MESSAGE);
			} else {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @param c
	 * @param color
	 */
	public static void setAllBackground(Container c, Color color) {
		c.setBackground(color);
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container)
				setAllBackground((Container) children[i], color);
			children[i].setBackground(color);
		}
	}
	
	/**
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
	 * Tries to recursively find instances of {@link AbstractButton} within the
	 * given container and sets their enabled status to the given value.
	 * 
	 * @param state
	 * @param c
	 * @param command
	 */
	public static void setEnabled(boolean state, Container c, Object command) {
		Component inside;
		for (int i = 0; i < c.getComponentCount(); i++) {
			inside = c.getComponent(i);
			if (inside instanceof Container) {
				setEnabled(state, (Container) inside, command);
			} else if (inside instanceof AbstractButton) {
				String com = ((AbstractButton) inside).getActionCommand();
				if ((com != null) && (com.toString().equals(command.toString()))) {
					inside.setEnabled(state);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param state
	 * @param menuBar
	 * @param command
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar, Object command) {
		setEnabled(state, menuBar, new Object[] {command});
	}
	
	/**
	 * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
	 * all menu items and buttons that are associated with the given actions are
	 * enabled or disabled.
	 * 
	 * @param state
	 *        if true buttons, items etc. are enabled, otherwise disabled.
	 * @param menuBar
	 * @param toolbar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar,
		JToolBar toolbar, Object... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (Object command : commands) {
			setOfCommands.add(command.toString());
		}
		if (menuBar != null) {
			for (i = 0; i < menuBar.getMenuCount(); i++) {
				JMenu menu = menuBar.getMenu(i);
				for (j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (item instanceof JMenu) {
						JMenu m = (JMenu) item;
						boolean containsCommand = false;
						for (int k = 0; k < m.getItemCount(); k++) {
							JMenuItem it = m.getItem(k);
							if (it != null && it.getActionCommand() != null
									&& setOfCommands.contains(it.getActionCommand())) {
								it.setEnabled(state);
								containsCommand = true;
							}
						}
						if (containsCommand) m.setEnabled(state);
					}
					if ((item != null) && (item.getActionCommand() != null)
							&& setOfCommands.contains(item.getActionCommand())) {
						item.setEnabled(state);
					}
				}
			}
		}
		if (toolbar != null) for (i = 0; i < toolbar.getComponentCount(); i++) {
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
	 * @param state
	 * @param menuBar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar,
		Object... commands) {
		setEnabled(state, menuBar, null, commands);
	}
	
	/**
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
		JOptionPane.showMessageDialog(parent, StringUtil.insertLineBreaks(exc
				.getMessage(), 60, "\n"), exc.getClass().getSimpleName(),
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows an error dialog with the given message in case the exception does not
	 * provide any detailed message.
	 * 
	 * @param parent
	 * @param exc
	 * @param defaultMessage
	 */
	public static void showErrorMessage(Component parent, Throwable exc,
		String defaultMessage) {
		if ((exc.getMessage() == null) || (exc.getMessage().length() == 0)) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(parent, defaultMessage, exc.getClass()
					.getSimpleName(), JOptionPane.ERROR_MESSAGE);
		} else {
			showErrorMessage(parent, exc);
		}
	}
	
	/**
	 * Displays a message on a message dialog window, i.e., an HTML document.
	 * 
	 * @param path
	 *        the URL of an HTML document.
	 * @param title
	 *        the title of the dialog to be displayed
	 * @param owner
	 *        the parent of the dialog or null.
	 */
	public static void showMessage(URL path, String title, Component owner) {
		showMessage(path, title, owner, null);
	}
	
	/**
	 * @param path
	 * @param title
	 * @param owner
	 * @param icon
	 */
	public static void showMessage(URL path, String title, Component owner,
		Icon icon) {
		JBrowserPane browser = new JBrowserPane(path);
		browser.removeHyperlinkListener(browser);
		browser.addHyperlinkListener(new SystemBrowser());
		browser.setBorder(BorderFactory.createEtchedBorder());
		
		try {
			File f = new File(OpenFile.doDownload(path.toString()));
			BufferedReader br;
			br = new BufferedReader(new FileReader(f));
			String line;
			int rowCount = 0, maxLine = 0;
			while (br.ready()) {
				line = br.readLine();
				if (line.length() > maxLine) {
					maxLine = line.length();
				}
				rowCount++;
			}
			
			if ((rowCount > 100) || (maxLine > 250)) {
				JScrollPane scroll = new JScrollPane(browser,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				// TODO: Calculate required size using the font size.
				scroll.setMaximumSize(new Dimension(470, 470));
				Dimension prefered = new Dimension(450, 450);
				browser.setPreferredSize(prefered);
				JOptionPane.showMessageDialog(owner, scroll, title,
					JOptionPane.INFORMATION_MESSAGE, icon);
			} else {
				JOptionPane.showMessageDialog(owner, browser, title,
					JOptionPane.INFORMATION_MESSAGE, icon);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
			showErrorMessage(owner, exc);
		}
	}
	
	/**
	 * Initializes the look and feel.
	 * 
	 * @param title
	 *        - Name of your application.
	 */
	public static void initLaF(String title) {
		Locale.setDefault(Locale.ENGLISH);
		// For MacOS X
		boolean isMacOSX = false;
		if (System.getProperty("mrj.version") != null) {
			isMacOSX = true;
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				title);
		}
		try {
			
			UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
			String osName = System.getProperty("os.name");
			if (osName.equals("Linux") || osName.equals("FreeBSD")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			} else if (isMacOSX) {
				UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
			} else if (osName.contains("Windows")) {
				UIManager
						.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
			} else {
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look
			// and feel.
			// Native look and feel for Windows, MacOS X. GTK look and
			// feel for Linux, FreeBSD
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
				
			} catch (Exception exc) {
				JOptionPane.showMessageDialog(null, StringUtil.toHTML(exc.getMessage(),
					40), exc.getClass().getName(), JOptionPane.WARNING_MESSAGE);
				exc.printStackTrace();
			}
		}
	}
	
	/**
	 * Recursively set opaque to a value for p and all JComoponents on p.
	 * @param p
	 * @param val - false means transparent, true means object has a background.
	 */
  public static void setOpaqueForAllElements(JComponent p, boolean val) {
    if (p instanceof JTextField || p instanceof JTextArea) return;
    p.setOpaque(val);
    
    for (Component c: p.getComponents()) {
      if (c instanceof JComponent) {
        setOpaqueForAllElements((JComponent) c, val);
      }
    }
      
  }
  
  
}
