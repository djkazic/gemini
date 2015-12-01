package listeners;

import java.util.ArrayList;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import atrium.Core;
import atrium.NetHandler;
import atrium.Peer;
import atrium.Utilities;
import io.BlockedFile;
import io.serialize.StreamedBlock;
import io.serialize.StreamedBlockedFile;
import packets.data.Data;
import packets.data.DataTypes;
import packets.requests.Request;
import packets.requests.RequestTypes;

public class DualListener extends Listener {
	
	private int inOut;
	
	public DualListener(int inOut) {
		super();
		this.inOut = inOut;
	}

	//New connection, either incoming or outgoing
	public void connected(Connection connection) {
		if(inOut == 1) {
			Utilities.log(this, "New incoming peer");
		} else {
			Utilities.log(this, "New outgoing peer");
		}
		try {
			if(inOut == 1) {
				new Peer(connection, inOut);
			} else {
				new Peer(connection, inOut);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void disconnected(Connection connection) {
		Peer foundPeer = Peer.findPeer(connection);
		if(foundPeer != null) {
			foundPeer.disconnect();
		}
	}
	
	//New incoming packet post-connection
	public void received(final Connection connection, Object object) {
		final Peer foundPeer = Peer.findPeer(connection);
		
		if(object instanceof Request) {
			final Request request = (Request) object;
			String type = request.getType();

			switch(type) {
				//Requests below are non-encrypted
			
				case RequestTypes.PUBKEY:
					Utilities.log(this, "Received request for pubkey");
					(new Thread(new Runnable() {
						public void run() {
							connection.sendTCP(new Data(DataTypes.PUBKEY, Core.pubKey));
							Utilities.log(this, "\tSent pubkey back");
						}
					})).start();
					break;
			
				case RequestTypes.MUTEX:
					Utilities.log(this, "Received request for mutex");
					(new Thread(new Runnable() {
						public void run() {
							connection.sendTCP(new Data(DataTypes.MUTEX, Core.rsa.encrypt(Core.mutex, foundPeer.getPubkey())));
							Utilities.log(this, "\tSent mutex back");
						}
					})).start();
					break;
					
				//Requests below are symmetrically encrypted
	
				case RequestTypes.PEERLIST:
					Utilities.log(this, "Received request for peerlist");
					//TODO: more refined peerList filtering
					(new Thread(new Runnable() {
						public void run() {
							ArrayList<String> refinedPeerList = new ArrayList<String> ();
							for(Peer peer : NetHandler.peers) {
								//if(peer.externallyVisible())
								String peerData = peer.getConnection().getRemoteAddressTCP().getHostString() + ":"
												+ peer.getConnection().getRemoteAddressTCP().getPort();
								refinedPeerList.add(Core.aes.encrypt(peerData));
							}
							connection.sendTCP(new Data(DataTypes.PEERLIST, refinedPeerList));
							Utilities.log(this, "\tSent peerlist back");
							if(foundPeer.getInOut() == 0) {
								foundPeer.getDeferredLatch().countDown();
							}
						}
					})).start();
					break;
					
				case RequestTypes.SEARCH:
					Utilities.log(this, "Received request for search");
					(new Thread(new Runnable() {
						public void run() {
							String encryptedQuery = (String) request.getPayload();
							String decrypted = foundPeer.getAES().decrypt(encryptedQuery);
							//For all known BlockedFiles, check if relevant
							ArrayList<StreamedBlockedFile> streams = new ArrayList<StreamedBlockedFile> ();
							for(BlockedFile bf : Core.blockDex) {
								if(bf.matchSearch(decrypted)) {
									streams.add(bf.toStreamedBlockedFile());
								}
							}
							connection.sendTCP(new Data(DataTypes.SEARCH, streams));
							Utilities.log(this, "\tSent search results back");
						}
					})).start();
					//Search results are ArrayList<StreamedBlockedFile> which have encrypted name + onboard encrypted blockList
					break;
					
			}
		} else if(object instanceof Data) {
			final Data data = (Data) object;
			String type = data.getType();

			switch(type) {
				//Data below are encryption keys, mutex is encrypted via RSA
			
				case DataTypes.PUBKEY:
					Utilities.log(this, "Received pubkey data: ");
					(new Thread(new Runnable() {
						public void run() {
							String pubkeyData = (String) data.getPayload();
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
							String encryptedMutex = (String) data.getPayload();
							try {
								String mutexData = Core.rsa.decrypt(encryptedMutex);
								//Update foundPeer
								Peer bridgeFoundPeer = foundPeer;
								while(bridgeFoundPeer == null) {
									bridgeFoundPeer = Peer.findPeer(connection);
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
							Object payload = data.getPayload();
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
							Object searchPayload = data.getPayload();
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
							StreamedBlock streamedBlock = (StreamedBlock) data.getPayload();
							String origin = foundPeer.getAES().decrypt(streamedBlock.getOrigin());
							Utilities.log(this, "\tBlock origin: " + origin + ", size = " + foundPeer.getAES().decrypt(streamedBlock.getFileBytes()).length);
							streamedBlock.insertSelf(foundPeer.getAES());
						}
					})).start();
					break;
			}
		}
	}
}