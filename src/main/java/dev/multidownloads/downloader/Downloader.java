package dev.multidownloads.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.multidownloads.config.Config;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Segmentation;
import dev.multidownloads.progress.DownloadListener;

/**
 * This is the base class of all connector to download a remote resource. It
 * transfers a segmentation of the remote stream to the local stream
 * 
 * @author vanvu
 *
 */
public abstract class Downloader {
	final static Logger logger = LogManager.getLogger(Downloader.class);
	
	DownloadListener progressListener;

	/**
	 * The segmentation to be retrieved
	 */
	protected Segmentation seg;
	/**
	 * The download information to keep track of the remote resource name and
	 * total file size to be download
	 */
	protected DownloadInfor infor;

	public Downloader(DownloadInfor infor, Segmentation seg, DownloadListener progressListener) {
		this.infor = infor;
		this.seg = seg;
		this.progressListener = progressListener;
		logger.info("To download a file {} in {}", infor.getFileName(), seg.toString());
	}

	/**
	 * This method transfer a segmentation from the remote stream to a local
	 * file
	 * 
	 * @param input
	 *            Remote stream
	 * @param raf
	 *            Loca file
	 * @param seg
	 *            A segmentation to be transferred. It is actually *this*
	 *            segmentation
	 * @throws IOException
	 */
	public void transfer(InputStream input, RandomAccessFile raf, Segmentation seg) throws IOException {
		int bufSize = Config.getParameterAsInteger("BUFFER_SIZE_IN_KB")* 1024;
		int numByteRead;
		byte data[] = new byte[bufSize];

		int remaining = seg.endByte - seg.startByte + 1;
		while (remaining > 0) {
			int readBufSize = bufSize < remaining ? bufSize : remaining;
			numByteRead = input.read(data, 0, readBufSize);

			raf.write(data, 0, numByteRead);
			progressListener.onUpdate(numByteRead, infor.getFileName());

			seg.startByte += numByteRead;
			remaining -= numByteRead;
		}

		if (remaining < 0) {
			logger.error("Number of Remaining bytes is negative");
		}
	}

	/**
	 * A method to set error when download fails
	 * 
	 * @param seg
	 */
	abstract protected void setError(Segmentation seg);
}
