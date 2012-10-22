package org.linphone.jlinphone.gui;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIMException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.KeywordFilterField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.text.PhoneTextFilter;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

public class AdvancedSearchableContactList extends VerticalFieldManager implements LinphoneResource  {

	private String mDisplayName;
	protected TextField  mInputAddress;
	protected KeywordFilterField mKeywordFilter;
	private ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private static Logger sLogger=JOrtpFactory.instance().createLogger(Custom.APPNAME);

	private void strangelyRetrieveKeywordFilter() {
		try {
			mKeywordFilter = new SearchableContactListListener(new SearchableContactListListener.Listener() {
				public void onSelected(Contact selected) {
					boolean previousValueConfirmed=!setAddressAndDisplay(selected);
					if (previousValueConfirmed) {
						onContactChosen(getAddress(), getDisplayName());						
					}
				}
			}).getKeywordFilterField();
		  } catch (PIMException e) {
			  sLogger.error("Cannot open contact list",e);
		  }

		mKeywordFilter.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (mKeywordFilter.getKeyword().length() == 0) {
					mKeywordFilter.setKeyword(null);
					 mInputAddress.setLabel("sip:");
				}
			}
		  });
	}

	private void initKeywordField() {
		mKeywordFilter.setKeywordField(new TextField(Field.NON_FOCUSABLE));
		mKeywordFilter.getKeywordField().setLabel(mRes.getString(FIND));
		mKeywordFilter.getKeywordField().setEditable(false);
	}

	private void initInputAddressField() {
		mInputAddress = new InputAddressField(Field.FOCUSABLE);
	    mInputAddress.setLabel("sip:");
	    mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,50));		
	}

	private void addFieldsToSelf() {
	    add(mInputAddress);
	    add(new SeparatorField());
	    add(mKeywordFilter.getKeywordField());
	    add(new SeparatorField());
	    add(mKeywordFilter);		
	}


	public AdvancedSearchableContactList() {
		strangelyRetrieveKeywordFilter();
		initKeywordField();
		initInputAddressField();
		addFieldsToSelf();
	}
	
	protected boolean setAddressAndDisplay (Contact aContact) {
		boolean changed=setAddress( aContact.getString(Contact.TEL, 0));
		String[] lContactNames = aContact.getStringArray(Contact.NAME, 0);
		StringBuffer lDisplayName = new StringBuffer();

		if (lContactNames[Contact.NAME_GIVEN] != null ) {
			lDisplayName.append(lContactNames[Contact.NAME_GIVEN]);
		}
		if (lContactNames[Contact.NAME_FAMILY] != null ) {
			if (lDisplayName.length()!= 0) lDisplayName.append(' ');
			lDisplayName.append(lContactNames[Contact.NAME_FAMILY]);
		}
		if (lDisplayName.length()!=0) {
			boolean displayNameChanged=setDisplayName(lDisplayName.toString());
			changed|=displayNameChanged;
		} else {
			changed=true;
			setDisplayName(null);
		}
		return changed;
	}
	public boolean setAddress(String aValue) {
		if (aValue.length()>0) mInputAddress.setLabel(null);
		if (aValue != null && aValue.equals(mInputAddress.getText())) {
			return false;
		}
		mInputAddress.setText(aValue);
		return true;
	}
	public String getAddress() {
		return mInputAddress.getText();
	}
	public String getDisplayName() {
		return mDisplayName;
	}
	public boolean setDisplayName(String aDisplayName) {
		if (aDisplayName!=null && aDisplayName.equals(mDisplayName)) {
			return false;
		}
		mDisplayName=aDisplayName;
		return true;
	}
	
	
	protected void onContactChosen(String uri, String displayName) {
		
	}

	
	

	private class InputAddressField extends TextField {
		private PhoneTextFilter mPhoneTextFilter = new PhoneTextFilter();
		public InputAddressField(long style) {
			super(style);
		}
		boolean mInDigitMode=true;
		protected boolean insert(char charater, int arg1) {
			char lNumber = mPhoneTextFilter.convert(charater, 0);
			StringBuffer lnewKey = new StringBuffer(mKeywordFilter.getKeywordField().getText());
			mKeywordFilter.setKeyword(lnewKey.insert(getCursorPosition(), charater).toString());

			if (mInDigitMode ==true && 0<=Character.digit(lNumber,10) && Character.digit(lNumber,10)<10) {
				return super.insert(lNumber, arg1);
			} else {
				if (mInDigitMode==true) {
					setText(mKeywordFilter.getKeyword());
					mInDigitMode=false;
					return true;
				}
				return super.insert(charater, arg1);
			}

		}
		protected synchronized boolean backspace() {
			if(getCursorPosition()<=mKeywordFilter.getKeyword().length()){
				StringBuffer lnewKey = new StringBuffer(mKeywordFilter.getKeyword());
				mKeywordFilter.setKeyword(lnewKey.delete(getCursorPosition()-1,getCursorPosition()).toString());
			}
			return super.backspace();
		}
		protected boolean keyChar(char key, int status, int time) {
			if (key == '\n') {
				onContactChosen(getText(), mDisplayName);
				return true;
			}
			mDisplayName=null; //Erase display name if any key is manually entered
			if (getTextLength()!=0) {
				setLabel("");
			} else {
				mInDigitMode=true;
			}
			if (key == Characters.BACKSPACE && getCursorPosition()==0 && getTextLength() !=0) {
				StringBuffer lnewKey = new StringBuffer(mKeywordFilter.getKeywordField().getText());
				mKeywordFilter.setKeyword(lnewKey.delete(0,1).toString());
			}
			if (getTextLength() == 0 || (key == Characters.BACKSPACE && getTextLength()==1)) {
				setLabel("sip:");
			}
			return super.keyChar(key, status, time);
		}
	}
}
