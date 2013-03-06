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

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;

/**
 * A class that provides the possibility to pick a date in a calendar dialog.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class JDialogCalendar extends JDialog implements ActionListener {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6496894143552652769L;
	private boolean dateChanged;
	private Calendar oldCalendar;
	private Calendar newCalendar;
	private Date selectedDate;
	private Font font;
	private JButton buttonPrevious;
	private JButton buttonNext;
	private JLabel[] labelsDayName;
	private JLabel[] labelsDay;
	private JLabel labelMonthYear;
	private JPanel panelCalendar;
	private JPanel panelLabels;
	private JPanel panelNavigation;
	private String[] dayNames;

	/**
	 * 
	 * @param owner
	 * @param modalityType
	 * @param selectedDate
	 */
	public JDialogCalendar(Window owner, ModalityType modalityType,
			Date selectedDate) {
		super(owner, modalityType);
		this.selectedDate = selectedDate;
		initialize();
	}

	/**
	 * 
	 */
	private void initialize() {
		dateChanged = false;

		oldCalendar = Calendar.getInstance();
		oldCalendar.setTime(selectedDate);

		newCalendar = Calendar.getInstance();
		newCalendar.setTime(selectedDate);

		font = new Font(getOwner().getFont().getName(), Font.BOLD, getOwner()
				.getFont().getSize());

		buttonPrevious = new JButton("<");
		buttonPrevious.setFont(font);
		buttonPrevious.addActionListener(this);

		labelMonthYear = new JLabel();
		labelMonthYear.setFont(font);
		labelMonthYear.setHorizontalAlignment(SwingConstants.CENTER);

		buttonNext = new JButton(">");
		buttonNext.setFont(font);
		buttonNext.addActionListener(this);

		panelLabels = new JPanel(new GridLayout(7, 7));

		labelsDayName = new JLabel[7];

		dayNames = new String[] {
				"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};

		for (int i = 0; i < labelsDayName.length; i++) {
			labelsDayName[i] = new JLabel(dayNames[i]);
			labelsDayName[i].setBorder(BorderFactory.createEmptyBorder());
			labelsDayName[i].setFont(font);
			labelsDayName[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDayName[i]
					.setForeground(new Color(0, 0, 0));
			labelsDayName[i]
					.setBackground(
							new Color(208, 199, 196));
			labelsDayName[i].setOpaque(true);
			panelLabels.add(labelsDayName[i]);
		}

		labelsDay = new JLabel[42];

		for (int i = 0; i < labelsDay.length; i++) {
			labelsDay[i] = new JLabel();
			labelsDay[i]
					.setBorder(BorderFactory.createLineBorder(
							new Color(208, 199, 196)));
			labelsDay[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDay[i].setForeground(
					new Color(57, 129, 129));
			labelsDay[i].setBackground(
					new Color(255, 255, 255));
			labelsDay[i].setOpaque(true);
			labelsDay[i].addMouseListener(new MouseAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
				 */
				public void mouseClicked(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						newCalendar.set(Calendar.DAY_OF_MONTH,
							Integer.valueOf(labelDayText));
						dateChanged = true;
						dispose();
					}
				}

				/*
				 * (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
				 */
				public void mouseEntered(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						labelDay.setForeground(
								new Color(255, 255, 255));
						labelDay.setBackground(
								new Color(128, 162, 205));
					}
				}

				/*
				 * (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
				 */
				public void mouseExited(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						newCalendar.set(Calendar.DAY_OF_MONTH,
								Integer.valueOf(labelDayText));
						if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar
								.get(Calendar.DAY_OF_MONTH)
								&& newCalendar.get(Calendar.MONTH) == oldCalendar
										.get(Calendar.MONTH)
								&& newCalendar.get(Calendar.YEAR) == oldCalendar
										.get(Calendar.YEAR)) {
							labelDay.setForeground(
									new Color(57, 129, 219));
							labelDay.setBackground(
									new Color(178, 212, 255));
						} else {
							labelDay.setForeground(
									new Color(57, 129, 129));
							labelDay.setBackground(
									new Color(255, 255, 255));
						}
					}
				}

			});
			panelLabels.add(labelsDay[i]);
		}

		panelNavigation = new JPanel(new BorderLayout());
		panelNavigation.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		panelNavigation.add(buttonPrevious, BorderLayout.WEST);
		panelNavigation.add(labelMonthYear, BorderLayout.CENTER);
		panelNavigation.add(buttonNext, BorderLayout.EAST);

		panelCalendar = new JPanel(new BorderLayout());
		panelCalendar
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelCalendar.add(panelNavigation, BorderLayout.NORTH);
		panelCalendar.add(panelLabels, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setTitle("Calendar");
		setSize(new Dimension(300, 250));
		setResizable(false);
		setLocationRelativeTo(getOwner());
		add(panelCalendar, BorderLayout.CENTER);
		displayDate();
		setVisible(true);
	}

	/**
	 * Redraws the calendar if changed.
	 */
	private void displayDate() {
		for (JLabel labelDay : labelsDay) {
			labelDay.setText("");
		}
		newCalendar.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = newCalendar.get(Calendar.DAY_OF_WEEK);
		int daysInMonth = newCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int i = dayOfWeek - 1, day = 1; day <= daysInMonth; i++, day++) {
			labelsDay[i].setText(String.valueOf(day));
			newCalendar.set(Calendar.DAY_OF_MONTH, day);
			if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar
					.get(Calendar.DAY_OF_MONTH)
					&& newCalendar.get(Calendar.MONTH) == oldCalendar
							.get(Calendar.MONTH)
					&& newCalendar.get(Calendar.YEAR) == oldCalendar
							.get(Calendar.YEAR)) {
				labelsDay[i]
						.setForeground(
								new Color(57, 129, 219));
				labelsDay[i]
						.setBackground(
								new Color(178, 212, 255));
			} else {
				labelsDay[i].setForeground(
						new Color(57, 129, 129));
				labelsDay[i].setBackground(
						new Color(255, 255, 255));
			}

		}
		labelMonthYear
				.setText(new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
						.format(newCalendar.getTime()));
	}

	/**
	 * Returns the picked date in the calendar.
	 * 
	 * @return
	 */
	public Date getSelectedDate() {
		if (!dateChanged) {
			return oldCalendar.getTime();
		} else {
			return newCalendar.getTime();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonPrevious)) {
			newCalendar
					.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) - 1);
			displayDate();
		}
		if (e.getSource().equals(buttonNext)) {
			newCalendar
					.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) + 1);
			displayDate();
		}
	}
}

