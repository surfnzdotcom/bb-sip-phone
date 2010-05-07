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


import java.io.IOException;
import java.io.InterruptedIOException;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;


import sip4me.gov.nist.core.Debug;
import sip4me.gov.nist.core.LogWriter;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.microedition.sip.StackConnector;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.message.Request;
import sip4me.gov.nist.siplite.stack.ServerLog;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipClientConnectionListener;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipException;
import sip4me.nist.javax.microedition.sip.SipHeader;
import sip4me.nist.javax.microedition.sip.SipRefreshListener;
import sip4me.nist.javax.microedition.sip.SipServerConnection;
import sip4me.nist.javax.microedition.sip.SipServerConnectionListener;

class SalImpl implements Sal, SipServerConnectionListener,SipRefreshListener {
	private SipConnectionNotifier mConnectionNotifier;
	Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SipClientConnection mRegisterCnx;
	int mRegisterRefreshID;
	private SalListener mSalListener;
	SalImpl() {
	
	}
	public void authenticate(SalOp op, SalAuthInfo info) throws SalException {
		((SalOpImpl)op).authenticate(info);
	}

	public void call(SalOp op) throws SalException {
		((SalOpImpl)op).call();
	}

	public void callAccept(SalOp op) {
		((SalOpImpl)op).callAccept();
	}

	public void callDecline(SalOp op, Reason r, String redirectUri) {
		((SalOpImpl)op).callDecline(r, redirectUri);
	}

	public void callSetLocalMediaDescription(SalOp op, SalMediaDescription md) {
		((SalOpImpl)op).callSetLocalMediaDescription(md);
	}

	public void callTerminate(SalOp op) {
		((SalOpImpl)op).callTerminate();
	}

	public void close() {
		if (mConnectionNotifier != null) {
			try {
				mConnectionNotifier.close();
			} catch (IOException e) {
				mLog.error("cannot close Sal connection", e);
			}
			mConnectionNotifier = null;
		}

	}

	public SalMediaDescription getFinalMediaDescription(SalOp h) {
		return ((SalOpImpl)h).getFinalMediaDescription();
	}

	public String getLocalAddr() throws SalException{
		try {
			if (mConnectionNotifier != null) {
				return mConnectionNotifier.getLocalAddress();
			} else {
				throw new Exception("no notification listener");
			}

		} catch (Exception e) {
			throw new SalException("Cannot get Local address from notification listener reason ["+e.getMessage()+"]");
		}
	}

	public void listenPort(SocketAddress addr, Transport t, boolean isSecure) throws SalException {
		// Configure logging of the stack
        try {
        
		Debug.enableDebug(false);
		LogWriter.needsLogging = true;
		LogWriter.setTraceLevel(LogWriter.TRACE_EXCEPTION);
		ServerLog.setTraceLevel(ServerLog.TRACE_NONE);
		
        StackConnector.properties.setProperty ("javax.sip.RETRANSMISSION_FILTER", "on");
        StackConnector.properties.setProperty("sip4me.gov.nist.javax.sip.NETWORK_LAYER", "sip4me.gov.nist.core.net.BBNetworkLayer");		
		StackConnector.properties.setProperty ("javax.sip.IP_ADDRESS", addr.getHost());  
		mLog.info("Stack initialized with IP: " + addr.getHost());
        String SipConnectorUri = "sip:";
//        if (addr.getHost().equalsIgnoreCase("0.0.0.0")) {
        	SipConnectorUri+=addr.getPort();
//        } else {
//        	SipConnectorUri+="anonymous@"+addr.getHost()+":"+addr.getPort();
//        }
        
        mConnectionNotifier = (SipConnectionNotifier) SipConnector.open(SipConnectorUri);
        mConnectionNotifier.setListener(this);
		System.out.println("SipConnectionNotifier opened at: "
				+ mConnectionNotifier.getLocalAddress() + ":"
				+ mConnectionNotifier.getLocalPort());
		
        } catch (Exception e) {
        	throw new SalException("Cannot listen port for ["+addr+"] reason ["+e.getMessage()+"]",e);
        }
	}

