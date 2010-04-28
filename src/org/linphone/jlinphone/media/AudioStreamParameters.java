package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;

public class AudioStreamParameters {
	SocketAddress mRemoteDest;
	RtpProfile mProfile;
	int mActivePt;
	
	public void setRemoteDest(SocketAddress addr){
		mRemoteDest=addr;
	}
	public SocketAddress getRemoteDest(){
		return mRemoteDest;
	}
	public void setRtpProfile(RtpProfile prof){
		mProfile=prof;
	}
	public RtpProfile getRtpProfile(){
		return mProfile;
	}
	public void setActivePayloadTypeNumber(int pt){
		mActivePt=pt;
	}
	public int getActivePayloadTypeNumber(){
		return mActivePt;
	}
}
