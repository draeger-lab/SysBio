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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

/**
 * This class loads other classes that implement certain interfaces or extend
 * certain super types. With this method it becomes possible to load and
 * initialize instances of certain classes at runtime.
 * 
 * @author Marcel Kronfeld
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2009-09-22
 * @since 1.0 (originates from SBMLsqueezer version 1.3 and EvA2)
 * @version $Rev$
 */
public class Reflect {
	
	/**
	 * The {@link Logger}.
	 */
	public static Logger logger = Logger.getLogger(Reflect.class.getName());
	
	/**
	 * Compares the string values of given objects.
	 * 
	 * @author draeger
	 * @param <T>
	 */
	static class ClassComparator<T> implements Comparator<T> {
		/**
		 * 
		 */
		public int compare(Object o1, Object o2) {
			return (o1.toString().compareTo(o2.toString()));
		}
	}
	
	/**
	 * 
	 */
	private static String[] dynCP = null;
	
	/**
	 * 
	 */
	static int missedJarsOnClassPath = 0;
	
	/**
	 * 
	 */
	private static boolean TRACE;
	
	/**
	 * 
	 */
	private static boolean useFilteredClassPath;
	
	/**
	 * 
	 */
	private static final String ERROR_MSG = String.format(
		"%s: Could not retrieve Class from jar for", Reflect.class.getName());
	
	/**
	 * @param <T>
	 * @param set
	 * @param cls
	 * @return
	 */
	private static <T> int addClass(HashSet<Class<T>> set, Class<T> cls) {
		if (TRACE) {
			logger.info("adding class " + cls.getName());
		}
		if (set.contains(cls)) {
			// logger.warn(String.format("warning, Class %s not added twice!", cls
			// .getName()));
			return 0;
		} else {
			set.add(cls);
			return 1;
		}
	}
	
	/**
	 * Collect all classes from a given package on the classpath. If includeSubs
	 * is true, the sub-packages are listed as well.
	 * 
	 * @param <T>
	 * @param pckg
	 * @param includeSubs
	 * @param bSort
	 *        sort alphanumerically by class name
	 * @return An ArrayList of Class objects contained in the package which may be
	 *         empty if an error occurs.
	 */
	public static <T> Class<T>[] getAllClassesInPackage(String pckg,
		boolean includeSubs, boolean bSort) {
		return getClassesInPackageFltr(new HashSet<Class<T>>(), pckg, includeSubs,
			bSort, null);
	}
	
	/**
	 * @param <T>
	 * @param pckg
	 * @param includeSubs
	 * @param bSort
	 * @param superClass
	 * @return
	 */
	public static <T> Class<T>[] getAllClassesInPackage(String pckg,
		boolean includeSubs, boolean bSort, Class<T> superClass) {
		return getClassesInPackageFltr(new HashSet<Class<T>>(), pckg, includeSubs,
			bSort, superClass);
	}
	
	/**
	 * Determines all classes in the given package (and probably subpackages) that
	 * inherit from the given superclass. In case of this beeing included into a
	 * jar file, the path to the jar might be important and can therefore be given
	 * as an additional argument.
	 * 
	 * @param <T>
	 * @param packageName
	 *        The name of the package of interest.
	 * @param includeSubs
	 *        If true, subpackages are also queried.
	 * @param bSort
	 *        If true, the classes will be sorted with respect to their name.
	 * @param superClass
	 *        The super class whose descendants are to be investigated.
	 * @param jarPath
	 *        Path to the jar file where the package is located.
	 * @return
	 */
	public static <T> Class<T>[] getAllClassesInPackage(String packageName,
		boolean includeSubs, boolean bSort, Class<T> superClass, String jarPath) {
		Class<T> classes[] = Reflect.getAllClassesInPackage(packageName, false,
			true, superClass);
		if ((classes == null) || (classes.length == 0)) {
			HashSet<Class<T>> set = new HashSet<Class<T>>();
			boolean tryDir = true;
			if (tryDir && jarPath!=null) {
				File f = new File(jarPath);
				if (f.isDirectory()) {
					String[] pathElements = f.list();
					for (String entry : pathElements) {
					  // Only works with jar files. Check file extension before calling it?
						Reflect.getClassesFromJarFltr(set, jarPath + entry, packageName,
							true, superClass);
					}
				}
			}
			classes = Reflect.hashSetToClassArray(set, true);
		}
		return classes;
	}
	
