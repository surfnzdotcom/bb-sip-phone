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

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.MediaEncryption;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;


public class SettingsScreen extends MainScreen implements Settings, LinphoneResource{

	private LinphonePersistance lp=LinphonePersistance.instance();
	private final LinphoneCore mCore;
	private Logger sLogger=JOrtpFactory.instance().createLogger(Custom.APPNAME);
	SettingsFieldContent mSettingsFields;
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	class SettingsFieldContent {
		private BasicEditField mUserNameField;
		private BasicEditField mUserPasswd;
		private BasicEditField mDomain;
		private BasicEditField mProxy;
		private CheckboxField mUseSrtp;
		private CheckboxField mDebugMode;
		private CheckboxField mSubstituteZero2Plus;
		private TransportChoice mTransport;
		private BasicEditField mPtime;
		VerticalFieldManager mMainFiedManager = new VerticalFieldManager(VERTICAL_SCROLL);

		public SettingsFieldContent (){
			VerticalFieldManager lSipAccount = new VerticalFieldManager();
			mMainFiedManager.add(lSipAccount);
			LabelField lSipAccountLabelField = new LabelField(mRes.getString(SETTING_SIP_ACCOUNT));
			lSipAccountLabelField.setFont(Font.getDefault().derive(Font.BOLD|Font.UNDERLINED));
			lSipAccount.add(lSipAccountLabelField);

			mUserNameField = new BasicEditField(mRes.getString(SETTING_USERNAME), "", 128, 0);
			mUserNameField.setText(lp.getString(SIP_USERNAME,""));
			lSipAccount.add(mUserNameField);

			mUserPasswd = new BasicEditField(mRes.getString(SETTING_PASSWD), "", 128, 0);
			mUserPasswd.setText(lp.getString(SIP_PASSWORD,""));
			lSipAccount.add(mUserPasswd);

			final String domain=lp.getString(SIP_DOMAIN,Custom.DEFAULT_DOMAIN);
			mDomain = new BasicEditField(mRes.getString(SETTING_DOMAIN), domain, 128, 0);
			lSipAccount.add(mDomain);

			final String proxy=lp.getString(SIP_PROXY,Custom.DEFAULT_PROXY);
			mProxy = new BasicEditField(mRes.getString(SETTING_PROXY), proxy, 128, 0);
			lSipAccount.add(mProxy);

			
			SeparatorField lSipAccountSeparator = new SeparatorField();
			mMainFiedManager.add(lSipAccountSeparator);

			VerticalFieldManager lAdvanced = new VerticalFieldManager();
			mMainFiedManager.add(lAdvanced);
			LabelField lAvancedLabelField = new LabelField(mRes.getString(SETTING_ADVANCED));
			lAvancedLabelField.setFont(Font.getDefault().derive(Font.BOLD|Font.UNDERLINED));
			lAdvanced.add(lAvancedLabelField);
			
			
			final String transport=lp.getString(SIP_TRANSPORT,Custom.DEFAULT_TRANSPORT);
			mTransport= new TransportChoice(mRes.getString(SETTING_TRANSPORT), transport);
			lAdvanced.add(mTransport);

			final String pTimeValue=lp.getString(ADVANCED_PTIME,Custom.DEFAULT_PTIME);
			mPtime = new BasicEditField(mRes.getString(SETTINGS_PTIME), pTimeValue, 3, 0);
			lAdvanced.add(mPtime);
			
			final boolean enableSrtp=lp.getBoolean(SRTP_ENCRYPTION, Custom.DEFAULT_ENABLE_SRTP);
			mUseSrtp = new CheckboxField(mRes.getString(SETTING_SRTP), enableSrtp);
			lAdvanced.add(mUseSrtp);

			final boolean debug=lp.getBoolean(ADVANCED_DEBUG,Custom.DEFAUL_DEBUG);
			mDebugMode = new CheckboxField(mRes.getString(SETTING_DEBUG), debug);
			lAdvanced.add(mDebugMode);

			final boolean plusTo00 = lp.getBoolean(ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO, Custom.DEFAULT_PLUS_TO_ZERO);
			mSubstituteZero2Plus = new CheckboxField(mRes.getString(SETTING_ESCAPE_PLUS), plusTo00);
			lAdvanced.add(mSubstituteZero2Plus);
		}
		public void save() {
			lp.put(SIP_USERNAME, mUserNameField.getText());
			lp.put(SIP_PASSWORD, mUserPasswd.getText());
			lp.put(SIP_DOMAIN, mDomain.getText());
			lp.put(SIP_PROXY, mProxy.getText());
			lp.put(SIP_TRANSPORT,mTransport.getSelectedString());
			lp.put(SRTP_ENCRYPTION, mUseSrtp.getChecked());
			lp.put(ADVANCED_DEBUG, mDebugMode.getChecked());
			lp.put(ADVANCED_PTIME,  mPtime.getText());
			lp.put(ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO, mSubstituteZero2Plus.getChecked());
			try {
				initFromConf();
				lp.commit();
			} catch (final LinphoneConfigException e) {
				sLogger.error("Configuration error",e);
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						Dialog.alert(e.getMessage());
					}
				});
			}
		}
		public Field getRootField() {
			return mMainFiedManager;
		}
	}
	SettingsScreen(LinphoneCore lc) {
		mCore = lc;	
		setTitle(Custom.APPNAME +" "+mRes.getString(SETTINGS));
		((VerticalFieldManager)getMainManager()).setBackground(BackgroundFactory.createSolidBackground(Color.LIGHTGRAY));
		mSettingsFields = new SettingsFieldContent(); 
		add(mSettingsFields.getRootField());
		try {
			initFromConf();
		} catch (LinphoneConfigException e) {
			sLogger.warn("no configuration ready yet", e);
		}


	}
	
	
	public static class TransportChoice extends ObjectChoiceField {
		public static final String[] SIP_TRANSPORT_TYPE={"udp","tcp","tls"};  

		public static int valueOf(String str) {
			for (int i=0; i < SIP_TRANSPORT_TYPE.length; ++i) {
				if (SIP_TRANSPORT_TYPE[i].equalsIgnoreCase(str)) return i;
			}
			return 0; // ;)
		}
		public TransportChoice(String label, String selected) {
			super(label, SIP_TRANSPORT_TYPE, selected.toLowerCase());
		}

		public String getSelectedString() {
			return SIP_TRANSPORT_TYPE[getSelectedIndex()];
		}
	}
	protected boolean onSave() {
		mSettingsFields.save();
		return true;
	}
	


	public void initFromConf() throws LinphoneConfigException {

		
		//traces
		boolean lIsDebug = lp.getBoolean(Settings.ADVANCED_DEBUG, Custom.DEFAUL_DEBUG);
		LinphoneCoreFactory.instance().setDebugMode(lIsDebug);
		
		//1 read proxy config from preferences
		String lUserName = lp.getString(Settings.SIP_USERNAME, null);
		if (lUserName == null || lUserName.length()==0) {
			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_NO_USER));
		}

		String lPasswd = lp.getString(Settings.SIP_PASSWORD, null);
		if (lPasswd == null || lPasswd.length()==0) {
			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_NO_PASSWD));
		}

		String lDomain = lp.getString(Settings.SIP_DOMAIN, Custom.DEFAULT_DOMAIN);
		if (lDomain == null || lDomain.length()==0) {
			throw new LinphoneConfigException(mRes.getString(SETTING_DOMAIN));
		}

		String lTransport = lp.getString(Settings.SIP_TRANSPORT, Custom.DEFAULT_TRANSPORT);
		LinphoneCore.Transports transport = new LinphoneCore.Transports();
		transport.tcp = 0;
		transport.udp = 0;
		transport.tls = 0;
		if (lTransport != null && "tcp".equalsIgnoreCase(lTransport)) {
			transport.tcp = Custom.SIP_PORT;
		} else if (lTransport != null && "tls".equalsIgnoreCase(lTransport)) {
			transport.tls = Custom.TLS_PORT;
		} else {
			transport.udp = Custom.SIP_PORT;
		}
		mCore.setSignalingTransportPorts(transport);	
		
		String lPtime = lp.getString(Settings.ADVANCED_PTIME, Custom.DEFAULT_PTIME);
		mCore.setUploadPtime(Integer.parseInt(lPtime));
		
		//auth
		mCore.clearAuthInfos();
		LinphoneAuthInfo lAuthInfo =  LinphoneCoreFactory.instance().createAuthInfo(lUserName, lPasswd,null);
		mCore.addAuthInfo(lAuthInfo);

		boolean encryptMedia=lp.getBoolean(Settings.SRTP_ENCRYPTION, Custom.DEFAULT_ENABLE_SRTP);
		mCore.setMediaEncryption(encryptMedia?MediaEncryption.SRTP:MediaEncryption.None);

		//proxy
		String lProxy = lp.getString(Settings.SIP_PROXY,Custom.DEFAULT_PROXY);
		if (lProxy == null || lProxy.length() == 0) {
			lProxy = "sip:"+lDomain;
		} else if (!lProxy.startsWith("sip:")){
			lProxy="sip:"+lProxy;
		}
		if (transport.tls != 0 && lProxy.substring(4).indexOf(":") == -1) {
			lProxy+=":"+transport.tls;
		}
		//get Default proxy if any
		LinphoneProxyConfig proxyConfig = mCore.getDefaultProxyConfig();
		String lIdentity = "sip:"+lUserName+"@"+lDomain;
		try {
			if (proxyConfig == null) {
				proxyConfig = LinphoneCoreFactory.instance().createProxyConfig(lIdentity, lProxy, null,true);
				proxyConfig.setExpires(Custom.EXPIRE);
				mCore.addProxyConfig(proxyConfig);
				mCore.setDefaultProxyConfig(proxyConfig);

			} else {
				proxyConfig.edit();
				proxyConfig.setIdentity(lIdentity);
				proxyConfig.setProxy(lProxy);
				proxyConfig.enableRegister(true);
				proxyConfig.done();
			}
			proxyConfig = mCore.getDefaultProxyConfig();
			proxyConfig.setDialEscapePlus(lp.getBoolean(Settings.ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO, Custom.DEFAULT_PLUS_TO_ZERO));

			//init network state
			
		} catch (LinphoneCoreException e) {
			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_BAD_CONFIG),e);
		}
	}
	public SettingsFieldContent createSettingsFields() {
		return new SettingsFieldContent();
	}

 }
