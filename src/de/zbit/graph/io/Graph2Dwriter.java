/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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
import java.util.logging.Level;
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
import de.zbit.kegg.io.KEGGtranslator;

/**
 * This class should be used whenever any yFiles data
 * structure should be written to a file. No matter if
 * JPG or GraphML is the destination format.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class Graph2Dwriter implements Graph2Dwriteable {
  public static final transient Logger log = Logger.getLogger(Graph2Dwriter.class.getName());
  

  /**
   * An enum of writeable output formats (identified by file extension).
   * <p>All lowercased!
   * @author Clemens Wrzodek
   */
  public static enum writeableFileExtensions {
    gif,graphml,gml,ygf,tgf,jpg,jpeg,svg;
  }
  
  /**
   * This is used internally to identify a certain dataHandler in the Graph document.
   * The content is not important, it should just be any defined static final string.
   */
  public final static String mapDescription="-MAP_DESCRIPTION-";
  
  /**
   * Important: This determines the output format. E.g. a GraphMLIOHandler
   * will write a graphML file, a GMLIOHandler will write a GML file.
   * 
   */
  private IOHandler outputHandler = null;
  
  /**
   * <code>TRUE</code> if any file during {@link #writeToFile(Graph2D, String)}
   * has been overwritten.
   */
  private boolean lastFileWasOverwritten=false;
  

  /**
   * An optional {@link GraphBackgroundImageProvider} that will draw
   * any image as background image of this {@link #graphLayer} or
   * the corresponding panel.
   */
  private GraphBackgroundImageProvider imageProvider = null;
  
  /**
   * This is required by some {@link GraphBackgroundImageProvider}s.
   */
  private KEGGtranslator<?> translator=null;
  
  
  /**
   * 
   * @param outputHandler2
   * @param translator
   */
  public Graph2Dwriter(IOHandler outputHandler2, KEGGtranslator<?> translator) {
    this(outputHandler2);
    this.translator=translator;
  }

  /**
   * @param f
   * @throws Exception 
   */
  public Graph2Dwriter(writeableFileExtensions f, KEGGtranslator<?> translator) throws Exception {
    this(getIOHandler(f), translator);
  }
  
  /**
   * @param outputHandler2
   */
  public Graph2Dwriter(IOHandler outputHandler2) {
    super();
    setOutputHandler(outputHandler2);
  }

  /**
   * @param f
   * @throws Exception 
   */
  public Graph2Dwriter(writeableFileExtensions f) throws Exception {
    this(getIOHandler(f));
  }

  /**
   * 
   * @param imageProvider
   */
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
   * @return the translator
   */
  public KEGGtranslator<?> getTranslator() {
    return translator;
  }

  /**
   * @param translator the translator to set
   */
  public void setTranslator(KEGGtranslator<?> translator) {
    this.translator = translator;
  }

  /**
   * The IOHandler determines the output format.
   * May be GraphMLIOHandler or GMLIOHandler,...
   * @return
   */
  public IOHandler getOutputHandler() {
    return outputHandler;
  }
  /**
   * Set the outputHander to use when writing the file.
   * May be GraphMLIOHandler or GMLIOHandler,...
   * See also: {@link #outputHandler}
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

  public boolean writeToFile(Graph2D graph, String outFile) {
    
    // initialize and check the IOHandler
    if (outputHandler==null) {
      outputHandler = new GraphMLIOHandler(); // new GMLIOHandler();
    }
    if (!outputHandler.canWrite()) {
      log.warning("Can not write to given path!");
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
        
      } catch(Throwable e) {
        log.warning("Can not write annotations to graph file.");
        e.printStackTrace();
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
    try {
      if (isSetBackgroundImageProvider()) {
        // translator should be ALLOWED to be null, must be considered in
        // the GraphBachgroundImageProviders... (NOT HERE!!!)
        getBackgroundImageProvider().addBackgroundImage(view, translator, true);
      }
    } catch (Exception e) { // NullPointer or MalformedURLException
      log.log(Level.WARNING, "Could not setup background image for output file.", e);
    }
    
    // => Moved to a global setting.
    //if (outputHandler instanceof JPGIOHandler) {
      //graph = modifyNodeLabels(graph,true,true);
    //}
    
    // Try to write the file.
    int retried=0;
    if (new File(outFile).exists()) {
      lastFileWasOverwritten = true;
    }
    boolean success = false;
    while (retried<3) {
      try {
        
        // Create a specific ouputStream, that removes all
        // y-Files-is-the-man--poser-strings.
        OutputStream out = null;
        if (outputHandler instanceof GraphMLIOHandler ||
            outputHandler instanceof GMLIOHandler ||
            outputHandler.getClass().getSimpleName().equals("SVGIOHandler")) {
          out = new YFilesWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
        }
        
        if (out==null)
          outputHandler.write(graph, outFile);
        else {
          outputHandler.write(graph, out);
          out.close();
        }
        
        success = true;
        break;// Success => No more need to retry
      } catch (IOException iex) {
        retried++;
        if (retried>2) {
          System.err.println("Error while encoding file " + outFile + "\n" + iex);
          iex.printStackTrace();
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
   * Check if {@link GraphMLmaps} contains a map with the
   * given descriptor.
   * @param descriptor
   * @return true if and only the the given descriptor corresponds
   * to a registered map.
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
    } catch (Exception e) {}
    return false;
  }
  

  /**
   * Add the given DataMap (e.g. NodeMap) to the given GraphHandler, 
   * using the given description.
   * Adds the map as InputDataAcceptor and OutputDataProvider.
   * Keytype will be set to KeyType.STRING by this function.
   * @param nm - the DataMap (NodeMap / EdgeMap)
   * @param ioh - the GraphHandler (e.g. Graph2DGraphMLHandler)
   * @param desc - the Description of the map
   * @param scope - KeyScope (e.g. KeyScope.NODE)
   */
  private static void addDataMap(DataMap nm, GraphMLHandler ioh, String desc) {
    KeyScope scope;
    if (nm instanceof NodeMap)scope = KeyScope.NODE;
    else if (nm instanceof EdgeMap)scope = KeyScope.EDGE;
    else scope = KeyScope.ALL;
    
    addDataMap(nm, ioh, desc, KeyType.STRING, scope);//AttributeConstants.TYPE_STRING
  }
  
  /**
   * Add the given DataAcceptor (e.g. NodeMap) to the given GraphHandler, 
   * using the given description.
   * Adds the map as InputDataAcceptor and OutputDataProvider.
   * @param nm - the DataAcceptor (NodeMap / EdgeMap)
   * @param ioh - the GraphHandler (e.g. Graph2DGraphMLHandler)
   * @param desc - Description
   * @param keytype - KeyType (e.g. KeyType.STRING)
   * @param scope - KeyScope (e.g. KeyScope.NODE)
   */
  private static void addDataMap(DataMap nm, GraphMLHandler ioh, String desc, KeyType keytype, KeyScope scope) {
    ioh.addInputDataAcceptor (desc, nm, scope, keytype);
    ioh.addOutputDataProvider(desc, nm, scope, keytype);
    //ioh.addAttribute(nm, desc, keytype);    // <= yf 2.6
  }
  
  
  /**
   * This will add the describing mapDescriptionMap to the graph.
   * @param graph
   * @return
   */
  @SuppressWarnings("unchecked")
  public static GenericDataMap<DataMap, String> addMapDescriptionMapToGraph(Graph2D graph) {
    GenericDataMap<DataMap, String> mapDescriptionMap = null;
    try {
      mapDescriptionMap = (GenericDataMap<DataMap, String>) graph.getDataProvider(Graph2Dwriter.mapDescription);
    } catch (Throwable t) {};
    if (mapDescriptionMap==null) {
      // Actually it is always null ;-)
      mapDescriptionMap = new GenericDataMap<DataMap, String>(Graph2Dwriter.mapDescription);
      graph.addDataProvider(Graph2Dwriter.mapDescription, mapDescriptionMap);
    }
    return mapDescriptionMap;
  }
  
  /**
   * Configures the view that is used as rendering environment for some output
   * formats.
   * @param view any Graph2DView of a graph to save
   * @param outputIsAPixelImage is the output a pixel based (jpeg, gif,...) image?
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
  

  /**
   * Ensures a minimum graph size of 1600x1200.
   * @param inBox input bounding box of graph.
   * @return
   */
  private static Dimension getOutputSize(Rectangle inBox) {
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


  public boolean isLastFileWasOverwritten() {
    return lastFileWasOverwritten;
  }
  

  
  /**
   * Requires the yFiles SVG extension libraries on the projects
   * build path!
   * @return new SVGIOHandler()
   */
  @SuppressWarnings("unchecked")
  public static IOHandler createSVGIOHandler() {
    try {
      Class<? extends IOHandler> svg = (Class<? extends IOHandler>) Class.forName("yext.svg.io.SVGIOHandler");    
      if (svg!=null) return svg.newInstance();
    } catch (Throwable e) {
      // Extension not installed
    }
    return null;
  }
  
  /**
   * @param document
   * @param path
   * @param format output file extension, e.g., "gif", "graphml", "gml", "jpg",...
   * @throws Exception 
   * @return true if everything went fine.
   */
  public boolean writeToFile(Graph2D graph, String outFile, String format) throws Exception {
    writeableFileExtensions ext = writeableFileExtensions.valueOf(format.toLowerCase().trim());
    setOutputHandler(getIOHandler(ext));
    return writeToFile(graph, outFile);
  }

  /**
   * @param io
   * @param ext
   * @return
   * @throws Exception
   */
  public static IOHandler getIOHandler(writeableFileExtensions ext) throws Exception {
    IOHandler io;
    if (ext.equals(writeableFileExtensions.gif)) {
      io = new GIFIOHandler();
    } else if (ext.equals(writeableFileExtensions.graphml)) {
      io = new GraphMLIOHandler();
    } else if (ext.equals(writeableFileExtensions.gml)) {
      io = new GMLIOHandler();
    } else if (ext.equals(writeableFileExtensions.ygf)) {
      io = new YGFIOHandler();
    } else if (ext.equals(writeableFileExtensions.tgf)) {
      io = new TGFIOHandler();
    } else if (ext.equals(writeableFileExtensions.jpg) || ext.equals(writeableFileExtensions.jpeg)) {
      io = new JPGIOHandler();
    } else if (ext.equals(writeableFileExtensions.svg)) {
      io = createSVGIOHandler();
      if (io==null) {
        throw new Exception("Unknown output format (SVG extension not installed).");
      }
    } else {
      throw new Exception("Unknown output format.");
    }
    return io;
  }
  
}
