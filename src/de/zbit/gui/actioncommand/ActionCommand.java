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
package de.zbit.gui.actioncommand;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-28
 * @version $Rev$
 * @since 1.0
 */
public interface ActionCommand {

    /**
     * Provides a human-readable name for this command.
     * 
     * @return
     */
    public String getName();

    /**
     * This gives a more comprehensive description of the purpose of a command.
     * 
     * @return
     */
    public String getToolTip();

}
