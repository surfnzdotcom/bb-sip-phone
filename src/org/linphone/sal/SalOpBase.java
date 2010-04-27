package org.linphone.sal;

public class SalOpBase implements SalOp {
	String mContact;
	String mFrom;
	String mTo;
	Object mContext;
	
	public String getContact() {
		return mContact;
	}

	public String getFrom() {
		return mFrom;
	}

	public String getTo() {
		return mTo;
	}

	public Object getUserContext() {
		return mContext;
	}

	public void setContact(String contact) {
		mContact=contact;
	}

	public void setFrom(String from) {
		mFrom=from;
	}

	public void setTo(String to) {
		mTo=to;
	}

	public void setUserContext(Object obj) {
		mContext=obj;
	}

}
