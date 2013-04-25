/*
 * $Id$ 
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

/**
 * <p>
 * Garuda is a kind of "App Store" for bioinformatics and systems biology
 * software. It basically allows Garuda-enabled tools to receive and sent files
 * from/to other Garuda-enabled tools.
 * <p>
 * Here follows a minimal example of how to use it to Garuda-enable any
 * software. When initializing your graphical user interface, which must be
 * derived from {@link de.zbit.UserInterface}, just insert the following piece 
 * of code:
 * <pre>
 * new Thread(new Runnable() {
 *   public void run() {
 *     try {
 *       GarudaSoftwareBackend garudaBackend = new GarudaSoftwareBackend(
 *         uuid,                // a unique identifier for your gadget
 *         (UserInterface) gui, // the user interface of the gadget
 *         icon,                // an icon of size 128x128 for the Garuda dash-board
 *         description,         // a few sentences describing the program
 *         keywords,            // a list of Strings giving the categories for the gadget
 *         screenshots          // a list of example images of the gadget
 *       );
 *       garudaBackend.addInputFileFormat(&quot;xml&quot;, &quot;SBML&quot;);
 *       // ... as many additional input file formats as supported
 *       garudaBackend.addOutputFileFormat(&quot;xml&quot;, &quot;SBML&quot;);
 *       // ... as many additional output file formats as supported
 *       garudaBackend.init();
 *       garudaBackend.registedSoftwareToGaruda();
 *     } catch (NetworkException exc) {
 *       GUITools.showErrorMessage(gui, exc);
 *     } catch (BackendNotInitializedException exc) {
 *       GUITools.showErrorMessage(gui, exc);
 *     } catch (Throwable exc) {
 *       String message = exc.getLocalizedMessage();
 *       logger.log(Level.FINE, message != null ? message : exc.getMessage(), exc);
 *     }
 *  }
 * }).start();
 * <pre>
 * In the above code, it is assumed that {@code gui} is some instance of a AWT
 * or SWING element. Insert this code in the method
 * {@link de.zbit.Launcher#initGUI(de.zbit.AppConf)} for the launcher of your
 * particular software. Of course, the only thing you'll have to adapt are the
 * supported file formats.
 * <p>
 * In order to avoid a timeout because no Garuda Core is running, you can use
 * the {@link de.zbit.garuda.GarudaOptions} class to add a command-line option
 * to your software. You might want to surround Garuda loading with the
 * following code in your {@link de.zbit.Launcher#initGUI(de.zbit.AppConf)}:
 * <pre>
 * if (!appConf.getCmdArgs().containsKey(GarudaOptions.CONNECT_TO_GARUDA)
 * 		|| appConf.getCmdArgs().getBoolean(GarudaOptions.CONNECT_TO_GARUDA)) {
 *   // load Garuda as shown above.
 * }
 * </pre>
 * <p>
 * Your user interface could provide a specialized {@link javax.swing.JMenu} for
 * interaction with Garuda. To this end, the
 * {@link de.zbit.garuda.GarudaGUIfactory} provides the method
 * {@link de.zbit.garuda.GarudaGUIfactory#createGarudaMenu(java.awt.event.ActionListener)}.
 * You only have to pass some {@link java.awt.event.ActionListener} to this
 * method and it will create a {@link javax.swing.JMenu} providing access to
 * Garuda functions.
 * <p>
 * However, before you can react to action events coming from the Garuda Core,
 * you'll have to register the {@link de.zbit.garuda.GarudaSoftwareBackend} in
 * your application: As any instance of {@link de.zbit.UserInterface}, also your
 * one must extend {@link java.beans.PropertyChangeListener}. In order to 
 * successfully enable Garuda in your application, do the following in your gui:
 * <pre>
 * public void propertyChange(PropertyChangeEvent evt) {
 *   String propName = evt.getPropertyName();
 *   if (propName.equals(GarudaSoftwareBackend.GARUDA_ACTIVATED)) {
 *     this.garudaBackend = (GarudaSoftwareBackend) evt.getNewValue();
 *     if (supportedFileOpened) {
 *       GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), GarudaActions.SENT_TO_GARUDA);
 *     }
 *   } else {
 *     // ... do whatever else could be necessary for your program.
 *   }
 * }
 * </pre>
 * In this method, the instance of the
 * {@link de.zbit.garuda.GarudaSoftwareBackend}
 * can be stored in the GUI as a controller for Garuda.
 * It is also important to enable or disable the action 
 * {@link de.zbit.garuda.GarudaActions#SENT_TO_GARUDA} when files are
 * opened or closed that could be sent to Garuda gadgets.
 * <p>
 * How to send a file to another Garuda-enabled software:
 * <pre>
 * GarudaFileSender sender = new GarudaFileSender(parentComponent, garudaBackend, file, fileType);
 * sender.execute();
 * </pre>
 * This example will open a {@link javax.swing.JOptionPane} displaying
 * compatible Garuda-enabled software as soon as it received the list of Garuda
 * software from the core. If the user selects one of these programs, the
 * {@link de.zbit.garuda.GarudaFileSender} will automatically sent the given
 * {@link java.io.File} to that program. Note that it could be necessary to
 * create a temporary {@link java.io.File} and saving the current document from
 * your software to this {@link java.io.File} before sending it to the Garuda
 * Core. Here, {@code fileType} is a String giving, e.g., &quot;SBML&quot;,
 * giving the exact type of file to be sended to other gadgets.
 * <p>
 * In order to sent a file to other Garuda-enabled tools, you will have to add
 * some buttons or menu items to your application. This can, for instance, be
 * done by using the {@link de.zbit.garuda.GarudaGUIfactory}. The action to sent
 * a file can be performed by using the {@link de.zbit.garuda.GarudaFileSender}.
 * <p>
 * For localization support you can find an XML file containing several useful
 * entries in the Garuda resource folder. This folder also contains Garuda icons
 * in several sizes. These icons and also the localization are automatically
 * linked to {@link de.zbit.garuda.GarudaActions} and therefore already used
 * when calling
 * {@link de.zbit.garuda.GarudaGUIfactory#createGarudaMenu(java.awt.event.ActionListener)}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1158$
 */
package de.zbit.garuda;
