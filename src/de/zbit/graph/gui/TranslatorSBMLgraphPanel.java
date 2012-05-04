/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of KEGGtranslator, a program to convert KGML files
 * from the KEGG database into various other formats, e.g., SBML, GML,
 * GraphML, and many more. Please visit the project homepage at
 * <http://www.cogsys.cs.uni-tuebingen.de/software/KEGGtranslator> to
 * obtain the latest version of KEGGtranslator.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * KEGGtranslator is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.groups.GroupModel;
import org.sbml.jsbml.ext.qual.QualConstant;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.xml.parsers.GroupsParser;

import y.base.Edge;
import y.view.Graph2D;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import de.zbit.graph.GraphTools;
import de.zbit.graph.io.SBML2GraphML;
import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.kegg.io.KEGGtranslator;
import de.zbit.sbml.gui.SBasePanel;
import de.zbit.util.NotifyingWorker;

/**
 * A basic panel which uses a GraphLayer to visualize SBML documents.
 * SBML-visualization is performed according to the SBGN
 * process descriptions.<br/>
 * <img src="http://www.sbgn.org/images/4/4a/Refcard-PD.png"/>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class TranslatorSBMLgraphPanel extends TranslatorGraphLayerPanel<SBMLDocument> {
  private static final long serialVersionUID = 2361032893527709646L;

  /**
   * The converter we used to generate the GraphML document
   * from our {@link SBMLDocument}.
   */
  private SBML2GraphML converter = null;
  
  /**
   * If false, shows the normal, quantitative SBML model.
   * If true, shows the qual model.
   */
  private boolean showQualModel=false;

//  /**
//   * Create a new translator-panel and initiates the translation.
//   * @param inputFile
//   * @param outputFormat
//   * @param translationResult
//   */
//  public TranslatorSBMLgraphPanel(File inputFile, ActionListener translationResult) {
//    this(inputFile, "SBML", translationResult);
//  }

//  /**
//   * Create a new translator-panel and initiates the translation.
//   * @param inputFile
//   * @param outputFormat
//   * @param translationResult
//   */
//  public TranslatorSBMLgraphPanel(File inputFile, String outputFormat, ActionListener translationResult) {
//    super(inputFile, outputFormat, translationResult);
//  }
//
//  /**
//   * Initiates a download and translation of the given pathway.
//   * @param pathwayID pathway identifier (e.g., "mmu00010")
//   * @param outputFormat
//   * @param translationResult
//   */
//  public TranslatorSBMLgraphPanel(String pathwayID, String outputFormat, ActionListener translationResult) {
//    super(pathwayID, outputFormat, translationResult);
//  }
//  
//  /**
//   * Initiates a download and translation of the given pathway.
//   * @param pathwayID pathway identifier (e.g., "mmu00010")
//   * @param translationResult
//   */
//  public TranslatorSBMLgraphPanel(String pathwayID, ActionListener translationResult) {
//    this(pathwayID, "SBML", translationResult);
//  }
//  
//  public TranslatorSBMLgraphPanel(File inputFile, String outputFormat, ActionListener translationResult, SBMLDocument document) {
//    this(inputFile, outputFormat, translationResult, document, false);
//  }

