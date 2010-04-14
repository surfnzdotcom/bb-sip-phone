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

public class LinphoneCoreImpl implements LinphoneCore {

	public LinphoneCoreImpl(LinphoneCoreListener listener, File userConfig,
			File factoryConfig, Object userdata) {
		// TODO Auto-generated constructor stub
	}

	public void acceptCall() {
		// TODO Auto-generated method stub

	}

	public void addAuthInfo(LinphoneAuthInfo info) {
		// TODO Auto-generated method stub

	}

	public void addProxyConfig(LinphoneProxyConfig proxyCfg)
			throws LinphoneCoreException {
		// TODO Auto-generated method stub

	}

	public void clearAuthInfos() {
		// TODO Auto-generated method stub

	}

	public void clearCallLogs() {
		// TODO Auto-generated method stub

	}

	public void clearProxyConfigs() {
		// TODO Auto-generated method stub

	}

	public LinphoneProxyConfig createProxyConfig(String identity, String proxy,
			String route, boolean enableRegister) throws LinphoneCoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public List<LinphoneCallLog> getCallLogs() {
		// TODO Auto-generated method stub
		return null;
	}

	public LinphoneProxyConfig getDefaultProxyConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public LinphoneAddress getRemoteAddress() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
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
