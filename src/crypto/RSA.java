package crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

/**
 * RSA cryptography helper class
 * @author Kevin Cai
 */
public class RSA {
	
	private KeyPairGenerator kpg;
	public static String pubKey;
	public KeyPair myPair;
	
	/**
	 * Initializes a KeyPairGenerator, then stores it
	 * @throws NoSuchAlgorithmException
	 */
	public RSA() throws NoSuchAlgorithmException {
		kpg = KeyPairGenerator.getInstance("RSA");
		myPair = kpg.generateKeyPair();
		byte[] pubKeyBytes = myPair.getPublic().getEncoded();
		RSA.pubKey = new String(DatatypeConverter.printBase64Binary(pubKeyBytes));
	}
	
	/**
	 * Encrypts a string using our public key
	 * @param str input string
	 * @param pk public key used to encrypt
	 * @return encrypted data
	 */
	public String encrypt(String str, PublicKey pk) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			return new String(cipher.doFinal(str.getBytes()), "ISO-8859-1");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Decrypts an input string data using our public key
	 * @param in input data
	 * @return the decrypted string
	 */
	public String decrypt(String in) {
		try {
			Cipher decipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			decipher.init(Cipher.DECRYPT_MODE, myPair.getPrivate());
			String output = new String(decipher.doFinal(in.getBytes("ISO-8859-1")));
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
