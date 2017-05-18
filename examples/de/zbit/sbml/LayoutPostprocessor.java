/*
 * $IdLayoutPostprocessor.java 10:48:35 draeger $
 * $URLLayoutPostprocessor.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2017 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml;

import static de.zbit.sbml.layout.RenderProcessor.RENDER_LINK;
import static java.text.MessageFormat.format;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.ProgressMonitorInputStream;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.XMLTools;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.NameFilter;
import org.sbml.jsbml.xml.XMLNode;

import de.zbit.sbml.layout.RenderProcessor;

/**
 * This takes an SBML file as input that has been converted from CellDesigner
 * and postprocesses the layout in there.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.2
 * @date 09.11.2016
 */
public class LayoutPostprocessor {
  
  /**
   * 
   * @param args
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws IOException
   */
  public static void main(String args[]) throws XMLStreamException, SBMLException, IOException {
    SBMLDocument doc = SBMLReader.read(new ProgressMonitorInputStream(null, "Reading File", new FileInputStream(new File(args[0]))));
    //new YGraphView(doc);
    LayoutPostprocessor lp = new LayoutPostprocessor(doc);
    logger.info("Removing unconnected species");
    lp.removeUnconnectedSpecies();
    logger.info("Removing unconnected species glyphs");
    lp.removeUnconnectedSpeciesGlyphs();
    logger.info("Trimming CellDesigner annotation");
    lp.trimCellDesignerAnnotation();
    logger.info("Removing invalid entries from style links");
    lp.removeUnconnecedStyles();
    logger.info("Defining SBO terms based on color");
    lp.setSBOtermsByFillColor(XMLTools.decodeStringToColor("#CCFF66FF"));
    logger.info("Merging MIRIAM annotations with identical qualifier");
    lp.mergeMIRIAMannotations(doc);
    logger.info("Updating species ids to BiGG ids");
    lp.constructBiGGidsFromNames(doc);
    logger.info("Removing empty XHTML head statements");
    lp.shrinkXHTML(doc);
    logger.info("Scaling spaces in layout");
    lp.scaleLayoutDistances(doc, 2d, 2d);
    
    // for testing
    logger.info("Validating SBML document");
    lp.validate(doc);
    File outFile = new File(args[1]);
    logger.info(format("Writing output file {0}", outFile));
    TidySBMLWriter.write(doc, outFile, ' ', (short) 2);
    logger.info("Launching preview");
  }
  
  /**
   * 
   * @param doc
   * @param scale
   */
  public void scaleLayoutDistances(SBMLDocument doc, double xScale, double yScale) {
    Model model = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
    for (Layout layout : lmp.getListOfLayouts()) {
      scaleLayoutDistances(layout, xScale, yScale);
    }
  }
  
  /**
   * 
   * @param layout
   * @param xScale
   * @param yScale
   */
  public void scaleLayoutDistances(Layout layout, double xScale, double yScale) {
    resize(layout.getDimensions(), xScale, yScale);
    if (layout.isSetListOfCompartmentGlyphs()) {
      logger.info("Repositioning all compartment glyps");
      for (CompartmentGlyph cg : layout.getListOfCompartmentGlyphs()) {
        resize(cg, xScale, yScale);
      }
    }
    if (layout.isSetListOfSpeciesGlyphs()) {
      logger.info("Repositioning all species glyphs");
      for (SpeciesGlyph sg : layout.getListOfSpeciesGlyphs()) {
        reposition(sg, xScale, yScale);
      }
    }
    if (layout.isSetListOfReactionGlyphs()) {
      logger.info("Repositioning all reaction glyphs");
      for (ReactionGlyph rg : layout.getListOfReactionGlyphs()) {
        reposition(rg, xScale, yScale);
        for (SpeciesReferenceGlyph srg : rg.getListOfSpeciesReferenceGlyphs()) {
          if (srg.isSetBoundingBox()) {
            reposition(srg, xScale, yScale);
            resize(srg, xScale, yScale);
          }
          if (srg.isSetCurve()) {
            Curve curve = srg.getCurve();
            for (int i = 0; i < curve.getCurveSegmentCount(); i++) {
              CurveSegment cs = curve.getCurveSegment(i);
              reposition(cs.getStart(), xScale, yScale);
              reposition(cs.getEnd(), xScale, yScale);
              if (cs instanceof CubicBezier) {
                CubicBezier cb = (CubicBezier) cs;
                reposition(cb.getBasePoint1(), xScale, yScale);
                reposition(cb.getBasePoint2(), xScale, yScale);
              }
            }
          }
        }
      }
    }
    if (layout.isSetListOfTextGlyphs()) {
      logger.info("Repositioning all text glyphs");
      for (TextGlyph tg : layout.getListOfTextGlyphs()) {
        reposition(tg, tg.isSetGraphicalObject() ? xScale : xScale, yScale);
      }
    }
    if (layout.isSetListOfAdditionalGraphicalObjects()) {
      logger.info("Repositioning all other graphical objects");
      for (GraphicalObject go : layout.getListOfAdditionalGraphicalObjects())  {
        reposition(go, xScale, yScale);
      }
    }
  }
  
