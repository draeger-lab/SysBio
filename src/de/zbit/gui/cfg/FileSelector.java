/**
 * 
 */
package de.zbit.gui.cfg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import de.zbit.gui.ActionCommand;
import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;
import de.zbit.util.StringUtil;

/**
 * This is a special {@link JPanel} that displays a {@link JTextField} together
 * with a {@link JLabel} and a {@link JButton} to select instances of
 * {@link File} representing files or directories from the file system to open
 * or save information. If you want to let the {@link JButton} display an
 * {@link Icon} such as a folder or a disk symbol, please put these icons under
 * the following keys into the {@link UIManager}:
 * <ul>
 * <li>ICON_OPEN</li>
 * <li>ICON_SAVE</li>
 * </ul>
 * To this end, please proceede as follows:
 * 
 * <pre>
 * UIManager.put(&quot;ICON_OPEN&quot;, myOpenIcon);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * UIManager.put(&quot;ICON_SAVE&quot;, mySaveIcon);
 * </pre>
 * 
 * where
 * 
 * <pre>
 * myOpenIcon
 * </pre>
 * 
 * and
 * 
 * <pre>
 * mySaveIcon
 * </pre>
 * 
 * are variables of previously loaded instances of the {@link Icon} class.
 * 
 * An example of how to use this class would be
 * 
 * <pre>
 * GUITools.initLaF(&quot;File selector&quot;);
 * FileSelector selector = new FileSelector(Command.OPEN,
 * 	SBFileFilter.SBML_FILE_FILTER);
 * JOptionPane.showMessageDialog(null, selector);
 * try {
 * 	System.out.println(selector.getSelectedFile());
 * } catch (IOException exc) {
 * 	GUITools.showErrorMessage(null, exc);
 * }
 * </pre>
 * 
 * @author draeger
 * @date 2010-11-02
 */
public class FileSelector extends JPanel implements ActionListener {
	
	/**
	 * 
	 * @return
	 */
	public static JPanel createOpenSavePanel() {
		JPanel p = new JPanel();
		// TODO
		return p;
	}
	
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
		
