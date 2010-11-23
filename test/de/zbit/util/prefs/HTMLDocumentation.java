/**
 * 
 */
package de.zbit.util.prefs;

import de.zbit.gui.GUIOptions;

/**
 * @author Andreas Dr&auml;ger
 *
 */
public class HTMLDocumentation {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(KeyProvider.Tools.createDocumentation(GUIOptions.class));
	}
	
}
