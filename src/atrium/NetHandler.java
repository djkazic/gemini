package atrium;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
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
			server.addListener(new Listener() {
				//New incoming connection
				public void connected(Connection connection) {
					try {
						Peer newPeer = new Peer(connection, 1);
						newPeer.getConnection().sendTCP(new Request(RequestTypes.MUTEX, null));
						newPeer.getConnection().sendTCP(new Request(RequestTypes.PEERLIST, null));
						newPeer.getLatch().await();
						addPeer(newPeer);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				//New incoming packet post-connection
				public void received(Connection connection, Object object) {
					if(object instanceof Request) {
						Request request = (Request) object;
						String type = request.getType();

						switch(type) {
							case RequestTypes.MUTEX:
								Utilities.log(this, "Received request for mutex");
								connection.sendTCP(new Data(DataTypes.MUTEX, Core.mutex));
								break;
						
							case RequestTypes.PEERLIST:
								Utilities.log(this, "Received request for peerlist");
								//TODO: more refined peerList filtering
								connection.sendTCP(new Data(DataTypes.PEERLIST, peers));
								break;
								
							default: 
								Utilities.log(this, "Got request type " + type + " with payload of " + request.getPayload().toString());
								break;
						}
					}

					if(object instanceof Data) {
						Data data = (Data) object;
						String type = data.getType();

						switch(type) {
							case DataTypes.MUTEX:
								Utilities.log(this, "Received mutex data");
								Peer.findPeer(connection).setMutex((String) data.getPayload());
								break;
								
							case DataTypes.PEERLIST:
								Utilities.log(this,  "Received peerlist data");
								//TODO: implement peerlist processing
								Utilities.log(this, "Peerlist: " + (String) data.getPayload());
								break;
								
							default: 
								Utilities.log(this, "Got data type " + type + " with payload of " + data.getPayload().toString());
								break;
						}
					}
				}
			});

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
			client.addListener(new Listener() {
				public void connected(Connection connection) {
					//TODO: fix this to allow for two-way communication
					addPeer(new Peer(connection, 0));
				}

				public void received(Connection connection, Object object) {

				}
			});

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
	private void addPeer(Peer peer) {
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
