/**
 * 
 */
package de.zbit.gui.cfg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;

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
	 * Explaining text for the buttons.
	 */
	private static final String toolTipButtons = "Select the default directory to %s various kinds of files.";

	/**
	 * Both arguments may be empty Strings, i.e., no default value is given, or
	 * null, i.e., no such label/field/button combination will appear.
	 * 
	 * @param openDir
	 *            the default directory to open files
	 * @param saveDir
	 *            the default directory to save files
	 */
	public DirectoryChooser(String openDir, String saveDir) {
		super();
		defaultOpenDir = openDir;
		defaultSaveDir = saveDir;
		if ((openDir != null) || (saveDir != null)) {
			JButton openButton = null, saveButton = null;
			JLabel labelOpenDir = null, labelSaveDir = null;
			LayoutHelper lh = new LayoutHelper(this);
			int row = 0;
			lh.add(new JPanel(), 1, row, 1, 1, 0, 0);
			lh.add(new JPanel(), 3, row, 1, 1, 0, 0);
			if (openDir != null) {
				defaultOpenDir = openDir;
				tfOpenDir = new JTextField(openDir);
				openButton = GUITools.createButton("Browse", UIManager
						.getIcon("ICON_OPEN"), this, Command.OPEN, String
						.format(toolTipButtons, "open"));
				// tfOpenDir.addKeyListener(this);
				labelOpenDir = new JLabel("Open directory:");
				lh.add(labelOpenDir, 0, row, 1, 1, 0, 0);
				lh.add(tfOpenDir, 2, row, 1, 1, 1, 0);
				lh.add(openButton, 4, row, 1, 1, 0, 0);
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
			switch (Command.valueOf(e.getActionCommand())) {
			case OPEN:
				chooseDirectory(tfOpenDir, Command.OPEN);
				break;
			case SAVE:
				chooseDirectory(tfSaveDir, Command.SAVE);
				break;
			default:
				break;
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
	 * 
	 * @param tf
	 * @param defaultDir
	 * @return
	 */
	private boolean checkDir(JTextField tf, String defaultDir) {
		if (tf != null) {
			File f = new File(tf.getText());
			if (f.exists() && f.isDirectory()) {
				return true;
			}
			JOptionPane.showMessageDialog(getTopLevelAncestor(), new JLabel(
					GUITools.toHTML(String.format("No such directory %s.", f
							.getPath()), 40)), "Warning",
					JOptionPane.WARNING_MESSAGE);
			tf.setText(defaultDir);
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean checkOpenDir() {
		return checkDir(tfOpenDir, defaultOpenDir);
	}

	/**
	 * 
	 * @return
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

}
