package de.zbit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * This class loads other classes that implement certain interfaces or extend
 * certain super types. With this method it becomes possible to load and
 * initialize instances of certain classes at runtime.
 * 
 * @author Marcel Kronfeld
 * @author Andreas Dr&auml;ger
 * @date 2009-09-22
 * @since This class was part of SBMLsqueezer version 1.3.
 */
public class Reflect {
	
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
		if (TRACE) System.out.println("adding class " + cls.getName());
		if (set.contains(cls)) {
			// System.err.printf("warning, Class %s not added twice!\n", cls
			// .getName());
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
		if (classes == null || classes.length == 0) {
			HashSet<Class<T>> set = new HashSet<Class<T>>();
			boolean tryDir = true;
			if (tryDir) {
				File f = new File(jarPath);
				if (f.isDirectory()) {
					String[] pathElements = f.list();
					for (String entry : pathElements) {
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
		if (TRACE)
			System.out.println("requesting classes assignable from "
					+ reqSuperCls.getName());
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
			System.err.printf("warning: %s is not a package!\n", className);
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
				System.err.printf("Warning: No configuration property found for %s.",
					className);
				classes.add(className);
			} else {
				for (Class<?> class1 : clsArr) {
					int m = class1.getModifiers();
					try {
						// a field allowing a class to indicate it doesn't want
						// to be displayed
						Field f = class1.getDeclaredField("hideFromGOE");
						if (f.getBoolean(class1) == true) {
							if (TRACE)
								System.out.println("Class " + class1
										+ " wants to be hidden from GOE, skipping...");
							continue;
						}
					} catch (Exception e) {
						
					} catch (Error e) {
						System.err
								.printf("Error on checking fields of %s: %s\n", class1, e);
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
							System.err.println("GOE warning: Class " + class1.getName()
									+ " has no default constructor, skipping...");
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
						Class<T> cls = (Class<T>) Class.forName(pckgname + '.'
								+ files[i].substring(0, files[i].length() - 6));
						if (reqSuperCls != null) {
							if (reqSuperCls.isAssignableFrom(cls)) {
								cntAdded += addClass(set, cls);
							}
						} else {
							cntAdded += addClass(set, cls);
						}
					} catch (Exception e) {
						System.err.printf("%s %s.%s: %s.\n", ERROR_MSG, pckgname.replace(
							'/', '.'), files[i], e.getMessage());
						//						e.printStackTrace();
					} catch (Error e) {
						System.err.printf("%s %s.%s: %s.\n", ERROR_MSG, pckgname.replace(
							'/', '.'), files[i], ": ", e.getMessage());
						//						e.printStackTrace();
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
				if (cld == null) { throw new ClassNotFoundException(
					"Can't get class loader."); }
				dir = path + "/" + pckgname.replace(".", "/");
				
				if (TRACE) {
					System.out.println(".. opening " + path);
				}
				
				directory = new File(dir);
				
			} catch (NullPointerException x) {
				if (TRACE) {
					System.err.printf("%s not found in %s.\n", directory.getPath(), path);
					System.err.printf("directory %s.\n", (directory.exists() ? "exists"
							: "does nott exist"));
				}
				return 0;
			}
			if (directory.exists()) {
				// Get the list of the files contained in the package
				return getClassesFromDirFltr(set, directory, pckgname, includeSubs,
					reqSuperCls);
			} else {
				if (TRACE) {
					System.err.printf("%s does not exist in %s, dir was %s.\n", directory
							.getPath(), path, dir);
				}
				return 0;
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
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
		
		packageName = packageName.replaceAll("\\.", "/");
		if (TRACE)
			System.out.printf("Jar %s looking for %s\n", jarName, packageName);
		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
			JarEntry jarEntry;
			
			while ((jarEntry = jarFile.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();
				// if (TRACE) System.out.println("- " + jarEntry.getName());
				if ((jarEntryName.startsWith(packageName))
						&& (jarEntryName.endsWith(".class"))) {
					// subpackages are hit here as well!
					if (!includeSubs) { // check if the class belongs to a
						// subpackage
						int lastDash = jarEntryName.lastIndexOf('/');
						if (lastDash > packageName.length() + 1)
							isInSubPackage = true;
						else isInSubPackage = false;
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
							System.err.printf("%s %s: %s\n", ERROR_MSG, clsName, e
									.getMessage());
							//							e.printStackTrace();
						} catch (Error e) {
							System.err.printf("%s %s: %s\n", ERROR_MSG, clsName, e
									.getMessage());
							// e.printStackTrace();
						}
					}
					
					// classes.add (jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		} catch (IOException e) {
			missedJarsOnClassPath++;
			if (missedJarsOnClassPath == 0) {
				System.err.printf("Could not open jar from class path: %s.\n", e
						.getMessage());
				System.err.println("Dirty class path?");
			} else if (missedJarsOnClassPath == 2) {
				System.err
						.println("Could not open jar from class path more than once...");
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
			System.out.printf(
				"getClassesFromProperties - requesting className: %s\n", className);
		}
		return getClassesFromClassPath(className);
	}
	
	/**
	 * Collect classes from a given package on the classpath which have the given
	 * Class as superclass or superinterface. If includeSubs is true, the
	 * sub-packages are listed as well.
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
					System.err.println(e.getMessage());
				}
			} else {
				dynCP = getClassPathElements();
			}
		}
		
		if (TRACE) {
			System.out.println("classpath is " + classPath);
		}
		for (int i = 0; i < dynCP.length; i++) {
			if (TRACE) {
				System.out.println("reading element " + dynCP[i]);
			}
			if (dynCP[i].endsWith(".jar")) {
				getClassesFromJarFltr(set, dynCP[i], pckg, includeSubs, reqSuperCls);
			} else {
				if (TRACE) {
					System.out.printf("reading from files: %s %s\n", dynCP[i], pckg);
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
		// System.out.println("classpath: " + classPath);
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
			// System.err.println(pathElements[i]);
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
	 * 
	 * 
	 */
	public Reflect() {
		TRACE = true;
	}
}
