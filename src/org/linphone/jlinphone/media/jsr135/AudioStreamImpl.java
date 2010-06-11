/*
AudioStreamImpl.java
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
package org.linphone.jlinphone.media.jsr135;

import org.linphone.jlinphone.media.AudioStream;
import org.linphone.jlinphone.media.AudioStreamParameters;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.SocketAddress;

public class AudioStreamImpl implements AudioStream {
	private RecvStream mRecvStream;
	private SendStream mSendStream;
	static Logger mLog = JOrtpFactory.instance().createLogger("AudioStream");
	private RtpSession mSession;
	
	public void stop() {
		if (mRecvStream!=null)
			mRecvStream.stop();
		if (mSendStream!=null)
			mSendStream.stop();
		mSession.close();
		mLog.warn("received stats :"+mSession.getRecvStats().toString());
		mLog.warn("send stats :"+mSession.getSendStats().toString());
	}
	public void init(SocketAddress local) throws RtpException {
		mSession=JOrtpFactory.instance().createRtpSession();
		mSession.setLocalAddr(local);
	}
	public void start(AudioStreamParameters params) throws RtpException {
		
		mSession.setRemoteAddr(params.getRemoteDest());
		mSession.setProfile(params.getRtpProfile());
		mSession.setSendPayloadTypeNumber(params.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(params.getActivePayloadTypeNumber());
		
		mRecvStream=new RecvStream(mSession);
		mSendStream=new SendStream(mSession);
		mSendStream.start();
		mRecvStream.start();
		
	}
	
}
