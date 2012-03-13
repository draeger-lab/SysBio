package de.zbit.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import de.zbit.resources.Resource;
import de.zbit.util.Species;

public class SpeciesTest {

  public static void main(String[] args) throws IOException {
    System.out.println(Species.getSpeciesWithKEGGIDInList("hsa", new BufferedReader(
            new InputStreamReader(Resource.class.getResourceAsStream("speclist.txt")))).getScientificName());
            
    List<Species> list = Species.generateSpeciesDataStructure(new BufferedReader(
        new InputStreamReader(Resource.class.getResourceAsStream("speclist.txt"))));
    
    System.out.println(list.get(list.indexOf(new Species("HoMo sapiEns"))).getKeggAbbr());
    System.out.println(list.get(list.indexOf(new Species("Mus musculus"))).getKeggAbbr());
    System.out.println(list.get(list.indexOf(new Species("Zygosaccharomyces rouxii"))).getKeggAbbr()); 
  }
  
}
