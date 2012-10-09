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
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.zbit.Launcher;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.gui.actioncommand.ActionCommandWithIcon;
import de.zbit.io.OpenFile;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.objectwrapper.ValuePair;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class contains various GUI tools.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class GUITools {
  
  /**
   * 
   */
  private static final Logger logger = Logger.getLogger(GUITools.class.getName());
  
  /**
   * Build a panel with cancel and ok buttons.
   * When any button is pressed, it will trigger setVisible(false).
   * <p>You can check if ok has been pressed with
   * <pre> ((JButton) ((JPanel) buttonPanel.getComponent(0)).getComponent(0)).isSelected()</pre>
   * @param parentDialog the dialog to close (hide) with ok and cancel.
   * @return
   */
  public static JPanel buildOkCancelButtons(final Component parentDialog) {
    JPanel southPanel = new JPanel(new BorderLayout());

    // Ok Button
    FlowLayout fr = new FlowLayout();
    fr.setAlignment(FlowLayout.RIGHT);
    JPanel se = new JPanel(fr);
    String text = GUITools.getOkButtonText();
    final JButton ok = new JButton(text);
    //if (this.defaultFont!=null) ok.setFont(defaultFont);
    se.add(ok);
    southPanel.add(se, BorderLayout.EAST);

    // Cancel Button
    FlowLayout fl = new FlowLayout();
    fl.setAlignment(FlowLayout.LEFT);
    JPanel sw = new JPanel(fl);
    text = GUITools.getCancelButtonText();
    final JButton cancel = new JButton(text);
    //if (this.defaultFont!=null) cancel.setFont(defaultFont);
    sw.add(cancel);
    southPanel.add(sw, BorderLayout.WEST);
    
    // Set common size
    ok.setPreferredSize(cancel.getPreferredSize()); // new Dimension(75,25)
    
    // Add listeners
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ok.setSelected(true);
        cancel.setSelected(false);
        if (parentDialog!=null) parentDialog.setVisible(false);
      }      
    });
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ok.setSelected(false);
        cancel.setSelected(true);
        if (parentDialog!=null) parentDialog.setVisible(false);
      }      
    });
    
    return southPanel;
  }
  
  
  /**
   * Sets the dimension of all given {@link Component} instances to the maximal
   * size, i.e., all components will be set to equal size, which is the maximal
   * preferred size of one of the components.
   * 
   * @param components
   */
  public static void calculateAndSetMaxWidth(Component... components) {
    double maxWidth = 0d, maxHeight = 0d;
    Dimension curr;
    for (Component component : components) {
      curr = component != null ? component.getPreferredSize() : new Dimension(0, 0);
      if (curr.getWidth() > maxWidth) {
        maxWidth = curr.getWidth();
      }
      if (curr.getHeight() > maxHeight) {
        maxHeight = curr.getHeight();
      }
    }
    for (Component component : components) {
    	if (component != null) {
    		component.setPreferredSize(new Dimension((int) maxWidth, (int) maxHeight));
    	}
    }
  }
  
  /**
   * 
   * @param todo
   */
  public static void processOnSwingEventThread(Runnable todo) {
    processOnSwingEventThread(todo, false);
  }

  /**
   * 
   * @param todo
   * @param wait
   */
  public static void processOnSwingEventThread(Runnable todo, boolean wait) {
    if (todo == null) {
      throw new IllegalArgumentException("Runnable == null");
    }

    if (wait) {
      if (SwingUtilities.isEventDispatchThread()) {
        todo.run();
      } else {
        try {
          SwingUtilities.invokeAndWait(todo);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    } else {
      if (SwingUtilities.isEventDispatchThread()) {
        todo.run();
      } else {
        SwingUtilities.invokeLater(todo);
      }
    }
  }
  
  /**
   * Useful method when launching a program.
   * 
   * @param versionNumber
   * @param yearOfProjectStart
   * @param yearOfRelease
   * @param showVersionNumber
   * @param showCopyright
   */
	public static void configureSplashScreen(String versionNumber,
		int yearOfProjectStart, int yearOfRelease, boolean showVersionNumber,
		boolean showCopyright) {
		int distanceToBorder = 7;
		SplashScreen splash = SplashScreen.getSplashScreen();
		Graphics2D g = splash == null ? null : splash.createGraphics();
		if ((g == null) || (!showVersionNumber && !showCopyright)) { 
			return;
		}
		
		// Decrease font size and set color
		g.setFont(g.getFont().deriveFont((g.getFont().getSize() * 0.8f)));
		g.setColor(ColorPalette.CAMINE_RED);
		
		Rectangle b = splash.getBounds();
		FontMetrics m = g.getFontMetrics();
		
		// Show version number in lower right corner
		if (showVersionNumber) {
			Rectangle2D stringBounds = m.getStringBounds(versionNumber, g);
			g.drawString(versionNumber,
				(int) (b.getWidth() - stringBounds.getWidth() - distanceToBorder),
				(int) (b.getHeight() - distanceToBorder));
		}
		
		// Show copyright in lower left corner
		if (showCopyright) {
			ResourceBundle resources = ResourceManager
					.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
			
			String cMessage = MessageFormat.format(resources.getString("COPYRIGHT_MESSAGE"),
				"", yearOfProjectStart, yearOfRelease).trim();
			int pos = StringUtil.indexOf(cMessage, ",", "\n");
			if (pos > 0) {
				cMessage = cMessage.substring(0, pos);
			}
			
			g.drawString(cMessage, distanceToBorder,
				(int) (b.getHeight() - distanceToBorder));
		}
		
		splash.update();
	}
  
  /**
   * Checks whether the first container contains the second one.
   * 
   * @param c
   * @param insight
   * @return True if c contains insight.
   */
  public static boolean contains(Component c, Component insight) {
    boolean contains = c.equals(insight);
    if ((c instanceof Container) && !contains)
      for (Component c1 : ((Container) c).getComponents()) {
        if (c1.equals(insight)) {
          return true;
        } else {
        	contains |= contains(c1, insight);
        }
      }
    return contains;
  }
  
  /**
   * 
   * @param c
   * @param clazz
   * @return
   */
  @SuppressWarnings("unchecked")
	public static <T extends Component> List<T> find(Container c, Class<T> clazz) {
  	List<T> list = new LinkedList<T>();
  	if (c.getClass().isAssignableFrom(clazz)) {
  		list.add((T) c);
  	}
  	for (Component c1 : c.getComponents()) {
  		if (c1 instanceof Container) {
  			list.addAll(find((Container) c1, clazz));
  		}
  	}
  	return list;
  }
  
  /**
   * Creates a JButton with the given properties. The tool tip becomes an HTML
   * formatted string with a line break after {@link #StringUtil.TOOLTIP_LINE_LENGTH} symbols.
   * 
   * @param icon
   * @param listener
   * @param com
   * @param toolTip
   * @return
   */
  public static JButton createButton(Icon icon, ActionListener listener,
    Object command, String toolTip) {
    JButton button = new JButton();
    if (icon != null) {
      button.setIcon(icon);
    }
    if (listener != null) {
      button.addActionListener(listener);
    }
    if (command != null) {
      button.setActionCommand(command.toString());
    }
    if (toolTip != null) {
      button.setToolTipText(StringUtil.toHTML(toolTip, StringUtil.TOOLTIP_LINE_LENGTH));
    }
    return button;
  }
  
  /**
   * @param text
   * @param icon
   * @param listener
   * @param command
   * @param toolTip
   * @return
   */
  public static JButton createButton(String text, Icon icon,
    ActionListener listener, Object command, String toolTip) {
    JButton button = createButton(icon, listener, command, toolTip);
    if (text != null) {
      button.setText(text);
    }
    return button;
  }
  
  /**
   * Add some buttons to a {@link ButtonGroup}.
   * @param buttons
   * @return
   */
  public static ButtonGroup createButtonGroup(AbstractButton... buttons) {
    ButtonGroup group = new ButtonGroup();
    for (AbstractButton abstractButton : buttons) {
      group.add(abstractButton);
    }
    return group;
  }
	
	/**
	 * Creates a new {@link JButton} with action listeners that invoke a specific
	 * method.
	 * 
	 * @param listener
	 *        the ActionListener to be added
	 * @param command
	 *        the action command for this button, i.e., the item in the menu. This
	 *        will be converted to a {@link String} using the {@link
	 *        String.#toString()} method.
	 * @return A new {@link JButton} with the given features.
	 */
	public static JButton createJButton(ActionListener listener,
		ActionCommand command) {
		return createJButton(listener, command, null);
	}
	
	/**
	 * Creates a new {@link JButton} with action listeners that invoke a specific
	 * method.
	 * 
	 * @param listener
	 *        the ActionListener to be added
	 * @param command
	 *        the action command for this button, i.e., the item in the menu. This
	 *        will be converted to a {@link String} using the {@link
	 *        String.#toString()} method.
	 * @param icon
	 *        the icon of the JButton (can be null)
	 * @return A new {@link JButton} with the given features.
	 */
	public static JButton createJButton(ActionListener listener,
		ActionCommand command, Icon icon) {
		return createJButton(listener, command, icon, null);
	}
  
  /**
   * Creates a {@link JButton}.
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @return
   */
  public static JButton createJButton(ActionListener listener,
    ActionCommand command, Icon icon, Character mnemonic) {
    return (JButton)createJButton(listener, command, icon, mnemonic, JButton.class);
  }
  
  /**
   * 
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @param instance
   *        defaults to {@link JButton}. Other options include, e.g.,
   *        {@link JToggleButton}.
   * @return
   */
  public static AbstractButton createJButton(ActionListener listener,
    ActionCommand command, Icon icon, Character mnemonic,
    Class<? extends AbstractButton> instance) {
    
    // Create an instance
    if (instance == null) {
      instance = JButton.class;
    }
    AbstractButton button;
    try {
      button = instance.newInstance();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Could not instantiate custom button class.", e);
      button = new JButton();
    }
    
    // Configure button
    if (listener != null) {
      button.addActionListener(listener);
    }
    if (mnemonic != null) {
      button.setMnemonic(mnemonic.charValue());
    }
    if (command != null) {
      button.setText(command.getName());
      String toolTip = command.getToolTip();
      if (toolTip != null) {
        button.setToolTipText(StringUtil.toHTML(toolTip, StringUtil.TOOLTIP_LINE_LENGTH));
      }
      button.setActionCommand(command.toString());
    }
    if (icon != null) {
      button.setIcon(icon);
    } else if (command instanceof ActionCommandWithIcon) {
      button.setIcon(((ActionCommandWithIcon)command).getIcon());
    }
    return button;
  }
  
  /**
	 * More convenient method to create a {@link JCheckBox} for a {@link Boolean}
	 * {@link Option}.
	 * 
	 * @param option
	 * @param selected
	 * @param listener
	 * @return
	 * @see #createJCheckBox(String, boolean, Object, String, ItemListener...)
	 */
	public static JCheckBox createJCheckBox(Option<Boolean> option,
		boolean selected, ItemListener... listener) {
		return createJCheckBox(option.getName(), option.toString(), selected,
			option, option.getToolTip(), listener);
	}
  
	/**
	 * More convenient method to create a {@link JCheckBox} for a {@link Boolean}
	 * {@link Option}.
	 * 
	 * @param option
	 * @param prefs
	 * @param listener
	 * @return
	 * @see #createJCheckBox(Option, boolean, ItemListener...)
	 */
	public static JCheckBox createJCheckBox(Option<Boolean> option,
		SBPreferences prefs, ItemListener... listener) {
		return createJCheckBox(option, prefs.getBoolean(option), listener);
	}
	
	/**
  	 * Creates and returns a JCheckBox with all the given properties.
	 * 
	 * @param label
	 * @param name
	 *        The name for the component to be identifiable by the
	 *        {@link ItemListener}. {@code null} allowed.
	 * @param selected
	 * @param command
	 * @param toolTip
	 * @param listener
	 * @return
	 */
	public static JCheckBox createJCheckBox(String label, String name, boolean selected,
		Object command, String toolTip, ItemListener... listener) {
		JCheckBox chkbx = new JCheckBox(label, selected);
		chkbx.setActionCommand(command.toString());
		if (listener.length > 0) {
			for (ItemListener l : listener) {
				chkbx.addItemListener(l);
			}
		}
		if (name != null) {
			chkbx.setName(name);
		}
		chkbx.setToolTipText(StringUtil.toHTML(toolTip, StringUtil.TOOLTIP_LINE_LENGTH));
		return chkbx;
	}
  
	/**
   * 
   * @param command
   * @param selected
   * @param enabled
   * @param listeners
   * @return
   */
	public static JCheckBoxMenuItem createJCheckBoxMenuItem(
		ActionCommand command, boolean selected, boolean enabled,
		ItemListener... listeners) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(command.getName(), selected);
		item.setEnabled(enabled);
		item.setToolTipText(command.getToolTip());
		item.setActionCommand(command.toString());
		for (ItemListener listener : listeners) {
			item.addItemListener(listener);
		}
		return item;
	}
	
	/**
	 * Creates a {@link JComboBox} with the given properties.
	 * 
	 * @param items
	 * @param renderer
	 *        {@code null} allowed.
	 * @param enabled
	 * @param name
	 *        {@code null} allowed.
	 * @param tooltip
	 *        {@code null} allowed.
	 * @param selectedIndex
	 * @param listeners
	 *        {@code null} allowed.
	 * @return
	 */
	public static JComboBox createJComboBox(Object[] items,
		ListCellRenderer renderer, boolean enabled, String name, String tooltip,
		int selectedIndex, ItemListener... listeners) {
		JComboBox box = new JComboBox(items);
		if (renderer != null) {
			box.setRenderer(renderer);
		}
		box.setEnabled(enabled);
		if ((name != null) && (name.length() > 0)) {
			box.setName(name);
		}
		if ((tooltip != null) && (tooltip.length() > 0)) {
			box.setToolTipText(StringUtil.toHTML(tooltip, StringUtil.TOOLTIP_LINE_LENGTH));
		}
		if ((-1 < selectedIndex) && (items != null)
				&& (selectedIndex < items.length)) {
			box.setSelectedIndex(selectedIndex);
		}
		if (listeners != null) {
			for (ItemListener listener : listeners) {
				box.addItemListener(listener);
			}
		}
		return box;
	}
	
	/**
	 * Creates a {@link JComboBox} with the given properties.
	 * 
	 * @param items
	 * @param renderer
	 *        {@code null} allowed.
	 * @param enabled
	 * @param name
	 *        {@code null} allowed.
	 * @param tooltip
	 *        {@code null} allowed.
	 * @param listeners
	 *        {@code null} allowed.
	 * @return
	 */
	public static JComboBox createJComboBox(Object[] items,
		ListCellRenderer renderer, boolean enabled, String name, String tooltip,
		ItemListener... listeners) {
		return createJComboBox(items, renderer, enabled, name, tooltip, 0,
			listeners);
	}
  
  /**
   * Creates a new {@link JDropDownButton} using the entries from the given
   * {@link ResourceBundle}. It is assumed that the bundle contains at least the
   * key specified by the given <code>name</code>. It then tries to obtain an
   * associated tooltip by first looking for the key
   * <code>name + "_TOOLTIP"</code>. If such a key is present, it uses this
   * tooltip. Otherwise it will try to split the text of the
   * {@link JDropDownButton} using the separator character <code>';'</code>.
   * Remember, the text is defined by the entry in the {@link ResourceBundle}
   * for the <code>name</code> key. If it is not possible obtain a tooltip by
   * splitting or concatanation of the name with the suffix
   * <code>"_TOOLTIP"</code>, no tooltip will be set.
   * 
   * @param name
   *        the name (not the {@link JDropDownButton#getText()}) of the
   *        {@link JDropDownButton} to be created, also used as look-up key in
   *        the given {@link ResourceBundle} and as the name of the action
   *        command for the new button.
   * @param bundle
   *        where to look keys up.
   * @param menu
   *        the options to be offered by the button.
   * @return a new {@link JDropDownButton} with many properties defined.
   */
  public static JDropDownButton createJDropDownButton(String name,
    ResourceBundle bundle, JPopupMenu menu) {
    JDropDownButton button = new JDropDownButton(bundle.getString(name), menu);
    String tooltip = null;
    String key = name + "_TOOLTIP";
    if (bundle.containsKey(key)) {
      tooltip = bundle.getString(key);
    } else if (button.getText().contains(";")) {
      String description[] = button.getText().split(";");
      button.setText(description[0]);
      tooltip = description[1];
    }
    if (tooltip != null) {
			button.setToolTipText(StringUtil.toHTML(tooltip,
				StringUtil.TOOLTIP_LINE_LENGTH));
    }
    button.setName(name);
    button.setActionCommand(name);
    return button;
  }
  
  /**
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @return
   */
  public static JFileChooser createJFileChooser(String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode) {
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
    chooser.setMultiSelectionEnabled(multiSelectionAllowed);
    chooser.setFileSelectionMode(mode);
    return chooser;
  }
  
  /**
   * Convenience method that creates a {@link JFileChooser} using the given
   * arguments.
   * 
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return
   */
  public static JFileChooser createJFileChooser(String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode);
    if (filter != null) {
			for (int i = filter.length; i > 0; i--) {
				chooser.addChoosableFileFilter(filter[i - 1]);
			}
      if (filter.length > 0) {
        chooser.setFileFilter(filter[0]);
      }
    }
    return chooser;
  }
  
  /**
   * Convenience method that creates a {@link JLabel} using the given
   * localization ID to retrieve the text and the tooltip text from the given
   * resource bundle.
   * 
   * @param labelLocalizationKey the name (not the {@link JLabel#getText()}) of
   *        the {@link JLabel} to be created, also used as look-up key in
   *        the given {@link ResourceBundle}.
   * @param bundle where to look up the keys
   * @return a new {@link JLabel}
   */
  public static JLabel createJLabel(String labelLocalizationKey, ResourceBundle bundle) {
    JLabel label = new JLabel();
    String text;
    try {
      text = bundle.getString(labelLocalizationKey);
    }
    catch( MissingResourceException e ) {
      logger.log(Level.WARNING,
          "Couldn't find localized string for label '" + labelLocalizationKey
          + "'. Please report this bug.",
          e);
      text = labelLocalizationKey;
    }
    
    String tooltip = null;
    String key = labelLocalizationKey + "_TOOLTIP";
    if (bundle.containsKey(key)) {
      tooltip = bundle.getString(key);
    } else if (text.contains(";")) {
      String description[] = text.split(";");
      label.setText(description[0]);
      tooltip = description[1];
    } else {
      label.setText(text);
    }
    if (tooltip != null) {
      label.setToolTipText(StringUtil.toHTML(tooltip,
        StringUtil.TOOLTIP_LINE_LENGTH));
    }
    return label;
  }
  
  /**
   * Creates a new JMenuItem with the given text as label and the mnemonic. All
   * given menu items are added to the menu.
   * 
   * @param text
   * @param mnemonic
   * @param menuItems
   *        instances of {@link JMenuItem} or {@link JSeparator}. Other
   *        {@link Object}s are ignored.
   * @return
   */
  public static JMenu createJMenu(String text, char mnemonic, Object... menuItems) {
    JMenu menu = new JMenu(text);
    menu.setMnemonic(mnemonic);
    for (Object item : menuItems) {
    	if (item == null) {
    		continue;
    	} else if (item instanceof JMenuItem) {
        menu.add((JMenuItem) item);
      } else if (item instanceof JSeparator) {
        menu.add((JSeparator) item);
      } else if (item instanceof JMenuItem[]) {
        for (JMenuItem i : (JMenuItem[]) item) {
          menu.add(i);
        }
      } else if (item instanceof JMenu) {
        menu.add((JMenu) item);
      }
    }
    return menu;
  }
  
  /**
   * Creates a new {@link JMenu} with the given menu items and sets the first
   * letter in the menu's name as mnemonic.
   * 
   * @param text
   * @param menuItems
   * @return
   */
  public static JMenu createJMenu(String text, Object... menuItems) {
    return createJMenu(text, text.charAt(0), menuItems);
  }
  
  /**
   * 
   * @param text
   * @param tooltip
   * @param menuItems
   * @return
   */
  public static JMenu createJMenu(String text, String tooltip, Object... menuItems) {
    JMenu menu = createJMenu(text, menuItems);
    if ((tooltip != null) && (tooltip.length() > 0) && (!tooltip.equals(text))) {
    	if (isMacOSX()) {
    		menu.setToolTipText(tooltip);
    	} else {
        menu.setToolTipText(StringUtil.toHTML(tooltip, StringUtil.TOOLTIP_LINE_LENGTH));
    	}
    }
    return menu;
  }

  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command) {
  	return createJMenuItem(listener, command, true);
  }
  
  /**
   * 
   * @param listener
   * @param command
   * @param enabled
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, boolean enabled) {
  	Icon icon = (command instanceof ActionCommandWithIcon) ? ((ActionCommandWithIcon) command).getIcon() : null;
    return createJMenuItem(listener, command, icon, enabled);
  }
  
  /**
   * @param listener
   * @param command
   * @param icon
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon) {
    return createJMenuItem(listener, command, icon, true);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, char mnemonic) {
    return createJMenuItem(listener, command, icon, mnemonic, true);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @param enabled
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, char mnemonic, boolean enabled) {
    return createJMenuItem(listener, command, icon, null, Character.valueOf(mnemonic), enabled);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param enabled
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, boolean enabled) {
    return createJMenuItem(listener, command, icon, null, null, enabled);
  }
  
  
  /**
   * Creates a new item for a {@link JMenu}.
   * 
   * @param listener
   *        the ActionListener to be added
   * @param command
   *        the action command for this button, i.e., the item in the menu. This
   *        will be converted to a {@link String} using the {@link
   *        String.#toString()} method.
   * @param icon
   *        the icon of the JMenuItem (can be null)
   * @param keyStroke
   *        the KeyStroke which will serve as an accelerator
   * @return A new {@link JMenuItem} with the given features.
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke keyStroke) {
  	return createJMenuItem(listener, command, icon, keyStroke, true);
  }
  
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke keyStroke, boolean enabled) {
  	return createJMenuItem(listener, command, icon, keyStroke, null, enabled);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic) {
    return createJMenuItem(listener, command, icon, ks, mnemonic, true);
  }
  
  /**
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @param enabled
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic,
    boolean enabled) {
    return createJMenuItem(listener, command, icon, ks, mnemonic, JMenuItem.class, enabled);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @param type may also be {@link JCheckBoxMenuItem} or other derivates
   * of {@link JMenuItem}.
   * @param enabled 
   * @return
   */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic,
		Class<? extends JMenuItem> type, boolean enabled) {
		if (type == null) {
			type = JMenuItem.class;
		}
		JMenuItem item;
		try {
			item = type.newInstance();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot instantiate class.", e);
			item = new JMenuItem();
		}
		
		if (ks != null) {
			item.setAccelerator(ks);
		}
		if (listener != null) {
			item.addActionListener(listener);
		}
		if (mnemonic != null) {
			item.setMnemonic(Character.getNumericValue(mnemonic.charValue()));
		}
		if (command != null) {
			item.setText(command.getName());
			String toolTip = command.getToolTip();
			if (toolTip != null) {
				if (isMacOSX()) {
					item.setToolTipText(toolTip);
				} else {
					item.setToolTipText(StringUtil.toHTML(toolTip, StringUtil.TOOLTIP_LINE_LENGTH));
				}
			}
			item.setActionCommand(command.toString());
			item.setName(command.toString());
		}
		if (icon != null) {
			item.setIcon(icon);
		} else if (command instanceof ActionCommandWithIcon) {
			item.setIcon(((ActionCommandWithIcon) command).getIcon());
		}
		
		item.setEnabled(enabled);
		
		return item;
	}
  
  
  /**
   * @param listener
   * @param command
   * @param keyStroke
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, KeyStroke keyStroke) {
    return createJMenuItem(listener, command, null, keyStroke);
  }
  
  /**
   * 
   * @param listener
   * @param command
   * @param mnemonic
   * @param enabled
   * @return
   */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommandWithIcon command, char mnemonic, boolean enabled) {
		return createJMenuItem(listener, command, (Icon) null, mnemonic, enabled);
	}
	
	/**
	 * 
	 * @param listener
	 * @param command
	 * @param ks
	 * @param mnemonic
	 * @param enabled
	 * @return
	 */
	public static JMenuItem createJMenuItem(ActionListener listener,
		ActionCommandWithIcon command, KeyStroke ks, char mnemonic, boolean enabled) {
		return createJMenuItem(listener, command, command.getIcon(), ks, mnemonic,
			enabled);
	}
  
  /**
	 * Creates a {@link JSpinner} with the given properties.
	 * 
	 * @param model
	 * @param name
	 *        {@code null} allowed.
	 * @param tooltip
	 *        {@code null} allowed.
	 * @param enabled
	 * @param cl
	 *        {@code null} allowed.
	 * @return
	 */
	public static JSpinner createJSpinner(SpinnerModel model, String name,
		String tooltip, boolean enabled, ChangeListener... cl) {
		JSpinner spinner = new JSpinner(model);
		if ((name != null) && (name.length() > 0)) {
			spinner.setName(name);
		}
		if ((tooltip != null) && (tooltip.length() > 0)) {
			spinner.setToolTipText(StringUtil.toHTML(tooltip, StringUtil.TOOLTIP_LINE_LENGTH));
		}
		spinner.setEnabled(enabled);
		if (cl != null) {
			for (ChangeListener c : cl) {
				spinner.addChangeListener(c);
			}
		}
		return spinner;
	}
  
  /**
	 * Creates an enabled {@link JSpinner} with the given properties.
	 * 
	 * @param model
	 * @param name
	 * @param tooltip
	 * @param listener
	 * @return
	 * @see #createJSpinner(SpinnerModel, String, String, boolean,
	 *      ChangeListener...)
	 */
	public static JSpinner createJSpinner(SpinnerModel model, String name,
		String tooltip, ChangeListener... listener) {
		return createJSpinner(model, name, tooltip, true, listener);
	}
  
  /**
   * Create a panel for a component and adds a title to it. 
   */
  public static JComponent createTitledPanel(JComponent content, String title) {
    /*JPanel panel = new JPanel();
	    JLabel label = new JLabel(title);
	    label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	    label.setBackground(new Color(231, 219,182));
	    label.setOpaque(true);
	    label.setForeground(Color.DARK_GRAY);
	    label.setFont(label.getFont().deriveFont(Font.BOLD));
	    label.setFont(label.getFont().deriveFont(13.0f));
	    panel.setLayout(new BorderLayout());
	    panel.add(label, BorderLayout.NORTH);
	    panel.add(content, BorderLayout.CENTER);*/
    //return panel;
    
    content.setBorder(BorderFactory.createTitledBorder(title));
    
    return content;
  }
  
  /**
   * Searches for the parent {@link java.awt.Window} of the given component c.
   * Checks if this Window contains a #{@link javax.swing.AbstractButton} which
   * is called "Ok" and disables this button.
   * @param c
   * @return true if and only if an ok-button has been disabled. Else, false.
   */
  public static boolean disableOkButton(Container c) {
    // Search for ok button and check if all other are enabled.
    if (c != null) {
      Component okButton = getOKButton(c, true);
      if (okButton != null) {
        okButton.setEnabled(false);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Searches for the parent {@link java.awt.Window} of the given component c.
   * Checks if this Window contains a #{@link javax.swing.AbstractButton} which
   * is called "Ok" and enables this button.
   * @param c
   * @return true if and only if an ok-button has been enabled. Else, false.
   */
  public static synchronized boolean enableOkButton(Container c) {
    // Search for ok button and enable.
    if (c!=null) {
      // c is now a Window.
      Component okButton = getOKButton(c, true);
      if (okButton!=null) {
        okButton.setEnabled(true);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Checks if this "c" contains a #{@link javax.swing.AbstractButton} which
   * is called "OK" and enables this button if and only if it a) exists and is
   * disabled and b) all other elements on this container and all
   * contained containers are enabled.
   * @param c
   * @return true if and only if an ok-button has been enabled. Else, false.
   */
  public static synchronized boolean enableOkButtonIfAllComponentsReady(Container c) {
    return enableOkButtonIfAllComponentsReady(c, false);
  }
	
	/**
   * Simply checks if all elements on "c" are enabled and if yes, the given "okButton" will
   * be enabled. Else, the current state stays untouched.
   * 
   * In opposite to {@link #enableOkButtonIfAllComponentsReady(Component)}, this function 
   * uses the given okButton.
   * @param c
   * @param okButton
   * @return
   */
  public static synchronized boolean enableOkButtonIfAllComponentsReady(Container c, AbstractButton okButton) {
    if (isEnabled(c)) {
      okButton.setEnabled(true);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Checks if this "c" contains a #{@link javax.swing.AbstractButton} which
   * is called "OK" and enables this button if and only if it a) exists and is
   * disabled and b) all other elements on this container and all
   * contained containers are enabled.
   * @param c
   * @param searchInWholeWindow if true, will not only look inside <code>c</code>
   * for an ok button, but in the whole windows.
   * @return
   */
  public static synchronized boolean enableOkButtonIfAllComponentsReady(Container c, boolean searchInWholeWindow) {
    
    // Search for ok button and check if all other are enabled.
    if (c != null) {
      Component okButton = getOKButton(c, searchInWholeWindow);
      if (okButton != null) {
        boolean previousState = okButton.isEnabled();
        okButton.setEnabled(true);
        if (isEnabled(c)) {
          return true;
        } else {
          okButton.setEnabled(previousState);
          return false;
        }
      }
    }
    return false;
  }
  
  
  /**
	 * 
	 * @param menu
	 * @param action
	 * @return
	 * @see #find(Container, ActionCommand)
	 */
	public static JMenuItem find(JMenu menu, Object action) {
		JMenuItem item;
		for (int i = 0; i < menu.getItemCount(); i++) {
			item = menu.getItem(i);
			if ((item.getActionCommand() != null)
					&& item.getActionCommand().equals(action.toString())) {
				return item;
			} else if (item instanceof JMenu) {
				JMenuItem subItem = find((JMenu) item, action);
				if (subItem != null) {
					return subItem;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param menuBar
	 * @param command
	 * @return
	 */
	public static JMenuItem find(JMenuBar menuBar, Object command) {
		JMenu menu;
		JMenuItem item;
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			menu = menuBar.getMenu(i);
			if (menu.getActionCommand().equals(command)) {
				return menu;
			}
			item = find(menu, command);
			if (item != null) {
				return item;
			}
		}
		return null;
	}
  
  /**
   * Tries to get the lokalized cancel Button Message, as it occurs
   * in the UIManager (e.g. JOptionPane).
   * @return
   */
  public static String getCancelButtonText() {
    
    // First, get it from the UI Manager
    Object cancel = UIManager.get("OptionPane.cancelButtonText");
    if (cancel!=null) return cancel.toString();
    
    // Second, try to get it from the internal resources
    ResourceBundle resource = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
    cancel = resource == null ? null: resource.getString("CANCEL");
    if (cancel != null) {
      if (cancel.toString().contains(";")) {
      	return cancel.toString().split(";")[0];
      } else {
      	return cancel.toString();
      }
    }
    
    return "Cancel";
  }
  
  /**
   * Computes and returns the dimension, i.e., the size of a given icon.
   * 
   * @param icon
   *        an icon whose dimension is required.
   * @return The dimension of the given icon.
   */
  public static Dimension getDimension(Icon icon) {
    return icon == null ? new Dimension(0, 0) : new Dimension(icon
      .getIconWidth(), icon.getIconHeight());
  }
  
  /**
   * @param jMenuBar
   * @param fileOpenRecent
   * @return
   */
  public static JMenuItem getJMenuItem(JMenuBar menuBar, Object command) {
    JMenu menu;
    JMenuItem item;
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      menu = menuBar.getMenu(i);
      if ((menu.getActionCommand() != null)
          && (menu.getActionCommand().equals(command.toString()))) { 
      	return menu; 
      }
      for (int j = 0; j < menu.getItemCount(); j++) {
        item = menu.getItem(j);
				if (item != null) {
					if ((item.getActionCommand() != null)
							&& (item.getActionCommand().equals(command.toString()))) { 
						return item; 
					}
					if (item instanceof JMenu) {
						item = find((JMenu) item, command);
						if (item != null) {
							return item;
						}
					}
				}
      }
    }
    return null;
  }
  
  /**
   * Determines the maximal preferred size of the two given elements and creates
   * a new instance of {@link Dimension} with exactly this size and returns it.
   * 
   * @param a
   * @param b
   * @return a new instance of {@link Dimension} of maximal width and height of
   *         the two given {@link Container}s.
   */
  public static Dimension getMaxPreferredSize(Container a, Container b) {
    Dimension prefA = a.getPreferredSize();
    Dimension prefB = b.getPreferredSize();
    return new Dimension((int) Math.max(prefA.getWidth(), prefB.getWidth()),
      (int) Math.max(prefA.getHeight(), prefB.getHeight()));
  }
  
  /**
   * Return a best suited string to describe this {@link Throwable}.
   * Prefers in this order: <ol><li>LocalizedMessage
   * <li>LocalizedMessage of the Cause
   * <li>message<li>toString()<li>NULL</ol>
   * @param exc
   * @return
   */
  private static String getMessage(Throwable exc) {
    if (exc==null) return "NULL";
    String msg = exc.getLocalizedMessage();
    if ((msg == null) && (exc.getCause() != null)) {
      msg = exc.getCause().getLocalizedMessage();
    }
    if (msg == null) msg = exc.getMessage();
    if (msg == null) msg = exc.toString(); // Axis Faults have all messages null
    if (msg == null) msg = "NULL";
    return msg;
  }
  
  /**
   * 
   * @param c
   * @param inspectWholeWindow
   * @return
   */
  // TODO: rename to findOKButton
  public static AbstractButton getOKButton(Container c, boolean inspectWholeWindow) {
    // Search for parent window
    Container oldC = c;
    if (inspectWholeWindow) {
    	c = getParentWindow(c);
    }
    if (c == null) {
    	c = oldC;
    }
    
    // Search for ok button and enable.
    if (c != null) {
      return (AbstractButton) searchFor(c, AbstractButton.class, "getText", getOkButtonText());
    } else {
      return null;
    }
  }
  
  /**
   * Tries to get the lokalized ok Button Message, as it occurs
   * in the UIManager (e.g. JOptionPane).
   * @return
   */
  public static String getOkButtonText() {
    
    // First, get it from the UI Manager
    Object ok = UIManager.get("OptionPane.okButtonText");
    if (ok!=null) return ok.toString();
    
    // Second, try to get it from the internal resources
    ResourceBundle resource = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
    ok = resource == null ? null : resource.getString("OK");
    if (ok != null) {
      if (ok.toString().contains(";")) {
      	return ok.toString().split(";")[0];
      } else {
      	return ok.toString();
      }
    }
    
    return "OK";
  }
  
  /**
   * Searches for the parent {@link Window} of the given component c.
   * @param c
   * @return the window if found, or null if not found.
   */
  public static Window getParentWindow(Component c) {
    while (c!=null) {
      if (c instanceof Window) {
        return (Window) c;
      }
      c = c.getParent();
    }
    return null;
  }
  
  /**
   * Searches for the parent {@link Dialog} of the given component c.
   * @param c
   * @return the Dialog if found, or null if not found.
   */
  public static Dialog getParentDialog(Component c) {
    while (c!=null) {
      if (c instanceof Dialog) {
        return (Dialog) c;
      }
      c = c.getParent();
    }
    return null;
  }
  
  /**
   * The JVM command line allows to show splash screens. If the user made
   * use of this functionality, the following code will hide the screen.
   * Otherwise, this function does nothing.
   */
  public static void hideSplashScreen() {
    try {
      final SplashScreen splash = SplashScreen.getSplashScreen();
      if (splash == null) {
        return;
      }
      splash.close();
    } catch (Throwable t) {
      // intentionally left blank
    }
  }


	/**
   * @param g
   * @param incrementBy
   * @return
   */
  public static Font incrementFontSize(Font g, int incrementBy) {
    return g.deriveFont((float) (g.getSize() + incrementBy));
  }
  
  /**
   * Initializes the look and feel.
   */
  public static void initLaF() {
    ImageTools.initImages();
  	
		// 15 s for tooltips to be displayed
		ToolTipManager.sharedInstance().setDismissDelay(15000);
		
		// The user may have specified a custom LaF on the command line
		// with "-Dswing.defaultlaf=XXX".
		String def = System.getProperty("swing.defaultlaf");
		Class<?> cl = null;
		try {
		  cl = Class.forName(def);
		} catch (Throwable t) {}
		
		// Only if the user specified either no LaF or an invalid one, try to
		// load an operating system dependent nice LaF.
		if (def==null || cl==null) {
		  try {
		    UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
		    String osName = System.getProperty("os.name");
		    if (osName.equals("Linux") || osName.equals("FreeBSD")) {
		      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		      // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		    } else if (isMacOSX()) {
		      String osVersion = System.getProperty("os.version");
		      if (osVersion.startsWith("10.4") || osVersion.startsWith("10.5") || osVersion.startsWith("10.6")) {
		        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
		      } else {
		        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	
		      }
		    } else if (osName.contains("Windows")) {
		      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		    } else {
		      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    }
		  } catch (Throwable e) {
		    try {
		      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } catch (Throwable e1) {
		      // If Nimbus is not available, you can set the GUI to another look
		      // and feel.
		      // Native look and feel for Windows, MacOS X. GTK look and
		      // feel for Linux, FreeBSD
		      try {
		        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		          if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		          }
		        }
		      } catch (Throwable exc) {
		        showErrorMessage(null, exc.getLocalizedMessage());
		      }
		    }
		  }
		}
  }
  
  /**
	 * Initializes the look and feel. This method is only useful when the calling
	 * class contains the main method of your program and is also derived from this
	 * class ({@link GUITools}). The preferred way to set up your application would
	 * be to let it extend {@link Launcher}.
	 * 
	 * @param title
	 *        the title to be displayed in the xDock in case of MacOS. Note that
	 *        this title can only be displayed if this method belongs to the class
	 *        that has the main method (or is in a super class of it), i.e., in
	 *        order to make use of this method in a proper way, you have to extend
	 *        this {@link GUITools} and to put the main method into your derived
	 *        class. From this main method you then have to call this method.
	 *        Otherwise, this title will not have any effect. An easier way to set
	 *        the xDock properties for your application would be to use the
	 *        {@link Launcher} class that already correctly sets all properties
	 *        for you.
	 */
  public static void initLaF(String title) {
		if (isMacOSX()) {
			Properties p = System.getProperties();
			// also use -Xdock:name="Some title" -Xdock:icon=path/to/icon on command line
			p.setProperty("apple.awt.graphics.EnableQ2DX", "true");
			p.setProperty("apple.laf.useScreenMenuBar", "true");
			p.setProperty("com.apple.macos.smallTabs", "true");
			p.setProperty("com.apple.macos.useScreenMenuBar", "true");
			if ((title != null) && (title.length() > 0)) {
				p.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
			}
			p.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			p.setProperty("com.apple.mrj.application.live-resize", "true");
		}
		initLaF();
  }
  
  /**
   * Checks, if all elements on c are enabled.
   * @param c
   * @return true if and only if c and all components on c are enabled.
   */
  public static boolean isEnabled(Container c) {
    Component inside;
    boolean enabled = c.isEnabled();
    for (int i = 0; i < c.getComponentCount(); i++) {
      inside = c.getComponent(i);
      enabled &= inside.isEnabled();
      if (!enabled) return false; // shortcut.
      if (inside instanceof Container) {
        enabled&=isEnabled((Container) inside);
      }
      if (!enabled) return false; // shortcut.
    }
    return enabled;
  }
  
  /**
   * 
   * @return
   */
  public static boolean isMacOSX() {
		return (System.getProperty("mrj.version") != null)
				|| (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1);
	}
  
  /**
   * Create an open file dialog that let's the user
   * select any file.
   * @param parent
   * @param dialogTitle allows to change the title of the
   * file dialog.
   * @param acceptURLs if true, will also accept any http
   * or ftp url as input and return this url.
   * @return
   */
  public static String openFileDialog(final Component parent,
    String dialogTitle, boolean acceptURLs) {
    JFileChooser chooser = createJFileChooser(null, true,
      false, JFileChooser.FILES_ONLY, (FileFilter)null);
    if (dialogTitle!=null) chooser.setDialogTitle(dialogTitle);
    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      String fString = f.getPath();
      if (acceptURLs) {
        // Stupid dialog converts the url to something like
        // C:\Users\Documents\http:\www.broadinstitute.org\c3.gmt
        int pos = fString.indexOf("http:");
        int pos2 = fString.indexOf("ftp:");
        pos = Math.max(pos, pos2);
        if (pos>=0) {
          String url = fString.substring(pos, fString.length());
          if (StringUtil.fileSeparator()=='\\') {
            url = url.replace('\\', '/');
          }
          url = url.replaceFirst(Pattern.quote("http:/"), "http://");
          url = url.replaceFirst(Pattern.quote("ftp:/"), "ftp://");
          return url;
        }
      }
      if (!f.canRead()) {
        showNowReadingAccessWarning(parent, f);
      } else {
        return f.getPath();
      }
    }
    return null;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return null if for some reason the no {@link File} has been selected or
   *         the {@link File} cannot be read, else the selected {@link File}.
   */
  public static File[] openFileDialog(final Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    File[] ret = null;
    JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode, filter);
    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      if (multiSelectionAllowed) {
        ret = chooser.getSelectedFiles();
      } else {
        ret = new File[] {chooser.getSelectedFile()};
      }
      for (File f: ret) {
        if (!f.canRead()) {
          showNowReadingAccessWarning(parent, f);
          return null;
        }
      }
    }
    return ret;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return null if for some reason the no {@link File} has been selected or
   *         the {@link File} cannot be read, else the selected {@link File}.
   */
  public static File openFileDialog(final Component parent, String dir,
    boolean allFilesAcceptable, int mode, FileFilter... filter) {
    File files[] = openFileDialog(parent, dir, false, allFilesAcceptable, mode, filter);
    if ((files != null) && (files.length == 1)) {
    	return files[0];
    }
    return null;
  }
  
  /**
   * @param parent
   * @param out
   * @return
   */
  public static boolean overwriteExistingFile(Component parent, File out) {
    return GUITools.overwriteExistingFileDialog(parent, out) == JOptionPane.YES_OPTION;
  }
  
  /**
   * Shows a dialog that asks whether or not to overwrite an existing file and
   * returns the answer from JOptionPane constants.
   * 
   * @param parent
   * @param out
   * @return An integer representing the user's choice.
   */
  public static int overwriteExistingFileDialog(Component parent, File out) {
    ResourceBundle resource = ResourceManager
    .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
    return JOptionPane.showConfirmDialog(parent, StringUtil.toHTML(String
      .format(resource.getString("OVERRIDE_EXISTING_FILE_QUESTION"),
        StringUtil.changeFirstLetterCase(resource
          .getString(out.isFile() ? "THE_FILE" : "THE_DIRECTORY"), true,
          false), out.getName()), StringUtil.TOOLTIP_LINE_LENGTH), resource
          .getString("OVERRIDE_EXISTING_FILE_TITLE"), JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
  }
  
  /**
   * Recursively looks for the first parent window of the
   * given component and calls the "pack()" method on it.
   * @param parent
   */
  public static void packParentWindow(Component parent) {
    Component c = parent;
    while (c!=null) {
      if (c instanceof Window) {
        ((Window)c).pack();
        //((Window)c).doLayout();
        break; // Dialogs, shown on top of windows. If no break here, window gets also packed.
      }
      c = c.getParent();
    }
  }
  
  /**
   * Remove all elements from this container and all childs, that
   * have a name that is equal to the given one.
   * @param container
   * @param nameToRemove
   * @return true if at least one element has been removed.
   */
  public static boolean removeAllComponentsWithName(Container container, String nameToRemove) {
    boolean removed = false;
    
    for (Component c: container.getComponents()) {
      String name = c.getName();
      
      if (name!=null && name.equals(nameToRemove)) {
        container.remove(c);
        removed=true;
      } else if (c instanceof Container) {
        // Recurse
        removed|=removeAllComponentsWithName((Container) c,nameToRemove);
      }
    }
    return removed;
  }
  
  /**
   * Replaces two components. Tries to preserve the layout while replacing the
   * two components on the parent of the oldOne.
   * 
   * @param oldOne
   * @param newOne
   */
  public static void replaceComponent(JComponent oldOne, JComponent newOne) {
    if (oldOne == null || oldOne.getParent() == null) {
      // All I can do here is replacing the variables...
      oldOne = newOne;
      return;
    }
    
    Container target = oldOne.getParent();
    LayoutManager lm = target.getLayout();
    
    // Try to replace by setting same layout as old component
    if (lm instanceof BorderLayout) {
      Object c = ((BorderLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((BorderLayout) lm).addLayoutComponent(newOne, c);
      
    } else if (lm instanceof GridBagLayout) {
      Object c = ((GridBagLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((GridBagLayout) lm).addLayoutComponent(newOne, c);
      
    } else {
      // Layouts have no constraints. Just set the correct index.
      boolean replaced = false;
      for (int i = 0; i < target.getComponents().length; i++) {
        if (target.getComponent(i).equals(oldOne)) {
          target.remove(i);
          target.add(newOne, i);
          replaced = true;
          break;
        }
      }
      
      // element not found? still add the new one.
      if (!replaced) {
        target.remove(oldOne);
        target.add(newOne);
      }
      
    }
  }
  
  /**
   * Shows a generic Save file Dialog.
   * 
   * @param parentComp
   * @return null if no {@link File} has been selected for any reason or the
   *         selected {@link File}.
   */
  public static File saveFileDialog(Component parentComp) {
    return saveFileDialog(parentComp, System.getProperty("user.dir"), true,
      false, JFileChooser.FILES_ONLY);
  }
  
  /**
   * 
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param checkFile
   *        decides whether to check if the file is writable and if existing
   *        files are to be overwritten. If false, no such check will be
   *        performed.
   * @param mode  e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return
   */
  public static File saveFileDialog(Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed,
    boolean checkFile, int mode, FileFilter... filter) {
    JFileChooser fc = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode, filter);
    if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
    	// FIXME: This only checks one file. If multiselection is allowed, multiple files may be returned!
      File f = fc.getSelectedFile();
      if (f.exists()) {
        if (checkFile && !f.canWrite()) {
          showNowWritingAccessWarning(parent, f);
        } else if (f.isDirectory()
            || (checkFile && GUITools.overwriteExistingFile(parent, f))) {
          return f;
        } else if (!checkFile) { return f; }
      } else {
        return f;
      }
    }
    return null;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode
   * @param filter
   * @return null if no {@link File} has been selected for any reason or the
   *         selected {@link File}.
   */
  public static File saveFileDialog(Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    return saveFileDialog(parent, dir, allFilesAcceptable,
      multiSelectionAllowed, true, mode, filter);
  }
  
  /**
   * Scrolls to the top of the given <code>scrollPanel</code>.
   * @param scrollPanel
   */
  public static void scrollToTop(final JScrollPane scrollPanel) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() { 
        synchronized (scrollPanel) {
          scrollPanel.getVerticalScrollBar().setValue(0);
          scrollPanel.getHorizontalScrollBar().setValue(0);
        }
      }
    });
  }
  
  /**
   * Searches recursively on "parent" and all components on "parent" for a component of
   * class "searchFor", with a Method (without inputs) called "methodName", that returns an
   * object that equals "retVal".
   * If "methodName" is null, this function will simply return the first instance of "searchFor"
   * on "parent".
   * @param parent
   * @param searchFor
   * @param methodName
   * @param retVal
   * @return the component, if found.
   */
  public static Component searchFor(Container parent, Class<?> searchFor, String methodName, Object retVal) {
    for (int i=0; i<parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      
      // Is c the component we are looking for?
      if (searchFor.isAssignableFrom(c.getClass())) {
        if (methodName!=null) {
          Object ret = Reflect.invokeIfContains(c, methodName);
          if (ret==null && retVal==null || ret.equals(retVal)) {
            return c;
          } else if (ret!=null && retVal!=null && ret instanceof String
              && ((String)ret).equalsIgnoreCase(retVal.toString())) {
            return c;
          }
        } else {
          return c;
        }
      }
      
      // Recurse further.
      if (c instanceof Container) {
        Component c2 = searchFor((Container)c, searchFor, methodName, retVal);
        if (c2!=null) return c2;
      }
    }
    return null;
  }
  
  /**
   * @param c
   * @param color
   */
  public static void setAllBackground(Container c, Color color) {
    c.setBackground(color);
    Component children[] = c.getComponents();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof Container)
        setAllBackground((Container) children[i], color);
      children[i].setBackground(color);
    }
  }


  /**
   * @param c
   * @param enabled
   */
  public static void setAllEnabled(Container c, boolean enabled) {
    Component children[] = c.getComponents();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof Container) {
        setAllEnabled((Container) children[i], enabled);
      }
      children[i].setEnabled(enabled);
    }
  }
  
  /**
   * Tries to recursively find instances of {@link AbstractButton} within the
   * given container and sets their enabled status to the given value.
   * 
   * @param state
   * @param c
   * @param command ActionCommand(s) of items to change the enabled state. May NOT be null!
   */
  public static void setEnabled(boolean state, Container c, Object... command) {
    Component inside;
    for (int i = 0; i < c.getComponentCount(); i++) {
      inside = c.getComponent(i);
      if (inside instanceof Container) {
        setEnabled(state, (Container) inside, command);
      } // Don't do "else if" here. JButtons are containers AND buttons itself! 
      if (inside instanceof AbstractButton) {
        String com = ((AbstractButton) inside).getActionCommand();
        if (com==null) continue;
        for (Object cmd: command) {
          if (com.toString().equals(cmd.toString())) {
            inside.setEnabled(state);
            continue;
          }
        }
      }
    }
  }
  
  /**
   * Enables or disables actions that can be performed by a program, i.e.,
   * all menu items and buttons that are associated with the given actions are
   * enabled or disabled.
   * 
   * Originates from SBMLsqueezer.
   * 
   * @param state
   *        if true buttons, items etc. are enabled, otherwise disabled.
   * @param menuBar
   *        the JMenuBar that contains the menu items that should be updated
   * @param toolbar
   *        the JToolBar that contains the menu items that should be updated
   * @param commands
   *        the list of commands that identify the components to be updated
   */
  public static void setEnabled(boolean state, JMenuBar menuBar,
    JToolBar toolbar, Object... commands) {
    int i, j;
    Set<String> setOfCommands = new HashSet<String>();
    for (Object command : commands) {
      setOfCommands.add(command.toString());
    }
    if (menuBar != null) {
      for (i = 0; i < menuBar.getMenuCount(); i++) {
        JMenu menu = menuBar.getMenu(i);
        for (j = 0; j < menu.getItemCount(); j++) {
          JMenuItem item = menu.getItem(j);
          if (item instanceof JMenu) {
            JMenu m = (JMenu) item;
            boolean containsCommand = false;
            for (int k = 0; k < m.getItemCount(); k++) {
              JMenuItem it = m.getItem(k);
              if (it != null && it.getActionCommand() != null
                  && setOfCommands.contains(it.getActionCommand())) {
                it.setEnabled(state);
                containsCommand = true;
              }
            }
            if (containsCommand) m.setEnabled(state);
          }
          if ((item != null) && (item.getActionCommand() != null)
              && setOfCommands.contains(item.getActionCommand())) {
            item.setEnabled(state);
          }
        }
      }
    }
    if (toolbar != null) for (i = 0; i < toolbar.getComponentCount(); i++) {
      Object o = toolbar.getComponent(i);
      if (o instanceof JButton) {
        JButton b = (JButton) o;
        if (setOfCommands.contains(b.getActionCommand())) {
          b.setEnabled(state);
          // if (b.getIcon() != null
          // && b.getIcon() instanceof CloseIcon)
          // ((CloseIcon) b.getIcon())
          // .setColor(state ? Color.BLACK : Color.GRAY);
        }
      }
    }
  }
  
  
  /**
   * 
   * @param state
   * @param menuBar
   * @param command
   */
  public static void setEnabled(boolean state, JMenuBar menuBar, Object command) {
    setEnabled(state, menuBar, new Object[] {command});
  }
  
  /**
   * @param state
   * @param menuBar
   * @param commands
   */
  public static void setEnabled(boolean state, JMenuBar menuBar,
    Object... commands) {
    setEnabled(state, menuBar, null, commands);
  }
  
  /**
   * @param state
   * @param toolbar
   * @param commands
   */
  public static void setEnabled(boolean state, JToolBar toolbar,
    Object... commands) {
    setEnabled(state, null, toolbar, commands);
  }
  
  /**
   * Disabled or enables the given components.
   * @param enabled
   * @param components
   */
  public static void setEnabledForAll(boolean enabled, Component...components) {
    for (Component component: components) {
      component.setEnabled(enabled);
    }
  }
  
  
  /**
   * Recursively set opaque to a value for p and all JComoponents on p.
   * @param p
   * @param val - false means transparent, true means object has a background.
   */
  public static void setOpaqueForAllElements(JComponent p, boolean val) {
    if (p instanceof JTextField || p instanceof JTextArea) return;
    p.setOpaque(val);
    
    for (Component c: p.getComponents()) {
      if (c instanceof JComponent) {
        setOpaqueForAllElements((JComponent) c, val);
      }
    }
    
  }
  
  /**
   * Show a {@link JPanel} as {@link Dialog}.
   * @param parent optional parent Frame or Dialog
   * @param c the panel to display
   * @param title a title that should be used for the window
   * @param addOkAndCancel if true, will add additional OK and CANCEL buttons
   * at the bottom of the dialog 
   * @return if ok has been pressed. More formarlly: either {@link JOptionPane#OK_OPTION} or
   * {@link JOptionPane#CANCEL_OPTION} or -2 if unknown (external closing, e.g., by your own
   * ok button).
   */
  public static int showAsDialog(Component parent, JPanel c, String title, final boolean addOkAndCancel) {

    // Initialize the dialog
    final JDialog jd;
    if (parent!=null && parent instanceof Frame) {
      jd = new JDialog((Frame)parent, title, true);
    } else if (parent!=null && parent instanceof Dialog) {
      jd = new JDialog((Dialog)parent, title, true);
    } else {
      jd = new JDialog();
      jd.setTitle(title);
      jd.setModal(true);
    }
    jd.setName(""); // The Name encodes the ok and cancel options!
    jd.setLayout(new BorderLayout());
    
    // Create ok and cancel buttons
    final JPanel okAndCancel;
    if (addOkAndCancel) {
      okAndCancel = buildOkCancelButtons(jd);
    } else { 
      okAndCancel = null;
    }
    
    // Initialize the panel
    jd.add(c, BorderLayout.CENTER);
    if (addOkAndCancel) jd.add(okAndCancel, BorderLayout.SOUTH);
    
    // Close dialog with ESC button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public synchronized void actionPerformed(ActionEvent e) {
        // deselect the OK button
        if (addOkAndCancel) {
          ((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).setSelected(false);
        }
        jd.setName(Integer.toString(JOptionPane.CANCEL_OPTION));
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    // Close dialog with ENTER button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // select the OK button
        if (addOkAndCancel) {
          ((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).setSelected(true);
        }
        jd.setName(Integer.toString(JOptionPane.OK_OPTION));
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    // Set close operations
    jd.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        jd.setVisible(false);
      }
    });
    c.addComponentListener(new ComponentListener() {
      public void componentHidden(ComponentEvent e) {
        jd.setVisible(false);
      }
      public void componentMoved(ComponentEvent e) {}
      public void componentResized(ComponentEvent e) {}
      public void componentShown(ComponentEvent e) {}
    });
    
    // Set size
    jd.pack();
    jd.setLocationRelativeTo(parent);
    
    // Set visible and wait until invisible
    jd.setVisible(true);
    
    // Dispose and return reader.
    jd.dispose();
    
    // Get the button/closing method that closed the dialog
    int retVal;
    if (jd.getName()=="") {
      // Exit with a button
      if (addOkAndCancel) {
        // Button from this method
        if (((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).isSelected()) {
          retVal = JOptionPane.OK_OPTION;
        } else {
          retVal = JOptionPane.CANCEL_OPTION;
        }
      } else {
        // External closing...
        retVal = -2; // Unknown
      }
    } else {
      // Exit with either Enter or Escape key
      retVal = Integer.parseInt(jd.getName());
    }
    
    return retVal;
  }
  
  /**
   * 
   * @param parent
   * @param message
   */
  public static void showWarningMessage(Component parent, String message) {
    logger.log(Level.WARNING, message);
    String name = ("Warning");
    JOptionPane.showMessageDialog(parent, StringUtil.toHTMLToolTip(message), name, JOptionPane.WARNING_MESSAGE);
  }
  
  
  /**
   * 
   * @param parent
   * @param message
   */
  public static void showErrorMessage(Component parent, String message) {
    showErrorMessage(parent, null, message);
  }
  
  /**
   * Displays the error message on a {@link JOptionPane}.
   * 
   * @param exc
   */
  public static void showErrorMessage(Component parent, Throwable exc) {
    String msg = getMessage(exc);
    
    if (logger != null) {
    	logger.log(Level.WARNING, msg, exc);
    }
    ValuePair<String, Integer> messagePair = StringUtil
    .insertLineBreaksAndCount(msg, StringUtil.TOOLTIP_LINE_LENGTH, "\n", null);
    Object message;
    if (messagePair.getB().intValue() > 30) {
      JEditorPane pane = new JEditorPane("text/html", messagePair.getA());
      pane.setEditable(false);
      pane.setPreferredSize(new Dimension(480, 240));
      message = new JScrollPane(pane);
    } else {
      message = messagePair.getA();
    }
		Class<? extends Throwable> clazz = exc.getCause() != null ? exc.getCause()
				.getClass() : exc.getClass();
		JOptionPane.showMessageDialog(parent, message,
			clazz != null ? clazz.getSimpleName() : "Error", JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Shows an error dialog with the given message in case the exception does not
   * provide any detailed message.
   * 
   * @param parent
   * @param exc
   * @param defaultMessage
   */
  public static void showErrorMessage(Component parent, Throwable exc, String defaultMessage) {
    if ((exc == null) || (exc.getLocalizedMessage() == null)
        || (exc.getLocalizedMessage().length() == 0)) {
      
      logger.log(Level.WARNING, defaultMessage, exc);
      String name = (exc!=null?exc.getClass().getSimpleName():"Error");
      JOptionPane.showMessageDialog(parent, StringUtil.toHTMLToolTip(defaultMessage), name, JOptionPane.ERROR_MESSAGE);
    } else {
      showErrorMessage(parent, exc);
    }
  }
  

  /**
	 * Shows a dialog that displays the given {@link listIndex} of {@link Object}s
	 * together with a label and a title.
	 * 
	 * @param parent
	 * @param label
	 *        A description what the purpose of the given {@link List} of
	 *        {@link File}s is.
	 * @param title
	 * @param things
	 */
	public static void showListMessage(Component parent, String label,
		String title, List<?> things) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(StringUtil.toHTML(label, StringUtil.TOOLTIP_LINE_LENGTH)),
			BorderLayout.NORTH);
		panel.add(new JScrollPane(new JList(things.toArray())));
		JOptionPane.showMessageDialog(parent, panel, title,
			JOptionPane.WARNING_MESSAGE);
	}
  
  
  
  /**
   * Shows a simple message with a given title and an ok button.
   * 
   * @param message
   * @param title
   * @return
   */
  public static void showMessage(String message, String title) {
    JOptionPane.showMessageDialog(null, StringUtil.toHTML(message,
      StringUtil.TOOLTIP_LINE_LENGTH), title, JOptionPane.INFORMATION_MESSAGE);
  }
  
  /**
   * Displays a message on a message dialog window, i.e., an HTML document.
   * 
   * @param path
   *        the URL of an HTML document.
   * @param title
   *        the title of the dialog to be displayed
   * @param owner
   *        the parent of the dialog or null.
   */
  public static void showMessage(URL path, String title, Component owner) {
    showMessage(path, title, owner, null);
  }


  /**
   * @param path
   * @param title
   * @param owner
   * @param icon
   */
  public static void showMessage(URL path, String title, Component owner,
    Icon icon) {
    JBrowserPane browser = new JBrowserPane(path);
    browser.removeHyperlinkListener(browser);
    browser.addHyperlinkListener(new SystemBrowser());
    browser.setBorder(BorderFactory.createEtchedBorder());
    
    try {
      File f = new File(OpenFile.doDownload(path.toString()));
      BufferedReader br;
      br = new BufferedReader(new FileReader(f));
      String line;
      int rowCount = 0, maxLine = 0;
      while (br.ready()) {
        line = br.readLine();
        if (line.length() > maxLine) {
          maxLine = line.length();
        }
        rowCount++;
      }
      
      if ((rowCount > 100) || (maxLine > 250)) {
        JScrollPane scroll = new JScrollPane(browser,
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // TODO: Calculate required size using the font size.
        scroll.setMaximumSize(new Dimension(470, 470));
        Dimension prefered = new Dimension(450, 450);
        browser.setPreferredSize(prefered);
        JOptionPane.showMessageDialog(owner, scroll, title,
          JOptionPane.INFORMATION_MESSAGE, icon);
      } else {
        JOptionPane.showMessageDialog(owner, browser, title,
          JOptionPane.INFORMATION_MESSAGE, icon);
      }
    } catch (IOException exc) {
      showErrorMessage(owner, exc);
    }
  }

  /**
   * 
   * @param parent
   * @param file
   */
  public static void showNowReadingAccessWarning(Component parent, File file) {
		ResourceBundle resource = ResourceManager
				.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
    JOptionPane.showMessageDialog(parent, StringUtil.toHTML(String.format(
      resource.getString("NO_READ_ACCESS_MESSAGE"), resource.getString(file
        .isFile() ? "THE_FILE" : "THE_DIRECTORY"), file.getAbsolutePath()),
        StringUtil.TOOLTIP_LINE_LENGTH), resource.getString("NO_READ_ACCESS_TITLE"),
        JOptionPane.WARNING_MESSAGE);
  }
  
  /**
   * 
   * @param parent
   * @param file
   */
  public static void showNowWritingAccessWarning(Component parent, File file) {
		ResourceBundle resource = ResourceManager
				.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
    JOptionPane.showMessageDialog(parent, StringUtil.toHTML(String.format(
      resource.getString("NO_WRITE_ACCESS_MESSAGE"), resource.getString(file
        .isFile() ? "THE_FILE" : "THE_DIRECTORY"), file.getAbsolutePath()),
        StringUtil.TOOLTIP_LINE_LENGTH), resource.getString("NO_WRITE_ACCESS_TITLE"),
        JOptionPane.WARNING_MESSAGE);
  }
  
  /**
   * Shows an ok/ cancel dialog in a new thread. You can specify runnables that
   * are executed, depending if the user confirms the dialog or not.
   * 
   * <p>This method has the advantage to allow modifications of the dialog, because
   * evaulation takes place in a new thread!
   * 
   * <b>WARNING: This might not work on all plattforms. Do not show messages in
   * different threads. There is a GUI thread in which messages should be
   * displayed, use other threads for computation. Do not go the opposite
   * way!</b>
   * 
   * @param component
   * @param caption
   * @param okAction optional, action to be performed after dialog confirmation.
   * @param cancelAction optional, action to be performed after dialog cancellation.
   */
  public static void showOkCancelDialogInNewThread(final Component component, final String caption, final Runnable okAction, final Runnable cancelAction) {
    SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
      /* (non-Javadoc)
       * @see javax.swing.SwingWorker#doInBackground()
       */
      protected Integer doInBackground() throws Exception {
        return JOptionPane.showConfirmDialog(null, component,
          caption, JOptionPane.OK_CANCEL_OPTION);
      }
      
      /* (non-Javadoc)
       * @see javax.swing.SwingWorker#done()
       */
      @Override
      protected void done() {
        super.done();
        try {
          Integer retVal = get();
          if ((retVal == JOptionPane.OK_OPTION) && (okAction != null)) {
            okAction.run();
          } else if ((retVal == JOptionPane.CANCEL_OPTION) && (cancelAction != null)) {
            cancelAction.run();
          }
        } catch (Exception exc) {
          GUITools.showErrorMessage(null, exc);
        }
      }
    };
    worker.execute();
    
    // Wait until the dialog is painted, before continuing
    while (worker.getState() == SwingWorker.StateValue.PENDING) {
      try {
        Thread.sleep(100); // do no decrease this value, JOptionPane takes longer to paint
      } catch (InterruptedException exc) {
        logger.finest(exc.getLocalizedMessage());
      }
    }
  }

	/**
   * Shows the output of a process inside a textarea.
   * Autoscrolls as the process genereates output and
   * automatically disables the ok-button as long as
   * the process is running and enables it, as soon
   * as the process terminates.
   * 
   * Hint: Consider setting {@link ProcessBuilder#redirectErrorStream()}
   * to true.
   * 
   * <b>WARNING: This might not work on all plattforms. Do not show messages in
   * different threads. There is a GUI thread in which messages should be
   * displayed, use other threads for computation. Do not go the opposite
   * way!</b>
   * 
   * @param p - the process to monitor.
   * @param f - the caption of the window
   * @param closeWindowAutomaticallyWhenDone - if set to
   * true, the window will be closed, as soon as the
   * process finished.
   * @return JTextArea
   */
  public static JTextArea showProcessOutputInTextArea(final Process p, Frame f,
    boolean closeWindowAutomaticallyWhenDone) {
    JTextArea area = new JTextArea();
    ProcessObservationWorker worker = new ProcessObservationWorker(p, area, f, closeWindowAutomaticallyWhenDone);
    worker.execute();
    
    // Wait until the dialog is painted, before continuing
    while (worker.getState() == SwingWorker.StateValue.PENDING) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }
    
    return area;
  }

	/**
   * Show a Question Dialog
   * @param parent may be null
   * @param message question to display
   * @param title dialog title
   * @param optionType e.g. {@link JOptionPane#YES_NO_OPTION}
   * @return an integer indicating the option selected by the user (e.g. {@link JOptionPane#YES_OPTION})
   */
  public static int showQuestionMessage(Component parent, String message, String title, int optionType) {
    return JOptionPane.showConfirmDialog(parent, StringUtil.toHTML(message, StringUtil.TOOLTIP_LINE_LENGTH), 
      title, optionType, JOptionPane.QUESTION_MESSAGE);
  }
  


	/**
   * Show a Question Dialog
   * @param parent may be null
   * @param message question to display
   * @param title dialog title
   * @param choices different choices
   * @return chosen index or JOptionPane static ints
   */
  public static int showQuestionMessage(Component parent, String message,
    String title, Object... choices) {
    return JOptionPane.showOptionDialog(parent, StringUtil.toHTML(message,
      StringUtil.TOOLTIP_LINE_LENGTH), title, JOptionPane.DEFAULT_OPTION,
      JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
  }

  /**
   * Shows a {@link JFileChooser} to the user, appends the selected file extension to the
   * selected file and checks weather the selected file is writable. If the file already
   * exists, the user will be asked if he wants to overwrite the file. If the file is not
   * writable, an error message id displayed to the user.
   * @param parent parent component for makeing a modal dialog. May be null.
   * @param saveDir initial directory for the {@link JFileChooser}. May be null.
   * @param filter {@link FileFilter} the user may choose from. May be null.
   * @return a file selected by the user if and only if this file is writable and the user
   * confirmed every warning. Else, null is returned. 
   */
  public static File showSaveFileChooser(Component parent, String saveDir, FileFilter... filter) {
    JFileChooser fc = GUITools.createJFileChooser(saveDir, (filter==null || filter.length<1),
      false, JFileChooser.FILES_ONLY, filter);
    if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
    
    // Get selected file and try to prepend extension
    File f = fc.getSelectedFile();
    
    // Eventually append file extension
    if (!f.getName().contains(".")) {
      String extension = fc.getFileFilter().getDescription();
      int pos = extension.lastIndexOf("*.");
      if (pos>=0) {
        extension = StringUtil.getWord(extension, pos+2, false);
        // *.* => (extension.length()==0)
        if (extension!=null && extension.length()>0) {
          f = new File(f.getPath() + "." + extension);
        }
      }
    }
    
    // Check if file exists and is writable
    boolean fileExists = f.exists();
    if (!fileExists) try {
      f.createNewFile();
    } catch (IOException e) {
      GUITools.showErrorMessage(parent, e);
      return null;
    }
    if (!f.canWrite() || f.isDirectory()) {
      GUITools.showNowWritingAccessWarning(parent, f);
    } else if (!fileExists || (fileExists && GUITools.overwriteExistingFile(parent, f))) {
      // This is the usual case
      return f;
    }
    return null;
  }

  /**
	 * Swaps the {@link KeyStroke} associated with the {@link JMenuItem}s that
	 * have the given actions, but only if both items can be found and are hence
	 * not null.
	 * 
	 * @param jMenuBar
	 * @param action1
	 * @param action2
	 */
	public static void swapAccelerator(JMenuBar jMenuBar, Object action1,
		Object action2) {
		JMenuItem item1 = GUITools.getJMenuItem(jMenuBar, action1);
		JMenuItem item2 = GUITools.getJMenuItem(jMenuBar, action2);
		
		if ((item1 != null) && (item2 != null)) {
			KeyStroke ks1 = item1.getAccelerator();
			KeyStroke ks2 = item2.getAccelerator();
			item1.setAccelerator(ks2);
			item2.setAccelerator(ks1);
		}
	}

}
