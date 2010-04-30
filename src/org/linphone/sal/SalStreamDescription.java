package org.linphone.sal;

import org.linphone.jortp.PayloadType;

public class SalStreamDescription {
	private String mAddress;
	private PayloadType [] mPayloadTypes;
	private int mPort;
	private Proto mProto;
	private Type mType;
	private int mPtime;
	
	public static class Proto{
		static public Proto RtpAvp = new Proto("RtpAvp");
		static public Proto RtpSavp = new Proto("RtpSavp");
		private String mStringValue;
		private Proto(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	};
	public static class Type{
		static public Type Audio = new Type("Audio");
		static public Type Video = new Type("Video");
		static public Type Other = new Type("Other");
		private String mStringValue;
		private Type(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	};
	
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

	public void setPtime(int ptime) {
		mPtime=ptime;
	}

	public int getPtime(){
		return mPtime;
	}
}
