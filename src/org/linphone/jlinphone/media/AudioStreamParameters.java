package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;

public interface AudioStreamParameters {
	void setLocalAddr(SocketAddress addr);
	SocketAddress getLocalAddr();
	void setRemoteDest(SocketAddress addr);
	SocketAddress getRemoteDest();
	void setRtpProfile(RtpProfile prof);
	RtpProfile getRtpProfile();
	void setActivePayloadTypeNumber(int pt);
	int getActivePayloadTypeNumber();
}
