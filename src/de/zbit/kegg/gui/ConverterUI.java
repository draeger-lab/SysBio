/**
 * 
 */
package de.zbit.kegg.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.sbml.jsbml.SBMLDocument;

import de.zbit.gui.GUITools;
import de.zbit.kegg.KeggInfoManagement;
import de.zbit.kegg.io.FileFilterKGML;
import de.zbit.kegg.io.KEGG2jSBML;

/**
 * @author draeger
 * 
 */
public class ConverterUI extends JDialog implements ActionListener {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is a enumeration of all possible commands this
	 * {@link ActionListener} can process.
	 * 
	 * @author draeger
	 * 
	 */
	public static enum Command {
		/**
		 * Command to open a file.
		 */
		OPEN_FILE
	}

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -3833481758555783529L;

	/**
	 * Speedup Kegg2SBML by loading already queried objects. Reduces network
	 * load and heavily reduces computation time.
	 */
	private static KEGG2jSBML k2s;

	static {
		if (new File("keggdb.dat").exists()
				&& new File("keggdb.dat").length() > 0) {
			KeggInfoManagement manager = (KeggInfoManagement) KeggInfoManagement
					.loadFromFilesystem("keggdb.dat");
			k2s = new KEGG2jSBML(manager);
		} else {
			k2s = new KEGG2jSBML();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			String infile = null, outfile = null;
			for (String arg : args) {
				if (arg.startsWith("--input=")) {
					infile = arg.split("=")[1];
				} else if (arg.startsWith("--output=")) {
					outfile = arg.split("=")[1];
				}
			}
			if (infile != null) {
				new ConverterUI(infile);
			}
			if ((infile != null) && (outfile != null)) {
				k2s.Convert(infile, outfile);
			}
		} else {
			new ConverterUI();
		}
	}

	/**
	 * Shows a small GUI.
	 */
	public ConverterUI() {
		this(System.getProperty("user.dir"));
	}

	/**
	 * 
	 * @param string
	 */
	public ConverterUI(String string) {
		super();
		setTitle("KGML2SBMLconverter");
		showGUI(string);
	}

	/**
	 * 
	 * @param absolutePath
	 */
	private void showGUI(String absolutePath) {
		// Convert Kegg File to SBML document.
		File f = new File(absolutePath);
		if (f.isDirectory()) {
			// Z:/workspace/SysBio/resources/de/zbit/kegg/samplefiles/hsa00010.xml
			setTitle("KGML2SBMLconverter");
			JFileChooser chooser = GUITools
					.createJFileChooser(absolutePath, false, false,
							JFileChooser.FILES_ONLY, new FileFilterKGML());
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			} else {
				dispose();
				System.exit(0);
			}
		}
		if (f.exists() && f.isFile() && f.canRead()) {
			absolutePath = f.getAbsolutePath();
			SBMLDocument doc = k2s.Kegg2jSBML(absolutePath);

			// Remember already queried objects
			if (k2s.getKeggInfoManager().hasChanged()) {
				KeggInfoManagement.saveToFilesystem("keggdb.dat", k2s
						.getKeggInfoManager());
			}
			// --

			getContentPane().add(new SBMLModelSplitPane(doc.getModel()));
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			pack();
			setTitle(getTitle() + " " + doc.getModel().getId());
			setLocationRelativeTo(null);
			setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Cannot read input file",
					"Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
