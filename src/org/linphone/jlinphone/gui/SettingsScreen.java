/*
SettingsScreen.java
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

import java.util.Hashtable;

import org.linphone.core.LinphoneCoreFactory;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.CheckboxField;

import net.rim.device.api.ui.container.MainScreen;

public class SettingsScreen extends MainScreen {
	 
	PersistentObject mPersistentObject;
	Hashtable lSettingsMap;
	SettingsScreen() {
		mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.settings".hashCode() );
		if (mPersistentObject.getContents() != null) {
			lSettingsMap = (Hashtable) mPersistentObject.getContents();
		} else {
			lSettingsMap = new Hashtable();
		}
		setTitle("Linphone Settings");
		CheckboxField lDebugMode = new CheckboxField("Enable debug mode", false);
		lDebugMode.setChangeListener(new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
				LinphoneCoreFactory.instance().setDebugMode(((CheckboxField)field).getChecked());
				
			}
			
		});
		add(lDebugMode);
	}
	
	public boolean getBoolean(String key,boolean defaultValue) {
		boolean lResult = defaultValue;
		return lResult;
		
	}


}
