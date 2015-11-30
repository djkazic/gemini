package listeners;

import java.nio.file.Files;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;

import atrium.FileUtils;
import atrium.Peer;
import atrium.Utilities;
import io.BlockedFile;
import io.StreamedBlock;
import packets.data.Data;
import packets.data.DataTypes;
import packets.requests.Request;
import packets.requests.RequestTypes;

public class BlockListener extends TcpIdleSender {

	private String blockOrigin;
	private String blockName;
	private ArrayList<Object> sendQueue;

	public BlockListener() {
		sendQueue = new ArrayList<Object> ();
	}

	public void idle(Connection connection) {
		connection.setIdleThreshold(0.4f);
		super.idle(connection);
	}

	public void received(final Connection connection, Object object) {
		if(object instanceof Request) {
			final Request request = (Request) object;
			if(request.getType().equals(RequestTypes.BLOCK)) {
				(new Thread(new Runnable() {
					public void run() {
						Utilities.log(this, "Received request for block:");
						Peer foundPeer = Peer.findPeer(connection);
						String[] encryptedBlock = (String[]) request.getPayload();
						blockOrigin = foundPeer.getAES().decrypt(encryptedBlock[0]);
						blockName = foundPeer.getAES().decrypt(encryptedBlock[1]);

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
									sendQueue.add(new Data(DataTypes.BLOCK, new StreamedBlock(blockOrigin, blockName, searchRes)));
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
