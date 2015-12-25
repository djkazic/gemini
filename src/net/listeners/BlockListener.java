package net.listeners;

import java.nio.file.Files;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;

import atrium.Core;
import atrium.Peer;
import atrium.Utilities;
import io.FileUtils;
import io.block.BlockedFile;
import io.serialize.StreamedBlock;
import packets.data.Data;
import packets.data.DataTypes;
import packets.requests.Request;
import packets.requests.RequestTypes;

public class BlockListener extends TcpIdleSender {

	private String blockOriginChecksum;
	private String blockName;
	private ArrayList<Object> sendQueue;

	public BlockListener() {
		sendQueue = new ArrayList<Object> ();
	}

	public void idle(Connection connection) {
		if(!sendQueue.isEmpty()) {
			super.idle(connection);
		}
	}

	public void received(final Connection connection, Object object) {
		if(object instanceof Request) {
			final Request request = (Request) object;
			
			if(request.getType() == (RequestTypes.BLOCK)) {
				Peer foundPeer = Peer.findPeer(connection);
				String[] encryptedBlock = (String[]) request.getPayload();
				blockOriginChecksum = foundPeer.getAES().decrypt(encryptedBlock[0]);
				blockName = foundPeer.getAES().decrypt(encryptedBlock[1]);

				//TODO: search for block
				BlockedFile foundBlock;
				if((foundBlock = FileUtils.getBlockedFile(blockOriginChecksum)) != null) {
					int blockPosition;
					if((blockPosition = foundBlock.getBlockList().indexOf(blockName)) != -1) {

						byte[] searchRes = null;
						if(!foundBlock.isComplete() || Core.config.hubMode) {
							//Attempt incomplete search
							try {
								searchRes = Files.readAllBytes(FileUtils.findBlockAppData(foundBlock, blockName).toPath());
							} catch (Exception ex) {
								Utilities.log(this, "Received request for block we do not yet have", true);
							}
						} else {
							if(foundBlock.isComplete()) {
								//Attempt complete search
								searchRes = FileUtils.findBlockFromComplete(foundBlock, blockPosition);
							}
						}

						if(searchRes != null) {
							Utilities.log(this, "\tSending back block " + blockName, true);
							StreamedBlock sb = new StreamedBlock(blockOriginChecksum, blockName, searchRes);
							boolean dupe = false;
							//TODO: replace with equals() impl
							for(Object odata : sendQueue) {
								if(odata instanceof Data) {
									Data data = (Data) odata;
									Object opayload = data.getPayload();
									if(opayload instanceof StreamedBlock) {
										StreamedBlock payload = (StreamedBlock) opayload;
										if(payload.getOrigin().equals(sb.getOrigin())) {
											dupe = true;
										}
									}
								}
							}
							if(!dupe) {
								sendQueue.add(new Data(DataTypes.BLOCK, sb));
							}
							//blockConn.sendTCP(new Data(DataTypes.BLOCK, new StreamedBlock(blockOrigin, blockName, searchRes)));
						} else {
							Utilities.log(this, "\tFailure: could not find block " + blockName, true);
						}
					} else {
						Utilities.log(this, "\tFailure: BlockedFile block mismatch; blockList: " 
								      + foundBlock.getBlockList(), false);
					}
				} else {
					Utilities.log(this, "\tFailure: don't have origin BlockedFile", true);
				}
			}
		}
	}

	protected Object next() {
		return sendQueue.remove(0);
	}
}