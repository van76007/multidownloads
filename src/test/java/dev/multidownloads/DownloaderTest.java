package dev.multidownloads;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadLocalRandom;

import dev.multidownloads.downloader.Downloader;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.MockUpdateFileDownloadProgress;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Downloader class
 * Mock network input stream
 */
public class DownloaderTest 
    extends TestCase
{
    private static final int TEST_FILE_LENGTH = 1000*1000;
    private static final String TEST_FILE_NAME = "temp";
    private static final int SEG_SIZE = TEST_FILE_LENGTH / 1000;
    private static final byte A_CHAR = 15;
    private static final byte ZERO = 0;
    
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DownloaderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DownloaderTest.class );
    }

    /**
     * Test download 1 part of the file at random offset
     */
    public void testTransferOnePartAtRandomOffSet()
    {
    	MockUpdateFileDownloadProgress progressListener = new MockUpdateFileDownloadProgress();
    	DownloadInfor infor = new DownloadInfor();
    	
    	int startSeg = ThreadLocalRandom.current().nextInt(0, TEST_FILE_LENGTH - SEG_SIZE);
    	Segmentation seg = new Segmentation(startSeg, startSeg + SEG_SIZE - 1);
    	InputStream input = buildInputStream();
    	
		try {
			RandomAccessFile raf = buildAllZeroTestFile();
			raf.seek(startSeg);
			
			Downloader downloader = new Downloader(infor, seg, progressListener);
	    	downloader.transfer(input, raf, seg);
	    	
	    	int numberOfWrittenFile = returnNumberOfWrittenBytes(raf, startSeg);
	    	assert( numberOfWrittenFile ==  SEG_SIZE);
	    	
	    	raf.close();
	    	new File(TEST_FILE_NAME).delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Simulate a file stream to be mocked as remote file
     * @return a file stream
     */
    private InputStream buildInputStream() {
    	byte[] data = new byte[TEST_FILE_LENGTH];
    	for (int i = 0; i < TEST_FILE_LENGTH; i++) {
    		data[i] = A_CHAR;
    	}
        return new ByteArrayInputStream(data);
    }
    
    /**
     * Build an all-zero file with the size is TEST_FILE_LENGTH
     * @return its output stream with support random access
     * @throws IOException
     */
    private RandomAccessFile buildAllZeroTestFile() throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(TEST_FILE_NAME, "rw");
    	for (int i = 0; i < TEST_FILE_LENGTH; i++) {
    		raf.write(ZERO);
    	}
    	return raf;
    }
    
    private int returnNumberOfWrittenBytes(RandomAccessFile raf, int offset) throws IOException {
    	int count = 0;
    	raf.seek(offset);
    	while(raf.readByte() == A_CHAR) {
    		count++;
    	}
    	return count;
    }
}
