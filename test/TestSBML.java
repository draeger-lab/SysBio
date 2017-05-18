import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;


public class TestSBML {
  
  public static void main(String[] args) throws SBMLException, XMLStreamException, IOException {
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model model = doc.createModel("my_model");
    model.createSpecies("s1", model.createCompartment("c1"));
    
    LayoutModelPlugin lmp = (LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
    Layout layout = lmp.createLayout();
    SpeciesGlyph sg = layout.createSpeciesGlyph("s1_glyph");
    sg.setSpecies("s1");
    BoundingBox bb = sg.createBoundingBox();
    bb.createDimensions(20d, 20d, 1d);
    bb.createPosition(0d, 0d, 0d);
    
    TextGlyph tg = layout.createTextGlyph("tg_1", "S1");
    tg.setGraphicalObject(sg);
    
    
    for (Reaction r : model.getListOfReactions()) {
      
    }
    
    
    //doc = SBMLReader.read(new File("~/Desktop/test.xml"));
    
    SBMLWriter.write(doc, System.out, ' ', (short) 2);
  }
}
