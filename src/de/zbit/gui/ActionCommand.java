package de.zbit.gui;

/**
 * @author draeger
 * @date 2010-10-28
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
