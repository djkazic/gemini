package crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
	
	public RSA(byte[] pubBytes, byte[] privBytes) {
		try {
			Utilities.log(this, "Loading RSA from file...", false);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
			PublicKey publicKey = kf.generatePublic(pubKeySpec);
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privBytes);
			PrivateKey privKey = kf.generatePrivate(privKeySpec);
			myPair = new KeyPair(publicKey, privKey);
			byte[] pubKeyBytes = publicKey.getEncoded();
			RSA.pubKey = new String(DatatypeConverter.printBase64Binary(pubKeyBytes));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initializes a KeyPairGenerator, then stores it
	 * @throws NoSuchAlgorithmException
	 */
	public RSA() throws NoSuchAlgorithmException {
		Utilities.log(this, "Generating RSA keys...", false);
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
	
	public String sign(String in) {
		try {
			byte[] data = in.getBytes("UTF8");

	        Signature sig = Signature.getInstance("SHA1WithRSA");
	        sig.initSign(myPair.getPrivate());
	        sig.update(data);
	        byte[] signatureBytes = sig.sign();
	        return new String(signatureBytes, "ISO-8859-1");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean verify(String in, PublicKey pk, String signature) {
		try {
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initVerify(pk);
			sig.update(in.getBytes("UTF8"));
			return sig.verify(signature.getBytes("ISO-8859-1"));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public PublicKey rawPublicKey() {
		return myPair.getPublic();
	}
	
	public byte[] publicKeyBytes() {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(myPair.getPublic().getEncoded());
		return x509.getEncoded();
	}
	
	public byte[] privateKeyBytes() {
		PKCS8EncodedKeySpec pkc = new PKCS8EncodedKeySpec(myPair.getPrivate().getEncoded());
		return pkc.getEncoded();
	}
}