	/**
	 * @param <T>
	 * @param packageName
	 * @param includeSubs
	 * @param bSort
	 * @param superClass
	 * @param jarPath
	 * @param excludeAbstractClasses
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T>[] getAllClassesInPackage(String packageName,
		boolean includeSubs, boolean bSort, Class<T> superClass, String jarPath,
		boolean excludeAbstractClasses) {
		Class<T> classes[] = getAllClassesInPackage(packageName, includeSubs,
			bSort, superClass, jarPath);
		if (excludeAbstractClasses) {
			List<Class<T>> impl = new LinkedList<Class<T>>();
			for (Class<T> c : classes) {
				if (!Modifier.isAbstract(c.getModifiers())) {
					impl.add(c);
				}
			}
			return (Class<T>[]) impl.toArray(new Class<?>[0]);
		}
		return classes;
	}
	
	/**
	 * Retrieve assignable classes of the given package from classpath.
	 * 
	 * @param <T>
	 * @param pckg
	 *        String denoting the package
	 * @param reqSuperCls
	 * @return
	 */
	public static <T> Class<T>[] getAssignableClassesInPackage(String pckg,
		Class<T> reqSuperCls, boolean includeSubs, boolean bSort) {
		if (TRACE){
			logger.info("requesting classes assignable from "
					+ reqSuperCls.getName());
		}
		return getClassesInPackageFltr(new HashSet<Class<T>>(), pckg, includeSubs,
			bSort, reqSuperCls);
	}
	
