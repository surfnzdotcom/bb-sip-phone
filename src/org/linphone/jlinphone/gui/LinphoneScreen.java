package org.linphone.jlinphone.gui;

import org.linphone.core.LinphoneCore;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.GridFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class LinphoneScreen extends MainScreen implements FieldChangeListener, FocusChangeListener{
	private BasicEditField mInputAddress;
	private ButtonField mCall;
	private ButtonField mHangup;
	private HorizontalFieldManager mLayout;
	private LabelField mStatus;
	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	private LinphoneCore mCore;
	
	LinphoneScreen()
    {
		VerticalFieldManager v=new VerticalFieldManager();
		mInputAddress=new BasicEditField(null,null);
		XYEdges edges = new XYEdges(8,8,8,8);
		Border border=BorderFactory.createRoundedBorder(edges);
		mInputAddress.setBorder(border);
		mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,30));
		mCall=new ButtonField("      ",Field.USE_ALL_WIDTH|Field.FIELD_LEFT|ButtonField.CONSUME_CLICK);
		Bitmap bitmap=Bitmap.getBitmapResource("startcall-green.png");
		Background b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
		mCall.setBackground(b);
		mHangup.setMargin(10, 10, 10, 10);
		
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
        
        //mLayout.add(mCall);
        //mLayout.add(mHangup);
        v.add(new LabelField("SIP address or phone number:"));
        v.add(mInputAddress);
        v.add(mLayout);
        mLayout.add(mCall);
        mLayout.add(mHangup);
        v.add(mStatus);
        
        mCall.setChangeListener(this);
        mHangup.setChangeListener(this);
    }

    
    /**
     * Displays a dialog box to the user with the text "Goodbye!" when the
     * application is closed.
     * 
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close()
    {
        // Display a farewell message before closing the application
        Dialog.alert("Goodbye!");     
        super.close();
    }


	public void fieldChanged(Field field, int context) {
		if (field==mCall){
			sLogger.info("Called button pressed.");
		}else if (field==mHangup){
			sLogger.info("Hangup button pressed");
		}
	}


	public void focusChanged(Field field, int eventType) {
		// TODO Auto-generated method stub
		
	}   
}
