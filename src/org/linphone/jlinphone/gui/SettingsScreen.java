/*
SettingsScreen.java
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
package org.linphone.jlinphone.gui;

import java.util.Hashtable;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;

import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class SettingsScreen extends MainScreen implements Settings {
	private PersistentObject mPersistentObject;
	private Hashtable lSettingsMap;
	private BasicEditField mUserNameField;
	private BasicEditField mUserPasswd;
	private BasicEditField mDomain;
	private BasicEditField mProxy;
	private CheckboxField mDebugMode;
	private final LinphoneCore mCore;
	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	
	SettingsScreen(LinphoneCore lc) {

		mCore = lc;	
		mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.settings".hashCode() );
		if (mPersistentObject.getContents() != null) {
			lSettingsMap = (Hashtable) mPersistentObject.getContents();
		} else {
			lSettingsMap = new Hashtable();
		}
		setTitle("Linphone Settings");


		VerticalFieldManager lMainFiedManager = new VerticalFieldManager();

		add (lMainFiedManager);

		VerticalFieldManager lSipAccount = new VerticalFieldManager();
		lMainFiedManager.add(lSipAccount);
		LabelField lSipAccountLabelField = new LabelField("SIP account");
		lSipAccount.add(lSipAccountLabelField);
		mUserNameField = new BasicEditField("Username*: ", "", 128, 0);
		mUserNameField.setText(getString(SIP_USERNAME,""));
		lSipAccount.add(mUserNameField);
		mUserPasswd = new BasicEditField("Passwd*: ", "", 128, 0);
		mUserPasswd.setText(getString(SIP_PASSWORD,""));
		lSipAccount.add(mUserPasswd);
		mDomain = new BasicEditField("Domain*: ", "", 128, 0);
		mDomain.setText(getString(SIP_DOMAIN,""));
		lSipAccount.add(mDomain);
		mProxy = new BasicEditField("Proxy: ", "", 128, 0);
		mProxy.setText(getString(SIP_PROXY,""));
		lSipAccount.add(mProxy);

		
		SeparatorField lSipAccountSeparator = new SeparatorField();
		lMainFiedManager.add(lSipAccountSeparator);

		VerticalFieldManager lAdvanced = new VerticalFieldManager();
		lMainFiedManager.add(lAdvanced);

		mDebugMode = new CheckboxField("Enable debug mode", false);
		mDebugMode.setChecked(getBoolean(ADVANCED_DEBUG,false));
		mDebugMode.setChangeListener(new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
				LinphoneCoreFactory.instance().setDebugMode(((CheckboxField)field).getChecked());

			}

		});
		lAdvanced.add(mDebugMode);
		try {
			initFromConf();
		} catch (LinphoneConfigException e) {
			sLogger.warn("no configuration ready yet", e);
		}


	}
	
	protected boolean onSave() {
		lSettingsMap.put(SIP_USERNAME, mUserNameField.getText());
		lSettingsMap.put(SIP_PASSWORD, mUserPasswd.getText());
		lSettingsMap.put(SIP_DOMAIN, mDomain.getText());
		lSettingsMap.put(SIP_PROXY, mProxy.getText());
		lSettingsMap.put(ADVANCED_DEBUG, new Boolean(mDebugMode.getChecked()));
		try {
			initFromConf();
			mPersistentObject.setContents(lSettingsMap);
			mPersistentObject.commit();
			
		} catch (final LinphoneConfigException e) {
			sLogger.error("Configuration error",e);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					
				}
			});
		}

		return true;
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.linphone.jlinphone.gui.Settings#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String key,boolean defaultValue) {
		boolean lResult = defaultValue;
		if (lSettingsMap != null) {
			Boolean value = (Boolean) lSettingsMap.get(key);
			if (value != null) {
				return value.booleanValue();
			}
		}
		return lResult;
	}
	
	/* (non-Javadoc)
	 * @see org.linphone.jlinphone.gui.Settings#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String key,String defaultValue) {
		String lResult = defaultValue;
		if (lSettingsMap != null) {
			String value = (String) lSettingsMap.get(key);
			if (value != null) {
				return value;
			}
		}
		return lResult;
	}

	public void initFromConf() throws LinphoneConfigException {

		
		//traces
		boolean lIsDebug = getBoolean(Settings.ADVANCED_DEBUG, false);
		LinphoneCoreFactory.instance().setDebugMode(lIsDebug);
		
		//1 read proxy config from preferences
		String lUserName = getString(Settings.SIP_USERNAME, null);
		if (lUserName == null || lUserName.length()==0) {
			throw new LinphoneConfigException("No username configured");
		}

		String lPasswd = getString(Settings.SIP_PASSWORD, null);
		if (lPasswd == null || lPasswd.length()==0) {
			throw new LinphoneConfigException("No password configured");
		}

		String lDomain = getString(Settings.SIP_DOMAIN, null);
		if (lDomain == null || lDomain.length()==0) {
			throw new LinphoneConfigException("No domain configured");
		}


		//auth
		mCore.clearAuthInfos();
		LinphoneAuthInfo lAuthInfo =  LinphoneCoreFactory.instance().createAuthInfo(lUserName, lPasswd,null);
		mCore.addAuthInfo(lAuthInfo);


		//proxy
		String lProxy = getString(Settings.SIP_PROXY,null);
		if (lProxy == null || lProxy.length() == 0) {
			lProxy = "sip:"+lDomain;
		} else if (lProxy.startsWith("sip:")== false){
			lProxy="sip:"+lProxy;
		}
		//get Default proxy if any
		LinphoneProxyConfig lDefaultProxyConfig = mCore.getDefaultProxyConfig();
		String lIdentity = "sip:"+lUserName+"@"+lDomain;
		try {
			if (lDefaultProxyConfig == null) {
				lDefaultProxyConfig = LinphoneCoreFactory.instance().createProxyConfig(lIdentity, lProxy, null,true);
				mCore.addProxyConfig(lDefaultProxyConfig);
				mCore.setDefaultProxyConfig(lDefaultProxyConfig);

			} else {
				lDefaultProxyConfig.edit();
				lDefaultProxyConfig.setIdentity(lIdentity);
				lDefaultProxyConfig.setProxy(lProxy);
				lDefaultProxyConfig.enableRegister(true);
				lDefaultProxyConfig.done();
			}
			lDefaultProxyConfig = mCore.getDefaultProxyConfig();

			//init network state
			
		} catch (LinphoneCoreException e) {
			throw new LinphoneConfigException("Wrong settings",e);
		}
	}
}
