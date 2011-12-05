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
package de.zbit.gui.prefs;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;
import de.zbit.util.prefs.KeyProvider;

/**
 * This class provides several tools to create an online help displaying the
 * command line options for a program based on {@link KeyProvider}s.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-24
 * @version $Rev$
 * @since 1.0
 */
public class CommandLineHelp {
	
	/**
	 * Creates a nice title for this online help.
	 */
	private static final String title = KeyProvider.Tools
			.createTitle(CommandLineHelp.class);
	
	/**
	 * Creates a non-editable {@link JEditorPane} embedded in a
	 * {@link JScrollPane} for each {@link KeyProvider} that displays a help page.
	 * If there is only one such class given, this will directly be returned. In
	 * case of multiple classes, one tab will be created for each class and a
	 * {@link JTabbedPane} will be returned that contains all these element. If no
	 * class is given, null will be returned.
	 * 
	 * @param c
	 * @param clazz
	 * @return A {@link JEditorPane} embedded in a {@link JScrollPane} if just one
	 *         {@link Class} is given, a {@link JTabbedPane} with one such tab for
	 *         each {@link Class}, or null if nothing is given.
	 */
	public static JComponent createHelpComponent(
		Class<? extends KeyProvider>... clazz) {
		if (clazz.length == 1) {
			return new JScrollPane(initJEditor(clazz[0]),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else if (clazz.length > 1) {
			//			JTabbedPane tabs = new JTabbedPane();
			//			for (Class<? extends KeyProvider> keyProvider : clazz) {
			//				if (keyProvider.getFields().length > 0) {
			//					tabs.add(KeyProvider.Tools.createTitle(keyProvider), new JScrollPane(
			//						initJEditor(keyProvider), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			//						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
			//				}
			//			}
			//			return tabs;
			return new JScrollPane(initJEditor(clazz),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return null;
	}
	
	/**
	 * Gathers all classes that implement the interface {@link KeyProvider} and
	 * that are located in the given package or any sub-package.
	 * 
	 * @param packageName
	 * @return
	 */
	public static Class<? extends KeyProvider>[] getAllKeyProvidersInPackage(
		String packageName) {
		return Reflect.getAllClassesInPackage(packageName, true, true,
			KeyProvider.class);
	}
	
	/**
	 * Creates a non-editable {@link JEditorPane} for the given
	 * {@link KeyProvider} class.
	 * 
	 * @param clazz
	 * @return
	 */
	private static JEditorPane initJEditor(Class<? extends KeyProvider> clazz) {
		JEditorPane editor = new JEditorPane("text/html", KeyProvider.Tools
				.createDocumentation(clazz));
		editor.setEditable(false);
		editor.setEnabled(true);
		editor.setBackground(Color.WHITE);
		return editor;
	}
	
	/**
	 * 
	 * @param clazzes
	 * @return
	 */
	private static JEditorPane initJEditor(Class<? extends KeyProvider>... clazzes) {
		JEditorPane editor = new JEditorPane("text/html", KeyProvider.Tools
				.createDocumentation(clazzes));
		editor.setEditable(false);
		editor.setEnabled(true);
		editor.setCaretPosition(0); // Set Scrollbars to top of Frame.
		return editor;
	}
	
	/**
	 * Shows a {@link JFrame} with a help page about the command line options for
	 * given or all {@link KeyProvider} classes.
	 * 
	 * @param args
	 *        Class names of one or multiple {@link KeyProvider} classes. If no
	 *        arguments are given, this method will search for all classes
	 *        implementing {@link KeyProvider} in the package 'de'.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Class<? extends KeyProvider> clazz[];
		if (args.length > 0) {
			clazz = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				try {
					clazz[i] = (Class<? extends KeyProvider>) Class.forName(args[i]);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
			clazz = getAllKeyProvidersInPackage("de");
		}
		GUITools.initLaF(title);
		new CommandLineHelp(clazz);
	}
	
	/**
	 * Creates a {@link JFrame} that displays in one tab for each given
	 * {@link KeyProvider} class a special help page.
	 */
	public CommandLineHelp(Class<? extends KeyProvider>... clazz) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(createHelpComponent(clazz));
		frame.pack();
		frame.setSize(640, 480);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
