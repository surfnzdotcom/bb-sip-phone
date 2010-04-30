package org.linphone.sal;

import java.util.Vector;

public class SalMediaDescription{
	Vector mStreams;
	String mAddress;

	public SalStreamDescription getStream(int index) {
		return (SalStreamDescription) mStreams.elementAt(index);
	}
	public int getNumStreams(){
		return mStreams.size();
	}
	public void addStreamDescription(SalStreamDescription sd) {
		mStreams.addElement(sd);
	}
	public void setAddress(String addr){
		mAddress=addr;
	}
	public String getAddress(){
		return mAddress;
	}
}
