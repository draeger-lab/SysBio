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
package de.zbit.gui.prefs;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class JDatePanel extends JPanel implements JComponentForOption, ActionListener{

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -1776415205536049365L;

	 /**
	   * Only necessary for using this class in Combination with
	   * {@link SBPreferences} and {@link Option}s.
	   */
	protected Option<?> option = null;

	/**
	 * The calendar for choosing the date.
	 */
	protected JDialogCalendar dialogCalendar;

	/**
	 * The text field for the date.
	 */
	protected JTextField textFieldCalendar;

	/**
	 * The button for opening the calendar.
	 */
	protected JButton buttonCalendar;

	/**
	 * The currently selected date
	 */
	protected Date selectedDate;

	/**
	 * The date format.
	 */
	protected SimpleDateFormat dateFormat;

	/**
	 * The list of change listeners.
	 */
	protected List<ChangeListener> changeListeners;
	
	/**
	 * 
	 * @param initial
	 */
	public JDatePanel(Date date) {
		ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
		this.selectedDate = date;
		
		textFieldCalendar = new JTextField();
		dateFormat = new SimpleDateFormat(bundle.getString("DATE_FORMAT"));
    	textFieldCalendar.setText(dateFormat.format(date));
		textFieldCalendar.setEnabled(false);
		
		this.changeListeners = new LinkedList<ChangeListener>();
		
		buttonCalendar = new JButton("Calendar", UIManager.getIcon("ICON_CALENDAR_16"));
		buttonCalendar.setToolTipText(bundle.getString("DATE_TOOLTIP"));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(textFieldCalendar);
		this.add(buttonCalendar);
		this.setBorder(BorderFactory.createTitledBorder(bundle.getString("DATE")));
		buttonCalendar.addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.prefs.JComponentForOption#getOption()
	 */
	public Option<?> getOption() {
		return option;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.prefs.JComponentForOption#isSetOption()
	 */
	public boolean isSetOption() {
		return option != null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.prefs.JComponentForOption#setOption(de.zbit.util.prefs.Option)
	 */
	public void setOption(Option<?> option) {
		this.option = option;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.prefs.JComponentForOption#getCurrentValue()
	 */
	public Date getCurrentValue() {
		return selectedDate;
	}

	/**
	 * Adds a change listener
	 * @param cl
	 */
	public void addChangeListener(ChangeListener cl) {
		this.changeListeners.add(cl);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonCalendar)) {
			dialogCalendar = new JDialogCalendar(
					SwingUtilities.getWindowAncestor(this),
					ModalityType.APPLICATION_MODAL, selectedDate);
			textFieldCalendar.setText(dateFormat.format(dialogCalendar
					.getSelectedDate()));
			this.selectedDate = dialogCalendar.getSelectedDate();
			for (ChangeListener cl: changeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
			
		}
	}
	
}
