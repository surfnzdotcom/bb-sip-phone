package org.linphone.sal;

import org.linphone.jortp.PayloadType;

public class SalStreamDescription {
	private String mAddress;
	private PayloadType [] mPayloadTypes;
	int mPort;
	enum Proto{
		RtpAvp,
		RtpSavp,
	}
	enum Type{
		Audio,
		Video,
		Other
	}
	Proto mProto;
	Type mType;
	public String getAddress() {
		return mAddress;
	}

	public PayloadType[] getPayloadTypes() {
		return mPayloadTypes;
	}

	public int getPort() {
		return mPort;
	}

	public Proto getProto() {
		return mProto;
	}

	public Type getType() {
		return mType;
	}

	public void setAddress(String addr) {
		mAddress=addr;
	}

	public void setPayloadTypes(PayloadType[] pts) {
		mPayloadTypes=pts;
	}

	public void setPort(int port) {
		mPort=port;
	}

	public void setProto(Proto p) {
		mProto=p;
	}

	public void setType(Type t) {
		mType=t;
	}

}
