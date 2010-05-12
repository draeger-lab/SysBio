package de.zbit.util;


/**
 * Draws a nice graphical ASCII/ANSI Prograss bar on the console.
 * Auto detects if output is piped to a file or virtual console (e.g. Eclipse Output window) and
 * simply outputs percentages in this case.
 * @author wrzodek
 */
public class ProgressBar {
  
  /**
   * 
   */
  private int aufrufeGesamt=0; // SET THIS VALUE!
  /**
   * 
   */
  private boolean estimateTime=false;
  
  /** 
   * Internal variables (not to set by user).
   */
  private int aufrufNr=0;
  /**
   * 
   */
  private int lastPerc=-1;
  /**
   * 
   */
  private boolean isWindows;
  
  /** 
   * for time duration estimations
   */
  private long measureTime = 0;
  /**
   * 
   */
  private int numMeasurements = 0;
  /**
   * 
   */
  private long lastCallTime;
  
  /**
   * 
   */
  protected boolean useSimpleStyle = useSimpleStyle();
  
  /**
   * Initialize the progressBar object
   * @param aufrufeGesamt - how often you are planning to call the "DisplayBar" method.
   */
  public ProgressBar(int aufrufeGesamt) {
    this.aufrufeGesamt = aufrufeGesamt;
    isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"))?true:false;
  }
  
  /**
   * 
   * @param aufrufeGesamt
   * @param estimateTime
   */
  public ProgressBar(int aufrufeGesamt, boolean estimateTime) {
    this(aufrufeGesamt);
    this.estimateTime = estimateTime;
    if (estimateTime) lastCallTime = System.currentTimeMillis();
  }
  
  /**
   * Please see "{@link DisplayBar(String additionalText)}".
   */
  public synchronized void DisplayBar() {
    DisplayBar(null);
  }
  
  /**
   * This function should be called exactly as aften as defined in the constructor. 
   * It will draw or update a previously drawn progressBar.
   * @param additionalText - Any additional text (e.g. "Best item found so far XYZ")
   */
  public synchronized void DisplayBar(String additionalText) {
    // ANSI Codes siehe http://en.wikipedia.org/wiki/ANSI_escape_code
    aufrufNr++;
    int perc = Math.min((int)((double)aufrufNr/(double)aufrufeGesamt*100), 100);
    String percString = perc + "%";
    
    // Calculate time remaining
    String ETA="";
    if (estimateTime) {
      measureTime += System.currentTimeMillis() - lastCallTime;
      numMeasurements++;
      
      //Infos ausgeben...
      double ScansRemaining = (aufrufeGesamt - (aufrufNr+1)); // /(double)MLIBSVMSettings.runs;
      double secsRemaining = ScansRemaining * ((measureTime/(double)numMeasurements)/1000.0) ;
      ETA = " ETR: " + Utils.getTimeString((long) secsRemaining);
    }

    // Simples File-out oder Eclipse-Output-Window tool. Windows Console unterstützt leider auch kein ANSI.
    if (useSimpleStyle) {
      if (perc!=lastPerc) {
        System.out.println(percString + ETA + (additionalText!=null && (additionalText.length()>0)? " " + additionalText:"") );
        lastPerc=perc;
      }
      return;
    }
    
    // Nice-and cool looking ANSI ProgressBar ;-)
    String anim= "|/-\\";
    StringBuilder sb = new StringBuilder();
    int x = perc / 2;
    sb.append("\r\033[K"); // <= clear line, Go to beginning
    sb.append("\033[107m"); // Bright white bg color
    int kMax = 50;
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
        if (x<=k) sb.append("\033[93m"+c);
        if (x> k) sb.append("\033[34m"+c);
      } else
        sb.append(" ");
      
    }

    sb.append("\033[0m "); // Reset colors and stuff.
    sb.append("\033[93m" + anim.charAt(aufrufNr % anim.length())  + " \033[1m" +  ETA.trim() + (ETA.length()>0?" ":"") + (additionalText!=null && (additionalText.length()>0)? additionalText:""));
    sb.append("\033[0m");
    
    //   \033[?25l  <=hide cursor.
    //   \033[?25h  <=show cursor.
    
    try {
      //System.console().writer().print(sb.toString()); // XXX: Not supported in Java 1.5
      //System.console().flush();
      System.out.print(sb.toString());
    } catch (Exception e) {e.printStackTrace();}
    
    return; // sb.toString();
  }
  
  /**
   * Determines if ANSI compliance console commands can be used, based on java version, os type and outputStream Type.
   * @return
   */
  protected boolean useSimpleStyle() {
    boolean useSimpleStyle = false;
    if (isWindows) useSimpleStyle = true; // MS Windows has (by default) no ANSI capabilities.
    
    // is TTY Check is only available for java 1.6. So a wrapper to determine java version is needed for Java 1.5 compatibility.
    String v = System.getProperty("java.version");
    if (v!=null && v.length()>2) {
      try {
        double d = Double.parseDouble(v.substring(0, 3));
        if (d<1.6) useSimpleStyle = true;
        else useSimpleStyle = !isTTY_Java16only.isTty();
      } catch (Exception e) {
        useSimpleStyle = true;
      }
    }
    
    return useSimpleStyle;
  }
  
}
