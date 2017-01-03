/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
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
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.html.HTMLDocument;
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
import org.sbml.jsbml.SBMLDocument;
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
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCSpeciesPlugin;
import org.sbml.jsbml.ext.groups.Group;
import org.sbml.jsbml.ext.groups.Member;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ontology.Term;
import org.sbml.jsbml.util.compilers.HTMLFormula;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;

import de.zbit.gui.ColorChooserWithPreview;
import de.zbit.gui.GUITools;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.gui.table.JTableHyperlinkMouseListener;
import de.zbit.gui.table.renderer.ColoredBooleanRenderer;
import de.zbit.sbml.io.SBOTermFormatter;
import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;

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
  
  private static final double SPINNER_MIN_VALUE = -1E9d;
  
  private static final double SPINNER_MAX_VALUE = 1E9d;
  
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
      } else if (sbase instanceof SBMLDocument) {
        addProperties((SBMLDocument) sbase);
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
      if (sbase instanceof GraphicalObject) {
        GraphicalObject go = (GraphicalObject) sbase;
        if (go.isSetBoundingBox()) {
          lh.add(new SBasePanel(go.getBoundingBox()), 1, ++row, 3, 1, 0d, 0d );
        }
        if (go instanceof TextGlyph) {
          addProperties((TextGlyph) go);
        } else if (go instanceof SpeciesReferenceGlyph) {
          addProperties((SpeciesReferenceGlyph) go);
        } else if (go instanceof AbstractReferenceGlyph) {
          addProperties((AbstractReferenceGlyph) go);
        }
      }
      if (sbase instanceof LineSegment) {
        addProperties((LineSegment) sbase);
      } else if (sbase instanceof Point) {
        addProperties((Point) sbase);
      } else if (sbase instanceof Dimensions) {
        addProperties((Dimensions) sbase);
      } else if (sbase instanceof BoundingBox) {
        addProperties((BoundingBox) sbase);
      } else if (sbase instanceof Group) {
        addProperties((Group) sbase);
      } else if (sbase instanceof Member) {
        addProperties((Member) sbase);
      }
      if (sbase instanceof ColorDefinition) {
        addProperties((ColorDefinition) sbase);
      } else if (sbase instanceof LocalStyle) {
        addProperties((LocalStyle) sbase);
      } else if (sbase instanceof RenderGroup) {
        addProperties((RenderGroup) sbase);
      }
    }
    //GUITools.setOpaqueForAllElements(this, false);
  }
  
  /**
   * 
   * @param g
   */
  private void addProperties(RenderGroup g) {
    // TODO!
    if (g.isSetStartHead()) {
      
    }
    if (g.isSetEndHead()) {
      
    }
    if (g.isSetFontFamily()) {
      
    }
    if (g.isSetFontSize()) {
      
    }
    if (g.isSetFontStyleItalic()) {
      
    }
    if (g.isSetFontWeightBold()) {
      
    }
    if (g.isSetTextAnchor()) {
      
    }
    if (g.isSetVTextAnchor()) {
      
    }
    if (g.isSetFill()) {
      
    }
    if (g.isSetStrokeWidth()) {
      
    }
    if (g.isSetTransform()) {
      
    }
  }
  
  /**
   * 
   * @param ls
   */
  private void addProperties(LocalStyle ls) {
    if (ls.isSetIDList()) {
      addLabeledComponent(bundle.getString("idList"), new JList<String>(ls.getIDList().toArray(new String[0])));
    }
    if (ls.isSetRoleList()) {
      addLabeledComponent(bundle.getString("roleList"), new JList<String>(ls.getRoleList().toArray(new String[0])));
    }
  }
  
  /**
   * 
   * @param cd
   */
  private void addProperties(ColorDefinition cd) {
    if (cd.isSetValue()) {
      addLabeledComponent(bundle.getObject("value"), new ColorChooserWithPreview(cd.getValue()));
    }
  }
  
  /**
   * 
   * @param srg
   */
  private void addProperties(SpeciesReferenceGlyph srg) {
    if (srg.isSetSpeciesReferenceRole()) {
      addLabeledComponent(bundle.getString("speciesReferenceRole"),
        enumComboBox(Arrays.asList(SpeciesReferenceRole.values()), srg.getSpeciesReferenceRole()));
    }
    addProperties((AbstractReferenceGlyph) srg);
  }
  
  /**
   * @param arg
   */
  public void addProperties(AbstractReferenceGlyph arg) {
    if (arg.isSetReference()) {
      NamedSBase nsb = arg.getReferenceInstance();
      if (nsb != null) {
        lh.add(new SBasePanel(nsb), 1, ++row, 3, 1, 0d, 0d );
      } else {
        String clazz = arg.getClass().getSimpleName();
        addLabeledTextField(bundle.getString(clazz.substring(0, clazz.indexOf("G") - 1)), arg.getReference());
      }
    }
  }
  
  /**
   * @param tg
   */
  public void addProperties(TextGlyph tg) {
    GraphicalObject go = tg.getGraphicalObjectInstance();
    if (tg.isSetOriginOfText()) {
      NamedSBase nsb = tg.getOriginOfTextInstance();
      if ((nsb == null) || ((go != null) && (go instanceof AbstractReferenceGlyph) && ((AbstractReferenceGlyph) go).getReference().equals(nsb.getId()))) {
        // The second condition avoids that we end up writing the same component twice
        addLabeledTextField(bundle.getString("originOfText"), (nsb != null) && nsb.isSetName() ? nsb.getName() : tg.getOriginOfText());
      } else {
        lh.add(new SBasePanel(nsb), 1, ++row, 3, 1, 0d, 0d );
      }
    } else if (tg.isSetText()) {
      addLabeledTextField(bundle.getString("text"), tg.getText());
    }
    if (tg.isSetGraphicalObject()) {
      if (go != null) {
        lh.add(new SBasePanel(go), 1, ++row, 3, 1, 0d, 0d );
      } else {
        addLabeledTextField(bundle.getString("graphicalObject"), tg.getGraphicalObject());
      }
    }
  }
  
  /**
   * 
   * @param member
   */
  private void addProperties(Member member) {
    SBase sbase = null;
    if (member.isSetIdRef()) {
      Model model = member.getModel();
      sbase = model.findNamedSBase(member.getIdRef());
    } else if (member.isSetMetaIdRef()) {
      SBMLDocument doc = member.getSBMLDocument();
      sbase = doc.findSBase(member.getMetaIdRef());
    }
    if (sbase != null) {
      SBasePanel panel = new SBasePanel(sbase);
      panel.setBorder(BorderFactory.createTitledBorder(" " + bundle.getString("member.ref") + " "));
      lh.add(panel, 1, ++row, 5, 1, 0d, 0d);
    }
  }
  
  /**
   * 
   * @param group
   */
  private void addProperties(Group group) {
    if (group.isSetKind()) {
      addLabeledComponent(bundle.getString("group.kind"), enumComboBox(Arrays.asList(Group.Kind.values()), group.getKind()));
    }
  }
  
  /**
   * 
   * @param doc
   */
  private void addProperties(SBMLDocument doc) {
    if (doc.isSetLevel()) {
      addLabeledComponent(bundle.getString("level"), createJSpinner(doc.getLevel(), 1, 100, 1));
    }
    if (doc.isSetVersion()) {
      addLabeledComponent(bundle.getString("version"), createJSpinner(doc.getVersion(), 1, 100, 1));
    }
  }
  
  /**
   * 
   * @param value
   * @param min
   * @param max
   * @param stepSize
   * @return
   */
  public JSpinner createJSpinner(int value, int min, int max, int stepSize) {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, stepSize));
    spinner.setEnabled(editable);
    return spinner;
  }
  
  /**
   * 
   * @param bbox
   */
  private void addProperties(BoundingBox bbox) {
    if (bbox.isSetPosition()) {
      SBasePanel p = new SBasePanel(bbox.getPosition());
      p.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("position") + ' '));
      lh.add(p);
    }
    if (bbox.isSetDimensions()) {
      SBasePanel p = new SBasePanel(bbox.getDimensions());
      p.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("dimensions") + ' '));
      lh.add(p);
    }
  }
  
  /**
   * 
   * @param dim
   */
  private void addProperties(Dimensions dim) {
    if (dim.isSetWidth()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(dim.getWidth(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("width"), spinner);
    }
    if (dim.isSetHeight()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(dim.getHeight(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("height"), spinner);
    }
    if (dim.isSetDepth()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(dim.getDepth(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("depth"), spinner);
    }
  }
  
  /**
   * 
   * @param point
   */
  private void addProperties(Point point) {
    if (point.isSetX()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(point.getX(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("x"), spinner);
    }
    if (point.isSetY()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(point.getY(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("y"), spinner);
    }
    if (point.isSetZ()) {
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(point.getZ(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1d));
      spinner.setEnabled(editable);
      addLabeledComponent(bundle.getString("z"), spinner);
    }
  }
  
  /**
   * 
   * @param ls
   */
  private void addProperties(LineSegment ls) {
    if (ls.isSetStart()) {
      SBasePanel panel = new SBasePanel(ls.getStart(), namesIfAvailable);
      panel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("start") + ' '));
      lh.add(panel);
    }
    if (ls.isSetEnd()) {
      SBasePanel panel = new SBasePanel(ls.getEnd(), namesIfAvailable);
      panel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("end") + ' '));
      lh.add(panel);
    }
    if (ls instanceof CubicBezier) {
      addProperties((CubicBezier) ls);
    }
  }
  
  /**
   * 
   * @param cb
   */
  private void addProperties(CubicBezier cb) {
    if (cb.isSetBasePoint1()) {
      SBasePanel panel = new SBasePanel(cb.getStart(), namesIfAvailable);
      panel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("basePoint1") + ' '));
      lh.add(panel);
    }
    if (cb.isSetBasePoint2()) {
      SBasePanel panel = new SBasePanel(cb.getStart(), namesIfAvailable);
      panel.setBorder(BorderFactory.createTitledBorder(' ' + bundle.getString("basePoint2") + ' '));
      lh.add(panel);
    }
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
      JTextField tf = new JTextField(c.isSetCompartmentType() ? print(c.getCompartmentTypeInstance()) : "");
      tf.setEditable(editable);
      addLabeledComponent(bundle.getString("compartmentType"), tf);
    }
    if (c.isSetOutside() || editable) {
      JTextField tf = new JTextField(c.isSetOutside() ? print(c.getOutsideInstance()) : "");
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
   * 
   * @param nsb
   * @return
   */
  private String print(NamedSBase nsb) {
    if (nsb.isSetName()) {
      return nsb.getName();
    }
    if (nsb.isSetId()) {
      return nsb.getId();
    }
    return nsb.getElementName();
  }
  
  /**
   * @param c
   */
  private void addProperties(Constraint c) {
    if (c.isSetMessage() || editable) {
      JTextField tf = new JTextField(org.sbml.jsbml.util.SBMLtools.toXML(c.getMessage()));
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
      lh.add(new SBasePanel(e.getTrigger(), namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 1d);
      lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
    }
    if (e.isSetDelay()) {
      lh.add(new SBasePanel(e.getDelay(), namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 1d);
      lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
    }
    if (e.isSetTimeUnits()) {
      lh.add(new SBasePanel(e.getTimeUnitsInstance(), namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 1d);
      lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
    }
    if (e.isSetListOfEventAssignments()) {
      for (EventAssignment ea : e.getListOfEventAssignments()) {
        lh.add(new SBasePanel(ea, namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 1d);
        lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
      }
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
        String equation = mc.getMath().compile(latex).toString()
            .replace("mathrm", "mbox").replace("text", "mbox")
            .replace("mathtt", "mbox");
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
          equation = equation.substring(7);
        } else if (mc instanceof Assignment) {
          Assignment ea = (Assignment) mc;
          laTeXpreview.append(latex.mbox(ea.getVariable()));
          laTeXpreview.append('=');
        }
        try {
          laTeXpreview.append(equation);
        } catch (Throwable exc) {
          logger.log(Level.WARNING, bundleWarnings.getString("COULD_NOT_CREATE_LATEX_FROM_ASTNODE"), exc);
          laTeXpreview.append(bundleWarnings.getString("invalid"));
          //exc.printStackTrace();
        }
        laTeXpreview.append(LaTeXCompiler.eqEnd);
        JComponent component = renderer.renderEquation(laTeXpreview.toString().replace("dcases", "cases"));
        lh.add(component, 1, ++row, 3, 1, 1d, 0d);
        lh.add(createJPanel(), 1, ++row, 5, 1, 0d, 0d);
      } else {
        JTextField tf = new JTextField(mc.getMath().toFormula());
        tf.setEditable(editable);
        tf.setColumns(25);
        tf.setCaretPosition(0);
        tf.setBorder(BorderFactory.createLoweredBevelBorder());
        addLabeledComponent(bundle.getString("formula"), tf);
      }
      if (mc instanceof Assignment) {
        lh.add(new SBasePanel(((Assignment) mc)
          .getVariableInstance(), namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 0d);
      }
      //      if (mc instanceof KineticLaw) {
      //        KineticLaw kl = (KineticLaw) mc;
      //        if (kl.isSetListOfLocalParameters()) {
      //          for (LocalParameter lp : kl.getListOfLocalParameters()) {
      //            lh.add(new SBasePanel(lp, namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 0d);
      //          }
      //        }
      //      }
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
   * 
   * @param label
   * @param text
   */
  private void addLabeledTextField(Object label, Object text) {
    addLabeledComponent(label, createTextField(text.toString()));
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
    return createTextField(nsb.isSetName() ? nsb.getName() : nsb.getId());
  }
  
  /**
   * 
   * @param content
   * @return
   */
  private JTextField createTextField(String content) {
    JTextField tf = new JTextField(content);
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
    addLabeledComponent(q instanceof Species ? bundle.getString("substanceUnit") : bundle.getString("unit"), unitPreview(q.getDerivedUnitDefinition()));
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
          rmp[count++][0] = print(specRef.getSpeciesInstance());
        }
      }
    }
    if (reaction.isSetListOfModifiers()) {
      count = 0;
      for (ModifierSpeciesReference mSpecRef : reaction.getListOfModifiers()) {
        if (mSpecRef.isSetSpeciesInstance()) {
          rmp[count++][1] = print(mSpecRef.getSpeciesInstance());
        }
      }
    }
    if (reaction.isSetListOfProducts()) {
      count = 0;
      for (SpeciesReference specRef : reaction.getListOfProducts()) {
        if (specRef.isSetSpeciesInstance()) {
          rmp[count++][2] = print(specRef.getSpeciesInstance());
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
      lh.add(new SBasePanel(reaction.getKineticLaw(), namesIfAvailable, renderer), 1, ++row, 3, 1, 0d, 0d);
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
      if (hist.isSetListOfCreators()) {
        int i = 0;
        for (Creator mc : hist.getListOfCreators()) {
          rowData[i][0] = mc.getGivenName();
          rowData[i][1] = mc.getFamilyName();
          rowData[i][2] = "<html><a href=\"mailto:" + mc.getEmail() + "?subject=" + print(sbase).replace(' ', '%') + "\">" + mc.getEmail() + "</a></html>";
          rowData[i][3] = mc.getOrganization();
          i++;
        }
      }
      JTable table = new JTable(rowData, columnNames);
      table.setEnabled(editable);
      table.setPreferredScrollableViewportSize(new Dimension(200, (table
          .getRowCount() + 1)
        * table.getRowHeight()));
      for (int j = 0; j < table.getModel().getColumnCount(); j++) {
        table.setDefaultRenderer(table.getModel().getColumnClass(j), new ColoredBooleanRenderer());
      }
      table.addMouseListener(new JTableHyperlinkMouseListener());
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
      for (int i = 0; i < hist.getModifiedDateCount(); i++) {
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
      JEditorPane notesArea = createHTMLPane(SBMLtools.createHTMLfromNotes(sbase));
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
        String cvtString = cvt.printCVTerm();
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
      JEditorPane l = createHTMLPane(sb.toString());
      JScrollPane editorScrollPane = new JScrollPane(l);
      editorScrollPane.setPreferredSize(new Dimension(250, 145));
      editorScrollPane.setMinimumSize(new Dimension(10, 10));
      editorScrollPane.setViewportBorder(BorderFactory.createLoweredBevelBorder());
      JPanel miriamPanel = createJPanel(new BorderLayout());
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
      try {
        Term term = SBO.getTerm(sbase.getSBOTerm());
        helper.add(new JLabel(bundle.getString("name")), 0, ++innerRow, 1, 1, 0d, 0d);
        JTextArea nameField = new JTextArea(term.getName(), 2, columns);
        nameField.setEditable(editable);
        nameField.setCaretPosition(0);
        nameField.setLineWrap(true);
        nameField.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(nameField);
        helper.add(scroll, 3, innerRow, 1, 1, 1d, 0d);
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
        JScrollPane scroll1 =  new JScrollPane(sboTermField);
        helper.add(scroll1, 3, innerRow, 1, 1, 1d, 0d);
      } catch (NoSuchElementException exc) {
        logger.warning(Utils.getMessage(exc));
      }
      helper.add(createJPanel(), 1, ++innerRow, 5, 1, 0d, 0d);
      
      lh.add(helper.getContainer(), 1, ++row, 3, 1, 0d, 0d);
    }
  }
  
  /**
   * 
   * @param sbase
   * @return
   */
  private String print(SBase sbase) {
    if (sbase instanceof NamedSBase) {
      return print((NamedSBase) sbase);
    }
    return sbase.toString();
  }
  
  /**
   * 
   * @param text
   * @return
   */
  private JEditorPane createHTMLPane(String text) {
    Dimension dimension = new Dimension(preferedWidth, 200);
    JEditorPane area = new JEditorPane("text/html", text);
    area.setDoubleBuffered(true);
    area.setEditable(editable);
    area.addHyperlinkListener(new SystemBrowser());
    area.setAutoscrolls(true);
    area.setPreferredSize(dimension);
    area.setBackground(Color.WHITE);
    area.setMaximumSize(dimension);
    area.setCaretPosition(0);
    setFont(area);
    return area;
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
      lh.add(new SBasePanel(ssr.getSpeciesInstance(), namesIfAvailable, renderer), 1,
        ++row, 3, 1, 1d, 1d);
    }
  }
  
  /**
   * @param sbase
   */
  private void addProperties(Species species) {
    if (species.isSetSpeciesType()) {
      JTextField tf = new JTextField(print(species.getSpeciesTypeInstance()));
      tf.setEditable(editable);
      addLabeledComponent(bundle.getString("speciesType"), tf);
    }
    JTextField tf = new JTextField(print(species.getCompartmentInstance()));
    tf.setEditable(editable);
    addLabeledComponent(bundle.getString("compartment"), tf);
    if (species.isSetSpeciesType() || editable) {
      tf = new JTextField(print(species.getSpeciesTypeInstance()));
      tf.setEditable(editable);
      addLabeledComponent(bundle.getString("speciesType"), tf);
    }
    int charge = species.getCharge();
    if (species.getPlugin(FBCConstants.getNamespaceURI(species.getLevel(), species.getVersion())) != null) {
      FBCSpeciesPlugin fbcSpecies = (FBCSpeciesPlugin) species.getPlugin(FBCConstants.getNamespaceURI(species.getLevel(), species.getVersion()));
      if (fbcSpecies.isSetCharge()) {
        charge = fbcSpecies.getCharge();
      }
      if (fbcSpecies.isSetChemicalFormula()) {
        addLabeledComponent(bundle.getString("chemicalFormula"), createTextField(fbcSpecies.getChemicalFormula()));
      }
    }
    JSpinner spinCharge = new JSpinner(new SpinnerNumberModel(charge, -10, 10, 1));
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
          l = sMath.getMath().compile(latex).toString().replace("\\\\", "\\");
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
        .getStoichiometry(), specRef.getStoichiometry() + SPINNER_MIN_VALUE,
        specRef.getStoichiometry() + SPINNER_MAX_VALUE, .1d));
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
        .getOffset(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1));
      sOffset.setEnabled(editable);
      addLabeledComponent(bundle.getString("offset"), sOffset);
    }
    JSpinner sScale = new JSpinner(new SpinnerNumberModel(
      unit.getScale(), SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, 1));
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
    return enumComboBox(Arrays.asList(Unit.Kind.values()), kind);
  }
  
  /**
   * 
   * @param listOfEnum
   * @param item
   * @return
   */
  private <E extends Enum<E>> JComboBox enumComboBox(List<E> listOfEnum, E item) {
    JComboBox box = new JComboBox();
    for (E e : listOfEnum) {
      box.addItem(e);
      if (e.equals(item)) {
        box.setSelectedItem(e);
      }
    }
    box.setEditable(editable);
    box.setEnabled(editable);
    box.setRenderer(new ListTextRenderer(box.getRenderer()));
    return box;
  }
  
  /**
   * 
   * @author Andreas Dr&auml;ger
   * @version $Rev$
   */
  private class ListTextRenderer implements ListCellRenderer {
    
    /**
     * 
     */
    private ListCellRenderer originalRenderer;
    
    /**
     * 
     * @param originalRenderer
     */
    public ListTextRenderer(ListCellRenderer originalRenderer) {
      this.originalRenderer = originalRenderer;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list,
      Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
      String text = null;
      try {
        text = bundle.getString(value.toString());
      } catch (MissingResourceException exc) {
      }
      return originalRenderer.getListCellRendererComponent(list,
        text == null ? value.toString() : text, index, isSelected, cellHasFocus);
    }
    
  }
  
  /**
   * @param ud
   */
  private void addProperties(UnitDefinition ud) {
    addLabeledComponent(bundle.getString("definition"), unitPreview(ud));
    if (ud.isSetListOfUnits()) {
      for (Unit u : ud.getListOfUnits()) {
        lh.add(new SBasePanel(u, namesIfAvailable, renderer), 1, ++row, 3, 1, 1d, 0d);
      }
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
  @Override
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
    return renderer != null;
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
  @Override
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
    JEditorPane preview = new JEditorPane();
    preview.setContentType("text/html");
    setFont(preview);
    preview.setEditable(editable);
    preview.setBorder(BorderFactory.createLoweredBevelBorder());
    preview.setText(StringUtil
      .toHTML(ud != null ? HTMLFormula.toHTML(ud) : ""));
    return preview;
  }
  
  /**
   * TODO
   * @param editor
   */
  private void setFont(JEditorPane editor) {
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    editor.setFont(font);
    String bodyRule = "body { font-family: " + font.getFamily() + "; " +
        "font-size: " + font.getSize() + "pt; }";
    ((HTMLDocument) editor.getDocument()).getStyleSheet().addRule(bodyRule);
  }
  
}
