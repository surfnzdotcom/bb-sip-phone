package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneAuthInfo;

public class LinphoneAuthInfoImpl implements LinphoneAuthInfo {
	private String mPassword,mUsername;
	public LinphoneAuthInfoImpl(String username, String password){
		mUsername=username;
		mPassword=password;
	}
}
