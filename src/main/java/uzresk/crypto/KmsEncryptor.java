package uzresk.crypto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Base64;
import java.util.ResourceBundle;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class KmsEncryptor implements Encryptor {

	String bucketName = null;

	String s3Region = null;

	String keyName = null;

	String kmsEndpoint = null;

	public KmsEncryptor() {
		ResourceBundle bundle = load();
		if (bundle == null) {
			throw new RuntimeException("missing resource file.["
					+ System.getProperty("key.propPath") + "]");
		}
		bucketName = bundle.getString("bucket.name");
		s3Region = bundle.getString("s3.region");
		keyName = bundle.getString("key.name");
		kmsEndpoint = bundle.getString("kms.endpoint");

		if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(s3Region)
				|| StringUtils.isEmpty(keyName)
				|| StringUtils.isEmpty(kmsEndpoint)) {
			throw new RuntimeException("invalid resource file.");
		}
	}

	@Override
	public String encrypt(String plain) {
		return CryptoUtils.encrypt(plain, provideDataKey());
	}

	@Override
	public String decrypt(String encrypted) {
		return CryptoUtils.decrypt(encrypted, provideDataKey());
	}

	protected Key provideDataKey() {
		// get encrypted key
		AmazonS3 s3 = new AmazonS3Client(
				new ClasspathPropertiesFileCredentialsProvider());
		s3.setRegion(com.amazonaws.services.s3.model.Region.fromValue(s3Region)
				.toAWSRegion());

		S3ObjectInputStream s3ois = s3.getObject(
				new GetObjectRequest(bucketName, keyName)).getObjectContent();
		String encryptedKey = new BufferedReader(new InputStreamReader(s3ois))
				.lines().findFirst().get();

		byte[] base64DecodedKey = Base64.getDecoder().decode(encryptedKey);
		ByteBuffer decodedKey = ByteBuffer.allocate(base64DecodedKey.length);
		decodedKey.put(base64DecodedKey);
		decodedKey.flip();

		AWSKMSClient kmsClient = new AWSKMSClient(
				new ClasspathPropertiesFileCredentialsProvider());
		kmsClient.setEndpoint(kmsEndpoint);

		DecryptRequest decryptRequest = new DecryptRequest()
				.withCiphertextBlob(decodedKey);
		ByteBuffer plainText = kmsClient.decrypt(decryptRequest).getPlaintext();

		return makeKey(plainText);
	}

	private Key makeKey(ByteBuffer key) {
		return new SecretKeySpec(getByteArray(key), "AES");
	}

	private byte[] getByteArray(ByteBuffer b) {
		byte[] byteArray = new byte[b.remaining()];
		b.get(byteArray);
		return byteArray;
	}
}