  /**
   * @param go
   * @param xScale
   * @param yScale
   */
  public void reposition(GraphicalObject go, double xScale, double yScale) {
    BoundingBox bbox = go.getBoundingBox();
    Point pos = bbox.getPosition();
    reposition(pos, xScale, yScale);
  }
  
  /**
   * @param pos
   * @param xScale
   * @param yScale
   */
  public void reposition(Point pos, double xScale, double yScale) {
    pos.setX(pos.x() * xScale);
    pos.setY(pos.y() * yScale);
  }
  
  /**
   * 
   * @param go
   * @param xScale
   * @param yScale
   */
  public void resize(GraphicalObject go, double xScale, double yScale) {
    resize(go.getBoundingBox(), xScale, yScale);
  }
  
  /**
   * 
   * @param bbox
   * @param xScale
   * @param yScale
   */
  public void resize(BoundingBox bbox, double xScale, double yScale) {
    resize(bbox.getDimensions(), xScale, yScale);
  }
  
  /**
   * @param dim
   * @param xScale
   * @param yScale
   */
  public void resize(Dimensions dim, double xScale, double yScale) {
    dim.setWidth(dim.getWidth() * xScale);
    dim.setHeight(dim.getHeight() * yScale);
  }
  
  
  /**
   * 
   * @param sbase
   */
  private void mergeMIRIAMannotations(SBase sbase) {
    if (sbase.isSetAnnotation()) {
      SortedMap<Qualifier, SortedSet<String>> miriam = new TreeMap<>();
      boolean doMerge = false;
      doMerge = hashMIRIAMuris(sbase, miriam);
      if (doMerge) {
        sbase.getAnnotation().unsetCVTerms();
        for (Entry<Qualifier, SortedSet<String>> entry : miriam.entrySet()) {
          logger.info(format("Merging all resources with identical MIRIAM qualifier ''{0}'' in {1} with id=''{2}''.",
            entry.getKey(), sbase.getClass().getSimpleName(), sbase.getId()));
          sbase.addCVTerm(new CVTerm(entry.getKey(), entry.getValue().toArray(new String[0])));
        }
      }
    }
    for (int i = 0; i < sbase.getChildCount(); i++) {
      TreeNode node = sbase.getChildAt(i);
      if (node instanceof SBase) {
        mergeMIRIAMannotations((SBase) node);
      }
    }
  }
  
  
  /**
   * @param sbase
   * @param miriam
   * @return
   */
  public boolean hashMIRIAMuris(SBase sbase, SortedMap<Qualifier, SortedSet<String>> miriam) {
    boolean doMerge = false;
    for (int i = 0; i < sbase.getCVTermCount(); i++) {
      CVTerm term = sbase.getCVTerm(i);
      Qualifier qualifier = term.getQualifier();
      if (!miriam.containsKey(qualifier)) {
        if (sbase instanceof Model) {
          if (!qualifier.isModelQualifier()) {
            logger.info(format("Correcting invalid use of biological qualifier ''{0}'' on model with id=''{1}''.",
              qualifier.getElementNameEquivalent(), sbase.getId()));
            qualifier = Qualifier.getModelQualifierFor(qualifier.getElementNameEquivalent());
          }
        } else if (!qualifier.isBiologicalQualifier()) {
          logger.info(format("Correcting invalid use of model qualifier ''{0}'' on {1} with id=''{2}''.",
            qualifier.getElementNameEquivalent(), sbase.getClass().getSimpleName(), sbase.getId()));
          qualifier = Qualifier.getBiologicalQualifierFor(qualifier.getElementNameEquivalent());
        }
        miriam.put(qualifier, new TreeSet<String>());
      } else {
        doMerge = true;
      }
      miriam.get(qualifier).addAll(term.getResources());
    }
    return doMerge;
  }
  
  
  /**
   * 
   * @param sbase
   */
  private void shrinkXHTML(SBase sbase) {
    if (sbase.isSetNotes()) {
      XMLNode node = sbase.getNotes();
      if ((node.getChildCount() > 1) && (node.getChildAt(1) != null)
          && (node.getChildAt(1).getChildCount() > 1)
          && (node.getChildAt(1).getChild(1) != null)
          && (node.getChildAt(1).getChildAt(1).getChildCount() == 3)
          && (node.getChild(1).getChildAt(1).getChildAt(1).getName().equals("title"))) {
        logger.info(format(
          "Removing unnecessary XHTML header entry with empty title tag from {0} with id=''{1}''.",
          sbase.getClass().getSimpleName(), sbase.getId()));
        node.getChildAt(1).removeChild(1);
      }
    }
    for (int i = 0; i < sbase.getChildCount(); i++) {
      TreeNode node = sbase.getChildAt(i);
      if (node instanceof SBase) {
        shrinkXHTML((SBase) node);
      }
    }
  }
  
