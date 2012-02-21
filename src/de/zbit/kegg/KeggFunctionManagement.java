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
package de.zbit.kegg;

import java.util.concurrent.TimeoutException;

import de.zbit.exception.UnsuccessfulRetrieveException;
import de.zbit.util.CustomObject;
import de.zbit.util.InfoManagement;

/**
 * Retrieves and caches specific informations from the Kegg database.
 * Uses Kegg functions (like retrieve all PWs for an organism) instead of just retrieving information
 * for a Kegg ID (what KeggInfoManagement does).
 * 
 * Think of this class as a cache.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KeggFunctionManagement extends InfoManagement<KeggQuery, CustomObject<Object>>{
  private static final long serialVersionUID = -4559358395869823899L;
  private transient KeggAdaptor adap=null;
  
  /**
   * If this flag is set to true, this class does NOT retrieve any Information, but uses stored information.
   */
  public static boolean offlineMode = false;
  
  /**
   * 
   */
  public KeggFunctionManagement () {
    super();
    this.adap = new KeggAdaptor();
  }
  
  public KeggFunctionManagement (int maxListSize) {
    this(maxListSize, new KeggAdaptor());
  }
  
  /**
   * 
   * @param maxListSize
   * @param adap
   */
  public KeggFunctionManagement (int maxListSize, KeggAdaptor adap) {
    super(maxListSize); // Remember maxListSize queries at max.
    this.adap = adap;
  }
  
  /**
   * 
   * @return
   */
  public KeggAdaptor getKeggAdaptor() {
    if (adap==null) adap = new KeggAdaptor();
    return adap;
  }
  
  /**
   * 
   * @param adap
   */
  public void setKeggAdaptor(KeggAdaptor adap) {
    this.adap = adap;
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#cleanupUnserializableObject()
   */
  @Override  
  protected void cleanupUnserializableObject() {
    adap = null;
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#restoreUnserializableObject()
   */
  @Override
  protected void restoreUnserializableObject () {
    adap = getKeggAdaptor();
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#fetchInformation(java.lang.Comparable)
   */
  @Override
  protected CustomObject<Object> fetchInformation(KeggQuery id) throws TimeoutException, UnsuccessfulRetrieveException {
    if (offlineMode) throw new TimeoutException();
    
    int j = id.getJobToDo();
    Object answer=null;
    if (adap==null) adap = getKeggAdaptor(); // create new one
    
    
    //try {
      if (j==KeggQuery.getGenesByPathway) {
        answer = adap.getGenesByPathwayWithTimeout(id.getQuery());
      } else if (j==KeggQuery.getIdentifier) {
        answer = adap.getIdentifierWithTimeout(id.getQuery());
      } else if (j==KeggQuery.getPathways) {
        answer = adap.getPathwaysWithTimeout(id.getQuery());
      } else if (j==KeggQuery.getKEGGIdentifierForAGeneSymbol) {
    	  answer = adap.getKEGGIdentifierForAGeneSymbol(id.getQuery(), null);
      } else if (j==KeggQuery.getOrganisms) {
        answer = adap.getOrganisms();
      } else if (j==KeggQuery.genericFind) {
      	answer = adap.find(id.getQuery());
      } else {
        System.err.println("Unknown job '" + j + "'.");
        throw new UnsuccessfulRetrieveException(); // don't retry.
      }
      /*
      } catch (AdaptorException e) {
        throw new TimeoutException(); // retry...
      } catch (ParseException e) {
        e.printStackTrace();
        throw new UnsuccessfulRetrieveException(); // don't retry.
      }
      */    
    if (answer==null || (answer instanceof String &&  ((String)answer).trim().length()==0)) throw new UnsuccessfulRetrieveException(); // Will cause the InfoManagement class to remember this one.
    
    return new CustomObject<Object>(answer); // Successfull and "with data" ;-)
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.util.InfoManagement#fetchMultipleInformations(IDtype[])
   */
  @SuppressWarnings("unchecked")
  @Override
  protected CustomObject[] fetchMultipleInformations(KeggQuery[] ids) throws TimeoutException, UnsuccessfulRetrieveException {
    System.err.println("Fetching multiple infos not supported. Please use KeggInfoManagement, if possible.");
    return null;
  }
  
  
}
