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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileFilter;

import y.base.Node;
import y.io.JPGIOHandler;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.router.OrganicEdgeRouter;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import de.zbit.graph.GraphTools;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.graph.gui.options.GraphBackgroundImageProvider;
import de.zbit.graph.gui.options.TranslatorPanelOptions;
import de.zbit.graph.io.Graph2Dwriteable;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.graph.io.Graph2Dwriter.writeableFileExtensions;
import de.zbit.gui.GUITools;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.NotifyingWorker;
import de.zbit.util.prefs.SBPreferences;

/**
 * This abstract class should be used for all output formats, that want to
 * get visualized as Graph. This builds a graph layer between the
 * actual output format and Graph, so your actual output format can be visualized
 * as Graph.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class TranslatorGraphLayerPanel <DocumentType> extends TranslatorPanel<DocumentType> {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3437289245211176473L;
  
  /**
   * This is the class in which required options are stored. If you
   * extend {@link TranslatorPanelOptions} class with your own one, you
   * need to overwrite this variable here!
   */
  public static Class<? extends TranslatorPanelOptions> optionClass = TranslatorPanelOptions.class;
  
  /**
   * Please NEVER use this variable to check if the detail panel is available (also NOT internally
   * in this class). The correct way to set this option is overwriting the {@link #isDetailPanelAvailable()}
   * method. Hence, the correct way to check if the panel is available is calling the
   * {@link #isDetailPanelAvailable()} method.
   */
  private final boolean showDetailedPanel;
  
  /**
   * The current graph layer
   */
  Graph2D graphLayer;
  
  /**
   * The current pane on which the {@link #graphLayer} is visualized.
   */
  Graph2DView pane;
  
  /**
   * @return the pane
   */
  public Graph2DView getGraph2DView() {
    return pane;
  }
  
  /**
   * This allows extending classes to build a panel with detailed
   * information that is shown on node-selection.
   */
  private JScrollPane detailPanel=null;
  
  /**
   * Thread that updates the detail panel.
   */
  private Thread detailPanelUpdater;
  
  /**
   * An optional {@link GraphBackgroundImageProvider} that will draw
   * any image as background image of this {@link #graphLayer} or
   * the corresponding panel.
   */
  private GraphBackgroundImageProvider imageProvider = null;
  
  
  /**
   * @return all available output file formats for GraphML files.
   */
  public static List<SBFileFilter> getGraphMLfilefilter() {
    LinkedList<SBFileFilter> ff = new LinkedList<SBFileFilter>();
    ff.add(SBFileFilter.createGraphMLFileFilter());
    ff.add(SBFileFilter.createGMLFileFilter());
    ff.add(SBFileFilter.createJPEGFileFilter());        
    ff.add(SBFileFilter.createGIFFileFilter());
    ff.add(SBFileFilter.createYGFFileFilter());
    ff.add(SBFileFilter.createTGFFileFilter());
    if (GraphTools.isSVGextensionInstalled()) {
      ff.add(SBFileFilter.createSVGFileFilter());
    }
    return ff;
  }
  
  
  //  /**
  //   * Create a new translator-panel and initiates the translation.
  //   * @param inputFile
  //   * @param outputFormat
  //   * @param translationResult
  //   */
  //  public TranslatorGraphLayerPanel(File inputFile, ActionListener translationResult) {
  //    this(inputFile, "JPG", translationResult);
  //  }
  //
  //  /**
  //   * Create a new translator-panel and initiates the translation.
  //   * @param inputFile
  //   * @param outputFormat
  //   * @param translationResult
  //   */
  //  public TranslatorGraphLayerPanel(File inputFile, String outputFormat,
  //    ActionListener translationResult) {
  //    super(inputFile, outputFormat, translationResult);
  //  }
  //
  //  /**
  //   * Initiates a download and translation of the given pathway.
  //   * @param pathwayID pathway identifier (e.g., "mmu00010")
  //   * @param outputFormat
  //   * @param translationResult
  //   */
  //  public TranslatorGraphLayerPanel(String pathwayID, String outputFormat,
  //    ActionListener translationResult) {
  //    super(pathwayID, outputFormat, translationResult);
  //  }
  //  
  //  /**
  //   * Initiates a download and translation of the given pathway.
  //   * @param pathwayID
  //   * @param translationResult
  //   */
  //  public TranslatorGraphLayerPanel(String pathwayID, ActionListener translationResult) {
  //    this(pathwayID, "JPG", translationResult);
  //  }
  
  /**
   * Use this constructor if the document has already been translated. This
   * constructor does not call {@link #createTabContent()}.
   * 
   * @param inputFile
   * @param outputFormat
   * @param translationResult
   * @param translatedDocument
 * @param showDetailedPanel 
   * @throws Exception
   */
  protected TranslatorGraphLayerPanel(final File inputFile,
    final String outputFormat, ActionListener translationResult,
    DocumentType translatedDocument, boolean showDetailedPanel) {
    super(inputFile, outputFormat, translationResult, translatedDocument);
    this.showDetailedPanel = showDetailedPanel;
  }
  
  /**
   * 
   * @param inputFile
   * @param outputFormat
   * @param translationResult
   * @param translatedDocument
   */
  protected TranslatorGraphLayerPanel(final File inputFile,
		    final String outputFormat, ActionListener translationResult,
		    DocumentType translatedDocument) {
	  this(inputFile, outputFormat, translationResult, translatedDocument, true);
  }
  
  /**
   * Initiates a download and translation of the given pathway.
   * @param downloadORTranslateWorker
   * @param outputFormat
   * @param translationResult
   */
  public TranslatorGraphLayerPanel(NotifyingWorker<?, ?> downloadORTranslateWorker, 
    final String outputFormat, ActionListener translationResult) {
    super(downloadORTranslateWorker, outputFormat, translationResult);
    this.showDetailedPanel = true;
  }
  
  /**
   * Initiates a translation of the given pathway.
   * @param downloadORTranslateWorker
   * @param outputFormat
   * @param translationResult
   */
  public TranslatorGraphLayerPanel(NotifyingWorker<?, ?> downloadORTranslateWorker, 
    File inputFile, String outputFormat, ActionListener translationResult) {
    super(downloadORTranslateWorker, inputFile, outputFormat, translationResult);
    this.showDetailedPanel = true;
  }
  
  
  /**
   * 
   * @param imageProvider
   */
  public void setBackgroundImageProvider(GraphBackgroundImageProvider imageProvider) {
    this.imageProvider = imageProvider;
    if (graphLayer!=null && isSetBackgroundImageProvider()) {
      try {
        getBackgroundImageProvider().addBackgroundImage(pane, getTranslator());
      } catch (MalformedURLException e) {
        GUITools.showErrorMessage(this, e);
      }
    }
  }
  
  /**
   * 
   * @return
   */
  public GraphBackgroundImageProvider getBackgroundImageProvider() {
    return imageProvider;
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetBackgroundImageProvider() {
    return imageProvider!=null;
  }
  
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    // Also enable the yFiles views
    try {
      if (graphLayer!=null) GraphTools.enableViews(graphLayer,enabled);
    } catch (Throwable e) {}
  }
  
  /* (non-Javadoc)
   * @see java.awt.Component#repaint()
   */
  @Override
  public void repaint() {
    super.repaint();
    if (!isReady()) return;
    
    // Update graph
    // updateViews() does not update, but clear the visualization
    // strange thing...
    //((Graph2D)document).updateViews();
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorPanel#createTabContent()
   */
  @Override
  public void createTabContent() throws Exception {
    graphLayer = createGraphFromDocument(document);
    
    /*
     * Get settings to control visualization behavior
     */
    SBPreferences prefs = SBPreferences.getPreferencesFor(optionClass);
    
    // If all coordinates are at the same position, make some automatic layout
    if (allNodesAtSamePosition(graphLayer)) {
      log.info("Layouting graph with SmartOrganicLayouter.");
      new GraphTools(graphLayer).layout(SmartOrganicLayouter.class);
      graphLayer.unselectAll();
    }
    
    if (TranslatorPanelOptions.LAYOUT_EDGES.getValue(prefs)) {
      new GraphTools(graphLayer).layout(OrganicEdgeRouter.class);
    }
    
    // After all the layouting, maybe an extending class would like to do
    // some post-processing...
    finalize(graphLayer);
    
    // Create a new visualization of the model.
    pane = new Graph2DView(graphLayer);
    
    if (isDetailPanelAvailable()) {
      //Create a split pane
      detailPanel = new JScrollPane();
      updateDetailPanel(detailPanel, null); // Build initial panel
      
      // Set a minimum size if we use the split pane
      Dimension minimumSize = new Dimension( (int)Math.max(pane.getMinimumSize().getWidth(), 100), (int)Math.max(pane.getMinimumSize().getHeight(), getHeight()/2) );
      pane.setMinimumSize(minimumSize);
      pane.setPreferredSize(new Dimension(100, (int) Math.max(getHeight()*0.6, 50)));
      detailPanel.setMinimumSize(new Dimension(100,50));
      detailPanel.setPreferredSize(new Dimension(100, (int)Math.max(getHeight()*0.4, 50)));
      detailPanel.setSize(detailPanel.getPreferredSize());
      
      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane, detailPanel);
      splitPane.setOneTouchExpandable(false);
      splitPane.setResizeWeight(0.8); // Make graph max visible
      
      
      add(splitPane);
    } else {
      add(pane);
    }
    
    
    // Draw edges on top or below nodes
    if (pane.getGraph2DRenderer() instanceof DefaultGraph2DRenderer) {
      if (TranslatorPanelOptions.DRAW_EDGES_ON_TOP_OF_NODES.getValue(prefs)) {
        ((DefaultGraph2DRenderer) pane.getGraph2DRenderer()).setDrawEdgesFirst(false);
      } else {
        ((DefaultGraph2DRenderer) pane.getGraph2DRenderer()).setDrawEdgesFirst(true);
      }
    }
    
    // Make group nodes collapsible.
    // Unfortunately work-in-progress.
    //pane.addViewMode(new CollapseGroupNodesViewMode((Graph2D) graphLayer));
    
    // Set KEGGtranslator logo as background
    if (isSetBackgroundImageProvider()) {
      getBackgroundImageProvider().addBackgroundImage(pane, getTranslator());
    }
    
    //--
    // Show Navigation and Overview
    if (TranslatorPanelOptions.SHOW_NAVIGATION_AND_OVERVIEW_PANELS.getValue(prefs)) {
      RestrictedEditMode.addOverviewAndNavigation(pane);
    }
    //--
    
    pane.setSize(getSize());
    //ViewMode mode = new NavigationMode();
    //pane.addViewMode(mode);
    ActionListener listener = getUIActionListener();
    
    EditMode editMode = new RestrictedEditMode(listener, this);
    editMode.showNodeTips(true);
    pane.addViewMode(editMode);
    
    if (TranslatorPanelOptions.SHOW_PROPERTIES_TABLE.getValue(prefs)) {
      ((RestrictedEditMode)editMode).addPropertiesTable(pane);
    }
    
    pane.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      pane.fitContent(true);
    } catch (Throwable t) {} // Not really a problem
    pane.setFitContentOnResize(true);
  }
  
  
  /**
   * Method might be implemented by extending classes to do 
   * some post-processing after the graph has been layouted
   * completely.
   * @param graphLayer
   */
  private void finalize(Graph2D graphLayer) {
    // Intentionally left blank
  }


  /**
   * Check if all nodes lay at the same X position.
   * @param graph
   * @return {@code true} if all nodes in the graph
   * have the same center X coordinate.
   */
  private static boolean allNodesAtSamePosition(Graph2D graph) {
    double centerX = Double.NaN;
    boolean isAllOnCenterX = true;
    double X = Double.NaN;
    boolean isAllOnX = true;
    for (Node n : graph.getNodeArray()) {
      NodeRealizer re = graph.getRealizer(n);
      if (re instanceof GroupNodeRealizer) {
        // They somehow have a different default center than other nodes...
        continue;
      }
      
      if (Double.isNaN(centerX)) {
        // Initial set
        centerX = re.getCenterX();
      } else if (re.getCenterX()!=centerX) {
        isAllOnCenterX = false;
      }
      
      if (Double.isNaN(X)) {
        // Initial set
        X = re.getX();
      } else if (re.getX()!=X) {
        isAllOnX = false;
      }
      
      if (!isAllOnCenterX && !isAllOnX) {
        break;
      }
    }
    
    return isAllOnX || isAllOnCenterX;
  }
  
  
  /**
   * Please see {@link #updateDetailPanel(JPanel, HitInfo)}.
   * Please USE this method, BUT overwrite {@link #updateDetailPanel(JScrollPane, HitInfo)}!
   * @param hitInfo
   * @see #updateDetailPanel(JPanel, HitInfo)
   */
  public void updateDetailPanel(final HitInfo hitInfo) {
    if (detailPanelUpdater!=null && !detailPanelUpdater.getState().equals(Thread.State.TERMINATED)) {
      detailPanelUpdater.interrupt();
    }
    
    // Set temporary progress bar
    JProgressBar prog = new JProgressBar();
    prog.setIndeterminate(true);
    final JPanel p = new JPanel();
    p.add(prog);
    detailPanel.setViewportView(p);
    
    // Update panel
    Runnable buildDetailPanel = new Runnable() {
      public void run() {
        updateDetailPanel(detailPanel, hitInfo);
        if (Thread.currentThread().isInterrupted()) return;
        // If it did not change, simply remove the temporaray progress bar
        if (detailPanel.getViewport().getView()!=null &&
            detailPanel.getViewport().getView().equals(p)) {
          detailPanel.setViewportView(null);
        }
        detailPanel.validate();
        detailPanel.repaint();
      }
    };
    
    detailPanelUpdater = new Thread(buildDetailPanel);
    detailPanelUpdater.start();
  }
  
  /**
   * Only if {@link #isDetailPanelAvailable()}, update the {@link #detailPanel}
   * to match current selection.
   * <p><code>clickedObjects</code> might explicitly be {@code null}
   * if nothing is selected, so please implement this method accordingly.
   * 
   * @param detailPanel
   * @param clickedObjects
   */
  protected void updateDetailPanel(JScrollPane detailPanel, HitInfo clickedObjects) {
    // Detail panel is disabled by default.
  }
  
  
  /**
   * Return true if not only the graph, but also a detail panel
   * that is activated on node-click should be visualized.
   * 
   * <p>This allows extending classes to build a panel with detailed
   * information that is shown on node-selection.
   * <p>Please overwrite this method to match your needs.
   * @return {@code true} if a split pane with more details for
   * a node should be introduced, {@code false} if only the
   * graph should get visualized.
   */
  public boolean isDetailPanelAvailable() {
    return showDetailedPanel;
  }
  
  
  /**
   * Convert the given document to a visualizable graph file.
   * @param document
   * @return
   */
  protected abstract Graph2D createGraphFromDocument(DocumentType document);
  
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorPanel#getOutputFileFilter()
   */
  @Override
  protected List<FileFilter> getOutputFileFilter() {
    List<FileFilter> ff = getOutputFileFilterForRealDocument();
    if (ff==null) ff = new LinkedList<FileFilter>();
    if (isAllowedToSaveAsGraphFormats()) {
      ff.addAll(getGraphMLfilefilter());
    }
    return ff;
  }
  
  /**
   * @return true if the users should be able to save this as graph formats,
   * in addition to the {@link #getOutputFileFilterForRealDocument()}.
   */
  public boolean isAllowedToSaveAsGraphFormats() {
    return true;
  }
  
  /**
   * Create all file filters that are available to save this
   * tabs content. The first in the list is assumed to be
   * the default file filter.
   * @return
   */
  protected abstract List<FileFilter> getOutputFileFilterForRealDocument();
  
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.gui.TranslatorPanel#writeToFileUnchecked(java.io.File, java.lang.String)
   */
  @Override
  protected boolean writeToFileUnchecked(File file, String format) throws Exception {
    
    // is it a Graph format?
    boolean isGraphFormat = false;
    if (isAllowedToSaveAsGraphFormats()) {
      try {
        isGraphFormat = Graph2Dwriter.writeableFileExtensions.valueOf(format.toLowerCase().trim())!=null;
      } catch (Exception e) {
        isGraphFormat = false;
      }
    }
    
    // Write graph formatted file
    if (isGraphFormat) {
      writeableFileExtensions f = null;
      try {
        f = Graph2Dwriter.writeableFileExtensions.valueOf(format);
      } catch (Exception e) {
        f=null;
      }
      
      // Get writer
      Graph2Dwriteable trans2;
      if (getTranslator() instanceof Graph2Dwriteable) {
        trans2 = (Graph2Dwriteable) getTranslator();
      } else if (f!=null) {
        trans2 = new Graph2Dwriter(f, getTranslator());
      } else {
        trans2 = new Graph2Dwriter(new JPGIOHandler(), getTranslator());
      }
      
      // Eventually setup background
      if (isSetBackgroundImageProvider() && (trans2 instanceof Graph2Dwriter)) {
        ((Graph2Dwriter) trans2).setBackgroundImageProvider(getBackgroundImageProvider());
      }
        
      return ((Graph2Dwriteable)trans2).writeToFile(graphLayer, file.getPath(), format);
      
    } else {
      
      return writeRealDocumentToFileUnchecked(file, format);
    }
  }
  
  /**
   * Invoke the file write. All checks have already been made and also
   * a message of success/ failure is sent by other methods. Simply
   * write to the file in this method here.
   * @param file
   * @param format
   * @return true if everything went ok, false else.
   * @throws Exception
   */
  protected abstract boolean writeRealDocumentToFileUnchecked(File file, String format) throws Exception;
  
  
  
}
