package net.listeners;

import java.nio.file.Files;
import java.util.HashMap;
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

public class BlockListener extends TcpIdleSender {

	private static HashMap<Connection, Data> sendQueue;
	private String blockOriginChecksum;
	private String blockName;

	public BlockListener() {
		if (sendQueue == null) {
			sendQueue = new HashMap<Connection, Data> ();
		}
	}

	public void idle(Connection connection) {
		if (!sendQueue.isEmpty()) {
			Data sendObj = sendQueue.get(connection);
			if (sendObj != null) {
				connection.sendTCP(sendObj);
				sendQueue.remove(connection);
			}
		}
	}

	public void received(final Connection connection, Object object) {
		if (object instanceof Data) {
			final Data request = (Data) object;

			if (request.getType() == (DataTypes.BLOCK_REQS)) {
				Peer foundPeer = Peer.findPeer(connection);
				String[] encryptedBlock = (String[]) request.getPayload();
				blockOriginChecksum = foundPeer.getAES().decrypt(encryptedBlock[0]);
				blockName = foundPeer.getAES().decrypt(encryptedBlock[1]);

				// TODO: search for block
				BlockedFile foundBlock;
				if ((foundBlock = FileUtils.getBlockedFile(blockOriginChecksum)) != null) {
					int blockPosition;
					if ((blockPosition = foundBlock.getBlockList().indexOf(blockName)) != -1) {
						byte[] searchRes = null;
						if (!foundBlock.isComplete() || Core.config.hubMode) {
							// Attempt incomplete search
							try {
								searchRes = Files.readAllBytes(FileUtils.findBlockAppData(foundBlock, blockName).toPath());
							} catch (Exception ex) {
								Utilities.log(this, "Received request for block we do not yet have", true);
							}
						} else {
							if (foundBlock.isComplete()) {
								// Attempt complete search
								searchRes = FileUtils.findBlockFromComplete(foundBlock, blockPosition);
							}
						}

						if (searchRes != null) {
							Utilities.log(this, "\tReadying block " + blockName, true);
							StreamedBlock sb = new StreamedBlock(blockOriginChecksum, blockName, searchRes);
							boolean dupe = sendQueue.containsValue(sb);
							if (!dupe) {
								sendQueue.put(connection, new Data(DataTypes.BLOCK_DATA, sb));
							} else {
								Utilities.log(this, "Duplicate detected in BlockListener HashMap", true);
							}
						} else {
							Utilities.log(this, "\tFailure: could not find block " + blockName, true);
						}
					} else {
						Utilities.log(this,
								"\tFailure: BlockedFile block mismatch; blockList: " + foundBlock.getBlockList(),
								false);
					}
				} else {
					Utilities.log(this, "\tFailure: don't have origin BlockedFile", true);
				}
			}
		}
	}

	@Override
	protected Object next() {
		return null;
	}
}