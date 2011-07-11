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
package de.zbit.kegg.gui;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import keggapi.Definition;
import de.zbit.gui.GUITools;
import de.zbit.gui.LayoutHelper;
import de.zbit.kegg.KeggFunctionManagement;
import de.zbit.kegg.KeggQuery;
import de.zbit.util.CustomObject;
import de.zbit.util.StringUtil;

/**
 * This will show a panel to choose one of Kegg's pathways.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class PathwaySelector extends JPanel {
  private static final long serialVersionUID = -7398698588689150610L;
  
  /**
   * This is the url that is used to download the pathway list, if the local
   * file does not exist.
   */
  //private final static String pathwayListUrl = "ftp://ftp.genome.jp/pub/kegg/pathway/map_title.tab";
  
  /**
   * This is the local file that will be used as cache for the
   * pathway list and will be opened first.
   */
  //private final static String pathwayListLocal = "de/zbit/kegg/map_title.tab";
  
  /**
   * The manager that is used to retrieve the organisms from kegg. (NOT the pathways).
   */
  private KeggFunctionManagement manag;
  
  /**
   * Our list of organisms. (Static, so they are cached).
   */
  private static HashMap<String, String> pathwayMap;
  
  /**
   * The pathway Selector.
   */
  private JComboBox pathwaySelector;
  
  /**
   * The organism Selector.
   */
  private OrganismSelector orgSel;
  
  
  public PathwaySelector() throws Exception {
    this(null, null);
  }
  
  public PathwaySelector(LayoutHelper lh) throws Exception {
    this(null, lh);
  }
  
  public PathwaySelector(KeggFunctionManagement manag) throws Exception {
    this(manag, null);
  }
  
  public PathwaySelector(KeggFunctionManagement manag, LayoutHelper lh) throws Exception {
    this(manag,lh,null);
  }
  
  /** 
   * @param manag cache to use
   * @param lh LayoutHelper to use
   * @param fixedOrganismKeggAbbr if not null, will preselect an organism and deactivate the organism selector
   * @throws Exception
   */
  public PathwaySelector(KeggFunctionManagement manag, LayoutHelper lh, String fixedOrganismKeggAbbr) throws Exception {
    super();
    
    if (manag==null) manag = new KeggFunctionManagement();
    if (lh==null) lh = new LayoutHelper(this);
    this.manag = manag;
    
    initGui(lh, fixedOrganismKeggAbbr);
  }
  
  /**
   * @param lh
   * @param fixedOrganismKeggAbbr if not null, will preselect an organism and deactivate the organism selector
   * @throws Exception 
   */
  private void initGui(LayoutHelper lh, final String fixedOrganismKeggAbbr) throws Exception {
    // Create orgaism selector
    /*
     *  XXX: It would be possible to accept certain organisms and then load
     *  the pw-list organism specific via the manager (KeggFunctionManagement).
     */
    
    //if (organismABBV==null || organismABBV.length()<3) {
    orgSel = OrganismSelector.createOrganismSelectorPanel(manag, lh);
    
    // Add "<Generic (Orthologous)>" and make default selection.
    String defaultItem = "<Generic (Orthologous)>";
    OrganismSelector.defaultSelection = defaultItem;
    for (int i=lh.getContainer().getComponentCount()-1; i>=0; i--) {
      Component c = lh.getContainer().getComponent(i);
      if (c instanceof JComboBox && c.getName().equals(OrganismSelector.class.getName())) {
        ((JComboBox)c).addItem(defaultItem);
        //((JComboBox)c).setSelectedItem(defaultItem);
        break;
      }
    }
    
    // Preselect a fixed organism
    if (fixedOrganismKeggAbbr!=null) {
      orgSel.setDefeaultSelectionLater(fixedOrganismKeggAbbr);
      // Since we did not load the reference pathways, deactivate
      // the organism selector.
      orgSel.setEnabled(false);
    }
    
    // Retrieve pathway list
    final String loadingItem = "Loading list of pathways...";
    final Container parent = lh.getContainer();
    GUITools.disableOkButton(parent);
    SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
      @Override
      protected Object doInBackground() {
        HashMap<String, String> orgs=null;
        try {
          orgs = getPathways(fixedOrganismKeggAbbr);
        } catch (IOException e) {
          GUITools.showErrorMessage(parent, e);
          e.printStackTrace();
        }
        return orgs;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      protected void done() {
        HashMap<String, String> orgs=null;
        try {
          orgs = (HashMap<String, String>) get();
        } catch (Exception e) {
          GUITools.showErrorMessage(parent, e);
        }
        if (orgs==null) {
          GUITools.showErrorMessage(parent, "Could not retrieve pathway list from KEGG.");
        } else {
          // Sort
          List<String> pathways = new LinkedList<String>(orgs.values());
          Collections.sort(pathways);
          
          // Add items to organism selector
          //organismSelector.removeAllItems();
          pathwaySelector.removeItem(loadingItem);
          for (String pw: pathways) {
            pathwaySelector.addItem(pw);
          }
          pathwaySelector.setEnabled(true);
          
          // Try to refresh parent (dialog size can only be changed by calling "pack").
          GUITools.packParentWindow(parent);
          GUITools.enableOkButtonIfAllComponentsReady(parent);
          
          if (fixedOrganismKeggAbbr!=null) {
            orgSel.getOrganisms(); // Wait to finish the dialog
            GUITools.enableOkButton(parent);
          }
          
        }
      }
    };
    
    
    // Create pathway selector
    pathwaySelector = new JComboBox();
    pathwaySelector.setName(this.getClass().getName());
    pathwaySelector.addItem(loadingItem);
    pathwaySelector.setEnabled(false);
    
    lh.add("Select pathway", pathwaySelector, true);
    worker.execute();
  }

  /**
   * @return all reference kegg pathways (multi-organism).
   * @throws IOException
   */
  @SuppressWarnings("unused")
  private HashMap<String, String> getPathways() throws IOException {
    return getPathways("map");
  }
  
  /**
   * @param organismKeggAbbr e.g. "hsa"
   * @return all kegg pathways for the given organism
   * @throws IOException
   */
  private HashMap<String, String> getPathways(String organismKeggAbbr) throws IOException {
    if (organismKeggAbbr==null || organismKeggAbbr.length()!=3) {
      organismKeggAbbr = "map";
    }
    try {
      KeggQuery query = new KeggQuery(KeggQuery.getPathways, organismKeggAbbr);
        CustomObject<Object> answer = manag.getInformation(query);
        Definition[] pathways = (Definition[]) answer.getObject();
        
        if (pathways == null || pathways.length<1) {
          GUITools.showErrorMessage(this, "Could not retrieve list of pathways from KEGG.");
          return null;
        }
        
        
        pathwayMap = new HashMap<String, String>(pathways.length);
        for (int i = 0; i < pathways.length; i++) {
          // Put e.g. Key: "04614", Value: "Renin-angiotensin system"
          // "path:map04614" => "04614"
          String idNum = pathways[i].getEntry_id();
          int pos = idNum.indexOf("map");
          if (pos>=0) idNum = idNum.substring(pos+3).trim();
          //pathwayMap.put(idNum, pathways[i].getDefinition().replace(" - Reference pathway", ""));
          int trimPos = pathways[i].getDefinition().lastIndexOf('-');
          pathwayMap.put(idNum, pathways[i].getDefinition().substring(0, trimPos<0?
              pathways[i].getDefinition().length():trimPos).trim());
        }
      return pathwayMap;
      
    } catch (Exception e) {
      GUITools.showErrorMessage(this, e);
      return null;
    }
    
  }
  /**
   * @return all kegg pathways (multi-organism).
   * @throws IOException 
   */
  /*
   * Methods to get pathway lists before 2011-07-01
  private HashMap<String, String> getPathways() throws IOException {
    
    // Try to retrieve pwlist from resources and download if it fails.
    String pwlist;
    InputStream in = OpenFile.searchFileAndGetInputStream(pathwayListLocal);
    if (in == null) {
      pwlist = downloadAndCachePWList();
    } else {
      // Convert the InputStream to a string (the content...)
      try {
        InputStreamReader insr = new InputStreamReader(in);
        BufferedReader buff = new BufferedReader(insr);
        
        StringBuffer pwlist2 = new StringBuffer();
        String line;
        while ((line = buff.readLine())!=null) {
          pwlist2.append(line);
          pwlist2.append('\n');
        }
        pwlist = pwlist2.toString();
        
        
        buff.close();
        insr.close();
      } catch (Exception e) {e.printStackTrace();pwlist=null;} // is handled in next line
      
      // FallBack to download
      if (pwlist==null || pwlist.trim().length()<1)
        pwlist = downloadAndCachePWList();
    }
    
    // Process pwlist and split to pathwayMap
    String[] lines = pwlist.split("\n");
    pathwayMap = new HashMap<String, String>();
    for (String line: lines) {
      if (line==null || line.length()<1 || line.startsWith("#") || line.startsWith(";")) continue;
      String[] kv = line.split("\t");
      if (kv.length<2) continue;
      pathwayMap.put(kv[0], kv[1]); // e.g. "00052" => "Galactose metabolism"
    }
    return pathwayMap;
  }
  
  private String downloadAndCachePWList() throws IOException {
    String pwlist;
    OutputStream out = new ByteArrayOutputStream();
    FileDownload.download(pathwayListUrl, new BufferedOutputStream(out));
    pwlist = out.toString();
    try {
      System.out.println(de.zbit.resources.Resource.class.getName());
      de.zbit.util.Utils.writeFile(pathwayListLocal, pwlist);
    } catch (IOException t) {} // Not that important. Just a cache.
    return pwlist;
  }
  */
  
  
  /**
   * Just for testing.
   * @param args
   */
  public static void main(String[] args) {
    GUITools.initLaF("PathwaySelector test");
    JPanel p = new JPanel();
    
    try {
      PathwaySelector selector = createPathwaySelectorPanel(new LayoutHelper(p));
      if (JOptionPane.showConfirmDialog(null, p, "Test",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {        
        System.out.println(selector.getSelectedPathway());
        System.out.println(selector.getSelectedPathwayID());
        System.out.println(selector.getOrganismSelector().getSelectedOrganism());
        System.out.println(selector.getOrganismSelector().getSelectedOrganismAbbreviation());
      }
      
    } catch (Throwable exc) {
      GUITools.showErrorMessage(null, exc);
    }
    
    
  }
  
  /**
   * @return the selected Pathway.
   */
  public String getSelectedPathway() {
    return pathwaySelector.getSelectedItem().toString();
  }
  
  /**
   * @return the kegg id for the selected organism-specific Pathway (e.g. hsa05410).
   */
  public String getSelectedPathwayID() {
    // Get the KEY of the selected pathway
    String selID = null;
    String selItem = pathwaySelector.getSelectedItem().toString();
    for (Map.Entry<String,String> e: pathwayMap.entrySet()) {
      if (e.getValue().equals(selItem)) selID = e.getKey();
    }
    if (selID==null) return null;
    
    // Convert e.g. "path:hsa05410" to 05410.
    selID = StringUtil.removeAllNonDigits(selID);
    
    // Prepend the organism
    String abv = getOrganismSelector().getSelectedOrganismAbbreviation();
    if (abv==null) abv = "ko";
    
    return abv + selID;
  }
  
  /**
   * If no organism has been given, this panel creates also an
   * {@link #getOrganismSelector()} which is returned using this
   * method.
   * @return OrganismSelector
   */
  public OrganismSelector getOrganismSelector() {
    return orgSel;
  }
  
  
  /**
   * Creates a new {@link #PathwaySelector} without any cache.
   * @return {@link #PathwaySelector}
   * @throws Exception 
   */
  public static PathwaySelector createPathwaySelectorPanel() throws Exception {
    PathwaySelector ret = new PathwaySelector();
    
    return ret;
  }
  
  /**
   * Creates a new {@link #PathwaySelector} without any cache.
   * @param layoutHelper
   * @return {@link #PathwaySelector}
   * @throws Exception 
   */
  public static PathwaySelector createPathwaySelectorPanel(LayoutHelper layoutHelper) throws Exception {
    PathwaySelector ret = new PathwaySelector(layoutHelper);
    
    return ret;
  }
  
  public static PathwaySelector createPathwaySelectorPanel(
    KeggFunctionManagement manag, LayoutHelper layoutHelper) throws Exception {
    
    PathwaySelector ret = new PathwaySelector(manag, layoutHelper);
    
    return ret;
  }
  
  
}
