package org.linphone.sal;

import java.util.List;

public class SalMediaDescription{
	List mStreams;
	public SalStreamDescription getStream(int index) {
		return (SalStreamDescription)mStreams.get(index);
	}
	public void addStreamDescription(SalStreamDescription sd) {
		mStreams.add(sd);
	}

}
