/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.svg;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JFrame;
import javax.xml.stream.XMLStreamException;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.util.XMLResourceDescriptor;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.TidySBMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.zbit.graph.sbgn.DrawingOptions;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.layout.y.YGraphView;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @param <T>
 */
public class SVGparser<T> implements Runnable {
  
  /**
   * Reads a file and parses the path elements.
   * 
   * @param args
   *        args[0] - Filename to parse.
   * @throws IOException
   *         Error reading the SVG file.
   * @throws XMLStreamException
   * @throws SBMLException
   * @see "http://stackoverflow.com/questions/26027313/how-to-load-and-parse-svg-documents"
   */
  @SuppressWarnings("unchecked")
  public static void main(String args[]) throws IOException, SBMLException, XMLStreamException {
    if (PreferencesDialog.showPreferencesDialog(DrawingOptions.class)) {
      URI uri = new File(args[0]).toURI();
      SVGparser<SBMLDocument> converter = new SVGparser<SBMLDocument>(uri.toString());
      converter.setSVGMHandler(new SBMLLayoutHandler());
      converter.run();
      File file = new File(args[1]);
      System.out.println("writing to file " + file.getAbsolutePath());
      TidySBMLWriter.write(converter.getSVGHandler().getResult(), file, ' ', (short) 2);
      SBMLDocument doc = converter.getSVGHandler().getResult();
      JFrame f = new JFrame();
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //f.setLocationRelativeTo(null);
      f.getContentPane().add(new SBMLModelSplitPane(new OpenedFile<SBMLDocument>(doc), true));
      f.pack();
      f.setVisible(true);
      new YGraphView(doc);
    }
  }
  
  /**
   * 
   * @return
   */
  private SVGHandler<T> getSVGHandler() {
    return handler;
  }
  
  /**
   * 
   */
  private SVGHandler<T> handler;
  
  /**
   * 
   */
  private Document svgDocument;
  
  /**
   * Creates an SVG Document given a URI.
   *
   * @param uri
   *        Path to the file.
   * @throws IOException
   *         Something went wrong parsing the SVG file.
   */
  public SVGparser(String uri) throws IOException {
    setSVGDocument(createSVGDocument(uri));
  }
  
  /**
   * Use the {@link SAXSVGDocumentFactory} to parse the given URI into a DOM.
   * 
   * @param uri
   *        The path to the SVG file to read.
   * @return A Document instance that represents the SVG file.
   * @throws IOException
   *         The file could not be read.
   */
  private Document createSVGDocument(String uri) throws IOException {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
    return factory.createDocument(uri);
  }
  
  /**
   * Returns a list of elements in the SVG document with names that match
   * the given {@link Element}.
   * 
   * @param element the name of an element type
   * @return The list of "path" elements in the SVG document.
   */
  private NodeList getElements(Element element) {
    return getSVGDocumentRoot().getElementsByTagName(element.toString());
  }
  
  /**
   * Returns the SVG document parsed upon instantiating this class.
   * 
   * @return A valid, parsed, non-null SVG document instance.
   */
  public Document getSVGDocument() {
    return svgDocument;
  }
  
  /**
   * Returns an SVGOMSVGElement that is the document's root element.
   * 
   * @return The SVG document typecast into an SVGOMSVGElement.
   */
  private SVGOMSVGElement getSVGDocumentRoot() {
    return (SVGOMSVGElement) getSVGDocument().getDocumentElement();
  }
  
  /**
   * Enhance the SVG DOM for the given document to provide CSS- and SVG-specific
   * DOM interfaces.
   * 
   * @param document
   *        The document to enhance.
   * @see "http://wiki.apache.org/xmlgraphics-batik/BootSvgAndCssDom"
   */
  private void initSVGDOM(Document document) {
    UserAgent userAgent = new UserAgentAdapter();
    DocumentLoader loader = new DocumentLoader(userAgent);
    BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
    bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
    
    // Enable CSS- and SVG-specific enhancements.
    (new GVTBuilder()).build(bridgeContext, document);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Document d = getSVGDocument();
    org.w3c.dom.Element svgRoot = d.getDocumentElement();
    String viewBox[] = svgRoot.getAttribute("viewBox").split(" ");
    double vBox[] = new double[viewBox.length];
    for (int i = 0; i < vBox.length; i++) {
      vBox[i] = Double.parseDouble(viewBox[i]);
    }
    String width = stripUnit(svgRoot.getAttribute("width"));
    String heigth = stripUnit(svgRoot.getAttribute("height"));
    handler.init(Double.parseDouble(width), Double.parseDouble(heigth), vBox);
    for (Element element : Element.values()) {
      handler.handle(element, getElements(element));
    }
  }
  
  /**
   * 
   * @param attribute
   * @return
   */
  private String stripUnit(String attribute) {
    if (attribute.endsWith("px")) {
      attribute = attribute.substring(0, attribute.length() - 2);
    }
    return attribute;
  }
  
  /**
   * This will set the document to parse. This method also initializes the SVG
   * DOM enhancements, which are necessary to perform SVG and CSS manipulations.
   * The initialization is also required to extract information from the SVG
   * path elements.
   *
   * @param document
   *        The document that contains SVG content.
   */
  public void setSVGDocument(Document document) {
    initSVGDOM(document);
    svgDocument = document;
  }
  
  /**
   * 
   * @param svgHandler
   */
  public void setSVGMHandler(SVGHandler<T> svgHandler) {
    handler = svgHandler;
  }
  
}
