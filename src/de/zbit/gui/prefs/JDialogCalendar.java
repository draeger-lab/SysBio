/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import de.zbit.gui.ColorPalette;
import de.zbit.util.ResourceManager;

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
	
	/**
	 * Localization support.
	 */
	private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
	
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

		font = new Font(getOwner().getFont().getName(), Font.BOLD, getOwner().getFont().getSize());

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

		DateFormatSymbols dateSym = DateFormatSymbols.getInstance();

		for (int i = 0; i < labelsDayName.length; i++) {
			labelsDayName[i] = new JLabel(dateSym.getShortWeekdays()[i + 1]);
			labelsDayName[i].setToolTipText(dateSym.getWeekdays()[i + 1]);
			labelsDayName[i].setBorder(BorderFactory.createEmptyBorder());
			labelsDayName[i].setFont(font);
			labelsDayName[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDayName[i].setForeground(Color.BLACK);
			labelsDayName[i].setBackground(ColorPalette.GOLD_50_PERCENT);
			labelsDayName[i].setOpaque(true);
			panelLabels.add(labelsDayName[i]);
		}

		labelsDay = new JLabel[42];

		for (int i = 0; i < labelsDay.length; i++) {
			labelsDay[i] = new JLabel();
			labelsDay[i].setBorder(BorderFactory.createLineBorder(ColorPalette.GOLD_50_PERCENT));
			labelsDay[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDay[i].setForeground(ColorPalette.ANTHRACITE);
			labelsDay[i].setBackground(Color.WHITE);
			labelsDay[i].setOpaque(true);
			labelsDay[i].addMouseListener(new MouseAdapter() {

				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseClicked(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						labelDay.setForeground(Color.WHITE);
						labelDay.setBackground(ColorPalette.SECOND_292.brighter());
						newCalendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(labelDayText));
						dateChanged = true;
						dispose();
					}
				}
				
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseEntered(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						labelDay.setForeground(Color.WHITE);
						labelDay.setBackground(ColorPalette.CAMINE_RED_50_PERCENT);
					}
				}
				
				/* (non-Javadoc)
				 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseExited(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						newCalendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(labelDayText));
						if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar.get(Calendar.DAY_OF_MONTH)
								&& newCalendar.get(Calendar.MONTH) == oldCalendar.get(Calendar.MONTH)
								&& newCalendar.get(Calendar.YEAR) == oldCalendar.get(Calendar.YEAR)) {
							labelDay.setForeground(Color.WHITE); // new Color(57, 129, 219)
							labelDay.setBackground(ColorPalette.SECOND_292.brighter()); // new Color(178, 212, 255)
						} else {
							labelDay.setForeground(ColorPalette.ANTHRACITE);
							labelDay.setBackground(Color.WHITE);
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
		panelCalendar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelCalendar.add(panelNavigation, BorderLayout.NORTH);
		panelCalendar.add(panelLabels, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setTitle(bundle.getString("CALENDAR"));
		setSize(new Dimension(300, 250));
		setResizable(false);
		setLocationRelativeTo(getOwner());
		add(panelCalendar, BorderLayout.CENTER);
		displayDate();
		
		
    // Close dialog with ESC button.
    getRootPane().registerKeyboardAction(new ActionListener() {
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
    	public void actionPerformed(ActionEvent e) {
    		dateChanged = false;
        dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    // Close dialog with ENTER button.
    getRootPane().registerKeyboardAction(new ActionListener() {
    	/* (non-Javadoc)
    	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    	 */
      public void actionPerformed(ActionEvent e) {
      	// TODO: Improve! When OK is pressed (enter), the current date could be selected.
      	dateChanged = false;
				dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
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
			if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar.get(Calendar.DAY_OF_MONTH)
					&& newCalendar.get(Calendar.MONTH) == oldCalendar.get(Calendar.MONTH)
					&& newCalendar.get(Calendar.YEAR) == oldCalendar.get(Calendar.YEAR)) {
				labelsDay[i].setForeground(Color.WHITE);
				labelsDay[i].setBackground(ColorPalette.SECOND_292.brighter());
			} else {
				labelsDay[i].setForeground(ColorPalette.ANTHRACITE);
				labelsDay[i].setBackground(Color.WHITE);
			}

		}
		labelMonthYear.setText(new SimpleDateFormat("MMMM yyyy").format(newCalendar.getTime()));
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

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonPrevious)) {
			newCalendar.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) - 1);
			displayDate();
		}
		if (e.getSource().equals(buttonNext)) {
			newCalendar.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) + 1);
			displayDate();
		}
	}

}
