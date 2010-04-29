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

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalException;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;


import sip4me.gov.nist.core.Debug;
import sip4me.gov.nist.core.LogWriter;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.microedition.sip.StackConnector;
import sip4me.gov.nist.siplite.stack.ServerLog;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipServerConnectionListener;

public class SalImpl implements Sal, SipServerConnectionListener {
	private SipConnectionNotifier mConnectionNotifier;
	Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	
	SalImpl() {
	
	}
	public void authenticate(SalOp op, SalAuthInfo info) {
		// TODO Auto-generated method stub

	}

	public void call(SalOp op) {
		// TODO Auto-generated method stub

	}

	public void callAccept(SalOp op) {
		// TODO Auto-generated method stub

	}

	public void callDecline(SalOp op, Reason r, String redirectUri) {
		// TODO Auto-generated method stub

	}

	public void callSetLocalMediaDescription(SalOp op, SalMediaDescription md) {
		// TODO Auto-generated method stub

	}

	public void callTerminate(SalOp op) {
		// TODO Auto-generated method stub

	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public SalMediaDescription getFinalMediaDescription(SalOp h) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalAddr() throws SalException{
		SocketConnection dummyCon=null;
		try {
			if (mConnectionNotifier != null) {
				return mConnectionNotifier.getLocalAddress();
			} else {

				String dummyConnString = "socket://linphone.org:80";
				String localAdd= null;
				dummyCon = (SocketConnection) Connector.open(dummyConnString);
				localAdd = dummyCon.getLocalAddress();

				return localAdd;
			}

		} catch (IOException e) {
			throw new SalException("Cannot get Local address from notification listener reason ["+e.getMessage()+"]");
		} finally {
			if (dummyCon != null)
				try {
					dummyCon.close();
				} catch (IOException e) {
					//nop
				}
		}
	}

	public void listenPort(SocketAddress addr, Transport t, boolean isSecure) throws SalException {
		// Configure logging of the stack
        try {
        	
		Debug.enableDebug(false);
		LogWriter.needsLogging = true;
		LogWriter.setTraceLevel(LogWriter.TRACE_DEBUG);
		ServerLog.setTraceLevel(ServerLog.TRACE_NONE);
		
        StackConnector.properties.setProperty ("javax.sip.RETRANSMISSION_FILTER", "on");
        		
		StackConnector.properties.setProperty ("javax.sip.IP_ADDRESS", addr.getHost());  
		mLog.info("Stack initialized with IP: " + addr.getHost());
        String SipConnectorUri = "sip:";
        if (!addr.getHost().equalsIgnoreCase("0.0.0.0")) {
        	SipConnectorUri+=":"+addr.getPort();
        } else {
        	SipConnectorUri+=addr.getPort();
        }
        
        mConnectionNotifier = (SipConnectionNotifier) SipConnector.open(SipConnectorUri);
        mConnectionNotifier.setListener(this);
		System.out.println("SipConnectionNotifier opened at: "
				+ mConnectionNotifier.getLocalAddress() + ":"
				+ mConnectionNotifier.getLocalPort());
		
        } catch (Exception e) {
        	throw new SalException("Cannot listen port for ["+addr+"] reason ["+e.getMessage()+"]",e);
        }
	}

	public void register(SalOp op, String proxy, String from, int expires) {
		
		// TODO Auto-generated method stub

	}

	public void setListener(SalListener listener) {
		// TODO Auto-generated method stub

	}

	public void setUserAgent(String ua) {
		// TODO Auto-generated method stub

	}

	public void unregister(SalOp op) {
		// TODO Auto-generated method stub

	}
	public void notifyRequest(SipConnectionNotifier ssc) {
		// TODO Auto-generated method stub
		
	}
	public SalOp createSalOp() {
		return new SalOpImpl(this);
	}

}
