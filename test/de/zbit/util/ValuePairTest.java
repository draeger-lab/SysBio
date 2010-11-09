/**
 * 
 */
package de.zbit.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.zbit.util.ValuePair;

/**
 * @author Finja B&uml;chel
 * 
 */
public class ValuePairTest {
  ValuePair<String, String> r1, r2, r3;
  
  @Before
  public void setUP(){
    r1 = new ValuePair<String, String>("path:hsa0010", "YWHAB");
    r2 = new ValuePair<String, String>("path:hsa0020", "YWHAB");
    r3 = new ValuePair<String, String>("path:hsa0010", "YWHAB");
  }
  
  
  @Test
  public void pwRankingsEqual(){
    assertEquals(r1.equals(r2), false);
    assertEquals(r1.equals(r3), true);
  }
  
  
}
