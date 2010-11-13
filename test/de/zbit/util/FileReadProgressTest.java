package de.zbit.util;




/**
 * NOTE: To run this from command line, go to the "bin" directory and execute:
 * 
 * <pre>java -cp ../lib/junit-4.3.1.jar:. org.junit.runner.JUnitCore de.zbit.util.FileReadProgressTest</pre>
 * 
 * @author Florian Mittag
 */
public class FileReadProgressTest {

  public static FileReadProgress frp;
  
  @Before
  public void setUp() throws Exception {
    frp = new FileReadProgress(150);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetSetOutputPercentage() {
    frp.setOutputPercentage(true);
    assertTrue(frp.getOutputPercentage());
    frp.setOutputPercentage(false);
    assertFalse(frp.getOutputPercentage());
  }

  @Test
  public void testProgressSameLine() throws InterruptedException {
    frp.setOutputPercentage(true);
    frp.setPrintProgessInSameLine(true);
    for( int i = 0; i < 150; i++ ) {
      frp.progress(1);
      Thread.sleep(30);
    }
    System.out.println();
  }

  @Test
  public void testProgressNewLine() throws InterruptedException {
    frp.setOutputPercentage(true);
    frp.setPrintProgessInSameLine(false);
    for( int i = 0; i < 150; i++ ) {
      frp.progress(1);
      Thread.sleep(30);
    }
    System.out.println();
  }
}
