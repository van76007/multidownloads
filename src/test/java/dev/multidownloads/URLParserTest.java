package dev.multidownloads;

import java.util.Arrays;
import java.util.List;

import dev.multidownloads.builder.URLParser;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class URLParserTest extends TestCase {
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public URLParserTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( URLParserTest.class );
    }
    
    public void testValidateValidProtocol() {
    	List<String> urls = Arrays.asList(
    			"http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf",
    			"ftp://mirrors.dotsrc.org/debian-cd/8.7.1/amd64/jigdo-bd/debian-8.7.1-amd64-BD-1.jigdo");
    	
    	for (String url : urls) {
    		DownloadInfor infor = new DownloadInfor();
        	infor.setUrl(url);
        	
        	URLParser parser = new URLParser();
        	parser.setAndValidateProtocol(infor);
        	
        	assertTrue(infor.isValid());
        	Protocol protocol = infor.getProtocol();
        	assertNotNull(protocol);
        	assertNotNull(Protocol.getEnum(protocol.toString().toLowerCase()));
    	}
    }
    
    public void testValidateInValidProtocol() {
    	DownloadInfor infor = new DownloadInfor();
    	infor.setUrl("sftp://www.a.com");
    	
    	URLParser parser = new URLParser();
    	parser.setAndValidateProtocol(infor);
    	
    	assertFalse(infor.isValid());
    	assertNull(infor.getProtocol());
    }
    
    public void testValidateValidFileName() {
    	DownloadInfor infor = new DownloadInfor();
    	infor.setUrl("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls.pdf");
    	
    	URLParser parser = new URLParser();
    	parser.setAndValidateFileName(infor);
    	
    	assertTrue(infor.isValid());
    }
    
    public void testValidateInValidFileName() {
    	DownloadInfor infor = new DownloadInfor();
    	infor.setUrl("http://www.freeclassicebooks.com/Louisa%20May%20Alcott/A%20Garland%20For%20Girls..pdf");
    	
    	URLParser parser = new URLParser();
    	parser.setAndValidateFileName(infor);
    	
    	assertTrue(infor.isValid());
    }
}
