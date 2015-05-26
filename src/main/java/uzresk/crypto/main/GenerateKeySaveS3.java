package uzresk.crypto.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class GenerateKeySaveS3 {

	private String kmsEndpoint = null;
	private String keyId = null;
	private String s3Region = null;
	private String bucketName = null;
	private String keyName = null;

	public static void main(String[] args) {

		new GenerateKeySaveS3().run();

	}

	public void run() {

		ResourceBundle bundle = load();
		if (bundle == null) {
			throw new RuntimeException("missing resource file.");
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
		System.out
				.println("KMSにアクセするためのKEYIDを入力して下さい(ex.arn:aws:kms:ap-northeast-1:194851312534:key/c8889211-ae96-4da1-9e85-01e353215a83)");
		Scanner scan = new Scanner(System.in);
		keyId = scan.next();

		// generate encrypt key
		ByteBuffer encryptedDataKey = generateEncryptKey();

		saveEncryptKey(encryptedDataKey);

		System.out.println("encrypted key upload s3.");
	}

	protected ByteBuffer generateEncryptKey() {
		// get data key
		AWSKMSClient kmsClient = new AWSKMSClient(
				new ClasspathPropertiesFileCredentialsProvider());
		kmsClient.setEndpoint(kmsEndpoint);
		GenerateDataKeyRequest dataKeyRequest = new GenerateDataKeyRequest();
		dataKeyRequest.setKeyId(keyId);
		dataKeyRequest.setKeySpec("AES_128");
		GenerateDataKeyResult dataKeyResult = kmsClient
				.generateDataKey(dataKeyRequest);

		return dataKeyResult.getCiphertextBlob();
	}

	protected void saveEncryptKey(ByteBuffer encryptedDataKey) {
		AmazonS3 s3 = new AmazonS3Client(
				new ClasspathPropertiesFileCredentialsProvider());
		s3.setRegion(com.amazonaws.services.s3.model.Region.fromValue(s3Region)
				.toAWSRegion());

		String base64EncryptedDataKey = Base64.getEncoder().encodeToString(
				getByteArray(encryptedDataKey));

		s3.putObject(new PutObjectRequest(bucketName, keyName,
				createFile(base64EncryptedDataKey)));

	}

	private static File createFile(String base64EncryptedDataKey) {

		File file = null;
		try {
			file = File.createTempFile("tmp", ".txt");
			file.deleteOnExit();

			Writer writer = new OutputStreamWriter(new FileOutputStream(file));
			writer.write(base64EncryptedDataKey);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("can't create file.", e);
		}

		return file;
	}

	public ResourceBundle load() {
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

	private byte[] getByteArray(ByteBuffer b) {
		byte[] byteArray = new byte[b.remaining()];
		b.get(byteArray);
		return byteArray;
	}
}
