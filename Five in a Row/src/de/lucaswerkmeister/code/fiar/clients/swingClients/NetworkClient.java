package de.lucaswerkmeister.code.fiar.clients.swingClients;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.lucaswerkmeister.code.fiar.clients.swingClients.GameFrame.BoardListener;
import de.lucaswerkmeister.code.fiar.framework.Hoster;
import de.lucaswerkmeister.code.fiar.framework.NoPlayer;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.RemoteClient;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.event.BlockDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.BlockField;
import de.lucaswerkmeister.code.fiar.framework.event.BoardSizeProposal;
import de.lucaswerkmeister.code.fiar.framework.event.FieldAction;
import de.lucaswerkmeister.code.fiar.framework.event.Forfeit;
import de.lucaswerkmeister.code.fiar.framework.event.GameEnd;
import de.lucaswerkmeister.code.fiar.framework.event.GameEvent;
import de.lucaswerkmeister.code.fiar.framework.event.JokerDistributionAccepted;
import de.lucaswerkmeister.code.fiar.framework.event.JokerField;
import de.lucaswerkmeister.code.fiar.framework.event.PhaseChange;
import de.lucaswerkmeister.code.fiar.framework.event.PlaceStone;
import de.lucaswerkmeister.code.fiar.framework.event.PlayerVictory;
import de.lucaswerkmeister.code.fiar.framework.event.UnblockField;
import de.lucaswerkmeister.code.fiar.framework.event.UnjokerField;
import de.lucaswerkmeister.code.fiar.framework.exception.IllegalMoveException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownPlayerException;
import de.lucaswerkmeister.code.fiar.hosters.FixedHoster;

