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
package de.zbit.graph.sbgn;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ResourceBundle;

import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.objectwrapper.ValuePairUncomparable;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * Options to let the user decide how SBGN elements should appear.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public interface DrawingOptions extends KeyProvider {
  
  /**
   * Localization support
   */
  public static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.graph.locales.Labels");

  /**
   * Decide if inner part of compartment glyphs should be filled, or if only the
   * outside should be drawn.
   */
  public static final Option<Boolean> COMPARTMENT_FILLED = new Option<Boolean>(
      "COMPARTMENT_FILLED", Boolean.class, bundle, Boolean.TRUE);
  
  /**
   * Defaults to dark yellow
   */
  public static final Option<Color> COMPARTMENT_LINE_COLOR = new Option<Color>(
      "COMPARTMENT_LINE_COLOR", Color.class, bundle, new Color(204, 204, 0));
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static final Option<Color> COMPARTMENT_FILL_COLOR = new Option<Color>(
      "COMPARTMENT_FILL_COLOR", Color.class, bundle, new Color(243, 243, 191),
      new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
          COMPARTMENT_FILLED,
          new Range<Boolean>(Boolean.class, Boolean.TRUE)));
  
  /**
   * 
   */
  public static final Option<Double> COMPARTMENT_LINE_WIDTH = new Option<Double>(
      "COMPARTMENT_LINE_WIDTH", Double.class, bundle, new Range<Double>(
          Double.class, "{[0,1000]}"), Double.valueOf(12d));
  
  /**
   * Defaults to DodgerBlue3.
   */
  public static final Option<Color> NONCOVALENT_COMPLEX_FILL_COLOR = new Option<Color>(
      "NONCOVALENT_COMPLEX_FILL_COLOR", Color.class, bundle, new Color(24, 116, 205));
  
  /**
   * Defaults to Yellow.
   */
  public static final Option<Color> GENE_FILL_COLOR = new Option<Color>(
      "GENE_FILL_COLOR", Color.class, bundle, new Color(255,255,0));
  
  /**
   * Defaults to Green 3.
   */
  public static final Option<Color> MACROMOLECULE_FILL_COLOR = new Option<Color>(
      "MACROMOLECULE_FILL_COLOR", Color.class, bundle, new Color(0, 205, 0));
  
  /**
   * Defaults to LightSkyBlue1.
   */
  public static final Option<Color> SIMPLE_MOLECULE_FILL_COLOR = new Option<Color>(
      "SIMPLE_MOLECULE_FILL_COLOR", Color.class, bundle, new Color(176, 226, 255));
  
  /**
   * Defaults to azure2.
   */
  public static final Option<Color> MAP_FILL_COLOR = new Option<Color>(
      "MAP_FILL_COLOR", Color.class, bundle, new Color(224, 238, 238));
  
  /**
   * Defaults to pink.
   */
  public static final Option<Color> EMPTY_SET_FILL_COLOR = new Option<Color>(
      "EMPTY_SET_FILL_COLOR", Color.class, bundle, new Color(255, 204, 204));
  
  /**
   * Defaults to drug pink.
   */
  public static final Option<Color> PERTURBING_AGENT_FILL_COLOR = new Option<Color>(
      "PERTURBING_AGENT_FILL_COLOR", Color.class, bundle, new Color(255, 0, 255));
  
  /**
   * Defaults to light green.
   */
  public static final Option<Color> DEFAULT_FILL_COLOR = new Option<Color>(
      "DEFAULT_FILL_COLOR", Color.class, bundle, new Color(144, 238, 144));
  
  /**
   * Defaults to {@link Color#BLACK}.
   */
  public static final Option<Color> DEFAULT_LINE_COLOR = new Option<Color>(
      "DEFAULT_LINE_COLOR", Color.class, bundle, Color.BLACK);
  
  /**
   * Defaults font for text labels.
   */
  public static final Option<Font> FONT = new Option<Font>(
      "FONT", Font.class, bundle, new Font("Arial", Font.PLAIN, 12));
  
  /**
   * Defaults to {@link Color#BLACK}.
   */
  public static final Option<Color> FONT_COLOR = new Option<Color>(
      "FONT_COLOR", Color.class, bundle, Color.BLACK);
  
  /**
   * Decide if in sloppy representations text should be drawn. By default, text
   * will be omitted in sloppy drawings (highly zoomed out).
   */
  public static final Option<Boolean> PAINT_SLOPPY_TEXT = new Option<Boolean>(
      "PAINT_SLOPPY_TEXT", Boolean.class, bundle, Boolean.FALSE);
  
  /**
   * Defaults to 2 px.
   */
  public static final Option<Double> GLYPH_LINE_WIDTH = new Option<Double>(
      "GLYPH_LINE_WIDTH", Double.class, bundle, new Range<Double>(Double.class,
          "{[0, 1024]}"), Double.valueOf(1d));
  
  /**
   * Defaults to 2 px.
   */
  public static final Option<Double> EDGE_LINE_WIDTH = new Option<Double>(
      "EDGE_LINE_WIDTH", Double.class, bundle, new Range<Double>(Double.class,
          "{[0, 1024]}"), Double.valueOf(1d));
  
  /**
   * Define color and appearance of compartment glyphs.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> COMPARTMENT_GLYPH = new OptionGroup(
    "COMPARTMENT_GLYPH", bundle, COMPARTMENT_LINE_WIDTH,
    COMPARTMENT_LINE_COLOR, COMPARTMENT_FILLED, COMPARTMENT_FILL_COLOR);
  
  /**
   * Define drawing properties of graphical elements other than compartments.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> GLYPH_PREFS = new OptionGroup(
    "GLYPH_PREFS", bundle, GLYPH_LINE_WIDTH, DEFAULT_LINE_COLOR,
    DEFAULT_FILL_COLOR, EMPTY_SET_FILL_COLOR, GENE_FILL_COLOR,
    MACROMOLECULE_FILL_COLOR, MAP_FILL_COLOR, NONCOVALENT_COMPLEX_FILL_COLOR,
    PERTURBING_AGENT_FILL_COLOR, SIMPLE_MOLECULE_FILL_COLOR);
  
  /**
   * Define how reactions should be drawn.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> REACTION_PREFS = new OptionGroup(
    "REACTION_PREFS", bundle, EDGE_LINE_WIDTH);
  
  /**
   * Define which fonts and colors to use for text labels and when these should
   * be shown.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<?> FONT_STYLE = new OptionGroup("FONT_STYLE",
    bundle, FONT, FONT_COLOR, PAINT_SLOPPY_TEXT);
  
}
