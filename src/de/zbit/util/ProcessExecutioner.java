/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @version $Rev$
 * @since 1.0
 */
public class ProcessExecutioner {

  public static final int BUFFER_SIZE = 1024;
  
  protected static File defaultWorkDir = null;
  
  // TODO: make configurable
  protected static String javaBinPath = "/usr/bin/java";
  
  public static void setDefaultWorkDir(File workdir) {
    defaultWorkDir = workdir;
  }
  
  /**
   * 
   * @param command, i.e. ./convertPathwayFiles.sh 
   * @param processOutput
   * @return the exit code of the process.
   * @throws IOException
   */
  public static int executeProcess(String command, OutputStream processOutput) throws IOException {
    return executeProcess(Arrays.asList(command.split("\\s+")), processOutput);
  }

  /**
   * Using this method, instead of the imploded {@link #executeProcess(String, OutputStream)},
   * allows to have spaces in arguments (e.g. "hal lo" as argument).
   * @param command, i.e. ./convertPathwayFiles.sh 
   * @param processOutput
   * @return the exit code of the process.
   * @throws IOException
   */
  public static int executeProcess(String[] command, OutputStream processOutput) throws IOException {
    return executeProcess(Arrays.asList(command), processOutput);
  }
  
  public static int executeProcess(List<String> command, OutputStream processOutput) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    pb.directory(defaultWorkDir);
    Process p = pb.start();
    
    p.getOutputStream();
    
    InputStream in = p.getInputStream();
    
    if( processOutput == null ) {
      processOutput = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          // intentionally left blank
        }
      };
    }
    int len;
    byte[] buffer = new byte[BUFFER_SIZE];
    while( (len = in.read(buffer)) != -1 ) {
      processOutput.write(buffer, 0, len);
    }
    
    try {
      int exitCode = p.waitFor();
      // do NOT use close here, or else you might be closing System.out
      processOutput.flush();
      return exitCode;
    } catch (InterruptedException e) {
      System.out.println("Process was interrupted:");
      e.printStackTrace();
    }
    return -1; 
  }
  
  /**
   * Executes a simple command and returns the applications output.
   * If an exception occurs, null is returned.
   * @param command
   * @return
   */
  public static String executeProcess(String... command) {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    try {
      ProcessExecutioner.executeProcess(command, new BufferedOutputStream(outStream));
    } catch (IOException e) {
      return null;
    }
    return outStream.toString();
  }
  
  
  public static int executeJavaProcess(String vmargs, String classpath,
      String mainClassName, List<String> command,
      OutputStream processOutput) throws IOException
  {
    List<String> javaCommand = new ArrayList<String>();
    javaCommand.add(javaBinPath);
    javaCommand.add(vmargs);
    javaCommand.add("-cp");
    javaCommand.add(classpath);
    javaCommand.add(mainClassName);
    javaCommand.addAll(command);
    return executeProcess(javaCommand, processOutput);
  }
}
