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

import java.awt.Container;
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

/**
 * This will show a panel to choose one of Kegg's supported organisms.
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class OrganismSelector extends JPanel {
  private static final long serialVersionUID = -45232441769930121L;

  /**
   * The manager that is used to retrieve the organisms from kegg.
   */
  private KeggFunctionManagement manag;
  
  /**
   * Our list of organisms. (Static, so they are cached).
   */
  private static HashMap<String, String> organismMap;
  
  /**
   * The Organism Selector.
   */
  private JComboBox organismSelector;
  
  public static String defaultSelection = "Homo sapiens (human)";
  
  /**
   * This is a convenient constructor. However, the {@link #OrganismSelector(KeggFunctionManagement)}
   * constroctor should be prefered and the cache (KeggFunctionManagement object)
   * should be saved after closing this panel.
   * @throws Exception 
   */
  public OrganismSelector() throws Exception {
    this (null);
  }
  
  /**
   * 
   * @param manag
   * @throws Exception 
   */
  public OrganismSelector(KeggFunctionManagement manag) throws Exception {
    this(manag, null);
  }
  
  /**
   * 
   * @param manag
   * @param lh - if null, a new LayoutHelper will be created on this panel.
   * @throws Exception
   */
  public OrganismSelector(KeggFunctionManagement manag, LayoutHelper lh) throws Exception {
    super();
    
    if (manag==null) manag = new KeggFunctionManagement();
    if (lh==null) lh = new LayoutHelper(this);
    this.manag = manag;
    
    initGui(lh);
  }
  
  /**
   * Creates the {@link #organismSelector} and puts it on
   * the given layoutHelper.
   * @param lh
   * @throws Exception
   */
  private void initGui(LayoutHelper lh) throws Exception {
    /*
     * Retrieve organisms via SwingWorker to avoid application "freeze"
     */
    final String loadingItem = "Loading list of organisms...";
    final Container parent = lh.getContainer();
    SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
      @Override
      protected Object doInBackground() {
        HashMap<String, String> orgs = getOrganisms();
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
          GUITools.showErrorMessage(parent, "Could not retrieve organisms from KEGG.");
        } else {
          // Sort
          List<String> organisms = new LinkedList<String>(orgs.values());
          Collections.sort(organisms);
          
          // Add items to organism selector
          //organismSelector.removeAllItems();
          organismSelector.removeItem(loadingItem);
          for (String org: organisms) {
            organismSelector.addItem(org);
            if (defaultSelection!=null && defaultSelection.length()>0 &&
                org.contains(defaultSelection)) {
              organismSelector.setSelectedItem(org);
            }
          }
          organismSelector.setEnabled(true);
          
          
          // Try to refresh parent (dialog size can only be changed by calling "pack").
          GUITools.packParentWindow(parent);
          GUITools.enableOkButtonIfAllComponentsReady(parent);
          
        }
      }
    };
    
    /*
    HashMap<String, String> orgs = getOrganisms();
    if (orgs==null) throw new Exception("Could not retrieve organisms from KEGG.");
    
    List<String> organisms = new LinkedList<String>(orgs.values());
    Collections.sort(organisms);
    
    organismSelector = new JComboBox();
    organismSelector.setName(this.getClass().getName());
    for (String org: organisms) {
      organismSelector.addItem(org);
      if (org.contains(defaultSelection)) {
        organismSelector.setSelectedItem(org);
      }
    }*/
    
    // Create temporary combo-box
    organismSelector = new JComboBox();
    organismSelector.setName(this.getClass().getName());
    organismSelector.addItem(loadingItem);
    organismSelector.setEnabled(false);
    
    lh.add("Select organism", organismSelector, true);
    worker.execute();
    
  }
  
  /**
   * Get organisms via {@link #manag}.
   * @return HashMap<Abbreviation, Description>. E.g. Key: "hsa", Value: "Homo sapiens (human)"
   */
  private HashMap<String, String> getOrganisms() {
    try {
      if (organismMap==null || organismMap.size()<1) {
        KeggQuery query = new KeggQuery(KeggQuery.getOrganisms,null);
        CustomObject<Object> answer = manag.getInformation(query);
        Definition[] organisms = (Definition[]) answer.getObject();
        
        if (organisms == null || organisms.length<1) {
          GUITools.showErrorMessage(this, "Could not retrieve list of organisms from KEGG.");
          return null;
        }
        
        
        organismMap = new HashMap<String, String>(organisms.length);
        for (int i = 0; i < organisms.length; i++) {
          // Put e.g. Key: "hsa", Value: "Homo sapiens (human)"
          organismMap.put(organisms[i].getEntry_id(), organisms[i].getDefinition());
        }
      }
      return organismMap;
      
    } catch (Exception e) {
      GUITools.showErrorMessage(this, e);
      return null;
    }
  }
  
  
  /**
   * Just for testing.
   * @param args
   */
  public static void main(String[] args) {
    GUITools.initLaF("OrganismSelector test");
    JPanel p = new JPanel();
    
    try {
      OrganismSelector selector = createOrganismSelectorPanel(new LayoutHelper(p));
      if (JOptionPane.showConfirmDialog(null, p, "Test",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {        
        System.out.println(selector.getSelectedOrganism());
        System.out.println(selector.getSelectedOrganismAbbreviation());
      }
      
    } catch (Throwable exc) {
      GUITools.showErrorMessage(null, exc);
    }

    
  }

  /**
   * @return the selected organism.
   */
  public String getSelectedOrganism() {
    return organismSelector.getSelectedItem().toString();
  }
  
  /**
   * @return the kegg abbreviation for the selected organism.
   */
  public String getSelectedOrganismAbbreviation() {
    String selItem = organismSelector.getSelectedItem().toString();
    if (organismMap==null) return null;
    for (Map.Entry<String,String> e: organismMap.entrySet()) {
      if (e.getValue().equals(selItem)) return e.getKey();
    }
    return null;
  }


  /**
   * Creates a new {@link #organismSelector} without any cache.
   * @return {@link #organismSelector}
   * @throws Exception 
   */
  public static OrganismSelector createOrganismSelectorPanel() throws Exception {
    OrganismSelector ret = new OrganismSelector();
    
    return ret;
  }
  
  /**
   * Creates a new {@link #organismSelector} without any cache.
   * @param layoutHelper
   * @return {@link #organismSelector}
   * @throws Exception 
   */
  public static OrganismSelector createOrganismSelectorPanel(
    LayoutHelper layoutHelper) throws Exception {
    
    OrganismSelector ret = new OrganismSelector(null, layoutHelper);
    
    return ret;
  }
  
  public static OrganismSelector createOrganismSelectorPanel(
    KeggFunctionManagement manag, LayoutHelper layoutHelper) throws Exception {
    
    OrganismSelector ret = new OrganismSelector(manag, layoutHelper);
    
    return ret;
  }
  
}
