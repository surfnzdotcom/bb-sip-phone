package org.linphone.sal;

public interface SalOp {
	String getFrom();
	void setFrom(String from);
	String getTo();
	void setTo(String to);
	String getContact();
	void setContact(String contact);
	void setUserContext(Object obj);
	Sal getSal();
	Object getUserContext();
}
