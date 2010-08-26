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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import org.linphone.bb.NetworkManager;
import org.linphone.bb.LogHandler;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCore.GeneralState;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;



import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Audio;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class LinphoneScreen extends MainScreen implements LinphoneCoreListener{

 	private HorizontalFieldManager mLayout;
	private LabelField mStatus;
	private static Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	private LinphoneCore mCore;
	private Timer mTimer;
	private SettingsScreen  mSettingsScreen ;
	private ListField mCallLogs;
	private DialerField mDialer;
	private TabField mTabField;
	private final int DIALER_TAB_INDEX=0;
	private final int HISTORY_TAB_INDEX=1;
	private final int SETTINGS_TAB_INDEX=2;
	
	LinphoneScreen()  {

		try {
			Player lDummyRecorder = Manager.createPlayer("capture://audio?encoding=audio/amr");

			lDummyRecorder.realize();
			RecordControl recordControl = (RecordControl) lDummyRecorder.getControl("RecordControl");
			OutputStream lOutput = new ByteArrayOutputStream();
			recordControl.setRecordStream(lOutput);
			recordControl.startRecord();
			lDummyRecorder.start();
			recordControl.commit();
			lOutput.close();
			lDummyRecorder.close();

		} catch (Exception e) {
			sLogger.error("Cannot ask for recorder permission",e);
		}

		LinphoneCoreFactory.setFactoryClassName("org.linphone.jlinphone.core.LinphoneFactoryImpl");
		//LinphoneCoreFactory.instance().setDebugMode(true);//Logger.setGlobalLogLevel(Logger.Debug);
		LinphoneCoreFactory.instance().setLogHandler(new LogHandler());
		sLogger.warn(" Starting version "+ApplicationDescriptor.currentApplicationDescriptor().getVersion());
		VerticalFieldManager v=new VerticalFieldManager();


		addKeyListener(new KeyListener() {
			final static int GREEN_BUTTON_KEY=1114112;
			final static int RED_BUTTON_KEY=1179648;
			final static int VOLUME_DOWN=268500992;
			final static int VOLUME_UP=268435456;
			private int mLastVolumeEvent; 
			public boolean keyChar(char key, int status, int time) {return false;}
			public boolean keyDown(int keycode, int time) {
				if (keycode == GREEN_BUTTON_KEY || keycode == RED_BUTTON_KEY) {
					return true;
				} else if (time != mLastVolumeEvent && (keycode == VOLUME_DOWN || keycode == VOLUME_UP)) {
					mLastVolumeEvent=time;
					// change volume
					int lLeveltoDisplay=100;
					if (mCore.isIncall() ) {
						if (keycode == VOLUME_DOWN && mCore.getPlayLevel() >= 0) {
							lLeveltoDisplay=Math.max(0,(mCore.getPlayLevel() - 10));
						} else if (keycode == VOLUME_UP) {
							lLeveltoDisplay=Math.min(100,mCore.getPlayLevel() + 10);
						}
						mCore.setPlayLevel(lLeveltoDisplay);
					}else {
						if (keycode == VOLUME_DOWN ) {
							lLeveltoDisplay=Math.max(0,(Audio.getVolume() - 10));
						} else if (keycode == VOLUME_UP) {
							lLeveltoDisplay=Math.min(100,Audio.getVolume() + 10);
						}
						Audio.setVolume(lLeveltoDisplay);
					}
					Status.show("Volume ["+lLeveltoDisplay+"]",500);
					return true;
				} else {
					return false;
				}
			}
			public boolean keyRepeat(int keycode, int time) {return false;}
			public boolean keyStatus(int keycode, int time) {return false;}
			public boolean keyUp(int keycode, int time) {
				if (keycode == GREEN_BUTTON_KEY) {
					callButtonPressed();
					return true;
				} else if (keycode == RED_BUTTON_KEY) {
					hangupButtonPressed();
					return true;
				} else {
					return false;
				}
			}
			
		});


		mLayout=new HorizontalFieldManager(Field.FIELD_HCENTER|HorizontalFieldManager.NO_HORIZONTAL_SCROLL);

		mStatus=new LabelField("",Field.FIELD_BOTTOM);
		mStatus.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,20));
		// Set the displayed title of the screen       
		setTitle("Linphone");

		// Add a read only text field (RichTextField) to the screen.  The
		// RichTextField is focusable by default. Here we provide a style
		// parameter to make the field non-focusable.
		add(v);



		v.add(mLayout);
		v.add(mStatus);
		v.add(new SeparatorField());


		try {
			mCore=LinphoneCoreFactory.instance().createLinphoneCore(this, null, null, this);
		} catch (final LinphoneCoreException e) {
			//sLogger.fatal("Cannot create LinphoneCore", e);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					close();
				}
			});

			return;
		}
		
		
		mTabField = new TabField();
		v.add(mTabField);		

		//dialer
		mDialer = new DialerField();
		mTabField.addTab(Bitmap.getBitmapResource("dialer_orange.png"), mDialer);
		//call logs
		mCallLogs = new CallLogsField(mCore, new CallLogsField.Listener() {
			
			public void onSelected(Object selected) {
				String lAddress;
				if (selected == CallDirection.Incoming) {
					lAddress = ((LinphoneCallLog)selected).getFrom().getUserName();
				} else {
					lAddress = ((LinphoneCallLog)selected).getTo().getUserName();
				}
				mDialer.setAddress(lAddress);
				mTabField.display(DIALER_TAB_INDEX);
				
			}
		});
		
		mTabField.addTab(Bitmap.getBitmapResource("history_orange.png"), mCallLogs);
		//settings
		mSettingsScreen = new SettingsScreen(mCore);
		mTabField.addTab(Bitmap.getBitmapResource("settings_orange.png"), mSettingsScreen.createSettingsFields());
		
		addMenuItem(new MenuItem("Settings", 110, 10)
		{
			public void run() 
			{
				UiApplication.getUiApplication().pushScreen(mSettingsScreen);
			}
		});
		addMenuItem(new MenuItem("Console", 110, 10)
		{
			public void run() 
			{
				EventLogger.startEventLogViewer();
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
	}

    
    protected boolean keyControl(char c, int status, int time) {
		// TODO Auto-generated method stub
		return super.keyControl(c, status, time);
	}


	/**
     * Displays a dialog box to the user with the text "Goodbye!" when the
     * application is closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
	public void close() {   
		try {// Display a farewell message before closing the application
			Dialog.alert("Goodbye!");
			if (mCore != null) mCore.destroy();
		} finally {
			super.close();
		}
	}


	private void callButtonPressed() {
		sLogger.info("Call button pressed.");
		try {
			if (mCore.isInComingInvitePending()){
				mCore.acceptCall();
			}else{
				mCore.invite(mDialer.getAddress());
			}
		} catch (final LinphoneCoreException e) {
			sLogger.error("call error",e);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					
				}
			});
		}
		
	}
	public void hangupButtonPressed() {
		sLogger.info("Hangup button pressed");
		mCore.terminateCall();		
	}
	public void focusChanged(Field field, int eventType) {
		// TODO Auto-generated method stub
		
	}


	public void authInfoRequested(LinphoneCore lc, String realm, String username) {
		// TODO Auto-generated method stub
		
	}


	public void byeReceived(LinphoneCore lc, String from) {
		// TODO Auto-generated method stub
		
	}


	public void displayMessage(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}


	public void displayStatus(LinphoneCore lc, final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				mStatus.setText(message);
			}
		});

	}


	public void displayWarning(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}




	public void inviteReceived(LinphoneCore lc, String from) {
		if (!UiApplication.getUiApplication().isForeground()) {
			UiApplication.getUiApplication().requestForeground();
		}
		
	}


	public void show(LinphoneCore lc) {
		// TODO Auto-generated method stub
		
	}


	public void generalState(LinphoneCore lc, GeneralState state, String message) {
		// TODO Auto-generated method stub
		
	}

		
}
