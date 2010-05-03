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

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalException;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOpBase;
import org.linphone.sal.Sal.Reason;

import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipConnection;

class SalOpImpl extends SalOpBase {
	static Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SalAuthInfo mAutInfo;
	SipClientConnection mSipRegisterCnx;
	
	public SalOpImpl(Sal sal) {
		super(sal);

	}
	public void setRegisterSipCnx(SipClientConnection cnx) {
		mSipRegisterCnx=cnx;
	}
	public SipConnection getSipCnx() {
		return mSipRegisterCnx;
	}
	public void authenticate(SalAuthInfo info) throws SalException{
		mAutInfo = info;
		try {
			if (mSipRegisterCnx != null) {
				if ( info != null) {
					mSipRegisterCnx.setCredentials(info.getUserid(), info.getPassword(),info.getRealm());
				} else {
					throw new Exception("Bad auth info ["+info+"]");
				}
			} else {
				mLog.warn("no registrar connection ready yet");
			}
		} catch (Exception e) {
			throw new SalException("Cannot authenticate",e);
		}
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
