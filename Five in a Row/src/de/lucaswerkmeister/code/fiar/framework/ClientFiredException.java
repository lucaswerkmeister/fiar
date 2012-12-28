package de.lucaswerkmeister.code.fiar.framework;

/**
 * A {@link ClientFiredException} should be thrown by a {@link Server} method to
 * indicate that the server no longer supports the {@link Client}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class ClientFiredException extends UnknownClientException {

	private static final long serialVersionUID = 7427056918189525715L;

	/**
	 * A new {@link ClientFiredException} with the specified fired client.
	 * 
	 * @param firedClient
	 *            The fired client.
	 */
	public ClientFiredException(Client firedClient) {
		super(firedClient);
	}

	/**
	 * A new {@link ClientFiredException} with the specified fired client and
	 * message.
	 * 
	 * @param firedClient
	 *            The fired client.
	 * @param message
	 *            The message.
	 */
	public ClientFiredException(Client firedClient, String message) {
		super(firedClient, message);
	}

	/**
	 * A new {@link ClientFiredException} with the specified fired client and
	 * cause.
	 * 
	 * @param firedClient
	 *            The fired client.
	 * @param cause
	 *            The cause.
	 */
	public ClientFiredException(Client firedClient, Throwable cause) {
		super(firedClient, cause);
	}

	/**
	 * A new {@link ClientFiredException} with the specified fired client, cause
	 * and message.
	 * 
	 * @param firedClient
	 *            The fired client.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public ClientFiredException(Client firedClient, String message,
			Throwable cause) {
		super(firedClient, message, cause);
	}

	/**
	 * A new {@link ClientFiredException} with the specified fired client,
	 * message, cause, enableSuppression and writableStackTrace.
	 * 
	 * @param firedClient
	 *            The fired client.
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 * @param enableSuppression
	 *            Whether to enable or disable suppression.
	 * @param writableStackTrace
	 *            Whether or not the stack trace should be writable.
	 */
	public ClientFiredException(Client firedClient, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(firedClient, message, cause, enableSuppression,
				writableStackTrace);
	}

	/**
	 * Gets the fired client.
	 * 
	 * @return The fired client.
	 */
	public Client getFiredClient() {
		return getUnknownClient();
	}
}