  /**
   * 
   * @param doc
   */
  @SuppressWarnings("unchecked")
  public void constructBiGGidsFromNames(SBMLDocument doc) {
    Model model = doc.getModel();
    for (int i = model.getSpeciesCount() - 1; i >= 0; i--) {
      Species species = model.getSpecies(i);
      if (species.isSetName() && species.getName().endsWith("]")) {
        String biggId = "M_" + species.getName().substring(0, species.getName().length() - 1).replace('[', '_');
        for (String rId : (SortedSet<String>) species.getUserObject(SPECIES_REACTION_LINK)) {
          updateReferences(model.getReaction(rId), species.getId(), biggId);
        }
        for (String gId : (SortedSet<String>) species.getUserObject(SPECIES_GLYPH_LINK)) {
          updateReferences(model.getSBaseById(gId), species.getId(), biggId);
        }
        SortedSet<String> setOfTextGlyphReferences = (SortedSet<String>) species.getUserObject(TEXT_GLYPH_LINK);
        if (setOfTextGlyphReferences != null) {
          for (String tId : setOfTextGlyphReferences) {
            TextGlyph tg = (TextGlyph) model.getSBaseById(tId);
            if (tg.getOriginOfText().equals(species.getId())) {
              tg.setOriginOfText(biggId);
            }
          }
        }
        if (model.getSpecies(biggId) != null) {
          logger.info(format("Deleting core species with id=''{0}'' because of duplicate BiGG id=''{1}''", species.getId(), biggId));
          // compare deleted species and existing one...
          Species other = model.getSpecies(biggId);
          if (species.isSetNotes() && !other.isSetNotes()) {
            other.setNotes(species.getNotes().clone());
          } else if (species.isSetNotes() && other.isSetNotes()) {
            try {
              if (!species.getNotesString().equals(other.getNotesString())) {
                // TODO: serious problem! But doesn't happen in Recon-2.01...
                System.out.println("Detected unsolved conflict - halting algorithm now. Need fix!");
                System.exit(1);
              }
            } catch (XMLStreamException exc) {
              exc.printStackTrace();
            }
          }
          if (!species.getCVTerms().equals(other.getCVTerms())) {
            logger.info(format("Merging non matching MIRIAM annotations for species with BiGG id=''{0}''", biggId));
            SortedMap<Qualifier, SortedSet<String>> miriam = new TreeMap<>();
            hashMIRIAMuris(species, miriam);
            hashMIRIAMuris(other, miriam);
            other.unsetCVTerms();
            for (Entry<Qualifier, SortedSet<String>> entry : miriam.entrySet()) {
              other.addCVTerm(new CVTerm(entry.getKey(), entry.getValue().toArray(new String[0])));
            }
          }
          species.removeFromParent();
        } else {
          logger.info(format("Updating species id=''{0}'' to BiGG id=''{1}''.", species.getId(), biggId));
          species.setId(biggId);
        }
      } else {
        System.out.println(species);
      }
    }
  }
  
