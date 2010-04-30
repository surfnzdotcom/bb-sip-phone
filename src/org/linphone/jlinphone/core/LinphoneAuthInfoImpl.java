package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneAuthInfo;

public class LinphoneAuthInfoImpl implements LinphoneAuthInfo {
	private String mPassword,mUsername,mRealm;
	public LinphoneAuthInfoImpl(String username, String password, String realm){
		mUsername=username;
		mPassword=password;
		mRealm=realm;
	}
	public String getPassword() {
		return mPassword;
	}
	public String getRealm() {
		return mRealm;
	}
	public String getUsername() {
		return mUsername;
	}
	public void setPassword(String password) {
		mPassword=password;
	}
	public void setRealm(String realm) {
		mRealm=realm;
	}
	public void setUsername(String username) {
		mUsername=username;
	}
}
