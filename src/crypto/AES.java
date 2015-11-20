package crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	private SecretKeySpec key;
	
	public AES(String mutex) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] keyBytes = mutex.getBytes("ISO-8859-1");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		keyBytes = sha.digest(keyBytes);
		keyBytes = Arrays.copyOf(keyBytes, 16);
		key = new SecretKeySpec(keyBytes, "AES");
	}
	
	public String encrypt(String in) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return new String(cipher.doFinal(in.getBytes()), "ISO-8859-1");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public String decrypt(String in) {
		try {
			Cipher decipher = Cipher.getInstance("AES");
			decipher.init(Cipher.DECRYPT_MODE, key);
			String output = new String(decipher.doFinal(in.getBytes("ISO-8859-1")));
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}