  /**
   * 
   * @param root
   * @param oldId
   * @param newId
   * @return
   */
  public boolean updateReferences(SBase root, String oldId, String newId) {
    if (root instanceof SimpleSpeciesReference) {
      SimpleSpeciesReference ssr = (SimpleSpeciesReference) root;
      if (ssr.isSetSpecies() && ssr.getSpecies().equals(oldId)) {
        ssr.setSpecies(newId);
        return true;
      }
    } else if (root instanceof SpeciesGlyph) {
      SpeciesGlyph sg = (SpeciesGlyph) root;
      if (sg.isSetSpecies() && sg.getSpecies().equals(oldId)) {
        sg.setSpecies(newId);
        return true;
      }
    } else if (!root.isLeaf()) {
      boolean change = false;
      for (int i = 0; i < root.getChildCount(); i++) {
        TreeNode node = root.getChildAt(i);
        if (node instanceof SBase) {
          change |= updateReferences((SBase) node, oldId, newId);
        }
      }
      return change;
    }
    return false;
  }
  
  /**
   * Validate.
   * @param doc
   */
  public void validate(SBMLDocument doc) {
    doc.checkConsistencyOffline();
    SBMLErrorLog errorLog = doc.getListOfErrors();
    List<SBMLError> errorList = errorLog.getValidationErrors();
    for (SBMLError e : errorList) {
      System.out.println(e.getLine() + " " + e.getMessage());
    }
  }
  
