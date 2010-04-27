package org.linphone.jlinphone.core;

import java.io.File;
import java.util.List;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;

public class LinphoneCoreImpl implements LinphoneCore {
	private Sal mSal;
	private LinphoneAuthInfo mAuthInfo;
	private LinphoneProxyConfig mProxyCfg;
	private Call mCall;
	private LinphoneCallListener mListener;
	
	private class Call{
		SalOp mOp;
		SalMediaDescription mLocalDesc;
		SalMediaDescription mFinal;
		private Call(){
			
		}
		public LinphoneAddress getRemoteAddress(){
			return null;
		}
		
	}
	private Call createIncomingCall(SalOp op){
		Call c=new Call();
		c.mOp=op;
		return c;
	}
	private Call createOutgoingCall(){
		Call c=new Call();
		c.mOp=new SalOp();
		return c;
	}
	
	public LinphoneCoreImpl(LinphoneCoreListener listener, File userConfig,
			File factoryConfig, Object userdata) {
		SocketAddress addr=org.linphone.jortp.Factory.get().createSocketAddress("0.0.0.0", 5060);
		mSal=new Sal();
		mSal.setUserAgent("jLinphone/0.0.1");
		mSal.listenPort(addr, Sal.Transport.Datagram, false);
		mListener=listener;
	}

	public void acceptCall() {
		if (mCall){
			mSal.callAccept(mCall.mOp);
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

	public List getCallLogs() {
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
		// TODO Auto-generated method stub
		return null;
	}

	public void invite(String uri) {
		// TODO Auto-generated method stub

	}

	public void invite(LinphoneAddress to) {
		// TODO Auto-generated method stub

	}

	public boolean isInComingInvitePending() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub

	}

	public void setNetworkStateReachable(boolean isReachable) {
		// TODO Auto-generated method stub

	}

	public void setSoftPlayLevel(float gain) {
		// TODO Auto-generated method stub

	}

	public void terminateCall() {
		// TODO Auto-generated method stub

	}

}
