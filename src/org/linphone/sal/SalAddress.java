package org.linphone.sal;

public interface SalAddress {
	public String getDisplayName();
	public String getUserName();
	public String getDomain();
	public String getPort();
	public int getPortInt();
	
	public void setDisplayName(String displayName);
	public void setUserName(String username);
	public void setDomain(String domain);
	public void setPort(String port);
	public void setPortInt(int port);
	
	/**
	 * Removes uri parameters.
	 */
	public void clean();
	
	public String asString();
	public String asStringUriOnly();
}
