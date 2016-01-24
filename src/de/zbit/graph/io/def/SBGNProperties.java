/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.io.def;

import java.util.logging.Logger;

import org.sbgn.bindings.Glyph;

import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.ext.EntryExtended;

/**
 * Properties for SBGN translations and visualizations.
 * 
 * @author Manuel Ruff
 * @author Clemens Wrzodek
 * @date 2011-12-29
 * @version $Rev$
 */
public class SBGNProperties {
  
  /**
   * 
   */
  public static final Logger log = Logger.getLogger(SBGNProperties.class.getName());
  
  /**
   * Determines the GlyphType for a corresponding KeggType:<br>
   * compound -> simple chemical<br>
   * enzyme -> macromolecule<br>
   * gene -> macromolecule<br>
   * group -> complex<br>
   * map -> submap<br>
   * ortholog -> unspecified entity<br>
   * other -> unspecified entity<br>
   * @param e
   * @return
   */
  public static GlyphType getGlyphType(Entry e) {
    if (e instanceof EntryExtended) {
      switch (((EntryExtended) e).getGeneType()) {
        case dna:
          return GlyphType.nucleic_acid_feature;
        case dna_region:
          return GlyphType.nucleic_acid_feature;
        case gene:
          return GlyphType.unit_of_information;
        case protein:
          return GlyphType.macromolecule;
        case rna:
          return GlyphType.nucleic_acid_feature;
        case rna_region:
          return GlyphType.nucleic_acid_feature;
        default:
          return null;
      }
    }
    
    switch (e.getType()) {
      case compound:
        return GlyphType.simple_chemical;
      case enzyme:
        return GlyphType.macromolecule;
      case gene:
        return GlyphType.macromolecule;
      case genes:
        return GlyphType.macromolecule_multimer;
      case group:
        return GlyphType.macromolecule_multimer;
      case map:
        return GlyphType.submap;
      case ortholog:
        return GlyphType.macromolecule;
      case reaction:
        return GlyphType.interaction;
        
      default:
        return GlyphType.unspecified_entity;
    }
  }
  
  /**
   * 
   * @author Clemens Wrzodek
   * @since 1.1
   * @version $Rev$
   */
  public static enum GlyphType
  {
    unspecified_entity,
    /**
     * simple chemical
     */
    simple_chemical(247),
    /**
     * macromolecule
     */
    macromolecule(245),
    /**
     * informational molecule segment
     */
    nucleic_acid_feature(354),
    /**
     * multimer of simple chemicals
     */
    simple_chemical_multimer(421),
    macromolecule_multimer,
    nucleic_acid_feature_multimer,
    /**
     * non-covalent complex
     */
    complex(253),
    /**
     * multimer of complexes
     */
    complex_multimer(418),
    /**
     * empty set
     */
    source_and_sink(291),
    perturbation,
    biological_activity,
    /**
     * perturbing agent
     */
    perturbing_agent(405),
    /**
     * physical compartment
     */
    compartment(290),
    /**
     * encapsulating process
     */
    submap(395),
    tag,
    terminal,
    /**
     * process
     */
    process(375),
    /**
     * omitted process
     */
    omitted_process(397),
    /**
     * uncertain process
     */
    uncertain_process(396),
    /**
     * non-covalent binding
     */
    association(177),
    /**
     * dissociation
     */
    dissociation(180),
    /**
     * phenotype
     */
    phenotype(358),
    and,
    or,
    not,
    state_variable,
    unit_of_information,
    stoichiometry,
    entity,
    outcome,
    observable,
    interaction,
    influence_target,
    annotation,
    variable_value,
    implicit_xor,
    delay,
    existence,
    location,
    cardinality;
    
    /**
     * 
     */
    private int sboTerm;
    
    /**
     * 
     * @param sboTerm
     */
    private GlyphType(int sboTerm) {
      this.sboTerm = sboTerm;
    }
    
    /**
     * 
     */
    private GlyphType() {
      this(-1);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return name().replace('_', ' ');
    }
    
    /**
     * @param arg0
     * @return reverse of {@link #toString()}
     */
    public static GlyphType valueOfString(String arg0) {
      return valueOf(arg0.replace(' ', '_'));
    }
    
    /**
     * 
     * @return
     */
    public int getSBOterm() {
      if (sboTerm > 0) {
        return sboTerm;
      }
      return 285; //=material entity of unspecified nature
    }
    
  }
  
  /**
   * 
   * @author Clemens Wrzodek
   * @since 1.1
   * @version $Rev$
   */
  public static enum GlyphOrientation
  {
    horizontal,
    vertical,
    left,
    right,
    up,
    down,
  }
  
  /**
   * 
   * @author Clemens Wrzodek
   * @since 1.1
   * @version $Rev$
   */
  public static enum ArcType
  {
    production,
    consumption,
    catalysis,
    modulation,
    stimulation,
    inhibition,
    assignment,
    interaction,
    absolute_inhibition,
    absolute_stimulation,
    positive_influence,
    negative_influence,
    unknown_influence,
    equivalence_arc,
    necessary_stimulation,
    logic_arc;
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return name().replaceAll("_", " ");
    }
    
    /**
     * @param arg0
     * @return reverse of {@link #toString()}
     */
    public static ArcType valueOfString(String arg0) {
      return valueOf(arg0.replace(' ', '_'));
    }
    
  }
  
  /**
   * A collection of enum states for {@link Glyph}s, according
   * to Table 2.4 of the SBGN Specification.
   * 
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public static enum GlyphState {
    Acetylation("Ac", 215),
    Glycosylation("G", 217),
    Hydroxylation("OH", 233),
    Methylation("Me", 214),
    Myristoylation("My", 219),
    Palmytoylation("Pa", 218),
    Phosphorylation("P", 216),
    Prenylation("Pr", 221),
    Protonation("H", 212),
    Sulfation("S", 220),
    Ubiquitination("Ub", 224);
    
    /**
     * 
     */
    private int sboTerm;
    /**
     * 
     */
    private String label;
    
    /**
     * 
     * @param label
     * @param sboTerm
     */
    private GlyphState(String label, int sboTerm) {
      this.sboTerm = sboTerm;
      this.label = label;
    }
    
    /**
     * 
     * @return
     */
    public int getSBOterm() {
      if (sboTerm > 0) {
        return sboTerm;
      }
      // This should actually never get returned!
      log.warning(String.format("Please set an sbo term for GlyphState '%s'.",toString()));
      return 0;
    }
    
    /**
     * 
     * @return
     */
    public String getLabel() {
      return label;
    }
    
    /**
     * 
     * @return
     */
    public String getName() {
      return toString();
    }
    
  }
  
}