		/**
		 * 
		 * @param fileMode
		 */
		public void setMode(boolean fileMode) {
			this.fileMode = fileMode;
		}
		
	}
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2479909701477969474L;
	
	/**
	 * Switch if all files can also be selected in case of {@link File} mode (not
	 * directory).
	 */
	private boolean allFilesAcceptable;
	
	/**
	 * The default, i.e., initial values for both text fields
	 */
	private String baseDir;
	
	/**
	 * Switch to decide if non-existing files or directories should be created.
	 */
	private boolean create;
	
	/**
	 * {@link FileFilter} instances for the supported file types. This field can
	 * be null or of zero length.
	 */
	private FileFilter filter[];
	
	/**
	 * An editable {@link JTextField} whith the full path of the selected file or
	 * directory.
	 */
	private JTextField textField;
	
	/**
	 * The type of this {@link FileSelector}, can either be {@link Command#OPEN}
	 * or {@link Command#SAVE}.
	 */
	private Command type;
	
	/**
	 * Creates a new {@link FileSelector} to open an directory. The base directory
	 * of browsing will be given by the {@link System} property "user.dir".
	 */
	public FileSelector() {
		this(Command.OPEN);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Command#OPEN} or {@link Command#SAVE}, for directories. The base
	 * directory of browsing will be given by the {@link System} property
	 * "user.dir".
	 * 
	 * @param type
	 */
	public FileSelector(Command type) {
		this(type, (String) null);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Command#OPEN} or {@link Command#SAVE}, whose selection starts at the
	 * directory specified by the {@link System} property "user.dir" as the base
	 * directory. The filters can be null or empty. In this case this object will
	 * allow to select directories only.
	 * 
	 * @param type
	 * @param filter
	 */
	public FileSelector(Command type, FileFilter... filter) {
		this(type, null, filter);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Command#OPEN} or {@link Command#SAVE}, whose selection starts
	 * opening files at the given base directory. It can be decided if besides the
	 * given {@link FileFilter} instances also the all files filter (*) should be
	 * available in the underlying {@link JFileChooser}. No filters are given, the
	 * value of this boolean flag is ignored, because then directories are to be
	 * selected.
	 * 
	 * @param type
	 * @param baseDir
	 * @param allFilesAreAcceptable
	 * @param filter
	 */
	public FileSelector(Command type, String baseDir,
		boolean allFilesAreAcceptable, FileFilter... filter) {
		this.create = true;
		this.allFilesAcceptable = allFilesAreAcceptable;
		this.type = type;
		this.baseDir = baseDir != null ? baseDir : System.getProperty("user.dir");
		this.filter = filter;
		// Decide whether to deal with directories or files:
		boolean mode = !((this.filter == null) || (this.filter.length == 0));
		this.type.setMode(mode);
		LayoutHelper lh = new LayoutHelper(this);
		textField = new JTextField(this.baseDir);
		String label = String.format(mode ? "%s file: " : "%s file directory: ",
			type == Command.OPEN ? "Open" : "Save");
		
		lh.add(new JLabel(label), 0, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(textField, 2, 0, 1, 1, 1, 0);
		lh.add(new JPanel(), 3, 0, 1, 1, 0, 0);
		lh.add(GUITools.createButton("Browse", UIManager
				.getIcon(type == Command.OPEN ? "ICON_OPEN" : "ICON_SAVE"), this, type,
			type.getToolTip()), 4, 0, 1, 1, 0, 0);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Command#OPEN} or {@link Command#SAVE}, whose selection starts
	 * opening files at the given base directory. The filters can be null or
	 * empty. In this case this object will allow to select directories only.
	 * 
	 * @param type
	 * @param baseDir
	 * @param filter
	 */
	public FileSelector(Command type, String baseDir, FileFilter... filter) {
		this(type, baseDir, false, filter);
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
			boolean mode = !((this.filter == null) || (this.filter.length == 0));
			switch (Command.valueOf(e.getActionCommand())) {
				case OPEN:
					file = GUITools.openFileDialog(this, baseDir,
						mode ? allFilesAcceptable : false, false,
						mode ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY,
						filter);
					break;
				case SAVE:
					file = GUITools.saveFileDialog(this, baseDir,
						mode ? allFilesAcceptable : false, false,
						mode ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY,
						filter);
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
	 * This directory is either the parent of the selected {@link File} if this
	 * {@link FileSelector} has been configured to select files, or the selected
	 * directory itself otherwise. If no change has been performed by the user,
	 * the returned directory path is the one that was specified when initializing
	 * this object, i.e., either given by the {@link System} property "user.dir"
	 * or directly specified.
	 * 
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
	 * Gives the {@link FileFilter} instances used in this class. Can be null or
	 * of zero length.
	 * 
	 * @return the filter
	 */
	public FileFilter[] getFilter() {
		return filter;
	}
	
	/**
	 * This method yields or creates the user-selected file or directory.
	 * 
	 * @return null if the selected file is not accessible or not of the desired
	 *         type (not accepted by any of the given {@link FileFilter}
	 *         instances), else a {@link File} object.
	 * @throws IOException
	 *         If the file or directory to be opened cannot be read or it cannot
	 *         be written to the file or directory where we want to save something
	 *         or if we have to create a new file or directory and this fails.
	 */
	public File getSelectedFile() throws IOException {
		File file = new File(textField.getText());
		boolean mode = !((this.filter == null) || (this.filter.length == 0));
		boolean justCreated = false;
		if (!file.exists() && create) {
			if (mode) {
				file.createNewFile();
			} else {
				file.mkdir();
			}
			justCreated = true;
		}
		if (file.exists()) {
			if (mode && file.isFile()) {
				switch (type) {
					case OPEN:
						if (file.canRead()) { return file; }
						throw new IOException(String.format("Cannot read from file %s.",
							file.getAbsolutePath()));
					case SAVE:
						if (file.canWrite()) {
							if (justCreated || GUITools.overwriteExistingFile(this, file)) { return file; }
						}
						throw new IOException(String.format("Cannot write to file %s.",
							file.getAbsolutePath()));
					default:
						break;
				}
			} else if (!mode && file.isDirectory()) {
				switch (type) {
					case OPEN:
						if (file.canRead()) { return file; }
						throw new IOException(String.format(
							"Cannot read from directory %s.", file.getAbsolutePath()));
					case SAVE:
						if (file.canWrite()) { return file; }
						throw new IOException(String.format(
							"Cannot write into directory %s.", file.getAbsolutePath()));
					default:
						break;
				}
			}
		} else {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		return null;
	}
	
	/**
	 * One of the types defined by the {@link ActionCommand} {@link Command#OPEN}
	 * or {@link Command#SAVE}
	 * 
	 * @return the type
	 */
	public Command getType() {
		return type;
	}
	
	/**
	 * Tells you whether the additional {@link FileFilter} for all files (*) is
	 * set to be available when selecting {@link File} instances (note that
	 * although it may be set to true this feature is ignored for directories).
	 * 
	 * @return the allFilesAcceptable
	 */
	public boolean isAllFilesAcceptable() {
		return allFilesAcceptable;
	}
	
	/**
	 * Answers the question whether or not this {@link FileSelector} creates
	 * non-existing files or directories when selected by the user.
	 * 
	 * @return the create
	 */
	public boolean isSetCreateFile() {
		return create;
	}
	
	/**
	 * Decide whether or not the additional {@link FileFilter} for all files (*)
	 * should be available when selecting {@link File} instances (note that
	 * although it may be set to true this feature is ignored for directories).
	 * 
	 * @param allFilesAcceptable
	 *        the allFilesAcceptable to set
	 */
	public void setAllFilesAcceptable(boolean allFilesAcceptable) {
		this.allFilesAcceptable = allFilesAcceptable;
	}
	
	/**
	 * Define the directory where the selection should be start. By default this
	 * will be given by the {@link System} property "user.dir", i.e., the working
	 * directory of this program.
	 * 
	 * @param baseDir
	 *        the baseDir to set
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
	/**
	 * Decide whether or not non-existing files or directories should be created
	 * when selected by the user.
	 * 
	 * @param create
	 *        the create to set
	 */
	public void setCreateFile(boolean create) {
		this.create = create;
	}
	
	/**
	 * This changes the selection of {@link FileFilter} instances.
	 * 
	 * @param filter
	 *        the filter to set
	 */
	public void setFilter(FileFilter... filter) {
		this.filter = filter;
	}
	
}
