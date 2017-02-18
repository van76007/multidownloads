package dev.multidownloads.factory;

import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.FTPProber;
import dev.multidownloads.prober.HTTPProber;
import dev.multidownloads.prober.Prober;

public class ProberFactory {
	public static Prober getProbe(Protocol proto) {
		if (Protocol.HTTP == proto) {
			return new HTTPProber();
		}
		if (Protocol.FTP == proto) {
			return new FTPProber();
		}
		return null;
	}
}
