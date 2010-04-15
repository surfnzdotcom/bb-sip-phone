package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;

public class LinphoneProxyConfigImpl implements LinphoneProxyConfig {
	private String mProxy;
	private String mIdentity;
	private String mDialPrefix;
	private boolean mEscapePlus;
	private boolean mEnableRegister;
	private boolean mRegistered;
	
	public LinphoneProxyConfigImpl(){
		mEscapePlus=false;
		mEnableRegister=false;
	}
	
	public void done() {
		// TODO Auto-generated method stub

	}

	public void edit() {
		// TODO Auto-generated method stub

	}

	public void enableRegister(boolean value) throws LinphoneCoreException {
		mEnableRegister=value;
	}

	public String getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	public String normalizePhoneNumber(String number) {
		return number;
	}

	public void setDialEscapePlus(boolean value) {
		mEscapePlus=value;
	}

	public void setDialPrefix(String prefix) {
		mDialPrefix=prefix;
	}

	public void setIdentity(String identity) throws LinphoneCoreException {
		mIdentity=identity;
	}

	public void setProxy(String proxyUri) throws LinphoneCoreException {
		mProxy=proxyUri;
	}

	public String getIdentity() {
		return mIdentity;
	}

	public String getProxy() {
		return mProxy;
	}

	public boolean isRegistered() {
		return mRegistered;
	}

	public boolean registerEnabled() {
		return mEnableRegister;
	}

}
