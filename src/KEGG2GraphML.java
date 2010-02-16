import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import kegg.KeggAdaptor;
import kegg.KeggParser;
import kegg.pathway.Entry;
import kegg.pathway.EntryType;
import kegg.pathway.Graphics;
import kegg.pathway.Pathway;
import kegg.pathway.Relation;
import kegg.pathway.SubType;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YInsets;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.view.Arrow;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

public class KEGG2GraphML {
  public static boolean silent = true; // Surpresses all outputs, except %-values
  public static boolean absoluteNoOutputs = true;
  
  public static boolean retrieveKeggAnnots=true; // z.B. "hsa" = homo sapiens, "" => General, null => keine infos von kegg.
  public static boolean showEntriesWithoutGraphAttribute=true;
  public static boolean skipCompounds=false;
  public static boolean groupNodesWithSameEdges=false;
  public static boolean removeDegreeZeroNodes=true; // Bezieht sich nur auf "Non-map nodes" (Also nicht auf Pathway referenzen).
  public static boolean renameCompoundToSmallMolecule=true; // F¸r Jochen & GePS unbedingt true lassen (so was wie LPS, CA2+ als "sm" bezeichnen).
  
  public static boolean lastFileWasOverwritten=false; // Gibt an, ob das letzte geschriebene outFile bereits vorhanden war und deshalb ¸berschrieben wurde.
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args!=null && args.length >0) {
      File f = new File(args[0]);
      if (f.isDirectory()) BatchConvertKegg.main(args);
      else {
        String outfile = args[0].substring(0, args[0].contains(".")?args[0].lastIndexOf("."):args[0].length())+".graphML";
        if (args.length>1) outfile = args[1];
        Pathway p = KeggParser.parse(args[0]).get(0);
        KEGG2GraphML(p, outfile);
      }
      return;
    }
    
    //KeggParser.silent=false;
    System.out.println("Reading kegg pathway...");
    Pathway p = KeggParser.parse("_ko00010.xml").get(0); //04115
    //Pathway p = KeggParser.parse("ko02010.xml").get(0);
    //p = KeggParser.parse("http://kaas.genome.jp/kegg/KGML/KGML_v0.6.1/ko/ko00010.xml").get(0);
    
    System.out.println("Converting to GraphML");
    //silent = false;
    KEGG2GraphML(p, "test.graphML");
  }
  
  public static void KEGG2GraphML(Pathway p, String outFile) {
    Graph2D graph = new Graph2D();
    ArrayList<String> PWReferenceNodeTexts = new ArrayList<String>(); 
    KeggAdaptor adap = null;
    if (retrieveKeggAnnots) adap = new KeggAdaptor();
    lastFileWasOverwritten=false;
    
    
    //Erstellen von Graph Annotationen
    NodeMap nodeDescription = graph.createNodeMap();
    NodeMap entityType = graph.createNodeMap();
    NodeMap nodeLabel = graph.createNodeMap();
    NodeMap entrezIds = graph.createNodeMap();
    NodeMap keggOntIds = graph.createNodeMap();
    NodeMap uniprotIds = graph.createNodeMap();
    NodeMap ensemblIds = graph.createNodeMap();
    NodeMap nodeURLs = graph.createNodeMap();
    //NodeMap bindsToChemicals = graph.createNodeMap();
    
    EdgeMap edgeDescription = graph.createEdgeMap(); // = Relation.type
    EdgeMap interactionDescription = graph.createEdgeMap(); // = Subtype.name "inhibition", "activation", "interaction", "physical interaction", "catalysis", "transcriptional control", "control"
    //EdgeMap interactionType = graph.createEdgeMap(); // "control", "catalysis", "physical interaction", "transport", "complex assembly", "biochemical reaction"
    //EdgeMap edgeURLs = graph.createEdgeMap();
    
    
    // Tut zwar eh nicht, aber egal...
    if (p.getLink()!=null && !p.getLink().isEmpty()) {
      try {
        graph.setURL(new URL(p.getLink()));
      } catch (MalformedURLException e1) {
        e1.printStackTrace();
      }
    }
    ArrayList<Node> parentGroupNodes = new ArrayList<Node>();
    ArrayList<ArrayList<Integer>> groupNodeChildren = new ArrayList<ArrayList<Integer>>();
    HierarchyManager hm = graph.getHierarchyManager();
    if (hm==null) hm = new HierarchyManager(graph);
    
    aufrufeGesamt=p.getEntries().size(); //+p.getRelations().size(); // Relations gehen sehr schnell.
    if (adap==null) aufrufeGesamt+=p.getRelations().size();
    
    // Add nodes for all Entries
    if (!silent) System.out.println("Creating nodes...");
    for (int i=0; i<p.getEntries().size(); i++) {
      if (silent && !absoluteNoOutputs) DisplayBar(null);
      if (!silent) System.out.println(i + "/" + p.getEntries().size());
      Entry e = p.getEntries().get(i);
      if (skipCompounds && e.getType().toString().equalsIgnoreCase("compound")) continue;
      
      Node n;
      boolean isPathwayReference=false; // TODO: Anders Layouten ??? weglassen ???
      String name = e.getName().trim();
      if (name.toLowerCase().startsWith("path:") || e.getType().equals(EntryType.map)) isPathwayReference=true;
      
      
      if (e.hasGraphics()) {
        /* XXX:
         *     <entry id="16" name="ko:K04467 ko:K07209 ko:K07210" type="ortholog">
                 <graphics name="IKBKA..." fgcolor="#000000" bgcolor="#FFFFFF"
                   type="rectangle" x="785" y="141" width="45" height="17"/>
                   
         *   ... is actually a compund!?!?
         */
        
        // Get name, description and other annotations via api (organism specific) possible!!
        Graphics g = e.getGraphics();
        if (!g.getName().isEmpty())
          name = g.getName(); // + " (" + name + ")"; // Append ko Id(s) possible!
        /*if (g.getWidth()>0 && g.getHeight()>0) {
          n = graph.createNode(g.getX(), g.getY(), g.getWidth(), g.getHeight(), name);
        } else {
          n = graph.createNode(g.getX(), g.getY(), name);
        }*/
        
        
          
        
        NodeRealizer nr;
        NodeLabel nl = new NodeLabel(name);
        
        // one of "rectangle, circle, roundrectangle, line, other"
        boolean addThisNodeToGorupNodeList=false;
        if (e.getType().toString().trim().equalsIgnoreCase("group")) {
          groupNodeChildren.add(e.getComponents());
          addThisNodeToGorupNodeList = true;
          

          // New Text
          String newText = null;
          if (nl.getText().isEmpty() || nl.getText().equals("undefined")) {
            /*String newText = "Group of: " + "\n";
            char sep = ',';
            for (Integer i2: e.getComponents()) {
              Entry e2 = p.getEntryForId(i2);
              if (e2==null) {
                newText+=i2+sep;
              } else if (e2.hasGraphics() && !e2.getGraphics().getName().isEmpty()) {
                newText+=e2.getGraphics().getName()+sep;
              } else {
                newText+=e2.getName()+sep;
              }
            }*/
            newText = "Group";
          }
          nr = setupGroupNode(nl, newText);
          
        } else if (g.getType().toString().equals("rectangle")) {
          nr = new ShapeNodeRealizer(ShapeNodeRealizer.RECT);
        } else if (g.getType().toString().equals("circle")) {
          nr = new ShapeNodeRealizer(ShapeNodeRealizer.ELLIPSE);
          nl.setFontSize(10); // ein bischen kleiner, sieht bei circles besser aus.
        } else if (g.getType().toString().equals("roundrectangle")) {
          nr = new ShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT);
        //} else if (g.getType().toString().equals("line")) { // Vielleicht in neuerer yFiles version line verf¸gbar?
          //nr = new ShapeNodeRealizer(ShapeNodeRealizer.TRAPEZOID);
        } else
          nr = new GenericNodeRealizer();
        
        try {
          if (g.getBgcolor()!=null && !g.getBgcolor().isEmpty() && !g.getBgcolor().trim().equalsIgnoreCase("none"))
            nr.setFillColor(ColorFromHTML(g.getBgcolor()));
        } catch (Throwable t) {t.printStackTrace();}
        
        try {
          if (g.getFgcolor()!=null && !g.getFgcolor().isEmpty() && !g.getFgcolor().trim().equalsIgnoreCase("none"))
            nl.setTextColor(ColorFromHTML(g.getFgcolor()));
        } catch (Throwable t) {t.printStackTrace();}
        
        nr.setLabel(nl);
        if (isPathwayReference && name.toUpperCase().startsWith("TITLE:")) {
          // Name dieses Pathways
          nl.setFontStyle(Font.BOLD);
          nl.setText(name); // name.substring("TITLE:".length()).trim()
          nr.setFillColor(Color.GREEN);
        } else if (isPathwayReference) {
          // Reference auf einen anderen Pathway
          nr.setFillColor(Color.LIGHT_GRAY);
        }
        
        
        // Links im Knotenlabel (und sp‰ter nochmal im Knoten) speichern.
        String link = e.getLink();
        if (link!=null && !link.isEmpty()) {
          try {
            nl.setUserData(new URL(link)); // In URL konvertieren, da er auch type speichert und URL besser ist als STRING
          } catch (MalformedURLException e1) {
            nl.setUserData(link);
          }
        }
        
        nr.setX(g.getX());
        nr.setY(g.getY());
        if (g.getWidth()>0 || g.getHeight()>0) {
          nr.setWidth(g.getWidth());
          nr.setHeight(g.getHeight());
        }
        
        
        n = graph.createNode(nr);
        if (addThisNodeToGorupNodeList) {
          hm.convertToGroupNode(n);
          parentGroupNodes.add(n);
        }
        
      } else {
        if (showEntriesWithoutGraphAttribute)
          n = graph.createNode(0,0, name);
        else
          n=null;
      }
      
      
      // Node was created. Postprocessing options.
      if (n!=null) {
        // Remember node in KEGG Structure for further references.
        e.setCustom(n);
        

        // KeggAdaptor. Achtung: Sehr langsam.
        if (adap!=null) {
          boolean hasMultipleIDs = false;
          if (e.getName().trim().contains(" ")) hasMultipleIDs = true;
          
          String name2="",definition="",entrezIds2="",uniprotIds2="",eType="",ensemblIds2="";
          for (String ko_id:e.getName().split(" ")) {
            //Definition[] results = adap.getGenesForKO(e.getName(), retrieveKeggAnnotsForOrganism); // => GET only (und alles aus GET rausparsen). Zus‰tzlich: in sortedArrayList merken.
            if (ko_id.trim().equalsIgnoreCase("undefined")) continue;
            
            String infos = adap.get(ko_id);
            
            // "NCBI-GeneID:","UniProt:", "Ensembl:", ... aus dem GET rausparsen
            if (infos!=null && infos.length()>0) {
              String oldText=graph.getRealizer(n).getLabelText();
              
              String exName = KeggAdaptor.extractInfo(infos, "NAME");
              if (exName!=null && !exName.isEmpty()) {
                int pos = exName.lastIndexOf(";");
                if (pos>0 && pos<(exName.length()-1)) exName = exName.substring(pos+1, exName.length()).replace("\n", "").trim();
                
                if (!hasMultipleIDs) // Knotennamen nur anpassen, falls nicht mehrere IDs.
                  graph.getRealizer(n).setLabelText(exName);
                else if (oldText.isEmpty()) // ... oder wenn er bisher leer ist.
                  graph.getRealizer(n).setLabelText(exName);
              }
              
              String text = KeggAdaptor.extractInfo(infos, "NAME");
              if (text!=null && !text.isEmpty()) name2+=(!name2.isEmpty()?",":"")+text.replace(",", "");
              
              if (e.getType().equals(EntryType.map)) { // => Link zu anderem Pathway oder Title-Node des aktuellem PW.
                text = KeggAdaptor.extractInfo(infos, "DESCRIPTION");
                if (text!=null && !text.isEmpty()) definition+=(!definition.isEmpty()?",":"")+text.replace(",", "").replace("\n", " ");
              } else {
                text = KeggAdaptor.extractInfo(infos, "DEFINITION");
                if (text!=null && !text.isEmpty()) definition+=(!definition.isEmpty()?",":"")+text.replace(",", "").replace("\n", " ");
              }
              
              text = KeggAdaptor.extractInfo(infos, "NCBI-GeneID:", "\n"); //adap.getEntrezIDs(ko_id);
              if (text!=null && !text.isEmpty()) entrezIds2+=(!entrezIds2.isEmpty()?",":"")+text; //.replace(",", "");
              text = KeggAdaptor.extractInfo(infos, "UniProt:", "\n"); //adap.getUniprotIDs(ko_id);
              if (text!=null && !text.isEmpty()) uniprotIds2+=(!uniprotIds2.isEmpty()?",":"")+text; //.replace(",", "");
              text = KeggAdaptor.extractInfo(infos, "Ensembl:", "\n"); //adap.getEnsemblIDs(ko_id);
              if (text!=null && !text.isEmpty()) ensemblIds2+=(!ensemblIds2.isEmpty()?",":"")+text; //.replace(",", "");
              
              ////eType+=(!eType.isEmpty()?",":"")+e.getType().toString();
              //eType+=(!eType.isEmpty()?",":"");
              //if (renameCompoundToSmallMolecule && e.getType().equals(EntryType.compound)) eType+="small molecule"; else eType+=e.getType().toString();
              if (eType.isEmpty()) {
                if (e.getType().equals(EntryType.compound))
                  eType = "small molecule";
                else if (e.getType().equals(EntryType.gene))
                  eType = "protein";
                else
                  e.getType().toString();
              }
              // XXX F¸r Jochens Annotationen (entityType):
              // Jochen:  "protein", "protein in complex", "complex", "RNA", "DNA", "small molecule", "RNA in complex", "DNA in complex", "small molecule in complex", "pathway", "biological process"
              // Mapping: gene/ortholog => protein. Group=>complex, compound=>complex, map=>pathway(/biolog. process)
              //          enzyme & other fehlen.
              // Auﬂerdem: "bindsToChemicals" nicht gesetzt.
            }
          }

          nodeLabel.set(n, name2);
          nodeDescription.set(n, definition);
          entrezIds.set(n, entrezIds2);
          uniprotIds.set(n, uniprotIds2);
          ensemblIds.set(n, ensemblIds2);
          entityType.set(n, eType);
        }
        keggOntIds.set(n, e.getName().replace(" ", ","));
        if (e.getLink()!=null && !e.getLink().isEmpty()) nodeURLs.set(n, e.getLink());

        if (isPathwayReference) PWReferenceNodeTexts.add(graph.getRealizer(n).getLabelText());
      }
        
    }
    
    
    // Make group node hirarchies
    for (int i=0; i<parentGroupNodes.size(); i++) {
      NodeList nl = new NodeList();
      double x=Double.MAX_VALUE,y=Double.MAX_VALUE,width=0,height=0;
      for (int n2: groupNodeChildren.get(i)) {
        Entry two = p.getEntryForId(n2);
        if (two==null) {
          System.out.println("WARNING: Missing node for id " + n2);
          continue;
        }
        Node twoNode = (Node) two.getCustom();
        NodeRealizer nr = graph.getRealizer(twoNode);
        x = Math.min(x, nr.getX());
        y = Math.min(y, nr.getY());
        width=Math.max(width, (nr.getWidth()+nr.getX()));
        height=Math.max(height, (nr.getHeight()+nr.getY()));
        
        nl.add(twoNode);
      }
      
      // Reposition group node to fit content
      int offset = 5;
      graph.setLocation(parentGroupNodes.get(i), x-offset, y-offset-14);
      graph.setSize(parentGroupNodes.get(i), width-x+2*offset, height-y+2*offset+11);
      
      // Set hirarchie
      hm.setParentNode(nl, parentGroupNodes.get(i));

      // Reposition group node to fit content (2nd time is neccessary. Maybe yFiles bug...)
      graph.setLocation(parentGroupNodes.get(i), x-offset, y-offset-14);
      graph.setSize(parentGroupNodes.get(i), width-x+2*offset, height-y+2*offset+11);
      
    }
    
    
    
    
    // Add Edges for all Relations
    if (!silent) System.out.println("Creating edges...");
    for (int i=0; i<p.getRelations().size(); i++) {
      if (silent && !absoluteNoOutputs && adap==null) DisplayBar(null);
      if (!silent) System.out.println(i + "/" + p.getRelations().size());
      Relation r = p.getRelations().get(i);
      Entry one = p.getEntryForId(r.getEntry1());
      Entry two = p.getEntryForId(r.getEntry2());
      
      if (one==null || two==null) {
        System.out.println("Relation with unknown entry!");
        continue;
      }
      
      Node nOne = (Node) one.getCustom();
      Node nTwo = (Node) two.getCustom();
      
      Edge myEdge;
      // XXX: Hier noch mˆglich die Type der reaktion (PPI, etc.) hinzuzuf¸gen.
      if (r.getSubtypes()!=null && r.getSubtypes().size()>0) {
        for (int stI=0; stI<r.getSubtypes().size(); stI++) {
          SubType st = r.getSubtypes().get(stI);
          EdgeRealizer er = new GenericEdgeRealizer();
          EdgeLabel el = new EdgeLabel(st.getName());
          el.setFontSize(8);
          er.addLabel(el);
          
          if (st.getName().trim().equalsIgnoreCase("compound") && isNumber(st.getValue())) {
            Entry compNode = p.getEntryForId(Integer.parseInt(st.getValue()));
            if (compNode==null || compNode.getCustom()==null) {System.err.println("Could not find Compound Node."); graph.createEdge(nOne, nTwo, er); continue;}
            
            Node compoundNode = (Node) compNode.getCustom();
            
            if (nTwo.getEdgeTo(compoundNode)==null)
              graph.createEdge(nTwo, compoundNode, er);
            if (compoundNode.getEdgeTo(nOne)==null)
              graph.createEdge(compoundNode, nOne, er);
            
            
          } else {
            
            String value = st.getValue();
            if (value!=null) {
              if (value.equals("-->")) {
                er.setTargetArrow((Arrow.STANDARD));
              } else if(value.equals("--|")) {
                er.setTargetArrow((Arrow.T_SHAPE)); // T_SHAPE erst ab yFiles 2.7
              } else if(value.equals("..>")) {
                er.setLineType(LineType.DASHED_1);
                er.setTargetArrow((Arrow.STANDARD));
              } else if(value.equals("...")) {
                er.setLineType(LineType.DASHED_1);
                er.setArrow(Arrow.NONE);
              } else if(value.equals("-+-")) { // Ab 2.7 YFiles Version only...
                er.setLineType(LineType.DASHED_DOTTED_1);
                er.setArrow(Arrow.NONE);
              } else if(value.equals("---")) {
                er.setArrow(Arrow.NONE);
              } else if (value.length()==2) {
                // +p +m und sowas...
                el.setText(st.getValue());
                el.setFontSize(10);
              }
            }
            
            if (nOne.getEdgeTo(nTwo)==null) {
              myEdge = graph.createEdge(nOne, nTwo, er);
              
              edgeDescription.set(myEdge, r.getType().toString());
              if (st!=null) interactionDescription.set(myEdge, st.getName()); // => Subtype Name // compound, hidden compound, activation, inhibition, expression, repression, indirect effect, state change, binding/association, dissociation, missing interaction, phosphorylation, dephosphorylation, glycosylation, ubiquitination, methylation 
              // Jochen: "inhibition", "activation", "interaction", "physical interaction", "catalysis", "transcriptional control", "control"
            }
          }
        }
      } else {
        if (nOne.getEdgeTo(nTwo)==null) {
          myEdge = graph.createEdge(nOne, nTwo);
          
          edgeDescription.set(myEdge, r.getType().toString());
        }
      }

    }
    
    
    // Kanten von Knoten, welche exakt selben In- und Output haben zusammenfassen. (=> Groupnode)
    if (groupNodesWithSameEdges) {
      Node[] myNodes = graph.getNodeArray();
      for (int i=0; i<myNodes.length-1; i++) {
        NodeList nl = new NodeList();
        nl.add(myNodes[i]);
        
        // Nur "Sinnvolle" zusammenfassungen vornehmen.       
        if (hm.isGroupNode(myNodes[i]) || hm.getParentNode(myNodes[i])!=null || !hm.isNormalNode(myNodes[i]) || myNodes[i].edges().size()<1) continue;
          
        for (int j=i+1; j<myNodes.length; j++) {
          if (hm.isGroupNode(myNodes[j]) || hm.getParentNode(myNodes[j])!=null || !hm.isNormalNode(myNodes[j])) continue;
          
          // Wenn in selber (optischer) "Reihe" und selbe kanten, dann groupen.
          if (graph.getRealizer(myNodes[i]).getX()==graph.getRealizer(myNodes[j]).getX() || graph.getRealizer(myNodes[i]).getY()==graph.getRealizer(myNodes[j]).getY())
            if (nodesHaveSameEdges(myNodes[i], myNodes[j], graph))
              nl.add(myNodes[j]);
        }
        
        // Remove Outlier (More than 50px away from closest node)
        nl = removeOutlier(nl,graph);
        
        if (nl.size()>1) {
          // Create new Group node and setup hirarchies
          
          GroupNodeRealizer gnr = (GroupNodeRealizer) setupGroupNode(new NodeLabel(), "");
          
          //gnr.setAutoBoundsInsets(new YInsets(1, 1, 1, 1));
          
          //gnr.setBorderInsets(new YInsets(1, 1, 1, 1));
          
          Node n = graph.createNode(gnr);
          hm.convertToGroupNode(n);
          hm.setParentNode(nl, n);
          
          //gnr.setAutoBoundsInsets(new YInsets(1, 1, 1, 1));
          //gnr.setMinimalInsets(new YInsets(1, 1, 1, 1));
          //gnr.setBorderInsets(new YInsets(1, 1, 1, 1));
          
          // Copy edges to group node
          Node source = ((Node)nl.get(0));
          Edge e = source.firstInEdge();
          while (e!=null) {
            graph.createEdge(e.source(), n, graph.getRealizer(e));
            e=e.nextInEdge();
          }
          e = source.firstOutEdge();
          while (e!=null) {
            graph.createEdge(n, e.target(), graph.getRealizer(e));
            e=e.nextOutEdge();
          }
          // Remove edges from all internal nodes
          for (int j=0; j<nl.size(); j++) {
            source = ((Node)nl.get(j));
            EdgeCursor ec = source.edges();
            while (ec.ok()) {
              graph.removeEdge(ec.edge());
              ec.next();
            }
          }
          gnr.setEdgesDirty();
          
        }
      }
    }
    
    if (removeDegreeZeroNodes) { // Verwaiste knoten lˆschen
      Node[] nodes = graph.getNodeArray();
      for (Node n: nodes) {
        if (PWReferenceNodeTexts.contains(graph.getRealizer(n).getLabelText())) continue; // Keine Links auf andere Pathways removen.
        
        if (n.degree()<1) graph.removeNode(n);
      }
    }
    
    
    
    // Resize Group Nodes to fit content
    /*
    for (int i=0; i<parentGroupNodes.size(); i++) {
      NodeList nl = new NodeList();
      double x=Double.MAX_VALUE,y=Double.MAX_VALUE,width=0,height=0;
      for (int n2: groupNodeChildren.get(i)) {
        Entry two = p.getEntryForId(n2);
        if (two==null) {
          System.out.println("WARNING: Missing node for id " + n2);
          continue;
        }
        Node twoNode = (Node) two.getCustom();
        NodeRealizer nr = graph.getRealizer(twoNode);
        x = Math.min(x, nr.getX());
        y = Math.min(y, nr.getY());
        width=Math.max(width, (nr.getWidth()+nr.getX()));
        height=Math.max(height, (nr.getHeight()+nr.getY()));
        
        nl.add(twoNode);
      }
      
      // Reposition group node to fit content
      int offset = 5;
      graph.setLocation(parentGroupNodes.get(i), x-offset, y-offset-14);
      graph.setSize(parentGroupNodes.get(i), width-x+2*offset, height-y+2*offset+11);
      
      // Set hirarchie
      hm.setParentNode(nl, parentGroupNodes.get(i));

      // Reposition group node to fit content (2nd time is neccessary. Maybe yFiles bug...)
      graph.setLocation(parentGroupNodes.get(i), x-offset, y-offset-14);
      graph.setSize(parentGroupNodes.get(i), width-x+2*offset, height-y+2*offset+11);
      
    }*/
    
    
    // write out the graph using outputHandler
    IOHandler outputHandler = new GraphMLIOHandler(); // new GMLIOHandler();
    if (!silent) System.out.println("Writing file...");
    if (outputHandler != null && outputHandler.canWrite()) {
      Graph2DView view = new Graph2DView(graph);
      configureView(view);

      if (outputHandler instanceof GraphMLIOHandler) {
        Graph2DGraphMLHandler ioh = ((GraphMLIOHandler) outputHandler).getGraphMLHandler() ;
        
        //addNodeMap(nodeLabel, ioh,"name");
        addNodeMap(nodeLabel, ioh,"nodeLabel");
        addNodeMap(entrezIds, ioh,"entrezIds");
        addNodeMap(entityType, ioh,"type");
        addNodeMap(nodeDescription, ioh,"description");
        addNodeMap(keggOntIds, ioh,"keggIds");
        addNodeMap(uniprotIds, ioh,"uniprotIds");
        addNodeMap(ensemblIds, ioh,"ensemblIds");
        addNodeMap(nodeURLs, ioh, "url");
               
        addEdgeMap(edgeDescription, ioh, "description");
        addEdgeMap(interactionDescription, ioh, "interactionDescription");
      }
      
      if (new File(outFile).exists()) lastFileWasOverwritten=true; // Remember that file was already there.
      int retried=0;
      while (retried<3) {
        try {
          outputHandler.write(graph, outFile);
          break; // Success => No more need to retry
        } catch (IOException iex) {
          retried++;
          if (retried>2) System.err.println("Error while encoding file " + outFile + "\n" + iex);
        }
      }
    } else {
      System.err.println("Y OutputHandler can't write.");
    }
  }

  private static void addNodeMap(NodeMap nm, Graph2DGraphMLHandler ioh, String desc, KeyType keytype) {
    ioh.addInputDataAcceptor (desc, nm, KeyScope.NODE, keytype);
    ioh.addOutputDataProvider(desc, nm, KeyScope.NODE, keytype);
    //ioh.addAttribute(nm, desc, keytype);    // <= yf 2.6
  }
  private static void addNodeMap(NodeMap nm, Graph2DGraphMLHandler ioh, String desc) {
    addNodeMap(nm, ioh, desc, KeyType.STRING);//AttributeConstants.TYPE_STRING
  }
  private static void addEdgeMap(EdgeMap nm, Graph2DGraphMLHandler ioh, String desc, KeyType keytype) {
    ioh.addInputDataAcceptor (desc, nm, KeyScope.EDGE, keytype);
    ioh.addOutputDataProvider(desc, nm, KeyScope.EDGE, keytype);
    //ioh.addAttribute(nm, desc, keytype);    // <= yf 2.6
  }
  private static void addEdgeMap(EdgeMap nm, Graph2DGraphMLHandler ioh, String desc) {
    addEdgeMap(nm, ioh, desc, KeyType.STRING);//AttributeConstants.TYPE_STRING
  }


  
  private static NodeList removeOutlier(NodeList nl, Graph2D graph) {
    // Calculate minimal distances
    double[] minDists = new double[nl.size()];
    for (int j=0; j<nl.size(); j++) {
      NodeRealizer n1 = graph.getRealizer((Node) nl.get(j));
      double minDist=Double.MAX_VALUE;
      for (int k=0; k<nl.size(); k++) {
        if (j==k) continue;
        NodeRealizer n2 = graph.getRealizer((Node) nl.get(k));
        double dist = Math.max(Math.abs(n1.getCenterX()-n2.getCenterX()), Math.abs(n1.getCenterY()-n2.getCenterY()));
        minDist = Math.min(minDist, dist);
      }
      //System.out.println(minDist + " \t" + n1.getLabelText() );
      minDists[j] = minDist;
    }
    
    ArrayList<Integer> toRemove = new ArrayList<Integer>();
    if (nl.size()<2) return nl; // one node
    
    for (int j=0; j<minDists.length; j++)
      if (minDists[j]>50) toRemove.add(j);
    
    // Nothing to do?
    if (toRemove.size()<1) return nl;
    
    NodeList nl2 = new NodeList(); 
    for (int j=0; j<nl.size(); j++) {
      if (toRemove.contains(j)) continue;
      nl2.add(nl.get(j));
    }
    nl = nl2;
    
    return nl;
  }
  
  /**
   * Standard Setup for group nodes.
   * @param nl
   * @param changeCaption = null if you don't want to change the caption.
   */
  private static NodeRealizer setupGroupNode(NodeLabel nl, String changeCaption) {
    GroupNodeRealizer nr = new GroupNodeRealizer();
    ((GroupNodeRealizer)nr).setGroupClosed(false);
    nr.setTransparent(true);
    
    if (changeCaption!=null) {
      String newText = changeCaption; //"Group";
      nl.setText(newText);
    }
    
    nr.setMinimalInsets(new YInsets(5, 2, 2, 2)); // top, left, bottom, right
    nr.setAutoBoundsEnabled(true);
    nl.setPosition(NodeLabel.TOP);
    nl.setBackgroundColor(new Color((float)0.8,(float)0.8,(float)0.8,(float)0.5));
    nl.setFontSize(10);
    nl.setAutoSizePolicy(NodeLabel.AUTOSIZE_NODE_WIDTH);
    
    nr.setLabel(nl);
    
    return nr;
  }
  
  
  /**
   * Tests, if two nodes do have exactly the same source and target nodes on their edges and if the edges do have the same shape (Arrow, LineType and description).
   * @param n1
   * @param n2
   * @param graph
   * @return
   */
  public static boolean nodesHaveSameEdges(Node n1, Node n2, Graph2D graph) {
    if (n1.inDegree()!=n2.inDegree() || n1.outDegree()!=n2.outDegree()) return false;
    
    Edge e = n1.firstInEdge();
    while (e!=null) {
      Edge e2 = n2.firstInEdge();
      boolean found = false;
      while (e2!=null) {
        if (e.source().equals(e2.source())) {
          if (edgesEqualExceptTargets(e, e2, graph)) {
            found = true;
            break;
          }
        }
        e2 = e2.nextInEdge();
      }
      
      if (!found) return false; // Edge from n1 not in n2
      e = e.nextInEdge();
    }

    // Same for outEdges
    e = n1.firstOutEdge();
    while (e!=null) {
      Edge e2 = n2.firstOutEdge();
      boolean found = false;
      while (e2!=null) {
        if (e.target().equals(e2.target())) {
          if (edgesEqualExceptTargets(e, e2, graph)) {
            found = true;
            break;
          }
        }
        e2 = e2.nextOutEdge();
      }
      
      if (!found) return false; // Edge from n1 not in n2
      e = e.nextOutEdge();
    }
    
    return true;
  }
  
  public static boolean edgesEqualExceptTargets(Edge e, Edge e2, Graph2D graph) {
    EdgeRealizer er = graph.getRealizer(e);
    EdgeRealizer er2 = graph.getRealizer(e2);
    if (er.equals(er2) || er.getTargetArrow().equals(er2.getTargetArrow()) && er.getSourceArrow().equals(er2.getSourceArrow()) && er.getLineType().equals(er2.getLineType()) && er.getLabelText().equals(er2.getLabelText())) {
      return true;
    }
    return false;
  }
  
  public static Color ColorFromHTML(String theColor) {
    if (theColor.startsWith("#")) theColor = theColor.substring(1);
    if (theColor.trim().equalsIgnoreCase("none")) theColor="000000";
    
    if (theColor.length() != 6)
      throw new IllegalArgumentException("Not a valid HTML color: " + theColor);
    return new Color(
      Integer.valueOf(theColor.substring(0, 2), 16).intValue(),
      Integer.valueOf(theColor.substring(2, 4), 16).intValue(),
      Integer.valueOf(theColor.substring(4, 6), 16).intValue());
  }

  public static boolean isNumber(String s) {
    if (s==null || s.trim().isEmpty()) return false;
    char[] m = s.toCharArray();
    for (char c: m)
      if (!Character.isDigit(c)) return false;
    return true;
  }
  
  
  /**
   * Configures the view that is used as rendering environment for some output
   * formats.
   */
  private static void configureView(Graph2DView view) {
    Graph2D graph = view.getGraph2D();
    Rectangle box = graph.getBoundingBox();
    // Dimension dim = //inBox.getSize();
    // view.setPreferredSize(dim);
    // view.setSize(dim);
    view.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    view.fitContent();
    view.setPaintDetailThreshold(0.0); // never switch to less detail mode
  }
  
  
  private static int aufrufNr=0;
  private static int aufrufeGesamt=0; // SET THIS VALUE!
  private static int lastPerc=-1;
  private static synchronized void DisplayBar(String additionalText) {
    // ANSI Codes siehe http://en.wikipedia.org/wiki/ANSI_escape_code
    aufrufNr++;
    int perc = Math.min((int)((double)aufrufNr/(double)aufrufeGesamt*100), 100);
    String percString = perc + "%";
    
    
    // Simples File-out oder Eclipse-Output-Window tool
    if (System.console()==null || System.console().writer()==null) {
      if (perc!=lastPerc) {
        System.out.println(percString + (additionalText!=null && !additionalText.isEmpty()? " " + additionalText:"") );
        lastPerc=perc;
      }
      return;
    }
    
    // H¸bsche ANSI ProgressBar ;-)
    String anim= "|/-\\";
    StringBuilder sb = new StringBuilder();
    int x = perc / 2;
    sb.append("\r\033[K"); // <= clear line, Go to beginning
    sb.append("\033[107m"); // Bright white bg color
    int kMax = 50;
    for (int k = 0; k < kMax; k++) {
      if (x==k) sb.append("\033[100m"); // grey like bg color
      
      /*
      // % Zahl ist immer am "Farbschwellwert" (klebt am rechten bankenrand)
      if (x<percString.length()) {
        if (x<=k && k<x+percString.length()) sb.append("\033[93m"+percString.charAt(k-x)); // yellow
        else sb.append(" ");
      } else {
        if (k<x && (x-percString.length())<=k) sb.append("\033[34m"+percString.charAt(1-(x-percString.length()-k+1))); // blue 
        else sb.append(" ");
      }*/
      
      // %-Angabe zentriert
      int pStart = kMax/2-percString.length()/2;
      int pEnd = kMax/2+percString.length()/2;
      if (k>=pStart && k<=pEnd) {
        char c = ' ';
        if (k-pStart<percString.length()) c = percString.charAt(k-pStart);
        if (x<=k) sb.append("\033[93m"+c);
        if (x> k) sb.append("\033[34m"+c);
      } else
        sb.append(" ");
      
    }

    sb.append("\033[0m "); // Reset colors and stuff.
    sb.append("\033[93m" + anim.charAt(aufrufNr % anim.length())  + " \033[1m" +  (additionalText!=null && !additionalText.isEmpty()? additionalText:""));
    sb.append("\033[0m");
    
    //   \033[?25l  <=hide cursor.
    //   \033[?25h  <=show cursor.
    
    try {
      System.console().writer().print(sb.toString());
      System.console().flush();
    } catch (Exception e) {e.printStackTrace();}
    
    return; // sb.toString();
  }
}
