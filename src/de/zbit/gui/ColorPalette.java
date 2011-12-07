/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.awt.Color;

/**
 * The colors of the University of T&uuml;bingen. 
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.1
 * @date 2011-03-27
 */
public class ColorPalette {
		
	/**
	 * 
	 */
	public static final Color CAMINE_RED = new Color(139, 36, 64); 
		// new Color(CMYK, new float[] {35, 100, 70, 10}, 0f);
	
	/**
	 * 
	 */
	public static final Color CAMINE_RED_50_PERCENT = new Color(181, 124, 127); 
		//new Color(CMYK, new float[] {18, 50, 35, 5}, 0f);
	
	/**
	 * 
	 */
	public static final Color GOLD = new Color(147, 117, 76); 
		//new Color(CMYK, new float[] {37, 51, 80, 1}, 0f);
	
	/**
	 * 
	 */
	public static final Color GOLD_50_PERCENT = new Color(206, 192, 174); 
		//new Color(CMYK, new float[] {12, 16, 24, 1}, 0f);
	
	/**
	 * Dark blue
	 */
	public static final Color SECOND_653 = new Color(64, 78, 118); 
		//new Color(CMYK, new float[] {70, 50, 0, 35}, 0f);
	
	/**
	 * Blue
	 */
	public static final Color SECOND_3015 = new Color(0, 93, 151); 
		// new Color(CMYK, new float[] {100, 50, 10, 0}, 0f);
	
	/**
	 * Light blue
	 */
	public static final Color SECOND_292 = new Color(63, 145, 182); 
		// new Color(CMYK, new float[] {60, 0, 0, 20}, 0f);
	
	/**
	 * Blue-green
	 */
	public static final Color SECOND_557 = new Color(95, 169, 146); 
		//new Color(CMYK, new float[] {50, 0, 40, 10}, 0f);
	
	/**
	 * Green
	 */
	public static final Color SECOND_7490 = new Color(93, 150, 81); 
		// new Color(CMYK, new float[] {50, 0, 80, 20}, 0f);
	
	/**
	 * Dark green
	 */
	public static final Color SECOND_364 = new Color(40, 96, 38); 
		// new Color(CMYK, new float[] {70, 00, 100, 50}, 0f);
	
	/**
	 * Warm red
	 */
	public static final Color SECOND_180 = new Color(180, 81, 65); 
		//new Color(CMYK, new float[] {20, 80, 80, 0}, 0f);
	
	/**
	 * Purple
	 */
	public static final Color SECOND_6880 = new Color(153, 97, 136); 
		// new Color(CMYK, new float[] {20, 60, 0, 20}, 0f);
	
	/**
	 * Gray-purple
	 */
	public static final Color SECOND_7530 = new Color(154, 134, 129);
		// new Color(CMYK, new float[] {20, 30, 30, 20}, 0f);
	
	/**
	 * Sand
	 */
	public static final Color SECOND_7508 = new Color(186, 159, 104); 
		// new Color(CMYK, new float[] {20, 30, 65, 0}, 0f);
	
	/**
	 * Brown
	 */
	public static final Color SECOND_7505 = new Color(123, 96, 70); 
		// new Color(CMYK, new float[] {30, 50, 70, 30}, 0f);
	
	/**
	 * Brown-yellow 
	 */
	public static final Color SECOND_131 = new Color(191, 138, 28); 
		// new Color(CMYK, new float[] {10, 40, 100, 10}, 0f);
	
	/**
	 * 
	 */
	public static final Color ANTHRACITE = new Color(50, 56, 59); 
		// new Color(CMYK, new float[] {30, 0, 0, 85}, 0f);
	
	/**
	 * Has 27 colors and returns one color for any index (using modulo).
	 * 
   * @param index
   * @return
   */
  public static Color indexToColor(int index) {
    switch (index % 27) {
    case 0:
      return ANTHRACITE;
    case 1:
      return SECOND_292;
    case 2:
      return SECOND_131;
    case 3:
      return SECOND_180;
    case 4:
      return SECOND_3015;
    case 5:
      return SECOND_364;
    case 6:
      return SECOND_557;
    case 7:
      return SECOND_653;
    case 8:
      return SECOND_6880;
    case 9:
      return SECOND_7490;
    case 10:
      return SECOND_7505;
    case 11:
      return SECOND_7508;
    case 12:
      return SECOND_7530;
    case 13:
      return GOLD;
    case 14:
      return CAMINE_RED;
    case 15:
      return CAMINE_RED_50_PERCENT;
    case 16:
      return GOLD_50_PERCENT;
    case 17:
      return Color.BLACK;
    case 18:
      return Color.RED;
    case 19:
      return Color.BLUE;
    case 20:
      return Color.PINK;
    case 21:
      return Color.GREEN;
    case 22:
      return Color.GRAY;
    case 23:
      return Color.MAGENTA;
    case 24:
      return Color.CYAN;
    case 25:
      return Color.ORANGE;
    case 26:
      return Color.DARK_GRAY;
    }
    return Color.BLACK;
  }
	
}