public TranslatorSBMLgraphPanel(File inputFile, String outputFormat, ActionListener translationResult, SBMLDocument document, boolean showQualModel) {
    super(inputFile, outputFormat, translationResult, document);
    this.showQualModel = showQualModel;
    
    try {
      createTabContent();
    } catch (Throwable e) {
      GUITools.showErrorMessage(null, e);
      fireActionEvent(new ActionEvent(this, JOptionPane.ERROR, COMMAND_TRANSLATION_DONE));
      return;
    }
  }
  
  /**
   * Visualizes the given {@link SBMLDocument}.
   * @param document
   * @param showQualModel
   */
  public TranslatorSBMLgraphPanel (SBMLDocument document, boolean showQualModel) {
    this((File) null, "SBML", null, document, showQualModel);
  }
  
  /**
   * Initiates a download and translation of the given pathway.
   * @param downloadORTranslateWorker
   * @param outputFormat
   * @param translationResult
   */
  public TranslatorSBMLgraphPanel(NotifyingWorker<?, ?> downloadORTranslateWorker, 
    final String outputFormat, ActionListener translationResult) {
    super(downloadORTranslateWorker, outputFormat, translationResult);
  }
  
  /**
   * Initiates a translation of the given pathway.
   * @param downloadORTranslateWorker
   * @param outputFormat
   * @param translationResult
   */
  public TranslatorSBMLgraphPanel(NotifyingWorker<?, ?> downloadORTranslateWorker, 
    File inputFile, String outputFormat, ActionListener translationResult) {
    super(downloadORTranslateWorker, inputFile, outputFormat, translationResult);
  }



  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorGraphLayerPanel#createGraphFromDocument(java.lang.Object)
   */
  @Override
  protected Graph2D createGraphFromDocument(SBMLDocument document) {
    converter = new SBML2GraphML(showQualModel);
    return converter.createGraph(document);
  }

  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorGraphLayerPanel#getOutputFileFilterForRealDocument()
   */
  @Override
  protected List<FileFilter> getOutputFileFilterForRealDocument() {
    List<FileFilter> ff = new LinkedList<FileFilter>();
    ff.add(SBFileFilter.createSBMLFileFilter());
    return ff;
  }


  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorGraphLayerPanel#writeRealDocumentToFileUnchecked(java.io.File, java.lang.String)
   */
  @Override
  protected boolean writeRealDocumentToFileUnchecked(File file, String format)
    throws Exception {
//    if (SBFileFilter.isTeXFile(file) || SBFileFilter.isPDFFile(file) || format.equals("tex") || format.equals("pdf")) {
      // TODO: How to include SBML2LaTeX?
//      TranslatorSBMLPanel.writeLaTeXReport(file, document);
//      return true; // Void result... can't check.
//    } else {
      return ((KEGGtranslator<SBMLDocument>)getTranslator()).writeToFile(document, file.getPath());
//    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorGraphLayerPanel#isDetailPanelAvailable()
   */
  @Override
  public boolean isDetailPanelAvailable() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorGraphLayerPanel#updateDetailPanel(javax.swing.JPanel, y.view.HitInfo)
   */
  @Override
  protected void updateDetailPanel(final JScrollPane detailPanel, HitInfo clickedObjects) {
    
    if (clickedObjects==null || !clickedObjects.hasHits()) {
      synchronized (detailPanel) {
        ((JScrollPane) detailPanel).setViewportView(null);
      }
    } else {
      Set<Object> hits = GraphTools.getHitEdgesAndNodes(clickedObjects, true);
      JPanel p = new JPanel(); // Gridlayout makes always same height...
      LayoutHelper lh = new LayoutHelper(p);
      for (Object nodeOrEdge: hits) {
        if (Thread.currentThread().isInterrupted()) return;
        
        // Try to get actual SBML-element
        String sbmlID = null;
        if (converter!=null && converter.getGraphElement2SBid()!=null) {
          sbmlID = converter.getGraphElement2SBid().get(nodeOrEdge);
        }
        SBase base = null;
        if (sbmlID!=null) {
          
          // First, try group (shared with core and qualitative modeling)
          SBasePlugin gm = document.getModel().getExtension(GroupsParser.namespaceURI);
          if (gm!=null && gm instanceof GroupModel) {
            base = ((GroupModel)gm).getGroup(sbmlID);
          }
          
          if (base==null) {
            if (!showQualModel) {
              // Try metabolic (core) model
              base = document.getModel().getSpecies(sbmlID);
              if (base==null ){
                base = document.getModel().getReaction(sbmlID);
              }
            } else {
              // Try qualitative (qual) model
              SBasePlugin qm = document.getModel().getExtension(QualConstant.namespaceURI);
              if (qm!=null && qm instanceof QualitativeModel) {
                QualitativeModel q = (QualitativeModel) qm;
                base = q.getQualitativeSpecies(sbmlID);
                if (base==null ){
                  try {
                    base = q.getTransition(sbmlID);
                  } catch (Exception e) {};
                }
              }
            }
          }
        }
        
        // Add a detail panel if we have the element.
        if (base!=null) {
          try {
            lh.add(new SBasePanel(base, true), true);
          } catch (Exception e) {
            log.log(Level.WARNING, "Could not create detail panel.", e);
          }
        }
      }
      
      // Add final panel
      if (Thread.currentThread().isInterrupted()) return;
      if (p.getComponentCount()>0) {
        synchronized (detailPanel) {
          ((JScrollPane) detailPanel).setViewportView(p);
          
          // Scroll to top.
          GUITools.scrollToTop(detailPanel);
        }
      } else {
        synchronized (detailPanel) {
          ((JScrollPane) detailPanel).setViewportView(null);
        }
      }
    }
    
  }
  
  
  /**
   * =============================================================
   * Experimental work in progress...
   * =============================================================
   * 
   * experimental, do not use
   * @param id
   * @param valueForGraph
   * @param realValue
   * @param labels
   */
  public void dynamicChangeOfNode(String id, double valueForGraph, double realValue, boolean labels){
        NodeRealizer nr = converter.getSimpleGraph().getRealizer(
                converter.getId2node().get(id));
        nr.setSize(valueForGraph, valueForGraph);
        nr.setFillColor(Color.CYAN);

        /*
         * Label Node with ID and real value at this timepoint.
         * Last label will be treated as dynamic label
         * TODO locale
         */
        NumberFormat round = NumberFormat.getInstance();
        round.setMaximumFractionDigits(4); //round to four digits
        if (labels){
            if (nr.labelCount() > 1){
                nr.getLabel(nr.labelCount() - 1).setText(id + ": " + round.format(realValue));
            }else{
                nr.addLabel(new NodeLabel(id + ": " + round.format(realValue)));
                NodeLabel nl = nr.getLabel(nr.labelCount() - 1);
                nl.setModel(NodeLabel.SIDES);
                nl.setPosition(NodeLabel.S); // South of node
                nl.setDistance(-3);
            }
        }else if(nr.labelCount() > 1){
            nr.removeLabel(nr.getLabel(nr.labelCount()-1));
        }
        converter.getSimpleGraph().updateViews();
  }
  
  /**
   * experimental do not use
   * @param id
   * @param value
   */
  public void dynamicChangeOfReaction(String id, double value){
//      System.out.println("start");
//      for (Entry<String, LinkedList<Edge>> entry : converter.getId2edge().entrySet()) {
//          System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//      }
//      System.out.println("ende");
      LinkedList<Edge> listOfEdges = converter.getId2edge().get(id);
//      for(Edge e : listOfEdges){
//          converter.getSimpleGraph().getRealizer(e).setLineColor(Color.green);
//          converter.getSimpleGraph().getRealizer(e).setLineType(LineType.createLineType(arg0, arg1, arg2, arg3, arg4, arg5))
          //TODO create linetypes with specific widths
//      }
//      System.out.println(e);
  }
  
  /**
   * experimental do not use
   * @param id
   */
  public void notSelected(String id){
      if(converter.getId2node().get(id) != null){
          NodeRealizer nr = converter.getSimpleGraph().getRealizer(
                  converter.getId2node().get(id));
          nr.setSize(8, 8);
          nr.setFillColor(Color.GRAY);
          
          if(nr.labelCount() > 1){
              //if not selected disable label
              nr.removeLabel(nr.getLabel(nr.labelCount()-1));
          }
      }
      converter.getSimpleGraph().updateViews();
  }
}
