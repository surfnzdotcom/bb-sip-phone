package org.linphone.jlinphone.gui;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIMException;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.KeywordFilterField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class DialerField extends VerticalFieldManager {
	private TextField  mInputAddress;
	private KeywordFilterField mkeyWordField;    
	private static Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
	public DialerField() {
		try {
		    
			mkeyWordField = new SearchableContactList(new SearchableContactList.Listener() {
				
				public void onSelected(Contact selected) {
					setAddress( selected.getString(Contact.TEL, 0));
					
				}
			}).getKeywordFilterField();
		  } catch (PIMException e) {
			  sLogger.error("Cannot open contact list",e);
		  }
	    mkeyWordField.setKeywordField(new TextField(Field.FOCUSABLE));
	    mInputAddress = mkeyWordField.getKeywordField();
	    mInputAddress.setLabel("sip:");
	    mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,30));
		add(mInputAddress);
		add(new SeparatorField());
		add(mkeyWordField);
	}
	public void setAddress(String aValue) {
		mInputAddress.setText(aValue);
	}
	public String getAddress() {
		return mInputAddress.getText();
	}
	protected void onFocus(int direction) {
		// TODO Auto-generated method stub
		super.onFocus(direction);
	}
	
}
