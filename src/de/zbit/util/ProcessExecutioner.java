package de.zbit.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessExecutioner {

  public static final int BUFFER_SIZE = 1024;
  
  protected static File defaultWorkDir = null;
  
  // TODO: make configurable
  protected static String javaBinPath = "/usr/bin/java";
  
  public static void setDefaultWorkDir(File workdir) {
    defaultWorkDir = workdir;
  }
  
  public static int executeProcess(String command, OutputStream processOutput) throws IOException {
    return executeProcess(Arrays.asList(command.split("\\s+")), processOutput);
  }
  
  public static int executeProcess(List<String> command, OutputStream processOutput) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    pb.directory(defaultWorkDir);
    Process p = pb.start();
    
    p.getOutputStream();
    
    InputStream in = p.getInputStream();
    
    int len;
    byte[] buffer = new byte[BUFFER_SIZE];
    while( (len = in.read(buffer)) != -1 ) {
      processOutput.write(buffer, 0, len);
    }
    
    try {
      int exitCode = p.waitFor();
      return exitCode;
    } catch (InterruptedException e) {
      System.out.println("Process was interrupted:");
      e.printStackTrace();
    }
    return -1; 
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
