package org.metaborg.spoofax.shell.services;

/**
 * Exception to be thrown when {@link IEditorServices} cannot return the requested service.
 */
public class ServiceUnavailableException extends Exception {

	private static final long serialVersionUID = 1L;
	private final String service;

	/**
	 * Create a new exception.
	 *
	 * @param service
	 *            String - The service that could not be provided.
	 */
	public ServiceUnavailableException(String service) {
		this.service = service;
	}

	@Override
	public String getMessage() {
		return "Could not provide the following service: " + service;
	}

}
