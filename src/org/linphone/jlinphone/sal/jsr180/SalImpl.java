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
import sip4me.gov.nist.siplite.stack.ServerLog;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipServerConnection;
import sip4me.nist.javax.microedition.sip.SipServerConnectionListener;

class SalImpl implements Sal, SipServerConnectionListener {
	private SipConnectionNotifier mConnectionNotifier;
	Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SipClientConnection mRegisterCnx;
	int mRegisterRefreshID;
	private SalListener mSalListener;
	private SalOp mIncallOp;
	SalImpl() {
	
	}
	public void authenticate(SalOp op, SalAuthInfo info) throws SalException {
		((SalOpImpl)op).authenticate(info);
	}

	public void call(SalOp op) throws SalException {
		mIncallOp = op;
		((SalOpImpl)op).call();
	}

	public void callAccept(SalOp op) throws SalException {
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
		mIncallOp=null;
	}
	
	public void callRinging(SalOp op) throws SalException {
		((SalOpImpl)op).callRinging();
		
	}
	public void register(SalOp op, String proxy, String from, int expires) throws SalException{
		((SalOpImpl)op).register(proxy, from, expires);
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
		SipServerConnection lCnx=null;
		try {
			lCnx = ssc.acceptAndOpen();
			mLog.info("receiving request: "+lCnx.getMethod()+" " +lCnx.getRequestURI());
			if ("INVITE".equals(lCnx.getMethod())) {
				SalOp lOp = new SalOpImpl(this,mConnectionNotifier,mSalListener,lCnx);
				SalAddress lFrom = SalFactory.instance().createSalAddress(lCnx.getHeader(Header.FROM));
				SalAddress lTo = SalFactory.instance().createSalAddress(lCnx.getHeader(Header.TO));
				lOp.setFrom(lFrom.asStringUriOnly());
				lOp.setTo(lTo.asStringUriOnly());
				lCnx.initResponse(100);
				lCnx.send();
				mSalListener.onCallReceived(lOp);
				
			} else if ("BYE".equals(lCnx.getMethod())) {
				mSalListener.onCallTerminated(mIncallOp);
				lCnx.initResponse(200);
				lCnx.send();
				mIncallOp=null;
			} else {
				lCnx.initResponse(500);
				lCnx.send();
			}
		} catch (Exception e) {
			if (lCnx !=null) {
				mLog.error("Cannot answer to : "+lCnx.getMethod()+" " +lCnx.getRequestURI(), e);
			} else {
				mLog.error("Unknown error while processing Request",e);
			}
		}
		
		
	}
	public SalOp createSalOp() {
		return new SalOpImpl(this,mConnectionNotifier,mSalListener);
	}



}
