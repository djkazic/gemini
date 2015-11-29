package org.cortex.Radiator.requests;

import java.nio.file.Files;
import java.util.ArrayList;
import org.cortex.Radiator.atrium.Core;
import org.cortex.Radiator.atrium.FileUtils;
import org.cortex.Radiator.atrium.NetHandler;
import org.cortex.Radiator.atrium.Peer;
import org.cortex.Radiator.atrium.Utilities;
import org.cortex.Radiator.data.Data;
import org.cortex.Radiator.data.DataTypes;
import org.cortex.Radiator.io.BlockedFile;
import org.cortex.Radiator.io.StreamedBlock;
import org.cortex.Radiator.io.StreamedBlockedFile;

import com.jfastnet.messages.Message;

public class Request extends Message {

	private static final long serialVersionUID = 8838706412092536001L;
	public ArrayList<Object> payloadContainer;

	public Request(String type, Object payload) {
		this.payloadContainer = new ArrayList<Object> ();
		this.payloadContainer.add(type);
		this.payloadContainer.add(payload);
	}

	@Override
	public void process() {
		Utilities.log(this, "Sender: " + getSenderId() + " | Receiver: " + getReceiverId());
		
		String type = (String) payloadContainer.get(0);
		final Object payload = payloadContainer.get(1);
		final Peer foundPeer = Peer.findPeer(getSenderId());

		if(foundPeer != null) {
			switch(type) {
				//Requests below are non-encrypted

				case RequestTypes.PUBKEY:
					Utilities.log(this, "Received request for pubkey");
					(new Thread(new Runnable() {
						public void run() {
							foundPeer.sendTo(new Data(DataTypes.PUBKEY, Core.pubKey));
							Utilities.log(this, "\tSent pubkey back");
						}
					})).start();
					break;

				case RequestTypes.MUTEX:
					Utilities.log(this, "Received request for mutex");
					(new Thread(new Runnable() {
						public void run() {
							foundPeer.sendTo(new Data(DataTypes.MUTEX, Core.rsa.encrypt(Core.mutex, foundPeer.getPubkey())));
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
								//TODO: handling of peerslist
								//if(peer.externallyVisible())
								//String peerData = peer.getConnection().getRemoteAddressTCP().getHostString() + ":"
								//				+ peer.getConnection().getRemoteAddressTCP().getPort();
								//refinedPeerList.add(Core.aes.encrypt(peerData));
							}
							foundPeer.sendTo(new Data(DataTypes.PEERLIST, refinedPeerList));
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
							String encryptedQuery = (String) payload;
							String decrypted = foundPeer.getAES().decrypt(encryptedQuery);
							//For all known BlockedFiles, check if relevant
							ArrayList<StreamedBlockedFile> streams = new ArrayList<StreamedBlockedFile> ();
							for(BlockedFile bf : Core.blockDex) {
								if(bf.matchSearch(decrypted)) {
									streams.add(bf.toStreamedBlockedFile());
								}
							}
							foundPeer.sendTo(new Data(DataTypes.SEARCH, streams));
							Utilities.log(this, "\tSent search results back");
						}
					})).start();
					//Search results are ArrayList<StreamedBlockedFile> which have encrypted name + onboard encrypted blockList
					break;	

				case RequestTypes.BLOCK:
					(new Thread(new Runnable() {
						public void run() {
							Utilities.log(this, "Received request for block:");
							String[] encryptedBlock = (String[]) payload;
							String blockOrigin = foundPeer.getAES().decrypt(encryptedBlock[0]);
							String blockName = foundPeer.getAES().decrypt(encryptedBlock[1]);

							//TODO: search for block
							BlockedFile foundBlock;
							if((foundBlock = FileUtils.getBlockedFile(blockOrigin)) != null) {
								int blockPosition;
								if((blockPosition = foundBlock.getBlockList().indexOf(blockName)) != -1) {

									byte[] searchRes = null;
									if(foundBlock.isComplete()) {
										//Attempt complete search
										searchRes = FileUtils.findBlockFromComplete(foundBlock, blockPosition);
									} else {
										//Attempt incomplete search
										try {
											searchRes = Files.readAllBytes(FileUtils.findBlockAppData(blockOrigin, blockName).toPath());
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}

									if(searchRes != null) {
										Utilities.log(this, "\tSending back block, length: " + searchRes.length);
										foundPeer.sendTo(new Data(DataTypes.BLOCK, new StreamedBlock(blockOrigin, blockName, searchRes)));
										//blockConn.sendTCP(new Data(DataTypes.BLOCK, new StreamedBlock(blockOrigin, blockName, searchRes)));
									} else {
										Utilities.log(this, "\tFailure: could not find block " + blockName);
									}
								} else {
									Utilities.log(this, "\tFailure: BlockedFile block mismatch; blockList: " 
											+ foundBlock.getBlockList());
								}
							} else {
								Utilities.log(this, "\tFailure: don't have origin BlockedFile");
							}
						}
					})).start();
					break;
			}
		} else {
			Utilities.log(this, "Peer for message " + getSenderId() + "S | R" + getReceiverId()  + " not found");
			Utilities.log(this, "\tDump of peers: " + NetHandler.peers.get(0).getClientId());
			Utilities.log(this, "Req type: " + type);
		}
	}

	public Object getPayload() {
		return payload;
	}
}