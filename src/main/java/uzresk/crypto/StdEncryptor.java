package uzresk.crypto;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.crypto.spec.SecretKeySpec;

public class StdEncryptor implements Encryptor {

	private Key secretKeySpec;

	private static final String DEFAULT_KEY = "defaultSecretKey";

	public StdEncryptor() {
		ResourceBundle bundle = load();
		String secretKey = DEFAULT_KEY;
		if (bundle != null) {
			secretKey = bundle.getString("secret.key");
		}
		if (secretKey == null || "".equals(secretKey)) {
			secretKey = DEFAULT_KEY;
		}

		this.secretKeySpec = makeKey(secretKey);
	}

	public StdEncryptor(String secretKey) {
		secretKeySpec = makeKey(secretKey);
	}

	@Override
	public String encrypt(String plain) {
		return CryptoUtils.encrypt(plain, secretKeySpec);
	}

	@Override
	public String decrypt(String encrypted) {
		return CryptoUtils.decrypt(encrypted, secretKeySpec);
	}

	public static Key makeKey(String secretKey) {
		try {
			byte[] key = secretKey.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			return new SecretKeySpec(key, "AES");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("encoding unsuppoted.");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("no such algorithm.[SHA-1]");
		}
	}
}
