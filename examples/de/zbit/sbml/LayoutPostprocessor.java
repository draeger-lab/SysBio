/*
 * $IdLayoutPostprocessor.java 10:48:35 draeger $
 * $URLLayoutPostprocessor.java $
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 * 
 * Copyright (C) 2009-2016  jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * 4. The University of California, San Diego, La Jolla, CA, USA
 * 5. The Babraham Institute, Cambridge, UK
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package de.zbit.sbml;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.ProgressMonitorInputStream;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.XMLTools;
import org.sbml.jsbml.util.filters.NameFilter;

import de.zbit.sbml.layout.RenderProcessor;
import de.zbit.sbml.layout.y.YGraphView;
import de.zbit.util.logging.LogUtil;

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
  
  public static void main(String args[]) throws XMLStreamException, SBMLException, IOException {
    LogUtil.initializeLogging("de.zbit");
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
    
    // for testing
    TidySBMLWriter.write(doc, new File(args[1]), ' ', (short) 2);
    new YGraphView(doc);
  }
  
  public void removeUnconnecedStyles() {
    Model model = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if (lmp != null) {
      for (int i = 0; i < lmp.getLayoutCount(); i++) {
        Layout layout = lmp.getLayout(i);
        RenderLayoutPlugin rlp = (RenderLayoutPlugin) layout.getExtension(RenderConstants.shortLabel);
        if ((rlp != null) && rlp.isSetListOfLocalRenderInformation()) {
          ListOf<LocalRenderInformation> listOfLocalRenderInformation = rlp.getListOfLocalRenderInformation();
          for (LocalRenderInformation lri : listOfLocalRenderInformation) {
            if (lri.isSetListOfLocalStyles() && lri.isSetListOfColorDefinitions()) {
              for (int k = lri.getListOfLocalStyles().size() - 1; k >= 0; k--) {
                LocalStyle ls = lri.getListOfLocalStyles().get(k);
                List<String> idList = new LinkedList<>();
                idList.addAll(ls.getIDList());
                for (int j = idList.size() - 1; j >= 0; j--) {
                  String id = idList.get(j);
                  SBase sbase = model.getSBaseById(id);
                  if (sbase == null) {
                    idList.remove(j);
                  }
                }
                if (idList.isEmpty()) {
                  logger.info("Removing local style " + ls.getId());
                  if (ls.isSetGroup() && ls.getGroup().isSetFill()) {
                    String color = ls.getGroup().getFill();
                    logger.info("Deleting Color definition " + color);
                    lri.getListOfColorDefinitions().removeFirst(new NameFilter(color));
                  }
                  lri.removeLocalStyle(k);
                } else if (idList.size() != ls.getIDList().size()) {
                  ls.setIDList(idList);
                }
              }
            }
          }
        }
      }
    }
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
    RenderProcessor.preprocess(layout);
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
                logger.info(MessageFormat.format("Updating SBO term from {0} to {1} in SpeciesReferenceGlyph with id=''{2}''", srg.getSBOTerm(), sbo, srg.getId()));
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
              if (!specRef.isSetSBOTerm() || SBO.isChildOf(sbo, specRef.getSBOTerm())) {
                logger.info(MessageFormat.format("Updating SBO term from {0} to {1} in SpeciesReference with id=''{2}''", specRef.getSBOTerm(), sbo, specRef.getId()));
                specRef.setSBOTerm(sbo);
              }
              if (srg.isSetSBOTerm()) {
                if (SBO.isChildOf(srg.getSBOTerm(), specRef.getSBOTerm())) {
                  logger.info(MessageFormat.format("Updating SBO term from {0} to {1} in SpeciesReference with id=''{2}''", specRef.getSBOTerm(), srg.getSBOTerm(), specRef.getId()));
                  specRef.setSBOTerm(srg.getSBOTerm());
                }
              } else {
                // TODO set SBO term and role of srg appropriately...
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * Recursively removes all non-RDF annotation from the SBML data structure,
   * beginning at the level of the {@link SBMLDocument}
   */
  public void trimCellDesignerAnnotation() {
    trimCellDesignerAnnotation(doc);
  }
  
  private SBMLDocument doc;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(LayoutPostprocessor.class.getName());
  private static final String SPECIES_REACTION_LINK = "SPECIES_REACTION_LINK";
  /**
   * Links speciesGlyphs to reaction glyphs
   */
  private static final String SPECIES_GLYPH_LINK = "SPECIES_GLYPH_LINK";
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
    // build links within each layout from species glyphs to reaction glyphs
    LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
    if (layoutPlugin != null) {
      for (int i = 0; i < layoutPlugin.getLayoutCount(); i++) {
        Layout layout = layoutPlugin.getLayout(i);
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
            logger.info(MessageFormat.format("Removing species glyph with id=''{0}''", layout.removeSpeciesGlyph(j).getId()));
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
        logger.info(MessageFormat.format("Removing species with id=''{0}''", model.removeSpecies(i).getId()));
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
