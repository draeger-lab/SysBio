/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.garuda;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jp.sbi.garuda.platform.commons.FileFormat;
import jp.sbi.garuda.platform.commons.Software;
import jp.sbi.garuda.platform.commons.exception.NetworkException;
import jp.sbi.garuda.platform.event.PlatformEvent;
import jp.sbi.garuda.platform.event.PlatformEventListener;
import jp.sbi.garuda.platform.event.PlatformEventManager;
import jp.sbi.garuda.platform.event.PlatformEventType;
import jp.sbi.garuda.platform.software.CoreClientAPI;
import de.zbit.UserInterface;
import de.zbit.gui.GUITools;
import de.zbit.io.FileTools;
import de.zbit.util.ResourceManager;

/**
 * <p>
 * An easily usable backend for Garuda support.
 * <p>
 * Here follows a minimal example of how to use it to Garuda-enable any
 * software. When initializing your graphical user interface, which must be
 * derived from {@link UserInterface}, just insert the following piece of code:
 * 
 * <pre>
 * new Thread(new Runnable() {
 * 	public void run() {
 * 		try {
 * 			GarudaSoftwareBackend garudaBackend = new GarudaSoftwareBackend(
 * 				(UserInterface) gui);
 * 			garudaBackend.addInputFileFormat(&quot;xml&quot;, &quot;SBML&quot;);
 * 			// ... as many additional input file formats as supported
 * 			garudaBackend.addOutputFileFormat(&quot;xml&quot;, &quot;SBML&quot;);
 * 			// ... as many additional output file formats as supported
 * 			garudaBackend.init();
 * 			garudaBackend.registedSoftwareToGaruda();
 * 		} catch (NetworkException exc) {
 * 			GUITools.showErrorMessage(gui, exc);
 * 		} catch (BackendNotInitializedException exc) {
 * 			GUITools.showErrorMessage(gui, exc);
 * 		} catch (Throwable exc) {
 * 			logger.fine(exc.getLocalizedMessage());
 * 		}
 * 	}
 * }).start();
 * </pre>
 * 
 * In the above code, it is assumed that {@code gui} is some instance of a AWT
 * or SWING element.
 * <p>
 * As any instance of {@link UserInterface}, also your one must extend
 * {@link PropertyChangeListener}. In order to successfully enable Garuda in
 * your application, do the following in your gui:
 * 
 * <pre>
 * public void propertyChange(PropertyChangeEvent evt) {
 * 	String propName = evt.getPropertyName();
 * 	if (propName.equals(GarudaSoftwareBackend.GARUDA_ACTIVATED)) {
 * 		this.garudaBackend = (GarudaSoftwareBackend) evt.getNewValue();
 * 		if (supportedFileOpened) {
 * 			enableGarudaInMenuBar();
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * In this method, the instance of the {@link GarudaSoftwareBackend} can be
 * stored in the GUI as a controller for Garuda.
 * <p>
 * In order to sent a file to other Garuda-enabled tools, you will have to add
 * some buttons or menu items to your application. The action to sent a file can
 * be performed by using the {@link GarudaFileSender}.
 * <p>
 * For localization support you can find an XML file containg several useful
 * entries in the garuda resource folder.
 * 
 * @author Andreas Dr&auml;ger
 * @date 17:42:09
 * @since 1.1
 * @version $Rev$
 */
public class GarudaSoftwareBackend {

	public static final String CONNECTION_NOT_INITIALIZED_ID = "de.zbit.garuda.GarudaSoftwareBackend.connectionNotInitialized";
	public static final String CONNECTION_TERMINATED_ID = "de.zbit.garuda.GarudaSoftwareBackend.connectionTerminated";
	public static final String GOT_ERRORS_PROPERTY_CHANGE_ID = "de.zbit.garuda.GarudaSoftwareBackend.gotErrors";
	public static final String GOT_SOFTWARES_PROPERTY_CHANGE_ID = "de.zbit.garuda.GarudaSoftwareBackend.gotSoftwares";
	public static final String LOAD_FILE_PROPERTY_CHANGE_ID = "de.zbit.garuda.GarudaSoftwareBackend.loadFile";
	public static final String LOAD_GADGET_PROPERTY_CHANGE_ID = "de.zbit.garuda.GarudaSoftwareBackend.loadGadget";
	public static final String SOFTWARE_DEREGISTRATION_ERROR_ID = "de.zbit.garuda.GarudaSoftwareBackend.softwareDeregistrationError";
	public static final String SOFTWARE_REGISTRATION_ERROR_ID = "de.zbit.garuda.GarudaSoftwareBackend.softwareRegistrationError";
	public static final String GARUDA_ACTIVATED = "de.zbit.garuda.GarudaSoftwareBackend.garudaActivated";

