package uzresk.crypto.main;

import java.util.Scanner;

import uzresk.crypto.Encryptor;
import uzresk.crypto.StdEncryptor;

public class StdEncryptorMain {

	private Encryptor encryptor = null;

	public static void main(String[] args) throws Exception {

		new StdEncryptorMain().run();
	}

	public void run() throws Exception {
		encryptor = new StdEncryptor();

		System.out.println("input database password.");
		Scanner scan = new Scanner(System.in);
		String databasePassword = scan.next();
		String encrypted = encrypt(databasePassword);

		System.out.println("encrypted[" + encrypted + "]");

		String decrypted = decrypt(encrypted);
		System.out.println("decrypted check.["
				+ databasePassword.equals(decrypted) + "]");
	}

	public String encrypt(String plain) {
		return encryptor.encrypt(plain);
	}

	public String decrypt(String encrypted) {
		return encryptor.decrypt(encrypted);
	}

}
