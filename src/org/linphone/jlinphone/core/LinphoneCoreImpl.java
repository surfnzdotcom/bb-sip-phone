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
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalStreamDescription;


public class LinphoneCoreImpl implements LinphoneCore {
	Sal mSal;
	LinphoneAuthInfo mAuthInfo;
	LinphoneProxyConfigImpl mProxyCfg;
	Call mCall;
	LinphoneCoreListener mListener;
	AudioStream mAudioStream;
	Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
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
			mListener.generalState(LinphoneCoreImpl.this, LinphoneCore.GeneralState.GSTATE_CALL_OUT_CONNECTED);
			c.mFinal=mSal.getFinalMediaDescription(op);
			startMediaStreams(c);
		}

		public void onCallReceived(SalOp op) {
			if (mCall!=null){
				mSal.callDecline(op, Sal.Reason.Busy, null);
			}else{
				try {
					mCall=createIncomingCall(op);
				} catch (SalException e) {
					System.err.println("Cannot create incoming call, reason ["+e.getMessage()+"]");
					e.printStackTrace();
					mSal.callDecline(op, Sal.Reason.Unknown, null);
					
				}
				mListener.inviteReceived(LinphoneCoreImpl.this, 
						op.getFrom());
				mListener.generalState(LinphoneCoreImpl.this, LinphoneCore.GeneralState.GSTATE_CALL_IN_INVITE);
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
				mListener.generalState(LinphoneCoreImpl.this, LinphoneCore.GeneralState.GSTATE_CALL_END);
				mCall=null;
			}
		}

		public void onAuthRequested(SalOp op, String realm, String userName) {
			try {
				if (mAuthInfo != null && userName.equalsIgnoreCase(mAuthInfo.getUsername())) {
					mSal.authenticate(op, new SalAuthInfo(realm,userName,mAuthInfo.getPassword()));
				} else {
					mListener.authInfoRequested(LinphoneCoreImpl.this, realm, userName);
				}
			} catch (Exception e) {
				mLog.error("Cannot provide auth info", e);
			}

		}

		public void onAuthSuccess(SalOp lSalOp, String realm, String username) {
			mProxyCfg.setRegistered(true);
			mListener.generalState(LinphoneCoreImpl.this, GeneralState.GSTATE_REG_OK);
			
		}
		
	};
	
	private class Call{
		CallDirection mDir;
		CallState mState;
		SalOp mOp;
		SalMediaDescription mLocalDesc;
		SalMediaDescription mFinal;
		
		private Call(SalOp op, CallDirection dir) throws SalException{
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

	private SalMediaDescription makeLocalDesc() throws SalException{
		SalMediaDescription md=SalFactory.instance().createSalMediaDescription();
		SalStreamDescription sd=new SalStreamDescription();
		PayloadType pts[]=new PayloadType[1];
		PayloadType amr;
		sd.setAddress(mSal.getLocalAddr());
		sd.setPort(7078);
		sd.setProto(SalStreamDescription.Proto.RtpAvp);
		sd.setType(SalStreamDescription.Type.Audio);
		amr=JOrtpFactory.instance().createPayloadType();
		amr.setClockRate(8000);
		amr.setMimeType("AMR");
		amr.setNumChannels(1);
		amr.setType(PayloadType.MediaType.Audio);
		pts[0]=amr;
		sd.setPayloadTypes(pts);
		md.addStreamDescription(sd);
		md.setAddress(mSal.getLocalAddr());
		return md;
	}
	private Call createIncomingCall(SalOp op) throws SalException{
		Call c=new Call(op,CallDirection.Incoming);
		mSal.callSetLocalMediaDescription(op,c.mLocalDesc);
		initMediaStreams(c);
		return c;
	}
	private Call createOutgoingCall(LinphoneAddress addr) throws SalException{
		SalOp op= mSal.createSalOp();
		Call c=new Call(op,CallDirection.Outgoing);
		c.mOp.setFrom(LinphoneCoreImpl.this.getIdentity());
		c.mOp.setTo(addr.asString());
		mSal.callSetLocalMediaDescription(op,c.mLocalDesc);
		initMediaStreams(c);
		return c;
	}
	
	private String getIdentity() throws SalException {
		if (mProxyCfg!=null){
			return mProxyCfg.getIdentity();
		}
		return "sip:anonymous@"+mSal.getLocalAddr()+Integer.toString(mSipPort);
	}
	private void initMediaStreams(Call call){
		mAudioStream=new AudioStreamEchoImpl();
		try {
			String host="0.0.0.0";
			int port=call.getLocalMediaDescription().getStream(0).getPort();
			
			SocketAddress addr=JOrtpFactory.instance().createSocketAddress(host, port);
			mAudioStream.init(addr);
		} catch (RtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private RtpProfile makeProfile(SalStreamDescription sd){
		RtpProfile prof=JOrtpFactory.instance().createRtpProfile();
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
			SocketAddress dest=JOrtpFactory.instance().createSocketAddress(
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
			String factoryConfig, Object userdata) throws LinphoneCoreException{
		try {
			SocketAddress addr=JOrtpFactory.instance().createSocketAddress("0.0.0.0", mSipPort);
			mSal=SalFactory.instance().createSal();
			mSal.setUserAgent("jLinphone/0.0.1");
			mSal.listenPort(addr, Sal.Transport.Datagram, false);
			mSal.setListener(mSalListener);
			mListener=listener;
		} catch (Exception e ) {
			throw new LinphoneCoreException("Cannot create Linphone core for user conf ["
											+userConfig
											+"] factory conf ["+factoryConfig+"] reason ["+e.getMessage()+"] ",e);
		}
	}

	public void acceptCall() {
		if (mCall!=null){
			SalMediaDescription md;
			mSal.callAccept(mCall.mOp);
			md=mCall.mFinal=mSal.getFinalMediaDescription(mCall.mOp);
			mCall.mState=CallState.Running;
			mListener.generalState(this, GeneralState.GSTATE_CALL_IN_CONNECTED);
			startMediaStreams(mCall);
		}
	}

	public void addAuthInfo(LinphoneAuthInfo info) {
		 mAuthInfo=info;
	}

	public void addProxyConfig(LinphoneProxyConfig proxyCfg)
			throws LinphoneCoreException {
		mProxyCfg=(LinphoneProxyConfigImpl) proxyCfg;
		proxyCfg.done();
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
		if (isIncall()) {
			terminateCall();
		}
		mSal.close();
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
					throw new LinphoneCoreException("Bad destination ["+destination+"]");
				}
			}
		}
		return addr;
	}

	public void invite(String address) throws LinphoneCoreException {
		LinphoneAddress addr=interpretUrl(address);
		invite(addr);
	}

	public void invite(LinphoneAddress to) throws LinphoneCoreException {
		if (mCall!=null){
			return;
		}
		try {
			
			Call c=createOutgoingCall(to);
			mSal.call(c.mOp);
			mCall=c;
			mListener.generalState(this, GeneralState.GSTATE_CALL_OUT_INVITE);
		} catch (Exception e) {
			throw new LinphoneCoreException("Cannot place call to ["+to+"]",e);
		}
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
		return false;
	}

	public void iterate() {
		if (mProxyCfg!=null)
			((LinphoneProxyConfigImpl)mProxyCfg).check(this);
	}

	public void muteMic(boolean isMuted) {
		// TODO Auto-generated method stub

	}

	public void sendDtmf(char number) {
		// TODO Auto-generated method stub

	}

	public void setDefaultProxyConfig(LinphoneProxyConfig proxyCfg) {
		mProxyCfg=(LinphoneProxyConfigImpl) proxyCfg;
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
			mListener.generalState(this, GeneralState.GSTATE_CALL_END);
			mCall=null;
		}
	}
	
	public Sal getSal(){
		return mSal;
	}

}
