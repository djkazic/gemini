package net.discover;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import atrium.Core;

public class DiscoveryServer implements Runnable {

	private DatagramSocket discoverySocket;

	public void run() {
		try {
			discoverySocket = new DatagramSocket(Core.config.discoverPort, InetAddress.getByName("0.0.0.0"));
			discoverySocket.setBroadcast(true);
			byte[] recvBuf;
			byte[] sendData;
			DatagramPacket packet = null;
			while (true) {
				// Receive a packet
				// Utilities.log(this, "Received potential broadcast packet");
				recvBuf = new byte[15000];
				packet = new DatagramPacket(recvBuf, recvBuf.length);
				discoverySocket.receive(packet);
				// Check packet contents
				String message = new String(packet.getData()).trim();
				if (message.equals("DISC_RAD_REQ")) {
					sendData = "DISC_RAD_RESP".getBytes();
					discoverySocket
							.send(new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort()));
				}
				Thread.sleep(300);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			discoverySocket.close();
		}
	}
}
