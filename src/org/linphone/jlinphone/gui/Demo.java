package org.linphone.jlinphone.gui;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import net.rim.device.api.io.FileInputStream;

import org.linphone.core.LinphoneCore;



public class Demo extends MIDlet implements CommandListener, ItemCommandListener {

	private Display mDisplay;
	private Command mExitCommand;
	private Form mForm;
	private TextField mEntry;
	private ImageItem mCallButton;
	private ImageItem mHangupButton;
	private Command mCallCommand;
	private Command mHangupCommand;
	private Image mGreen,mRed;
	private TextField mStatusBar;
	
	private LinphoneCore mCore;
	
	public Demo(){
		FileInputStream is;
		mDisplay = Display.getDisplay(this);
		mForm=new Form("jLinphone");
		mEntry=new TextField("SIP address or number", null, 100, TextField.ANY);
		try {
			is=new FileInputStream(0,"startcall-green.png");
			mGreen=Image.createImage(is);
			is=new FileInputStream(0,"stopcall-red.png");
			mRed=Image.createImage(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCallButton=new ImageItem("Call",mGreen,Item.LAYOUT_CENTER,"Call",Item.BUTTON);
		mHangupButton=new ImageItem("Hangup",mRed,Item.LAYOUT_CENTER,"Hangup",Item.BUTTON);
		mCallCommand=new Command("Call", Command.ITEM,1);
		mHangupCommand=new Command("Hangup", Command.ITEM,1);
		mStatusBar=new TextField(null,null,100,TextField.UNEDITABLE);
	}

	
	protected void startApp() throws MIDletStateChangeException {
		mExitCommand = new Command("Exit", Command.EXIT, 0x01);
		
		mForm.setCommandListener(this);
		mForm.addCommand(mExitCommand);
		
		mCallButton.setDefaultCommand(mCallCommand);
		mCallButton.setItemCommandListener(this);
		mHangupButton.setDefaultCommand(mHangupCommand);
		mHangupButton.setItemCommandListener(this);
		
		mEntry.setLayout(Item.LAYOUT_CENTER);
		mForm.append(mEntry);
		mForm.append(mCallButton);
		mForm.append(mHangupButton);
		mStatusBar.setLayout(Item.LAYOUT_BOTTOM);
		mForm.append(mStatusBar);
		
		mDisplay.setCurrent(mForm);
	}

	

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable displayable) {
		if (command == mExitCommand) {
			try {
				this.destroyApp(true);
			} catch (MIDletStateChangeException e) {
				e.printStackTrace();
			} finally {				
				this.notifyDestroyed();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {}


	public void commandAction(Command arg0, Item arg1) {
		
	}
}
