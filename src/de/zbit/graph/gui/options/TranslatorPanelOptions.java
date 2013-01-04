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
 * Copyright (C) 2011-2013 by the University of Tuebingen, Germany.
 *
 * KEGGtranslator is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.gui.options;

import de.zbit.graph.gui.TranslatorPanel;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * Contains options for the {@link TranslatorPanel}.
 * @author Clemens Wrzodek
 * @since 1.0
 * @version $Rev$
 */
public abstract interface TranslatorPanelOptions extends KeyProvider{
  
  /**
   * A range that just contains {@code false}.
   */
  static Range<Boolean> FALSE_RANGE = new Range<Boolean>(Boolean.class, Boolean.FALSE);
  
  /**
   * A range that just contains {@code true}.
   */
  static Range<Boolean> TRUE_RANGE = new Range<Boolean>(Boolean.class, Boolean.TRUE);
  
  
  // TODO: Move those options to an extending KEGGtranslator class.
//  /**
//   * If true, shows the KEGGtranslator picture in every graph frame
//   * as background image.
//   */
//	public static final Option<Boolean> SHOW_LOGO_IN_GRAPH_BACKGROUND = new Option<Boolean>(
//		"SHOW_LOGO_IN_GRAPH_BACKGROUND", Boolean.class, String.format(
//			"If true, shows the %s logo in the background of each graph.", System
//					.getProperty("app.name")), false);
//
//  /**
//   * If true, shows the original KEGG picture in the background layer of a translated graph.
//   */
//  public static final Option<Boolean> SHOW_KEGG_PICTURE_IN_GRAPH_BACKGROUND = new Option<Boolean>("SHOW_KEGG_PICTURE_IN_GRAPH_BACKGROUND",Boolean.class,
//      "If true, shows the original KEGG picture in the background layer of a translated graph.", true, SHOW_LOGO_IN_GRAPH_BACKGROUND, FALSE_RANGE);
//  
//  /**
//   * Select percentage for brightening the KEGG background image.
//   */
//  public static final Option<Integer> BRIGHTEN_KEGG_BACKGROUND_IMAGE = new Option<Integer>("BRIGHTEN_KEGG_BACKGROUND_IMAGE",Integer.class,
//      "Select percentage for brightening the KEGG background image.", new Range<Integer>(Integer.class, "{[0,100]}"), 80, SHOW_KEGG_PICTURE_IN_GRAPH_BACKGROUND, TRUE_RANGE);
  
  /**
   * Shows an overview and navigation panel in every graph frame.
   */
  public static final Option<Boolean> SHOW_NAVIGATION_AND_OVERVIEW_PANELS = new Option<Boolean>("SHOW_NAVIGATION_AND_OVERVIEW_PANELS",Boolean.class,
      "If true, shows a navigation and overview panel on the left side of each graph.", true);

  /**
   * Show a table with the node/edge properties on the right side.
   */
  public static final Option<Boolean> SHOW_PROPERTIES_TABLE = new Option<Boolean>("SHOW_PROPERTIES_TABLE",Boolean.class,
      "If true, shows a properties table on the upper right corner of each graph.", true);
  
  /**
   * Layout edges everytime a graph is displayed.
   * Default should be false as this changes the docking of SBML edges to reaction nodes!
   */
  public static final Option<Boolean> LAYOUT_EDGES = new Option<Boolean>("LAYOUT_EDGES",Boolean.class,
      "If true, performs an organic edge routing algorithm for every graph.", false);
  
  /**
   * Draw edges on top. This was requested (with default=true) by some reviewers.
   */
  public static final Option<Boolean> DRAW_EDGES_ON_TOP_OF_NODES = new Option<Boolean>("DRAW_EDGES_ON_TOP_OF_NODES",Boolean.class,
      "If this is selected, edges are drawn on top of nodes. Else, edges will be drawn first and subsequently, they will be below all nodes.", true);
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup GRAPH_PANEL_OPTIONS = new OptionGroup(
      "Graph-panel visualization options",
      "Define various options that control the look and feel of GraphML visualizing panels.",
      //SHOW_LOGO_IN_GRAPH_BACKGROUND, SHOW_KEGG_PICTURE_IN_GRAPH_BACKGROUND, BRIGHTEN_KEGG_BACKGROUND_IMAGE,
      SHOW_NAVIGATION_AND_OVERVIEW_PANELS, SHOW_PROPERTIES_TABLE, LAYOUT_EDGES, DRAW_EDGES_ON_TOP_OF_NODES);
  
  
  
}
