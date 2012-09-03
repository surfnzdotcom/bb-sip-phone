/*
SettingField.java
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

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.linphone.jlinphone.gui.SettingsScreen.SettingsFieldContent;

public class SettingField extends VerticalFieldManager implements LinphoneResource, TabFieldItem {
	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private Field contentField;
	private ButtonField saveButton;
	public SettingField(final SettingsFieldContent aContentSettings) {
		contentField=aContentSettings.getRootField();
		add(contentField);
		add( new SeparatorField());
		saveButton = new ButtonField(mRes.getString(SAVE),Field.FOCUSABLE |ButtonField.CONSUME_CLICK);
		saveButton.setRunnable(new Runnable() {
			public void run() {
				aContentSettings.save();
				Dialog.alert("Saved");
			}
		});
		add (saveButton);
	}
	public void onSelected() {
		// TODO Auto-generated method stub
		
	}
	public void onUnSelected() {
		// TODO Auto-generated method stub
		
	}
	public boolean navigateBack() {
		return false;
	}

	public boolean keyChar(char ch, int status, int time) {
		return super.keyChar(ch, status, time);
	}

	protected void sublayout(int maxWidth, int maxHeight) {
		super.sublayout(maxWidth, maxHeight);
		int width= Math.min(Display.getWidth(), getPreferredWidth());
		int height= Display.getHeight() - getTitleHeight();
		int saveHeight=2*saveButton.getPreferredHeight();
		layoutChild(saveButton,width,saveHeight);
		int saveXPos=width/2-saveButton.getPreferredWidth()/2;
		int saveYPos=height-saveHeight;
		setPositionChild(saveButton, saveXPos, saveYPos);

		int msgMaxHeight=height-saveHeight;
		int msgHeight=contentField.getPreferredHeight();
		msgHeight=Math.min(msgHeight, msgMaxHeight);
		layoutChild(contentField,width,msgHeight);
		setPositionChild(contentField, 0, 0);

		setExtent(width, height);
	}
	
	private int getTitleHeight() {
		return ((LinphoneScreen)getScreen()).getTitlePreferredHeight();			
	}
}
