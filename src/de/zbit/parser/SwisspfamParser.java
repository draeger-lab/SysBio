/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.parser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.zbit.io.OpenFile;

/**
 * @author Finja B&uml;chel
 * 
 * 
 * Der Parser erstellt aus der ursprünglichen Swisspfam Datei, eine Datei in folgendem Format:
 * 
 * Protein (uniprot id) \t Domäne (Pfam id) \t startpos im Prot \t endpos im Prot \n
 * Beispiel:
 * 006L_IIV6  PF12299 231 324
 * 006L_IIV6 PF04383 19  126
 * 007R_IIV3 PF10927 181 430
 * 029R_IIV6 PF12299 1 100
 * @version $Rev$
 * @since 1.0
 */
public class SwisspfamParser {

  public static final Logger log = Logger.getLogger(SwisspfamParser.class.getName());
  /**
   * 
   * @param in, input file from SwissPfam
   * @param out, output file (tab delimited) <br>
   * containing : UniProt ID, [UniProtAC], Pfam ID, [domain start in amino acid sequence], 
   * [domain end in amino acid sequence]
   */
  public void parseFile(String in, String out, boolean includeAC, boolean includeBPNo, boolean includePBDomains) {
    log.info("Start parsing file: " + in + ", outFile: " + out + ", includeAC: " + includeAC + ", includeBPNo: " + includeBPNo);
    String line = "", protein = "", ac = "", pfamID = "";
    try {
      BufferedReader br = OpenFile.openFile(in);
      BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      int start = -1, end = -1;

      line = br.readLine();
      log.fine("line: '" + line + "'");

      //  >006L_IIV6        |============================================| Q91G88.1 352 a.a.
      //  DUF3627          1                             ____________     (216) PF12299.1 Protein of unknown function (DUF3627)  231-324
      //  KilA-N           1   _____________                              (1764) PF04383.6 KilA-N domain  19-126
      //
      //  >11011_ASFM2      |================================================| P0C9J5.1 286 a.a.
      //  v110             2 __________________         __________________    (87) PF01639.10 Viral family 110  1-111 165-274

      while ((line = br.readLine()) != null){
        log.fine("line: '" + line + "'");
        if(line.isEmpty()){log.fine("in erster if");continue;}
        if (line.substring(0, 1).equals(">")) {
          // get uniprot id
          start = 0;
          end = line.indexOf(' ', start + 1);
          if (end > 0)
            protein = line.substring(1, end);
          else
            protein = "";

          // get accession id
          start = line.indexOf('|', 0);
          start = line.indexOf('|', start + 1);
          end = line.indexOf('.', start + 1);

          if (start > 0 && end > 0)
            ac = line.substring(start + 2, end);
          else
            ac = "";
          log.fine("uniprotId: '" + protein + "', ac: '" + ac + "'");
        }
        else{
          start = line.indexOf(')', 1);
          end = line.indexOf('.', start+2); // PF id
          if (end < 0)
            end = line.indexOf(' ', start+2); // PB id
          //            if((start+2 - end) < 0) 
          //              end = line.indexOf(' ', end); // PB id

          if (start > 0 && end > 0)
            pfamID = line.substring(start + 2, end);
          else
            pfamID = "";

          while(line.indexOf("  ", end+1) > 0){
            end = line.indexOf("  ", end+1);
          }

          if(pfamID.startsWith("PF") || (pfamID.startsWith("PB") && includePBDomains)){            
            if(includeBPNo){
              List<int[]> number = parseNumbers(end, line);
              for (int[] is : number) {
                bw.append(protein + "\t");  //protein id
                if(includeAC)
                  bw.append(ac + "\t");     //protein ac

                bw.append(pfamID + "\t");   //pfam id
                bw.append(is[0] + "\t" + is[1]); // begin \t end
                bw.append("\n");
              }  
            }
            else{
              bw.append(protein + "\t");  //protein id
              if(includeAC)
                bw.append(ac + "\t");     //protein ac

              bw.append(pfamID + "\t");   //pfam id
              bw.append("\n");
            }
          }
          log.fine("else finished");
        }
      }

      br.close();
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    log.info("Parsing ready");
  }

  /**
   * 
   * @param in input file
   * @param out output file
   * @param species uniprot species identifier, i.e. "_HUMAN", or "_MOUSE"
   */
  public void getSpeciesProtDomFile(String in, String out, String species){
    log.info("getSpeciesSpecificProtDomFiles");
    try{
      BufferedReader br = new BufferedReader(new FileReader(in));
      BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      String line = "";
      while((line=br.readLine())!= null){
        String[]split = line.split("\t");
        if(split[0].endsWith(species))
          bw.append(line + "\n");      
      }
      br.close();
      bw.close();
    }
    catch(Exception e){
      log.log(Level.SEVERE, "File could not be generated.", e);
    }
  }


  /**
   * Returns a ziped File containing 1) the uniprot id, 2) the pfam id
   * @param bw, input file as BufferedeReader
   * @param outFilename 
   */
  public static void reformatProt_Dom_Files(BufferedReader bw, String outFilename) {
    log.info("reformatiingProt_Dom_File - outFilename: " + outFilename);
    try  {  
      // ZIP
      FileOutputStream f = new FileOutputStream (outFilename) ;             
      ZipOutputStream zout = new ZipOutputStream (new BufferedOutputStream(f)); 
      //zout.setComment(Comment); //Custom Archive comment
      zout.setLevel(9); //0-9. 9 ist Maximum!

      zout.putNextEntry(new ZipEntry(outFilename.replace(".zip", "")));

      String line;
      String[] splitt;
      while ((line = bw.readLine()) !=null) {
        splitt = line.split("\t");

        zout.write( (splitt[0]+ '\t' + splitt[1]+ '\n').getBytes() );
      }

      zout.close () ;             
    }  
    catch  (Exception e) {
      log.log(Level.SEVERE, "Could not reformat the file.", e);
    }  
  }

  /**
   * 
   * @param start
   * @param line
   * @return an array of numbers, where the domain starts and ends in the amino acid sequence
   */
  private List<int[]> parseNumbers(int start, String line) {
    List<int[]> list = new ArrayList<int[]>();
    boolean finish = false;

    int domStart = 0, domEnd = 0, end = 0;

    // getting the first numbers
    while (end == 0) {
      start = line.indexOf("  ", start);
      end = line.indexOf('-', start);
      try {
        domStart = Integer.parseInt(line.substring(start, end).trim());
      } catch (Exception e) {
        continue;
      }
    }

    start = end + 1;
    end = line.indexOf(' ', start);
    if (end < 0) {
      end = line.length();
      finish = true;
    }

    domEnd = Integer.parseInt(line.substring(start, end).trim());
    int[] i = new int[] { domStart, domEnd };
    list.add(i);

    while (!finish) {
      start = end;
      end = line.indexOf('-', start);
      domStart = Integer.parseInt(line.substring(start, end).trim());

      start = end + 1;
      end = line.indexOf(' ', start);
      if (end < 0) {
        end = line.length();
        finish = true;
      }

      domEnd = Integer.parseInt(line.substring(start, end).trim());
      i = new int[] { domStart, domEnd };
      list.add(i);
    }

    return list;
  }
}
