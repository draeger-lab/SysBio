package de.zbit.util.prefs;

import java.awt.Font;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class OptionTest {
  
  /**
   * 
   */
  private Option<Font> fontOption = new Option<Font>("fontOption", Font.class, "Font test");
  /**
   * 
   */
  private Font testFont;
  
  /**
   * 
   */
  @Before
  public void setUp() {
    testFont = new Font("Arial", Font.BOLD, 4);
    fontOption.setDefaultValue(testFont);
  }
  
  /**
   * 
   */
  @Test
  public void testParseOrCastClassOfTypeObject() {
    testFont.toString().equals(fontOption.parseOrCast(fontOption.getDefaultValue()));
  }
  
}
