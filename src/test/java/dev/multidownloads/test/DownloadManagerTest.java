package dev.multidownloads.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.multidownloads.DownloadManager;
import dev.multidownloads.builder.CatalogBuilder;
import dev.multidownloads.builder.CatalogReader;
import junit.framework.TestCase;

/**
 * Integration test for DownloadManager class
 * 
 * @author vanvu
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadManagerTest extends TestCase {
	private static final int LEN = 4;
	DownloadManager manager;
	CatalogReader[] readers = new CatalogReader[LEN];
	CatalogBuilder[] builders = new CatalogBuilder[LEN];

	@Before
	public void setUp() {
		manager = new DownloadManager();

		for (int i = 0; i < LEN; i++) {
			readers[i] = Mockito.spy(new CatalogReader());
			Mockito.when(readers[i].readDownloadCatalog(Mockito.anyString())).thenReturn(getSampleDownloadList(i));
		}
	}

	private List<String> getSampleDownloadList(int i) {
		switch (i) {
		case 0:
			return Arrays.asList("http://www.freeclassicebooks.com/Agatha%20Christie/The%20Secret%20Adversary.pdf",
					"http://www.freeclassicebooks.com/charlotte%20bronte/Jane%20Eyre.pdf");
		case 1:
			return Arrays.asList(
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.template",
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
		case 2:
			return Arrays.asList("http://dl.my-film.org/reza/film/Coral.Reef.Adventure.2003.IMAX.720p-[My-Film].mkv",
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.template",
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
		case 3:
			return Arrays.asList("http://www.freeclassicebooks.com/Agatha%20Christie/The%20Secret%20Adversary.pdf",
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.template;A;A",
					"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
		default:
			return null;
		}
	}

	/**
	 * Test download multiple files only via HTP protocol
	 */
	@Test
	public void testDownloadHTTP() {
		builders[0] = new CatalogBuilder();
		builders[0].setReader(readers[0]);
		manager.setCatalogBuilder(builders[0]);

		boolean downloadResult = manager.download("");

		if (downloadResult) {
			try {
				StringBuilder sb1 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha1 = calculateSHA1OfFile(sb1.append("Jane%20Eyre.pdf").toString());
				assertEquals("3bb7131d221a351cf0cc9d27968fe730abbfedfd", sha1);

				StringBuilder sb2 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha2 = calculateSHA1OfFile(sb2.append("The%20Secret%20Adversary.pdf").toString());
				assertEquals("d872abc65a2f576e03d0cc3c79b57019220f7e14", sha2);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		cleanup(manager.getCatalogBuilder().getDownloadDirectory());
	}

	/**
	 * Test download multiple files only via FTP protocol
	 */
	@Test
	public void testDownloadFTP() {
		builders[1] = new CatalogBuilder();
		builders[1].setReader(readers[1]);
		manager.setCatalogBuilder(builders[1]);

		boolean downloadResult = manager.download("");

		if (downloadResult) {
			try {
				StringBuilder sb1 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha1 = calculateSHA1OfFile(sb1.append("debian-8.7.1-amd64-BD-1.template").toString());
				assertEquals("213afc90291ba68f5734e7775f80602e16eb6f42", sha1);

				StringBuilder sb2 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha2 = calculateSHA1OfFile(sb2.append("debian-8.7.1-amd64-BD-1.jigdo").toString());
				assertEquals("b804bcbad2d3e5fd8e4d809b9cf55b1dd72866e5", sha2);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		cleanup(manager.getCatalogBuilder().getDownloadDirectory());
	}

	/**
	 * Test download multiple files via HTP&FTP protocol
	 */
	@Test
	public void testDownloadFTPAndHTTP() {
		builders[2] = new CatalogBuilder();
		builders[2].setReader(readers[2]);
		manager.setCatalogBuilder(builders[2]);

		boolean downloadResult = manager.download("");

		if (downloadResult) {
			try {
				StringBuilder sb1 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha1 = calculateSHA1OfFile(
						sb1.append("Coral.Reef.Adventure.2003.IMAX.720p-[My-Film].mkv").toString());
				assertEquals("5d28eb107cc2879d5eb24d7bb131ccdc01a394d0", sha1);

				StringBuilder sb2 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha2 = calculateSHA1OfFile(sb2.append("debian-8.7.1-amd64-BD-1.template").toString());
				assertEquals("213afc90291ba68f5734e7775f80602e16eb6f42", sha2);

				StringBuilder sb3 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
				String sha3 = calculateSHA1OfFile(sb3.append("debian-8.7.1-amd64-BD-1.jigdo").toString());
				assertEquals("b804bcbad2d3e5fd8e4d809b9cf55b1dd72866e5", sha3);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		cleanup(manager.getCatalogBuilder().getDownloadDirectory());
	}

	/**
	 * Test download multiple files should eventually return even if 1 download
	 * terminated and we retry for a few times Here we simulate wrong
	 * credentials A/AA for FTP login
	 */
	@Test
	public void testDownloadIfFailedShouldReturn() {
		builders[3] = new CatalogBuilder();
		builders[3].setReader(readers[3]);
		manager.setCatalogBuilder(builders[3]);

		manager.download("");

		try {
			StringBuilder sb1 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
			File downloadedFile = new File(sb1.append("debian-8.7.1-amd64-BD-1.template").toString());
			assertFalse(downloadedFile.exists());

			StringBuilder sb2 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
			String sha2 = calculateSHA1OfFile(sb2.append("The%20Secret%20Adversary.pdf").toString());
			assertEquals("d872abc65a2f576e03d0cc3c79b57019220f7e14", sha2);

			StringBuilder sb3 = new StringBuilder(manager.getCatalogBuilder().getDownloadDirectory());
			String sha3 = calculateSHA1OfFile(sb3.append("debian-8.7.1-amd64-BD-1.jigdo").toString());
			assertEquals("b804bcbad2d3e5fd8e4d809b9cf55b1dd72866e5", sha3);
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		cleanup(manager.getCatalogBuilder().getDownloadDirectory());
	}

	private String calculateSHA1OfFile(String datafile) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(datafile);
		byte[] dataBytes = new byte[1024];

		int nread = 0;

		while ((nread = fis.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		}
		fis.close();

		byte[] mdbytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	private void cleanup(String downloadDirectory) throws SecurityException {
		File folder = new File(downloadDirectory);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			file.delete();
		}
	}
}
