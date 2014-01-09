/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.gui;

import java.awt.Component;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;

import de.zbit.gui.layout.LayoutHelper;
import de.zbit.util.StringUtil;

/**
 * Creates a {@link JPanel} for an {@link ASTNode}.
 * This class is required for visualizing SBML documents.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @date 2010-05-11
 * @since 1.0
 * @version $Rev$
 */
public class ASTNodePanel extends JPanel implements EquationComponent {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -7683674821264614573L;
		private EquationRenderer renderer;
		
		/**
		 * @param node
		 * @param namesIfAvailable 
		 * @throws SBMLException
		 * @throws IOException
		 */
		public ASTNodePanel(ASTNode node, boolean namesIfAvailable) throws SBMLException, IOException {
			this(node, namesIfAvailable, true, null);
		}
		
		/**
		 * @param node
		 * @param namesIfAvailable 
		 * @param useJTextFiled
		 * @param renderer
		 * @throws SBMLException
		 * @throws IOException
		 */
		public ASTNodePanel(ASTNode node, boolean namesIfAvailable, boolean useJTextFiled, EquationRenderer renderer) throws SBMLException, IOException {
			super();
			this.renderer = renderer;
			LayoutHelper lh = new LayoutHelper(this);
			lh.add(new JPanel(), 0, 0, 1, 1, 0d, 0d);
			lh.add(createPanel(node,namesIfAvailable, useJTextFiled, renderer), 1, 0, 1, 1, 0d, 0d);
			lh.add(new JPanel(), 2, 0, 1, 1, 0d, 0d);
			
			setBorder(BorderFactory.createTitledBorder(String.format(" %s %s ",
				node.getClass().getSimpleName(), node.toString())));
		}

	/**
	 * @param node
	 * @param namesIfAvailable 
	 * @param useJTextFiled
	 * @param renderer
	 * @return
	 */
	private Component createPanel(ASTNode node, boolean namesIfAvailable, boolean useJTextFiled, EquationRenderer renderer) {
		LayoutHelper lh = new LayoutHelper(new JPanel());
		boolean enabled = false;
		JSpinner spinner;
		JTextField tf;
		String name;
	
		name = node.getParent() == null ? "undefined" : node.getParent().toString();
		tf = new JTextField(name);
		tf.setEditable(enabled);
		lh.add("Parent node", tf, true);
	
		if (node.getParentSBMLObject() == null) {
		    name = "undefined";
		} else {
		    MathContainer parent = node.getParentSBMLObject();
		    name = parent.getClass().getSimpleName();
		    name += " " + node.getParentSBMLObject().toString();
		}
		if (useJTextFiled) {
			tf = new JTextField(name);
			tf.setEditable(enabled);
			lh.add("Parent SBML object", tf, true);
		} else {
			JEditorPane editor = new JEditorPane("text/html", StringUtil.toHTML(name, 60));
			editor.setEditable(enabled);
			editor.setBorder(BorderFactory.createLoweredBevelBorder());
			lh.add("Parent SBML object", editor, true);
		}
	
		tf = new JTextField(Integer.toString(node.getChildCount()));
		tf.setEditable(false);
		lh.add("Number of children", tf, true);
	
		if (useJTextFiled) {
			tf = new JTextField(node.toFormula());
			tf.setEditable(false);
			lh.add("Formula", tf, true);
		} else {
			JTextArea area = new JTextArea();
			area.setColumns(60);
			try {
				area.setText(node.toFormula());
			} catch (SBMLException e) {
				area.setText("invalid");
			}
			area.setEditable(false);
			area.setBorder(BorderFactory.createLoweredBevelBorder());
			lh.add("Formula", area, true);
		}
	
		JComboBox opt = new JComboBox();
		for (ASTNode.Type t : ASTNode.Type.values()) {
			opt.addItem(t);
			if (t.equals(node.getType())) {
				opt.setSelectedItem(t);
			}
		}
		opt.setEditable(enabled);
		opt.setEnabled(enabled);
		lh.add("Type", opt, true);
	
		if (node.isRational()) {
			spinner = new JSpinner(new SpinnerNumberModel(node.getNumerator(),
				-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Numerator", spinner, true);
			spinner = new JSpinner(new SpinnerNumberModel(
				node.getDenominator(), -1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Denominator", spinner, true);
		}
	
		if (node.isReal()) {
			spinner = new JSpinner(new SpinnerNumberModel(node.getMantissa(),
				-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Mantissa", spinner, true);
			spinner = new JSpinner(new SpinnerNumberModel(node.getExponent(),
				-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Exponent", spinner, true);
		}
	
		if (node.isString()) {
			tf = new JTextField(node.getName());
			tf.setEditable(enabled);
			lh.add("Name", tf, true);
			if (node.getVariable() != null) {
				lh.add(new SBasePanel(node.getVariable(), namesIfAvailable, renderer), 0, lh.getRow() + 1,
					3, 1);
				lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1);
			}
		}
	
		JPanel unitPanel = new JPanel();
		LayoutHelper l = new LayoutHelper(unitPanel);
		JCheckBox chck = new JCheckBox("Contains undeclared units", node.containsUndeclaredUnits());
		chck.setEnabled(enabled);
		l.add(chck, 0, 0, 3, 1);
		UnitDefinition ud;
		try {
			ud = node.deriveUnit();
		} catch (Throwable e) {
			ud = new UnitDefinition();
		}
		JEditorPane unitPane = GUIToolsForSBML.unitPreview(ud != null ? ud : new UnitDefinition());
		unitPane.setBorder(BorderFactory.createLoweredBevelBorder());
		l.add(new JPanel(), 0, 1, 3, 1);
		l.add("Derived unit:", unitPane, true);
		unitPanel.setBorder(BorderFactory.createTitledBorder(" Derived units "));
		lh.add(unitPanel, 0, lh.getRow() + 1, 3, 1, 0d, 0d);
		lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1, 0d, 0d);
		
		if (renderer != null) {
			JComponent preview;
			
			StringBuilder latex = new StringBuilder();
			latex.append(LaTeXCompiler.eqBegin);
			String ltx;
			try {
				ltx = node.compile(new LaTeXCompiler()).toString().replace("mathrm",
						"mbox").replace("text", "mbox").replace("mathtt", "mbox");
			} catch (SBMLException e) {
				ltx = "invalid";
			}
			latex.append(ltx);
			latex.append(LaTeXCompiler.eqEnd);
			preview = renderer.renderEquation(latex.toString());
			preview.setBorder(BorderFactory.createLoweredBevelBorder());
			
			JScrollPane scroll = new JScrollPane(preview);
			scroll.setBorder(BorderFactory.createTitledBorder(" Preview "));
			lh.add(scroll, 0, lh.getRow() + 1, 3, 1);
			lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1, 0d, 0d);
		}
		
		return lh.getContainer();
		
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#getEquationRenderer()
	 */
	public EquationRenderer getEquationRenderer() {
		return renderer;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setEquationRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
	}

}
