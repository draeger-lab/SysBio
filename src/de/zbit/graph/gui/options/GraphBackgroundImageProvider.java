/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.gui.options;

import java.net.MalformedURLException;
import java.net.URL;

import y.view.Graph2DView;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.kegg.io.KEGGtranslator;
import de.zbit.util.ThreadManager;

/**
 * A provider that is setting up a background image on a {@link Graph2DView}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface GraphBackgroundImageProvider {
  
  /**
   * Setup the background image as set in the preferences
   * 
   * @param pane the pane to add the background image
   * @param translator the last used translator, which also contains the
   * last translated pathway. You can ignore this if you don't need it. 
   * @throws MalformedURLException
   */
  public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator) throws MalformedURLException;
  
  /**
   * See {@link #addBackgroundImage(Graph2DView, KEGGtranslator)}
   * @param pane
   * @param translator the last used translator, which also contains the
   * last translated pathway. You can ignore this if you don't need it.
   * @param waitUntilComplete if this is <code>TRUE</code>, this method should
   * not return until the background image is completely set up!
   * @throws MalformedURLException
   */
  public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator, boolean waitUntilComplete)
    throws MalformedURLException;
  
  
  public static class Factory {
    
    /**
     * A {@link GraphBackgroundImageProvider} that creates a solid static
     * background image that is not affected by any changes of the graph
     * (e.g., zooming, etc).
     * @param imagePath
     * @return
     */
    public static GraphBackgroundImageProvider createStaticImageProvider(final URL imagePath) {
      return new GraphBackgroundImageProvider() {
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator) throws MalformedURLException {
          addBackgroundImage(pane, translator, false);
        }
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator, boolean waitUntilComplete)
          throws MalformedURLException {
          RestrictedEditMode.addBackgroundImage(imagePath, pane);
        }
      };
    }
    
    /**
     * A {@link GraphBackgroundImageProvider} that creates a dynamic
     * background image that is changing with the graph view on zooming,
     * moving positions, etc.
     * @param imagePath
     * @param brightenImagePercentage optional parameter that is used to
     * brighten the image (percentage 0 to 100). Set to 0 to disable.
     * @return
     */
    public static GraphBackgroundImageProvider createDynamicImageProvider(final URL imagePath, final int brightenImagePercentage) {
      return new GraphBackgroundImageProvider() {
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator) throws MalformedURLException {
          addBackgroundImage(pane, translator, false);
        }
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator, boolean waitUntilComplete)
          throws MalformedURLException {
          if (imagePath!=null && imagePath.getPath()!=null && imagePath.getPath().length()>0) {
            Thread t = RestrictedEditMode.addDynamicBackgroundImage(imagePath, pane, brightenImagePercentage);
            if (waitUntilComplete) {
              ThreadManager.awaitTermination(t);
            }
          }
        }
      };
    }
    
    /**
     * A {@link GraphBackgroundImageProvider} that creates a dynamic
     * background image for the last translated KGML-formatted pathway.
     * @param translator the translator used for translation
     * @param brightenImagePercentage
     * @return
     */
    public static GraphBackgroundImageProvider createDynamicTranslatorImageProvider(final int brightenImagePercentage) {
      return new GraphBackgroundImageProvider() {
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator, boolean waitUntilComplete) throws MalformedURLException {
          if (translator==null) return;
          String image = translator.getLastTranslatedPathway().getImage();
          if (image!=null && image.length()>0) {
            createDynamicImageProvider(new URL(image), brightenImagePercentage).addBackgroundImage(pane, translator, waitUntilComplete);
          }
        }
        
        @Override
        public void addBackgroundImage(Graph2DView pane, KEGGtranslator<?> translator)
          throws MalformedURLException {
          addBackgroundImage(pane, translator, false);
        }
      };
    }

    
  }
  
}
