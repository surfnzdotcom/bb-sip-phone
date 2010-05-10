package org.linphone.jlinphone.media;

import java.util.Timer;
import java.util.TimerTask;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.SocketAddress;

public class AudioStreamEchoImpl implements AudioStream {
	private AudioStreamParameters mParams;
	private RtpSession mSession;
	private Timer mTimer = new Timer();
	
	class EchoTask extends TimerTask{
		private long mTime;
		private RtpSession mSession;
		private int mClockrate;
		
		EchoTask(){
			mSession=AudioStreamEchoImpl.this.mSession;
			RtpProfile prof=mSession.getProfile();
			PayloadType pt=prof.getPayloadType(mSession.getSendPayloadTypeNumber());
			mClockrate=pt.getClockRate();
		}
		public void run() {
			int ts=(int)mTime*mClockrate;

			RtpPacket p=null;;
			try {
				p=mSession.recvPacket(ts);
				if (p!=null){

					mSession.sendPacket(p, ts);

				}
			} catch (RtpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void init(SocketAddress local) throws RtpException {
		mSession=JOrtpFactory.instance().createRtpSession();
		mSession.setLocalAddr(local);
	}

	public void start(AudioStreamParameters params) throws RtpException{
		mParams=params;
		
		mSession.setRemoteAddr(mParams.getRemoteDest());
		mSession.setProfile(mParams.getRtpProfile());
		mSession.setSendPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mTimer=new Timer();
		mTimer.scheduleAtFixedRate(new EchoTask(), 0, 10);
	}

	public void stop() {
		mTimer.cancel();
		mSession.close();
	}

}
