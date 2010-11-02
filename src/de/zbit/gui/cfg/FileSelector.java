/**
 * 
 */
package de.zbit.gui.cfg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import de.zbit.gui.ActionCommand;
import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;
import de.zbit.util.StringUtil;

/**
 * @author draeger
 * @date 2010-11-02
 */
public class FileSelector extends JPanel implements ActionListener {
	
	/**
	 * Lists all possible action commands for this {@link FileSelector}. These are
	 * necessary when calling the
	 * {@link FileSelector#actionPerformed(ActionEvent)} to decide what to do.
	 * 
	 * @author draeger
	 * @date 2010-11-02
	 */
	public enum Command implements ActionCommand {
		/**
		 * Command to open a {@link JFileChooser} with an open dialog.
		 */
		OPEN,
		/**
		 * Command to open a {@link JFileChooser} with a save dialog.
		 */
		SAVE;
		
		private boolean fileMode = true;
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			return StringUtil.firstLetterUpperCase(this.toString());
		}
		
		/**
		 * 
		 * @param fileMode
		 */
		public void setMode(boolean fileMode) {
			this.fileMode = fileMode;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			if (fileMode) { return String.format("Select the file to be %s.",
				this == OPEN ? "opened" : "saved"); }
			return String.format(
				"Select the default directory to %s various kinds of files.",
				this == OPEN ? "open" : "save");
		}
		
	}
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2479909701477969474L;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GUITools.initLaF("File selecter");
		FileSelector selector = new FileSelector(Command.SAVE);
		JOptionPane.showMessageDialog(null, selector);
		System.out.println(selector.getSelectedFile());
	}
	
	/**
	 * 
	 */
	private String baseDir;
	
	/**
	 * 
	 */
	private FileFilter filter[];
	
	/**
	 * 
	 */
	private JTextField textField;
	
	/**
	 * The type of this {@link FileSelector}
	 */
	private Command type;
	
	/**
	 * 
	 */
	public FileSelector() {
		this(Command.OPEN);
	}
	
	/**
	 * 
	 * @param type
	 */
	public FileSelector(Command type) {
		this(type, null);
	}
	
	/**
	 * Depending on the given type, this constructor looks in the
	 * {@link UIManager} for the icons ICON_OPEN or ICON_SAVE to be displayed on
	 * the browse button.
	 * 
	 * @param type
	 * @param baseDir
	 * @param filter
	 */
	public FileSelector(Command type, String baseDir, FileFilter... filter) {
		this.type = type;
		this.baseDir = baseDir != null ? baseDir : System.getProperty("user.dir");
		this.filter = filter;
		// Decide whether to deal with directories or files:
		boolean mode = !((this.filter == null) || (this.filter.length == 0));
		this.type.setMode(mode);
		LayoutHelper lh = new LayoutHelper(this);
		textField = new JTextField(this.baseDir);
		lh.add(new JLabel(type == Command.OPEN ? "Open file: " : "Save file: "), 0,
			0, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(textField, 2, 0, 1, 1, 1, 0);
		lh.add(new JPanel(), 3, 0, 1, 1, 0, 0);
		lh.add(GUITools.createButton("Browse", UIManager
				.getIcon(type == Command.OPEN ? "ICON_OPEN" : "ICON_SAVE"), this, type,
			type.getToolTip()), 4, 0, 1, 1, 0, 0);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() != null) {
			baseDir = getBaseDir();
			File file;
			switch (Command.valueOf(e.getActionCommand())) {
				case OPEN:
					file = GUITools.openFileDialog(this, baseDir, filter == null ? true
							: false, false, JFileChooser.FILES_ONLY, filter);
					break;
				case SAVE:
					file = GUITools.saveFileDialog(this, baseDir, filter == null ? true
							: false, false, JFileChooser.FILES_ONLY, filter);
					break;
				default:
					file = null;
					break;
			}
			if (file != null) {
				textField.setText(file.getAbsolutePath());
				baseDir = file.getParent();
			}
		}
	}
	
	/**
	 * @return the baseDir
	 */
	public String getBaseDir() {
		File file = new File(textField.getText());
		if (file.canRead()) {
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			baseDir = file.getAbsolutePath();
		}
		return baseDir;
	}
	
	/**
	 * @return the filter
	 */
	public FileFilter[] getFilter() {
		return filter;
	}
	
	/**
	 * 
	 * @return null if the selected file is not accessible, else a {@link File}
	 *         object.
	 */
	public File getSelectedFile() {
		File file = new File(textField.getText());
		if (file.isFile()) {
			switch (type) {
				case OPEN:
					if (file.canRead()) { return file; }
				case SAVE:
					if (file.canWrite() && GUITools.overwriteExistingFile(this, file)) { return file; }
				default:
					break;
			}
		} else if (!file.exists() && !file.isDirectory()) {
			try {
				file.createNewFile();
				return file;
			} catch (IOException exc) {
				GUITools.showErrorMessage(this, exc);
			}
		}
		return null;
	}
	
	/**
	 * @param baseDir
	 *        the baseDir to set
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
	/**
	 * @param filter
	 *        the filter to set
	 */
	public void setFilter(FileFilter[] filter) {
		this.filter = filter;
	}
	
}
