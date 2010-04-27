package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpException;

public interface AudioStream {
	public void init(AudioStreamParameters params)throws RtpException;
	public void start();
	public void stop();
}
