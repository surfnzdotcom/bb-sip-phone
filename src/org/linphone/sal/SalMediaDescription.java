package org.linphone.sal;

import java.util.Vector;

public class SalMediaDescription{
	Vector mStreams;
	public SalStreamDescription getStream(int index) {
		return (SalStreamDescription) mStreams.elementAt(index);
	}
	public void addStreamDescription(SalStreamDescription sd) {
		mStreams.addElement(sd);
	}

}