	/**
	 * Return the names of all classes in the same package that are assignable
	 * from the named class, and that can be loaded through the classpath. If a
	 * class has a declared field called "hideFromGOE" this method will skip it.
	 * Abstract classes and interfaces will be skipped as well.
	 * 
	 * @see ReflectPackage.getAssignableClassesInPackage
	 * @param className
	 * @return
	 */
	public static ArrayList<String> getClassesFromClassPath(String className) {
		ArrayList<String> classes = new ArrayList<String>();
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex <= 0) {
			logger.warning(String.format("warning: %s is not a package!", className));
		} else {
			String pckg = className.substring(0, className.lastIndexOf('.'));
			Class<?>[] clsArr;
			try {
				clsArr = getAssignableClassesInPackage(pckg, Class.forName(className),
					true, true);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				clsArr = null;
			}
			if (clsArr == null) {
				logger.warning(String.format(
					"Warning: No configuration property found for %s.", className));
				classes.add(className);
			} else {
				for (Class<?> class1 : clsArr) {
					int m = class1.getModifiers();
					try {
						// a field allowing a class to indicate it doesn't want
						// to be displayed
						Field f = class1.getDeclaredField("hideFromGOE");
						if (f.getBoolean(class1) == true) {
							if (TRACE) {
								logger.info("Class " + class1
										+ " wants to be hidden from GOE, skipping...");
							}
							continue;
						}
					} catch (Exception e) {
						// 
					} catch (Error e) {
						logger.warning(String.format(
							"Error on checking fields of %s: %s", class1, e));
						continue;
					}
					// if (f)
					if (!Modifier.isAbstract(m) && !class1.isInterface()) {
						/*
						 * don't take abstract classes or interfaces
						 */
						try {
							Class<?>[] params = new Class[0];
							class1.getConstructor(params);
							classes.add(class1.getName());
						} catch (NoSuchMethodException e) {
							logger.warning(String.format(
												"GOE warning: Class %s has no default constructor, skipping...",
												class1.getName()));
						}
					}
				}
			}
		}
		return classes;
	}
	
	/**
	 * @param <T>
	 * @param set
	 * @param directory
	 * @param pckgname
	 * @param includeSubs
	 * @param reqSuperCls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> int getClassesFromDirFltr(HashSet<Class<T>> set,
		File directory, String pckgname, boolean includeSubs, Class<T> reqSuperCls) {
		int cntAdded = 0;
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					try {
						Class<T> cls = null;
						String className = StringUtil.concat(pckgname, ".",
								files[i].substring(0, files[i].length() - 6))
								.toString();
						if (!ClassLoader.getSystemClassLoader().loadClass(className).isInterface()) {
						  cls=(Class<T>) Class.forName(className);
						  if (reqSuperCls != null) {
							  if (reqSuperCls.isAssignableFrom(cls)) {
								  cntAdded += addClass(set, cls);
							  }
						  } else {
							  cntAdded += addClass(set, cls);
						  }
						}
					}
					catch (Exception e) {
						logger.warning(String.format("%s %s.%s: %s.", ERROR_MSG, pckgname.replace(
							'/', '.'), files[i], e.getLocalizedMessage()));
					} catch (Error e) {
						logger.warning(String.format("%s %s.%s: %s.", ERROR_MSG, pckgname.replace(
							'/', '.'), files[i], ": ", e.getLocalizedMessage()));
					}
				} else if (includeSubs) {
					// do a recursive search over subdirs
					File subDir = new File(directory.getAbsolutePath()
							+ File.separatorChar + files[i]);
					if (subDir.exists() && subDir.isDirectory()) {
						cntAdded += getClassesFromDirFltr(set, subDir, pckgname + "."
								+ files[i], includeSubs, reqSuperCls);
					}
				}
			}
		}
		return cntAdded;
	}
	
	/**
	 * Collect classes of a given package from the file system.
	 * 
	 * @param <T>
	 * @param pckgname
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static <T> int getClassesFromFilesFltr(HashSet<Class<T>> set,
		String path, String pckgname, boolean includeSubs, Class<T> reqSuperCls) {
		try {
			// Get a File object for the package
			File directory = null;
			String dir = null;
			try {
				ClassLoader cld = ClassLoader.getSystemClassLoader();
				if (cld == null) { throw new ClassNotFoundException("Can't get class loader."); }
				dir = path + "/" + pckgname.replace(".", "/");
				
				if (TRACE) {
					logger.warning(String.format(".. opening %s", path));
				}
				
				directory = new File(dir);
				
			} catch (NullPointerException x) {
				if (TRACE) {
					logger.warning(String.format("%s not found in %s.", directory.getPath(), path));
					logger.warning(String.format("directory %s.", (directory.exists() ? "exists"
							: "does nott exist")));
				}
				return 0;
			}
			if (directory.exists()) {
				// Get the list of the files contained in the package
				return getClassesFromDirFltr(set, directory, pckgname, includeSubs, reqSuperCls);
			} else {
				if (TRACE) {
					logger.warning(String.format("%s does not exist in %s, dir was %s.", directory
							.getPath(), path, dir));
				}
				return 0;
			}
		} catch (ClassNotFoundException e) {
			logger.warning(e.getLocalizedMessage());
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Collect classes of a given package from a jar file.
	 * 
	 * @param <T>
	 * @param jarName
	 * @param packageName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> int getClassesFromJarFltr(HashSet<Class<T>> set,
		String jarName, String packageName, boolean includeSubs,
		Class<T> reqSuperCls) {
		boolean isInSubPackage = true;
		int cntAdded = 0;
		
		packageName = packageName.replace('.', '/');
		if (TRACE) {
			logger.info(String.format("Jar %s looking for %s", jarName, packageName));
		}
		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
			JarEntry jarEntry;
			
			while ((jarEntry = jarFile.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();
				// if (TRACE) { logger.info("- " + jarEntry.getName()); }
				if ((jarEntryName.startsWith(packageName))
						&& (jarEntryName.endsWith(".class"))) {
					// subpackages are hit here as well!
					if (!includeSubs) { // check if the class belongs to a
						// subpackage
						int lastDash = jarEntryName.lastIndexOf('/');
						if (lastDash > packageName.length() + 1) {
							isInSubPackage = true;
						} else {isInSubPackage = false;}
					}
					if (includeSubs || !isInSubPackage) { // take the right
						// ones
						String clsName = jarEntryName.replace("/", ".");
						try {
							// removes the .class extension
							Class<T> cls = (Class<T>) Class.forName(clsName.substring(0,
								jarEntryName.length() - 6));
							if (reqSuperCls != null) {
								if (reqSuperCls.isAssignableFrom(cls)) {
									cntAdded += addClass(set, cls);
								}
							} else cntAdded += addClass(set, cls);
						} catch (Exception e) {
							logger.warning(String.format("%s %s: %s", ERROR_MSG, clsName, e
									.getMessage()));
							//							e.printStackTrace();
						} catch (Error e) {
							logger.warning(String.format("%s %s: %s", ERROR_MSG, clsName, e
									.getMessage()));
							// e.printStackTrace();
						}
					}
					
					// classes.add (jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		} catch (IOException e) {
			missedJarsOnClassPath++;
			if (missedJarsOnClassPath <2) {
				logger.warning(String.format(
					"Could not open jar from class path: %s. - Dirty class path?", e
							.getLocalizedMessage()));
			} else if (missedJarsOnClassPath == 2) {
				logger.warning("Could not open jar from class path more than once...");
			}
			// e.printStackTrace();
		}
		return cntAdded;
	}
	
	/**
	 * Read the classes available for user selection from the properties or the
	 * classpath respectively
	 */
	public static ArrayList<String> getClassesFromProperties(String className) {
		if (TRACE) {
			logger.info(String.format(
				"getClassesFromProperties - requesting className: %s", className));
		}
		return getClassesFromClassPath(className);
	}

  
	/**
	 * Collect classes from a given package on the classpath which have the given
	 * Class as superclass or superinterface. If includeSubs is true, the
	 * sub-packages are listed as well.
	 * 
	 * <p>NOTE (wrzodek,2011): This method does not work in java web-start
	 * applications, because no (valid) class path is defined here and it is not
	 * possible to get the actual location of the currently running jar file.</p> 
	 * 
	 * @param <T>
	 * @see Class.assignableFromClass(Class cls)
	 * @param pckg
	 * @return
	 */
	public static <T> Class<T>[] getClassesInPackageFltr(HashSet<Class<T>> set,
		String pckg, boolean includeSubs, boolean bSort, Class<T> reqSuperCls) {
	  String classPath = null;
	  
	   if (!useFilteredClassPath || (dynCP == null)) {
	      classPath = System.getProperty("java.class.path", ".");
	      if (useFilteredClassPath) {
	        try {
	          dynCP = getValidCPArray();
	        } catch (Exception e) {
	          logger.warning(e.getLocalizedMessage());
	        }
	      } else {
	        dynCP = getClassPathElements();
	      }
	    }
	   
	  /*
	   * Alternative Method to marcel's one (by Wrzodek). This is much faster, because the
	   * current classLoader is used, instead of using the system-class path. But not already
	   * loaded classes are missing (which might be a good/wanted effect or not)!
	   */
	  /*
	  List<String> dirs = new ArrayList<String>();
		try {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    String path = pckg.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    path='/'+path;
    while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        String fileName = resource.getFile();
        String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
        
        // E.g., http://www.cogsys.cs.uni-tuebingen.de/software/KEGGtranslator/downloads/KEGGtranslator.jar!/de/zbit/gui/prefs
        // - Remove the package extension from the string and remove eventual "!"'s and "file:/" prefixes. 
         
        if (fileNameDecoded.endsWith(path)) {
          fileNameDecoded = fileNameDecoded.substring(0, fileNameDecoded.length()-path.length());
        }
        if (fileNameDecoded.endsWith(".jar!")) {
          fileNameDecoded = fileNameDecoded.substring(0, fileNameDecoded.length()-1);
        }
        if (fileNameDecoded.startsWith("file://")) {
          fileNameDecoded = fileNameDecoded.substring(7);
        }
        if (fileNameDecoded.startsWith("file:/")) {
          fileNameDecoded = fileNameDecoded.substring(6);
        }
        
        logger.info(fileNameDecoded);
        
        dirs.add((fileNameDecoded));
    }
		} catch (Exception e) {
		  e.printStackTrace();
		}
		dynCP = dirs.toArray(new String[0]);

		 * 
		 */
		
		
		if (TRACE) {
			logger.info(String.format("classpath is %s", classPath));
		}
		for (int i = 0; i < dynCP.length; i++) {
			if (TRACE) {
				logger.info("reading element " + dynCP[i]);
			}
			if (dynCP[i].endsWith(".jar")) {
				getClassesFromJarFltr(set, dynCP[i], pckg, includeSubs, reqSuperCls);
			} else {
				if (TRACE) {
					logger.info(String.format("reading from files: %s %s", dynCP[i], pckg));
				}
				getClassesFromFilesFltr(set, dynCP[i], pckg, includeSubs, reqSuperCls);
			}
		}
		return hashSetToClassArray(set, bSort);
	}
	
	/**
	 * @return
	 */
	public static String[] getClassPathElements() {
		String classPath = System.getProperty("java.class.path", ".");
		// logger.info("classpath: " + classPath);
		return classPath.split(File.pathSeparator);
	}
	
	/**
	 * @return
	 */
	public static String[] getValidCPArray() {
		ArrayList<String> valids = getValidCPEntries(getClassPathElements());
		// vp = valids.toArray(dynCP); // this causes Matlab to crash meanly.
		String[] vp = new String[valids.size()];
		for (int i = 0; i < valids.size(); i++) {
			vp[i] = valids.get(i);
		}
		return vp;
	}
	
	/**
	 * @return
	 */
	public static ArrayList<String> getValidCPEntries(String[] pathElements) {
		// String[] pathElements = getClassPathElements();
		File f;
		ArrayList<String> valids = new ArrayList<String>(pathElements.length);
		for (int i = 0; i < pathElements.length; i++) {
			// logger.warning(pathElements[i]);
			f = new File(pathElements[i]);
			// if (f.canRead()) {valids.add(pathElements[i]);}
			if (f.exists() && f.canRead()) {
				valids.add(pathElements[i]);
			}
		}
		return valids;
	}
	
	/**
	 * @param <T>
	 * @param set
	 * @param bSort
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T>[] hashSetToClassArray(HashSet<Class<T>> set,
		boolean bSort) {
		Object[] clsArr = set.toArray();
		if (bSort) {
			Arrays.sort(clsArr, new ClassComparator<Object>());
		}
		
		List<Object> list;
		list = Arrays.asList(clsArr);
		return (Class<T>[]) list.toArray(new Class<?>[list.size()]);
	}
	
	
	/**
	 * Returns true if and only if the given class
	 * contains a String-parser (e.g. Boolean.parseBoolean()
	 * or Float.parseFloat()).
	 * @param clazz
	 * @return
	 */
	public static boolean containsParser(Class<?> clazz) {
		return getStringParser(clazz)!=null;
	}
	
	/**
	 * Returns the parse-Method for the given class
	 * (e.g. Boolean.parseBoolean())
	 * @param clazz
	 * @return
	 */
	public static Method getStringParser(Class<?> clazz) {
		String searchFor = "parse" + clazz.getSimpleName();
		if (searchFor.equals("parseInteger")) searchFor = "parseInt";
		try {
			return clazz.getMethod(searchFor, String.class);
		} catch (Exception e) {
			// NoSuchMethodException, SecurityException
			try {
				// Decode does the same as the "parseX" methods.
				return clazz.getMethod("decode", String.class);
			} catch (Exception e2) {}
      return null;
		}
	}
	
	/**
	 * Invokes the parse-Method of clazz on the given Object.
	 * @param clazz
	 * @param toParse
	 * @return
	 * @throws Throwable 
	 */
	public static Object invokeParser(Class<?> clazz, Object toParse) throws Throwable {
		Method m = getStringParser(clazz);
		if (m==null) return null;
		
		try {
			return m.invoke(clazz, toParse);
			
		} catch (Exception e) {
		  // IllegalArgumentException, IllegalAccessException, InvocationTargetException
			if (e.getCause()!=null && 
			    e.getCause() instanceof java.lang.NumberFormatException) {
			  // E.g. if Integer.parseInt("Hallo") is Called, thow the NumberFormatException.
			  throw e.getCause();
			} else {
			  throw e;
			}
		}
		
		//return null;
	}
	
	/**
	 * Checks, if a class contains a certain Method. If it does, this
	 * Method is executed and the return value is returned. If the
	 * Method does not exists, or an Exception occurs, null is returned.
	 * @param clazz - Class instance to check for methods.
	 * @param methodName - Name of the Method to invoke.
	 * @param parameterTypes - ParameterTypes of the Method to invoke.
	 * @param parameters - Parameters to use, when invoking the method.
	 * @return null if something failed, or the return value of the
	 * invoked Method instead.
	 */
	public static Object invokeIfContains(Object clazz, String methodName,
		Class<?>[] parameterTypes, Object[] parameters) {
		
		try {
			Method m = clazz.getClass().getMethod(methodName, parameterTypes);
			return m.invoke(clazz, parameters);
		} catch (Exception e) {
			// Mostly java.lang.NoSuchMethodException
      return null;
		}
	}
	
	/**
	 * Checks, if a class contains a certain Method. If it does, this
	 * Method is executed and the return value is returned. If the
	 * Method does not exists, or an Exception occurs, null is returned.
	 * @param clazz - Class instance to check for methods.
	 * @param methodName - Name of the Method to invoke.
	 * @param parameters - Parameters to use, when invoking the method.
	 * @return null if something failed, or the return value of the
	 * invoked Method instead.
	 */
	public static Object invokeIfContains(Object clazz, String methodName,
		Object[] parameters) {
		Method[] ms;
		if (clazz instanceof Class<?>) {
			ms = ((Class<?>)clazz).getMethods();
		} else {
			ms = clazz.getClass().getMethods();
		}
		for (Method m:ms) {
			if (m.getName().equals(methodName)) {
				try {
					return m.invoke(clazz, parameters);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	 /**
   * Checks, if a class contains a certain Method. If it does, this
   * Method is executed and the return value is returned. If the
   * Method does not exists, or an Exception occurs, null is returned.
   * @param clazz - Class instance to check for methods.
   * @param methodName - Name of the Method to invoke.
   * @return null if something failed, or the return value of the
   * invoked Method instead.
   */
  public static Object invokeIfContains(Object clazz, String methodName) {
    
    try {
      Method m = clazz.getClass().getMethod(methodName);
      return m.invoke(clazz);
    } catch (Exception e) {
      // Mostly java.lang.NoSuchMethodException
      return null;
    }
  }
	
	/**
	 * Checks whether a method with this name and the given parameter types exists
	 * for the given {@link Object} clazz.
	 * 
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 */
	public static boolean contains(Object clazz, String methodName,
		Class<?>... parameterTypes) {
		try {
			Method m = getMethod(clazz, methodName, parameterTypes);
			return m != null;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 * @throws NoSuchMethodException
	 */
  public static Method getMethod(Object clazz, String methodName,
    Class<?>... parameterTypes) throws NoSuchMethodException {
    Method m = clazz.getClass().getMethod(methodName, parameterTypes);
    return m;
  }

	/**
	 * Checks if a certain method exists and invokes it.
	 * Just a wrapper for
	 * {@link #invokeIfContains(Class, String, Class[], Object[])}.
	 * @param clazz
	 * @param methodName
	 * @param parameterType
	 * @param parameter
	 * @return value of the executed method
	 */
	public static Object invokeIfContains(Object clazz, String methodName,
		Class<?> parameterType, Object parameter) {
		return invokeIfContains(clazz, methodName, new Class<?>[]{parameterType},
			new Object[]{parameter});
	}
	
	/**
	 * 
	 * 
	 */
	public Reflect() {
		TRACE = true;
	}

  /**
   * @return the name of the parent class of the current calling classe.
   * i.e. the class the called the method, that is calling this method.
   * Returns null if no parent is available or an error occurs.
   */
  public static Class<?> getParentClass() {
    // Get the list of classes from the stackTrace
    final Throwable t = new Throwable();
    StackTraceElement[] se = t.getStackTrace();
    
    /*return Class.forName(se[2].getClassName());
     *is wrong because multipleMethids from class[1] might be in the stack trace.*/ 
    
    int diffClassName=0;
    String lastClassName="";
    for (StackTraceElement s: se) {
      if (!(s.getClassName().equals(lastClassName))) {
        diffClassName++;
        lastClassName = s.getClassName();
      }
      // 1=this(Reflect), 2=the Caller, 3=the parent of the caller
      if (diffClassName==3) try {
        return Class.forName(s.getClassName());
      } catch (ClassNotFoundException e) {
        // Not possible, because class is in StackTrace
        e.printStackTrace();
        return null;
      }
    }
    
    return null;
  }

  /**
   * Returns the name of the main Class that has been used to execute this application.
   * More detailed, the last main class in the current stack trace.
   * 
   * <p>This method does NOT work in Java Web Start applications!</p>
   * @return
   */
  public static Class<?> getMainClass() {
    Class<?> lastMain = null;
    // Get the main class from the stackTrace
    final Throwable t = new Throwable();
    for (StackTraceElement e : t.getStackTrace()) {
      // Search the main class
      if (e.getMethodName().equalsIgnoreCase("main")) {
        // Get it's name
        try {
          lastMain = Class.forName(e.getClassName());
          //break;
        } catch (ClassNotFoundException e1) {
          // Not possible, because class is in StackTrace
        }
      }
    }
    return lastMain;
  }

}
