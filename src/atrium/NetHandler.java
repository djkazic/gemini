package atrium;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class NetHandler {

	public static ArrayList<Peer> peers;
	
	private Server server;
	private Client client;

	public NetHandler() {
		peers = new ArrayList<Peer> ();
		registerServerListeners();
		registerClientListeners();
	}

	public void registerServerListeners() {
		try {
			server = new Server();
			registerClasses(server.getKryo());

			Utilities.log(this, "Registering server listeners");
			server.addListener(new DualListener(this));

			Utilities.log(this, "Starting server component");
			server.bind(Core.tcp, Core.udp);
			server.start();

		} catch (Exception ex) {}
	}

	public void registerClientListeners() {
		try {
			client = new Client();
			registerClasses(client.getKryo());

			Utilities.log(this, "Registering client listeners");

			//Use listeners, not arbitrary code
			client.addListener(new DualListener(this));

			Utilities.log(this, "Starting client component");
			client.start();

			//Do peer discovery
			peerDiscovery();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void registerClasses(Kryo kryo) {
		//Shared fields import
		kryo.register(ArrayList.class);

		//Specifics import
		kryo.register(Data.class);
		kryo.register(Request.class);
	}
	
	private void peerDiscovery() {
		try {
			Utilities.log(this, "Discovering hosts");
			List<InetAddress> foundHosts = client.discoverHosts(Core.udp, 8500);

			//Filter out local IP
			foundHosts.remove(InetAddress.getLocalHost());

			//Filter out loop-back interfaces
			Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				Enumeration<InetAddress> addresses =  networkInterface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress inetAddress = addresses.nextElement();
					if(inetAddress.isLoopbackAddress()) {
						while(foundHosts.contains(inetAddress)) {
							foundHosts.remove(inetAddress);
						}
					}
				}
			}

			if(foundHosts.size() == 0) {
				Utilities.log(this, "No hosts found on LAN");
			} else {
				Utilities.log(this, "Found hosts: " + foundHosts);
			}

			//DEBUG
			//TODO: make this not just an IP, but also port
			//TODO: port randomization
			for(InetAddress ia : foundHosts) {
				Utilities.log(this, "Attempting connect to " + ia.getHostAddress());
				client.connect(5000, ia, Core.tcp, Core.udp);
			}

			Utilities.log(this, "Terminated peer discovery");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Adds a peer only if it isn't of self or other peers' mutex
	 * @param peer
	 */
	public void addPeer(Peer peer) {
		if(peer.getMutex().equals(Core.mutex)) {
			return;
		}
		boolean passed = true;
		for(Peer otherPeer : peers) {
			if(otherPeer.getMutex().equals(peer.getMutex())) {
				passed = false;
			}
		}
		if(passed) {
			peers.add(peer);
		}
	}
	
	
}
