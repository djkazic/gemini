package filter;

import java.io.File;
import java.util.ArrayList;

import io.FileUtils;

/**
 * Object representing filters that are applied on search replies (adult)
 * And search request responses (hardcode) for a separate, internal "bad" list
 * @author Kevin Cai
 */
public class FilterUtils {

	private ArrayList<String> badFilter;         //internal res, stored as hex
	private ArrayList<String> adultFilter;       //adultfilter.dat
	private ArrayList<String> extensionFilter;   //extfilter.dat
	
	public FilterUtils() {
		if(adultFilter == null) {
			adultFilter = tryToLoadFilter(0);
		}
		
		if(extensionFilter == null) {
			extensionFilter = tryToLoadFilter(1);
		}
	}
	
	private ArrayList<String> tryToLoadFilter(int switchInt) {
		File filterToLoad = null;
		switch(switchInt) {
			case 0:
				filterToLoad = new File(FileUtils.getConfigDir() + "/adultfilter.dat");
				break;
				
			case 1:
				filterToLoad = new File(FileUtils.getConfigDir() + "/extfilter.dat");
				break;
		}
		if(filterToLoad.exists()) {
			try {
				ArrayList<String> output = new ArrayList<String> ();
				/**
				for(Object o : filterRead) {
					if(o instanceof String) {
						output.add((String) o);
					}
				}
				**/
				return output;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}