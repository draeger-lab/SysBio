package de.zbit.kegg.gui;

public interface PWSel {

  /**
   * @return the selected Pathway.
   */
  public String getSelectedPathway();

  /**
   * @return the kegg id for the selected organism-specific Pathway (e.g. hsa05410).
   */
  public String getSelectedPathwayID();

}