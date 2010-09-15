/**
 * 
 */
package de.zbit.gui.cfg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;

/**
 * This is a special {@link JPanel} that displays {@link JTextField}s together
 * with a {@link JLabel} and {@link JButton} to open default directories from
 * the file system. If you want to let the buttons display icons such as a
 * folder or a disk symbol, please put these icons under the following keys into
 * the {@link UIManager}:
 * <ul>
 * <li>ICON_OPEN</li>
 * <li>ICON_SAVE</li>
 * </ul>
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-09-14
 * 
 */
public class DirectoryChooser extends JPanel implements ActionListener {

	/**
	 * Lists all possible action commands for this {@link DirectoryChooser}.
	 * These are necessary when calling the
	 * {@link DirectoryChooser#actionPerformed(ActionEvent)} to decide what to
	 * do.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-09-10
	 */
	public enum Command {
		/**
		 * Command to open a {@link JFileChooser} with an open dialog.
		 */
		OPEN,
		/**
		 * Command to open a {@link JFileChooser} with a save dialog.
		 */
		SAVE
	}

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3353915785190340070L;

	/**
	 * These fields contain the desired directory name.
	 */
	private JTextField tfOpenDir, tfSaveDir;

	/**
	 * The default, i.e., initial values for both text fields
	 */
	private String defaultOpenDir, defaultSaveDir;

	/**
	 * Switch of whether to
	 * <ul>
	 * <li>show an error message if a selected directory does not yet exist
	 * (default; false)</li>
	 * <li>create the directory and only show an error message if it is not
	 * possible to create this directory (true).</li>
	 * </ul>
	 */
	private boolean createDir;

	/**
	 * Explaining text for the buttons.
	 */
	private static final String toolTipButtons = "Select the default directory to %s various kinds of files.";

	/**
	 * Both arguments may be empty Strings, i.e., no default value is given, or
	 * null, i.e., no such label/field/button combination will appear. If
	 * selected directories do not yet exist, with this constructor those will
	 * not be created. This behavior can be changed using
	 * {@link #setCreateDir(boolean)}.
	 * 
	 * @param openDir
	 *            The default directory to open files
	 * @param saveDir
	 *            The default directory to save files
	 */
	public DirectoryChooser(String openDir, String saveDir) {
		this(openDir, saveDir, false);
	}

