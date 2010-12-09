package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * This class implements a {@link JWindow} object which is shown on the bottom
 * right corner of the screen. It notifies the user that a more recent version
 * of a program is available. The release notes of this version can be shown.
 * 
 * @author Hannes Borch
 * @author Andreas Dr&auml;ger
 * @since This was part of SBMLsqueezer 1.2 and 1.3.
 */

public class UpdateMessage {
	
	/**
	 * A small yellow {@link JWindow} without regular window decoration that pops
	 * up on the right bottom of the screen to display the update message.
	 * 
	 * @author Hannes Borch
	 * @author Andreas Dr&auml;ger
	 * 
	 */
	private static class UpdateMessageWindow extends JWindow implements
			ActionListener {
		
		/**
		 * Generated serial version uid.
		 */
		private static final long serialVersionUID = -6726847840681376184L;
		
		/**
		 * 
		 */
		private JPanel contentPanel;
		
		/**
		 * 
		 */
		private JButton okButton;
		
		/**
		 * 
		 */
		private JButton showHideButton;
		
		/**
		 * Main constructor. Initializes a {@link JWindow} object containing a (at the
		 * beginning) invisible {@link JEditorPane} which contains the program's release
		 * notes and two {@link JButtons} for showing them and exiting the {@link Window}.
		 * 
		 * @param u
		 * @param applicationName
		 * @throws IOException
		 */
		public UpdateMessageWindow(String u, String applicationName)
			throws IOException {
			super();
			setBackground(Color.YELLOW);
			setAlwaysOnTop(true);
			URL url = new URL(u);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.setBackground(getBackground());
			
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			showHideButton = new JButton("show release notes");
			showHideButton.setIcon(UIManager.getIcon("ICON_ARROW_RIGHT"));
			showHideButton.setIconTextGap(5);
			showHideButton.setBorderPainted(false);
			showHideButton.setBackground(new Color(buttonPanel.getBackground()
					.getRGB()));
			showHideButton.setSize(150, 20);
			showHideButton.addActionListener(this);
			buttonPanel.add(showHideButton);
			buttonPanel.add(okButton);
			
			Scanner scanner = new Scanner(url.openStream());
			String s = scanner.useDelimiter("<h1>").next();
			s = "<html><body>" + scanner.useDelimiter("\\Z").next();
			JEditorPane pane = new JEditorPane("text/html", s);
			pane.setEditable(false);
			pane.addHyperlinkListener(new SystemBrowser());
			contentPanel = new JPanel(new BorderLayout());
			JScrollPane scroll = new JScrollPane(pane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setPreferredSize(new Dimension(550, 400));
			contentPanel.add(scroll, BorderLayout.CENTER);
			
			contentPanel.setVisible(false);
			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.setBackground(getBackground());
			mainPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(
				Color.BLACK), String.format("An update for %s is available.",
				applicationName), TitledBorder.CENTER, TitledBorder.BELOW_TOP));
			mainPanel.add(contentPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
			setContentPane(mainPanel);
			pack();
			adjustLocation();
		}
		
		/**
		 * If the "show release notes" button is hit, the {@link JEditorPane} object
		 * containing the release notes is made visible and the button's text is
		 * changed to "hide release notes". If "hide release notes" is hit, the
		 * {@link JEditorPane} gets invisible again, and the button's text is
		 * rechanged to it's default. If "OK" is hit, the window is closed.
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JButton) {
				String buttonText = ((JButton) e.getSource()).getText();
				if (buttonText.equals("show release notes")) {
					contentPanel.setVisible(true);
					showHideButton.setText("hide release notes");
				} else if (buttonText.equals("hide release notes")) {
					contentPanel.setVisible(false);
					showHideButton.setText("show release notes");
				} else {
					dispose();
				}
			}
			validate();
			pack();
			adjustLocation();
		}
		
		/**
		 * Adjusts the location of the update message window to it's actual size.
		 */
		private void adjustLocation() {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(d.width - this.getWidth(), d.height - this.getHeight()
					- 30);
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1923146558856297087L;
	
	private String applicationName;
	
	private String dottedVersionNumber;
	
	/**
	 * 
	 */
	private String latestVersion;
	
	/**
	 * 
	 */
	private URL urlPrefix;

	private List<WindowListener> listOfListeners;
	
	/**
	 * 
	 * @param applicationName
	 * @param urlPrefix
	 */
	public UpdateMessage(String applicationName, URL urlPrefix) {
		this.applicationName = applicationName;
		this.urlPrefix = urlPrefix;
		this.listOfListeners = new LinkedList<WindowListener>();
	}
	
	/**
	 * Checks if there is an update for a program available.
	 * 
	 * @param gui
	 *        If true, a small window is displayed with the update message,
	 *        otherwise the message is printed on the console.
	 * 
	 * @param dottedVersionNumber
	 * @return <code>true</code> if an update is available <code>false</code>
	 *         otherwise.
	 * @throws IOException
	 *         If the {@link URL} to the update is incorrect or if there is
	 *         neither a file with the name <code>releaseNotes&lt;latestVersion&gt;.htm</code>
	 *         or <code>releaseNotes&lt;latestVersion&gt;.html</code>.
	 */
	public boolean checkForUpdate(boolean gui, String dottedVersionNumber)
		throws IOException {
		URL url = new URL(urlPrefix + "latest.txt");
		latestVersion = (new Scanner(url.openStream())).next();
		String notes = "releaseNotes" + latestVersion;
		if (notes.endsWith(".0")) {
			notes = notes.substring(0, notes.length() - 2);
		}
		this.dottedVersionNumber = dottedVersionNumber;
		if (compareVersionNumbers(dottedVersionNumber, latestVersion)) {
			try {
				showUpdateMessage(gui, urlPrefix + notes + ".htm");
			} catch (IOException exc) {
				showUpdateMessage(gui, urlPrefix + notes + ".html");
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Compares the version with which this object is initialized and the file "latest.txt" on
	 * www.
	 * 
	 * @param prog
	 * @param url
	 * @return
	 */
	private boolean compareVersionNumbers(String prog, String url) {
		StringTokenizer progToken = new StringTokenizer(prog);
		StringTokenizer urlToken = new StringTokenizer(url);
		boolean lastTokenComp = false;
		while (progToken.hasMoreElements() && urlToken.hasMoreElements()) {
			int localVers = Integer.parseInt(progToken.nextToken("."));
			int latestVers = Integer.parseInt(urlToken.nextToken("."));
			if (localVers <= latestVers) {
				if ((localVers < latestVers) && lastTokenComp) { return true; }
				lastTokenComp = true;
			} else {
				lastTokenComp = false;
			}
		}
		if (urlToken.hasMoreElements()
				&& (Integer.parseInt(urlToken.nextToken(".")) > 0) && lastTokenComp) { return true; }
		return false;
	}
	
	/**
	 * Show the update message window.
	 * 
	 * @param gui
	 *        If true the update message is shown in a graphical mode, otherwise
	 *        just as text.
	 * 
	 * @param url
	 * @throws IOException
	 */
	private void showUpdateMessage(boolean gui, String url) throws IOException {
		if (gui) {
			JWindow umw = new UpdateMessage.UpdateMessageWindow(url, applicationName);
			for (WindowListener wl : listOfListeners) {
				umw.addWindowListener(wl);
			}
			umw.setVisible(true);
		} else {
			System.out.printf(
						"\nUpdate notification:\n--------------------\nA new version of %s is available.\nPlease visit %s\nto obtain version %s.\nFor your information: you are now using %s version %s.\n",
						applicationName, urlPrefix, latestVersion, applicationName,
						dottedVersionNumber);
		}
	}

	/**
	 * 
	 * @param wl
	 * @return
	 */
	public boolean addWindowListener(WindowListener wl) {
		return this.listOfListeners.add(wl);
	}
	
	/**
	 * 
	 * @param wl
	 * @return
	 */
	public boolean removeWindowListener(WindowListener wl) {
		return this.listOfListeners.remove(wl);
	}
}
