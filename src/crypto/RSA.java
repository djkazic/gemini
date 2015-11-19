package crypto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

public class RSA {
	
	private KeyPairGenerator kpg;
	private KeyPair myPair;
	
	/**
	 * Encrypts a string using a generated key, and generates a key pair if not there
	 * @param str
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public SealedObject encrypt(String str) throws InvalidKeyException, 
												   IllegalBlockSizeException, 
												   IOException, 
												   NoSuchAlgorithmException, 
												   NoSuchPaddingException {
		if(myPair == null) {
			kpg = KeyPairGenerator.getInstance("RSA");
			myPair = kpg.generateKeyPair();
		}
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, myPair.getPublic());
		return new SealedObject(str, cipher);
	}
	
	public String decrypt(SealedObject in, KeyPair extPair) throws NoSuchAlgorithmException, 
	                                                               NoSuchPaddingException, 
	                                                               InvalidKeyException, 
	                                                               ClassNotFoundException, 
	                                                               IllegalBlockSizeException, 
	                                                               BadPaddingException, IOException {
		Cipher dec = Cipher.getInstance("RSA");
		dec.init(Cipher.DECRYPT_MODE, extPair.getPrivate());
		return (String) in.getObject(dec);
	}
}
