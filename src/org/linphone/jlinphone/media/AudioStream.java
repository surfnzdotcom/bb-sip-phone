package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpException;
import org.linphone.jortp.SocketAddress;

public interface AudioStream {
	public void init(SocketAddress local) throws RtpException;
	public void start(AudioStreamParameters params) throws RtpException;
	public void stop();
}