	public void register(SalOp op, String proxy, String from, int expires) throws SalException{
		// save from
		op.setFrom(from);

		final SalOpImpl lSalOp = (SalOpImpl) op;
		try {
			
			mRegisterCnx = (SipClientConnection) SipConnector.open(from);
			lSalOp.setRegisterSipCnx(mRegisterCnx);
			mRegisterCnx.initRequest(Request.REGISTER, mConnectionNotifier);
			mRegisterCnx.setHeader(Header.FROM, from);
			mRegisterCnx.setHeader(Header.EXPIRES, String.valueOf(expires));

			final SalAddress lAddress = SalFactory.instance().createSalAddress(from);
			String contactHdr = lAddress.getUserName() 	+ "@"
														+ mConnectionNotifier.getLocalAddress() + ":"
														+ mConnectionNotifier.getLocalPort();
			mRegisterCnx.setHeader(Header.CONTACT, contactHdr);

			mRegisterCnx.setRequestURI("sip:" + lAddress.getDomain());
			mRegisterCnx.setListener(new SipClientConnectionListener() {

				public void notifyResponse(SipClientConnection scc) {
					// positione credential
					try {
						scc.receive(0);
						switch (scc.getStatusCode()) {
						case 407:
							SipHeader lAuthHeader = new SipHeader(Header.AUTHORIZATION,scc.getHeader(Header.PROXY_AUTHENTICATE));
							/*
							String lStringAuthHeader = lAuthHeader.getValue(); 
							String lRealm=null;
							String lDigestToken = "Digest realm=\"";
							int disgestIndex = lStringAuthHeader.indexOf(lDigestToken);
							if (disgestIndex < 0) {
								mLog.error("cannot find Digest realm");
							} else {
								lRealm = lStringAuthHeader.substring(disgestIndex+lDigestToken.length()+1, lStringAuthHeader.indexOf("=\"", disgestIndex+lDigestToken.length()+1));
								 if (lRealm == null) {
									mLog.error("cannot find Digest ream");
								}
							}
							*/
							
							mSalListener.onAuthRequested(lSalOp,lAuthHeader.getParameter("realm"),lAddress.getUserName());
							break;
							
						case 200:
							mSalListener.onAuthSuccess(lSalOp,lSalOp.getAuthInfo().getRealm(),lSalOp.getAuthInfo().getUsername());
							break;
						default: 
							mLog.error("Unexpected answer ["+scc+"]");
						}
					} catch (Exception e) {
						mLog.error("Cannot process REGISTER answer", e);
					} 
					
				}
				
			});

			 
			mRegisterCnx.enableRefresh(this);

			// Finally, send register
			mRegisterCnx.send();
			mLog.info("REGISTER sent from ["+lAddress+"] to ["+proxy+"]");
		} catch (Exception e) {
			throw new SalException("cannot send register  from ["+from+"] to ["+proxy+"]",e);
		}


	}

	public void setListener(SalListener listener) {
		mSalListener = listener;
	}

	public void setUserAgent(String ua) {
		// TODO Auto-generated method stub

	}

	public void unregister(SalOp op) {
		// TODO Auto-generated method stub

	}
	public void notifyRequest(SipConnectionNotifier ssc) {
		try {
			SipServerConnection lCnx = ssc.acceptAndOpen();
			mLog.debug("receiving request: " +lCnx.getRequestURI());
			lCnx.initResponse(500);
			lCnx.send();
		} catch (Exception e) {
		}
		
		
	}
	public SalOp createSalOp() {
		return new SalOpImpl(this,mConnectionNotifier,mSalListener);
	}
	public void refreshEvent(int refreshID, int statusCode, String reasonPhrase) {
		// TODO Auto-generated method stub
		
	}


}
