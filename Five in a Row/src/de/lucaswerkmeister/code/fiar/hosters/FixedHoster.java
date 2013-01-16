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
import java.rmi.AlreadyBoundException;
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

import de.lucaswerkmeister.code.fiar.clients.swingClient.SwingClient;
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
	private HashSet<RemoteClient> knownClients;
	private HashMap<Player, ClientPlayerPair> pairs;
	private DefaultListModel<String> players;

	private FixedHoster(final String globalAddress, final String localAddress) {
		super("Hosting");
		knownClients = new HashSet<>();
		pairs = new HashMap<>();

		setLayout(new BorderLayout());
		JPanel addresses = new JPanel(new GridLayout(2, 2));
		addresses.add(new JLabel("Global address:", JLabel.TRAILING));
		JTextField globalTF = new JTextField(globalAddress);
		globalTF.setEditable(false);
		addresses.add(globalTF);
		addresses.add(new JLabel("Local address:", JLabel.TRAILING));
		JTextField localTF = new JTextField(localAddress);
		localTF.setEditable(false);
		addresses.add(localTF);
		add(addresses, BorderLayout.NORTH);

		ScrollPane playersPanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		players = new DefaultListModel<>();
		JList<String> playerList = new JList<>(players);
		playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersPanel.add(playerList);
		add(playersPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridLayout(2, 1));
		JPanel startLocalClientPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton startLocalClient = new JButton("Start local client");
		final FixedHoster gui = this;
		startLocalClient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String[] addressParts = localAddress.split(":");
					new Thread(new SwingClient(addressParts[0], Integer.parseInt(addressParts[1]))).start();
				} catch (NumberFormatException | RemoteException | NotBoundException exception) {
					StringWriter sw = new StringWriter();
					sw.write("Failed to start local client. Exception:\n");
					exception.printStackTrace(new PrintWriter(sw));
					JOptionPane.showMessageDialog(gui, sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		startLocalClientPanel.add(startLocalClient);
		buttons.add(startLocalClientPanel);
		JPanel startServerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton startServer = new JButton("Start server");
		startServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Set<RemoteClient> watchingClients = new HashSet<>(knownClients);
				for (ClientPlayerPair pair : pairs.values())
					watchingClients.remove(pair.getClient());
				Set<ClientPlayerPair> pairSet = new HashSet<>(pairs.values());
				Server server = new FixedServer(pairSet, watchingClients);
				do {
					try {
						UnicastRemoteObject.exportObject(server, 0);
						break;
					} catch (RemoteException e2) {
						if (JOptionPane.showOptionDialog(gui, "Failed to host server. Retry?", "Error",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
							continue;
						System.exit(1);
					}
				} while (true);
				for (RemoteClient client : knownClients)
					try {
						client.gameStarts(server);
					} catch (RemoteException e1) {
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
	public void addClient(RemoteClient client) throws RemoteException {
		knownClients.add(client);
		for (Player p : pairs.keySet())
			client.playerJoined(p);
	}

	@Override
	public void removeClient(RemoteClient client) {
		knownClients.remove(client);
	}

	@Override
	public void addPlayer(RemoteClient controller, Player player) throws UnknownClientException,
			IllegalArgumentException, RemoteException {
		if (!knownClients.contains(controller))
			throw new UnknownClientException(controller);
		if (pairs.containsKey(player) && !pairs.get(player).getClient().equals(controller))
			throw new IllegalArgumentException("Wrong client for player " + player.toString() + "!");
		pairs.put(player, new ClientPlayerPair(controller, player));
		players.addElement(player.getName() + " (" + player.getID() + ")");
		for (RemoteClient c : knownClients)
			c.playerJoined(player);
		pack();
	}

	@Override
	public void removePlayer(Player player) throws UnknownPlayerException, RemoteException {
		if (!pairs.containsKey(player))
			throw new UnknownPlayerException(player);
		pairs.remove(player);
		players.removeElement(player.getName() + " (" + player.getID() + ")");
		for (RemoteClient c : knownClients)
			c.playerLeft(player);
		pack();
	}

	public static void main(String[] args) {
		try {
			File policy = new File("bin/FixedHoster.policy");
			try (BufferedWriter policyWriter = new BufferedWriter(new FileWriter(policy))) {
				policyWriter.write("grant { permission java.security.AllPermission;};");
			} catch (IOException e) {
				System.err.println("Failed to write policy file, exception:");
				e.printStackTrace();
				System.exit(1);
			}
			System.setProperty("java.security.policy", policy.getPath());
			System.setSecurityManager(new SecurityManager());
		} catch (SecurityException e) {
			System.err.println("FixedHoster must be allowed to set its security manager! Exception:");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			int port = Registry.REGISTRY_PORT;
			Registry registry = LocateRegistry.createRegistry(port);
			// We get our public IP from checkip.amazonaws.com
			Hoster hoster =
					new FixedHoster(new BufferedReader(new InputStreamReader(
							new URL("http://checkip.amazonaws.com").openStream())).readLine()
							+ ":" + port, InetAddress.getLocalHost().getHostAddress() + ":" + port);
			registry.bind("hoster", UnicastRemoteObject.exportObject(hoster, 0));
		} catch (AlreadyBoundException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}