/**
 * A client that runs in a Swing GUI and is connected to a remote {@link Server} / {@link Hoster}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class NetworkClient implements RemoteClient, Runnable {
	private static final long serialVersionUID = 2228474887736398898L;
	private final Hoster hoster;
	private final List<Player> ownPlayers; // note that the contents of the list are not final
	private final Map<Integer, Player> allPlayers;
	private final NetworkClient instance; // needed for event listeners
	private DefaultListModel<String> playerListModel;
	private List<Player> listPlayers;
	private DefaultListModel<String> dimensionListModel;
	private Server server;
	private JFrame initFrame;
	private GameFrame gameFrame;
	private volatile int currentPlayerID;
	private String victoryMessage;

	/**
	 * Creates a new {@link NetworkClient} that asks for the host address via a
	 * {@link JOptionPane#showInputDialog(Object) input dialog}.
	 * 
	 * @throws RemoteException
	 *             If something remotely goes wrong.
	 * @throws NotBoundException
	 *             If no hoster can be found at the specified address.
	 */
	public NetworkClient() throws RemoteException, NotBoundException {
		this(JOptionPane.showInputDialog("Please enter the hoster address (hostname:port)"));
	}

	/**
	 * Creates a new {@link NetworkClient} with the specified address.
	 * 
	 * @param address
	 *            The address in the format "hostname:port".
	 * @throws RemoteException
	 *             If something remotely goes wrong.
	 * @throws NotBoundException
	 *             If no hoster can be found at the specified address.
	 */
	public NetworkClient(final String address) throws RemoteException, NotBoundException {
		this(address.substring(0, address.indexOf(':')), Integer.parseInt(address.substring(address.indexOf(':') + 1)));
	}

	/**
	 * Creates a new {@link NetworkClient} with the specified address.
	 * 
	 * @param hostName
	 *            The host name.
	 * @param port
	 *            The port.
	 * @throws RemoteException
	 *             If something remotely goes wrong.
	 * @throws NotBoundException
	 *             If no hoster can be found at the specified address.
	 */
	public NetworkClient(final String hostName, final int port) throws RemoteException, NotBoundException {
		instance = this;
		hoster = (Hoster) LocateRegistry.getRegistry(hostName, port).lookup("hoster");
		UnicastRemoteObject.exportObject(this, 0);
		ownPlayers = new LinkedList<>();
		allPlayers = new HashMap<>();
		hoster.addClient(this);
	}

	@Override
	public void run() {
		try {
			initFrame = new JFrame("Lobby");
			initFrame.setLayout(new BorderLayout());
			final JButton addPlayerButton = new JButton("Add players");
			addPlayerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					Player player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
					while (player != null)
						try {
							hoster.addPlayer(instance, player);
							ownPlayers.add(player);
							player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
						} catch (final UnknownClientException e) {
							e.printStackTrace();
							System.exit(1); // this should never happen, I can afford a user-unfriendly shutdown here
						} catch (final IllegalArgumentException e) {
							// @formatter:off
							final int option = JOptionPane.showConfirmDialog(
											initFrame,
											"Player could not be added. Add another player?", "Error",
											JOptionPane.YES_NO_OPTION);
							// @formatteR:on
							if (option == JOptionPane.YES_OPTION)
								player = GameFrame.showAddPlayerDialog(false, findID(), initFrame);
							else
								player = null;
							e.printStackTrace();
						} catch (final RemoteException e) {
							JOptionPane.showMessageDialog(initFrame,
									"An error occured while sending the information to the hoster. Exiting.", "Error",
									JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
							System.exit(1);
						}
				}
			});
			final JPanel addPlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			addPlayerPanel.add(addPlayerButton);
			initFrame.add(addPlayerPanel, BorderLayout.NORTH);
			playerListModel = new DefaultListModel<>();
			listPlayers = new LinkedList<>();
			final JList<String> playerList = new JList<>(playerListModel);
			initFrame.add(new JScrollPane(playerList), BorderLayout.CENTER);
			final JButton removePlayerButton = new JButton("Remove player(s)");
			removePlayerButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					final int[] players = playerList.getSelectedIndices();
					if (players.length > 0)
						try {
							for (final int index : players)
								hoster.removePlayer(listPlayers.get(index));
						} catch (final UnknownPlayerException e) {
							// This should NEVER EVER happen, so again no user-friendly shutdown
							e.printStackTrace();
							System.exit(1);
						} catch (final RemoteException e) {
							JOptionPane.showMessageDialog(initFrame,
									"An error occured while sending the information to the hoster. Exiting.", "Error",
									JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
							System.exit(1);
						}
				}
			});
			final JPanel removePlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			removePlayerPanel.add(removePlayerButton);
			initFrame.add(removePlayerPanel, BorderLayout.SOUTH);
			initFrame.pack();
			initFrame.setVisible(true);
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			initFrame.setVisible(false);
			initFrame.dispose();

			initFrame = new JFrame("Board size");
			initFrame.setLayout(new BorderLayout());
			final JButton addDimensionButton = new JButton("Add size");
			addDimensionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					final Dimension size = GameFrame.showChooseBoardSizeDialog(initFrame);
					for (final Player p : ownPlayers)
						try {
							server.action(instance, new BoardSizeProposal(p, size));
						} catch (IllegalStateException | IllegalMoveException e) {
							e.printStackTrace();
						} catch (final RemoteException e) {
							failRemote(e);
						}
				}
			});
			final JPanel addDimensionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			addDimensionPanel.add(addDimensionButton);
			initFrame.add(addDimensionPanel, BorderLayout.NORTH);
			dimensionListModel = new DefaultListModel<>();
			final JList<String> dimensionList = new JList<>(dimensionListModel);
			initFrame.add(new JScrollPane(dimensionList), BorderLayout.CENTER);
			final JButton acceptDimensionButton = new JButton("Accept size(s)");
			acceptDimensionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					for (final String d : dimensionList.getSelectedValuesList())
						for (final Player p : ownPlayers)
							try {
								final String[] coords = d.split("×");
								server.action(
										instance,
										new BoardSizeProposal(p, new Dimension(Integer.parseInt(coords[0]), Integer
												.parseInt(coords[1]))));
							} catch (IllegalStateException | IllegalMoveException e) {
								e.printStackTrace();
							} catch (final RemoteException e) {
								failRemote(e);
							}
				}
			});
			final JPanel acceptDimensionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			acceptDimensionPanel.add(acceptDimensionButton);
			initFrame.add(acceptDimensionPanel, BorderLayout.SOUTH);
			initFrame.pack();
			initFrame.setVisible(true);
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			initFrame.setVisible(false);
			initFrame.dispose();
			initFrame = null;

			// @formatter:off
			gameFrame = new GameFrame(
					server.getCurrentBoard(this), (Server.IN_A_ROW == 5 ? "Five" : Server.IN_A_ROW) + " in a Row");
			// @formatteR:on
			gameFrame.setButtons(new String[] {"Accept current block distribution" });
			gameFrame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					try {
						for (final Player p : ownPlayers)
							switch (event.getActionCommand()) {
							case "Accept current block distribution":
								server.action(instance,
										new BlockDistributionAccepted(p, server.getCurrentBoard(instance)));
								break;
							case "Accept current joker distribution":
								server.action(instance,
										new JokerDistributionAccepted(p, server.getCurrentBoard(instance)));
								break;
							}
					} catch (final RemoteException e) {
						failRemote(e);
					} catch (IllegalStateException | IllegalMoveException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			gameFrame.addBoardListener(new BoardListener() {
				@Override
				public void fieldClicked(final Field field) {
					try {
						// This board listener is going to stay in place for the rest of the game, so it doesn't just
						// handle blocking
						final int[] phase = server.getPhase(instance);
						final Point xy = field.getField();
						if (phase[0] == 0 && phase[1] == 1)
							// blocking
							server.action(
									instance,
									NoPlayer.getInstance().equals(server.getCurrentBoard(instance).getPlayerAt(xy))
											? new BlockField(ownPlayers.get(0), xy) : new UnblockField(ownPlayers
													.get(0), xy));
						else if (phase[0] == 0 && phase[1] == 2)
							// jokers
							server.action(
									instance,
									NoPlayer.getInstance().equals(server.getCurrentBoard(instance).getPlayerAt(xy))
											? new JokerField(ownPlayers.get(0), xy) : new UnjokerField(ownPlayers
													.get(0), xy));
						else if (phase[0] == 1 && phase[1] == 1)
							// move
							if (ownPlayers.contains(allPlayers.get(currentPlayerID)))
								server.action(instance, new PlaceStone(allPlayers.get(currentPlayerID), xy));
					} catch (final RemoteException e) {
						failRemote(e);
					} catch (final IllegalMoveException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			gameFrame.setStatus("Choose blocked fields");
			gameFrame.setVisible(true);
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			gameFrame.setStatus("Choose joker fields");
			gameFrame.setButtons(new String[] {"Accept current joker distribution" });
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
			currentPlayerID = server.getPhase(this)[2];
			gameFrame.setStatus(allPlayers.get(currentPlayerID).getName() + "'"
					+ (GameFrame.endsWithSSound(allPlayers.get(currentPlayerID).getName()) ? "" : "s") + " turn!");
			gameFrame.setButtons(new String[] {"Forfeit" });
			gameFrame.removeAllActionListeners();
			gameFrame.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent event) {
					try {
						server.action(instance, new Forfeit(allPlayers.get(currentPlayerID)));
					} catch (final RemoteException e) {
						failRemote(e);
					} catch (IllegalStateException | IllegalMoveException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}

			gameFrame.setStatus(victoryMessage);
			gameFrame.setButtons(new String[0]);
			gameFrame.removeAllActionListeners();
			gameFrame.removeAllBoardListeners();
			JOptionPane.showMessageDialog(gameFrame, victoryMessage, "Game ended", JOptionPane.INFORMATION_MESSAGE);
			gameFrame.toFront();
			synchronized (this) {
				try {
					wait();
				} catch (final InterruptedException e) {
					// This should never happen; wait() should be ended by notify(), never by interrupt()
					e.printStackTrace();
					System.exit(1);
				}
			}
		} catch (final RemoteException e) {
			failRemote(e);
		}
	}

	@Override
	public void playerJoined(final Player player) {
		allPlayers.put(player.getID(), player);
		final int index = Arrays.binarySearch(playerListModel.toArray(), player.getName());
		final int insertIndex = -(index + 1);
		playerListModel.add(insertIndex, player.getName());
		listPlayers.add(insertIndex, player);
		initFrame.pack();
		GameFrame.reshowAddPlayerDialog(findID());
	}

	@Override
	public void playerLeft(final Player player) {
		final int index = Arrays.binarySearch(playerListModel.toArray(), player.getName());
		playerListModel.removeElementAt(index);
		listPlayers.remove(index);
		allPlayers.remove(player.getID());
		if (ownPlayers.contains(player))
			ownPlayers.remove(player);
	}

	@Override
	public void gameStarts(final Server server) {
		GameFrame.hideAddPlayerDialog();
		this.server = server;
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void gameEvent(final GameEvent e) throws RemoteException {
		if (e instanceof BoardSizeProposal) {
			final Dimension d = ((BoardSizeProposal) e).getSize();
			final String size = d.width + "×" + d.height;
			if (!dimensionListModel.contains(size)) {
				final int index = Arrays.binarySearch(dimensionListModel.toArray(), size);
				final int insertIndex = -(index + 1);
				dimensionListModel.add(insertIndex, size);
			}
		} else if (e instanceof PhaseChange)
			synchronized (this) {
				this.notify();
			}
		else if (e instanceof FieldAction) {
			final FieldAction fa = (FieldAction) e;
			gameFrame
					.setPlayerAt(((FieldAction) e).getField(), server.getCurrentBoard(this).getPlayerAt(fa.getField()));
			if (e instanceof PlaceStone) {
				final int[] phase = server.getPhase(instance);
				currentPlayerID = phase[2];
				gameFrame.setButtons(new String[] {"Forfeit" },
						new boolean[] {ownPlayers.contains(allPlayers.get(currentPlayerID)) });
				gameFrame.setStatus(allPlayers.get(currentPlayerID).getName() + "'"
						+ (GameFrame.endsWithSSound(allPlayers.get(currentPlayerID).getName()) ? "" : "s") + " turn!");
			}
		} else if (e instanceof GameEnd) {
			if (e instanceof PlayerVictory)
				victoryMessage = ((PlayerVictory) e).getWinningPlayer().getName() + " wins!";
			else
				victoryMessage = "Game ended.";
			synchronized (this) {
				this.notify();
			}
		}
	}

	/**
	 * Finds an unused ID.
	 * 
	 * @return An ID that is not yet used by any player known to the client.
	 */
	private int findID() {
		int id = 0;
		boolean isUsed;
		do {
			id++;
			isUsed = false;
			for (final Player p : allPlayers.values())
				if (p.getID() == id) {
					isUsed = true;
					break;
				}
		} while (isUsed);
		return id;
	}

	/**
	 * Private utility method to tell the user that a {@link RemoteException} occurred,
	 * {@link RemoteException#printStackTrace() print its stack trace}, and then {@link System#exit(int) exit}.
	 * 
	 * @param e
	 *            The exception.
	 */
	private void failRemote(final RemoteException e) {
		JOptionPane.showMessageDialog(initFrame,
				"An error occured while sending the information to the server. Exiting.", "Error",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		System.exit(1);
	}

	/**
	 * Utility main method to start a network client that will ask for the address (parameterless constructor).
	 * 
	 * @param args
	 *            Currently ignored.
	 * @throws RemoteException
	 *             If something remotely goes wrong.
	 * @throws NotBoundException
	 *             If no hoster can be found. (To start a hoster, run {@link FixedHoster#main(String[])}.
	 */
	public static void main(final String[] args) throws RemoteException, NotBoundException {
		new Thread(new NetworkClient()).start();
	}
}