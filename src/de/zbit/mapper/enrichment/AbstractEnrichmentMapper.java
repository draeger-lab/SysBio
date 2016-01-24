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
package de.zbit.mapper.enrichment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.mapper.AbstractMapper;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.mapper.Mapper;
import de.zbit.util.prefs.Option;
import de.zbit.util.progressbar.AbstractProgressBar;


/**
 * This is an abstract implementation for the {@link EnrichmentMapper} interface.
 * 
 * <p>Note: Since the {@link EnrichmentMapper} extends the {@link Mapper} interface, this
 * abstract implementation also extends the {@link AbstractMapper} implementation. 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class AbstractEnrichmentMapper<SourceType, TargetType> extends AbstractMapper<SourceType, Collection<TargetType>>  implements EnrichmentMapper<SourceType, TargetType> {
  private static final long serialVersionUID = -1427102769288229924L;
  public static final Logger log = Logger.getLogger(AbstractEnrichmentMapper.class.getName());

  /**
   * This represents the total number of 1:1 mappings (Key2ElementInCollection),
   * whereas {@link AbstractMapper#size()} is the number of 1:many (Key2Collection) size.
   */
  public int sumOfCollectionSizes;
  
  /**
   * This list counts the number of genes in an enrichment class.
   * Thus, the key is the enrichment-class-id(.toString()) and the Integer is the
   * total number of genes in the enrichment class.
   */
  public Map<String, Integer> entitiesInPathway = new HashMap<String, Integer>();
  
  /**
   * @param sourceType
   * @param targetType
   * @param progress
   * @throws IOException
   */
  public AbstractEnrichmentMapper(Class<SourceType> sourceType,
    Class<Collection<TargetType>> targetType, AbstractProgressBar progress)
    throws IOException {
    super(sourceType, targetType, progress);
  }
  
  /**
   * We do no postProcessing here, but fill our private variables.
   * 
   * <p>We count the number of entities in {@link #sumOfCollectionSizes}
   * and the entities in an Enrichment class {@link #genesInPathway}. An
   * entity might be a geneID or a compoundID
   * 
   * <p>Methods, overriding this method should at any cost make
   * a reference to this super method!
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Collection postProcessTargetID(Collection target) {
    // Is actually always a collection of exactly one element.
    // But better make it more generic...
    Iterator it = target.iterator();
    while (it.hasNext()) {
      // Remark: No matter what the targetType originally was,
      // here it is always a string!
      String key = it.next().toString();
      Integer count = entitiesInPathway.get(key);
      if (count==null) count = new Integer(0);
      
      
      entitiesInPathway.put(key, (++count));
      sumOfCollectionSizes++;
    }
    
    return super.postProcessTargetID(target);
  }
  

  
  /* (non-Javadoc)
   * @see de.zbit.mapper.EnrichmentMapper#getGenomeSize()
   */
  public int getTotalSumOfEntitiesInAllClasses() {
    return this.sumOfCollectionSizes;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.EnrichmentMapper#getEnrichmentClassSize()
   */
  public int getEnrichmentClassSize(TargetType className) {
    Integer i = entitiesInPathway.get(className.toString());
    return i==null?0:i;
  }
  
  
  /**
   * Uses the {@link KeggPathwayID2PathwayName} mapper to map all IDs in
   * this mapping to the pathway name (e.g., "Tight junction" instead
   * of "path:mmu04530").
   * @throws IOException 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void convertIDsToNames(AbstractMapper<TargetType, String> ID2Name) throws IOException {
    Set<Entry<SourceType, Collection<TargetType>>>  entries = getMapping().entrySet();
    for (Entry<SourceType, Collection<TargetType>> entry : entries) {
      Collection c = entry.getValue();
      if (c!=null && c.size()>0) {
        Collection cNew = new ArrayList();
        for (Object object : c) {
          try {
            cNew.add(ID2Name.map((TargetType) object));
          } catch (Exception e) {
            log.log(Level.WARNING, "Could not map " + object==null?"NULL":object.toString() + " to a Enrichment Name.");
          }
        }
        
        // Change old 2id mapping to 2name mapping
        getMapping().put(entry.getKey(), cNew);
      }
    }
    
    // Reflect this change also in private map
    String[] oldKeys = entitiesInPathway.keySet().toArray(new String[0]);
    for (String key : oldKeys) {
      try {
        entitiesInPathway.put(ID2Name.map(Option.parseOrCast(ID2Name.getSourceType(), key)), entitiesInPathway.remove(key));
      } catch (Exception e) {
        log.log(Level.WARNING, "Could not reconstrocut enrichment classes and counters", e);
      }
    }  
    
  }
  
  
}
  
