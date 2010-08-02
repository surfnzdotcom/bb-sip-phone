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
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneLogHandler;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LinphoneCore.GeneralState;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;



import net.rim.device.api.collection.util.BasicFilteredList;
import net.rim.device.api.collection.util.BasicFilteredListResult;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.WLANConnectionListener;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.AutoCompleteField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.KeywordFilterField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class LinphoneScreen extends MainScreen implements FieldChangeListener, FocusChangeListener, LinphoneCoreListener{
	private AutoCompleteField  mInputAddress;
	private KeywordFilterField mkeyWordField;    
    private PhoneNumberList mPhoneNumberList;
    
	private ButtonField mCall;
	private ButtonField mHangup;
	private HorizontalFieldManager mLayout;
	private LabelField mStatus;
	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	private LinphoneCore mCore;
	private Timer mTimer;
	private ConsoleScreen mConsoleScreen = new ConsoleScreen();
	private SettingsScreen  mSettingsScreen ;
	private ListField mCallLogs;
	 
	
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
		LinphoneCoreFactory.instance().setLogHandler(new LinphoneLogHandler() {

			public void log(String loggerName, int level, String levelName, String msg, Throwable e) {
				StringBuffer sb=new StringBuffer();
				sb.append(loggerName);
				sb.append("-");
				sb.append(levelName);
				sb.append(":");
				sb.append(msg);
				if (e!=null) {
					sb.append(" ["+e.getMessage()+"]");
				}

				mConsoleScreen.log(sb.toString());

			}

		});
		VerticalFieldManager v=new VerticalFieldManager();
/*		ContactList contacts = null;
		  try {
		     contacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
		     Enumeration items = contacts.items();
		     Contact lContact;
		     Vector lPhoneNumbers = new Vector();
		     while (items.hasMoreElements()) {
		    	 lContact = (Contact) items.nextElement();
		    	 int phoneNumbers = lContact.countValues(Contact.TEL);
		    	 for(int i = 0; i < phoneNumbers; i++) {
		    		 lPhoneNumbers.addElement(lContact.getString(Contact.TEL, i));
		    	 }
		     }
		     mPhoneNumberList = new PhoneNumberList(lPhoneNumbers);
		     mkeyWordField = new KeywordFilterField();
		     mkeyWordField.setSourceList(mPhoneNumberList, mPhoneNumberList);
		  } catch (PIMException e) {
			  sLogger.error("Cannot open contact list",e);
		  }
*/
		addKeyListener(new KeyListener() {
			final static int GREEN_BUTTON_KEY=1114112;
			final static int RED_BUTTON_KEY=1179648;
			final static int VOLUME_DOWN=268500992;
			final static int VOLUME_UP=268500992;
			
			public boolean keyChar(char key, int status, int time) {return false;}
			public boolean keyDown(int keycode, int time) {
				if (keycode == GREEN_BUTTON_KEY || keycode == RED_BUTTON_KEY) {
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
		BasicFilteredList filterList = new BasicFilteredList();
	    int uniqueID = 0;
	    int srcType = BasicFilteredList.DATA_SOURCE_CONTACTS;
	    long searchField = BasicFilteredList.DATA_FIELD_CONTACTS_NAME_FULL|BasicFilteredList.DATA_FIELD_CONTACTS_PHONE_ALL;
	    long requestedFields = /*BasicFilteredList.DATA_FIELD_CONTACTS_NAME_FULL|*/BasicFilteredList.DATA_FIELD_CONTACTS_PHONE_ALL;
	    long primaryField = BasicFilteredList.DATA_FIELD_CONTACTS_PHONE_ALL;
	    long secondaryField = BasicFilteredList.DATA_FIELD_CONTACTS_PHONE_ALL;
	    filterList.addDataSource(uniqueID,
	    							srcType,
	    							searchField,
	    							requestedFields,
	    							primaryField,
	    							-1, //no secondary specified
	      							"Contact");
	      long style = AutoCompleteField.LIST_EXPAND_ON_HIGHLIGHT ;
	      AutoCompleteField autoCompleteField = new AutoCompleteField(filterList, style)  {
	    	  protected void onSelect(Object selection,
                      int type) {
	    		  
	    		  BasicFilteredListResult lResult =((BasicFilteredListResult)selection);
	    		  Field toto = getLeafFieldWithFocus();
	    		  String lSelectedNumber;
	    		  for (int i = 0; i<getListField().getSelectedIndex();i++) {
	    			  //lSelectedNumber
	    			 // toto = getListField()
	    		  }
	    		  super.onSelect(selection,type);
	    	  }
	    	  public Object get(ListField listField,
	                  int index) {
	    		  return super.get(listField, index);
	    	  }
	      };
	     
	    mInputAddress = autoCompleteField;
	    //add(autoCompleteField);
		//mInputAddress = mkeyWordField.getKeywordField();
		//mInputAddress=new BasicEditField(null,null);
		//mkeyWordField.setKeywordField(mInputAddress);
		XYEdges edges = new XYEdges(8,8,8,8);
		Border border=BorderFactory.createRoundedBorder(edges);
		mInputAddress.setBorder(border);
		mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,30));
		
		//mInputAddress.setText("0033952636505");
		mCall=new ButtonField("      ",Field.USE_ALL_WIDTH|Field.FIELD_LEFT|ButtonField.CONSUME_CLICK);
		Bitmap bitmap=Bitmap.getBitmapResource("startcall-green.png");
		Background b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
		mCall.setBackground(b);
		mCall.setMargin(10, 10, 10, 10);

		mHangup=new ButtonField("      ",Field.USE_ALL_WIDTH|Field.FIELD_RIGHT|ButtonField.CONSUME_CLICK);
		bitmap=Bitmap.getBitmapResource("stopcall-red.png");
		b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
		mHangup.setBackground(b);
		mHangup.setMargin(10, 10, 10, 10);

		mLayout=new HorizontalFieldManager(Field.FIELD_HCENTER|HorizontalFieldManager.NO_HORIZONTAL_SCROLL);

		mStatus=new LabelField("",Field.FIELD_BOTTOM);
		// Set the displayed title of the screen       
		setTitle("Linphone");

		// Add a read only text field (RichTextField) to the screen.  The
		// RichTextField is focusable by default. Here we provide a style
		// parameter to make the field non-focusable.
		add(v);

		v.add(mInputAddress);
		v.add(mLayout);
		if (Touchscreen.isSupported()) {
			mLayout.add(mCall);
			mLayout.add(mHangup);
		}
		v.add(mStatus);
		v.add(new SeparatorField());

		
		
		

		mCall.setChangeListener(this);
		mHangup.setChangeListener(this);


		try {

			mCore=LinphoneCoreFactory.instance().createLinphoneCore(this, null, null, this);
			mCore.setNetworkStateReachable(false); //we don't know yet network state
		} catch (final LinphoneCoreException e) {
			sLogger.fatal("Cannot create LinphoneCore", e);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					close();
				}
			});

			return;
		}
		
		
		//call logs
		mCallLogs = new CallLogsField(mCore, new CallLogsField.Listener() {
			
			public void onSelected(LinphoneCallLog selected) {
				String lAddress;
				if (selected == CallDirection.Incoming) {
					lAddress = selected.getFrom().getUserName();
				} else {
					lAddress = selected.getTo().getUserName();
				}
				mInputAddress.getEditField().setText(lAddress);
				
			}
		});
		v.add(mCallLogs);
		
		mSettingsScreen = new SettingsScreen(mCore);
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
				UiApplication.getUiApplication().pushScreen(mConsoleScreen);
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


	public void fieldChanged(Field field, int context) {
		if (field==mCall){
			callButtonPressed();
		}else if (field==mHangup){
			hangupButtonPressed();
		}
	}

	private void callButtonPressed() {
		sLogger.info("Called button pressed.");
		try {
			if (mCore.isInComingInvitePending()){
				mCore.acceptCall();
			}else{
				mCore.invite(mInputAddress.getEditField().getText());
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


	public void generalState(LinphoneCore lc, GeneralState state) {
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

		
}
