package uzresk.crypto;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public interface Encryptor {

	public String encrypt(String plain);

	public String decrypt(String encrypted);

	default public ResourceBundle load() {
		String propPath = System.getProperty("key.propPath");
		if (propPath == null) {
			return null;
		}
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					propPath));

			ResourceBundle bundle = new PropertyResourceBundle(in);

			return bundle;
		} catch (Exception e) {
			return null;
		}
	}
}
