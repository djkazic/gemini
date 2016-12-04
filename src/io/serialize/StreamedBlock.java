package io.serialize;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import atrium.Core;
import atrium.Utilities;
import crypto.AES;
import io.FileUtils;
import io.block.BlockedFile;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class StreamedBlock {

	private String originChecksum;
	private String blockName;
	private byte[] fileBytes;

	public StreamedBlock() {
		originChecksum = null;
		blockName = null;
		fileBytes = null;
	}

	public StreamedBlock(String originChecksum, String blockName, byte[] searchRes) {
		this.originChecksum = Core.aes.encrypt(originChecksum);
		this.blockName = Core.aes.encrypt(blockName);
		try {
			if (Core.config.hubMode) {
				// Already encrypted format
				fileBytes = searchRes;
			} else {
				// Encrypt for transmission
				fileBytes = Core.aes.encrypt(searchRes);
			}
		} catch (Exception ex) {
			Utilities.log(this, "Could not get file bytes for StreamedBlock", true);
		}
	}

	public String getOrigin() {
		return originChecksum;
	}

	public byte[] getFileBytes() {
		return fileBytes;
	}

	public boolean equals(Object o) {
		if (o instanceof StreamedBlock) {
			return blockName.equals(((StreamedBlock) o).blockName);
		}
		return false;
	}

	public void insertSelf(final AES aes) {
		Thread insertionThread = (new Thread(new Runnable() {
			public void run() {
				String blockDest = aes.decrypt(blockName);
				byte[] decrypted = aes.decrypt(fileBytes);

				// Match BlockedFile from blockDex by checksum
				BlockedFile bf = FileUtils.getBlockedFile(aes.decrypt(originChecksum));
				if (bf.isComplete()) {
					Utilities.log(this, "Discarding block, BlockedFile is done", true);
				} else {
					try {
						int position = bf.getBlockList().indexOf(blockDest);
						FileChannel fc = bf.getBufferFileChannel();
						MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, position * Core.blockSize, Core.blockSize);
						bb.put(decrypted);
						//bb.force();

						Cleaner cleaner = ((DirectBuffer) bb).cleaner();
						if (cleaner != null) {
							cleaner.clean();
						}
						Utilities.log(this, "Wrote position " + position + " to RAF", false);

						bf.logBlock(blockDest);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}));
		insertionThread.setName(blockName + " Inserter");
		insertionThread.start();
	}
}