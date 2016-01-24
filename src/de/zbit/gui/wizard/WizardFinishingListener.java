/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.wizard;

import java.util.EventListener;

/**
 * With the help of this interface it is possible to display additional
 * information when finishing a wizard. Note that a
 * {@link WizardPanelDescriptor} that is supposed to display, e.g., a progress
 * bar when finishing, must override its method
 * {@link WizardPanelDescriptor#finish()} in order to indicate
 * that it does not immediately finish, but is going to display further
 * information. It is also possible to add multiple
 * {@link WizardFinishingListener}s to one such descriptor in order to receive a
 * signal as soon as the wizard is done.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public interface WizardFinishingListener extends EventListener {
	
	/**
	 * This method is called in order to inform a {@link Wizard} that it is
	 * finished and that its view can be closed.
	 */
	public void wizardFinished();
	
}
