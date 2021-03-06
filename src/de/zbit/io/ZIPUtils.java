/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import de.zbit.io.bzip2.CBZip2InputStream;
import de.zbit.io.tar.TarEntry;
import de.zbit.io.tar.TarInputStream;


/**
 * 
 * @author Clemens Wrzodek
 * @contens -Zipping / Unzipping /GZipping
 * @version $Rev$
 * @since 1.0
 */
public class ZIPUtils {
  
  /**
   * Allows for unpacking of archives into a specific path.
   */
  public static String prefixOfOutFile = "";
  
  /**
   * While extracting archives, skip files that already exist.
   */
  public static boolean skipIfExist=true;
  
  /**
   * The buffer that is used to extract some files.
   */
  public static int BUFFER = 4096;
  
  /**
   * This is used to search for files, relative to parent packages.
   */
  public static Class<?> parentClass = ZIPUtils.class;
  
  private static byte[] desKeyData = new byte[] {0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05,0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06,0x03, 0x04, 0x03, 0x04,0x03, 0x04,0x05, 0x06};
  
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static ByteArrayOutputStream BZ2unCompressData(String INfilename) throws IOException {
    // Now decompress archive
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    
    /*CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    CBZip2InputStream in2 = new CBZip2InputStream(new BufferedInputStream(csumi));
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int x;
    while ((x = in2.read()) != -1)
      out.write(x);
    in2.close();
    return out;*/
    
    BufferedReader in2 = new BufferedReader( new InputStreamReader( new CBZip2InputStream(fi)));
    int s;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while ((s = in2.read()) != -1) {
      out.write(s);
    }
    //out.close();
    in2.close();
    return out;
    
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static BufferedReader BZ2unCompressReader(String INfilename) throws IOException {
    InputStream in = BZ2unCompressStream(INfilename);
    
    if(in!=null) {
      return new BufferedReader( new InputStreamReader(in));
    } else {
      return null;
    }
  }
  
  public static InputStream BZ2unCompressStream(String INfilename) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi == null) {
      return null;
    }
    
