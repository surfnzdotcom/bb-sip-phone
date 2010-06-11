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




import java.io.InputStream;


import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.OfferAnswerHelper;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;

import org.linphone.sal.SalOpBase;

import org.linphone.sal.OfferAnswerHelper.AnswerResult;
import org.linphone.sal.Sal.Reason;

import sip4me.gov.nist.javax.sdp.SdpFactory;
import sip4me.gov.nist.javax.sdp.SessionDescription;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.message.Request;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipClientConnectionListener;
import sip4me.nist.javax.microedition.sip.SipConnection;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipDialog;
import sip4me.nist.javax.microedition.sip.SipException;
import sip4me.nist.javax.microedition.sip.SipServerConnection;


class SalOpImpl extends SalOpBase {
	static Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SalAuthInfo mAutInfo;
	SipClientConnection mSipRegisterCnx;
	SipClientConnection mInviteTransaction;
	SipServerConnection mInviteServerTransaction;
	final SipConnectionNotifier mConnectionNotifier;
	SalMediaDescription mLocalSalMediaDescription;
	SalMediaDescription mFinalSalMediaDescription;
	final SalListener mSalListener;
	SipDialog mDialog;
	
	public SalOpImpl(Sal sal, SipConnectionNotifier aConnectionNotifier, SalListener aSalListener) {
		super(sal);
		mConnectionNotifier = aConnectionNotifier;
		mSalListener = aSalListener;
	}
	public SalOpImpl(Sal sal, SipConnectionNotifier aConnectionNotifier, SalListener aSalListener,SipServerConnection aServerInviteTransaction) {
		this(sal,aConnectionNotifier,aSalListener);
		mInviteServerTransaction = aServerInviteTransaction;
	}
	public void setRegisterSipCnx(SipClientConnection cnx) {
		mSipRegisterCnx=cnx;
	}
	public SipConnection getSipCnx() {
		return mSipRegisterCnx;
	}
	public void authenticate(SalAuthInfo info) throws SalException {
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

	public void call() throws SalException {
		
		try {
			SalAddress lToAddress = SalFactory.instance().createSalAddress(getTo());
			/*if (lToAddress.getPortInt() < 0) {
				lToAddress.setPortInt(5060);
			}
			*/
			mInviteTransaction = (SipClientConnection) SipConnector.open(lToAddress.asString());
			mInviteTransaction.initRequest(Request.INVITE,mConnectionNotifier);
			mInviteTransaction.setHeader(Header.FROM, getFrom());
			mInviteTransaction.setRequestURI("sip:" + getTo());
			mInviteTransaction.setHeader(Header.CONTENT_TYPE, "application/sdp");
			if (getRoute() != null && getRoute().length()>0) {
				mInviteTransaction.setHeader(Header.ROUTE, getRoute());
			}
			
			String lSdp = mLocalSalMediaDescription.toString();
			mInviteTransaction.setHeader(Header.CONTENT_LENGTH, String.valueOf(lSdp.length()));
			mInviteTransaction.openContentOutputStream().write(lSdp.getBytes("US-ASCII"));
			mInviteTransaction.setListener(new SipClientConnectionListener() {
			
				public void notifyResponse(SipClientConnection scc) {
					try {
						scc.receive(0);
						switch (scc.getStatusCode()) {
						case 200:
							//SipClientConnection lAckTransaction  = scc.getDialog().getNewClientConnection(Request.ACK);
							//lAckTransaction.initAck();
							InputStream lSdpInputStream = mInviteTransaction.openContentInputStream();
							byte [] lRawSdp = new byte [lSdpInputStream.available()];
							lSdpInputStream.read(lRawSdp);
							
							SessionDescription lSessionDescription  = SdpFactory.getInstance().createSessionDescription(new String(lRawSdp)) ;
							SalMediaDescription lRemote = SdpUtils.toSalMediaDescription(lSessionDescription);
							mFinalSalMediaDescription = OfferAnswerHelper.computeOutgoing(mLocalSalMediaDescription, lRemote);
							mDialog=mInviteTransaction.getDialog(); 
							mInviteTransaction.initAck();
							mInviteTransaction.send();
							
							mSalListener.onCallAccepted(SalOpImpl.this);
							break;
						case 180:
							mSalListener.onCallRinging(SalOpImpl.this);
							break;
						default:
							if (scc.getStatusCode() > 300) {
								mSalListener.onCallFailure(SalOpImpl.this,scc.getReasonPhrase());
							} else {
								mLog.warn("Unexpected answer ["+scc.getStatusCode()+" "+scc.getRequestURI()+"]");
							}
						}
					} catch (Exception e) {
						mLog.error("cannot handle invite answer", e);
					}
					
				}
			});
			mInviteTransaction.send();
		} catch (Exception e) {
			throw new SalException(e);
		}

	}

	public void callAccept() throws SalException {
		try {
			InputStream lSdpInputStream = mInviteServerTransaction.openContentInputStream();
			byte [] lRawSdp = new byte [lSdpInputStream.available()];
			lSdpInputStream.read(lRawSdp);
			
			SessionDescription lSessionDescription  = SdpFactory.getInstance().createSessionDescription(new String(lRawSdp)) ;
			SalMediaDescription lRemote = SdpUtils.toSalMediaDescription(lSessionDescription);
			
			AnswerResult lAnswerResult = OfferAnswerHelper.computeIncoming(mLocalSalMediaDescription, lRemote);
			if (lAnswerResult.getResult().getNumStreams() == 0) {
				mLog.warn("No codec matching");
				mInviteServerTransaction.initResponse(404);
				mInviteServerTransaction.send();
				mSalListener.onCallFailure(this, "no matching codecs");
			} else {
				mFinalSalMediaDescription = lAnswerResult.getResult();
				mInviteServerTransaction.initResponse(200);
				mInviteServerTransaction.setHeader(Header.CONTENT_TYPE, "application/sdp");
				
				String lSdp = lAnswerResult.getAnswer().toString();
				mInviteServerTransaction.setHeader(Header.CONTENT_LENGTH, String.valueOf(lSdp.length()));
				mInviteServerTransaction.openContentOutputStream().write(lSdp.getBytes("US-ASCII"));
				mInviteServerTransaction.send();
				mDialog = mInviteServerTransaction.getDialog();
				
			}
			
			
			
			
		} catch (Exception e) {
			throw new SalException(e);
		}

	}

	public void callDecline(Reason r, String redirectUri) {
			if (mInviteServerTransaction != null) {
				try {
					mInviteServerTransaction.initResponse(603);
					mInviteServerTransaction.send();
					mSalListener.onCallTerminated(this);
					mInviteServerTransaction=null;
					
					

				} catch (Exception e) {
					mLog.error("cannot cancel call", e);
				} 
			}
		
	}

	public void callSetLocalMediaDescription(SalMediaDescription md) {
		mLocalSalMediaDescription = md;

	}

	public void callTerminate() {
		try {
			if (mDialog != null) {
				SipClientConnection lByeConnection = mDialog.getNewClientConnection(Request.BYE);
				lByeConnection.setListener(new SipClientConnectionListener() {

					public void notifyResponse(SipClientConnection scc) {
						try {
							scc.receive(0);
							mSalListener.onCallTerminated(SalOpImpl.this);

						} catch (Exception e) {
							mLog.error("cannot receive bye answer", e);
						} 
					}});
				lByeConnection.send();

			} else if (mInviteTransaction != null) {
				try {
					SipClientConnection mSipRegisterCnx = mInviteTransaction.initCancel();
					mSipRegisterCnx.setListener(new SipClientConnectionListener() {

						public void notifyResponse(SipClientConnection scc) {
							try {
								scc.receive(0);
								mSalListener.onCallTerminated(SalOpImpl.this);

							} catch (Exception e) {
								mLog.error("cannot receive bye answer", e);
							} 
						}});
					mSipRegisterCnx.send();

				} catch (SipException e) {
					mLog.error("cannot cancel call", e);
				}
			}
		} catch (Exception e) {
			mLog.error("cannot terminate call", e);
		}
	}
	public SalAuthInfo getAuthInfo() {
		return mAutInfo;
	}
	public SalMediaDescription getFinalMediaDescription() {
		return mFinalSalMediaDescription;
	}
	public void callRinging() throws SalException{
		try { 
			if (mInviteServerTransaction != null) {
				mInviteServerTransaction.initResponse(180);
				mInviteServerTransaction.send();
			} else {
				throw new SalException("no in a proper state");
			}
		} catch (Exception e) {
			throw new SalException(e);
		}



	}
}
