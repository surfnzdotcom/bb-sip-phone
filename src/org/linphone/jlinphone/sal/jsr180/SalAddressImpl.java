/*
SalAddressImpl.java
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

import org.linphone.sal.SalAddress;

import sip4me.nist.javax.microedition.sip.SipAddress;

class SalAddressImpl implements SalAddress {
	final SipAddress mAddress;
	SalAddressImpl(String address) {
		mAddress = new SipAddress(address);
	}
	public String asString() {
		return mAddress.getURI().toString();
	}

	public String asStringUriOnly() {
		return mAddress.getURI();
	}

	public void clean() {
		throw new RuntimeException("Not implemented"); 
	}

	public String getDisplayName() {
		return mAddress.getDisplayName();
	}

	public String getDomain() {
		return mAddress.getHost(); 
	}

	public String getPort() {
		return String.valueOf(getPortInt());
	}

	public int getPortInt() {
		return mAddress.getPort();
	}

	public String getUserName() {
		return mAddress.getUser();
	}

	public void setDisplayName(String displayName) {
		mAddress.setDisplayName(displayName);
	}

	public void setDomain(String domain) {
		mAddress.setHost(domain);
	}

	public void setPort(String port) {
		setPortInt(Integer.parseInt(port));
	}

	public void setPortInt(int port) {
		mAddress.setPort(port);
	}

	public void setUserName(String username) {
		mAddress.setUser(username);
	}
	public String toString() {
		return asString();
	}
}
