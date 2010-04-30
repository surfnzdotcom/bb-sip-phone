package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalOp;

public class LinphoneProxyConfigImpl implements LinphoneProxyConfig {
	private String mProxy;
	private String mIdentity;
	private String mDialPrefix;
	private int mExpires;
	private boolean mEscapePlus;
	private boolean mEnableRegister;
	private boolean mRegistered;
	private boolean mCommit;
	private SalOp mOp;
	private LinphoneCoreImpl mCore;
	static Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	
	LinphoneProxyConfigImpl(){
		mEscapePlus=false;
		mEnableRegister=false;
		mCommit=false;
		mExpires=3600;
	}
	
	LinphoneProxyConfigImpl(String identity, String proxy, String route,
			boolean enableRegister) {
		this();
		mIdentity = identity;
		mProxy = proxy;
		mEnableRegister = enableRegister;
	}

	public void done() {
		mCommit=true;
	}

	public void edit() {
		if (mOp!=null && mCore!=null){
			mCore.getSal().unregister(mOp);
			mOp=null;
		}
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
	
	public void check(LinphoneCoreImpl core){
		mCore=core;
		if (mCommit){
			if (mEnableRegister){
				mOp=core.getSal().createSalOp();
				try {
					core.getSal().register(mOp, mProxy, mIdentity, mExpires);
				} catch (SalException e) {
					mLog.error("Registration Error",e);
				}
			}else if (mOp!=null){
				core.getSal().unregister(mOp);
				mOp=null;
			}
			mCommit=false;
		}
	}

}
