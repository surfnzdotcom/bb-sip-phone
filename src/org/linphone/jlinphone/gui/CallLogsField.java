/*
CallLogsField.java
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

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public class CallLogsField extends SelectableListField {

	final LinphoneCore mCore;
	
	CallLogsField(LinphoneCore aCore,Listener aListener) {
		super (aListener);
		mCore = aCore;
		
		setCallback(new ListFieldCallback() { 
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w) { 
		        LinphoneCallLog lCallLog = (LinphoneCallLog) get(list,index);
		    	if (lCallLog.getDirection() == CallDirection.Incoming) {
		    		g.drawText("IN "+ (lCallLog.getFrom()!=null?lCallLog.getFrom().getUserName():"unknown"), 0,y,0,w);
		    	} else {
		    		g.drawText("OUT "+ (lCallLog.getTo()!=null?lCallLog.getTo().getUserName():"unknown"), 0,y,0,w);
		    		
		    	}
		    } 
		    public Object get(ListField list, int index) {
		        return mCore.getCallLogs().elementAt(getLenth()-index-1); 
		    } 
		    public int indexOfList(ListField list, String prefix, int string) { 
		        return mCore.getCallLogs().indexOf(prefix, string); 
		    } 
		    public int getPreferredWidth(ListField list) { 
		        return Display.getWidth(); 
		    }
		    private int getLenth() {
		    	return  mCore.getCallLogs().size();
		    }
		});
		refresh();
	}
	public void refresh() {
		setSize(mCore.getCallLogs().size());
	}
	


	
}
