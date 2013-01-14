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

import java.util.Arrays;

/**
 * Represents a change in the current game Phase.
 * <p>
 * An instance of this class shall not be fired if the occurrence of a phase change can be implicitly derived from the
 * firing of another {@link GameEvent}, such as (but not limited to):
 * <ul>
 * <li>{@link GameEnd} and any of its subclasses</li>
 * <li>{@link PlaceStone}</li>
 * </ul>
 * <small>Author's note: The above list is, at the time of writing, complete; however, I wrote "not limited to" to
 * protect against future changes when I possibly can't be bothered updating this javadoc :-)</small>
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PhaseChange extends GameEvent {
	private static final long serialVersionUID = 8538900938762208485L;
	private final int[] newPhase;

	/**
	 * A new {@link PhaseChange} with the specified new phase.
	 * 
	 * @param newPhase
	 *            The new Phase.
	 */
	public PhaseChange(final int[] newPhase) {
		this.newPhase = Arrays.copyOf(newPhase, newPhase.length);
	}

	/**
	 * Gets the new Phase.
	 * 
	 * @return The new Phase.
	 */
	public int[] getNewPhase() {
		return newPhase;
	}
}