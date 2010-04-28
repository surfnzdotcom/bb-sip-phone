package org.linphone.jlinphone.media;

import org.linphone.jortp.Factory;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.SocketAddress;

public class AudioStreamImpl implements AudioStream {
	private RecvStream mRecvStream;
	private SendStream mSendStream;
	private AudioStreamParameters mParams;
	private RtpSession mSession;
	
	public void stop() {
		mRecvStream.stop();
		mSendStream.stop();
		mSession.close();
	}
	public void init(SocketAddress local) throws RtpException {
		mSession=Factory.get().createRtpSession();
		mSession.setLocalAddr(local);
	}
	public void start(AudioStreamParameters params) throws RtpException {
		mSession.setRemoteAddr(mParams.getRemoteDest());
		mSession.setProfile(mParams.getRtpProfile());
		mSession.setSendPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		
		mRecvStream=new RecvStream(mSession);
		mSendStream=new SendStream(mSession);
		mRecvStream.start();
		mSendStream.start();
	}
	
}
