package de.zbit.kegg.parser.pathway;

/**
 * Corresponding to the possible Kegg Entry-Types (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author wrzodek
 */
public enum EntryType {
	ortholog, enzyme, gene, group, compound, map, other
	//protein                Complex
}
