/*
SendStream.java
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

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.system.DeviceInfo;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;

public class SendStream implements PlayerListener{
	private static Logger sLogger=JOrtpFactory.instance().createLogger("SendStream");
	
	private RtpSession mSession;
	private boolean mRunning;
	private Player mPlayer;
	private int mTs=0;
	private int mFrameSize=32;
	private OutputStream mOutput= new OutputStream(){

		public void close() throws IOException {
		}

		public void flush() throws IOException {
		}

		public void write(byte[] buffer, int offset, int count)
		throws IOException {
			sLogger.info("Called in write date ["+System.currentTimeMillis()+"] offset ["+offset+"] length ["+count+"] ts ["+mTs+"]");
			int lOffset=offset;
			if (mTs == 0) {
				lOffset += "#!AMR\n".length();
			}
			while (lOffset + mFrameSize <= offset+count) {
				RtpPacket packet=JOrtpFactory.instance().createRtpPacket(mFrameSize+1);
				System.arraycopy(buffer, lOffset, packet.getBytes(), packet.getDataOffset()+1, mFrameSize);
				//set cmr byte
				packet.getBytes()[packet.getDataOffset()]=(byte)0xf0;
				try {
					mSession.sendPacket(packet, mTs);
				} catch (RtpException e) {
					sLogger.error("Fail to send RTP packet",e);
				}
				mTs+=160;
				lOffset+=mFrameSize;
			}
		}

		public void write(byte[] buffer) throws IOException {
			write( buffer, 0, buffer.length);
			}

		public void write(int arg0) throws IOException {
			sLogger.error("write(int arg0) not implemented");
		}
		
	};

	public SendStream(RtpSession session) {
		
		mSession=session;
		mRunning=false;
		mTs=0;
	}


	public void stop() {
		try {
			RecordControl recordControl = (RecordControl) mPlayer.getControl("RecordControl");
			recordControl.commit();
			mOutput.close();
			mPlayer.close();
		}  catch (Exception e) {
			sLogger.error("InterruptedException in SendStream !",e);
		} 

	}

	public void start() {
		try {
			mPlayer = Manager.createPlayer("capture://audio?encoding=audio/amr&updateMethod=time&updateThreshold=20");
			mPlayer.addPlayerListener(this);
			mPlayer.realize();
			RecordControl recordControl = (RecordControl) mPlayer.getControl("RecordControl");
			recordControl.setRecordStream(mOutput);

			if (DeviceInfo.isSimulator() == false) { //only start record on real device
				recordControl.startRecord();
				mPlayer.start();
			}

		} catch (IOException e) {
			sLogger.error("IOException in SendStream !",e);
		} catch (MediaException e) {
			sLogger.error("MediaException in SendStream !",e);
		}

	}

	public void playerUpdate(Player arg0, String event, Object eventData) {
		if (sLogger.isLevelEnabled(sLogger.Warn))
			sLogger.warn("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]");
	}

}
