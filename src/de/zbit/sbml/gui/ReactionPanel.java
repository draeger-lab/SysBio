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
package de.zbit.sbml.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JPanel;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.util.StringTools;

/**
 * {@link JPanel} for visualization of JSBML {@link Reaction}s.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-18
 * @since 1.0
 * @version $Rev$
 */
public class ReactionPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1820252195280709150L;
	/**
	 * 
	 */
	private int preferredWith, preferredHeight;
	/**
	 * 
	 */
	private Reaction reaction;
	/**
	 * 
	 */
	private boolean namesIfAvailalbe;
	
	/**
	 * 
	 * @param reaction
	 */
	public ReactionPanel(Reaction reaction, boolean namesIfAvailalbe) {
		super();
		this.reaction = reaction;
		this.namesIfAvailalbe = namesIfAvailalbe;
		int fontSize = getFont().getSize();
		this.preferredWith = 0;
		if (reaction.isSetListOfReactants()) {
		   preferredWith += createString(reaction.getListOfReactants(), false, true).length();
		}
		if (reaction.isSetListOfModifiers()) {
		  preferredWith += createString(reaction.getListOfModifiers(), true, true).length(); 
		}
		if (reaction.isSetListOfProducts()) {
			preferredWith += createString(reaction.getListOfProducts(), true, false).length();
		}
		preferredWith *= fontSize;
		this.preferredHeight = reaction.isReversible() ? fontSize * 5
				: fontSize * 3;
	}
	
	/**
	 * 
	 * @param listOf
	 * @param separator
	 * @param leadingBlank
	 * @param tailingBlank
	 * @return
	 */
	private String createString(ListOf<? extends SimpleSpeciesReference> listOf,
		boolean leadingBlank, boolean tailingBlank) {
		StringBuilder sb = new StringBuilder();
		if (leadingBlank) {
			sb.append(' ');
		}
		String name;
		if (listOf.size() > 0) {
			SimpleSpeciesReference specRef;
			for (int i = 0; i < listOf.size(); i++) {
				specRef = listOf.get(i);
				if (specRef instanceof SpeciesReference) {
					SpeciesReference s = (SpeciesReference) specRef;
					if (s.getStoichiometry() != 1d) {
						sb.append(StringTools.toString(s.getStoichiometry()));
						sb.append(' ');
					}
				}
				Species species = specRef.getSpeciesInstance();
        if ((species != null) && namesIfAvailalbe && species.isSetName()) {
          name = species.getName();
				} else {
					name = specRef.getSpecies();
				}
				sb.append(name);
				if (i < listOf.size() - 1) {
					sb.append((specRef instanceof SpeciesReference) ? " + " : ", ");
				}
			}
		} else {
			// empty set symbol
			sb.append("\u2205");
		}
		if (tailingBlank) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param length
	 * @param fontSize
	 * @param g
	 */
	private void drawReactionArrow(int x, int y, double length, int fontSize,
		Graphics g) {
	  
	  int width = fontSize / 3;
	  
	  // Ensure a minimum arrow length
    length = Math.max(length, width * 3);
    
		int x2 = (int) (x + length), y2, height = fontSize / 4;
		
		if (reaction.isReversible()) {
			y2 = y - width;
			int y3 = y2 + height;
			g.drawLine(x, y2, x2, y2);
			g.drawLine(x, y3, x2, y3);
			
			Polygon rightArrow = new Polygon(new int[] { x2 - width / 2,
					x2 - width / 2, x2 }, new int[] { y2, y2 - height / 2, y2 }, 3);
			g.fillPolygon(rightArrow);
			Polygon leftArrow = new Polygon(new int[] { x, x + width / 2,
					x + width / 2 }, new int[] { y3, y3 + height / 2, y3 }, 3);
			g.fillPolygon(leftArrow);
			
			g.drawLine(x2, y2, x2 - width, y2 - height);
			g.drawLine(x, y3, x + width, y3 + height);
			
		} else {
			height = fontSize / 3;
			y2 = y - width;
			g.drawLine(x, y2, x2 - 1, y2);
			Polygon p = new Polygon(new int[] { x2 - width, x2, x2 - width },
				new int[] { y2 - height, y2, y2 + height }, 3);
			g.fillPolygon(p);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(preferredWith, preferredHeight);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (g != null) {
			int fontSize = g.getFont().getSize(), x = 0, y = fontSize;
			double length;
			if (reaction.getModifierCount() > 0) {
				y *= 2;
			}
			FontMetrics metrics = g.getFontMetrics();
			String curr = reaction.isSetListOfReactants() ? createString(reaction.getListOfReactants(), false, true) : "\u2205";
			g.drawString(curr, x, y);
			x += metrics.getStringBounds(curr, g).getWidth();
			if (reaction.getModifierCount() > 0) {
				curr = createString(reaction.getListOfModifiers(), true, true);
				g.drawString(curr, x, y - fontSize);
				length = metrics.getStringBounds(curr, g).getWidth();
				drawReactionArrow(x, y, length, fontSize, g);
				x += length;
			} else {
				// simply draw a regular reaction arrow
				curr = reaction.isReversible() ? " \u21CC " : " \u2192 ";
				g.drawString(curr, x, y);
				x += metrics.getStringBounds(curr, g).getWidth();
			}
			curr = reaction.isSetListOfProducts() ? createString(reaction.getListOfProducts(), true, false) : "\u2205";
			g.drawString(curr, x, y);
			preferredWith = 2 * fontSize
					+ (int) (x + metrics.getStringBounds(curr, g).getWidth());
			preferredHeight = fontSize + y;
		}
	}
	
}
