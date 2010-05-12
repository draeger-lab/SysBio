/**
 * 
 */
package de.zbit.kegg.gui;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.gui.SBMLModelSplitPane;

import de.zbit.kegg.KeggInfoManagement;
import de.zbit.kegg.io.KEGG2jSBML;

/**
 * @author draeger
 * 
 */
public class ConverterUI extends JDialog {

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
		// Z:/workspace/SysBio/resources/de/zbit/kegg/samplefiles/hsa00010.xml
		super();
		setTitle("KGML2SBMLconverter");
		JFileChooser chooser = GUITools.createJFileChooser("usr.dir", false,
				false, JFileChooser.FILES_ONLY, new FileFilterKGML());
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			showGUI(chooser.getSelectedFile().getAbsolutePath());
		} else {
			dispose();
		}
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
		SBMLDocument doc = k2s.Kegg2jSBML(absolutePath);

		// Remember already queried objects
		KeggInfoManagement.saveToFilesystem("keggdb.dat", k2s
				.getKeggInfoManager());
		// --

		getContentPane().add(
				new SBMLModelSplitPane(doc.getModel(), SBMLsqueezer
						.getDefaultSettings()));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setTitle(getTitle() + " " + doc.getModel().getId());
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
