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
	    simple_chemical,
	    macromolecule,
	    nucleic_acid_feature,
	    simple_chemical_multimer,
	    macromolecule_multimer,
	    nucleic_acid_feature_multimer,
	    complex,
	    complex_multimer,
	    source_and_sink,
	    perturbation,
	    biological_activity,
	    perturbing_agent,
	    compartment,
	    submap,
	    tag,
	    terminal,
	    process,
	    omitted_process,
	    uncertain_process,
	    association,
	    dissociation,
	    phenotype,
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
	    
	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString(){
	    	return this.name().replace('_', ' ');
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
	      switch(this) {
          case association:
            return 177;//=non-covalent binding
          case compartment:
            return 290;//=physical compartment
          case complex:
            return 253;//=non-covalent complex
          case complex_multimer:
            return 418;//=multimer of complexes
          case dissociation:
            return 180;//=dissociation.
          case macromolecule:
            return 245; //=macromolecule
          case nucleic_acid_feature:
            return 354; // = informational molecule segment
          case omitted_process:
            return 397;//=omitted process.
          case perturbing_agent:
            return 405;//=perturbing agent
          case phenotype:
            return 358;//=phenotype
          case process:
            return 375;//=process
          case simple_chemical:
            return 247; //=simple chemical
          case simple_chemical_multimer:
            return 421;//=multimer of simple chemicals
          case source_and_sink:
            return 291;//=empty set
          case submap:
            return 395;//=encapsulating process

          case uncertain_process:
            return 396;//=uncertain process.
            
          default:
            return 285; //=material entity of unspecified nature
	        
	      }
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
		 public String toString(){
			 return this.name().replaceAll("_", " ");
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
	  Acetylation,// Ac SBO:0000215
	  Glycosylation, // G SBO:0000217
	  Hydroxylation, // OH SBO:0000233
	  Methylation, // Me SBO:0000214
	  Myristoylation, // My SBO:0000219
	  Palmytoylation, // Pa SBO:0000218
	  Phosphorylation, // P SBO:0000216
	  Prenylation, // Pr SBO:0000221
	  Protonation, // H SBO:0000212
	  Sulfation, // S SBO:0000220
	  Ubiquitination;// Ub SBO:0000224
	  
	  public int getSBOterm() {
	    switch(this) {
        case Acetylation:
          return 215;
        case Glycosylation:
          return 217;
        case Hydroxylation:
          return 233;
        case Methylation:
          return 214;
        case Myristoylation:
          return 219;
        case Palmytoylation:
          return 218;
        case Phosphorylation:
          return 216;
        case Prenylation:
          return 221;
        case Protonation:
          return 212;
        case Sulfation:
          return 220;
        case Ubiquitination:
          return 224;
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
	    switch(this) {
	      // Special cases
        case Hydroxylation:
          return "OH";
        case Protonation:
          return "H";
          
          // First-letter returns
        case Phosphorylation:
        case Sulfation:
        case Glycosylation:
          return Character.toString(toString().charAt(0));
          
          // All First-two-letter returns (default)
        default:
          return toString().substring(0, 2);
	      
	    }
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
