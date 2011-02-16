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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A helper class that provides several methods for working with a
 * {@link GridBagLayout}.
 * 
 * @since 1.0
 * @author Andreas Dr&auml;ger
 * @date 2005-07-29
 * @version $Rev$
 */
public class LayoutHelper {
	
	/**
	 * 
	 * @param cont
	 * @param gbl
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 */
	public static void addComponent(Container cont, GridBagLayout gbl,
		Component c, int x, int y, int width, int height, double weightx,
		double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		addComponent(cont, gbl, c, x, y, width, height, weightx, weighty,
			gbc.ipadx, gbc.ipady);
	}
	
	/**
	 * TODO
	 * 
	 * @param cont
	 * @param gbl
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 * @param ipadx
	 * @param ipady
	 */
	public static void addComponent(Container cont, GridBagLayout gbl,
		Component c, int x, int y, int width, int height, double weightx,
		double weighty, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.ipadx = ipadx;
		gbc.ipady = ipady;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}
	
	/**
	 * 
	 */
	private Container cont;
	
	/**
	 * 
	 */
	private GridBagLayout gbl;
	
	/**
	 * 
	 */
	private int row;
	
	/**
	 * Creates a new GridBaglayout and associates this with the given container.
	 * 
	 * @param cont
	 */
	public LayoutHelper(Container cont) {
		this.cont = cont;
		this.gbl = new GridBagLayout();
		this.cont.setLayout(gbl);
		this.row = 0;
	}
	
	/**
	 * 
	 * @param cont
	 * @param gbl
	 */
	public LayoutHelper(Container cont, GridBagLayout gbl) {
		this.cont = cont;
		this.gbl = gbl;
		this.cont.setLayout(this.gbl);
		this.row = 0;
	}
	
	/**
	 * 
	 * @param sameWidth
	 *        if true, a maximum width will be applied to each component, sharing
	 *        the total width of the {@link Container}.
	 * @param c
	 * @param components
	 */
	public void add(boolean sameWidth, Component c, Component... components) {
		add(c, 0, ++row, 1, 1, sameWidth ? 1 : 0, 0);
		for (int i = 0; i < components.length; i++) {
			add(components[i], i + 1, row, 1, 1, sameWidth ? 1 : 0, 0);
		}
	}
	
	/**
	 * adds this component in the next row.
	 * 
	 * @param c
	 */
	public void add(Component c) {
		add(c, 0, ++row, 1, 1, 1d, 1d);
	}
	
	/**
	 * Add one or many components in one line.
	 * 
	 * @param c
	 * @param comps
	 */
	public void add(Component c, Component... comps) {
		add(false, c, comps);
	}
	
	/**
	 * 
	 * @param c
	 * @param width
	 */
	public void add(Component c, int width) {
		add(c, 0, ++row, width, 1);
	}
	
	/**
	 * Add a component without specifying the row.
	 * 
	 * @param c
	 * @param x
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 */
	public void add(Component c, int x, int width, int height, double weightx,
		double weighty) {
		add(c, x, row, width, height, weightx, weighty);
		row++;
	}
	
	/**
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void add(Component c, int x, int y, int width, int height) {
		LayoutHelper
				.addComponent(this.cont, this.gbl, c, x, y, width, height, 0, 0);
		row = y;
	}
	
	/**
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 */
	public void add(Component c, int x, int y, int width, int height,
		double weightx, double weighty) {
		addComponent(this.cont, this.gbl, c, x, y, width, height, weightx, weighty);
		row = y;
	}
	
	/**
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 * @param ipadx
	 * @param ipady
	 */
	public void add(Component c, int x, int y, int width, int height,
		double weightx, double weighty, int ipadx, int ipady) {
		LayoutHelper.addComponent(this.cont, this.gbl, c, x, y, width, height,
			weightx, weighty, ipadx, ipady);
		row = y;
	}
	
	/**
	 * 
	 * @param label
	 * @param c
	 */
	public void add(String label, Component c) {
		add(label, c, 0, ++row);
	}
	
	/**
	 * A row of components
	 * 
	 * @param label
	 * @param components
	 */
	public void add(String label, Component... components) {
		int x = 0;
		add(new JLabel(label), x, ++row, 1, 1, 0, 0);
		for (Component component : components) {
			add(new JPanel(), ++x, row, 1, 1, 0, 0);
			add(component, ++x, row, 1, 1);
		}
	}
	
	/**
	 * 
	 * @param label
	 * @param c
	 * @param spaceLine
	 *        If true, a new line with an empty JPanel will be created as a
	 *        spacer.
	 */
	public void add(String label, Component c, boolean spaceLine) {
		add(label, c, 0, ++row);
		if (spaceLine) {
			add(new JPanel(), 0, ++row, 3, 1, 0, 0);
		}
	}
	
	/**
	 * Creates a pair of a label and a component separated by a spacing panel.
	 * 
	 * @param label
	 * @param c
	 * @param x
	 * @param y
	 */
	public void add(String label, Component c, int x, int y) {
		add(new JLabel(label), x, y, 1, 1, 0, 0);
		add(new JPanel(), x + 1, y, 1, 1, 0, 0);
		add(c, x + 2, y, 1, 1);
	}
	
	/**
	 * 
	 * @param label
	 * @param c
	 * @param width
	 * @param weightx
	 * @param weighty
	 */
	public void add(String label, Component c, int width, int weightx, int weighty) {
		add(label, c, 0, row, weightx, weighty);
		row++;
	}
	
	/**
	 * 
	 * @param label
	 * @param c
	 * @param x
	 * @param y
	 * @param weightx
	 * @param weighty
	 */
	public void add(String label, Component c, int x, int y, int weightx,
		int weighty) {
		add(new JLabel(label), x, y, 1, 1, 0, 0);
		add(new JPanel(), x + 1, y, 1, 1, 0, 0);
		add(c, x + 2, y, 1, 1, weightx, weighty);
		this.row = y;
	}
	
	/**
	 * 
	 * @return
	 */
	public Container getContainer() {
		return this.cont;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Manually increment the current row.
	 * 
	 * @param increment
	 */
	public void incrementRowBy(int increment) {
		row += increment;
	}
	
}
