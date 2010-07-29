/*
LinphoneCoreImpl.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone.jlinphone.core;


import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import net.rim.device.api.io.transport.TransportDescriptor;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.WLANInfo;

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
import org.linphone.jlinphone.media.AudioStreamParameters;
import org.linphone.jlinphone.media.jsr135.AudioStreamImpl;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalError;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalReason;
import org.linphone.sal.SalStreamDescription;
import org.linphone.sal.Sal.Reason;


public class LinphoneCoreImpl implements LinphoneCore {
	Sal mSal;
	LinphoneAuthInfo mAuthInfo;
	LinphoneProxyConfigImpl mProxyCfg;
	Call mCall;
	LinphoneCoreListener mListener;
	AudioStream mAudioStream;
	Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	Player mRingPlayer;
	Vector mCallLogs;
	private PersistentObject mPersistentObject;
	boolean mNetworkIsUp=false;
	
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
			mListener.displayStatus(LinphoneCoreImpl.this,"Connected to "+getRemoteAddress().toString());
			mListener.generalState(LinphoneCoreImpl.this, LinphoneCore.GeneralState.GSTATE_CALL_OUT_CONNECTED);
			c.mFinal=mSal.getFinalMediaDescription(op);
			startMediaStreams(c);
		}

		public void onCallReceived(SalOp op) {
			try {
				LinphoneCallLog lCallLog = new LinphoneCallLogImpl(CallDirection.Incoming
																, LinphoneCoreFactory.instance().createLinphoneAddress(op.getFrom())
																, null);
				mCallLogs.addElement(lCallLog);
				
				if (mCall!=null){
					mSal.callDecline(op, Sal.Reason.Busy, null);
				}else{
					mCall=createIncomingCall(op);

					mCall.mState=CallState.Ringing;
					mListener.displayStatus(LinphoneCoreImpl.this,getRemoteAddress()+" is calling you");
					mListener.inviteReceived(LinphoneCoreImpl.this, 
							op.getFrom());
					mListener.generalState(LinphoneCoreImpl.this, LinphoneCore.GeneralState.GSTATE_CALL_IN_INVITE);
					mSal.callRinging(op);
					mRingPlayer = Manager.createPlayer(getClass().getResourceAsStream("/oldphone.wav"),"audio/wav");
					mRingPlayer.realize();
					mRingPlayer.prefetch();
					mRingPlayer.setLoopCount(Integer.MAX_VALUE);
					mRingPlayer.start();
					
					
				} 
			}catch (Exception e) {
				mLog.error("Cannot create incoming call",e);
				mSal.callDecline(op, Sal.Reason.Unknown, null);
				return;
			}

		}

		public void onCallRinging(SalOp op) {
			Call c=(Call)op.getUserContext();
			if (mCall==null || mCall!=c){
				mSal.callTerminate(op);
			}
			if (mCall!=null){
				mCall.mState=CallState.Ringing;
				mListener.displayStatus(LinphoneCoreImpl.this,"Remote ringing...");
			}
			if (mSal.getFinalMediaDescription(op) !=null) {
				mListener.displayStatus(LinphoneCoreImpl.this,"Early Media");
				c.mFinal=mSal.getFinalMediaDescription(op);
				startMediaStreams(c);
			}
		}

		public void onCallTerminated(SalOp op) {
			if (mCall!=null){
				mListener.displayStatus(LinphoneCoreImpl.this,"Call terminated");
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
			mListener.displayStatus(LinphoneCoreImpl.this,"Registered to "+lSalOp.getTo());
			
			
		}

		public void onCallFailure(SalOp op, String reasonPhrase) {
			stopMediaStreams(mCall);
			mListener.displayStatus(LinphoneCoreImpl.this,"Call failure ["+reasonPhrase+"]");
			mListener.generalState(LinphoneCoreImpl.this,  LinphoneCore.GeneralState.GSTATE_CALL_ERROR);
			mCall=null;
			
		}

		public void OnRegisterFailure(SalOp op, SalError error,
				SalReason reason, String details) {
			mListener.displayStatus(LinphoneCoreImpl.this,"Registration failure ["+details+"]");
			mListener.generalState(LinphoneCoreImpl.this, GeneralState.GSTATE_REG_FAILED);
			
		}

		public void OnRegisterSuccess(SalOp op, boolean registered) {
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
		public void setState(CallState aState) {
			mState = aState;
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
		amr.appendRecvFmtp("octet-align=1");
		amr.setNumChannels(1);
		amr.setType(PayloadType.MediaType.Audio);
		amr.setNumber(103);
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
		//is route header required ?
		if (getDefaultProxyConfig() != null
				&& getDefaultProxyConfig().getDomain().equalsIgnoreCase(addr.getDomain())) {
			op.setRoute(getDefaultProxyConfig().getProxy());
		}
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
		return "sip:anonymous@"+mSal.getLocalAddr()+":"+Integer.toString(mSipPort);
	}
	private void initMediaStreams(Call call){
		mAudioStream=new AudioStreamImpl();
		try {
			String host="0.0.0.0";
			int port=call.getLocalMediaDescription().getStream(0).getPort();
			
			SocketAddress addr=JOrtpFactory.instance().createSocketAddress(host, port);
			mAudioStream.init(addr);
		} catch (RtpException e) {
			mLog.error("Cannot init media stream",e);
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
				mLog.error("Cannot start stream", e);
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
			mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.logs".hashCode() );
			if (mPersistentObject.getContents() != null) {
				mCallLogs = (Vector) mPersistentObject.getContents();
				//limit Call logs number
				while (mCallLogs.size()>30) {
					mCallLogs.removeElementAt(0);
				}
			} else {
				mCallLogs = new Vector();
			}
			
			mListener=listener;
			mListener.generalState(this, GeneralState.GSTATE_POWER_ON);
		} catch (Exception e ) {
			destroy();
			throw new LinphoneCoreException("Cannot create Linphone core for user conf ["
					+userConfig
					+"] factory conf ["+factoryConfig+"]",e);

		}
	}

	public void acceptCall() throws LinphoneCoreException {
		if (mCall!=null){
			try {
				mSal.callAccept(mCall.mOp);
				mCall.mFinal=mSal.getFinalMediaDescription(mCall.mOp);
				mCall.mState=CallState.Running;
				mListener.displayStatus(LinphoneCoreImpl.this,"Connected");
				mListener.generalState(this, GeneralState.GSTATE_CALL_IN_CONNECTED);
				mRingPlayer.stop();
				mRingPlayer.close();
				mRingPlayer=null;
				startMediaStreams(mCall);
			} catch (Exception e) {
				throw new LinphoneCoreException("cannot accept call from ["+getRemoteAddress()+"]");
			}
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
		if (mSal != null) {
			mSal.close();
			mSal=null;
		}
		if (mPersistentObject != null) {
			mPersistentObject.setContents(mCallLogs);
			mPersistentObject.commit();
		}
		if (mListener !=null) mListener.generalState(this, GeneralState.GSTATE_POWER_OFF);
	}

	public Vector getCallLogs() {
		return mCallLogs;
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
				}else throw new LinphoneCoreException("Bad destination ["+destination+"]");
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
			mListener.displayStatus(LinphoneCoreImpl.this,"Calling  "+getRemoteAddress());
			mListener.generalState(this, GeneralState.GSTATE_CALL_OUT_INVITE);
			mCallLogs.addElement(new LinphoneCallLogImpl(CallDirection.Outgoing, null, to));
		} catch (Exception e) {
			terminateCall();
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

	public synchronized void iterate() {
		if (mSal == null && mNetworkIsUp ==true) {
			//create Sal
			String localAdd=null;
			try {
				String dummyConnString = "socket://www.linphone.org:80;deviceside=true";
				if (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) {
					dummyConnString+=";interface=wifi";
				}

				mLog.info("Opening dummy socket connection to : " + dummyConnString);
				SocketConnection dummyCon = (SocketConnection) Connector.open(dummyConnString);
				localAdd = dummyCon.getLocalAddress();
				dummyCon.close();
			} catch (IOException ioexp) {
				mLog.error("Network unreachable, please enable wifi/or 3G");
			}
			// check if local port is available
//			try {
//				Connector.open("datagram://:"+mSipPort);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			SocketAddress addr=JOrtpFactory.instance().createSocketAddress(localAdd, mSipPort);
			mSal=SalFactory.instance().createSal();
			mSal.setUserAgent("jLinphone/0.0.1");
			try {
				mSal.listenPort(addr, Sal.Transport.Datagram, false);
			} catch (SalException e) {
				mLog.error("Cannot listen of on ["+addr+"]",e);
			}
			mSal.setListener(mSalListener);
			
			mListener.displayStatus(LinphoneCoreImpl.this,"Ready");
			
			
		} else if (mSal !=null && mNetworkIsUp == false) {
			if (isIncall()) {
				terminateCall();
			}
			mSal.close();
			mSal=null;
		}
		if (mSal!=null && mProxyCfg!=null)
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
		
		if (mNetworkIsUp != isReachable) {
			mLog.warn("New network state is ["+isReachable+"]");
		}
		mNetworkIsUp=isReachable;

	}

	public void setSoftPlayLevel(float gain) {
		// TODO Auto-generated method stub

	}

	public void terminateCall() {
		if (mCall!=null){
			if (isInComingInvitePending()) {
				try {
					mRingPlayer.stop();
					mRingPlayer.close();
					mRingPlayer=null;
				} catch (MediaException e) {
					mLog.error("cannot stop ringer", e);
				}
				mSal.callDecline(mCall.mOp, Reason.Declined, null);
			} else {
				mSal.callTerminate(mCall.mOp);
				stopMediaStreams(mCall);
			}
			mListener.generalState(this, GeneralState.GSTATE_CALL_END);
			mCall=null;
		}
	}
	
	public Sal getSal(){
		return mSal;
	}

}
