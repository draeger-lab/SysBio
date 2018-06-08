/*
 * $Id: YGraphView.java 1064 2012-10-29 15:46:01Z jmatthes $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/test/de/zbit/sbml/layout/y/YGraphView.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML Editor.
 *
 * Copyright (C) 2012-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.layout.y;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.util.StringTools;

import de.zbit.graph.RestrictedEditMode;
import de.zbit.graph.io.Graph2Dwriteable;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.gui.GUITools;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.sbml.layout.GlyphCreator;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.util.logging.LogUtil;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.ImageIoOutputHandler;
import y.layout.organic.SmartOrganicLayouter;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.NodeLabel;
import y.view.NodeRealizer;

/**
 * Simple GUI to display a Graph2DView.
 *
 * It renders an {@link SBMLDocument} (from command line arg0, or falling back to a
 * default) with {@link LayoutDirector} using {@link YLayoutBuilder} and the TikZLayoutAlgorithm
 * (YLayoutAlgorithm is not yet functional).
 *
 * @author Jakob Matthes
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1064 $
 */
public class YGraphView implements PropertyChangeListener {

  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(YGraphView.class.getName());

  /**
   *
   */
  private static String out;
  /**
   *
   */
  private static final int WINDOW_HEIGHT = 720;

  /**
   * Initial dimensions  of the window.
   */
  private static final int WINDOW_WIDTH = 960;

  private YLayoutAlgorithm algorithm = new YLayoutAlgorithm();
  /**
   * @param args
   */
  public static void main(final String[] args) {
    LogUtil.initializeLogging(YGraphView.class.getPackage().toString());
    final File in = new File(args[0]);
    out = args.length > 1 ? args[1] : null;
    javax.swing.SwingUtilities.invokeLater(() -> {
      try {
        new YGraphView(in);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    });

    // Do-nothing-loop because in MacOS the application terminates suddenly.
    if (GUITools.isMacOSX()) {
      for (int i = 0; i < 1E12; i++) {
        ;
      }
    }
  }

  /**
   * SBML document from which to create the graph.
   */
  private OpenedFile<SBMLDocument> document;

  /**
   * Title for the the window.
   */
  private String title;

  /**
   *
   */
  public YGraphView() {
  }

  /**
   * @param inputFile File to display
   * @throws Throwable
   */
  public YGraphView(File inputFile) throws Throwable {
    this();
    SBMLReadingTask readingTask = new SBMLReadingTask(inputFile, null, this);
    readingTask.execute();
  }

  /**
   * @param doc SBMLDocument to display
   */
  public YGraphView(SBMLDocument doc) {
    this();
    setSBMLDocument(doc);
  }

  /**
   *
   * @param product
   * @param windowWidth
   * @param windowHeight
   * @return
   */
  public Graph2DView createGraph2DView(Graph2D product, int windowWidth, int windowHeight) {
    Graph2DView view = new Graph2DView(product);
    DefaultGraph2DRenderer dgr = new DefaultGraph2DRenderer();
    dgr.setDrawEdgesFirst(true);
    view.setGraph2DRenderer(dgr);
    Rectangle box = view.getGraph2D().getBoundingBox();
    Dimension dim = box.getSize();
    view.setSize(dim);
    // view.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    Dimension minimumSize = new Dimension(
      Math.min(windowWidth, Math.max((int) view.getMinimumSize().getWidth(), 100)),
      (int) Math.max(view.getMinimumSize().getHeight(), windowHeight/2d));
    view.setMinimumSize(minimumSize);
    view.setPreferredSize(new Dimension(100, (int) Math.max(windowHeight * 0.6d, 50d)));
    view.setOpaque(false);

    DefaultGraph2DRenderer renderer = new DefaultGraph2DRenderer() {
      /* (non-Javadoc)
       * @see y.view.DefaultGraph2DRenderer#getLayer(y.view.Graph2D, y.base.Edge)
       */
      @Override
      protected int getLayer(Graph2D graph, Edge edge) {
        return 1;
      }
      /* (non-Javadoc)
       * @see y.view.DefaultGraph2DRenderer#getLayer(y.view.Graph2D, y.base.Node)
       */
      @Override
      protected int getLayer(Graph2D graph, Node node) {
        return 0;
      }
    };
    renderer.setLayeredPainting(true);
    view.setGraph2DRenderer(renderer);

    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      view.fitContent(true);
    } catch (Throwable t) {
      // Not really a problem
    }
    RestrictedEditMode.addOverviewAndNavigation(view);
    view.addViewMode(new RestrictedEditMode());
    view.setFitContentOnResize(true);

