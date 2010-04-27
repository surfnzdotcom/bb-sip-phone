package org.linphone.jlinphone.core;

import org.linphone.core.LinphoneAddress;
import org.linphone.sal.SalAddress;

public class LinphoneAddressImpl implements LinphoneAddress {
	private SalAddress mAddress;
	
	public LinphoneAddressImpl(String username, String domain,
			String displayName) {
		// TODO Auto-generated constructor stub
	}

	public String getDisplayName() {
		return mAddress.getDisplayName();
	}

	public String getDomain() {
		return mAddress.getDomain();
	}

	public String getUserName() {
		return mAddress.getUserName();
	}

	public void setDisplayName(String name) {
		mAddress.setDisplayName(name);
	}

	public String asString() {
		return mAddress.asString();
	}

	public String asStringUriOnly() {
		return mAddress.asStringUriOnly();
	}

	public void clean() {
		mAddress.clean();
	}

	public String getPort() {
		return mAddress.getPort();
	}

	public int getPortInt() {
		return mAddress.getPortInt();
	}

	public void setDomain(String domain) {
		mAddress.setDomain(domain);
	}

	public void setPort(String port) {
		mAddress.setPort(port);
	}

	public void setPortInt(int port) {
		mAddress.setPortInt(port);
	}

	public void setUserName(String username) {
		mAddress.setUserName(username);
	}

}
