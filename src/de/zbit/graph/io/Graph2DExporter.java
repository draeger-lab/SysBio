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
package de.zbit.graph.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import y.base.DataMap;
import y.base.DataProvider;
import y.base.EdgeMap;
import y.base.NodeMap;
import y.io.GIFIOHandler;
import y.io.GMLIOHandler;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.io.JPGIOHandler;
import y.io.TGFIOHandler;
import y.io.YGFIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.View;
import de.zbit.graph.gui.options.GraphBackgroundImageProvider;
import de.zbit.graph.io.def.GenericDataMap;
import de.zbit.graph.io.def.GraphMLmaps;
import de.zbit.util.Utils;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class Graph2DExporter implements Graph2Dwriteable {
  
  /**
   * 
   */
  private static final transient Logger logger = Logger.getLogger(Graph2DExporter.class.getName());
  
  public static final String mapDescription = "-MAP_DESCRIPTION-";
  
  protected static Dimension getOutputSize(Rectangle inBox) {
    /*if (outputWidth > 0 && outputHeight > 0) {
      //output completely specified. use it
      return new Dimension((int) outputWidth, (int) outputHeight);
    } else if (outputWidth > 0) {
      //output width specified. determine output height
      return new Dimension(outputWidth,
          (int) (outputWidth * (inBox.getHeight() / inBox.getWidth())));
    } else if (outputHeight > 0) {
      //output height specified. determine output width
      return new Dimension((int) (outputHeight * (inBox.getWidth() / inBox.getHeight())), outputHeight);
    } else { //no output size specified*/
    //no output size specified. use input size, but only if smaller than 1024
    double width = inBox.getWidth();
    double height = inBox.getHeight();
    //scale down if necessary, keeping aspect ratio
    if (width < 1600) {
      height *= 1600 / width;
      width = 1600;
    }
    if (height < 1200) {
      width *= 1200 / height;
      height = 1200;
    }
    return new Dimension((int) width, (int) height);
    //}
  }
  
  @SuppressWarnings("unchecked")
  public static GenericDataMap<DataMap, String> addMapDescriptionMapToGraph(Graph2D graph) {
    GenericDataMap<DataMap, String> mapDescriptionMap = null;
    try {
      mapDescriptionMap = (GenericDataMap<DataMap, String>) graph.getDataProvider(Graph2DExporter.mapDescription);
    } catch (Throwable t) {};
    if (mapDescriptionMap == null) {
      // Actually it is always null ;-)
      mapDescriptionMap = new GenericDataMap<DataMap, String>(Graph2DExporter.mapDescription);
      graph.addDataProvider(Graph2DExporter.mapDescription, mapDescriptionMap);
    }
    return mapDescriptionMap;
  }
  
  /**
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public static IOHandler createSVGIOHandler() {
    try {
      Class<? extends IOHandler> svg = (Class<? extends IOHandler>) Class.forName("yext.svg.io.SVGIOHandler");
      if (svg!=null) {
        return svg.newInstance();
      }
    } catch (Throwable exc) {
      // Extension not installed
    }
    return null;
  }
  
  
  /**
   * 
   * @param ext
   * @return
   * @throws Exception
   */
  public static IOHandler getIOHandler(WriteableFileExtensions ext) throws Exception {
    IOHandler io;
    if (ext.equals(WriteableFileExtensions.gif)) {
      io = new GIFIOHandler();
    } else if (ext.equals(WriteableFileExtensions.graphml)) {
      io = new GraphMLIOHandler();
    } else if (ext.equals(WriteableFileExtensions.gml)) {
      io = new GMLIOHandler();
    } else if (ext.equals(WriteableFileExtensions.ygf)) {
      io = new YGFIOHandler();
    } else if (ext.equals(WriteableFileExtensions.tgf)) {
      io = new TGFIOHandler();
    } else if (ext.equals(WriteableFileExtensions.jpg) || ext.equals(WriteableFileExtensions.jpeg)) {
      io = new JPGIOHandler();
    } else if (ext.equals(WriteableFileExtensions.svg)) {
      io = createSVGIOHandler();
      if (io == null) {
        throw new Exception("Unknown output format (SVG extension not installed).");
      }
    } else {
      throw new Exception("Unknown output format.");
    }
    return io;
  }
  
  /**
   * 
   * @param descriptor
   * @return
   */
  public static boolean GraphMLmapsContainsMap(String descriptor) {
    try {
      for (Field f: GraphMLmaps.class.getFields()) {
        // Get field value (for static fields, object is null) and
        // compare with given descriptor.
        if (f.get(null).equals(descriptor)) {
          return true;
        }
      }
    } catch (Exception exc) {
      logger.finest(Utils.getMessage(exc));
    }
    return false;
  }
  
  /**
   * 
   * @param nm
   * @param ioh
   * @param desc
   */
  private static void addDataMap(DataMap nm, GraphMLHandler ioh, String desc) {
    KeyScope scope;
    if (nm instanceof NodeMap) {
      scope = KeyScope.NODE;
    } else if (nm instanceof EdgeMap) {
      scope = KeyScope.EDGE;
    } else {
      scope = KeyScope.ALL;
    }
    
    addDataMap(nm, ioh, desc, KeyType.STRING, scope);//AttributeConstants.TYPE_STRING
  }
  
  /**
   * 
   * @param nm
   * @param ioh
   * @param desc
   * @param keytype
   * @param scope
   */
  private static void addDataMap(DataMap nm, GraphMLHandler ioh, String desc,
    KeyType keytype, KeyScope scope) {
    ioh.addInputDataAcceptor (desc, nm, scope, keytype);
    ioh.addOutputDataProvider(desc, nm, scope, keytype);
    //ioh.addAttribute(nm, desc, keytype);    // <= yf 2.6
  }
  
  /**
   * 
   * @param view
   * @param outputIsAPixelImage
   */
  private static void configureView(Graph2DView view, boolean outputIsAPixelImage) {
    Graph2D graph = view.getGraph2D();
    Rectangle box = graph.getBoundingBox();
    if (outputIsAPixelImage) {
      Dimension dim = getOutputSize(box);
      view.setSize(dim);
    }
    view.zoomToArea(box.getX() - 5, box.getY() - 5, box.getWidth() + 10, box.getHeight() + 10);
    view.setPaintDetailThreshold(0.0); // paint all details
    
    // Set the view as active view, such that io handlers take it.
    graph.setCurrentView(view);
  }
  
  protected IOHandler outputHandler = null;
  protected boolean lastFileWasOverwritten = false;
  protected GraphBackgroundImageProvider imageProvider = null;
  
  
  public void setBackgroundImageProvider(GraphBackgroundImageProvider imageProvider) {
    this.imageProvider = imageProvider;
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
  public IOHandler getOutputHandler() {
    return outputHandler;
  }
  
  /**
   * 
   * @param outputHandler
   */
  public void setOutputHandler(IOHandler outputHandler) {
    this.outputHandler = outputHandler;
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetBackgroundImageProvider() {
    return imageProvider!=null;
  }
  
  /**
   * 
   */
  public Graph2DExporter() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * 
   * @return
   */
  public boolean isLastFileWasOverwritten() {
    return lastFileWasOverwritten;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.io.Graph2Dwriteable#writeToFile(y.view.Graph2D, java.lang.String)
   */
  @Override
  public boolean writeToFile(Graph2D graph, String outFile) {
    
    // initialize and check the IOHandler
    if (outputHandler == null) {
      outputHandler = new GraphMLIOHandler(); // new GMLIOHandler();
    }
    if (!outputHandler.canWrite()) {
      logger.warning("Can not write to given path!");
      return false;
    }
    
    // Try to set metadata annotations
    if (outputHandler instanceof GraphMLIOHandler) {
      Graph2DGraphMLHandler ioh = ((GraphMLIOHandler) outputHandler).getGraphMLHandler() ;
      
      try {
        // Add known maps from GraphMLMapsExtended.
        DataProvider mapDescriptionMap = graph.getDataProvider(mapDescription);
        
        EdgeMap[] eg = graph.getRegisteredEdgeMaps();
        if (eg!=null) {
          for (int i=0; i<eg.length;i++) {
            Object desc_o = mapDescriptionMap.get(eg[i]);
            String desc = desc_o==null?null:desc_o.toString();
            if (desc!=null && GraphMLmapsContainsMap( desc )) {
              addDataMap(eg[i], ioh, desc);
            }
          }
        }
        NodeMap[] nm = graph.getRegisteredNodeMaps();
        if (nm!=null) {
          for (int i=0; i<nm.length;i++) {
            Object desc_o = mapDescriptionMap.get(nm[i]);
            String desc = desc_o==null?null:desc_o.toString();
            if (desc!=null && GraphMLmapsContainsMap( desc )) {
              addDataMap(nm[i], ioh, desc);
            }
          }
        }
        
      } catch(Throwable exc) {
        logger.warning("Can not write annotations to graph file.");
        exc.printStackTrace();
      }
      
      
      
    }
    // ----------------
    
    // Zoom by default to fit content in graphML
    boolean isGraphOutput=false;
    if (outputHandler instanceof GraphMLIOHandler ||
        outputHandler instanceof GMLIOHandler ||
        outputHandler instanceof YGFIOHandler) {
      isGraphOutput = true;
    }
    
    // Configure view and rememeber old one to restore after saving.
    View old_v = graph.getCurrentView();
    Graph2DView view = new Graph2DView(graph);
    configureView(view, !isGraphOutput);
    processBackgroundImage(view, true);
    
    // => Moved to a global setting.
    //if (outputHandler instanceof JPGIOHandler) {
    //graph = modifyNodeLabels(graph,true,true);
    //}
    
    // Try to write the file.
    int retried = 0;
    if (new File(outFile).exists()) {
      lastFileWasOverwritten = true;
    }
    boolean success = false;
    while (retried < 3) {
      try {
        
        // Create a specific ouputStream, that removes all
        // y-Files-is-the-man--poser-strings.
        OutputStream out = null;
        if (outputHandler instanceof GraphMLIOHandler ||
            outputHandler instanceof GMLIOHandler ||
            outputHandler.getClass().getSimpleName().equals("SVGIOHandler")) {
          out = new YFilesWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
        }
        
        if (out == null) {
          outputHandler.write(graph, outFile);
        } else {
          outputHandler.write(graph, out);
          out.close();
        }
        
        success = true;
        break;// Success => No more need to retry
      } catch (IOException exc) {
        retried++;
        if (retried > 2) {
          logger.severe("Error while encoding file " + outFile + "\n" + exc);
          exc.printStackTrace();
          break;
        }
      } finally {
        graph.setCurrentView(old_v);
      }
    }
    
    // Remove unused view (also saves memory, because BG-images might be stored in view).
    graph.removeView(view);
    
    return success;
  }
  
  /**
   * 
   * @param view
   * @param waitUntilComplete
   */
  protected void processBackgroundImage(Graph2DView view, boolean waitUntilComplete) {
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.io.Graph2Dwriteable#writeToFile(y.view.Graph2D, java.lang.String, java.lang.String)
   */
  @Override
  public boolean writeToFile(Graph2D graph, String outFile, String format) throws Exception {
    WriteableFileExtensions ext = WriteableFileExtensions.valueOf(format.toLowerCase().trim());
    setOutputHandler(getIOHandler(ext));
    return writeToFile(graph, outFile);
  }
  
}
