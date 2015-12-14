package net.listeners;

import java.nio.file.Files;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;

import atrium.Core;
import atrium.Peer;
import atrium.Utilities;
import io.BlockedFile;
import io.FileUtils;
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
		connection.setIdleThreshold(0.3f);
		super.idle(connection);
	}

	public void received(final Connection connection, Object object) {
		if(object instanceof Request) {
			final Request request = (Request) object;
			if(request.getType().equals(RequestTypes.BLOCK)) {
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
								ex.printStackTrace();
							}
						} else {
							if(foundBlock.isComplete()) {
								//Attempt complete search
								searchRes = FileUtils.findBlockFromComplete(foundBlock, blockPosition);
							}
						}

						if(searchRes != null) {
							Utilities.log(this, "\tSending back block " + blockName, true);
							sendQueue.add(new Data(DataTypes.BLOCK, new StreamedBlock(blockOriginChecksum, blockName, searchRes)));
							//blockConn.sendTCP(new Data(DataTypes.BLOCK, new StreamedBlock(blockOrigin, blockName, searchRes)));
						} else {
							Utilities.log(this, "\tFailure: could not find block " + blockName, false);
						}
					} else {
						Utilities.log(this, "\tFailure: BlockedFile block mismatch; blockList: " 
								      + foundBlock.getBlockList(), false);
					}
				} else {
					Utilities.log(this, "\tFailure: don't have origin BlockedFile", false);
				}
			}
		}
	}

	protected Object next() {
		if(!sendQueue.isEmpty()) {
			return sendQueue.remove(0);
		}
		return null;
	}

}
