package net.api.util;

import io.block.BlockedFile;

import java.util.Comparator;

public class BlockedFileComparator implements Comparator<BlockedFile> {
	
	@Override
	public int compare(BlockedFile b1, BlockedFile b2) {
		return b1.getPointer().getName().compareTo(b2.getPointer().getName());
	}
	
}
