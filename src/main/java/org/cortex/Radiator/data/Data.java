package org.cortex.Radiator.data;

import java.util.ArrayList;

import org.cortex.Radiator.atrium.Core;
import org.cortex.Radiator.atrium.Peer;
import org.cortex.Radiator.atrium.Utilities;
import org.cortex.Radiator.io.BlockedFile;
import org.cortex.Radiator.io.StreamedBlock;
import org.cortex.Radiator.io.StreamedBlockedFile;

import com.jfastnet.messages.Message;

public class Data extends Message {

	private static final long serialVersionUID = -1111007186733125975L;
	public ArrayList<Object> payloadContainer;
	
	public Data(String type, Object payload) {
		this.payloadContainer = new ArrayList<Object> ();
		this.payloadContainer.add(type);
		this.payloadContainer.add(payload);
	}
	
	@Override
	public void process() {
		String type = (String) payloadContainer.get(0);
		final Object payload = payloadContainer.get(1);
		final Peer foundPeer = Peer.findPeer(getSenderId());
		
		switch(type) {
			//Data below are encryption keys, mutex is encrypted via RSA
		
			case DataTypes.PUBKEY:
				Utilities.log(this, "Received pubkey data: ");
				(new Thread(new Runnable() {
					public void run() {
						String pubkeyData = (String) payload;
						if(foundPeer.setPubkey(pubkeyData)) {
							foundPeer.getPubkeyLatch().countDown();
						}
					}
				})).start();
				break;
		
			case DataTypes.MUTEX:
				Utilities.log(this, "Received mutex data");
				(new Thread(new Runnable() {
					public void run() {
						String encryptedMutex = (String) payload;
						try {
							String mutexData = Core.rsa.decrypt(encryptedMutex);
							//Update foundPeer
							Peer bridgeFoundPeer = foundPeer;
							while(bridgeFoundPeer == null) {
								bridgeFoundPeer = Peer.findPeer(getSenderId());
								Thread.sleep(100);
							}
							if(bridgeFoundPeer.mutexCheck(mutexData)) {
								bridgeFoundPeer.getCryptoLatch().countDown();
							}
						} catch (Exception ex) {
							Utilities.log(this, "Failed to set mutex");
							ex.printStackTrace();
						}
					}
				})).start();
				break;

			//All data past this point is encrypted via symmetric encryption
			//TODO: symmetric encryption for peerlist and on
				
			case DataTypes.PEERLIST:
				Utilities.log(this,  "Received peerlist data");
				//TODO: implement peerlist processing
				(new Thread(new Runnable() {
					public void run() {
						if(payload instanceof ArrayList<?>) {
							ArrayList<String> finishedList = new ArrayList<String> ();
							ArrayList<?> potentialList = (ArrayList<?>) payload;
							for(int i=0; i < potentialList.size(); i++) {
								Object o = potentialList.get(i);
								if(o instanceof String) {
									String encrypted = (String) o;
									String decrypted = foundPeer.getAES().decrypt(encrypted);
									finishedList.add(decrypted);
								}
							}
							Utilities.log(this, "\tPeerlist: " + finishedList);
						}
					}
				})).start();
				break;
				
			case DataTypes.SEARCH:
				Utilities.log(this,  "Received search reply data");
				(new Thread(new Runnable() {
					public void run() {
						Object searchPayload = payload;
						if(searchPayload instanceof ArrayList<?>) {
							ArrayList<?> potentialStreams = (ArrayList<?>) searchPayload;
							for(int i=0; i < potentialStreams.size(); i++) {
								Object o = potentialStreams.get(i);
								if(o instanceof StreamedBlockedFile) {
									StreamedBlockedFile sbl = (StreamedBlockedFile) o;
									BlockedFile intermediate = sbl.toBlockedFile(foundPeer.getAES());
									
									//Store name and blockList in preparation for Download thread fetching from GUI
									String name = intermediate.getPointer().getName();
									ArrayList<String> blockList = intermediate.getBlockList();
									Core.index.put(name, blockList);
									
									String sizeEstimate = "";
									int estimateKb = (int) ((Core.blockSize * blockList.size()) / 1000);
									if(estimateKb > 1000) {
										int estimateMb = (int) (estimateKb / 1000D);
										sizeEstimate += estimateMb + "MB";
									} else {
										sizeEstimate += estimateKb + "KB";
									}
									
									Core.mainWindow.addRowToSearchModel(new String[] {name, sizeEstimate});
								}
							}
						}
					}
				})).start();
				break;
				
			case DataTypes.BLOCK:
				Utilities.log(this, "Received block data");
				//Threaded decryption
				(new Thread(new Runnable() {
					public void run() {
						StreamedBlock streamedBlock = (StreamedBlock) payload;
						String origin = foundPeer.getAES().decrypt(streamedBlock.getOrigin());
						Utilities.log(this, "\tBlock origin: " + origin + ", size = " + foundPeer.getAES().decrypt(streamedBlock.getFileBytes()).length);
						streamedBlock.insertSelf(foundPeer.getAES());
					}
				})).start();
				break;
		}
	}

	public Object getPayload() {
		return payload;
	}
}