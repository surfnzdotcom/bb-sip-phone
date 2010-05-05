package org.linphone.jlinphone.gui;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.GridFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class LinphoneScreen extends MainScreen implements FieldChangeListener{
	private BasicEditField mInputAddress;
	private ButtonField mCall;
	private ButtonField mHangup;
	private BitmapField mBitmap;
	private HorizontalFieldManager mLayout;
	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	
	LinphoneScreen()
    {
		VerticalFieldManager v=new VerticalFieldManager();
		mInputAddress=new BasicEditField(null,"sip:");
		mCall=new ButtonField("Call",Field.USE_ALL_WIDTH|Field.USE_ALL_HEIGHT|Field.FOCUSABLE|Field.FIELD_HCENTER);
		Bitmap bitmap=Bitmap.getBitmapResource("startcall-green.png");
		Background b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
		mCall.setBackground(b);
		
		mHangup=new ButtonField("Hangup",Field.USE_ALL_WIDTH|Field.FOCUSABLE|Field.FIELD_HCENTER);
		bitmap=Bitmap.getBitmapResource("stopcall-red.png");
		b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
		mHangup.setBackground(b);
		
		mLayout=new HorizontalFieldManager();
		
		mBitmap=new BitmapField();
		mBitmap.setBitmap(bitmap);
		
        // Set the displayed title of the screen       
        setTitle("Linphone");

        // Add a read only text field (RichTextField) to the screen.  The
        // RichTextField is focusable by default. Here we provide a style
        // parameter to make the field non-focusable.
        add(v);
        
        //mLayout.add(mCall);
        //mLayout.add(mHangup);
        v.add(mInputAddress);
        v.add(mCall);
        v.add(mHangup);
        //v.add(mLayout);
        //mLayout.add(mBitmap);
        
        
        
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
		}
	}   
}
