package de.zbit.gui;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JToolBar;

import org.sbml.jsbml.SBMLDocument;

import de.zbit.sbml.io.OpenedFile;

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
    System.out.println(Arrays.toString(files));
    return null;
  }

  public File saveFileAs() {
    return null;
  }

  public static void main(String args[]) {
    DragNDropTest test = new DragNDropTest();
    test.setVisible(true);
  }

/* (non-Javadoc)
 * @see de.zbit.gui.BaseFrame#openFile(de.zbit.sbml.io.OpenedFile<org.sbml.jsbml.SBMLDocument>[])
 */
@SuppressWarnings("unchecked")
@Override
protected OpenedFile<SBMLDocument>[] openFile(OpenedFile<SBMLDocument>... files) {
    System.out.println(Arrays.toString(files));
	return null;
}

/* (non-Javadoc)
 * @see de.zbit.gui.BaseFrame#saveFile()
 */
@Override
public File saveFile() {
	return null;
}

}
