package atrium;

import java.nio.file.Files;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;

import data.Data;
import data.DataTypes;
import io.BlockedFile;
import io.StreamedBlock;
import requests.Request;
import requests.RequestTypes;

public class BlockListener extends TcpIdleSender {

	private String blockOrigin;
	private String blockName;
	private ArrayList<Object> sendQueue;

	public BlockListener() {
		sendQueue = new ArrayList<Object> ();
	}

	public void received(Connection connection, Object object) {
		if(object instanceof Request) {
			Peer foundPeer = Peer.findPeer(connection);
			Request request = (Request) object;
			if(request.getType().equals(RequestTypes.BLOCK)) {
				Utilities.log(this, "Received request for block:");
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
							searchRes = FileUtils.findBlockRAF(foundBlock, blockPosition);
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
		}
	}

	protected Object next() {
		if(!sendQueue.isEmpty()) {
			return sendQueue.remove(0);
		}
		return null;
	}

}
