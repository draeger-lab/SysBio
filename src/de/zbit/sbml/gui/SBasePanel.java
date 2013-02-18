/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.History;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SBaseWithDerivedUnit;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.util.compilers.HTMLFormula;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;

import de.zbit.gui.GUITools;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.gui.table.renderer.ColoredBooleanRenderer;
import de.zbit.sbml.io.SBOTermFormatter;
import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * A specialized {@link JPanel} that displays all available properties of a
 * given {@link SBase} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @author Sebastian Nagel
 * @since 1.0 (originates from SBMLsqueezer 1.3)
 * @version $Rev$
 */
@SuppressWarnings("deprecation")
public class SBasePanel extends JPanel implements EquationComponent {
	
  /**
   * A {@link Logger} for this class.
   */
  public static final transient Logger logger = Logger.getLogger(SBasePanel.class.getName());
  
	/**
	 * Localization for SBML element names.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.sbml.locales.ElementNames");
	/**
	 * 
	 */
	private static final ResourceBundle bundleWarnings = ResourceManager.getBundle("de.zbit.sbml.locales.Warnings");
	
  /**
   * 
   */
	private static final int preferedWidth = 450;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -4969096536922920641L;

	private boolean editable;

	private LaTeXCompiler latex;

	private final LayoutHelper lh;

	private boolean namesIfAvailable;
	
	private EquationRenderer renderer;
	
	private int row;

	/**
   * 
   * @param sbase
   */
  public SBasePanel(SBase sbase) {
  	this(sbase, true);
  }
  
  /**
   * 
   * @param sbase
   * @param namesIfAvailable
   */
  public SBasePanel(SBase sbase, boolean namesIfAvailable) {
  	this(sbase, namesIfAvailable, null);
  }

  /**
   * 
   * @param sbase
   * @param namesIfAvailable
   */
  public SBasePanel(SBase sbase, boolean namesIfAvailable, EquationRenderer renderer) {
  	super(true);
  	this.namesIfAvailable = namesIfAvailable;
  	this.renderer = renderer;
  	GridBagLayout gbl = new GridBagLayout();
  	setLayout(gbl);
  	latex = new LaTeXCompiler(namesIfAvailable);
  	lh = new LayoutHelper(this, gbl);
  	editable = false;
  	row = -1;
  	if (sbase != null) {
  		setBorder(BorderFactory.createTitledBorder(' ' + createPanelTitle(sbase) + ' '));
  		lh.add(createJPanel(), 0, ++row, 5, 1, 0d, 0d);
  		if (sbase instanceof NamedSBase) {
  			addProperties((NamedSBase) sbase);
  		}
  		addProperties(sbase);
  		if (sbase instanceof SimpleSpeciesReference) {
  			addProperties((SimpleSpeciesReference) sbase);
  		}
  		if (sbase instanceof MathContainer) {
  			addProperties((MathContainer) sbase);
  		}
  		if (sbase instanceof ListOf<?>) {
  			addProperties((ListOf<?>) sbase);
  			// ListOf<?> list = (ListOf<?>) sbase;
  			// for (SBase s : list) {
  			// lh.add(new SBasePanel(s, settings));
  			// }
  		} else if (sbase instanceof Model) {
  			addProperties((Model) sbase);
  		} else if (sbase instanceof UnitDefinition) {
  			addProperties((UnitDefinition) sbase);
  		} else if (sbase instanceof Unit) {
  			addProperties((Unit) sbase);
  		} else if (sbase instanceof Compartment) {
  			addProperties((Compartment) sbase);
  		} else if (sbase instanceof Species) {
  			addProperties((Species) sbase);
  		} else if (sbase instanceof Parameter) {
  			addProperties((Parameter) sbase);
  		/* This is not necessary because on other information than in
  		 * QuantityWithUnit (see below) is added; so we would have a duplication
  		 * of information:
  		 * } else if (sbase instanceof LocalParameter) {
  			addProperties((LocalParameter) sbase);
  		 */
  		} else if (sbase instanceof Constraint) {
  			addProperties((Constraint) sbase);
  		} else if (sbase instanceof Reaction) {
  			try {
  				addProperties((Reaction) sbase);
  			} catch (XMLStreamException exc) {
  				exc.printStackTrace();
  				//added
  				GUITools.showErrorMessage(this, exc);
  			}
  		} else if (sbase instanceof Event) {
  			addProperties((Event) sbase);
  		}
  		if (sbase instanceof QuantityWithUnit) {
  			addProperties((QuantityWithUnit) sbase);
  		} else if ((sbase instanceof SBaseWithDerivedUnit) && !(sbase instanceof Reaction)) {
  			// We exclude reactions because the information would be displayed twice in case that a kinetic law is set.
  			addProperties((SBaseWithDerivedUnit) sbase);
  		}
  		if (sbase instanceof Variable) {
  			addProperties((Variable) sbase);
  		}
  	}
  	GUITools.setOpaqueForAllElements(this, true);
  }
  
