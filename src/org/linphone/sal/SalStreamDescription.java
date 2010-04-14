package org.linphone.sal;

public interface SalStreamDescription {
	enum Type{
		Audio,
		Video,
		Other
	}
	enum Proto{
		RtpAvp,
		RtpSavp
	}
	void setType(Type t);
	Type getType();
	void setProto(Proto p);
	Proto getProto();
	void setAddress(String addr);
	String getAddress();
	void setPort(int port);
	int getPort();
	
}