    return view;
  }

  /**
   * Create a window showing the graph view.
   * @param product
   */
  private void displayGraph2DView(Graph2D product) {
    // Create a viewer for the graph
    Graph2DView view = createGraph2DView(product, WINDOW_WIDTH, WINDOW_HEIGHT);

    // Create and show window
    JFrame frame = new JFrame();
    frame.setTitle(getTitle());
    JTabbedPaneDraggableAndCloseable tabs = new JTabbedPaneDraggableAndCloseable();
    tabs.add("Graph", view);
    tabs.add("Detail", new SBMLModelSplitPane(document, true));
    frame.getContentPane().add(tabs);
    frame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    YImageTools.writeSVGImage(document.getDocument().getModel(), product, System.getProperty("user.home") + "/out.svg");
  }

  /**
   * Write a textual representation of the graph (all nodes and edges
   * including realizier information) to standard out for debugging purposes.
   */
  private void dumpGraph(Graph2D product) {
    NodeCursor nodeCursor = product.nodes();
    System.out.println("Nodes:");
    for (; nodeCursor.ok(); nodeCursor.next()) {
      Node n = (Node) nodeCursor.current();
      NodeRealizer nr = product.getRealizer(n);
      NodeLabel nodeLabel = nr.getLabel();
      System.out.println(n);
      System.out.println("  " + product.getRealizer(n));
      System.out.println(nodeLabel.toString());
    }
    System.out.println("Edges:");
    EdgeCursor edgeCursor = product.edges();
    for (; edgeCursor.ok(); edgeCursor.next()) {
      Edge e = (Edge) edgeCursor.current();
      System.out.println(e);
      System.out.println("  " + prettyPrintEdgeRealizer(product.getRealizer(e)));
    }
  }

  /**
   * @return
   */
  public String getTitle() {
    return title != null ? title: YGraphView.class.getSimpleName();
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
      setOpenedFile((OpenedFile<SBMLDocument>) evt.getNewValue());
    }
  }


  public void setSBMLDocument(SBMLDocument doc) {
    setOpenedFile(new OpenedFile<>(doc));
  }

  /**
   *
   * @param doc
   */
  public void setOpenedFile(OpenedFile<SBMLDocument> of) {
    if ((of == null) || !of.isSetDocument()) {
      logger.warning("No SBML document given.");
      System.exit(1);
    }

    document = of;

    SBMLDocument doc = of.getDocument();

    Model model = doc.getModel();
    LayoutModelPlugin ext = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));

    // Generate glyphs for SBML documents without layout information
    if (ext == null) {
      logger.info("Model does not contain layouts, creating glyphs for every object.");
      (new GlyphCreator(model)).create();
      ext = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));
    }

    // Display option pane to choose specific layout if multiple layouts are available
    int layoutIndex = 0;
    if (ext.getLayoutCount() > 1) {
      String layouts[] = new String[ext.getLayoutCount()];
      for (int i = 0; i < ext.getLayoutCount(); i++) {
        Layout layout = ext.getLayout(i);
        layouts[i] = layout.isSetName() ? layout.getName() : layout.getId();
      }
      layoutIndex = JOptionPane.showOptionDialog(null,
        "Select the layout to be displayed", "Layout selection",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        layouts, layouts[0]);
      if (layoutIndex < 0) {
        System.exit(0);
      }
    }

    // Run LayoutDirector and create product
    LayoutDirector<ILayoutGraph> director =
        new LayoutDirector<ILayoutGraph>(doc, new YLayoutBuilder(), algorithm); //new YLayoutAlgorithm());
    director.setLayoutIndex(layoutIndex);
    director.run();
    //    ILayoutGraph hello = director.getProduct();
    //    hello.getNode2glyph();

    SmartOrganicLayouter sol = new SmartOrganicLayouter();
    sol.setCompactness(.2);

    //    new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED).doLayout(product, sol);
    updateSBMLDocument(director);
    // experimental or debug features
    //    writeModifiedModel(System.getProperty("user.dir")+"/out.xml");
    //writeSVGImage(director.getProduct().getGraph2D(), out);
    //dumpGraph();

    displayGraph2DView(director.getProduct().getGraph2D());
  }

  public YLayoutAlgorithm getAlgorithm() {
    return algorithm;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   *
   * @param director
   */
  private void updateSBMLDocument(LayoutDirector<ILayoutGraph> director) {
    LayoutGraph layoutProduct = (LayoutGraph) director.getProduct();
    Graph2D graph = layoutProduct.getGraph2D();

    Map<Node, AbstractReferenceGlyph> node2glyph = layoutProduct.getNode2glyph();

    for (Map.Entry<Node, AbstractReferenceGlyph> entry : node2glyph.entrySet()) {
      Node key = entry.getKey();
      AbstractReferenceGlyph value = entry.getValue();

      value.getBoundingBox().getPosition().setX(graph.getX(key));
      value.getBoundingBox().getPosition().setY(graph.getY(key));
      value.getBoundingBox().getDimensions().setWidth(graph.getWidth(key));
      value.getBoundingBox().getDimensions().setHeight(graph.getHeight(key));

    }
  }

  /**
   * Write image file (png) of the graph.
   *
   * @param outFile path of the output file
   */
  private void writeImage(Graph2D product, String outFile) {
    Iterator<javax.imageio.ImageWriter> iterator =
        javax.imageio.ImageIO.getImageWritersBySuffix("png");
    javax.imageio.ImageWriter imageWriter =
        iterator.hasNext() ? iterator.next() : null;

        if (imageWriter != null) {
          Graph2Dwriteable graph2Dwriter =
              new Graph2Dwriter(new ImageIoOutputHandler(imageWriter));
          graph2Dwriter.writeToFile(product, outFile);
          logger.info(MessageFormat.format("Image written to ''{0}''.", outFile));
        }
        else {
          logger.warning("Could not write image: ImageWriter not available.");
        }
  }

  /**
   * Write the modified SBML model to a file.
   *
   * @param document document to write
   * @param outFile
   * @throws XMLStreamException
   * @throws IOException
   */
  private void writeModifiedModel(String outFile) {
    try {
      SBMLWriter.write(document.getDocument(), new File(outFile), ' ', (short) 2);
    } catch (SBMLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    logger.info(MessageFormat.format("Modified model written to ''{0}''.", outFile));
  }

}
