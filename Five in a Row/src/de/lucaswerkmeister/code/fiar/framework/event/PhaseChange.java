package de.lucaswerkmeister.code.fiar.framework.event;

/**
 * Represents a change in the current game Phase.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class PhaseChange extends GameEvent {
	private final int[] newPhase;

	/**
	 * A new {@link PhaseChange} with the specified new phase.
	 * 
	 * @param newPhase
	 *            The new Phase.
	 */
	public PhaseChange(int[] newPhase) {
		this.newPhase = newPhase;
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