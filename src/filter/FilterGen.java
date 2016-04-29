package filter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Debug class used to generate an internal "badlist"
 * 
 * @author Kevin Cai
 */
public class FilterGen {

	public static void main(String[] args) {
		System.out.println("Standing by to write badfile");
		ArrayList<String> badFileArrayList = new ArrayList<String>();
		Scanner in = new Scanner(System.in);
		while (in.hasNext()) {
			String got = in.nextLine();
			if (got.length() > 0) {
				if (got.equalsIgnoreCase("QUIT")) {
					System.out.println("Detected end of input");
					break;
				} else {
					badFileArrayList.add(toHexString(got));
					System.out.println("\tHex conversion of [" + got + "] logged: [" + toHexString(got) + "]");
				}
			}
		}
		in.close();
		System.out.println();
		System.out.println();
		System.out.println("Writing badfile to badfilter.dat");
		try {
			PrintWriter writer = new PrintWriter("badfilter.dat", "UTF-8");
			for (String str : badFileArrayList) {
				writer.println(str);
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(badFileArrayList);
	}

	public static String toHexString(String strInput) {
		byte[] ba = strInput.getBytes();
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < ba.length; i++)
			str.append(String.format("%x", ba[i]));
		return str.toString();
	}
}
