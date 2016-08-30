package edu.cornell.cs.apl.makepassword;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.AbstractPreferences;
import org.apache.commons.codec.binary.Base64;

public class Pwgen {

	final String base64_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	final String punctuation = ".!#%&()*+,-./<=>?";
	final String digits = "0123456789";

	public Pwgen() {
	}

	public String generate(String id, byte[] material, int key_length, String passphrase, boolean shuffle,
			boolean alpha, boolean numericonly, Procedure<String> crash) {
		id = id + passphrase;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			crash.apply(e.getMessage());
		}
		try {
			md.reset();
			md.update(id.getBytes("UTF-8"));
			md.update(material, 0, key_length);
		} catch (UnsupportedEncodingException e) {
			crash.apply(e.getMessage());
		}
		byte[] digest = md.digest();
		// Base64.Encoder encoder = Base64.getEncoder();
		Base64 encoder = new Base64();
		byte[] encoding = encoder.encode(digest);
		String s = null;
		try {
			s = new String(encoding, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			crash.apply(e.getMessage());
		}
		String prefix = s.substring(0, 5);
		int i6 = base64_chars.indexOf(encoding[5]);
		int i7 = base64_chars.indexOf(encoding[6]);
		int i8 = base64_chars.indexOf(encoding[7]);
		char p6 = punctuation.charAt(i6 % punctuation.length());
		char p7 = digits.charAt(i7 % 10);
		char p8 = digits.charAt(i8 % 10);
		String unshuffled = prefix + p6 + p7 + p8;

		if (alpha) {
			String alphanumeric = base64_chars.substring(0, 62);
			StringBuilder b = new StringBuilder();
			for (int j = 0; j < 8; j++) {
				int i = base64_chars.indexOf(encoding[j]);
				b.append(alphanumeric.charAt(i % 62));
			}
			unshuffled = b.toString();
		} else if (numericonly) {
			StringBuilder b = new StringBuilder();
			for (int j = 0; j < 8; j++) {
				int i = base64_chars.indexOf(encoding[j]);
				b.append(digits.charAt(i % 10));
			}
			unshuffled = b.toString();
		}
		String result = unshuffled;
		if (shuffle) {
			StringBuilder b = new StringBuilder();
			int i9 = base64_chars.indexOf(encoding[8]);
			int i10 = base64_chars.indexOf(encoding[9]);
			int i11 = base64_chars.indexOf(encoding[10]);
			int perm = (i9 * 4096 + i10 * 64 + i11) % 40320;
			boolean[] used = new boolean[8];
			for (int i = 8; i >= 1; i--) {
				int j = perm % i;
				perm /= i;
				int k;
				// select the jth unused char
				for (k = 0; j != 0 || used[k]; k++)
					if (!used[k])
						j--;
				used[k] = true;
				b.append(unshuffled.charAt(k));
			}
			result = b.toString();
		}
		return result;
	}

}
