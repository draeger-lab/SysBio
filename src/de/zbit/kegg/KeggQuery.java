package de.zbit.kegg;

import java.io.Serializable;

/**
 * This class is used by KeggFunctionManagement.
 * It determines which function should be executed and what are the parameters for this function.
 * @author wrzodek
 */
public class KeggQuery implements Comparable<KeggQuery>, Serializable {
  private static final long serialVersionUID = -2970366298298913439L;

  /**
   * Input: Organism id (e.g. "hsa")
   */
  public final static int getPathways=0; // returns: String[]
  /**
   * Input: Pathway id (e.g. "path:hsa04010")
   */
  public final static int getGenesByPathway=1; // returns: String[]
  /**
   * Input: KG-Gene-ids, separated by space (e.g. "hsa:123 hsa:142")
   */
  public final static int getIdentifier=2; // returns: String (each entry separated by new line)
  
  
  private int jobToDo; // Required
  private String query; // Required
  
  public KeggQuery(int jobToDo, String query) {
    this.jobToDo = jobToDo;
    this.query = query;
  }
  
  // Getters and Setters
  public int getJobToDo() {
    return jobToDo;
  }
  public void setJobToDo(int jobToDo) {
    this.jobToDo = jobToDo;
  }
  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }
  //------------------------------


  @Override
  public int compareTo(KeggQuery o) {
    if (jobToDo<o.getJobToDo()) return -1;
    else if (jobToDo>o.getJobToDo()) return 1;
    else { // Same job to do
      return query.compareTo(o.getQuery());
    }
  }
  

  @Override
  public String toString(){
    return "Job:" + jobToDo + " Query:" +query;
  }
  
  @Override
  public int hashCode(){
    int hc = (int) (jobToDo + query.hashCode());
    return (hc);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof KeggQuery) {
      KeggQuery e = (KeggQuery)o;
      if (e.jobToDo==this.jobToDo && this.query.equals(e.query)) return true;
      return false;
    } else
      return super.equals(o);
  }
  
  @Override
  public KeggQuery clone() {
    KeggQuery clone = new KeggQuery(this.jobToDo, new String(query));
    return clone;
  }
  
}
