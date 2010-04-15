package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneProxyConfig;

import java.io.File;
import java.io.IOException;

public class Factory {
	private static Factory sFactory=null;
	
	public Factory get(){
		if (sFactory==null){
			sFactory=new Factory();
		}
		return sFactory;
	}
	
	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener, File userConfig,File factoryConfig,Object  userdata) throws IOException {
		return new LinphoneCoreImpl(listener,userConfig,factoryConfig,userdata);
	}
	public LinphoneProxyConfig createLinphoneProxyConfig(){
		return new LinphoneProxyConfigImpl();
	}
}
