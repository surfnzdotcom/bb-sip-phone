package org.linphone.jlinphone.core;


import java.util.Vector;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jlinphone.media.AudioStream;
import org.linphone.jlinphone.media.AudioStreamEchoImpl;
import org.linphone.jlinphone.media.AudioStreamParameters;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalStreamDescription;

public class LinphoneCoreImpl implements LinphoneCore {
	Sal mSal;
	LinphoneAuthInfo mAuthInfo;
	LinphoneProxyConfig mProxyCfg;
	Call mCall;
	LinphoneCoreListener mListener;
	AudioStream mAudioStream;
	
	static class CallState {
		static CallState Init = new CallState ("Init");
		static CallState Ringing = new CallState ("Ringing");
		static CallState Running = new CallState ("Running");
		static CallState Terminated = new CallState ("Terminated");
		final String mValue;
		private CallState(String state) {
			mValue = state;
		}
		public String toString () {
			return mValue;
		}
	}
		
	int mSipPort=5060;
	SalListener mSalListener= new SalListener(){

		public void onCallAccepted(SalOp op) {
			Call c=(Call)op.getUserContext();
			if (mCall==null || mCall!=c){
				mSal.callTerminate(op);
			}
			c.mFinal=mSal.getFinalMediaDescription(op);
			startMediaStreams(c);
		}

		public void onCallReceived(SalOp op) {
			if (mCall!=null){
				mSal.callDecline(op, Sal.Reason.Busy, null);
			}else{
				mCall=createIncomingCall(op);
				mListener.inviteReceived(LinphoneCoreImpl.this, 
						op.getFrom());
			}
		}

		public void onCallRinging(SalOp op) {
			if (mCall!=null){
				mCall.mState=CallState.Ringing;
				mListener.displayStatus(LinphoneCoreImpl.this,"Remote ringing...");
			}
		}

		public void onCallTerminated(SalOp op) {
			if (mCall!=null){
				mListener.byeReceived(LinphoneCoreImpl.this, op.getFrom());
				mCall=null;
			}
		}
		
	};
	
	private class Call{
		CallDirection mDir;
		CallState mState;
		SalOp mOp;
		SalMediaDescription mLocalDesc;
		SalMediaDescription mFinal;
		
		private Call(SalOp op, CallDirection dir){
			mState=CallState.Init;
			mOp=op;
			mDir=dir;
			mOp.setUserContext(this);
			mLocalDesc=makeLocalDesc();
		}
		
		public LinphoneAddress getRemoteAddress(){
			if (mOp!=null){
				if (mDir==CallDirection.Incoming){
					return LinphoneCoreFactory.instance().createLinphoneAddress(mOp.getFrom());
				}else return LinphoneCoreFactory.instance().createLinphoneAddress(mOp.getTo());
			}
			return null;
		}
		public SalMediaDescription getLocalMediaDescription(){
			return mLocalDesc;
		}
		public SalMediaDescription getFinalMediaDescription(){
			return mFinal;
		}
		public CallState getState(){
			return mState;
		}
		public CallDirection getDir(){
			return mDir;
		}
		
	}
	private String getLocalAddr(){
		return mSal.getLocalAddr();
	}
	private SalMediaDescription makeLocalDesc(){
		SalMediaDescription md=new SalMediaDescription();
		SalStreamDescription sd=new SalStreamDescription();
		PayloadType pts[]=new PayloadType[1];
		PayloadType amr;
		sd.setAddress(getLocalAddr());
		sd.setPort(7078);
		sd.setProto(SalStreamDescription.Proto.RtpAvp);
		sd.setType(SalStreamDescription.Type.Audio);
		amr=org.linphone.jortp.Factory.get().createPayloadType();
		amr.setClockRate(8000);
		amr.setMimeType("AMR");
		amr.setNumChannels(1);
		amr.setType(PayloadType.MediaType.Audio);
		pts[0]=amr;
		sd.setPayloadTypes(pts);
		md.addStreamDescription(sd);
		return md;
	}
	private Call createIncomingCall(SalOp op){
		Call c=new Call(op,CallDirection.Incoming);
		mSal.callSetLocalMediaDescription(op,c.mLocalDesc);
		initMediaStreams(c);
		return c;
	}
	private Call createOutgoingCall(LinphoneAddress addr){
		SalOp op=new SalOp();
		Call c=new Call(op,CallDirection.Outgoing);
		c.mOp.setFrom(LinphoneCoreImpl.this.getIdentity());
		c.mOp.setTo(addr.asString());
		mSal.callSetLocalMediaDescription(op,c.mLocalDesc);
		initMediaStreams(c);
		return c;
	}
	
