/*
 * $Id$
 * $URL$
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
package de.zbit.sbml.layout.y;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;


/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @see http://docs.oracle.com/javafx/2/canvas/jfxpub-canvas.htm
 */
public class RedrawReactionEdge extends Application {
  
  /* (non-Javadoc)
   * @see javafx.application.Application#init()
   */
  @Override
  public void init() throws Exception {
    Parameters p = getParameters();
    List<String> args = p.getRaw();
    SBMLDocument doc = SBMLReader.read(new File(args.get(0)));
    if (doc.isSetModel()) {
      Model m = doc.getModel();
      setModel(m);
      if (m.isSetListOfReactions() && m.isSetPlugin(LayoutConstants.shortLabel)) {
        rIds = new String[args.size() - 1];
        for (int i = 1; i < args.size(); i++) {
          rIds[i - 1] = args.get(i).startsWith("R_") ? args.get(i) : "R_" + args.get(i);
        }
      }
    }
  }
  
  private Model model;
  private LayoutModelPlugin layoutPlugin;
  private String rIds[];
  
  /**
   * @return the model
   */
  public Model getModel() {
    return model;
  }
  
  /**
   * @param model the model to set
   */
  public void setModel(Model model) {
    this.model = model;
    layoutPlugin = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
  }
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    launch(args);
  }
  
  public void redraw(Reaction reaction, GraphicsContext gc) {
    if (layoutPlugin.isSetListOfLayouts()) {
      for (Layout layout : layoutPlugin.getListOfLayouts()) {
        redraw(reaction, layout, gc);
      }
    }
  }
  
  public void redraw(Reaction reaction, Layout layout, GraphicsContext gc) {
    if (layout.isSetListOfReactionGlyphs()) {
      for (ReactionGlyph rg : layout.getListOfReactionGlyphs()) {
        if (rg.isSetReaction() && rg.getReaction().equals(reaction.getId())) {
          redraw(rg, gc);
        }
      }
    }
  }
  
  public void redraw(ReactionGlyph rg, GraphicsContext gc) {
    BoundingBox bbox = rg.getBoundingBox();
    Dimensions dim = bbox.getDimensions();
    Point p = bbox.getPosition();
    
    Layout l = (Layout) rg.getParent().getParent();
    Dimensions d = l.getDimensions();
    double cw = gc.getCanvas().getWidth();
    double ch = gc.getCanvas().getHeight();
    double x = scale(p.x(), d.getWidth(), cw);
    double y = scale(p.y(), d.getHeight(), ch);
    double w = scale(dim.getWidth(), d.getWidth(), cw);
    double h = scale(dim.getHeight(), d.getHeight(), ch);
    gc.setStroke(Color.BLACK);
    gc.setFill(Color.BLACK);
    gc.setLineWidth(5);
    gc.strokeRect(x, y, w, h);
    
    for (int i = 0; i < rg.getSpeciesReferenceGlyphCount(); i++) {
      SpeciesReferenceGlyph srg = rg.getSpeciesReferenceGlyph(i);
      if (srg.isSetCurve()) {
        Curve curve = srg.getCurve();
        for (int j = 0; j < curve.getCurveSegmentCount(); j++) {
          CurveSegment cs = curve.getCurveSegment(j);
          if (cs instanceof CubicBezier) {
            CubicBezier cb = (CubicBezier) cs;
            Point start = cb.getStart();
            Point end = cb.getEnd();
            Point bz1 = cb.getBasePoint1();
            Point bz2 = cb.getBasePoint2();
            gc.beginPath();
            gc.moveTo(scale(start.x(), d.getWidth(), cw), scale(start.y(), d.getHeight(), ch));
            gc.bezierCurveTo(
              scale(bz1.x(), d.getWidth(), cw),
              scale(bz1.y(), d.getHeight(), ch),
              scale(bz2.x(), d.getWidth(), cw),
              scale(bz2.y(), d.getHeight(), ch),
              scale(end.x(), d.getWidth(), cw),
              scale(end.y(), d.getHeight(), ch)
                );
            gc.setStroke(Color.FORESTGREEN);
            gc.setLineWidth(2);
            gc.stroke();
          } else {
            
          }
        }
      } else {
        
      }
    }
    
    
  }
  
  private CubicCurve createCurve(CubicBezier cb, double oldW, double newW, double oldH, double newH) {
    Point start = cb.getStart();
    Point end = cb.getEnd();
    Point bz1 = cb.getBasePoint1();
    Point bz2 = cb.getBasePoint2();
    
    CubicCurve curve = new CubicCurve();
    curve.setStartX(scale(start.x(), oldW, newW));
    curve.setStartY(scale(start.y(), oldH, newH));
    curve.setControlX1(scale(bz1.x(), oldW, newW));
    curve.setControlY1(scale(bz1.y(), oldH, newH));
    curve.setControlX2(scale(bz2.x(), oldW, newW));
    curve.setControlY2(scale(bz2.y(), oldH, newH));
    curve.setEndX(scale(end.x(), oldW, newW));
    curve.setEndY(scale(end.y(), oldH, newH));
    curve.setStroke(Color.FORESTGREEN);
    curve.setStrokeWidth(4);
    curve.setStrokeLineCap(StrokeLineCap.ROUND);
    curve.setFill(Color.CORNSILK.deriveColor(0, 1.2, 1, 0.6));
    return curve;
  }
  
  private double scale(double p, double length, double newLength) {
    return p * (newLength / length);
  }
  
  private void drawShapes(GraphicsContext gc) {
    gc.setFill(Color.GREEN);
    gc.setStroke(Color.BLUE);
    gc.setLineWidth(5);
    gc.strokeLine(40, 10, 10, 40);
    gc.fillOval(10, 60, 30, 30);
    gc.strokeOval(60, 60, 30, 30);
    gc.fillRoundRect(110, 60, 30, 30, 10, 10);
    gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
    gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
    gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
    gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
    gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
    gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
    gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
    gc.fillPolygon(new double[]{10, 40, 10, 40},
      new double[]{210, 210, 240, 240}, 4);
    gc.strokePolygon(new double[]{60, 90, 60, 90},
      new double[]{210, 210, 240, 240}, 4);
    gc.strokePolyline(new double[]{110, 140, 110, 140},
      new double[]{210, 210, 240, 240}, 4);
  }
  
  
  
  
  @Override
  public void start(Stage primaryStage) {
    TabPane tabPane = new TabPane();
    BorderPane borderPane = new BorderPane();
    for (int i = 0; i< layoutPlugin.getLayoutCount(); i++) {
      
      Layout layout = layoutPlugin.getLayout(i);
      Dimensions dim = layout.getDimensions();
      Canvas canvas = new Canvas(Math.min(640, dim.getWidth()), Math.min(480, dim.getHeight()));
      GraphicsContext gc = canvas.getGraphicsContext2D();
      for (int j = 0; j < rIds.length; j++) {
        redraw(model.getReaction(rIds[j]), gc);
      }
      
      Tab tab = new Tab();
      tab.setText(layout.isSetName() ? layout.getName() : layout.getId());
      tab.setContent(canvas);
      tabPane.getTabs().add(tab);
    }
    
    primaryStage.setTitle("Drawing Operations Test");
    Group root = new Group();
    Scene scene = new Scene(root, 700, 500, Color.WHITE);
    
    // bind to take available space
    borderPane.prefHeightProperty().bind(scene.heightProperty());
    borderPane.prefWidthProperty().bind(scene.widthProperty());
    
    borderPane.setCenter(tabPane);
    root.getChildren().add(borderPane);
    
    primaryStage.setScene(scene);
    primaryStage.show();
  }
  
}
