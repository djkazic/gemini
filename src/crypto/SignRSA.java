package crypto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

import atrium.Utilities;

public class SignRSA {

	private PublicKey pubkey;

	public SignRSA(String pubKey) {
		Utilities.log(this, "Loaded SignRSA key [" + pubKey.substring(0, 5) + "]", false);
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(pubKey));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(keySpec);
			this.pubkey = pk;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Verifies if a file has been signed by this key
	 * @param in
	 * @param pk
	 * @param signature
	 * @return
	 */
	public boolean verify(String in, String signature) {
		try {
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initVerify(pubkey);
			sig.update(in.getBytes("UTF8"));
			return sig.verify(signature.getBytes("ISO-8859-1"));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
