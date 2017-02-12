package dev.multidownloads.factory;

import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.FTPProber;
import dev.multidownloads.prober.HTTPProber;
import dev.multidownloads.prober.Prober;

public class ProberFactory {
	public static Prober getProbe(DownloadInfor infor) {
		if (Protocol.HTTP == infor.getProtocol()) {
			return new HTTPProber();
		}
		if (Protocol.FTP == infor.getProtocol()) {
			return new FTPProber();
		}
		return null;
	}
}
