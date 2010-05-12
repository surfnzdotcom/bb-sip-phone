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



import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class ConsoleScreen extends MainScreen {
	private RichTextField mLogField = new RichTextField();

	public ConsoleScreen() {
		setTitle("Linphone Console");
		
		Font f = mLogField.getFont();
		mLogField.setFont(f.derive(f.getStyle(), f.getHeight() * 3 / 5));
		add(mLogField);
	}
	public void log(String message) {
		mLogField.insert(message);
	}

	
}