	private String getIdentity() {
		if (mProxyCfg!=null){
			return mProxyCfg.getIdentity();
		}
		return "sip:anonymous@"+getLocalAddr()+Integer.toString(mSipPort);
	}
	private void initMediaStreams(Call call){
		mAudioStream=new AudioStreamEchoImpl();
		try {
			String host="0.0.0.0";
			int port=call.getLocalMediaDescription().getStream(0).getPort();
			
			SocketAddress addr=org.linphone.jortp.Factory.get().createSocketAddress(host, port);
			mAudioStream.init(addr);
		} catch (RtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private RtpProfile makeProfile(SalStreamDescription sd){
		RtpProfile prof=org.linphone.jortp.Factory.get().createRtpProfile();
		PayloadType pts[]=sd.getPayloadTypes();
		int i;
		for(i=0;i<pts.length;i++){
			prof.setPayloadType(pts[i], pts[i].getNumber());
		}
		return prof;
	}
	private AudioStreamParameters makeAudioStreamParams(Call call){
		AudioStreamParameters p=null;
		SalStreamDescription sd=call.getFinalMediaDescription().getStream(0);
		if (sd!=null){
			SocketAddress dest=org.linphone.jortp.Factory.get().createSocketAddress(
					sd.getAddress(), sd.getPort());
			p=new AudioStreamParameters();
			p.setRtpProfile(makeProfile(sd));
			p.setRemoteDest(dest);
			p.setActivePayloadTypeNumber(sd.getPayloadTypes()[0].getNumber());
		}
		return p;
	}
	
	private void startMediaStreams(Call call){
		if (mAudioStream!=null){
			try {
				mAudioStream.start(makeAudioStreamParams(call));
			} catch (RtpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void stopMediaStreams(Call call){
		if (mAudioStream!=null){
			mAudioStream.stop();
			mAudioStream=null;
		}
	}
	
	public LinphoneCoreImpl(LinphoneCoreListener listener, String userConfig,
			String factoryConfig, Object userdata) {
		SocketAddress addr=org.linphone.jortp.Factory.get().createSocketAddress("0.0.0.0", mSipPort);
		mSal=new Sal();
		mSal.setUserAgent("jLinphone/0.0.1");
		mSal.listenPort(addr, Sal.Transport.Datagram, false);
		mSal.setListener(mSalListener);
		mListener=listener;
	}

	public void acceptCall() {
		if (mCall!=null){
			SalMediaDescription md;
			mSal.callAccept(mCall.mOp);
			md=mCall.mFinal=mSal.getFinalMediaDescription(mCall.mOp);
			mCall.mState=CallState.Running;
			startMediaStreams(mCall);
		}
	}

	public void addAuthInfo(LinphoneAuthInfo info) {
		 mAuthInfo=info;
	}

	public void addProxyConfig(LinphoneProxyConfig proxyCfg)
			throws LinphoneCoreException {
		mProxyCfg=proxyCfg;
	}

	public void clearAuthInfos() {
		mAuthInfo=null;
	}

	public void clearCallLogs() {
		// TODO Auto-generated method stub

	}

	public void clearProxyConfigs() {
		mProxyCfg=null;
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public Vector getCallLogs() {
		// TODO Auto-generated method stub
		return null;
	}

	public LinphoneProxyConfig getDefaultProxyConfig() {
		return mProxyCfg;
	}

	public LinphoneAddress getRemoteAddress() {
		if (mCall!=null) return mCall.getRemoteAddress();
		return null;
	}

	public float getSoftPlayLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public LinphoneAddress interpretUrl(String destination)
			throws LinphoneCoreException {
		LinphoneAddress addr=LinphoneCoreFactory.instance().createLinphoneAddress(destination);
		if (addr==null){
			if (destination.indexOf("@") == -1){
				LinphoneProxyConfig cfg=getDefaultProxyConfig();
				if (cfg!=null){
					LinphoneAddress tmp=LinphoneCoreFactory.instance()
						.createLinphoneAddress(cfg.getIdentity());
					tmp.setDisplayName(null);
					tmp.setUserName(destination);
					return tmp;
				}else throw new LinphoneCoreException("Bad destination.");
			}else{
				addr=LinphoneCoreFactory.instance().createLinphoneAddress(
						"sip:"+destination);
				if (addr==null){
					throw new LinphoneCoreException("Bad destination.");
				}
			}
		}
		return addr;
	}

	public void invite(String address) throws LinphoneCoreException {
		LinphoneAddress addr=interpretUrl(address);
		invite(addr);
	}

	public void invite(LinphoneAddress to) {
		if (mCall!=null){
			
			return;
		}
		Call c=createOutgoingCall(to);
		mSal.call(c.mOp);
		mCall=c;
	}

	public boolean isInComingInvitePending() {
		return mCall!=null 
			&& mCall.getDir()==CallDirection.Incoming
			&& mCall.getState()==CallState.Ringing;
	}

	public boolean isIncall() {
		return mCall!=null;
	}

	public boolean isMicMuted() {
		// TODO Auto-generated method stub
		return false;
	}

	public void iterate() {
		// TODO Auto-generated method stub

	}

	public void muteMic(boolean isMuted) {
		// TODO Auto-generated method stub

	}

	public void sendDtmf(char number) {
		// TODO Auto-generated method stub

	}

	public void setDefaultProxyConfig(LinphoneProxyConfig proxyCfg) {
		mProxyCfg=proxyCfg;
	}

	public void setNetworkStateReachable(boolean isReachable) {
		// TODO Auto-generated method stub

	}

	public void setSoftPlayLevel(float gain) {
		// TODO Auto-generated method stub

	}

	public void terminateCall() {
		if (mCall!=null){
			mSal.callTerminate(mCall.mOp);
			stopMediaStreams(mCall);
			mCall=null;
		}
	}

}
