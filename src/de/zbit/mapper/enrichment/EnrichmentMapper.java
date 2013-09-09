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
package de.zbit.mapper.enrichment;

import java.util.Collection;

import de.zbit.mapper.AbstractMapper;
import de.zbit.mapper.Mapper;

/**
 * A special {@link Mapper} that can be used for enrichment tests, e.g.,
 * based on a gene list. These are always 1:n mappings, for example
 * 1 gene is contained in n pathways, thus, the collection consists of
 * pathway ids.
 * 
 * @see KeggID2PathwayMapper KeggID2PathwayMapper for an example implementation.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @param <SourceType> gene identifier
 * @param <Collection> list of enrichment categories (e.g., pathways)
 */
public interface EnrichmentMapper<SourceType, TargetType> extends Mapper<SourceType, Collection<TargetType>> {
  
  /**
   * Total number of entities (genes/compounds), occuring in all
   * enrichment classes including multiples (entities occuring in
   * multiple e.g., pathways).
   * <p>
   * Use {@link AbstractMapper#size()} to get number of
   * unique entities, occuring in all pathways.
   * <p>
   * This is mostly equal to the sum of all {@link java.util.Collection#size()}s.
   * @return
   */
  public int getSumOfEntitiesInClasses();
  
  /**
   * Return the size of an enrichment class.
   * 
   * <p>To implement this function, you will have to have a second map
   * from TargetType to {@link Integer}, counting the number of
   * Occurrences of className in the {@link Collection} across all
   * SourceTypes.
   * @param className one element of any Collection
   * @return number of genes in the given enrichment class.
   */
  public int getEnrichmentClassSize(TargetType className);
  
  
}
