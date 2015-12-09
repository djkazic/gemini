package crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES cryptography helper class.
 * @author Kevin Cai
 */
public class AES {
	
	private SecretKeySpec key;
	
	/**
	 * Instantiates a new instance of the AES object
	 *
	 * @param mutex mutex value
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public AES(String mutex) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] keyBytes = mutex.getBytes("ISO-8859-1");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		keyBytes = sha.digest(keyBytes);
		keyBytes = Arrays.copyOf(keyBytes, 16);
		key = new SecretKeySpec(keyBytes, "AES");
	}
	
	/**
	 * Encrypts a string
	 *
	 * @param in value provided
	 * @return encrypted string
	 */
	public String encrypt(String in) {
		try {
			return new String(encrypt(in.getBytes()), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Encrypt a byte array
	 *
	 * @param in value provided
	 * @return encrypted byte[]
	 */
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
	
	/**
	 * Decrypts a string
	 *
	 * @param in value provided
	 * @return decrypted string
	 */
	public String decrypt(String in) {
		try {
			return new String(decrypt(in.getBytes("ISO-8859-1")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Decrypts a byte array
	 *
	 * @param in value provided
	 * @return decrypted byte[]
	 */
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
	
	/**
	 * Gets an instance of the AES cipher object
	 *
	 * @param enc toggle for encryption or decryption
	 * @return cipher instance
	 */
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