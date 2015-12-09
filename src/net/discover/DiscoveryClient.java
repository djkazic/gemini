package net.discover;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import atrium.Core;
import atrium.NetHandler;

public class DiscoveryClient implements Runnable {

	private DatagramSocket searchSocket;

	public void run() {
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() < (start + 4000L)) {
			try {
				searchSocket = new DatagramSocket();
				searchSocket.setBroadcast(true);

				byte[] sendData = "DISC_RAD_REQ".getBytes();

				//Broadcast to 255.255.255.255
				try {
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Core.config.discoverPort);
					searchSocket.send(sendPacket);
				} catch (Exception e) {}

				// Broadcast the message over all the network interfaces
				Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
					if (networkInterface.isLoopback() || !networkInterface.isUp()) {
						continue; // Don't broadcast to the loopback interface
					}
					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if (broadcast == null) {
							continue;
						}
						try {
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
							searchSocket.send(sendPacket);
						} catch (Exception e) {}
					}
				}
				//Wait for a response
				byte[] recvBuf = new byte[15000];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				searchSocket.receive(receivePacket);

				String message = new String(receivePacket.getData()).trim();
				if(message.equals("DISC_RAD_RESP") && 
				   !NetHandler.foundHosts.contains(InetAddress.getByName(receivePacket.getAddress().getHostAddress())) &&
					!receivePacket.getAddress().getHostAddress().equals("127.0.0.1")) {
					try {
						String potentialPeer = receivePacket.getAddress().getHostAddress();
						NetHandler.foundHosts.add(InetAddress.getByName(potentialPeer));
						//Utilities.log(this, "Local peer identified: " + potentialPeer);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				searchSocket.close();
				Thread.sleep(100);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}