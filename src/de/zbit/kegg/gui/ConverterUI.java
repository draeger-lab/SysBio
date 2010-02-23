/**
 * 
 */
package de.zbit.kegg.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.gui.SBMLModelSplitPane;

import de.zbit.kegg.KeggInfoManagement;
import de.zbit.kegg.io.KEGG2jSBML;
import de.zbit.util.InfoManagement;

/**
 * @author draeger
 * 
 */
public class ConverterUI extends JFrame {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -3833481758555783529L;

	/**
	 * Shows a small GUI.
	 */
	public ConverterUI() {
		// Z:/workspace/SysBio/src/de/zbit/kegg/samplefiles/hsa00010.xml
		super("KGML2SBMLconverter");
		JFileChooser chooser = GUITools.createJFileChooser("usr.dir", false,
				false, JFileChooser.FILES_ONLY, new FileFilterKGML());
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			// Speedup Kegg2SBML by loading alredy queried objects. Reduces
			// network load and heavily reduces computation time.
			KEGG2jSBML k2s;
			if (new File("keggdb.dat").exists()
					&& new File("keggdb.dat").length() > 0) {
				KeggInfoManagement manager = (KeggInfoManagement) KeggInfoManagement
						.loadFromFilesystem("keggdb.dat");
				k2s = new KEGG2jSBML(manager);
			} else {
				k2s = new KEGG2jSBML();
			}
			// ---

			// Convert Kegg File to SBML document.
			SBMLDocument doc = k2s.Kegg2jSBML(chooser.getSelectedFile()
					.getAbsolutePath());

			// Remember already queried objects
			KeggInfoManagement.saveToFilesystem("keggdb.dat", k2s
					.getKeggInfoManager());
			// --

			getContentPane().add(
					new SBMLModelSplitPane(doc.getModel(), SBMLsqueezer
							.getDefaultSettings()));
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			pack();
			setVisible(true);
		} else
			dispose();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ConverterUI();
	}

}