	/**
	 * Both arguments may be empty Strings, i.e., no default value is given, or
	 * null, i.e., no such label/field/button combination will appear. If
	 * selected directories do not yet exist, with this constructor those will
	 * be created if possible. This behavior can be changed using
	 * {@link #setCreateDir(boolean)}.
	 * 
	 * @param openDir
	 *            The default directory to open files
	 * @param saveDir
	 *            The default directory to save files
	 * @param createDir
	 *            Whether or not to try to create directories that do not yet
	 *            exist.
	 */
	public DirectoryChooser(String openDir, String saveDir, boolean createDir) {
		super();
		this.createDir = createDir;
		defaultOpenDir = openDir;
		defaultSaveDir = saveDir;
		if ((openDir != null) || (saveDir != null)) {
			JButton openButton = null, saveButton = null;
			JLabel labelOpenDir = null, labelSaveDir = null;
			LayoutHelper lh = new LayoutHelper(this);
			int row = 0;
			if (openDir != null) {
				defaultOpenDir = openDir;
				tfOpenDir = new JTextField(openDir);
				openButton = GUITools.createButton("Browse", UIManager
						.getIcon("ICON_OPEN"), this, Command.OPEN, String
						.format(toolTipButtons, "open"));
				// tfOpenDir.addKeyListener(this);
				labelOpenDir = new JLabel("Open directory:");
				lh.add(labelOpenDir, 0, row, 1, 1, 0, 0);
				lh.add(new JPanel(), 1, row, 1, 1, 0, 0);
				lh.add(tfOpenDir, 2, row, 1, 1, 1, 0);
				lh.add(new JPanel(), 3, row, 1, 1, 0, 0);
				lh.add(openButton, 4, row, 1, 1, 0, 0);
				if (saveDir != null) {
					lh.add(new JPanel(), 0, ++row, 5, 1, 1, 0);
				}
				row++;
			}
			if (saveDir != null) {
				defaultSaveDir = saveDir;
				tfSaveDir = new JTextField(saveDir);
				saveButton = GUITools.createButton("Browse", UIManager
						.getIcon("ICON_SAVE"), this, Command.SAVE, String
						.format(toolTipButtons, "save"));
				// tfSaveDir.addKeyListener(this);
				labelSaveDir = new JLabel("Save directory:");
				lh.add(labelSaveDir, 0, row, 1, 1, 0, 0);
				if (openDir == null) {
					lh.add(new JPanel(), 3, row, 1, 1, 0, 0);
				}
				lh.add(tfSaveDir, 2, row, 1, 1, 1, 0);
				lh.add(saveButton, 4, row, 1, 1, 0, 0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() != null) {
			try {
				Command com = Command.valueOf(e.getActionCommand());
				switch (com) {
				case OPEN:
					chooseDirectory(tfOpenDir, Command.OPEN);
					break;
				case SAVE:
					chooseDirectory(tfSaveDir, Command.SAVE);
					break;
				default:
					break;
				}
			} catch (Throwable t) {
				// Just ignore this invalid event.
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
		if (tfOpenDir != null) {
			tfOpenDir.addKeyListener(listener);
		}
		if (tfSaveDir != null) {
			tfSaveDir.addKeyListener(listener);
		}
	}

	/**
	 * This actually checks whether or not the directory on the given
	 * {@link JTextField} does exist or not. The second argument gives the
	 * default value to reset the element. Depending on the value of the field
	 * {@link #createDir} this method attempts to create the directory if not
	 * yet existent or directly displays an error message on a
	 * {@link JOptionPane}.
	 * 
	 * @param tf
	 *            The text field with a directory name.
	 * @param defaultDir
	 *            The default value in case of invalid content in the text
	 *            field.
	 * @return True if the content of the given text field is a valid directory
	 *         or could be successfully created, false otherwise. Maybe error
	 *         messages will be displayed to the user on {@link JOptionPane}s.
	 */
	private boolean checkDir(JTextField tf, String defaultDir) {
		if (tf != null) {
			File f = new File(tf.getText());
			if (f.exists() && f.isDirectory()) {
				return true;
			}
			if (createDir) {
				try {
					f.createNewFile();
					return true;
				} catch (IOException exc) {
					GUITools.showErrorMessage(this, exc);
				}
			} else {
				JOptionPane.showMessageDialog(getTopLevelAncestor(),
						new JLabel(GUITools.toHTML(String.format(
								"No such directory %s.", f.getPath()), 40)),
						"Warning", JOptionPane.WARNING_MESSAGE);
				tf.setText(defaultDir);
			}
		}
		return false;
	}

	/**
	 * Checks whether or not the currently selected open directory represents a
	 * valid and existing directory. Depending on the field {@link #createDir}
	 * this method will attempt to create the directory if not yet present or
	 * directly display an error message to the user using a certain
	 * {@link JOptionPane}.
	 * 
	 * @return True if the directory in the open text field is already existing
	 *         or could be successfully created, false otherwise.
	 */
	public boolean checkOpenDir() {
		return checkDir(tfOpenDir, defaultOpenDir);
	}

	/**
	 * Checks whether or not the currently selected save directory represents a
	 * valid and existing directory. Depending on the field {@link #createDir}
	 * this method will attempt to create the directory if not yet present or
	 * directly display an error message to the user using a certain
	 * {@link JOptionPane}.
	 * 
	 * @return True if the directory in the save text field is already existing
	 *         or could be successfully created, false otherwise.
	 */
	public boolean checkSaveDir() {
		return checkDir(tfSaveDir, defaultSaveDir);
	}

	/**
	 * Displays a {@link JFileChooser} to the user to select the desired
	 * directory. If the user approves the dialog, the corresponding
	 * {@link JTextField} will display the selected file.
	 * 
	 * @param tf
	 *            The text field whose value is to be changed.
	 * @param com
	 *            The command, i.e., open directory or save directory.
	 */
	private void chooseDirectory(JTextField tf, Command com) {
		JFileChooser chooser = GUITools.createJFileChooser(tf.getText(), false,
				false, JFileChooser.DIRECTORIES_ONLY);
		int returnType;
		switch (com) {
		case OPEN:
			returnType = chooser.showOpenDialog(this);
			break;
		case SAVE:
			returnType = chooser.showSaveDialog(this);
			break;
		default:
			returnType = JFileChooser.CANCEL_OPTION;
			break;
		}
		if (returnType == JFileChooser.APPROVE_OPTION) {
			tf.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Provides access to the default value for the open directory.
	 * 
	 * @return The path to the directory that is the default value for the open
	 *         text field or null if no default value has been set for this
	 *         element.
	 */
	public String getDefaultOpenDir() {
		return defaultOpenDir;
	}

	/**
	 * Provides access to the default value for the save directory.
	 * 
	 * @return The path to the directory that is the default value for the save
	 *         text field or null if no default value has been set for this
	 *         element.
	 */
	public String getDefaultSaveDir() {
		return defaultSaveDir;
	}

	/**
	 * Returns the directory to open files as selected by the user or null if no
	 * such field is present on this {@link DirectoryChooser}.
	 * 
	 * @return The path to the selected directory. This may be an invalid path,
	 *         or the file may have to be created. An application will have to
	 *         check this.
	 * @see #checkOpenDir()
	 */
	public String getOpenDir() {
		return tfOpenDir != null ? tfOpenDir.getText() : null;
	}

	/**
	 * Returns the directory to save files as selected by the user or null if no
	 * such field is present on this {@link DirectoryChooser}.
	 * 
	 * @return The path to the selected directory. This may be an invalid path,
	 *         or the file may have to be created. An application will have to
	 *         check this.
	 * @see #checkSaveDir()
	 */
	public String getSaveDir() {
		return tfSaveDir != null ? tfSaveDir.getText() : null;
	}

	/**
	 * Necessary to check whether the methods {@link #checkOpenDir()} and
	 * {@link #checkSaveDir()} will attempt to create a directory if the entry
	 * in the associated text field is not yet existing.
	 * 
	 * @return True if directories should be created or false otherwise.
	 */
	public boolean isSetCreateDir() {
		return createDir;
	}

	/**
	 * Method to check whether or not this element displays a chooser for open
	 * directories.
	 * 
	 * @return true if the element to open directories is present, false
	 *         otherwise.
	 */
	public boolean isSetOpenChooser() {
		return tfOpenDir != null;
	}

	/**
	 * Method to check whether or not this element displays a chooser for save
	 * directories.
	 * 
	 * @return true if the element to save directories is present, false
	 *         otherwise.
	 */
	public boolean isSetSaveChooser() {
		return tfSaveDir != null;
	}

	/**
	 * Method to decide whether or not this element should attempt to create
	 * directories in the methods {@link #checkOpenDir()} and
	 * {@link #checkSaveDir()} if the value in the corresponding text field
	 * points to a path that is not yet existent.
	 * 
	 * @param createDir
	 *            True means try to create directories, false not.
	 */
	public void setCreateDir(boolean createDir) {
		this.createDir = createDir;
	}

}
