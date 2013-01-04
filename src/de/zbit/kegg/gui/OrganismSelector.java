/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.kegg.api.cache.KeggFunctionManagement;
import de.zbit.kegg.api.cache.KeggQuery;
import de.zbit.util.Species;
import de.zbit.util.objectwrapper.CustomObject;

/**
 * This will show a panel to choose one of Kegg's supported organisms.
 * @author Clemens Wrzodek
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
   * Key is kegg abbreviation; Value is scientific name
   */
  private static HashMap<String, String> organismMap=null;
  
  /**
   * A current instance of {@link #organismMap}. Allows customizations
   * without modifying the static list.
   */
  private HashMap<String, String> currentOrganismMap=null;
  
  /**
   * The Organism Selector.
   */
  private JComboBox organismSelector;
  
  /**
   * A default selection for the ComboBox.
   */
  public static String defaultSelection = "Homo sapiens (human)";
  
  /**
   * Is set to true when this panel is completely inizialized.
   */
  private boolean isInitialized=false;

  /**
   * Listeners that are informed when this item loaded completely.
   */
  private List<ActionListener> listeners = null;

  /**
   * This is the ActionCommand String of the Event that
   * is fired to {@link #listeners} when loading is compelte.
   */
  public static String LOADING_COMPLETE_ACTION_COMMAND = "ORGANISM_LOADING_COMPLETE";
  
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
    this(manag, lh, null);
  }
  
  /**
   * @param manag
   * @param lh
   * @param onlyShowThese
   * @throws Exception 
   */
  public OrganismSelector(KeggFunctionManagement manag, LayoutHelper lh, List<Species> onlyShowThese) throws Exception {
    super();
    
    if (manag==null) manag = new KeggFunctionManagement();
    if (lh==null) lh = new LayoutHelper(this);
    this.manag = manag;
    
    initGui(lh, onlyShowThese);
  }

  /**
   * Creates the {@link #organismSelector} and puts it on
   * the given layoutHelper.
   * @param lh
   * @throws Exception
   */
  private void initGui(LayoutHelper lh, final List<Species> onlyShowThese) throws Exception {
    /*
     * Retrieve organisms via SwingWorker to avoid application "freeze"
     */
    final String loadingItem = "Loading list of organisms...";
    final Container parent = lh.getContainer();
    SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
      @Override
      protected Object doInBackground() {
        HashMap<String, String> orgs;
        if (onlyShowThese!=null && onlyShowThese.size()>0) {
          orgs = getOrganisms(onlyShowThese);
        } else {
          orgs = getOrganisms();
        }
        return orgs;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      protected void done() {
        if (Thread.currentThread().isInterrupted()) return; // don't continue
        HashMap<String, String> orgs=null;
        try {
          orgs = (HashMap<String, String>) get();
        } catch (Exception e) {
          GUITools.showErrorMessage(parent, e);
        }
        if (orgs==null) {
          GUITools.showErrorMessage(parent, "Could not retrieve organisms from KEGG.");
        } else {
          currentOrganismMap = orgs;
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
          
          // Might not be desired to enable this dialog.
          organismSelector.setEnabled(isEnabled());
          
          
          // Try to refresh parent (dialog size can only be changed by calling "pack").
          GUITools.packParentWindow(parent);
          if (isEnabled()) {
            // Only change dialog ok if this is enabled. Else, do nothing.
            GUITools.enableOkButtonIfAllComponentsReady(parent, true);
          }
          
          isInitialized=true;
        }
        repaint();
        fireOrganismBoxLoadedCompletelyListeners();
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

  public void addOrganismBoxLoadedCompletelyListener(ActionListener l) {
    if (listeners==null) listeners = new LinkedList<ActionListener>();
    listeners.add(l);
  }

  
  protected void fireOrganismBoxLoadedCompletelyListeners() {
    if (listeners==null) return;
    ActionEvent e = new ActionEvent(this, 1, LOADING_COMPLETE_ACTION_COMMAND);
    for (ActionListener l : listeners) {
      l.actionPerformed(e);
    }
  }

  public boolean isInitialized() {
    return isInitialized;
  }
  
  /**
   * Set the default selection of this organism selector.
   * @param organism as given as full name by KEGG e.g. "Homo sapiens (human)".
   */
  public void setDefaultSelection(String organism) {
    defaultSelection = organism;
    /* Panel has not yet been initialized and will take
     * automatically the defaultSelection.
     */
    if (currentOrganismMap==null) return;
    
    // Else, we have to take care by our own.
    organismSelector.setSelectedItem(organism);
  }
  
  /**
   * Will convert the given kegg abbreviation (e.g. "hsa") to the
   * organism name and set this as default value, as soon as the
   * kegg adaptor has given all organisms from kegg. This will be
   * executed as new thread!
   * @param organismKeggAbbr e.g. "hsa"
   */
  public void setDefeaultSelectionLater(final String organismKeggAbbr) {
    Thread r = new Thread() {
      @Override
      public void run() {
        // Wait until we have an answer from KEGG and
        // then convert to full name
        String fullName = getOrganisms().get(organismKeggAbbr);
        setDefaultSelection(fullName);
      }
    };
    r.start();
  }
  
  /**
   * Get organisms from <code>onlyShowThese</code>.
   * @param onlyShowThese
   * @return HashMap<Abbreviation, Description>. E.g. Key: "hsa", Value: "Homo sapiens (human)"
   */
  public HashMap<String, String> getOrganisms (List<Species> onlyShowThese) {
    HashMap<String, String> localMap = new HashMap<String, String>(onlyShowThese.size());
    for (int i = 0; i < onlyShowThese.size(); i++) {
      // Put e.g. Key: "hsa", Value: "Homo sapiens (human)"
      localMap.put(onlyShowThese.get(i).getKeggAbbr(), onlyShowThese.get(i).getScientificName());
    }
    
    return localMap;
  }
  
  /**
   * Get organisms via {@link #manag}.
   * @return HashMap<Abbreviation, Description>. E.g. Key: "hsa", Value: "Homo sapiens (human)"
   */
  public synchronized HashMap<String, String> getOrganisms() {
    try {
      synchronized (defaultSelection) { // Just anything static and not-null.
        if (organismMap==null || organismMap.size()<1) {
          KeggQuery query = new KeggQuery(KeggQuery.getOrganisms,null);
          CustomObject<Object> answer = manag.getInformation(query);
          Definition[] organisms = (Definition[]) answer.getObject();
          if (organisms!=null && organisms.length<1) {
            // Delete invalid objects from cache
            manag.removeInformation(query);
            answer = manag.getInformation(query);
            organisms = (Definition[]) answer.getObject();
          }
          
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
      }
      
    } catch (Exception e) {
      GUITools.showErrorMessage(this, e);
      return null;
    }
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    organismSelector.setEnabled(enabled);
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
    if (organismSelector==null || organismSelector.getSelectedItem()==null) return null;
    String selItem = organismSelector.getSelectedItem().toString();
    if (currentOrganismMap==null) return null;
    for (Map.Entry<String,String> e: currentOrganismMap.entrySet()) {
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

  /**
   * @param actionListener
   */
  public void addActionListener(ActionListener actionListener) {
    organismSelector.addActionListener(actionListener);
  }


  
}
