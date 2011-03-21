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
package de.zbit.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.EventHandler;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 * @author Andreas Dr&auml;ger
 * @since 18.03.2011
 * @version $Rev$
 */
public abstract class BaseFrameWithInternalFrames extends BaseFrame {
	
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2843934008835005719L;
	
	/**
	 * 
	 */
	private static final Logger logger = Logger
			.getLogger(BaseFrameWithInternalFrames.class.getName());
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 */
	public static enum WindowAction implements ActionCommand {
		CLOSE_ALL,
		CLOSE_LATEST,
		NEXT_TO_EACH_OTHER;

		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			switch (this) {
				case NEXT_TO_EACH_OTHER:
					return "Next to each other";
				case CLOSE_ALL:
					return "Close all";
				case CLOSE_LATEST:
					return "Close latest";
				default:
					break;
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			switch (this) {
				case NEXT_TO_EACH_OTHER:
					return "Places all windows next to each other";
				default:
					break;
			}
			return null;
		}
	}
	
	/**
	 * 
	 */
	private JMenu menuWindows;
	
	/**
	 * 
	 */
	protected JDesktopPane desktop;
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
	@Override
	protected Component createMainComponent() {
		desktop = new JDesktopPane();
		desktop.setOpaque(true);
		return desktop;
	}
	
	/**
	 * Places all {@link InternalFrame}s next to each other.
	 */
	public void placeInternalFramesNextToEachOther() {
		// TODO
	}
		
	/**
	 * 
	 * @return
	 */
	public boolean closeInternalFrame(String title) {
		selectInternalFrame(title);
		boolean success = closeFile();
		desktop.getSelectedFrame().dispose();
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFileAndLogHistory(java.io.File[])
	 */
	File[] openFileAndLogHistory(File... files) {
		files = super.openFileAndLogHistory(files);
		JInternalFrame frames[] = desktop.getAllFrames(); 
		if (frames.length > 0) {
			GUITools.setEnabled(true, getJMenuBar(), toolBar, WindowAction.CLOSE_ALL,
				WindowAction.CLOSE_LATEST);
			int i;
			for (i = menuWindows.getItemCount() - 1; i > WindowAction.values().length; i--) {
        menuWindows.remove(i);
			}
			menuWindows.add(new JSeparator());
			i = 0;
			for (JInternalFrame iFrame : frames) {
				JMenuItem item = new JMenuItem(iFrame.getTitle());
				if (frames.length <= 10) {
					item.setAccelerator(KeyStroke.getKeyStroke(String.valueOf(
						(i + 1 < 10) ? i + 1 : 0).charAt(0), InputEvent.CTRL_DOWN_MASK));
				}
				i++;
				final String name = item.getText();
				item.addActionListener(new ActionListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @seejava.awt.event.ActionListener#actionPerformed(java.awt.event.
					 * ActionEvent)
					 */
					public void actionPerformed(ActionEvent e) {
						selectInternalFrame(name);
					}
				});
				menuWindows.add(item);
			}
			if (frames.length > 1) {
				GUITools.setEnabled(true, getJMenuBar(), toolBar,
					WindowAction.NEXT_TO_EACH_OTHER);
			}
		}
		return files;
	}
	
	/**
	 * 
	 * @param title
	 */
	public void selectInternalFrame(String title) {
		JInternalFrame iFrame = findInternalFrameFor(title);
		desktop.moveToFront(iFrame);
		try {
			iFrame.setSelected(true);
		} catch (PropertyVetoException exc) {
			logger.log(Level.WARNING, exc.getMessage());
		}
	}
	
	/**
	 * @param title
	 * @return
	 */
	private JInternalFrame findInternalFrameFor(String title) {
		JInternalFrame frames[] = desktop.getAllFrames(); 
		JInternalFrame iFrame = frames[frames.length - 1];
		for (int i = frames.length - 2; i >= 0
				&& !iFrame.getTitle().equals(frames[i].getTitle()); i--)
			;
		return iFrame;
	}

	/**
	 * 
	 */
	public final void closeAllInternalFrames() {
		JInternalFrame frames[] = desktop.getAllFrames();
		for (int i = frames.length - 1; i >= 0; i--) {
			closeInternalFrame(frames[i].getTitle());
		}
		GUITools.setEnabled(false, getJMenuBar(), toolBar, WindowAction.CLOSE_ALL,
			WindowAction.CLOSE_LATEST);
	}
	
	/**
	 * 
	 * @return
	 */
	public final boolean closeLatestInternalFrame() {
		JInternalFrame frames[] = desktop.getAllFrames();
		boolean success = closeInternalFrame(frames[getPositionOfCurrentInternalFrame()]
				.getTitle());
		if (success && (frames.length == 1)) {
			GUITools.setEnabled(false, getJMenuBar(), toolBar,
				WindowAction.CLOSE_ALL, WindowAction.CLOSE_LATEST);
		}
		return success;
	}

	/**
	 * @return
	 */
	public int getPositionOfCurrentInternalFrame() {
		JInternalFrame frame = desktop.getSelectedFrame();
		if (frame == null) {
			JInternalFrame frames[] = desktop.getAllFrames();
			return frames.length > 0 ? frames.length - 1 : -1;
		}
		return desktop.getIndexOf(frame);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJMenuBar(boolean)
	 */
	@Override
	protected JMenuBar createJMenuBar(boolean loadDefaultFileMenuEntries) {
		/*
		 *  By overriding the method from the super class, it is still possible
		 *  for derived classes to make use of the method additionalMenus().
		 */		
		JMenuBar menu = super.createJMenuBar(loadDefaultFileMenuEntries);
		JMenuItem itemNext = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "placeInternalFramesNextToEachOther"),
			WindowAction.NEXT_TO_EACH_OTHER);
		itemNext.setEnabled(false);
		JMenuItem itemCloseAll = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "closeAllInternalFrames"), WindowAction.CLOSE_ALL);
		itemCloseAll.setEnabled(false);
		JMenuItem itemCloseLatest = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "closeLatestInternalFrame"), WindowAction.CLOSE_LATEST);
		itemCloseLatest.setEnabled(false);
		menuWindows = GUITools.createJMenu("Windows", "Manages windows", itemNext,
			new JSeparator(), itemCloseAll, itemCloseLatest);
		JMenu menuHelp = null;
		if (menu.getMenuCount() > 0) {
			if (menu.getMenu(menu.getMenuCount() - 1).getText().equals(
				BaseAction.HELP.getName())) {
				menuHelp = menu.getMenu(menu.getMenuCount() - 1);
				menu.remove(menuHelp);
			}
		}
		menu.add(menuWindows);
		if (menuHelp != null) {
			try {
				menu.setHelpMenu(menuHelp);
			} catch (Error e) {
				menu.add(menuHelp);
			}
		}
		return menu;
	}
	
}
