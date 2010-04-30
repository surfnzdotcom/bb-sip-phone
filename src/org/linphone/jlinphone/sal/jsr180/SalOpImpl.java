/*
SalImpl.java
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
package org.linphone.jlinphone.sal.jsr180;

import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalOpBase;
import org.linphone.sal.Sal.Reason;

class SalOpImpl extends SalOpBase {

	SalAuthInfo mAutInfo;
	
	public SalOpImpl(Sal sal) {
		super(sal);

	}

	public void authenticate(SalAuthInfo info) {
		mAutInfo = info;
	}

	public void call() {
		// TODO Auto-generated method stub

	}

	public void callAccept() {
		// TODO Auto-generated method stub

	}

	public void callDecline(Reason r, String redirectUri) {
		// TODO Auto-generated method stub

	}

	public void callSetLocalMediaDescription(SalMediaDescription md) {
		// TODO Auto-generated method stub

	}

	public void callTerminate() {
		// TODO Auto-generated method stub
	}
	public SalAuthInfo getAuthInfo() {
		return mAutInfo;
	}
}
