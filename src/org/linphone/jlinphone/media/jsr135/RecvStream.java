/*
RecvStream.java
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
import java.io.InputStream;

import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.protocol.ContentDescriptor;
import javax.microedition.media.protocol.DataSource;
import javax.microedition.media.protocol.SourceStream;

import net.rim.device.api.media.control.AudioPathControl;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;

public class RecvStream implements /*Runnable,*/ PlayerListener {
	private Player mPlayer;
	
	private RtpSession mSession;
	private boolean mRunning;
	private int mTs;
	private long mStartTime=0;
	private static Logger sLogger=JOrtpFactory.instance().createLogger("RecvStream");
	
	
	private SourceStream mInput= new SourceStream(){
		 byte [] sSilentAmr= {  (byte)0x3c, (byte)0x48, (byte)0xf5, (byte)0x1f,
			        (byte)0x96, (byte)0x66, (byte)0x79, (byte)0xe1,
			        (byte)0xe0, (byte)0x01, (byte)0xe7, (byte)0x8a,
			        (byte)0xf0, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

		private RtpPacket mTroncatedPacket;
		private int mTroncatedPacketSize;

		 ContentDescriptor mContentDescriptor=new ContentDescriptor("audio/amr");
		 /* (non-Javadoc)
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		public int read(byte[] b, int offset, int length) throws IOException {
			try {
				if (sLogger.isLevelEnabled(Logger.Info))sLogger.info("Called in read date ["+System.currentTimeMillis()+"] offset ["+offset+"]length ["+length+"] ts ["+mTs+"]");

				int lWrittenLenth=0;
				if (mTs==0) {
					mStartTime = System.currentTimeMillis();
					String lAmrHeader="#!AMR\n";
					lWrittenLenth=lAmrHeader.length();
					System.arraycopy(lAmrHeader.getBytes("US-ASCII"),0, b, offset, lWrittenLenth );
					length = length -lWrittenLenth;
					offset+=lWrittenLenth;

				} else {
					long nextTick;
					while (( nextTick =  (System.currentTimeMillis() - mStartTime-(mTs/8))) < 20) {
						long sleepTime = Math.max(20 - nextTick,10); //sleep accuracy
						if (sLogger.isLevelEnabled(Logger.Debug)) sLogger.debug("blocking reader for ["+sleepTime+"] ms");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							sLogger.info("blocked reader interrupted",e);
						}
					}
				}
				
				RtpPacket packet=null;
				try {
					int lNumberOfPacketConsumed=0;
					if ((length < sSilentAmr.length) && ((packet=mSession.recvPacket(mTs))!=null))  {
						// special case for end of buffer
						
						System.arraycopy(packet.getBytes(),packet.getDataOffset()+1, b, offset, length );
						lWrittenLenth+= length;
						mTroncatedPacketSize=length;
						mTroncatedPacket = packet;
						lNumberOfPacketConsumed++;
						return mTroncatedPacketSize;
					} 
					if (mTroncatedPacket != null) {
						// special case for troncated packet
						int datalen=mTroncatedPacket.getRealLength()-mTroncatedPacket.getDataOffset()-1;
						System.arraycopy(mTroncatedPacket.getBytes(),mTroncatedPacket.getDataOffset()+1+mTroncatedPacketSize, b, offset, datalen- mTroncatedPacketSize);
						lWrittenLenth+= datalen- mTroncatedPacketSize;
						mTroncatedPacketSize=0;
						mTroncatedPacket = null;
						lNumberOfPacketConsumed++;
						offset+= datalen - mTroncatedPacketSize;
					}
					while((lWrittenLenth + sSilentAmr.length < length) && (packet=mSession.recvPacket(mTs))!=null) {
						//+1 because we need to skip the CMR bytes
						int datalen=packet.getRealLength()-packet.getDataOffset()-1;
						System.arraycopy(packet.getBytes(),packet.getDataOffset()+1, b, offset, datalen );
						lWrittenLenth+= datalen;
						offset+=datalen;
						lNumberOfPacketConsumed++;
					}
					

					mTs+=160;
					

				} catch (RtpException e) {
					sLogger.error("Bad RTP packet", e);
				}
				if (sLogger.isLevelEnabled(Logger.Debug)) sLogger.debug("["+lWrittenLenth+"] bytes returned"); 
				return lWrittenLenth;
			} catch (Throwable e) {
				sLogger.error("Exiting player input stream",e);
				return 0;
			}
		}



		public ContentDescriptor getContentDescriptor() {
			return mContentDescriptor;
		}

		public long getContentLength() {
			return -1;
		}

		public int getSeekType() {
			return SourceStream.NOT_SEEKABLE;
		}

		public int getTransferSize() {
			return sSilentAmr.length;
		}

		public long seek(long where) throws IOException {
			throw new IOException("not seekable");
		}

		public long tell() {
			return mTs;
		}

		public Control getControl(String controlType) {
			return null;
		}

		public Control[] getControls() {
			return null;
		}
		
	};

	public RecvStream(RtpSession session) {
		//mThread=new Thread(this,"RecvStream thread");
		mSession=session;
		mTs=0;
	}


	public void stop() {
		try {
			mPlayer.stop();
			mPlayer.close();
		} catch (MediaException e) {
			sLogger.error("Error stopping reveive stream",e);
		}
	}

	public void start() {
		
		try{
			mPlayer = Manager.createPlayer(new DataSource (null) {
				SourceStream[] mStream = {mInput};
				public void connect() throws IOException {
					sLogger.info("connect data source");
					
				}

				public void disconnect() {
					sLogger.info("disconnect data source");
					
				}

				public String getContentType() {
					return "audio/amr";
				}

				public SourceStream[] getStreams() {
					return mStream;
				}

				public void start() throws IOException {
					sLogger.info("start data source");
					
				}

				public void stop() throws IOException {
					sLogger.info("start data source");
					
				}

				public Control getControl(String controlType) {
					return null;
				}

				public Control[] getControls() {
					return null;
				}
				
			});
	
			mPlayer.addPlayerListener(this);
			mPlayer.realize();
			mPlayer.prefetch();
			AudioPathControl  lPathCtr = (AudioPathControl) mPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
			lPathCtr.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSET);
			
			//((VolumeControl)mPlayer.getControl("VolumeControl")).setLevel(50);
			mPlayer.start();
	
		}catch (Exception e){
			sLogger.error("player error:",e);
		}

	}

	public void playerUpdate(Player arg0, String event, Object eventData) {
		if (sLogger.isLevelEnabled(sLogger.Warn))
				sLogger.warn("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]");
	}

}
