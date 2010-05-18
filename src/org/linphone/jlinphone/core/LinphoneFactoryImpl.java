package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneLogHandler;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.Logger;
import org.linphone.sal.SalFactory;



public class LinphoneFactoryImpl extends LinphoneCoreFactory {
	
	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener, String userConfig,String factoryConfig,Object  userdata) throws LinphoneCoreException {
		return new LinphoneCoreImpl(listener,userConfig,factoryConfig,userdata);
	}

	public LinphoneAuthInfo createAuthInfo(String username, String password, String realm) {
		return new LinphoneAuthInfoImpl(username,password, null);
	}

	public LinphoneAddress createLinphoneAddress(String username,
			String domain, String displayName) {
		return null;
	}
	
	public LinphoneProxyConfig createProxyConfig(String identity, String proxy,
			String route, boolean enableRegister) throws LinphoneCoreException {
		return new LinphoneProxyConfigImpl(identity,proxy,route,enableRegister);
	}

	
	public void setDebugMode(boolean enable) {
		if (enable) {
			Logger.setGlobalLogLevel(Logger.Info);
		}
		SalFactory.instance().setDebugMode(enable);
		
	}
	public LinphoneFactoryImpl(){
		
	}

	public LinphoneAddress createLinphoneAddress(String address) {
		LinphoneAddress ret=null;
		try{
			ret=new LinphoneAddressImpl(address);
		}catch(Exception e){
			
		}
		return ret;
	}

	public void setLogHandler(final LinphoneLogHandler handler) {
		Logger.setLogHandler(new Logger.Handler() {
			
			public void log(String loggerName, int level, String levelName, String msg, Throwable e) {
				handler.log(loggerName, level, levelName, msg, e);
				
			}
		});
		
	}
}
