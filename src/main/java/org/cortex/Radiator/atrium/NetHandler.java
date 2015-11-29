package org.cortex.Radiator.atrium;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.cortex.Radiator.requests.Request;
import org.cortex.Radiator.requests.RequestTypes;
import com.jfastnet.Client;
import com.jfastnet.Config;
import com.jfastnet.Server;

public class NetHandler {

	public static ArrayList<Peer> peers;

	public NetHandler() {
		peers = new ArrayList<Peer> ();
		configServer();
		configClient();
		//peerDiscovery(initialClient);
		connectCheck();
	}
	
	private void configServer() {
		//Config
		Config serverConfig = new Config();
		serverConfig.setBindPort(Core.udp);
		serverConfig.serverHooks = new ServerHook();
		serverConfig.maximumUdpPacketSize = 2048;
		Core.server = new Server(serverConfig);
		Utilities.log("atrium.Core", "Starting server component");
		Core.server.start();
	}
	
	private void configClient() {
		Config clientConfig = new Config();
		clientConfig.setPort(Core.udp);
		clientConfig.maximumUdpPacketSize = 2048;
		//clientConfig.host = "136.167.192.28";
		Core.client = new Client(clientConfig);
		Utilities.log("atrium.Core", "Starting client component");
		Core.client.start();
		Core.client.blockingWaitUntilConnected();
	}
	
	private void peerDiscovery(Client client) {
		try {
			Utilities.switchGui(this, "Finding peers...");
			Utilities.log(this, "Discovering hosts");
			
			List<InetAddress> foundHosts = new ArrayList<InetAddress> ();

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
					Config clientConfig = new Config();
					clientConfig.setPort(Core.udp);
					clientConfig.host = ia.getHostAddress();
					Client newConnection = new Client(clientConfig);
					newConnection.blockingWaitUntilConnected();
					Utilities.log(this, "Outgoing connection ID: " + newConnection.getConfig().senderId);
				} catch (Exception ex) {
					Utilities.log(this, "Connection to " + ia.getHostAddress() + " failed");
				}
			}

			Utilities.log(this, "Terminated peer discovery");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void connectCheck() {
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
	}
	
	public static void doSearch(String keyword) {
		Core.server.send(new Request(RequestTypes.SEARCH, Core.aes.encrypt(keyword)));
		Core.client.send(new Request(RequestTypes.SEARCH, Core.aes.encrypt(keyword)));
	}
	
	public static void requestBlock(String origin, String block) {
		Core.server.send(new Request(RequestTypes.BLOCK, new String[] {
														 	Core.aes.encrypt(origin), 
														 	Core.aes.encrypt(block)
														 }));
		Core.client.send(new Request(RequestTypes.BLOCK, new String[] {
															Core.aes.encrypt(origin), 
															Core.aes.encrypt(block)
														 }));
	}
}
