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
package de.zbit.util;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is a helper for parallizing your calculations.
 * You can simply submit all calculations to be done in parallel
 * and wait until they are ready.
 * 
 * <p><b> Usage Example: </b>
 * <pre>
 *  ThreadManager m = new ThreadManager();
 *  // Add a few jobs to execute
 *  for (int i=0; i<(numberOfSlots*5); i++) {
 *    Runnable r = ... // Any runnable.
 *    m.addToPool(r);
 *  }
 *  
 *  // Wait until all calculations finished or interrupted.
 *  m.awaitTermination();
 *  
 *  // You can work with the results here. 
 * </pre></p>
 * @author wrzodek
 * @version $Rev$
 */
public class ThreadManager {
  /**
   * The actual ThreadPoolExecutor that is used for
   * queue management and executing the runnables.
   */
  private ThreadPoolExecutor pool;
  
  /**
   * Number of actual or / and virtual processors in the current machine.
   * ( = Runtime.getRuntime().availableProcessors() ).
   */
  public final static int NUMBER_OF_PROCESSORS=Runtime.getRuntime().availableProcessors();
  
  /**
   * Initializies a new ThreadManager with ideal settings(
   * NUMBER_OF_PROCESSORS - 1, but minimal 1). So always at least one processor
   * is left for GUI operations and other stuff.
   */
  public ThreadManager() {
    this(Math.max(NUMBER_OF_PROCESSORS-1, 1));
  }
  
  /**
   * Initializes a new ThreadManager with the given number of slots.
   * @param numberOfSlots
   */
  public ThreadManager(int numberOfSlots) {
    super();
    pool = new ThreadPoolExecutor(numberOfSlots, numberOfSlots, 0L,
        TimeUnit.MILLISECONDS, new LinkedBlockingQueue <Runnable>());
  }
  
  /**
   * @return true if and only if there is an idle slot that
   * is currently not executing a task.
   */
  public boolean isFreeSlot() {
    return (getPoolSize()<pool.getCorePoolSize());
    // TODO: ProgressListener, AllDoneListener (kein sleep!)
  }
  
  /**
   * @return the number of slots that will be used for
   * parallel execution of runnables.
   */
  public int getNumberOfSplots() {
    return pool.getCorePoolSize();
  }
  
  /**
   * Returns the approximate total number of tasks that
   * have been scheduled for execution. Because the states
   * of tasks and threads may change dynamically during computation,
   * the returned value is only an approximation, but one that does
   * not ever decrease across successive calls.
   *  
   * @return the number of tasks currently queued. This includes
   * also all terminated tasks!
   */
  public int getPoolSize() {
    return (int) pool.getTaskCount();
  }
  
  /**
   * WARNING: this object is the core of this class.
   * Modifications might have a hughe impact or even
   * break this class!
   * @return the ThreadPoolExecutor that is used for the
   * internal queue management and execution of runnables.
   */
  public ThreadPoolExecutor getThreadPoolExecutor() {
    return pool;
  }
  
  /**
   * Blocks until all tasks have completed execution, or
   * completed execution after a shutdown request, or
   * the timeout occurs, or the current thread is
   * interrupted, whichever happens first. 
   * 
   * This will also terminate all idle threads as soon
   * as all runnables are ready.
   */
  public void awaitTermination() {
    long sleepTime = 0;
    // Avoid having zero active threads just because they
    // aren't started.
    pool.prestartAllCoreThreads();
    
    while ( (pool.getActiveCount()>0) && !pool.isTerminated()) {
      // Increase sleep timer in a way, that processor performance is
      // not used to simply checking the active count over and over.
      try {Thread.sleep(sleepTime);} catch (InterruptedException e) {
        pool.shutdownNow();
        break;
      }
      if (sleepTime<1000) sleepTime += 10; // Sleep a second at max.
      //else System.out.println(pool.getTaskCount());
    }
    
    // Terminate all idle threads.
    shutdown();
  }
  
  /**
   * This will
   * a) terminate all idle threads
   * b) still execute all submitted runnables, but not
   * accept new runnables.
   */
  public void shutdown() {
    pool.shutdown();
  }
  
  /**
   * Attempts to stop all actively executing tasks, halts the
   * processing of waiting tasks, and returns a list of the
   * tasks that were awaiting execution.
   * This is the same as shutdownNow().
   * @return list of tasks that never commenced execution 
   */
  public List<Runnable> interrupt() {
    return pool.shutdownNow();
  }
  
  /**
   * @return true if and only if there is no active thread.
   */
  public boolean isAllDone() {
    return pool.getActiveCount()<=0;
  }
  
  /**
   * Add a task to the pool and execute it immedeately if
   * there is a free slot.
   * @param r - runnable to submit.
   */
  public void addToPool(Runnable r) {
    pool.execute(r);
  }
  
  /**
   * Just for samples and demos.
   */
  public static void main(String[] args) {
    // The lock object is used for synchronizing the different tasks.
    Object lock = new Object();
    
    // Initialize a new ThreadManager.
    // For the demo to make sense, we need at least two
    // slots that are used for parallel execution.
    int numberOfSlots = Math.max((ThreadManager.NUMBER_OF_PROCESSORS-1),2);
    ThreadManager m = new ThreadManager(numberOfSlots);
    System.out.println("Initialized a new thread manager with " + 
        m.getNumberOfSplots() + " slots to execute in parallel.");
    
    // Add a few jobs to execute
    for (int i=0; i<(numberOfSlots*5); i++) {
      Runnable r = getNextDemoJob(i, lock);
      m.addToPool(r);
    }
    
    synchronized (lock) {
      System.out.println("Currently " + m.getPoolSize() + 
          " jobs in ThreadManager. Now awaiting termination.");
    }
    
    // Wait until all calculations finished or interrupted.
    m.awaitTermination();
    
    // No need for synchronization here.
    System.out.println("All done.");
  }
  
  private static Runnable getNextDemoJob(final int x, final Object lock) {
    return new Runnable() {
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          System.out.println(x + " has been interupted.");
        }
        synchronized(lock) {
          // Using the same lock object for all threads will
          // block threads from accessing this part of the
          // core asynchronously.
          System.out.println("Number " + x + " slept long enough.");
        }
      }      
    };
  }
  
  
  
}