  /**
   * 
   * @return
   */
  private JPanel createJPanel() {
		return createJPanel(new FlowLayout());
	}

  /**
   * 
   * @param layout
   * @return
   */
	private JPanel createJPanel(LayoutManager layout) {
		JPanel p = new JPanel(layout, true);
		p.setOpaque(true);
		return p;
	}

	/**
   * 
   * @param sbase
   * @return
   */
  public String createPanelTitle(SBase sbase) {
		String elementName = sbase.getElementName();
		if (bundle.containsKey(elementName)) {
			elementName = bundle.getString(elementName);
		} else if (elementName.equals(ListOf.Type.other.toString())) {
			elementName = sbase.getClass().getSimpleName();
		}
		return elementName;
	}

	/**
	 * @param c
	 */
	private void addProperties(Compartment c) {
		if (c.isSetCompartmentType() || editable) {
			JTextField tf = new JTextField(c.getCompartmentTypeInstance().toString());
			tf.setEditable(editable);
			addLabeledComponent(bundle.getString("compartmentType"), tf);
		}
		if (c.isSetOutside() || editable) {
			JTextField tf = new JTextField(c.getOutsideInstance().toString());
			tf.setEditable(editable);
			addLabeledComponent(bundle.getString("outside"), tf);
		}
		if (c.isSetSpatialDimensions()) {
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(c.getSpatialDimensions(), 0, 3, 1));
			spinner.setEnabled(editable);
			addLabeledComponent(bundle.getString("spatialDimensions"), spinner);
		}
		addProperties((Symbol) c);
	}
  
  /**
	 * @param c
	 */
	private void addProperties(Constraint c) {
		if (c.isSetMessage() || editable) {
			JTextField tf = new JTextField(c.getMessageString());
			tf.setEditable(editable);
			addLabeledComponent(bundle.getString("message"), tf);
		}
	}

  /**
	 * @param e
	 */
	private void addProperties(Event e) {
		JCheckBox check = new JCheckBox(bundle.getString("useValuesFromTriggerTime"), e.getUseValuesFromTriggerTime());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		if (e.isSetTrigger()) {
			lh.add(new SBasePanel(e.getTrigger(), namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		if (e.isSetDelay()) {
			lh.add(new SBasePanel(e.getDelay(), namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		if (e.isSetTimeUnits()) {
			lh.add(new SBasePanel(e.getTimeUnitsInstance(), namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		for (EventAssignment ea : e.getListOfEventAssignments()) {
			lh.add(new SBasePanel(ea, namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
	}

	/**
	 * @param list
	 */
	private void addProperties(ListOf<? extends SBase> list) {
		JList l = new JList(list.toArray(new SBase[] {}));
		l.setCellRenderer(new SBMLlistCellRenderer());
		l.setBorder(BorderFactory.createLoweredBevelBorder());
		lh.add(new JScrollPane(l), 1, ++row, 3, 1, 1d, 1d);
	}

	/**
	 * @param mc
	 */
	private void addProperties(MathContainer mc) {
		if (mc.isSetMath()) {
			if (isRendererAvailable()) {
				StringBuffer laTeXpreview = new StringBuffer();
				laTeXpreview.append(LaTeXCompiler.eqBegin);
				if (mc instanceof KineticLaw) {
					KineticLaw k = (KineticLaw) mc;
					laTeXpreview.append("v_");
					laTeXpreview.append(latex.mbox(k.getParentSBMLObject().getId()));
					laTeXpreview.append('=');
				} else if (mc instanceof FunctionDefinition) {
					FunctionDefinition f = (FunctionDefinition) mc;
					laTeXpreview.append(latex.mbox(f.getId()));
				} else if (mc instanceof Assignment) {
					Assignment ea = (Assignment) mc;
					laTeXpreview.append(latex.mbox(ea.getVariable()));
					laTeXpreview.append('=');
				}
				try {
					laTeXpreview.append(mc.getMath().compile(latex).toString()
						.replace("mathrm", "mbox").replace("text", "mbox")
						.replace("mathtt", "mbox"));
				} catch (Throwable e) {
					logger.log(Level.WARNING, bundleWarnings.getString("COULD_NOT_CREATE_LATEX_FROM_ASTNODE"), e);
					laTeXpreview.append(bundleWarnings.getString("invalid"));
					//e.printStackTrace();
				}
				laTeXpreview.append(LaTeXCompiler.eqEnd);
				lh.add(renderer.renderEquation(laTeXpreview.toString()), 1, ++row, 3, 1, 1d, 0d);
				lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
			}
			if (mc instanceof Assignment) {
				lh.add(new SBasePanel(((Assignment) mc)
						.getVariableInstance(), namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
			}
		}
	}
	
	/**
	 * 
	 * @param label
	 * @param component
	 */
	private void addLabeledComponent(Object label, Component component) {
		Component jlabel = null;
		if (label instanceof String) {
			jlabel = new JLabel(label.toString().endsWith(": ") ? label.toString() : label + ": ");
		} else if (label instanceof Component) {
			jlabel = (Component) label;
		}
		lh.add(jlabel, 1, ++row, 1, 1, 0d, 0d);
		lh.add(component, 3, row, 1, 1, 1, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
	}

	/**
	 * @param model
	 */
	private void addProperties(Model model) {
		if (model.getLevel() > 2) {
			if (model.isSetConversionFactor()) {
				Parameter conversionFactor = model.getConversionFactorInstance();
				JTextField tf = createNameField(conversionFactor);
				addLabeledComponent(bundle.getString("conversionFactor"), tf);
			}
			if (model.isSetAreaUnits()) {
				addUnit(bundle.getString("areaUnits"), model.getAreaUnits(), model);
			}
			if (model.isSetLengthUnits()) {
				addUnit(bundle.getString("lengthUnits"), model.getLengthUnits(), model);
			}
			if (model.isSetExtentUnits()) {
				addUnit(bundle.getString("extentUnits"), model.getExtentUnits(), model);
			}
			if (model.isSetSubstanceUnits()) {
				addUnit(bundle.getString("substanceUnits"), model.getSubstanceUnits(), model);
			}
			if (model.isSetTimeUnits()) {
				addUnit(bundle.getString("timeUnits"), model.getTimeUnits(), model);
			}
			if (model.isSetVolumeUnits()) {
				addUnit(bundle.getString("volumeUnits"), model.getVolumeUnits(), model);
			}
		}
		String columnNames[] = new String[] { bundle.getString("element"), bundle.getString("quantity") };
		String rowData[][] = new String[][] {
				{ bundle.getString("listOfFunctionDefinitions"),
						Integer.toString(model.getFunctionDefinitionCount()) },
				{ bundle.getString("listOfUnitDefinitions"),
						Integer.toString(model.getUnitDefinitionCount()) },
				{ bundle.getString("listOfCompartmentTypes"),
						Integer.toString(model.getCompartmentTypeCount()) },
				{ bundle.getString("listOfSpeciesTypes"), Integer.toString(model.getSpeciesTypeCount()) },
				{ bundle.getString("listOfCompartments"), Integer.toString(model.getCompartmentCount()) },
				{ bundle.getString("listOfSpecies"), Integer.toString(model.getSpeciesCount()) },
				{ bundle.getString("listOfParameters"), Integer.toString(model.getParameterCount()) },
				{ bundle.getString("listOfLocalParameters"),
						Integer.toString(model.getLocalParameterCount()) },
				{ bundle.getString("listOfInitialAssignments"),
						Integer.toString(model.getInitialAssignmentCount()) },
				{ bundle.getString("listOfRules"), Integer.toString(model.getRuleCount()) },
				{ bundle.getString("listOfConstraints"), Integer.toString(model.getConstraintCount()) },
				{ bundle.getString("listOfReactions"), Integer.toString(model.getReactionCount()) },
				{ bundle.getString("listOfEvents"), Integer.toString(model.getEventCount()) } };
		JTable table = new JTable(rowData, columnNames);
		table.setEnabled(editable);
		table.setPreferredScrollableViewportSize(new Dimension(200, table
				.getRowCount()
				* table.getRowHeight()));
		for (int i = 0; i < table.getModel().getColumnCount(); i++) {
			table.setDefaultRenderer(table.getModel().getColumnClass(i), new ColoredBooleanRenderer());
		}
		JScrollPane scroll = new JScrollPane(table);
		Dimension dim = table.getPreferredScrollableViewportSize();
		scroll.setPreferredSize(new Dimension((int) dim.getWidth() + 10,
				(int) dim.getHeight() + 18));
		lh.add(scroll, 1, ++row, 3, 1, 1d, 1d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
	}

	/**
	 * 
	 * @param nsb
	 * @return
	 */
	private JTextField createNameField(NamedSBase nsb) {
		JTextField tf = new JTextField(nsb.isSetName() ? nsb.getName() : nsb.getId());
		tf.setEditable(editable);
		tf.setColumns(25);
		tf.setCaretPosition(0);
		return tf;
	}

	/**
	 * 
	 * @param label
	 * @param units
	 * @param m
	 */
	private void addUnit(String label, String units, Model m) {
		int level = m.getLevel(), version = m.getVersion();
		if (Unit.Kind.isValidUnitKindString(units, level, version)) {
			addLabeledComponent(label, unitKindComboBox(Unit.Kind.valueOf(units.toUpperCase())));
		} else {
			addLabeledComponent(label, unitPreview(m.getUnitDefinition(units)));
		}
	}

	/**
	 * @param sbase
	 */
	private void addProperties(ModifierSpeciesReference msr) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param nsb
	 */
	private void addProperties(NamedSBase nsb) {
		if (nsb.isSetName() || nsb.isSetId() || editable) {
			JTextField tf = createNameField(nsb);
			addLabeledComponent(bundle.getString("name"), tf);
		}
	}

	/**
	 * @param sbase
	 */
	private void addProperties(Parameter p) {
		addProperties((Symbol) p);
	}

	/**
	 * 
	 * @param q
	 */
	private void addProperties(QuantityWithUnit q) {
		addLabeledComponent(q instanceof Species ? bundle.getString("substanceUnit")	: bundle.getString("unit"), unitPreview(q.getUnitsInstance()));
	}

	/**
	 * @param sbase
	 * @throws XMLStreamException
	 */
	private void addProperties(Reaction reaction) throws XMLStreamException {
		JCheckBox check = new JCheckBox(bundle.getString("reversible"), reaction.getReversible());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		check = new JCheckBox(bundle.getString("fast"), reaction.getFast());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);

		// Create Table of reactants, modifiers and products
		String rmp[][] = new String[Math.max(reaction.getReactantCount(), Math
				.max(reaction.getModifierCount(), reaction.getProductCount()))][3];
		String colNames[] = new String[] { bundle.getString("listOfReactants"), bundle.getString("listOfModifiers"), bundle.getString("listOfProducts") };
		int count;
		if (reaction.isSetListOfReactants()) {
			count = 0;
			for (SpeciesReference specRef : reaction.getListOfReactants()) {
				if (specRef.isSetSpeciesInstance()) {
					rmp[count++][0] = specRef.getSpeciesInstance().toString();
				}
			}
		}
		if (reaction.isSetListOfModifiers()) {
			count = 0;
			for (ModifierSpeciesReference mSpecRef : reaction.getListOfModifiers()) {
				if (mSpecRef.isSetSpeciesInstance()) {
					rmp[count++][1] = mSpecRef.getSpeciesInstance().toString();
				}
			}
		}
		if (reaction.isSetListOfProducts()) {
			count = 0;
			for (SpeciesReference specRef : reaction.getListOfProducts()) {
				if (specRef.isSetSpeciesInstance()) {
					rmp[count++][2] = specRef.getSpeciesInstance().toString();
				}
			}
		}
		JTable table = new JTable(rmp, colNames);
		table.setPreferredScrollableViewportSize(new Dimension(200, (table
				.getRowCount() + 1)
				* table.getRowHeight()));
		table.setEnabled(editable);
		for (int i = 0; i < table.getModel().getColumnCount(); i++) {
			table.setDefaultRenderer(table.getModel().getColumnClass(i), new ColoredBooleanRenderer());
		}
		JScrollPane scroll = new JScrollPane(table);
		Dimension dim = table.getPreferredScrollableViewportSize();
		scroll.setPreferredSize(new Dimension((int) dim.getWidth() + 10,
				(int) dim.getHeight() + 18));
		lh.add(scroll, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		JPanel rEqPanel = createJPanel(new BorderLayout());
		ReactionPanel reactionPanel = new ReactionPanel(reaction, namesIfAvailable);
		reactionPanel.setBackground(Color.WHITE);
		JScrollPane s = new JScrollPane(reactionPanel);
		s.setBorder(BorderFactory.createLoweredBevelBorder());
		s.setPreferredSize(new Dimension(preferedWidth, 50));
		rEqPanel.add(s, BorderLayout.CENTER);
		rEqPanel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("reactionEquation") + ' '));
		lh.add(rEqPanel, 1, ++row, 3, 1, 1d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		if (reaction.isSetKineticLaw()) {
			lh.add(new SBasePanel(reaction.getKineticLaw(), namesIfAvailable, this.renderer), 1, ++row, 3, 1, 0d, 0d);
		}
	}
	
	/**
	 * @param sbase
	 */
	private void addProperties(SBase sbase) {
		lh.add(createJPanel(), 0, row, 1, 1, 0d, 0d);
		lh.add(createJPanel(), 4, row, 1, 1, 0d, 0d);
		lh.add(createJPanel(), 2, row, 1, 1, 0d, 0d);
		if (sbase.isSetHistory()) {
			History hist = sbase.getHistory();
			lh.add(new JLabel(bundle.getString("listOfCreators")), 1, ++row, 1, 1, 0d, 0d);
			String columnNames[] = new String[] { bundle.getString("givenName"), bundle.getString("familyName"),
				bundle.getString("eMail"), bundle.getString("organization") };
			String rowData[][] = new String[hist.getCreatorCount()][4];
			int i = 0;
			for (Creator mc : hist.getListOfCreators()) {
				rowData[i][0] = mc.getGivenName();
				rowData[i][1] = mc.getFamilyName();
				rowData[i][2] = mc.getEmail();
				rowData[i][3] = mc.getOrganization();
				i++;
			}
			JTable table = new JTable(rowData, columnNames);
			table.setEnabled(editable);
			table.setPreferredScrollableViewportSize(new Dimension(200, (table
					.getRowCount() + 1)
					* table.getRowHeight()));
			for (int j = 0; j < table.getModel().getColumnCount(); j++) {
				table.setDefaultRenderer(table.getModel().getColumnClass(j), new ColoredBooleanRenderer());
			}
			JScrollPane scroll = new JScrollPane(table);
			Dimension dim = table.getPreferredScrollableViewportSize();
			scroll.setPreferredSize(new Dimension((int) dim.getWidth() + 10,
					(int) dim.getHeight() + 18));
			lh.add(scroll, 1, ++row, 3, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
			if (hist.isSetCreatedDate()) {
				JTextField tf = new JTextField(hist.getCreatedDate().toString());
				tf.setEditable(editable);
				addLabeledComponent(bundle.getString("dateOfCreation"), tf);
			}
			Vector<Date> modification = new Vector<Date>();
			if (hist.isSetModifiedDate()) {
				modification.add(hist.getModifiedDate());
			}
			for (i = 0; i < hist.getModifiedDateCount(); i++) {
				if (!modification.contains(hist.getModifiedDate(i))) {
					modification.add(hist.getModifiedDate(i));
				}
			}
			if (modification.size() > 0) {
				JList l = new JList(modification);
				l.setEnabled(editable);
				JScrollPane scroll2 = new JScrollPane(l);
				scroll2.setPreferredSize(new Dimension(preferedWidth,
						modification.size() * 20));
				scroll2.setBorder(BorderFactory.createLoweredBevelBorder());
				addLabeledComponent(bundle.getString("dateOfModification"), scroll2);
			}
		}
		if (sbase.isSetNotes() || editable) {
			JEditorPane notesArea = new JEditorPane("text/html", SBMLtools.createHTMLfromNotes(sbase));
			notesArea.setDoubleBuffered(true);
			notesArea.setEditable(editable);
			notesArea.addHyperlinkListener(new SystemBrowser());
//			notesArea.setMaximumSize(new Dimension(preferedWidth, 200));
			notesArea.setDoubleBuffered(true);
			notesArea.setAutoscrolls(true);
			notesArea.setPreferredSize(new Dimension(preferedWidth, 200));
			JScrollPane editorScrollPane = new JScrollPane(notesArea);
			editorScrollPane.setViewportBorder(BorderFactory.createLoweredBevelBorder());
			//scroll.setMaximumSize(notesArea.getMaximumSize());
			// We NEED to set a PreferredSize on the scroll. Else, Long description strings
			// are printed on one large line without a line break!
			// Setting a maximum size has (unfortunately) no influence on this behavior
//			scroll.setPreferredSize(new Dimension(preferedWidth, 500));
			editorScrollPane.setPreferredSize(new Dimension(250, 145));
      editorScrollPane.setMinimumSize(new Dimension(10, 10));
			JPanel notesPanel = createJPanel(new BorderLayout());
			notesPanel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("notes") + ' '));
			notesPanel.setOpaque(true);
			notesPanel.add(editorScrollPane, BorderLayout.CENTER);
			lh.add(notesPanel, 1, ++row, 3, 1, 1d, 0d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		if (sbase.getCVTermCount() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			if (sbase.getCVTermCount() > 1) {
				sb.append("<ul>");
			}
			for (CVTerm cvt : sbase.getCVTerms()) {
				if (sbase.getCVTermCount() > 1) {
					sb.append("<li>");
				}
				String cvtString = cvt.toString();
				LinkedList<String> replacedURIs = new LinkedList<String>();
				for (int k = 0; k < cvt.getResourceCount(); k++) {
					String uri = cvt.getResourceURI(k);
					if (!replacedURIs.contains(uri)) {
					  replacedURIs.add(uri);
				    String url = null;
				    // XXX: NOTE: startsWith is CASE-Sensitive! => "URN*" will lead
				    // to wrong urls.
				    if (!uri.startsWith("urn")) {
				      url = uri;
				    } else {
				      /* Please node for "replace(':', '/')":
				       * according to the official MIRIAM documentation,
				       * ':' in identifiers must be replaced by "%3A". So if you think
				       * here is a problem with replacing all ':', you should rather
				       * replace ':' in your ids by "%3A" in your code.
				       */
				      url = "http://identifiers.org/" + uri.substring(11).replace(':', '/');
				    }
					  if (url != null) {
					    // The old code here was wrong!
					    cvtString = cvtString.replace(uri,
					      "<a href=\""+url+"\">"+uri.replace("%3A", ":")+"</a>\n");
					  }
					}
				}
				sb.append(cvtString);
				if (sbase.getCVTermCount() > 1) {
					sb.append("</li>");
				}
			}
			if (sbase.getCVTermCount() > 1) {
				sb.append("</ul>");
			}
			sb.append("</body></html>");
			JEditorPane l = new JEditorPane("text/html", sb.toString());
			l.setAutoscrolls(true);
			l.addHyperlinkListener(new SystemBrowser());
			l.setDoubleBuffered(true);
			l.setEditable(editable);
			l.setBackground(Color.WHITE);
			Dimension dim = new Dimension(preferedWidth, 125);
			l.setMaximumSize(dim);
			JScrollPane editorScrollPane = new JScrollPane(l);
			editorScrollPane.setPreferredSize(new Dimension(250, 145));
      editorScrollPane.setMinimumSize(new Dimension(10, 10));
      editorScrollPane.setViewportBorder(BorderFactory.createLoweredBevelBorder());
			JPanel miriamPanel = createJPanel(new BorderLayout());
			miriamPanel.setOpaque(true);
			miriamPanel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("MIRIAM") + ' '));
			miriamPanel.add(editorScrollPane, BorderLayout.CENTER);
			lh.add(miriamPanel, 1, ++row, 3, 1, 1d, 0d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		if (sbase.isSetSBOTerm()) {
			JPanel sboPanel = createJPanel();
			sboPanel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("SBO") + ' '));
			LayoutHelper helper = new LayoutHelper(sboPanel);
			
			int columns = 35, innerRow = -1;
			SBO.Term term = SBO.getTerm(sbase.getSBOTerm());
			helper.add(new JLabel(bundle.getString("name")), 0, ++innerRow, 1, 1, 0d, 0d);
			JTextArea nameField = new JTextArea(term.getName(), 2, columns);
			nameField.setEditable(editable);
			nameField.setCaretPosition(0);
			nameField.setLineWrap(true);
			nameField.setWrapStyleWord(true);
			helper.add(new JScrollPane(nameField), 3, innerRow, 1, 1, 1d, 0d);
			helper.add(createJPanel(), 1, innerRow, 1, 1, .1d, 0d);
			helper.add(createJPanel(), 0, ++innerRow, 6, 1, 1d, 0d);
			
			helper.add(new JLabel(bundle.getString("definition")), 0, ++innerRow, 1, 1, 0d, 0d);
			JTextArea sboTermField = new JTextArea(5, columns);
			sboTermField.setCaretPosition(0);
			sboTermField.setLineWrap(true);
			sboTermField.setWrapStyleWord(true);
			sboTermField.setEditable(editable);
			try {
				sboTermField.setText(SBOTermFormatter.getShortDefinition(term));
			} catch (Exception exc) {
				// NoSuchElementException if ontology file is outdated
				logger.log(Level.WARNING, bundleWarnings.getString("COULD_NOT_GET_SBO_IDENTIFIER"), exc);
			}
			helper.add(new JScrollPane(sboTermField), 3, innerRow, 1, 1, 1d, 0d);
			helper.add(createJPanel(), 1, ++innerRow, 5, 1, 0d, 0d);
			
			lh.add(helper.getContainer(), 1, ++row, 3, 1, 0d, 0d);
		}
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(SBaseWithDerivedUnit sbase) {
		JEditorPane pane = unitPreview(sbase.getDerivedUnitDefinition());
		pane.setBorder(BorderFactory.createLoweredBevelBorder());
		addLabeledComponent(bundle.getString("derivedUnit"), pane);
		JCheckBox chck = new JCheckBox(bundle.getString("containsUndeclaredUnits"), sbase.containsUndeclaredUnits());
		chck.setEnabled(false);
		lh.add(chck, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 0, ++row, 1, 1, 0d, 0d);
	}

	/**
	 * @param ssr
	 */
	private void addProperties(SimpleSpeciesReference ssr) {
		if (ssr.isSetSpecies()) {
			Model m = ssr.getModel();
			String idsOrNames[] = new String[m.getSpeciesCount()];
			int index = 0;
			for (int i = 0; i < m.getSpeciesCount(); i++) {
				Species s = m.getSpecies(i);
				idsOrNames[i] = s.isSetName() ? s.getName() : s.getId();
				if (s.getId().equals(ssr.getSpecies())) {
					index = i;
				}
			}
			JComboBox combo = new JComboBox(idsOrNames);
			combo.setSelectedIndex(index);
			combo.setEnabled(editable);
			lh.add(new JLabel(bundle.getString("species")), 1, ++row, 1, 1, 0d, 0d);
			lh.add(combo, 3, row, 1, 1, 1d, 0d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
		if (ssr instanceof SpeciesReference) {
			addProperties((SpeciesReference) ssr);
		} else if (ssr instanceof ModifierSpeciesReference) {
			addProperties((ModifierSpeciesReference) ssr);
		}
    if (ssr.isSetSpecies()) {
      lh.add(new SBasePanel(ssr.getSpeciesInstance(), namesIfAvailable, this.renderer), 1,
        ++row, 3, 1, 1d, 1d);
    }
	}
	
	/**
	 * @param sbase
	 */
	private void addProperties(Species species) {
		if (species.isSetSpeciesType()) {
			JTextField tf = new JTextField(species.getSpeciesTypeInstance().toString());
			tf.setEditable(editable);
			addLabeledComponent(bundle.getString("speciesType"), tf);
		}
		JTextField tf = new JTextField(species.getCompartmentInstance().toString());
		tf.setEditable(editable);
		addLabeledComponent(bundle.getString("compartment"), tf);
		if (species.isSetSpeciesType() || editable) {
			tf = new JTextField(species.getSpeciesTypeInstance().toString());
			tf.setEditable(editable);
			addLabeledComponent(bundle.getString("speciesType"), tf);
		}
		JSpinner spinCharge = new JSpinner(new SpinnerNumberModel(species.getCharge(), -10, 10, 1));
		spinCharge.setEnabled(editable);
		addLabeledComponent(bundle.getString("charge"), spinCharge);
		addProperties((Symbol) species);
		JCheckBox check = new JCheckBox(bundle.getString("boundaryCondition"), species.getBoundaryCondition());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		check = new JCheckBox(bundle.getString("hasOnlySubstanceUnits"), species.getHasOnlySubstanceUnits());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
	}

	/**
	 * @param sbase
	 */
	private void addProperties(SpeciesReference specRef) {
		if (specRef.isSetStoichiometryMath()) {
			StoichiometryMath sMath = specRef.getStoichiometryMath();
			JPanel p = createJPanel(new GridLayout(1, 1));
			p.setBorder(BorderFactory.createTitledBorder(' '
					+ sMath.getClass().getCanonicalName() + ' '));
			
			if (isRendererAvailable()) {
				String l;
				try {
					l = sMath.getMath().compile(latex).toString().replace("\\\\",
							"\\");
				} catch (SBMLException e) {
					l = bundleWarnings.getString("invalid");
				}
				JComponent eqn = renderer.renderEquation(l);
				eqn.setBorder(BorderFactory.createLoweredBevelBorder());
				p.add(eqn);
			}

			lh.add(p, 3, ++row, 1, 1, 1d, 1d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		} else {
			lh.add(new JLabel(bundle.getString("stoichiometry")), 1, ++row, 1, 1, 0d, 0d);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(specRef
					.getStoichiometry(), specRef.getStoichiometry() - 1000,
					specRef.getStoichiometry() + 1000, .1d));
			spinner.setEnabled(editable);
			lh.add(spinner, 3, row, 1, 1, 1d, 0d);
			lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
		}
	}

	/**
	 * @param s
	 */
	private void addProperties(Symbol s) {
		double val = Double.NaN;
		double min = 0d;
		double max = 9999.9;
		Object label = null;
		if (s instanceof Species) {
			Species species = (Species) s;
			String types[] = new String[] { bundle.getString("initialAmount"),
					bundle.getString("initialConcentration") };
			boolean amount = true;
			if (species.isSetInitialAmount()) {
				val = species.getInitialAmount();
			} else if (species.isSetInitialConcentration()) {
				val = species.getInitialConcentration();
				amount = false;
			}
			JComboBox type = new JComboBox(types);
			type.setSelectedIndex(amount ? 0 : 1);
			type.setEnabled(editable);
			label = type;
		} else {
			if (s instanceof Compartment) {
				Compartment c = (Compartment) s;
				if (c.isSetSize()) {
					val = c.getSize();
				}
				label = bundle.getString("size");
			} else {
				Parameter p = (Parameter) s;
				if (p.isSetValue()) {
					val = p.getValue();
				}
				label = bundle.getString("value");
			}
		}
		JSpinner spinValue = new JSpinner(new SpinnerNumberModel(val, Math.min(
				val, min), Math.max(val, max), .1d));
		spinValue.setEnabled(editable);
		addLabeledComponent(label, spinValue);
	}
	
	/**
	 * @param unit
	 */
	private void addProperties(Unit unit) {
		JComboBox unitSelection = unitKindComboBox(unit.getKind());
		unitSelection.setEditable(false);
		unitSelection.setEnabled(editable);
		addLabeledComponent(bundle.getString("kind"), unitSelection);
		double multiplier = unit.getMultiplier();
		JSpinner sMultiplier = GUITools.createJSpinner(new SpinnerNumberModel(
			multiplier, spinnerMinValue(multiplier), spinnerMaxValue(multiplier),
			spinnerStepSize(multiplier)), bundle.getString("multiplier"),
			bundle.getString("multiplier_tooltip"), editable);
		addLabeledComponent(bundle.getString("multiplier"), sMultiplier);
		if ((unit.getLevel() == 1)
				|| ((unit.getLevel() == 2) && (unit.getVersion() == 1))) {
			JSpinner sOffset = new JSpinner(new SpinnerNumberModel(unit
					.getOffset(), -1000, 1000, 1));
			sOffset.setEnabled(editable);
			addLabeledComponent(bundle.getString("offset"), sOffset);
		}
		JSpinner sScale = new JSpinner(new SpinnerNumberModel(unit.getScale(),
				-1000, 1000, 1));
		sScale.setEnabled(editable);
		addLabeledComponent(bundle.getString("scale"), sScale);
		double exponent = unit.getExponent();
		JSpinner sExponent = new JSpinner(new SpinnerNumberModel(exponent,
			spinnerMinValue(exponent), spinnerMaxValue(exponent),
			spinnerStepSize(exponent)));
		sExponent.setEnabled(editable);
		addLabeledComponent(bundle.getString("exponent"), sExponent);
	}

	/**
	 * 
	 * @param kind
	 * @return
	 */
	private JComboBox unitKindComboBox(Kind kind) {
		JComboBox unitSelection = new JComboBox();
		for (Unit.Kind unitKind : Unit.Kind.values()) {
			unitSelection.addItem(unitKind);
			if (unitKind.equals(kind)) {
				unitSelection.setSelectedItem(unitKind);
			}
		}
		return unitSelection;
	}

	/**
	 * @param ud
	 */
	private void addProperties(UnitDefinition ud) {
		addLabeledComponent(bundle.getString("definition"), unitPreview(ud));
		for (Unit u : ud.getListOfUnits()) {
			lh.add(new SBasePanel(u, namesIfAvailable, this.renderer), 1, ++row, 3, 1, 1d, 1d);
		}
	}

	/**
	 * 
	 * @param v
	 */
	private void addProperties(Variable v) {
		JCheckBox check = new JCheckBox(bundle.getString("constant"), v.isConstant());
		check.setEnabled(editable);
		lh.add(check, 1, ++row, 3, 1, 0d, 0d);
		lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#getRenderer()
	 */
	public EquationRenderer getEquationRenderer() {
		return renderer;
	}

	/**
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
   * @return the namesIfAvailable
   */
  public boolean isNamesIfAvailable() {
    return namesIfAvailable;
  }

	/**
   * @return isRendererAvailable
   */
  public boolean isRendererAvailable() {
    return this.renderer != null;
  }

	/**
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.EquationComponent#setRenderer(de.zbit.sbml.gui.EquationRenderer)
	 */
	public void setEquationRenderer(EquationRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * 
	 * @param multiplier
	 * @return
	 */
	public double spinnerMaxValue(double currVal) {
		return -spinnerMinValue(-currVal);
	}

	/**
	 * 
	 * @param multiplier
	 * @return
	 */
	public double spinnerMinValue(double currVal) {
		return Math.min(currVal, -1E6d);
	}

	/**
	 * 
	 * @param multiplier
	 * @return
	 */
	public double spinnerStepSize(double currVal) {
		return (spinnerMaxValue(currVal) - spinnerMinValue(currVal)) / 50d;
	}

	/**
	 * Creates a {@link JEditorPane} that displays the given
	 * {@link UnitDefinition} as a HTML.
	 * 
	 * @param ud
	 * @return
	 */
	private JEditorPane unitPreview(UnitDefinition ud) {
		JEditorPane preview = new JEditorPane("text/html", StringUtil
				.toHTML(ud != null ? HTMLFormula.toHTML(ud) : ""));
		preview.setEditable(editable);
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		return preview;
	}

}
