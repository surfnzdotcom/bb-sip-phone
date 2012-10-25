package org.linphone.jlinphone.gui;

public final class Custom {

	private Custom() {
		throw new IllegalStateException();
	}
	public static final String APPNAME = "Linphone";
	public static final String APPLINK = "http://www.linphone.org";
	public static final Object COPYRIGHT = "© 2012 Belledonne Communications";
	public static final int EXPIRE = 1800; // freephonie

	public static final int TLS_PORT = 5061;
	public static final int SIP_PORT = 5060;

	public static final String DEFAULT_TRANSPORT = "udp";
	public static final String DEFAULT_DOMAIN = null;
	public static final String DEFAULT_PROXY = null;
	public static final boolean DEFAUL_DEBUG = false;

	public static final boolean DEFAULT_ENABLE_SRTP = false;

	public static final String DEFAULT_PTIME = "20";
	public static final boolean DEFAULT_PLUS_TO_ZERO = false;
}
