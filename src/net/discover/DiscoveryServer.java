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
			while(true) {
				//Receive a packet
				//Utilities.log(this, "Received potential broadcast packet");
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				discoverySocket.receive(packet);
				//Check packet contents
				String message = new String(packet.getData()).trim();
				if(message.equals("DISC_RAD_REQ")) {
					byte[] sendData = "DISC_RAD_RESP".getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					discoverySocket.send(sendPacket);
				}
				Thread.sleep(300);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			discoverySocket.close();
		}
	}
}
