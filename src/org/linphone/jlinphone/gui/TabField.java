/*
TabField.java
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

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;

import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class TabField extends VerticalFieldManager {
	HorizontalFieldManager mTabController = new HorizontalFieldManager();
	Vector mTabFields = new Vector();
	Field mCurrentField;
	int mDefault=0;
	public TabField() {
		add(mTabController);
		add(new SeparatorField());
	}
	void setDefault(int index) {
		mDefault=index;
		if (mDefault <= mTabFields.size()-1) {
			delete(mCurrentField);
			mCurrentField=(Field) mTabFields.elementAt(mDefault);
			add(mCurrentField);
			setDirty(true);
		}
	}
	public void addTab(Bitmap aBitmap, final Field aTabField) {
		Bitmap lBitmapScaled = new Bitmap(50, 50);
		lBitmapScaled.createAlpha(Bitmap.ALPHA_BITDEPTH_MONO);
		aBitmap.scaleInto(lBitmapScaled,  Bitmap.FILTER_LANCZOS);
		BitmapField lButton = new BitmapField(lBitmapScaled,Field.FOCUSABLE) {
			 
			protected boolean navigationUnclick(int status, int time) {
				if (aTabField == null) {
					return  false; //nop
				}
				if (mCurrentField != null) TabField.this.delete(mCurrentField);
				TabField.this.add(aTabField);
				TabField.this.setDirty(true);
				mCurrentField=aTabField;
				return true;
				
			}
		};
		mTabController.add(lButton);
		mTabFields.addElement(aTabField);
		
		if (mDefault == mTabFields.size()-1 && aTabField!=null) {
			this.add(aTabField);
			mCurrentField=aTabField;
		}
	}

	
}
