package dev.multidownloads.progress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeedInformer {
	final static Logger logger = LogManager.getLogger(SpeedInformer.class);
	
	private String fileName;
	private double speedInKBps;
	private long startTime;
	
	public SpeedInformer(String fileName) {
		this.fileName = fileName;
	}
	
	public double getSpeedInKBps() {
		return speedInKBps;
	}
	
	public void setSpeedInKBps(double speedInKBps) {
		this.speedInKBps = speedInKBps;
		
	}
	
	public synchronized void startCalculatingDownloadSpeed() {
		this.startTime = System.currentTimeMillis();
	}
	
	public synchronized void publishDownloadSpeed(int numberOfCompleteBytes) {
		long currentTime = System.currentTimeMillis();
		this.speedInKBps = (numberOfCompleteBytes * 1000 / (currentTime - startTime)) / 1024D;
		logger.info(String.format("Download speed of %s is %f KBps\n", this.fileName, this.speedInKBps));
	}
}