    return new CBZip2InputStream(new CheckedInputStream(fi,new CRC32()));
  }
  
  /**
   * 
   * @param in
   * @return
   */
  public static String deCrypt(InputStream in) {
    OutputStream out = deCryptStream(in);
    return (out==null)?"":out.toString();
  }
  
  /**
   * 
   * @param in
   * @return
   */
  public static OutputStream deCryptStream(InputStream in) {
    //byte[] buf = new byte[1024];
    
    //AlgorithmParameterSpec paramSpec = new IvParameterSpec(desKeyData);
    OutputStream out = null;
    try {
      DESedeKeySpec keyspec = new DESedeKeySpec(desKeyData);
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
      SecretKey key = keyfactory.generateSecret(keyspec);
      
      Cipher dcipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      dcipher.init(Cipher.DECRYPT_MODE, key);
      
      out = new ByteArrayOutputStream();
      in = new CipherInputStream(in, dcipher);
      // Read in the decrypted bytes and write the cleartext to out
      //int numRead = 0;
      //while ((numRead = in.read(buf)) >= 0) {
      //  out.write(buf, 0, numRead);
      //}
      int x;
      while ((x = in.read()) != -1) {
        out.write(x);
      }
      out.flush();
      out.close();
      
    } catch (Exception e) {e.printStackTrace();}
    
    return out;
  }
  
  public static InputStream deCryptInputStream(InputStream in) {
    CipherInputStream cis = null;
    try {
      DESedeKeySpec keyspec = new DESedeKeySpec(desKeyData);
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
      SecretKey key = keyfactory.generateSecret(keyspec);
      
      Cipher dcipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      dcipher.init(Cipher.DECRYPT_MODE, key);
      
      cis = new CipherInputStream(in, dcipher);
      
    } catch (Exception e) {e.printStackTrace();}
    
    return cis;
  }
  
  public static OutputStream enCryptOutputStream(OutputStream out) {
    CipherOutputStream cos = null;
    //AlgorithmParameterSpec paramSpec = new IvParameterSpec(desKeyData);
    try {
      DESedeKeySpec keyspec = new DESedeKeySpec(desKeyData);
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
      SecretKey key = keyfactory.generateSecret(keyspec);
      
      Cipher ecipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      ecipher.init(Cipher.ENCRYPT_MODE, key);
      
      cos = new CipherOutputStream(out, ecipher);
      
    } catch (Exception e) {e.printStackTrace();}
    return cos;
  }
  
  /**
   * 
   * @param in
   * @return
   */
  public static OutputStream enCrypt(InputStream in) {
    return enCrypt(in, new BufferedOutputStream(new ByteArrayOutputStream()));
  }
  /**
   * 
   * @param in
   * @param out2
   * @return
   */
  public static OutputStream enCrypt(InputStream in, OutputStream out2) {
    
    OutputStream out = out2;
    //AlgorithmParameterSpec paramSpec = new IvParameterSpec(desKeyData);
    try {
      DESedeKeySpec keyspec = new DESedeKeySpec(desKeyData);
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
      SecretKey key = keyfactory.generateSecret(keyspec);
      
      Cipher ecipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      ecipher.init(Cipher.ENCRYPT_MODE, key);
      
      //InputStream in = new ByteArrayInputStream(inString.getBytes());
      out = new CipherOutputStream(out2, ecipher);
      // Read in the cleartext bytes and write to out to encrypt
      //int numRead = 0;
      //while ((numRead = in.read(buf)) >= 0) {
      //  out.write(buf, 0, numRead);
      //}
      int x;
      while ((x = in.read()) != -1) {
        out.write(x);
      }
      out.flush();
      out.close();
      
    } catch (Exception e) {e.printStackTrace();}
    return out;
  }
  
  /**
   * 
   * @param inString
   * @return
   */
  public static ByteArrayOutputStream enCrypt(String inString) {
    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
    enCrypt(inString, out2);
    return out2;
  }
  
  /**
   * 
   * @param inString
   * @param out2
   */
  public static void enCrypt(String inString, OutputStream out2) {
    InputStream in = new ByteArrayInputStream(inString.getBytes());
    enCrypt(in,out2);
  }
  
  /**
   * 
   * @param infile
   * @return
   * @throws IOException
   */
  public static String GUnzip(String infile) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(infile, parentClass);
    if (fi==null) {
      return null;
    }
    
    //Asuumes your file ends with ".gz" - returns outFilename
    String outFilename = infile.substring(0, infile.length()-3);
    BufferedReader in2 = new BufferedReader( new InputStreamReader(
      new GZIPInputStream(fi)));
    int s;
    BufferedWriter out = new BufferedWriter(new FileWriter(outFilename));
    while ((s = in2.read()) != -1) {
      out.write(s);
    }
    out.close();
    in2.close();
    return outFilename;
  }
  
  /**
   * 
   * @param infile
   * @return
   * @throws IOException
   */
  public static ByteArrayOutputStream GUnzipData(String infile) throws IOException {
    BufferedReader in2 = GUnzipReader(infile);
    if (in2==null) {
      return null;
    }
    
    int s;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while ((s = in2.read()) != -1) {
      out.write(s);
    }
    //out.close();
    in2.close();
    return out;
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static BufferedReader GUnzipReader(String INfilename) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    return GUnzipReader(fi);
  }
  
  /**
   * 
   * @param fi
   * @return
   * @throws IOException
   */
  public static BufferedReader GUnzipReader(InputStream fi) throws IOException {
    
    return new BufferedReader( new InputStreamReader( new GZIPInputStream(fi)));
  }
  
  /**
   * 
   * @param fi
   * @return
   * @throws IOException
   */
  public static InputStream GUnzipStream(InputStream fi) throws IOException {
    if (fi==null) {
      return null;
    }
    
    return new GZIPInputStream(fi);
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static InputStream GUnzipStream(String INfilename) throws IOException{
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    return GUnzipStream(fi);
  }
  
  /**
   * 
   * @throws IOException
   */
  public static void GZIP() throws IOException {
    // Muesste noch in compress und uncompress getrennt werden, bei Bedarf!
    
    // first compress inputfile.txt into out.gz
    BufferedReader in = new BufferedReader(new FileReader("inputfile.txt"));
    BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream("out.gz")));
    int c;
    while ((c = in.read()) != -1) {
      out.write(c);
    }
    in.close();
    out.close();
    
    // now decompress our new file
    BufferedReader in2 = new BufferedReader( new InputStreamReader(
      new GZIPInputStream(new FileInputStream("out.gz"))));
    String s;
    while ((s = in2.readLine()) != null) {
      System.out.println(s);
    }
  }
  
  /**
   * @param inputPath
   * @param targetPath
   * @throws IOException
   */
  public static void GZip(String inputPath, String targetPath) throws IOException {
    IOException exc1 = null;
    FileOutputStream fileOutputStream = null;
    GZIPOutputStream gzipOuputStream = null;
    FileInputStream fileInput = null;
    try {
      fileOutputStream = new FileOutputStream(targetPath);
      gzipOuputStream = new GZIPOutputStream(fileOutputStream);
      fileInput = new FileInputStream(inputPath);
      
      int bytesRead;
      
      byte[] buffer = new byte[1024];
      while ((bytesRead = fileInput.read(buffer)) > 0) {
        gzipOuputStream.write(buffer, 0, bytesRead);
      }
      
      fileInput.close();
      gzipOuputStream.finish();
      gzipOuputStream.close();
    } catch (IOException exc) {
      /*
       * Catching this exception makes sure that we have still the chance to
       * close the streams. Otherwise they will stay opened although the
       * execution of this method is over.
       */
      exc1 = exc;
    } finally {
      try {
        try {
          fileInput.close();
        } finally {
          gzipOuputStream.close();
        }
      } catch (IOException exc2) {
        // Ok, we lost. No chance to really close these streams. Heavy error.
        if (exc1 != null) {
          exc2.initCause(exc1);
        }
        throw exc2;
      } finally {
        if (exc1 != null) {
          throw exc1;
        }
      }
    }
  }
  
  /**
   * 
   * @param in
   * @return
   */
  public static StringBuffer inputStream2Data(InputStream in) {
    StringBuffer ret = new StringBuffer();
    try {
      int x;
      while ((x = in.read()) != -1) {
        ret.append(x);
      }
    } catch (Exception e) {e.printStackTrace();}
    return ret;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    /*String a = "Dies ist ein\nTest String.\tbb\raa%!\"%\\";
    System.out.println ("1"+a+"\n");
    ByteArrayInputStream  b = new ByteArrayInputStream(enCrypt(a).toByteArray());
    System.out.println ("2"+b.toString()+"\n");
    a = deCrypt(b);
    System.out.println ("3"+a+"\n");
    if (true) return;*/
    
    // Nicht wundern! Ist nicht die schnellste Funktion...
    String myDir = "Z:\\workspace\\dipl\\tmpTestFiles\\Matritzen\\neu\\NEU ANNOTIERT\\allerneuste/"; // mit "/" abschliessend!
    String[] files = new File(myDir).list();
    // XXX: PWMANNOTATIONS.JAVA DANACH AUF DIE GEMERGTEN PWMS AUSFUEHREN!!!, AN ARAB_ANNOT DENKEN!
    for (String file: files) {
      if (!file.endsWith(".arabAnnot2")) {
        continue;
      }
      try {
        System.out.println("Encrypting " + file + "...");
        String fn = myDir + file + ".tmp";
        enCrypt( new FileInputStream(myDir + file), new FileOutputStream(fn));
        System.out.println("Zipping...");
        ZIPcompress(new String[]{fn}, myDir + file + ".zip", "ModuleMaster\nEncrypted Data", true);
        new File(fn).delete();
        
        // Decrypt test
        System.out.println("Decrypt test...");
        ByteArrayOutputStream out = ZIPunCompressData(myDir + file + ".zip");
        String myTest = deCrypt(new ByteArrayInputStream(out.toByteArray()));
        System.out.println (myTest.substring(0, Math.min(1000, myTest.length())));
        
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * 
   * @param in
   * @return
   * @throws IOException
   */
  public static ByteArrayOutputStream TARunCompressData(ByteArrayInputStream in) throws IOException {
    if (in==null) {
      return null;
    }
    
    CheckedInputStream csumi = new CheckedInputStream(in,new CRC32());
    TarInputStream in2 = new TarInputStream(new BufferedInputStream(csumi));
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TarEntry ze;
    
    while ((ze = in2.getNextEntry()) != null && (ze.getName().endsWith("/") || ze.getName().endsWith("\\"))) {
      ;
    }
    //while ((ze = in2.getNextEntry()) != null) {
    //System.out.println(ze);
    int x;
    while ((x = in2.read()) != -1) {
      out.write(x);
    }
    if (in2.getNextEntry()!=null) {
      System.out.println("TAR stream contains multiple files. Just taking the first file (" + ze.getName() + ").");
    }
    //break; // Read only the first file
    //}
    in2.close();
    return out;
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static ByteArrayOutputStream TARunCompressData(String INfilename) throws IOException {
    // Now decompress archive
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    
    CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    TarInputStream in2 = new TarInputStream(new BufferedInputStream(csumi));
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TarEntry ze;
    
    // Jump to first file, which is not a folder
    while ((ze = in2.getNextEntry()) != null && (ze.getName().endsWith("/") || ze.getName().endsWith("\\"))) {
      ;
    }
    //while ((ze = in2.getNextEntry()) != null) {
    //System.out.println(ze);
    int x;
    while ((x = in2.read()) != -1) {
      out.write(x);
    }
    if (in2.getNextEntry()!=null) {
      System.out.println("TAR Archive '" + INfilename + "' contains multiple files. Just taking the first file (" + ze.getName() + ").");
    }
    //break; // Read only the first file
    //}
    in2.close();
    return out;
  }
  
  /**
   * Returns input stream for streaming the content of the first file in the tar
   * @param in
   * @return
   * @throws IOException
   */
  public static InputStream TARunCompressStream(InputStream in) throws IOException {
    CheckedInputStream csumi = new CheckedInputStream(in,new CRC32());
    TarInputStream in2 = new TarInputStream(new BufferedInputStream(csumi));
    
    TarEntry ze;
    while ((ze = in2.getNextEntry()) != null && (ze.getName().endsWith("/") || ze.getName().endsWith("\\"))) {
      ;
    }
    
    if (ze!=null) {
      return in2; // Liefert NUR DIE ERSTE DATEI!
    } else {
      in2.close();
      return null;
    }
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static InputStream TARunCompressStream(String INfilename) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    return TARunCompressStream(fi);
  }
  
  
  /**
   * Returns an input reader for reading the content of the first file in the tar
   * @param in
   * @return
   * @throws IOException
   */
  public static BufferedReader TARunCompressReader(InputStream in) throws IOException { //ByteArrayInputStream
    InputStream in2 = TARunCompressStream(in);
    
    if(in2!=null) {
      return new BufferedReader(new InputStreamReader(in2));
    } else {
      return null;
    }
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static BufferedReader TARunCompressReader(String INfilename) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    return TARunCompressReader(fi);
  }
  
  /**
   * 
   * @param INfilenames
   * @param outFilename
   * @param Comment
   * @throws IOException
   */
  public static void ZIPcompress(String INfilenames, String outFilename, String Comment) throws IOException {
    ZIPcompress(new String[]{INfilenames}, outFilename, Comment);
  }
  
  /**
   * 
   * @param INfilenames
   * @param outFilename
   * @param Comment
   * @throws IOException
   */
  public static void ZIPcompress(String[] INfilenames, String outFilename, String Comment) throws IOException {
    ZIPcompress(INfilenames, outFilename, Comment, false);
  }
  
  /**
   * 
   * @param INfilenames
   * @param outFilename
   * @param Comment
   * @param ignorePath
   * @throws IOException
   */
  public static void ZIPcompress(String[] INfilenames, String outFilename, String Comment, boolean ignorePath) throws IOException {
    FileOutputStream f = new FileOutputStream(outFilename);
    CheckedOutputStream csum = new CheckedOutputStream(f, new CRC32());
    ZipOutputStream out = new ZipOutputStream(
      new BufferedOutputStream(csum));
    out.setComment(Comment); //Custom Archive comment
    out.setLevel(9); //0-9. 9 ist Maximum!
    
    // now adding files -- any number with putNextEntry() method
    for (int i=0; i< INfilenames.length; i++) {
      String nameInZip = INfilenames[i];
      if (ignorePath) {
        nameInZip = new File(INfilenames[i]).getName();
      }
      //BufferedReader in = new BufferedReader( new FileReader(INfilenames[i])); //<= funzt nur bei ASCII codes.
      InputStream in = OpenFile.searchFileAndGetInputStream(INfilenames[i], parentClass);
      out.putNextEntry(new ZipEntry(nameInZip));
      int c;
      while ((c = in.read()) != -1) {
        out.write(c);
      }
      in.close();
    }
    out.close();
    
    // printing a checksum calculated with CRC32
    // System.out.println("Checksum: "+csum.getChecksum().getValue());
    
  }
  
  /**
   * 
   * @param INData
   * @param desiredFilename
   * @param outFilename
   * @param Comment
   * @throws IOException
   */
  public static void ZIPcompressData(Object INData[], String desiredFilename, String outFilename, String Comment) throws IOException {
    try  {
      // ZIP
      FileOutputStream f = new FileOutputStream (outFilename) ;
      ZipOutputStream zout = new ZipOutputStream (new BufferedOutputStream(f));
      zout.setComment(Comment); //Custom Archive comment
      zout.setLevel(9); //0-9. 9 ist Maximum!
      
      zout.putNextEntry(new ZipEntry(desiredFilename));
      ObjectOutputStream aout = new ObjectOutputStream (zout) ;
      for (int i=0; i<INData.length; i++) {
        aout.writeObject (INData[i]) ;
      }
      
      zout.close () ;
    }
    catch  (Exception e) {e.printStackTrace();}
  }
  
  /**
   * 
   * @param INData
   * @param desiredFilename
   * @param outFilename
   * @param Comment
   * @throws IOException
   */
  public static void ZIPcompressData(String INData, String desiredFilename, String outFilename, String Comment) throws IOException {
    FileOutputStream f = new FileOutputStream(outFilename);
    CheckedOutputStream csum = new CheckedOutputStream(f, new CRC32());
    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(csum));
    out.setComment(Comment); //Custom Archive comment
    out.setLevel(9); //0-9. 9 ist Maximum!
    
    out.putNextEntry(new ZipEntry(desiredFilename));
    out.write(INData.getBytes());
    out.close();
  }
  
  /**
   * 
   * @param INfilename
   * @return true if and only if files have been successfully extracted (CRC Checked).
   * @throws IOException
   */
  public static boolean ZIPunCompress(String INfilename) throws IOException {
    return ZIPunCompress(INfilename,false);
  }
  
  /**
   * 
   * @param INfilename
   * @param silentMode
   * @return true if and only if files have been successfully extracted (CRC Checked).
   * @throws IOException
   */
  public static boolean ZIPunCompress(String INfilename, boolean silentMode) throws IOException {
    return ZIPunCompress(new String[]{INfilename},silentMode);
  }
  
  /**
   * 
   * @param INfilename
   * @param fileInZip
   * @param outFile
   * @return true if and only if files have been successfully extracted (CRC Checked).
   * @throws IOException
   */
  public static boolean ZIPunCompress(String INfilename, String fileInZip, String outFile) throws IOException {
    // Check, if file exists
    File inFile;
    try {
      inFile = OpenFile.searchFile(INfilename);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
    if (inFile==null) {
      return false;
    }
    
    // Look, if desired file in Zip exists
    ZipFile zipfile = new ZipFile(inFile);
    ZipEntry entry = zipfile.getEntry(fileInZip);
    if (entry==null) {
      return false;
    }
    
    // Extract file
    BufferedInputStream is = new BufferedInputStream(zipfile.getInputStream(entry));
    int count;
    byte data[] = new byte[BUFFER];
    FileOutputStream fos = new FileOutputStream(outFile);
    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
    while ((count = is.read(data, 0, BUFFER)) != -1) {
      dest.write(data, 0, count);
    }
    dest.flush();
    dest.close();
    is.close();
    
    return true;
  }
  
  /**
   * 
   * @param INfilenames
   * @return true if and only if files have been successfully extracted (CRC Checked).
   * @throws IOException
   */
  public static boolean ZIPunCompress(String[] INfilenames) throws IOException {
    return ZIPunCompress(INfilenames,false);
  }
  
  /**
   * 
   * @param INfilenames
   * @param silentMode
   * @return true if and only if files have been successfully extracted (CRC Checked).
   * @throws IOException
   */
  public static boolean ZIPunCompress(String[] INfilenames, boolean silentMode) throws IOException {
    boolean noErrors=true;
    for (int i=0; i< INfilenames.length; i++) {
      // Now decompress archive
      InputStream fi = OpenFile.searchFileAndGetInputStream(INfilenames[i], parentClass);
      if (fi==null) {
        System.err.println("Could not get input stream for " + INfilenames[i]);
        noErrors=false;
        continue;
      }
      ZipInputStream in2 = new ZipInputStream( new BufferedInputStream(fi));
      
      ZipEntry ze;
      while ((ze = in2.getNextEntry()) != null) {
        if (!silentMode) {
          System.out.println("Extracting file "+ze);
        }
        if (!silentMode && ze.getComment()!=null && ze.getComment().length()!=0) {
          System.out.println("Comment: " + ze.getComment());
        }
        
        // Eventually create directories
        if (ze.getName().endsWith("/") || ze.getName().endsWith("\\")) {
          try {
            new File(prefixOfOutFile + ze.getName()).mkdirs();
            continue;
          } catch (Throwable e) {}
        }
        
        // Check if file already exists
        if (skipIfExist && new File(prefixOfOutFile+ze.getName()).exists()) {
          if (ze.getSize()>=0 && ze.getSize()==new File(prefixOfOutFile+ze.getName()).length()) {
            continue;
          } else {
            if (!silentMode) {
              System.out.println("Overwriting '" + ze.getName() + "', because file length differs.");
            }
          }
        }
        
        // Create output streams
        FileOutputStream fos = new FileOutputStream(prefixOfOutFile+ze.getName());
        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
        CheckedInputStream cisZE = new CheckedInputStream(in2, new CRC32());
        
        // Uncompress and write data
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = cisZE.read(data, 0, BUFFER)) != -1) {
          dest.write(data, 0, count);
        }
        
        // Flush and close streams
        dest.flush();
        dest.close();
        fos.close();
        
        // CRC-Check
        if (ze.getCrc()!=cisZE.getChecksum().getValue()) {
          System.err.println("CRC-Error in file '" + ze.getName() + "': extracted " + cisZE.getChecksum().getValue() + " but expected " + ze.getCrc());
          noErrors=false;
        }
      }
      
      in2.close();
      fi.close();
    }
    return noErrors;
  }
  
  /**
   * 
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static ByteArrayOutputStream ZIPunCompressData(String INfilename) throws IOException {
    // Now decompress archive
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    ZipInputStream in2 = new ZipInputStream(new BufferedInputStream(csumi));
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ZipEntry ze;
    while ((ze = in2.getNextEntry()) != null) {
      if (ze.getName().endsWith("/") || ze.getName().endsWith("\\")) {
        continue;
      }
      int x;
      while ((x = in2.read()) != -1) {
        out.write(x);
      }
      
      if (in2.getNextEntry()!=null) {
        System.out.println("ZIP Archive '" + INfilename + "' contains multiple files. Just taking the first file (" + ze.getName() + ").");
      }
      break;
    }
    in2.close();
    return out;
  }
  
  /**
   * 
   * @param INfilename
   * @param fileInZip
   * @return
   * @throws IOException
   */
  public static String ZIPunCompressData(String INfilename, String fileInZip) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return "";
    }
    
    // Now decompress archive
    CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    ZipInputStream in2 = new ZipInputStream(new BufferedInputStream(csumi));
    
    fileInZip = fileInZip.toLowerCase().trim();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ZipEntry ze;
    while ((ze = in2.getNextEntry()) != null) {
      if (!ze.getName().toLowerCase().trim().equals(fileInZip)) {
        continue;
      }
      
      int x;
      while ((x = in2.read()) != -1) {
        out.write(x);
      }
      break;
    }
    in2.close();
    return out.toString();
  }
  
  /**
   * Return a Reader for reading the content of a zip file
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static BufferedReader ZIPunCompressReader(String INfilename) throws IOException {
    InputStream in = ZIPunCompressStream(INfilename);
    if(in!=null) {
      return new BufferedReader(new InputStreamReader(in));
    } else {
      return null;
    }
  }
  
  /**
   * Returns an input stream for streaming the content of a zip file
   * @param INfilename
   * @return
   * @throws IOException
   */
  public static InputStream ZIPunCompressStream(String INfilename) throws IOException{
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return null;
    }
    
    CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    ZipInputStream in2 = new ZipInputStream(new BufferedInputStream(csumi));
    //if ((in2.getNextEntry()) != null) { // Liefert NUR DIE ERSTE DATEI!
    
    ZipEntry ze;
    while ((ze = in2.getNextEntry()) != null && (ze.getName().endsWith("/") || ze.getName().endsWith("\\"))) {
      ;
    }
    if (ze!=null) {
      return in2;
    } else {
      in2.close();
      return null;
    }
  }
  
  /**
   * Returns the file size of ZIP-compressed single files.
   * @param INfilename
   * @return Uncompressed file size of the file, that {@link #ZIPunCompressReader(String)} is deflating.
   * @throws IOException
   */
  public static long getUncompressedSizeOf_ZIPunCompressStream(String INfilename) throws IOException {
    InputStream fi = OpenFile.searchFileAndGetInputStream(INfilename, parentClass);
    if (fi==null) {
      return -1;
    }
    
    CheckedInputStream csumi = new CheckedInputStream(fi,new CRC32());
    ZipInputStream in2 = new ZipInputStream(new BufferedInputStream(csumi));
    //if ((in2.getNextEntry()) != null) { // Liefert NUR DIE ERSTE DATEI!
    
    ZipEntry ze;
    while ((ze = in2.getNextEntry()) != null && (ze.getName().endsWith("/") || ze.getName().endsWith("\\"))) {
      ;
    }
    if (ze!=null) {
      in2.close();
      return ze.getSize();
    } else {
      in2.close();
      return -1;
    }
  }
  
  /**
   * Returns the file size of ZIP-compressed single files.
   * <p>LIMITATIONS:<ul>
   * <li>Does only work for local files (not from a JAR-stream or something).</li>
   * <li>Does only work for files, less than 4GB of size.</li>
   * </ul></p>
   * @param INfilename
   * @return Uncompressed file size in bytes of any GZipped file.
   * @throws IOException
   * @see http://www.abeel.be/content/determine-uncompressed-size-gzip-file
   */
  public static long getUncompressedSizeOf_GZIPfile(String INfilename) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(INfilename, "r");
    raf.seek(raf.length() - 4);
    int b4 = raf.read();
    int b3 = raf.read();
    int b2 = raf.read();
    int b1 = raf.read();
    int val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
    raf.close();
    return val;
  }
}
