package atrium;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import data.Data;
import io.StreamedBlock;
import io.StreamedBlockedFile;
import requests.Request;
import requests.RequestTypes;

public class NetHandler {

	public static ArrayList<Peer> peers;
	
	private Server server;

	public NetHandler() {
		peers = new ArrayList<Peer> ();
		registerServerListeners();
		Client initialClient = getClient();
		registerClientListeners(initialClient);
		peerDiscovery(initialClient);
	}

	public void registerServerListeners() {
		try {
			server = new Server(512000 * 4, 512000 * 4);
			registerClasses(server.getKryo());

			Utilities.switchGui(this, "Registering server listeners");
			server.addListener(new DualListener(1));
			server.addListener(new BlockListener());

			Utilities.switchGui(this, "Starting server component");
			server.bind(Core.tcp, Core.udp);
			server.start();

		} catch (Exception ex) {}
	}
	
	private Client getClient() {
		Client client = new Client(512000 * 4, 512000 * 4);
		registerClientListeners(client);
		return client;
	}

	public void registerClientListeners(Client client) {
		try {
			registerClasses(client.getKryo());
			Utilities.log(this, "Registered client listeners");

			//Use listeners, not arbitrary code
			client.addListener(new DualListener(0));

			Utilities.log(this, "Starting client component");
			client.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void registerClasses(Kryo kryo) {
		//Shared fields import
		kryo.register(String[].class);
		kryo.register(ArrayList.class);
		kryo.register(byte[].class);
		
		//Specifics import
		kryo.register(Data.class);
		kryo.register(Request.class);
		kryo.register(Peer.class);
		kryo.register(Client.class);
		kryo.register(Inet4Address.class);
		kryo.register(StreamedBlockedFile.class);
		kryo.register(StreamedBlock.class);
	}
	
	private void peerDiscovery(Client client) {
		try {
			Utilities.switchGui(this, "Finding peers...");
			Utilities.log(this, "Discovering hosts");
			
			List<InetAddress> foundHosts = client.discoverHosts(Core.udp, 4000);

			//TODO: remove this debug section
			foundHosts.clear();
			//foundHosts.add(InetAddress.getByName("136.167.199.57"));
			foundHosts.add(InetAddress.getByName("192.227.251.74"));
			//foundHosts.add(InetAddress.getByName("136.167.192.28"));
			
			//Filter out local IP
			InetAddress localhost = InetAddress.getLocalHost();
			foundHosts.remove(localhost);
			InetAddress[] allLocalIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
			if(allLocalIps != null && allLocalIps.length > 1) {
				Utilities.log(this, "Multiple local IPs detected");
				for(int i=0; i < allLocalIps.length; i++) {
					foundHosts.remove(allLocalIps[i]);
				}
			}

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
				try {
					Utilities.log(this, "Attempting connect to " + ia.getHostAddress());
					Client newConnection = getClient();
					newConnection.connect(8000, ia, Core.tcp, Core.udp);
				} catch (Exception ex) {
					Utilities.log(this, "Connection to " + ia.getHostAddress() + " failed");
				}
			}

			Utilities.log(this, "Terminated peer discovery");
			(new Thread(new Runnable() {
				public void run() {
					while(true) {
						if(!Core.headless) {
							if(peers.isEmpty()) {
								try {
									Thread.sleep(1000);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							} else {
								Core.mainWindow.ready();
								break;
							}
						}
					}
				}
			})).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void doSearch(String keyword) {
		for(Peer peer : peers) {
			peer.getConnection().sendTCP(new Request(RequestTypes.SEARCH, Core.aes.encrypt(keyword)));
		}
	}
	
	public static void requestBlock(String origin, String block) {
		for(Peer peer : peers) {
			peer.getConnection().sendTCP(new Request(RequestTypes.BLOCK, new String[] {Core.aes.encrypt(origin), Core.aes.encrypt(block)}));
		}
	}
}
