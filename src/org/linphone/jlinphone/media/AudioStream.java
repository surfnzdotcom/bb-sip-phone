package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpException;

public interface AudioStream {
	public void setParameters(AudioStreamParameters params);
	public void start() throws RtpException;
	public void stop();
}
