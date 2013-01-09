/*
 * Five in a Row, a short game.
 * Copyright (C) 2012/2013 Lucas Werkmeister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lucaswerkmeister.code.fiar.framework.event;

/**
 * Indicates that the game ended due to an internal error.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class InternalError extends GameEnd {

	private final Throwable cause;
	private final String message;

	/**
	 * A new {@link InternalError}.
	 */
	public InternalError() {
		this(null, "Unknown internal error");
	}

	/**
	 * A new {@link InternalError} with the specified cause.
	 * 
	 * @param cause
	 *            The cause of the internal error.
	 */
	public InternalError(final Throwable cause) {
		this(cause, "Internal error: " + cause.getMessage());
	}

	/**
	 * A new {@link InternalError} with the specified message.
	 * 
	 * @param message
	 *            The message.
	 */
	public InternalError(final String message) {
		this(null, message);
	}

	/**
	 * A new {@link InternalError} with the specified cause and message.
	 * 
	 * @param cause
	 *            The cause of the internal error.
	 * @param message
	 *            The message.
	 */
	public InternalError(final Throwable cause, final String message) {
		this.cause = cause;
		this.message = message;
	}

	/**
	 * Gets the cause of the internal error.
	 * 
	 * @return The cause of the internal error.
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Gets the message of the internal error.
	 * 
	 * @return The message of the internal error.
	 */
	public String getMessage() {
		return message;
	}
}