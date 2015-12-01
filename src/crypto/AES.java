package crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES cryptography helper class
 * @author Kevin Cai
 */
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
			return new String(encrypt(in.getBytes()), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] encrypt(byte[] in) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(in);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public String decrypt(String in) {
		try {
			return new String(decrypt(in.getBytes("ISO-8859-1")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] in) {
		try {
			Cipher decipher = Cipher.getInstance("AES");
			decipher.init(Cipher.DECRYPT_MODE, key);
			return decipher.doFinal(in);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public Cipher getCipher(boolean enc) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES");
			if(enc) {
				cipher.init(Cipher.ENCRYPT_MODE, key);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cipher;
	}
}