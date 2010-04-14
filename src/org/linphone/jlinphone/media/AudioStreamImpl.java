package org.linphone.jlinphone.media;

import org.linphone.jortp.Factory;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpSession;

public class AudioStreamImpl implements AudioStream {
	private RecvStream mRecvStream;
	private SendStream mSendStream;
	private AudioStreamParameters mParams;
	private RtpSession mSession;
	public void setParameters(AudioStreamParameters params) {
		mParams=params;
	}
	public void start() throws RtpException {
		mSession=Factory.get().createRtpSession();
		mSession.setLocalAddr(mParams.getLocalAddr());
		mSession.setRemoteAddr(mParams.getRemoteDest());
		mSession.setProfile(mParams.getRtpProfile());
		mSession.setSendPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		
		mRecvStream=new RecvStream(mSession);
		mSendStream=new SendStream(mSession);
		mRecvStream.start();
		mSendStream.start();
	}
	public void stop() {
		mRecvStream.stop();
		mSendStream.stop();
		mSession.close();
		
	}
	
}
