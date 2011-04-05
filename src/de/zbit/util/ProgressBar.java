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
package de.zbit.util;


/**
 * Draws a nice graphical ASCII/ANSI Prograss bar on the console.
 * Auto detects if output is piped to a file or virtual console (e.g. Eclipse Output window) and
 * simply outputs percentages in this case.
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class ProgressBar extends AbstractProgressBar {
  private static final long serialVersionUID = 2073719565121276629L;
  
  private int lastPerc=-1;
  private static boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"))?true:false;
  protected boolean useSimpleStyle = useSimpleStyle();
  private int consoleWidth = (useSimpleStyle?0:ConsoleTools.getColumns());
  
  /**
   * Initialize the progressBar object
   * @param totalCalls - how often you are planning to call the "DisplayBar" method.
   */
  public ProgressBar(int totalCalls) {
    setNumberOfTotalCalls(totalCalls);
  }
  
  /**
   * Initialize the progressBar object
   * @param totalCalls - how often you are planning to call the "DisplayBar" method.
   */
  public ProgressBar(long totalCalls) {
    setNumberOfTotalCalls(totalCalls);
  }
  
  /**
   * 
   * @param aufrufeGesamt
   * @param estimateTime
   */
  public ProgressBar(int totalCalls, boolean estimateTime) {
    this(totalCalls);
    setEstimateTime(estimateTime);
  }
  
  /**
   * @return if there is a ANSI compliant console available.
   */
  public boolean isSimpleStyle() {
    return useSimpleStyle;
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.util.aProgressBar#drawProgressBar(int, double, java.lang.String)
   */
  protected synchronized void drawProgressBar(int percent, double miliSecondsRemaining, String additionalText) {
    String percString = percent + "%";
    
    // Calculate time remaining
    String ETA="";
    if (getEstimateTime() && miliSecondsRemaining>=0) {
      ETA = "ETR: " + Utils.getTimeString((long) miliSecondsRemaining);
    }

    // Simples File-out oder Eclipse-Output-Window tool. Windows Console unterstützt leider auch kein ANSI.
    if (useSimpleStyle) {
      if (percent!=lastPerc) {
        System.out.println(percString + ' ' + ETA + (additionalText!=null && (additionalText.length()>0)? " " + additionalText:"") );
        lastPerc=percent;
      }
      return;
    }
    
    
    // Adjust bar width to fit in line.
    int kMax = 50; // = ProgressBar width
    if (consoleWidth>0) {
      // [BarWidth]+ 2(for animation) + (ETA+1) + (additionalText+1)
      int additionalSpace = 2 + ((ETA!=null&&ETA.length()>0)?(ETA.length()+1):0);
      additionalSpace+= ((additionalText!=null&&additionalText.length()>0)?(additionalText.length()+1):0);
      int totalStringWidth = kMax + additionalSpace;
      if (totalStringWidth>consoleWidth) kMax = consoleWidth-additionalSpace;
      kMax = Math.max(kMax, 4); // At least four chars for "100%" are required.
    }
    
    // Nice-and cool looking ANSI ProgressBar ;-)
    String anim= "|/-\\";
    StringBuilder sb = new StringBuilder();
    int x = percent / 2;
    sb.append("\r\033[K"); // <= clear line, Go to beginning
    sb.append("\033[107m"); // Bright white bg color
    for (int k = 0; k < kMax; k++) {
      if (x==k) sb.append("\033[100m"); // grey like bg color
      
      /*
      // % Zahl ist immer am "Farbschwellwert" (klebt am rechten bankenrand)
      if (x<percString.length()) {
        if (x<=k && k<x+percString.length()) sb.append("\033[93m"+percString.charAt(k-x)); // yellow
        else sb.append(" ");
      } else {
        if (k<x && (x-percString.length())<=k) sb.append("\033[34m"+percString.charAt(1-(x-percString.length()-k+1))); // blue 
        else sb.append(" ");
      }*/
      
      // %-Angabe zentriert
      int pStart = kMax/2-percString.length()/2;
      int pEnd = kMax/2+percString.length()/2;
      if (k>=pStart && k<=pEnd) {
        char c = ' ';
        if (k-pStart<percString.length()) c = percString.charAt(k-pStart);
        if (x<=k) sb.append("\033[93m"+c); // Foreground colors
        if (x> k) sb.append("\033[34m"+c); // Text colors
      } else
        sb.append(" ");
      
    }
    
    // Reset colors and stuff.
    sb.append("\033[0m");
    
    // Animated char
    sb.append(" \033[93m" + anim.charAt((int) (getCallNumber() % anim.length()))  + "\033[1m");
    
    // ETA
    if (ETA.length()>0) sb.append(' ' + ETA);
    
    // Additional Text
    if (additionalText!=null && additionalText.length()>0) sb.append(' ' + additionalText);
    
    // Reset colors and stuff.
    sb.append("\033[0m");
    
    //   \033[?25l  <=hide cursor.
    //   \033[?25h  <=show cursor.
    
    try {
      //System.console().writer().print(sb.toString()); // Not supported in Java 1.5!
      //System.console().flush();
      System.out.print(sb.toString());
    } catch (Exception e) {e.printStackTrace();}
    
    return; // sb.toString();
  }
  
  /**
   * Determines if ANSI compliance console commands can be used,
   * based on java version, os type and outputStream Type.
   * @return
   */
  protected static boolean useSimpleStyle() {
    boolean useSimpleStyle = false;
    if (isWindows) {
      useSimpleStyle = true; // MS Windows has (by default) no ANSI capabilities.
    } else {
      useSimpleStyle = !ConsoleTools.isTTY();
    }
    
    return useSimpleStyle;
  }
  
  public void finished() {
    if (!useSimpleStyle) System.out.println();
  }
  
}