  /**
   * 
   */
  public void removeUnconnecedStyles() {
    Model model = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if (lmp != null) {
      for (int i = 0; i < lmp.getLayoutCount(); i++) {
        Layout layout = lmp.getLayout(i);
        RenderLayoutPlugin rlp = (RenderLayoutPlugin) layout.getExtension(RenderConstants.shortLabel);
        if ((rlp != null) && rlp.isSetListOfLocalRenderInformation()) {
          RenderProcessor.preprocess(layout);
          ListOf<LocalRenderInformation> listOfLocalRenderInformation = rlp.getListOfLocalRenderInformation();
          Map<LocalStyle, Set<GraphicalObject>> usedStyles = new HashMap<>();
          layout.getListOfAdditionalGraphicalObjects().forEach(go -> {findUsedStyles(usedStyles, go);});
          layout.getListOfCompartmentGlyphs().forEach(go -> {findUsedStyles(usedStyles, go);});
          layout.getListOfReactionGlyphs().forEach(go -> {findUsedStyles(usedStyles, go);});
          layout.getListOfSpeciesGlyphs().forEach(go -> {findUsedStyles(usedStyles, go);});
          layout.getListOfTextGlyphs().forEach(go -> {findUsedStyles(usedStyles, go);});
          
          for (LocalRenderInformation lri : listOfLocalRenderInformation) {
            if (lri.isSetListOfLocalStyles() && lri.isSetListOfColorDefinitions()) {
              for (int k = lri.getListOfLocalStyles().size() - 1; k >= 0; k--) {
                LocalStyle ls = lri.getListOfLocalStyles().get(k);
                
                List<String> idList = null;
                if ((ls.isSetId() && !ls.getIDList().isEmpty())) {
                  idList = new LinkedList<>(ls.getIDList());
                  for (int j = idList.size() - 1; j >= 0; j--) {
                    String id = idList.get(j);
                    SBase sbase = model.getSBaseById(id);
                    if (sbase == null) {
                      idList.remove(j);
                    }
                  }
                }
                if (((idList != null) && idList.isEmpty()) || !usedStyles.containsKey(ls)) {
                  removeLocalStyle(lri, k);
                } else {
                  if ((idList != null) && (idList.size() != ls.getIDList().size())) {
                    ls.setIDList(idList);
                  } else if (ls.isSetIDList() && ls.getIDList().isEmpty()) {
                    ls.unsetIDList();
                  }
                  int pos = k + 1;
                  ls.setId(ls.getId().substring(0, ls.getId().indexOf('_')) + StringTools.fill(getDigitCount(lri.getLocalStyleCount()) - getDigitCount(pos), '0') + pos);
                }
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param n
   * @return
   */
  public static int getDigitCount(int n) {
    return (int) Math.floor(1 + Math.log10(Math.abs(n)));
  }
  
  private Set<String> processedColor = new HashSet<>();
  
  /**
   * @param usedStyles
   * @param go
   */
  @SuppressWarnings("unchecked")
  private void findUsedStyles(Map<LocalStyle, Set<GraphicalObject>> usedStyles, GraphicalObject go) {
    if (go.getUserObject(RENDER_LINK) != null) {
      for (LocalStyle style : (List<LocalStyle>) go.getUserObject(RENDER_LINK)) {
        if (go instanceof AbstractReferenceGlyph) {
          AbstractReferenceGlyph arg = (AbstractReferenceGlyph) go;
          if (arg.isSetReference() && (go.getModel().getSBaseById(arg.getReference()) == null)) {
            continue;
          }
        }
        if (!usedStyles.containsKey(style)) {
          usedStyles.put(style, new HashSet<>());
        }
        if (style.isSetGroup()) {
          RenderGroup g = style.getGroup();
          if (g.isSetFill() && !processedColor.contains(g.getFill())) {
            LocalRenderInformation lri = (LocalRenderInformation) style.getParent().getParent();
            ColorDefinition cd = lri.getColorDefinition(g.getFill());
            int pos = lri.getListOfColorDefinitions().indexOf(cd) + 1;
            Set<GraphicalObject> set = usedStyles.remove(style);
            cd.setId("color_" + StringTools.fill(getDigitCount(lri.getColorDefinitionCount()) - getDigitCount(pos), '0') + pos);
            g.setFill(cd.getId());
            processedColor.add(g.getFill());
            usedStyles.put(style, set);
          }
        }
        usedStyles.get(style).add(go);
      }
    }
  }
  
  /**
   * 
   * @param ls
   * @return
   */
  private void removeLocalStyle(LocalRenderInformation lri, int k) {
    LocalStyle ls = lri.getListOfLocalStyles().get(k);
    logger.info("Removing local style " + ls.getId());
    if (ls.isSetGroup() && ls.getGroup().isSetFill()) {
      String color = ls.getGroup().getFill();
      logger.info("Deleting Color definition " + color);
      lri.getListOfColorDefinitions().removeFirst(new NameFilter(color));
    }
    lri.removeLocalStyle(k);
  }
  
  /**
   * Set SBO terms for secondary metaoblites based on the color of the main
   * metabolites
   * 
   * @param color
   *        the color of the main metabolites, i.e., those along the main axes
   *        in the pathways.
   */
  public void setSBOtermsByFillColor(Color color) {
    Model model = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if ((lmp != null) && lmp.isSetListOfLayouts()) {
      for (Layout layout : lmp.getListOfLayouts()) {
        setSBOtermsByFillColor(layout, color);
      }
    }
  }
  
  /**
   * @param layout
   * @param color
   */
  public void setSBOtermsByFillColor(Layout layout, Color color) {
    Model model = layout.getModel();
    if (layout.isSetListOfSpeciesGlyphs()) {
      for (SpeciesGlyph sg : layout.getListOfSpeciesGlyphs()) {
        Color c = RenderProcessor.getRenderFillColor(sg);
        Set<String> setOfreactionGlyphs = (Set<String>) sg.getUserObject(SPECIES_GLYPH_LINK);
        if (setOfreactionGlyphs != null) {
          for (String rgId : setOfreactionGlyphs) {
            ReactionGlyph rg = (ReactionGlyph) model.getSBaseById(rgId);
            SpeciesReferenceGlyph srg = rg.getListOfSpeciesReferenceGlyphs().firstHit((Object o) -> {
              return ((SpeciesReferenceGlyph) o).getSpeciesGlyph().equals(sg.getId());
            });
            SpeciesReference specRef = (SpeciesReference) srg.getReferenceInstance();
            
            //Species species = (Species) sg.getReferenceInstance();
            
            // We identified a secondary metabolite, now let's set role and SBO term appropriately.
            boolean isMainMetabolite = c.equals(color);
            
            if (srg.isSetSpeciesReferenceRole()) {
              switch (srg.getRole()) {
                case SUBSTRATE:
                  srg.setRole(isMainMetabolite ? SpeciesReferenceRole.SUBSTRATE : SpeciesReferenceRole.SIDESUBSTRATE);
                  break;
                case PRODUCT:
                  srg.setRole(isMainMetabolite ? SpeciesReferenceRole.PRODUCT : SpeciesReferenceRole.SIDEPRODUCT);
                  break;
                default:
                  break;
              }
              int sbo = srg.getRole().toSBOterm();
              if (!srg.isSetSBOTerm() || (srg.getSBOTerm() != sbo)) {
                logger.info(generateSBOTermUpdateMessage(srg, sbo));
                srg.setSBOTerm(sbo);
              }
            }
            if (specRef != null) {
              Reaction r = (Reaction) specRef.getParent().getParent();
              int sbo = specRef.getSBOTerm();
              if (r.getListOfReactants() == specRef.getParent()) {
                sbo = isMainMetabolite ? SBO.getReactant() : SBO.getSideSubstrate();
              } else if (r.getListOfProducts() == specRef.getParent()) {
                sbo = isMainMetabolite ? SBO.getProduct() : SBO.getSideProduct();
              } else if (r.getListOfModifiers() == specRef.getParent()) {
                sbo = SBO.getModifier();
              }
              if (!specRef.isSetSBOTerm() || (SBO.isChildOf(sbo, specRef.getSBOTerm()) && (sbo != specRef.getSBOTerm()))) {
                logger.info(generateSBOTermUpdateMessage(specRef, sbo));
                specRef.setSBOTerm(sbo);
              }
              if (srg.isSetSBOTerm()) {
                if (SBO.isChildOf(srg.getSBOTerm(), specRef.getSBOTerm()) && (srg.getSBOTerm() != specRef.getSBOTerm())) {
                  logger.info(generateSBOTermUpdateMessage(specRef, srg.getSBOTerm()));
                  specRef.setSBOTerm(srg.getSBOTerm());
                }
              } else {
                logger.info(generateSBOTermUpdateMessage(srg, specRef.getSBOTerm()));
                srg.setSBOTerm(specRef.getSBOTerm());
                if (!srg.isSetSpeciesReferenceRole()) {
                  srg.setRole(SpeciesReferenceRole.valueOf(srg.getSBOTerm()));
                }
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param sbase
   * @param sboTerm
   * @return
   */
  private String generateSBOTermUpdateMessage(SBase sbase, int sboTerm) {
    return format(
      "Updating SBO term from ''{0}'' to ''{1}'' in {3} with id=''{2}''",
      sbase.isSetSBOTerm() ? SBO.getTerm(sbase.getSBOTerm()).getName() : "undefined",
          SBO.getTerm(sboTerm).getName(),
          sbase.getId(), sbase.getClass().getSimpleName());
  }
  
  /**
   * Recursively removes all non-RDF annotation from the SBML data structure,
   * beginning at the level of the {@link SBMLDocument}
   */
  public void trimCellDesignerAnnotation() {
    trimCellDesignerAnnotation(doc);
    doc.getDeclaredNamespaces().remove("xmlns:celldesigner");
  }
  
  private SBMLDocument doc;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(LayoutPostprocessor.class.getName());
  /**
   * Links each species to a {@link SortedSet} of reaction ids where it participates.
   */
  private static final String SPECIES_REACTION_LINK = "SPECIES_REACTION_LINK";
  /**
   * Links speciesGlyphs to reaction glyphs
   */
  private static final String SPECIES_GLYPH_LINK = "SPECIES_GLYPH_LINK";
  /**
   * Links species to corresponding text glyphs
   */
  private static final String TEXT_GLYPH_LINK = "TEXT_GLYPH_LINK";
  private static final String LAYOUT_LINK = "LAYOUT_LINK";
  
  private Map<String, String> id2name;
  
  /**
   * @param doc
   * 
   */
  public LayoutPostprocessor(SBMLDocument doc) {
    this.doc = doc;
    preprocessSBMLDocument();
    id2name = new HashMap<>();
  }
  
  /**
   * Build links using user objects within the SBML data structure.
   */
  @SuppressWarnings("unchecked")
  private void preprocessSBMLDocument() {
    Model model = doc.getModel();
    // build links from species to reactions.
    for (int i = 0; i < model.getReactionCount(); i++) {
      Reaction r = model.getReaction(i);
      if (r.isSetListOfReactants()) {
        fillMap(r.getId(), r.getListOfReactants());
      }
      if (r.isSetListOfModifiers()) {
        fillMap(r.getId(), r.getListOfModifiers());
      }
      if (r.isSetListOfProducts()) {
        fillMap(r.getId(), r.getListOfProducts());
      }
    }
    // preprocess species names
    for (int i = 0; i < model.getSpeciesCount(); i++) {
      Species s = model.getSpecies(i);
      if (s.isSetName() && !s.getName().equals(s.getName().trim())) {
        logger.info(format("Trimming species name ''{0}''.", s.getName()));
        s.setName(s.getName().trim());
      }
    }
    LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if (layoutPlugin != null) {
      for (int i = 0; i < layoutPlugin.getLayoutCount(); i++) {
        Layout layout = layoutPlugin.getLayout(i);
        // build links within each layout from species glyphs to reaction glyphs
        for (int j = 0; j < layout.getReactionGlyphCount(); j++) {
          ReactionGlyph rg = layout.getReactionGlyph(j);
          if (rg.isSetListOfSpeciesReferenceGlyphs()) {
            for (SpeciesReferenceGlyph srg : rg.getListOfSpeciesReferenceGlyphs()) {
              if (srg.isSetSpeciesGlyph()) {
                SpeciesGlyph sg = srg.getSpeciesGlyphInstance();
                if (sg.getUserObject(SPECIES_GLYPH_LINK) == null) {
                  sg.putUserObject(SPECIES_GLYPH_LINK, new TreeSet<String>());
                }
                ((Set<String>) sg.getUserObject(SPECIES_GLYPH_LINK)).add(rg.getId());
              }
            }
          }
        }
        // build links from species to corresponding species glyphs across all layouts
        for (int j = 0; j < layout.getSpeciesGlyphCount(); j++) {
          SpeciesGlyph sg = layout.getSpeciesGlyph(j);
          if (sg.isSetSpecies()) {
            Species species = model.getSpecies(sg.getSpecies());
            if (species.getUserObject(SPECIES_GLYPH_LINK) == null) {
              species.putUserObject(SPECIES_GLYPH_LINK, new TreeSet<String>());
            }
            ((Set<String>) species.getUserObject(SPECIES_GLYPH_LINK)).add(sg.getId());
          }
        }
        for (int j = 0; j < layout.getTextGlyphCount(); j++) {
          TextGlyph tg = layout.getTextGlyph(j);
          if (tg.isSetOriginOfText()) {
            Species s = model.getSpecies(tg.getOriginOfText());
            if (s != null) {
              if (s.getUserObject(TEXT_GLYPH_LINK) == null) {
                s.putUserObject(TEXT_GLYPH_LINK, new TreeSet<String>());
              }
              ((Set<String>) s.getUserObject(TEXT_GLYPH_LINK)).add(tg.getId());
            }
          }
        }
      }
    }
  }
  
  /**
   * 
   */
  public void removeUnconnectedSpeciesGlyphs() {
    Model model = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if (lmp != null) {
      for (int i = 0; i < lmp.getLayoutCount(); i++) {
        Layout layout = lmp.getLayout(i);
        for (int j = layout.getSpeciesGlyphCount() - 1; j >= 0; j--) {
          SpeciesGlyph sg = layout.getSpeciesGlyph(j);
          //if (sg.getUserObject(SPECIES_GLYPH_LINK) == null) {
          if (sg.getSpeciesInstance() == null) {
            logger.info(format("Removing species glyph with id=''{0}''", layout.removeSpeciesGlyph(j).getId()));
          }
        }
        for (int j = layout.getTextGlyphCount() - 1; j >= 0; j--) {
          TextGlyph tg = layout.getTextGlyph(j);
          if (tg.isSetGraphicalObject() && !tg.isSetGraphicalObjectInstance()) {
            if (!tg.isSetText() && tg.isSetOriginOfText()) {
              NamedSBase nsb = tg.getReferenceInstance();
              if (nsb == null) {
                tg.setText(id2name.get(tg.getReference()));
              } else if ((nsb.getUserObject(SPECIES_REACTION_LINK) == null) && (nsb instanceof Species)) {
                tg.setText(nsb.isSetName() ? nsb.getName() : nsb.getId());
              }
              tg.unsetReference();
              
              // Adjust positioning of the text
              BoundingBox bbox = tg.getBoundingBox();
              Point pos = bbox.getPosition();
              Dimensions dim = bbox.getDimensions();
              double width = tg.getText().length() * 20d; // experimental values...
              pos.setX(pos.x() + dim.getWidth() / 2d - width / 2d);
              pos.setY(pos.y() + dim.getHeight() * 1d/4d);
              dim.setWidth(width * 3d/4d);
            }
            tg.unsetGraphicalObject();
          }
        }
      }
    }
  }
  
  /**
   * 
   */
  public void removeUnconnectedSpecies() {
    Model model = doc.getModel();
    for (int i = model.getSpeciesCount() - 1; i >= 0; i--) {
      Species s = model.getSpecies(i);
      if (s.getUserObject(SPECIES_REACTION_LINK) == null) {
        id2name.put(s.getId(), s.getName());
        logger.info(format("Removing species with id=''{0}''", model.removeSpecies(i).getId()));
      }
    }
  }
  
  /**
   * 
   * @param species2reaction
   * @param rId
   * @param participants
   */
  @SuppressWarnings("unchecked")
  private void fillMap(String rId, ListOf<? extends SimpleSpeciesReference> participants) {
    for (int j = 0; j < participants.size(); j++) {
      SimpleSpeciesReference ssr = participants.get(j);
      if (ssr.isSetSpecies()) {
        Species species = ssr.getSpeciesInstance();
        if (species.getUserObject(SPECIES_REACTION_LINK) == null) {
          species.putUserObject(SPECIES_REACTION_LINK, new TreeSet<String>());
        }
        ((Set<String>) species.getUserObject(SPECIES_REACTION_LINK)).add(rId);
      }
    }
  }
  
  /**
   * 
   * @param sbase
   */
  public void trimCellDesignerAnnotation(SBase sbase) {
    Annotation a = sbase.getAnnotation();
    if (a.isSetNonRDFannotation()) {
      a.unsetNonRDFannotation();
    }
    for (int i = 0; i < sbase.getChildCount(); i++) {
      TreeNode tn = sbase.getChildAt(i);
      if (tn instanceof SBase) {
        trimCellDesignerAnnotation((SBase) tn);
      }
    }
  }
  
}
