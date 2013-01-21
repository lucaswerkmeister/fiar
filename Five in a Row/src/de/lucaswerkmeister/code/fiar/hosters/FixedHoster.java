package de.lucaswerkmeister.code.fiar.hosters;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import de.lucaswerkmeister.code.fiar.clients.swingClients.NetworkClient;
import de.lucaswerkmeister.code.fiar.framework.Hoster;
import de.lucaswerkmeister.code.fiar.framework.Player;
import de.lucaswerkmeister.code.fiar.framework.RemoteClient;
import de.lucaswerkmeister.code.fiar.framework.Server;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownClientException;
import de.lucaswerkmeister.code.fiar.framework.exception.UnknownPlayerException;
import de.lucaswerkmeister.code.fiar.servers.ClientPlayerPair;
import de.lucaswerkmeister.code.fiar.servers.FixedServer;

/**
 * A hoster that displays a GUI that allows the user to start the game, which will start a {@link FixedServer}. Adding
 * clients or servers after the game was started has no effect.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class FixedHoster extends JFrame implements Hoster {
	private static final long serialVersionUID = -195985835430112510L;
	private final HashSet<RemoteClient> knownClients;
	private final HashMap<Player, ClientPlayerPair> pairs;
	private final DefaultListModel<String> players;

	/**
	 * Creates a new {@link FixedHoster} with the specified addresses.
	 * <p>
	 * The addresses are only used for display in the GUI; the hoster has to be made available under these addresses
	 * separately.
	 * 
	 * @param globalAddress
	 *            The global address.
	 * @param localAddress
	 *            The local address.
	 */
	private FixedHoster(final String globalAddress, final String localAddress) {
		super("Hosting");
		knownClients = new HashSet<>();
		pairs = new HashMap<>();

		setLayout(new BorderLayout());
		final JPanel addresses = new JPanel(new GridLayout(2, 2));
		addresses.add(new JLabel("Global address:", SwingConstants.TRAILING));
		final JTextField globalTF = new JTextField(globalAddress);
		globalTF.setEditable(false);
		addresses.add(globalTF);
		addresses.add(new JLabel("Local address:", SwingConstants.TRAILING));
		final JTextField localTF = new JTextField(localAddress);
		localTF.setEditable(false);
		addresses.add(localTF);
		add(addresses, BorderLayout.NORTH);

		final ScrollPane playersPanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		players = new DefaultListModel<>();
		final JList<String> playerList = new JList<>(players);
		playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersPanel.add(playerList);
		add(playersPanel, BorderLayout.CENTER);

		final JPanel buttons = new JPanel(new GridLayout(2, 1));
		final JPanel startLocalClientPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton startLocalClient = new JButton("Start local client");
		final FixedHoster gui = this;
		startLocalClient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					new Thread(new NetworkClient(localAddress)).start();
				} catch (NumberFormatException | RemoteException | NotBoundException exception) {
					final StringWriter sw = new StringWriter();
					sw.write("Failed to start local client. Exception:\n");
					exception.printStackTrace(new PrintWriter(sw));
					JOptionPane.showMessageDialog(gui, sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		startLocalClientPanel.add(startLocalClient);
		buttons.add(startLocalClientPanel);
		final JPanel startServerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton startServer = new JButton("Start server");
		startServer.addActionListener(new ActionListener() {



			@Override
			public void actionPerformed(final ActionEvent e) {
				final Set<RemoteClient> watchingClients = new HashSet<>(knownClients);
				for (final ClientPlayerPair pair : pairs.values())
					watchingClients.remove(pair.getClient());
				final Set<ClientPlayerPair> pairSet = new HashSet<>(pairs.values());
				final Server server = new FixedServer(pairSet, watchingClients);
				do
					try {
						UnicastRemoteObject.exportObject(server, 0);
						break;
					} catch (final RemoteException e2) {
						// @formatter:off
						if (JOptionPane.showOptionDialog(gui, "Failed to host server. Retry?", "Error", 
								JOptionPane.OK_CANCEL_OPTION,JOptionPane.ERROR_MESSAGE, null, null, null)
								== JOptionPane.OK_OPTION)
							// @formatter:on
							continue;
						System.exit(1);
					}
				while (true);
				for (final RemoteClient client : knownClients)
					try {
						client.gameStarts(server);
					} catch (final RemoteException e1) {
						e1.printStackTrace();
					}
				gui.setVisible(false);
			}
		});
		startServerPanel.add(startServer);
		buttons.add(startServerPanel);
		add(buttons, BorderLayout.SOUTH);

		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void addClient(final RemoteClient client) throws RemoteException {
		knownClients.add(client);
		for (final Player p : pairs.keySet())
			client.playerJoined(p);
	}

	@Override
	public void removeClient(final RemoteClient client) {
		knownClients.remove(client);
	}

	@Override
	public void addPlayer(final RemoteClient controller, final Player player) throws UnknownClientException,
			IllegalArgumentException, RemoteException {
		if (!knownClients.contains(controller))
			throw new UnknownClientException(controller);
		if (pairs.containsKey(player) && !pairs.get(player).getClient().equals(controller))
			throw new IllegalArgumentException("Wrong client for player " + player.toString() + "!");
		pairs.put(player, new ClientPlayerPair(controller, player));
		players.addElement(player.getName() + " (" + player.getID() + ")");
		for (final RemoteClient c : knownClients)
			c.playerJoined(player);
		pack();
	}

	@Override
	public void removePlayer(final Player player) throws UnknownPlayerException, RemoteException {
		if (!pairs.containsKey(player))
			throw new UnknownPlayerException(player);
		pairs.remove(player);
		players.removeElement(player.getName() + " (" + player.getID() + ")");
		for (final RemoteClient c : knownClients)
			c.playerLeft(player);
		pack();
	}

	/**
	 * Starts a new {@link FixedHoster} and publishes it.
	 * 
	 * @param args
	 *            Currently ignored.
	 */
	public static void main(final String[] args) {
		try {
			final File policy = new File("bin/FixedHoster.policy");
			try (BufferedWriter policyWriter = new BufferedWriter(new FileWriter(policy))) {
				policyWriter.write("grant { permission java.security.AllPermission;};");
			} catch (final IOException e) {
				System.err.println("Failed to write policy file, exception:");
				e.printStackTrace();
				System.exit(1);
			}
			System.setProperty("java.security.policy", policy.getPath());
			System.setSecurityManager(new SecurityManager());
		} catch (final SecurityException e) {
			System.err.println("FixedHoster must be allowed to set its security manager! Exception:");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			final int port = Registry.REGISTRY_PORT;
			final Registry registry = LocateRegistry.createRegistry(port);
			// We get our public IP from checkip.amazonaws.com
			// @formatter:off
			final Hoster hoster = new FixedHoster(
							new BufferedReader(new InputStreamReader(
							new URL("http://checkip.amazonaws.com").openStream())).readLine()
							+ ":" + port, InetAddress.getLocalHost().getHostAddress() + ":" + port);
			// @formatter:on
			registry.rebind("hoster", UnicastRemoteObject.exportObject(hoster, 0));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}