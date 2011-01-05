/**
 * 
 */
package de.zbit.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
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
import de.zbit.io.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * This is a special {@link JPanel} that displays a {@link JTextField} together
 * with a {@link JLabel} and a {@link JButton} to select instances of
 * {@link File} representing files or directories from the file system to open
 * or save information. If you want to let the {@link JButton} display an
 * {@link Icon} such as a folder or a disk symbol, please put these icons under
 * the following keys into the {@link UIManager}:
 * <ul>
 * <li>ICON_OPEN_16</li>
 * <li>ICON_SAVE_16</li>
 * </ul>
 * To this end, please proceed as follows:
 * 
 * <pre>
 * UIManager.put(&quot;ICON_OPEN_16&quot;, myOpenIcon);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * UIManager.put(&quot;ICON_SAVE_16&quot;, mySaveIcon);
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
 * FileSelector selector = new FileSelector(Type.OPEN,
 * 	SBFileFilter.SBML_FILE_FILTER);
 * JOptionPane.showMessageDialog(null, selector);
 * try {
 * 	System.out.println(selector.getSelectedFile());
 * } catch (IOException exc) {
 * 	GUITools.showErrorMessage(null, exc);
 * }
 * </pre>
 * 
 * The following example demonstrate how to create a {@link FileSelector} for an
 * input and an output file:
 * 
 * <pre>
 * GUITools.initLaF(&quot;FileSelector test&quot;);
 * JPanel p = new JPanel();
 * FileSelector selectors[] = createOpenSavePanel(new LayoutHelper(p), System
 * 		.getProperty(&quot;user.dir&quot;), false,
 * 	new SBFileFilter[] { SBFileFilter.SBML_FILE_FILTER }, System
 * 			.getProperty(&quot;user.dir&quot;), false,
 * 	new SBFileFilter[] { SBFileFilter.TeX_FILE_FILTER });
 * if (JOptionPane
 * 		.showConfirmDialog(null, p, &quot;Test&quot;, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
 * 	for (FileSelector fs : selectors) {
 * 		try {
 * 			System.out.println(fs.getSelectedFile());
 * 		} catch (IOException exc) {
 * 			GUITools.showErrorMessage(null, exc);
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * @author draeger
 * @date 2010-11-02
 */
public class FileSelector extends JPanel implements ActionListener {
	
	/**
	 * 
	 * @author draeger
	 * @date 2010-11-03
	 */
	public class Command implements ActionCommand {
		/**
		 * 
		 */
		private Type type;
		
		/**
		 * 
		 * @param type
		 */
		public Command(String type) {
			this.type = Type.valueOf(type);
		}
		
		/**
		 * 
		 * @param type
		 */
		public Command(Type type) {
			this.type = type;
		}
		
		/**
		 * 
		 * @return
		 */
		public String getLabelText() {
			return String.format(acceptOnlyFiles() ? "%s file: " : "%s directory: ",
				command.getType() == Type.OPEN ? "Open" : "Save");
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			return StringUtil.firstLetterUpperCase(type.toString());
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			if (acceptOnlyFiles()) { return String.format(
				"Select the file to be %s.", type == Type.OPEN ? "opened" : "saved"); }
			return String.format(
				"Select the default directory to %s various kinds of files.",
				type == Type.OPEN ? "open" : "save");
		}
		
		/**
		 * 
		 * @return
		 */
		public Type getType() {
			return type;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		textField.addKeyListener(l);
	}
		
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		textField.setName(name);
	}
	
	/**
	 * Contains the complete configuration for a {@link FileSelector}.
	 * 
	 * @author draeger
	 * @date 2010-11-03
	 */
	public static class Configuration {
		private boolean allFilesAcceptable;
		private String baseDir;
		private FileFilter filter[];
		private Type type;
		
		public Configuration(Type type, String baseDir, boolean allFilesAcceptable,
			FileFilter... filter) {
			this.type = type;
			this.baseDir = baseDir;
			this.allFilesAcceptable = allFilesAcceptable;
			this.filter = filter;
		}
		
		/**
		 * @return the baseDir
		 */
		public String getBaseDir() {
			return baseDir;
		}
		
		/**
		 * @return the filter
		 */
		public FileFilter[] getFileFilters() {
			return filter;
		}
		
		/**
		 * @return the type
		 */
		public Type getType() {
			return type;
		}
		
		/**
		 * @return the allFilesAcceptable
		 */
		public boolean isAllFilesAcceptable() {
			return allFilesAcceptable;
		}
		
		/**
		 * @param allFilesAcceptable
		 *        the allFilesAcceptable to set
		 */
		public void setAllFilesAcceptable(boolean allFilesAcceptable) {
			this.allFilesAcceptable = allFilesAcceptable;
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
		public void setFileFilters(FileFilter... filter) {
			this.filter = filter;
		}
		
		/**
		 * @param type
		 *        the type to set
		 */
		public void setType(Type type) {
			this.type = type;
		}
	}
	
	/**
	 * Lists all possible action commands for this {@link FileSelector}. These are
	 * necessary when calling the
	 * {@link FileSelector#actionPerformed(ActionEvent)} to decide what to do.
	 * 
	 * @author draeger
	 * @date 2010-11-02
	 */
	public enum Type {
		/**
		 * Command to open a {@link JFileChooser} with an open dialog.
		 */
		OPEN,
		/**
		 * Command to open a {@link JFileChooser} with a save dialog.
		 */
		SAVE;
	}
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2479909701477969474L;
	
	/**
	 * 
	 * @param lh
	 * @param configuration
	 * @return
	 */
	public static FileSelector[] addSelectorsToLayout(LayoutHelper lh,
		Configuration... configuration) {
		FileSelector selectors[] = new FileSelector[configuration != null ? configuration.length
				: 0];
		for (int i = 0; i < configuration.length; i++) {
			selectors[i] = new FileSelector();
			selectors[i].init(lh, configuration[i].getType(), configuration[i]
					.getBaseDir(), configuration[i].isAllFilesAcceptable(),
				configuration[i].getFileFilters());
			if (i < configuration.length - 1) {
				lh.add(new JPanel(), 0, 5, 1, 1d, 0d);
			}
		}
		return selectors;
	}
	
	/**
	 * Adds all properties of the given {@link FileSelector} to the given
	 * {@link LayoutHelper}.
	 * 
	 * @param lh
	 * @param fs
	 * @param addSpace
	 */
	public static void addSelectorsToLayout(LayoutHelper lh, FileSelector fs,
		boolean addSpace) {
		lh.add(fs.label, 0, lh.getRow(), 1, 1, 0d, 0d);
		lh.add(new JPanel(), 1, lh.getRow(), 1, 1, 0d, 0d);
		lh.add(fs.textField, 2, lh.getRow(), 1, 1, 1d, 0d);
		lh.add(new JPanel(), 3, lh.getRow(), 1, 1, 0d, 0d);
		lh.add(fs.button, 4, 1, 1, 0d, 0d);
		if (addSpace) {
			lh.add(new JPanel(), 0, 5, 1, 0d, 0d);
		}
	}
	
	/**
	 * Adds all properties of the given {@link FileSelector} to the given
	 * {@link LayoutHelper}.
	 * 
	 * @param lh
	 * @param fs
	 */
	public static void addSelectorsToLayout(LayoutHelper lh, FileSelector fs) {
		addSelectorsToLayout(lh, fs, false);
	}
	
	/**
	 * 
	 * @return
	 */
	public static FileSelector[] createOpenSavePanel(LayoutHelper lh,
		String openBaseDir, boolean allOpenFilesAcceptable,
		FileFilter[] openFilter, String saveBaseDir,
		boolean allSaveFilesAcceptable, FileFilter[] saveFilter) {
		Configuration openConfig = new Configuration(Type.OPEN, openBaseDir,
			allOpenFilesAcceptable, openFilter);
		Configuration saveConfig = new Configuration(Type.SAVE, saveBaseDir,
			allSaveFilesAcceptable, saveFilter);
		return addSelectorsToLayout(lh, openConfig, saveConfig);
	}
	
	public static void main(String args[]) {
		GUITools.initLaF("FileSelector test");
		JPanel p = new JPanel();
		FileSelector selectors[] = createOpenSavePanel(new LayoutHelper(p), System
				.getProperty("user.dir"), false, new SBFileFilter[] { SBFileFilter
				.createSBMLFileFilter() }, System.getProperty("user.dir"), false,
			new SBFileFilter[] { SBFileFilter.createTeXFileFilter() });
		if (JOptionPane.showConfirmDialog(null, p, "Test",
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			for (FileSelector fs : selectors) {
				try {
					System.out.println(fs.getSelectedFile());
				} catch (IOException exc) {
					GUITools.showErrorMessage(null, exc);
				}
			}
		}
		
	}
	
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
	 * We need a reference to the {@link JButton} just to be able to change its
	 * tool tip text if necessary.
	 */
	private JButton button;
	
	/**
	 * The type of this {@link FileSelector}, can either be {@link Type#OPEN} or
	 * {@link Type#SAVE}.
	 */
	private Command command;
	
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
	 * We need a reference to the {@link JLabel} just to be able to change its
	 * text text if necessary.
	 */
	private JLabel label;
	
	/**
	 * The caption of the above defined label. Will be autogenerated if this value
	 * is null.
	 */
	private String labelText = null;
	
	/**
	 * An editable {@link JTextField} whith the full path of the selected file or
	 * directory.
	 */
	private JTextField textField;
	
	/**
	 * Creates a new empty {@link FileSelector}. All of its properties are
	 * undefined.
	 */
	public FileSelector() {
		super();
		this.create = true;
		this.allFilesAcceptable = false;
		this.command = null;
		this.baseDir = System.getProperty("user.dir");
		this.filter = null;
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Type#OPEN} or {@link Type#SAVE}, for directories. The base directory
	 * of browsing will be given by the {@link System} property "user.dir".
	 * 
	 * @param type
	 */
	public FileSelector(Type type) {
		this(type, (String) null);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Type#OPEN} or {@link Type#SAVE}, whose selection starts at the
	 * directory specified by the {@link System} property "user.dir" as the base
	 * directory. The filters can be null or empty. In this case this object will
	 * allow to select directories only.
	 * 
	 * @param type
	 * @param filter
	 *        - if null, only directories are permitted. If you want to be able to
	 *        select all Files, use e.g. {@link SBFileFilter#getAllFileFilter}.
	 */
	public FileSelector(Type type, FileFilter... filter) {
		this(type, null, filter);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Type#OPEN} or {@link Type#SAVE}, whose selection starts opening
	 * files at the given base directory. It can be decided if besides the given
	 * {@link FileFilter} instances also the all files filter (*) should be
	 * available in the underlying {@link JFileChooser}. No filters are given, the
	 * value of this boolean flag is ignored, because then directories are to be
	 * selected.
	 * 
	 * @param type
	 * @param baseDir
	 * @param allFilesAreAcceptable
	 * @param fileFilter
	 */
	public FileSelector(Type type, String baseDir, boolean allFilesAreAcceptable,
		FileFilter... fileFilter) {
		super();
		init(new LayoutHelper(this), type, baseDir, allFilesAreAcceptable,
			fileFilter);
	}
	
	/**
	 * Creates a new {@link FileSelector} of the desired type, i.e., to
	 * {@link Type#OPEN} or {@link Type#SAVE}, whose selection starts opening
	 * files at the given base directory. The filters can be null or empty. In
	 * this case this object will allow to select directories only.
	 * 
	 * @param type
	 * @param baseDir
	 * @param filter
	 */
	public FileSelector(Type type, String baseDir, FileFilter... filter) {
		this(type, baseDir, false, filter);
	}
	
	/**
	 * This method checks if the current configuration of this
	 * {@link FileSelector} only accepts files and no directories.
	 * 
	 * @return true if only files are accepted, false if only directories are
	 *         accepted.
	 */
	public boolean acceptOnlyFiles() {
		return !((this.filter == null) || (this.filter.length == 0))
				|| allFilesAcceptable;
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
			boolean mode = acceptOnlyFiles();
			switch (Type.valueOf(e.getActionCommand())) {
				case OPEN:
					file = GUITools.openFileDialog(this, baseDir, allFilesAcceptable,
						mode ? JFileChooser.FILES_ONLY
								: JFileChooser.DIRECTORIES_ONLY, filter);
					break;
				case SAVE:
					file = GUITools.saveFileDialog(this, baseDir, allFilesAcceptable,
						false, false, mode ? JFileChooser.FILES_ONLY
								: JFileChooser.DIRECTORIES_ONLY, filter);
					break;
				default:
					file = null;
					break;
			}
			if (file != null) {
				textField.setText(file.toString());
				baseDir = file.getParent();
	      // Notify KeyListeners of texfield change
        processKeyEvent(new KeyEvent(this, KeyEvent.KEY_RELEASED, System
                  .currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED,
                  KeyEvent.CHAR_UNDEFINED));
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
				//file = file.getParentFile();
				baseDir = file.getParent();
			} else {
				// getAbsolutePath() is NULL if e.g. "new File("text.txt")" is invoked.
				//baseDir = file.getAbsolutePath();
				baseDir = file.getPath();
			}
		}
		return baseDir;
	}
	
	public Command getCommand(Type type) {
		return new Command(type);
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
		boolean mode = acceptOnlyFiles();
		boolean justCreated = false;
		boolean acceptable = false;
		for (int i=0; (i<filter.length) && !acceptable; i++) {
			if (filter[i].accept(file)) {
				acceptable = true;
			}
		}
		if (acceptable) {
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
					switch (command.getType()) {
						case OPEN:
							if (file.canRead()) { return file; }
							//						throw new IOException(String.format("Cannot read from file %s.",
							//							file.getAbsolutePath()));
						case SAVE:
							if (file.canWrite()) {
								if (justCreated || GUITools.overwriteExistingFile(this, file)) {
									return file;
								} else {
									return null;
								}
							}
							//						throw new IOException(String.format("Cannot write to file %s.",
							//							file.getAbsolutePath()));
						default:
							break;
					}
				} else if (!mode && file.isDirectory()) {
					switch (command.getType()) {
						case OPEN:
							if (file.canRead()) { return file; }
							//						throw new IOException(String.format(
							//							"Cannot read from directory %s.", file.getAbsolutePath()));
						case SAVE:
							if (file.canWrite()) { return file; }
							//						throw new IOException(String.format(
							//							"Cannot write into directory %s.", file.getAbsolutePath()));
						default:
							break;
					}
				}
			} else {
				//			throw new FileNotFoundException(file.getAbsolutePath());
			}
		}
		return null;
	}
	
	/**
	 * One of the types defined by the {@link ActionCommand} {@link Type#OPEN} or
	 * {@link Type#SAVE}
	 * 
	 * @return the type
	 */
	public Command getType() {
		return command;
	}
	
	/**
	 * 
	 * @param fileSelector
	 * @param type
	 * @param baseFileDir
	 * @param allFilesAreAcceptable
	 * @param filters
	 */
	private void init(LayoutHelper lh, Type type, String baseFileDir,
		boolean allFilesAreAcceptable, FileFilter... filters) {
		this.create = true;
		this.allFilesAcceptable = allFilesAreAcceptable;
		this.command = getCommand(type);
		this.baseDir = baseFileDir != null ? baseFileDir : System.getProperty("user.dir");
		this.filter = filters;
		textField = new JTextField();
		textField.setText(this.baseDir);
		textField.setColumns(30);
		textField.setInputVerifier(new FileInputVerifier(
			acceptOnlyFiles() ? FileInputVerifier.FileType.FILE
					: FileInputVerifier.FileType.DIRECTORY));
		ResourceBundle resource = ResourceManager.getBundle("de.zbit.locales.Labels");
		button = GUITools.createButton(resource.getString("BROWSE"), UIManager
				.getIcon(type == Type.OPEN ? "ICON_OPEN_16" : "ICON_SAVE_16"), this, type,
			command.getToolTip());
		label = new JLabel();
		autoSetLabelText();
		lh.add(label, 0, lh.getRow(), 1, 1, 0, 0);
		lh.add(new JPanel(), 1, lh.getRow(), 1, 1, 0, 0);
		lh.add(textField, 2, lh.getRow(), 1, 1, 1, 0);
		lh.add(new JPanel(), 3, lh.getRow(), 1, 1, 0, 0);
		lh.add(button, 4, 1, 1, 0, 0);
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
		updateGUIelements();
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
		updateGUIelements();
	}
	
	/**
	 * Updates label and tool tip for this element.
	 */
	private void updateGUIelements() {
		button.setToolTipText(command.getToolTip());
		autoSetLabelText();
		textField.setInputVerifier(new FileInputVerifier(
			acceptOnlyFiles() ? FileInputVerifier.FileType.FILE
					: FileInputVerifier.FileType.DIRECTORY));
		validate();
	}
	
	public void setLabelText(String text) {
		labelText = text;
		autoSetLabelText();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String text) {
		textField.setToolTipText(text);
	}
	
	/**
	 * 
	 */
	private void autoSetLabelText() {
		if (label == null) { return; }
		if ((labelText != null) && (labelText.length() > 0)) {
			label.setText(labelText);
		} else if (command != null) {
			label.setText(command.getLabelText());
		}
	}
	
}