	public static final String SOFTWARE_DEREGISTERED_ERROR = "Protocol Error: SoftwareNotRegistered";
	public static final String SOFTWARE_REGISTERED_ERROR = "Protocol Error: SoftwareRegistered";

	private static final int GARUDA_CORE_PORT = 9000;
  private static final int GARUDA_CORE_TIMEOUT = 1000;

	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(GarudaSoftwareBackend.class.getName());
	
	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
	
	/**
	 * 
	 */
	private boolean initialized;

	/**
	 * 
	 */
	private List<Software> listOfCompatibleSoftware;
	/**
	 * 
	 */
	private List<FileFormat> listOfOutputFileFormats, listOfInputFileFormats;
	
	/**
	 * 
	 */
	private UserInterface parent;

	/**
	 * 
	 */
	private PropertyChangeSupport pcs;
	/**
	 * 
	 */
	private String softwareID;
	
	/**
	 * 
	 */
	private GarudaSoftwareListener softwareListener;

	/**
	 * 
	 * @param parent
	 */
	public GarudaSoftwareBackend(UserInterface parent) {
		super();
		this.softwareID = parent.getApplicationName(); 
		this.listOfCompatibleSoftware = null;
		this.parent = parent;
		this.pcs = new PropertyChangeSupport(parent);
		this.listOfOutputFileFormats = new LinkedList<FileFormat>();
		this.listOfInputFileFormats = new LinkedList<FileFormat>();
    this.softwareListener = new GarudaSoftwareListener(this);
    addPropertyChangeListenerForAllProperties(this.softwareListener);
    addPropertyChangeListenerForAllProperties(this.parent);
	}
	
	/**
	 * 
	 * @param ff
	 */
	public void addInputFileFormat(FileFormat ff) {
		listOfInputFileFormats.add(ff);
	}

	/**
	 * 
	 * @param fileExtension
	 * @param fileFormat
	 */
	public void addInputFileFormat(String fileExtension, String fileFormat) {
		addInputFileFormat(new FileFormat(fileExtension, fileFormat));
	}

	/**
	 * 
	 * @param ff
	 */
	public void addOutputFileFormat(FileFormat ff) {
		listOfOutputFileFormats.add(ff);
	}
	
	/**
	 * 
	 * @param fileExtension
	 * @param fileFormat
	 */
	public void addOutputFileFormat(String fileExtension, String fileFormat) {
		addOutputFileFormat(new FileFormat(fileExtension, fileFormat));
	}
	
	/**
	 * 
	 * @param pcl
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param pcl
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(propertyName, pcl);
	}
	
	/**
	 * 
	 * @param listener
	 */
	private void addPropertyChangeListenerForAllProperties(PropertyChangeListener listener) {
		addPropertyChangeListener(LOAD_FILE_PROPERTY_CHANGE_ID, listener);
		addPropertyChangeListener(LOAD_GADGET_PROPERTY_CHANGE_ID, listener);
		addPropertyChangeListener(GOT_SOFTWARES_PROPERTY_CHANGE_ID, listener);
		addPropertyChangeListener(GOT_ERRORS_PROPERTY_CHANGE_ID, listener);
		addPropertyChangeListener(CONNECTION_TERMINATED_ID, listener);
		addPropertyChangeListener(CONNECTION_NOT_INITIALIZED_ID, listener);
		addPropertyChangeListener(SOFTWARE_REGISTRATION_ERROR_ID, listener);
		addPropertyChangeListener(SOFTWARE_DEREGISTRATION_ERROR_ID, listener);
		addPropertyChangeListener(GARUDA_ACTIVATED, listener);
	}
	
