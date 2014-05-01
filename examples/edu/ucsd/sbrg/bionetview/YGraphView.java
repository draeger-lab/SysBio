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
package edu.ucsd.sbrg.bionetview;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.SwingWorker.StateValue;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.util.Pair;
import org.sbml.jsbml.util.StringTools;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.ImageIoOutputHandler;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import de.zbit.AppConf;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable;
import de.zbit.io.OpenedFile;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.util.Utils;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class YGraphView extends BaseFrame {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(YGraphView.class.getName());
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -963737478725297232L;
  
  /**
   * Maintains all opened files in a {@link List}.
   */
  private List<OpenedFile<SBMLDocument>> listOfOpenedFiles;
  
  /**
   * The main component inside of this window.
   */
  private JTabbedPaneDraggableAndCloseable tabbedPane;
  
  /**
   * 
   * @param appConf
   */
  public YGraphView(AppConf appConf) {
    super(appConf);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#closeFile()
   */
  @Override
  public boolean closeFile() {
    // TODO Auto-generated method stub
    return false;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createJToolBar()
   */
  @Override
  protected JToolBar createJToolBar() {
    return createDefaultToolBar();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createMainComponent()
   */
  @Override
  protected Component createMainComponent() {
    tabbedPane = new JTabbedPaneDraggableAndCloseable();
    return tabbedPane;
  }
  
  /**
   * Run LayoutDirector and create product
   * 
   * @param layoutPlugin
   */
  private void display(int index, LayoutModelPlugin layoutPlugin) {
    LayoutDirectionTask layoutTask = new LayoutDirectionTask(layoutPlugin, this);
    @SuppressWarnings("unchecked")
    final BioNetViewPanel panel = new BioNetViewPanel();
    Model model = layoutPlugin.getModel();
    String title = model.isSetName() ? model.getName() : model.getId();
    if ((title == null) || (title.trim().length() == 0)) {
      title = listOfOpenedFiles.get(index).getFile().getName();
    }
    tabbedPane.add(panel, index);
    tabbedPane.setTitleAt(index, title);
    layoutTask.addPropertyChangeListener(new PropertyChangeListener() {
      /* (non-Javadoc)
       * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
       */
      @Override
      @SuppressWarnings("unchecked")
      public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() instanceof LayoutDirectionTask) && (evt.getPropertyName().equals(LayoutDirectionTask.GRAPH_LAYOUT_DONE))) {
          for (Pair<String, Graph2D> pair : (List<Pair<String, Graph2D>>) evt.getNewValue()) {
            panel.add(pair);
          }
        }
      }
    });
    layoutTask.execute();
  }
  
  /**
   * 
   * @param openedFile
   */
  private void display(OpenedFile<SBMLDocument> openedFile) {
    SBMLDocument doc = openedFile.getDocument();
    if (doc == null) {
      logger.warning("No SBML document given.");
    }
    
    Model model = doc.getModel();
    LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));
    
    // Generate glyphs for SBML documents without layout information
    final int index = listOfOpenedFiles.size() - 1;
    if (layoutPlugin == null) {
      logger.info("Model does not contain layouts, creating glyphs for every object.");
      GlyphCreationTask creation = new GlyphCreationTask(doc, this);
      final YGraphView parent = this;
      creation.addPropertyChangeListener(new PropertyChangeListener() {
        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName().equals(StateValue.DONE)) {
            GlyphCreationTask task = (GlyphCreationTask) evt.getSource();
            try {
              display(index, task.get());
            } catch (Throwable exc) {
              GUITools.showErrorMessage(parent, Utils.getMessage(exc));
            }
          }
        }
      });
      creation.execute();
    } else {
      display(index, layoutPlugin);
    }
  }
  
  /**
   * Write a textual representation of the graph (all nodes and edges
   * including realizer information) to standard out for debugging purposes.
   */
  private void dumpGraph(Graph2D graph) {
    NodeCursor nodeCursor = graph.nodes();
    System.out.println("Nodes:");
    for (; nodeCursor.ok(); nodeCursor.next()) {
      Node n = (Node) nodeCursor.current();
      NodeRealizer nr = graph.getRealizer(n);
      NodeLabel nodeLabel = nr.getLabel();
      System.out.println(n);
      System.out.println("  " + graph.getRealizer(n));
      System.out.println(nodeLabel.toString());
    }
    System.out.println("Edges:");
    EdgeCursor edgeCursor = graph.edges();
    for (; edgeCursor.ok(); edgeCursor.next()) {
      Edge e = (Edge) edgeCursor.current();
      System.out.println(e);
      System.out.println("  " + prettyPrintEdgeRealizer(graph.getRealizer(e)));
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLAboutMessage()
   */
  @Override
  public URL getURLAboutMessage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLLicense()
   */
  @Override
  public URL getURLLicense() {
    return appConf.getLicenceFile();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLOnlineHelp()
   */
  @Override
  public URL getURLOnlineHelp() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
   */
  @Override
  protected File[] openFile(File... files) {
    if ((files == null) || (files.length == 0)) {
      SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
      files = GUITools.openFileDialog(
        this,
        prefs.get(OPEN_DIR),
        false,
        true,
        JFileChooser.FILES_ONLY,
        SBFileFilter.createSBMLFileFilterList());
    }
    if (files != null) {
      for (File file : files) {
        try {
          SBMLReadingTask readingTask = new SBMLReadingTask(file, this, this);
          readingTask.execute();
        } catch (FileNotFoundException exc) {
          exc.printStackTrace();
        }
      }
    }
    return files;
  }
  
  /**
   * @param realizer
   * @return a textual representation of an edge realizer
   */
  private String prettyPrintEdgeRealizer(EdgeRealizer r) {
    return StringTools.concat(r.getClass().getSimpleName(),
      " [sourcePoint=", r.getSourcePoint(),
      ", targetPoint=", r.getTargetPoint(),
        "]").toString();
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
      OpenedFile<SBMLDocument> openedFile = (OpenedFile<SBMLDocument>) evt.getNewValue();
      if (listOfOpenedFiles == null) {
        listOfOpenedFiles = new LinkedList<OpenedFile<SBMLDocument>>();
      }
      listOfOpenedFiles.add(openedFile);
      display(openedFile);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#saveFileAs()
   */
  @Override
  public File saveFileAs() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Write image file (png) of the graph.
   * 
   * @param graph
   * @param outFile path of the output file
   */
  private void writeImage(Graph2D graph, String outFile) {
    Iterator<javax.imageio.ImageWriter> iterator =
        javax.imageio.ImageIO.getImageWritersBySuffix("png");
    javax.imageio.ImageWriter imageWriter =
        iterator.hasNext() ? iterator.next() : null;
        
        if (imageWriter != null) {
          Graph2Dwriter graph2Dwriter =
              new Graph2Dwriter(new ImageIoOutputHandler(imageWriter));
          graph2Dwriter.writeToFile(graph, outFile);
          logger.info(MessageFormat.format("Image written to ''{0}''.", outFile));
        }
        else {
          logger.warning("Could not write image: ImageWriter not available.");
        }
  }
  
}
