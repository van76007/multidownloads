package dev.multidownloads.factory;

import dev.multidownloads.model.Protocol;
import dev.multidownloads.prober.FTPProber;
import dev.multidownloads.prober.HTTPProber;
import dev.multidownloads.prober.Prober;

/**
 * This class return different network prober depending on the download
 * protocol. Any future supported protocol will require an ad-on to this class
 * 
 * @author vanvu
 *
 */
public class ProberFactory {
	/**
	 * This method initialize different types of network prober
	 * 
	 * @param proto
	 *            The current supported download protocol
	 * @return A network prober
	 */
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
