package de.zbit.gui.cfg;

import java.io.File;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * This class checks any type of {@link JTextComponent} whether its text
 * represents a valid file or directory. In all other cases it returns false.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 */
public class FileInputVerifier extends InputVerifier {
	
	/**
	 * This enum helps to distinguish between files and directories.
	 * 
	 * @author draeger
	 * @date 2010-11-04
	 */
	public enum FileType {
		/**
		 * Identifier for a valid directory.
		 */
		DIRECTORY,
		/**
		 * Identifier for a valid file.
		 */
		FILE;
	}
	
	/**
	 * Whether to check for files or directories.
	 */
	private FileType mode;
	
	/**
	 * This creates a {@link FileInputVerifier} that allows for both, files and
	 * directories.
	 */
	public FileInputVerifier() {
		mode = null;
	}
	
	/**
	 * The {@link Type} argument allows to select whether to allow for files or
	 * directories.
	 * 
	 * @param mode
	 */
	public FileInputVerifier(FileType mode) {
		this.mode = mode;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
	 */
	@Override
	public boolean verify(JComponent input) {
		if (input instanceof JTextComponent) {
			String text = ((JTextComponent) input).getText();
			File file = new File(text);
			if (((mode == null) && (file.isFile() || file.isDirectory()))
					|| (file.isFile() && (mode == FileType.FILE))
					|| (file.isDirectory() && (mode == FileType.DIRECTORY))) {
				return true; }
		}
		return false;
	}
}
