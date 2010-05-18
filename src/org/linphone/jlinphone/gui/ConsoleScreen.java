/*
ConsoleScreen.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package org.linphone.jlinphone.gui;



import java.util.Vector;

import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class ConsoleScreen extends MainScreen {
	Vector mLogs = new Vector(2048);
	int writePointer=0;
	private RichTextField mLogField = new RichTextField();

	public ConsoleScreen() {
		setTitle("Linphone Console");
		for (int i=0;i<mLogs.capacity();i++) {
			mLogs.addElement(null);
		}
		Font f = mLogField.getFont();
		mLogField.setFont(f.derive(f.getStyle(), f.getHeight() * 3 / 5));
		add(mLogField);
	}
	public synchronized void log(String message) {
		mLogs.setElementAt(message, writePointer);
		if (writePointer < mLogs.capacity()) {
			writePointer++;
		} else {
			writePointer=0;
		}
	}
	protected synchronized void  onUiEngineAttached(boolean attached) {
		super.onUiEngineAttached(attached);
		if (attached) {
			int i=writePointer;
			do  {
				String lMessage = (String) mLogs.elementAt(i);
				if (lMessage !=null) {
					mLogField.insert(lMessage + "\n");
				}
				if (i < mLogs.size()-1) {
					i++;
				} else {
					i=0;
				}

			} while (i!=writePointer);
		} else {
			mLogField.clear(0);
		}
	}
	
	

	
}
