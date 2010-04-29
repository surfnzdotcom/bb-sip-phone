package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneProxyConfig;

import java.io.File;
import java.io.IOException;

public class Factory extends LinphoneCoreFactory {
	
	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener, File userConfig,File factoryConfig,Object  userdata) throws IOException {
		return new LinphoneCoreImpl(listener,userConfig,factoryConfig,userdata);
	}

	public LinphoneAuthInfo createAuthInfo(String username, String password) {
		return new LinphoneAuthInfoImpl(username,password);
	}

	public LinphoneAddress createLinphoneAddress(String username,
			String domain, String displayName) {
		return null;
	}
	
	public LinphoneProxyConfig createProxyConfig(String identity, String proxy,
			String route, boolean enableRegister) throws LinphoneCoreException {
		return new LinphoneProxyConfigImpl();
	}

	@Override
	public void setDebugMode(boolean enable) {
		// TODO Auto-generated method stub
		
	}
	private Factory(){
		
	}

	@Override
	public LinphoneAddress createLinphoneAddress(String address) {
		// TODO Auto-generated method stub
		return null;
	}
}
