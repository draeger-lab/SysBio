package de.zbit.gui;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JToolBar;

public class DragNDropTest extends BaseFrame {
 
  private static final long serialVersionUID = -2668467238882169768L;

  public DragNDropTest() {
    super();
  }
  
  public boolean closeFile() {
    return false;
  }
  
  protected JToolBar createJToolBar() {
    return null;
  }
  
  protected Component createMainComponent() {
    return null;
  }
  
  public URL getURLAboutMessage() {
    return null;
  }
  
  public URL getURLLicense() {
    return null;
  }
  
  public URL getURLOnlineHelp() {
    return null;
  }
  
  protected File[] openFile(File... files) {
    // TODO: Test method!!
    System.out.println(files);
    return null;
  }
  
  public File saveFile() {
    return null;
  }
  
  public static void main(String args[]) {
    DragNDropTest test = new DragNDropTest();
    test.setVisible(true);
  }
  
}