	/**
	 * 
	 * @throws NetworkException
	 * @throws BackendNotInitializedException
	 */
	public void deregisterSoftwareFromGaruda() throws NetworkException,
		BackendNotInitializedException {
		if (!initialized) {
			throw new BackendNotInitializedException();
		}
		CoreClientAPI.getInstance().deregisterSoftware();
		logger.fine(bundle.getString("SOFTWARE_DEREGISTERED"));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GarudaSoftwareBackend)) {
			return false;
		}
		GarudaSoftwareBackend other = (GarudaSoftwareBackend) obj;
		if (initialized != other.initialized) {
			return false;
		}
		if (listOfInputFileFormats == null) {
			if (other.listOfInputFileFormats != null) {
				return false;
			}
		} else if (!listOfInputFileFormats.equals(other.listOfInputFileFormats)) {
			return false;
		}
		if (listOfOutputFileFormats == null) {
			if (other.listOfOutputFileFormats != null) {
				return false;
			}
		} else if (!listOfOutputFileFormats.equals(other.listOfOutputFileFormats)) {
			return false;
		}
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		if (softwareID == null) {
			if (other.softwareID != null) {
				return false; 
			}
		} else if (!softwareID.equals(other.softwareID)) {
			return false; 
		}
		if (softwareListener == null) {
			if (other.softwareListener != null) {
				return false; 
			}
		} else if (!softwareListener.equals(other.softwareListener)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param propertyName
	 * @param newValue
	 */
	public void firePropertyChange(String propertyName, Object newValue) {
		firePropertyChange(propertyName, null, newValue);
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	public void firePropertyChange(String propertyName, Object oldValue,
		Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * @return the listOfCompatibleSoftware
	 */
	public List<Software> getListOfCompatibleSoftware() {
		return listOfCompatibleSoftware;
	}
	
	/**
	 * 
	 * @return
	 */
	public UserInterface getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (initialized ? 1231 : 1237);
		result = prime * result + ((listOfInputFileFormats == null) ? 0 : listOfInputFileFormats .hashCode());
		result = prime * result + ((listOfOutputFileFormats == null) ? 0 : listOfOutputFileFormats .hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((softwareID == null) ? 0 : softwareID.hashCode());
		result = prime * result + ((softwareListener == null) ? 0 : softwareListener.hashCode());
		return result;
	}

	/**
	 * 
	 * @throws NetworkException
	 */
	public void init() throws NetworkException {
		if (softwareListener == null) {
			throw new IllegalStateException(bundle.getString("GARUDA_LISTENER_NOT_INITIALIZED")); 
		}
		
		CoreClientAPI api = CoreClientAPI.getInstance();
		
		api.initialize(
			softwareID,
			parent.getDottedVersionNumber(),
			parent.getApplicationName(),
			softwareListener,
			GARUDA_CORE_PORT,
			GARUDA_CORE_TIMEOUT);
		logger.fine(bundle.getString("GARUDA_CORE_INITIALIZED"));
		
		api.start();
		
		logger.fine(bundle.getString("GARUDA_CORE_STARTED"));
		
		PlatformEventManager.getInstance().addListener(new PlatformEventListener() {
			/* (non-Javadoc)
			 * @see jp.sbi.garuda.platform.event.PlatformEventListener#accept(jp.sbi.garuda.platform.event.PlatformEventType)
			 */
			@Override
			public boolean accept(PlatformEventType type) { 
				return type == PlatformEventType.COMMAND_ERROR;
			}
			
			/* (non-Javadoc)
			 * @see jp.sbi.garuda.platform.event.PlatformEventListener#fire(jp.sbi.garuda.platform.event.PlatformEvent)
			 */
			@Override
			public void fire(PlatformEvent event) {
				String message = event.getMessage();
				logger.warning(message);
				if (!initialized) {
					firePropertyChange(CONNECTION_NOT_INITIALIZED_ID, null);
				} else {
					if (initialized) {
						if (SOFTWARE_REGISTERED_ERROR.equals(message)) {
							firePropertyChange(SOFTWARE_REGISTRATION_ERROR_ID, message);
						} else if (SOFTWARE_DEREGISTERED_ERROR.equals(message)) { 
							firePropertyChange(SOFTWARE_DEREGISTRATION_ERROR_ID, message); 
						} else {
							firePropertyChange(CONNECTION_TERMINATED_ID, null);
						}
					}
				}
			}
		});
		
		PlatformEventManager.getInstance().addListener(new PlatformEventListener() {
			/* (non-Javadoc)
			 * @see jp.sbi.garuda.platform.event.PlatformEventListener#accept(jp.sbi.garuda.platform.event.PlatformEventType)
			 */
			@Override
			public boolean accept(PlatformEventType type) { 
				return type == PlatformEventType.PROTOCOL_ERROR;
			}
			
			/* (non-Javadoc)
			 * @see jp.sbi.garuda.platform.event.PlatformEventListener#fire(jp.sbi.garuda.platform.event.PlatformEvent)
			 */
			@Override
			public void fire(PlatformEvent event) { 
				String message = event.getMessage();
				if (initialized) {
					firePropertyChange(GOT_ERRORS_PROPERTY_CHANGE_ID, message);
				}
				logger.warning(message);
			}
		});
		
		api.activateSoftware();
		logger.fine(bundle.getString("GARUDA_CORE_SOFTWARE_ACTIVATED"));
		firePropertyChange(GARUDA_ACTIVATED, this);
		
		initialized = true;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * 
	 * @param files
	 * @return
	 */
	public File[] openFile(File... files) {
		return parent.openFileAndLogHistory(files);
	}
	
	/**
	 * 
	 * @throws NetworkException
	 * @throws BackendNotInitializedException
	 */
	public void registedSoftwareToGaruda() throws NetworkException,
		BackendNotInitializedException {
		if (!initialized) {
			throw new BackendNotInitializedException();
		}
		File parentFolder = new File(System.getProperty("user.dir"));
		logger.fine(MessageFormat.format(bundle.getString("PATH_FOR_SOFTWARE"), parentFolder.getAbsolutePath()));
		//The path of the software that the core will use in order to launch it. Note that this 
		//is the command that someone would use in order to launch the software. In this case we are 
		//using the command to launch a jar file.
		//      String filePath = "javaw -jar " + parentFolder.getAbsolutePath().toString()  +  "/NewTestSoftwareA.jar";
		
		String filePath = GarudaSoftwareBackend.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		
		try {
			filePath = URLDecoder.decode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException exc) {
			if (parent instanceof Component) {
				GUITools.showErrorMessage((Component) parent, exc);
			} else {
				exc.printStackTrace();
			}
		}
		
		filePath = filePath.substring(1);
		
		//      String filePath = null; 
		
		String os_name = System.getProperty("os.name").toLowerCase();
		
		if (os_name.indexOf("mac") >= 0) {
			filePath = "open \"" + filePath;
		} else if (os_name.contains("windows")) {
			filePath = "cmd /c start jre\\bin\\javaw.exe -jar \"" + filePath + "\"";
		} else {
			filePath = "java -jar " + filePath;
		}
		
		//The working directory of the software. It would be advisable to make it the same with the one the 
		//software is in.
		String workingDirectory = parentFolder.getAbsolutePath().toString();
		
		//List of the file outputs from this software
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("HOME", "home");
		
		//The register call for this software to the Core.
		
		CoreClientAPI api = CoreClientAPI.getInstance();
		api.registerSoftware(filePath, workingDirectory,
			listOfInputFileFormats, listOfOutputFileFormats, map);
		
		logger.fine(MessageFormat.format(bundle.getString("REGISTERED_SOFTWARE_TO_CORE"), parentFolder.getAbsolutePath()));
	}
	
	/**
	 * 
	 * @param pcl
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param pcl
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(propertyName, pcl);
	}
	
	/**
	 * 
	 * @param selectedFile
	 * @throws NetworkException
	 * @throws IllegalStateException
	 */
	public void requestForLoadableSoftwares(File selectedFile) throws NetworkException,
		IllegalStateException {
		if (selectedFile != null) {
			CoreClientAPI.getInstance().doGetLoadableSoftwares(
				softwareID,
				selectedFile.getName().substring(
					selectedFile.getName().indexOf('.') + 1));
			logger.fine(MessageFormat.format(bundle.getString("REQUESTED_COMPATIBLE_SOFTWARE"), selectedFile));
		} else {
			throw new IllegalStateException(bundle.getString("NO_FILE_SELECTED"));
		}
	}
	
	/**
	 * 
	 * @param selectedFile
	 * @param indexOfSoftware
	 * @throws NetworkException
	 * @throws IllegalStateException
	 */
	public void sentFileToSoftware(File selectedFile, int indexOfSoftware)
		throws NetworkException, IllegalStateException {
		if ((selectedFile == null) || !selectedFile.exists()) {
			throw new IllegalStateException(MessageFormat.format(bundle.getString("FILE_DOES_NOT_EXIST"), selectedFile));
		}
		if ((listOfCompatibleSoftware == null)
				|| listOfCompatibleSoftware.isEmpty()
				|| (listOfCompatibleSoftware.get(indexOfSoftware) == null)) {
			throw new IllegalStateException(MessageFormat.format(
				bundle.getString("NO_COMPATIBLE_SOFTWARE_FOUND"),
				FileTools.getExtension(selectedFile.getName())));
		}
		CoreClientAPI.getInstance().loadFileOntoSoftware(
			listOfCompatibleSoftware.get(indexOfSoftware),
			selectedFile.getAbsolutePath());
		logger.fine(MessageFormat.format(
			bundle.getString("FILE_LOADING_REQUEST"),
			selectedFile.getAbsolutePath(),
			listOfCompatibleSoftware.get(indexOfSoftware).getName()));
	}
	
	/**
	 * 
	 * @param softwareList
	 */
	public void setListOfCompatibleSoftware(List<Software> softwareList) {
		this.listOfCompatibleSoftware = softwareList;
  	firePropertyChange(GOT_SOFTWARES_PROPERTY_CHANGE_ID, softwareList);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [softwareID=");
		builder.append(softwareID);
		builder.append(", initialized=");
		builder.append(initialized);
		builder.append(", listOfInputFileFormats=");
		builder.append(listOfInputFileFormats);
		builder.append(", listOfOutputFileFormats=");
		builder.append(listOfOutputFileFormats);
		builder.append(']');
		return builder.toString();
	}
	
}
