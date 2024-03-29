/*
LinphoneScreen.java
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



import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

import org.linphone.bb.LogHandler;
import org.linphone.bb.NetworkManager;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.GlobalState;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jlinphone.gui.msg.MessengerManager;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

public class LinphoneScreen extends MainScreen implements LinphoneCoreListener , LinphoneResource {
 	private LabelField mStatus;
	private static Logger sLogger=JOrtpFactory.instance().createLogger(Custom.APPNAME);
	private LinphoneCore mCore;
	private Timer mTimer;
	private SettingsScreen  mSettingsScreen ;
	private ListField mCallLogs;
	private DialerField mDialer;
	private Field mMessenger;
	private TabField mTabField;
	private final int HISTORY_TAB_INDEX=0;
	private final int DIALER_TAB_INDEX=1;
	public static final int MESSENGER_TAB_INDEX=2;
	private final int SETTINGS_TAB_INDEX=3;
	static int[] sPermissions = { 
		ApplicationPermissions.PERMISSION_INTERNET
		,ApplicationPermissions.PERMISSION_MEDIA
		,ApplicationPermissions.PERMISSION_ORGANIZER_DATA
		,ApplicationPermissions.PERMISSION_RECORDING
		,ApplicationPermissions.PERMISSION_WIFI
		,ApplicationPermissions.PERMISSION_FILE_API
		,ApplicationPermissions.PERMISSION_SECURITY_DATA
		,ApplicationPermissions.PERMISSION_PHONE};
	
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	LinphoneScreen()  {
		LinphoneCoreFactory.setFactoryClassName("org.linphone.jlinphone.core.LinphoneFactoryImpl");
		LinphoneCoreFactory.instance().setLogHandler(new LogHandler());
		LinphoneCoreFactory.instance().setDebugMode(true);//debug mode until configuration is loaded
		sLogger.warn(" Starting version "+ApplicationDescriptor.currentApplicationDescriptor().getVersion());

		ApplicationPermissions lCurentPermission = ApplicationPermissionsManager.getInstance().getApplicationPermissions();
		boolean lPermissionRequestNeeded = false;
		for (int i=0;i<sPermissions.length;i++) {
			if (!lCurentPermission.containsPermissionKey(sPermissions[i]) 
					|| lCurentPermission.getPermission(sPermissions[i]) != ApplicationPermissions.VALUE_ALLOW) {
				lPermissionRequestNeeded=true;
			}
		}

		if (lPermissionRequestNeeded) {
			ApplicationPermissions lLinphonePermission = new ApplicationPermissions();
			for (int i=0;i<sPermissions.length;i++) {
				lLinphonePermission.addPermission(sPermissions[i]);
			}
			if (!ApplicationPermissionsManager.getInstance().invokePermissionsRequest(lLinphonePermission)) {
				sLogger.error("permission refused");
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						Dialog.alert(mRes.getString(ERROR_AUDIO_PERMISSION_DENY));
						close();
					}
				});
				return;
			}
		}

		// volume control keys
		addKeyListener(new KeyListener() {
			final static int GREEN_BUTTON_KEY=1114112;
			final static int RED_BUTTON_KEY=1179648;
			final static int VOLUME_DOWN=268500992;
			final static int VOLUME_UP=268435456;
			private int mLastVolumeEvent; 
			public boolean keyChar(char key, int status, int time) {return false;}
			public boolean keyDown(int keycode, int time) {
//				mStatus.setText("Key down: " + keycode);
				if(Keypad.key(keycode) == Keypad.KEY_ESCAPE) {
					TabFieldItem item=(TabFieldItem) mTabField.getCurrentField();
					if (item.navigateBack()) return true;
					if (mTabField.getDefault() != mTabField.getCurrentIndex()) {
						mTabField.display(mTabField.getDefault());
						return true;
					}
				}
				if (keycode == GREEN_BUTTON_KEY || keycode == RED_BUTTON_KEY) {
					return true;
				} else if (time != mLastVolumeEvent && (keycode == VOLUME_DOWN || keycode == VOLUME_UP) && mCore.isIncall()) {
					mLastVolumeEvent=time;
					// change volume
					int lLeveltoDisplay=100;
					if (keycode == VOLUME_DOWN && mCore.getPlayLevel() >= 0) {
						lLeveltoDisplay=Math.max(0,(mCore.getPlayLevel() - 10));
					} else if (keycode == VOLUME_UP) {
						lLeveltoDisplay=Math.min(100,mCore.getPlayLevel() + 10);
					}
					mCore.setPlayLevel(lLeveltoDisplay);
					Status.show("Volume ["+lLeveltoDisplay+"]",500);					
					return true;
				} else {
					return false;
				}
			}
			public boolean keyRepeat(int keycode, int time) {return false;}
			public boolean keyStatus(int keycode, int time) {return false;}
			public boolean keyUp(int keycode, int time) {
//				mStatus.setText("Key up: " + keycode);

				if (keycode==GREEN_BUTTON_KEY) {
					return callButtonPressed();
				} else if (keycode==RED_BUTTON_KEY) {
					hangupButtonPressed();
					return true;
				}
				return false;
			}
			
		});


//		Manager titleBar= new VerticalFieldManager();
//		add(titleBar);
		mStatus=new LabelField("",FIELD_BOTTOM);
//		mStatus.setMinimalWidth(Display.getWidth());
		mStatus.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,20));
//		mStatus.setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
		// Set the displayed title of the screen       
		setTitle(mStatus);

		// init liblinphone
		try {
			mCore=LinphoneCoreFactory.instance().createLinphoneCore(this, null, null, this);
		} catch (final LinphoneCoreException e) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					close();
				}
			});

			return;
		}
		
		mTabField = new TabField();

		//call logs
		mCallLogs = new CallLogsField(mCore, new CallLogsField.SelectedListener() {
			
			public void onSelected(Object selected) {
				LinphoneAddress lAddress;
				if (((LinphoneCallLog)selected).getDirection() == CallDirection.Incoming) {
					lAddress = ((LinphoneCallLog)selected).getFrom();
				} else {
					lAddress = ((LinphoneCallLog)selected).getTo();
				}
				mDialer.setAddress(lAddress.getUserName());
				mDialer.setDisplayName(lAddress.getDisplayName());
				mTabField.display(DIALER_TAB_INDEX);
				
			}
		});
		
		mTabField.addTab(Bitmap.getBitmapResource("history_orange.png"), mCallLogs);
		//dialer
		mDialer = new DialerField(mCore);
		mTabField.addTab(Bitmap.getBitmapResource("dialer_orange.png"), mDialer);
		//messenger
		mMessenger = new MessengerManager(mCore);
		mTabField.addTab(Bitmap.getBitmapResource("msg_orange.png"), mMessenger);
		//settings
		mSettingsScreen = new SettingsScreen(mCore);
		mTabField.addTab(Bitmap.getBitmapResource("settings_orange.png"), new SettingField(mSettingsScreen.createSettingsFields()));

		add(mTabField);
		//setBanner(mTabField);
		mTabField.setDefault(DIALER_TAB_INDEX);


		//menu
		addMenuItem(new MenuItem(mRes.getString(SETTINGS), 110, 10)
		{
			public void run() 
			{
				UiApplication.getUiApplication().pushScreen(mSettingsScreen);
			}
		});
		addMenuItem(new MenuItem(mRes.getString(CONSOLE), 110, 10)
		{
			public void run() 
			{
				EventLogger.startEventLogViewer();
			}
		});
		addMenuItem(new MenuItem(mRes.getString(ABOUT), 110, 10)
		{
			public void run() 
			{
				UiApplication.getUiApplication().pushScreen(new AboutScreen());
			}
		});
		
		mTimer=new Timer();
		TimerTask task=new TimerTask(){

			public void run() {
				mCore.iterate();
			}

		};
		mTimer.scheduleAtFixedRate(task, 0, 200);
		

		NetworkManager lNetworkManager = new NetworkManager(mCore);
		//to kick off network state
		lNetworkManager.handleCnxStateChange();
		Application.getApplication().addRadioListener(RadioInfo.WAF_3GPP|RadioInfo.WAF_CDMA,lNetworkManager );
		WLANInfo.addListener(lNetworkManager);
		mCore.setUploadPtime(20);
		
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.LIGHTGRAY));
	}

    
	public void close() {   
		try {
			if (mCore != null) mCore.destroy();
		} finally {
			super.close();
		}
	}


	private boolean callButtonPressed() {
		sLogger.info("Call button pressed.");
		try {
			if (mCore.isInComingInvitePending()){
				mCore.acceptCall(mCore.getCurrentCall());
				return true;
			}
		} catch (final LinphoneCoreException e) {
			sLogger.error("call error",e);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					
				}
			});
		}
		return false;
	}
	public void hangupButtonPressed() {
		sLogger.info("Hangup button pressed");
		mCore.terminateCall(mCore.getCurrentCall());		
	}

	public void authInfoRequested(LinphoneCore lc, String realm, String username) {
		// TODO Auto-generated method stub
		
	}




	public void displayMessage(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}


	public void displayStatus(LinphoneCore lc, final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				mStatus.setText(Custom.APPNAME +"  "+message);
			}
		});

	}


	public void displayWarning(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}







	public void show(LinphoneCore lc) {
		// TODO Auto-generated method stub
		
	}

	public void callState(LinphoneCore lc, LinphoneCall call, final State state,
			String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (state == LinphoneCall.State.IncomingReceived && !UiApplication.getUiApplication().isForeground()) {
					UiApplication.getUiApplication().requestForeground();
				}
				if (state == LinphoneCall.State.OutgoingInit || state == LinphoneCall.State.IncomingReceived ) {
					mDialer.enableIncallFields();
				} else if (state == LinphoneCall.State.Connected) {
					mTabField.mCallStateField.setCallState(CallStateIndicatorField.CallStateINCALL);
				} else if (state==LinphoneCall.State.CallEnd | state==LinphoneCall.State.Error) {
					mDialer.enableOutOfCallFields();
					mTabField.mCallStateField.setCallState(CallStateIndicatorField.CallStateENDCALL);
				}
			}
		});
	}


	public void globalState(LinphoneCore lc, GlobalState state, String message) {
		// TODO Auto-generated method stub
		
	}


	public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
			RegistrationState cstate, String smessage) {
		
		sLogger.info("new registration state ["+cstate+"]");
		if (cfg.getState() == RegistrationState.RegistrationFailed && lc.isNetworkReachable()) {
			int nextTryIn = 15;
			sLogger.info("trying new registration in ["+nextTryIn+"]");
			mTimer.schedule(new TimerTask() {
				
				public void run() {
					mCore.refreshRegisters();
					
				}
			}, nextTryIn*1000);
		}
	}


	public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,
			String url) {
		// TODO Auto-generated method stub
		
	}


	public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
		// TODO Auto-generated method stub
		
	}

	public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneChatMessage message) {
		// See directly in LinphoneCoreImpl
	}
	

	public void textReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneAddress from, String message) {
		throw new RuntimeException("deprecated");
	}


	public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status,
			int delayMs, Object data) {
		// TODO Auto-generated method stub
		
	}


	public void callEncryptionChanged(final LinphoneCore lc, final LinphoneCall call,
			final boolean encrypted, final String authenticationToken) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				int state=encrypted?CallStateIndicatorField.CallStateSECURE:CallStateIndicatorField.CallStateUNSECURE;
				mTabField.mCallStateField.setCallState(state);
			}
		});
	}


	public void notifyReceived(LinphoneCore lc, LinphoneCall call,
			LinphoneAddress from, byte[] event) {
		// TODO Auto-generated method stub
		
	}

	protected boolean onSavePrompt() {
		return true; // don't bug at user on close
	}
	
	protected void sublayout(int width, int height) {
		super.sublayout(width, height);
	}

	private static int titleHeightCache=0;
	public int getTitlePreferredHeight() {
		if (titleHeightCache==0) {
			titleHeightCache=mStatus.getHeight()+mTabField.getHeight();
		}
		return titleHeightCache;
	}
	
	public void setFocusToTab(int pos) {
		mTabField.setFocusOnTab(pos);
	}
	
	public boolean isDisplayedTab(int pos) {
		return mTabField.mCurrentIndex==pos;
	}


	public void callStatsUpdated(LinphoneCore lc, LinphoneCall call,
			LinphoneCallStats stats) {
	}


	public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {
	}
}
