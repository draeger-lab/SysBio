package de.zbit.exception;

/**
 * Used in "InfoManagement" to distinct between temporary errors and actual
 * unsuccessfull retrievements.
 * 
 * @author wrzodek
 */
public class UnsuccessfulRetrieveException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4556630433492743411L;

	/**
	 * 
	 */
	public UnsuccessfulRetrieveException() {
		super();
	}
}
