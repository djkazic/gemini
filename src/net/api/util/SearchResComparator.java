package net.api.util;

import io.FileUtils;

import java.util.Comparator;

public class SearchResComparator implements Comparator<String[]> {

	@Override
	public int compare(String[] o1, String[] o2) {
		// TODO Auto-generated method stub
		return FileUtils.removeExtension(o1[0]).compareTo(FileUtils.removeExtension(o2[0]));
	}

}
