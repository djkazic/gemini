package filter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

import atrium.Utilities;

/**
 * Object representing filters that are applied on search replies (adult) + generation of BlockedFile
 * And search request responses (hardcode) for a separate, internal "bad" list
 * @author Kevin Cai
 */
public class FilterUtils {

	private static ArrayList<String> badFilter;         //internal res, stored as hex
	private static ArrayList<String> adultFilter;       //adultfilter.dat
	private static ArrayList<String> extensionFilter;   //extfilter.dat
	
	public static void init() {
		try {
			badFilter = tryToLoadFilter(0);
			adultFilter = tryToLoadFilter(1);
			extensionFilter = tryToLoadFilter(2);
		} catch(Exception ex) {
			Utilities.log("filter.FilterUtils", "Exception in loading filters: ", false);
			ex.printStackTrace();
		}
	}
	
	public static boolean adultFilter(String input) {
		if(!mandatoryFilter(input)) {
			return false;
		} else {
			String lowercaseVer = input.toLowerCase();
			for(String term : adultFilter) {
				if(lowercaseVer.contains(term)) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static boolean mandatoryFilter(String input) {
		String lowercaseVer = input.toLowerCase();
		for(String term : badFilter) {
			if(lowercaseVer.contains(term)) {
				return false;
			}
		}
		int ind = input.lastIndexOf(".");
		if(ind > 0) {
			String extension = input.substring(ind + 1);
			return extensionFilter.contains(extension.toLowerCase());
		} else {
			Utilities.log("filter.FilterUtils", "Rejected [" + input + "]", true);
			return false;
		}
	}
	
	private static ArrayList<String> tryToLoadFilter(int switchInt) throws URISyntaxException {
		InputStream filterToLoad = null;
		switch(switchInt) {
			case 0:
				filterToLoad = FilterUtils.class.getResourceAsStream("/res/filterres/badfilter.dat");
				break;
			
			case 1:
				filterToLoad = FilterUtils.class.getResourceAsStream("/res/filterres/adultfilter.dat");
				break;
				
			case 2:
				filterToLoad = FilterUtils.class.getResourceAsStream("/res/filterres/extfilter.dat");
				break;
		}
		if(filterToLoad != null) {
			try {
				ArrayList<String> output = new ArrayList<String> ();
				BufferedReader br = new BufferedReader(new InputStreamReader(filterToLoad));
				String line;
				while((line = br.readLine()) != null && !line.equals("")) {
					output.add(fromHexString(line));
				}
				br.close();
				return output;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	private static String fromHexString(String hex) {
	    StringBuilder str = new StringBuilder();
	    for (int i = 0; i < hex.length(); i+=2) {
	        str.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
	    }
	    return str.toString();
	}

	public static boolean extensionOnly(String input) {
		return extensionFilter.contains(input.toLowerCase());
	}
}