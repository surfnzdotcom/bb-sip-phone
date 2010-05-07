package org.linphone.jlinphone.media.jsr135;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.RecordControl;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;

public class SendStream implements Runnable, PlayerListener{
	private static Logger sLogger=JOrtpFactory.instance().createLogger("SendStream");
	private Thread mThread;
	private RtpSession mSession;
	private boolean mRunning;
	private Player mPlayer;
	private int mTs;

	private OutputStream mOutput= new OutputStream(){

		public void close() throws IOException {
		}

		public void flush() throws IOException {
		}

		public void write(byte[] buffer, int offset, int count)
				throws IOException {
			sLogger.warn("Called in write(buffer,offset,count)",null);
			write(buffer);
		}

		public void write(byte[] buffer) throws IOException {
			RtpPacket packet=JOrtpFactory.instance().createRtpPacket(buffer.length+1);
			System.arraycopy(buffer, 0, packet.getBytes(), packet.getDataOffset()+1, buffer.length);
			//set cmr byte
			packet.getBytes()[packet.getDataOffset()]=(byte)0xf0;
			try {
				mSession.sendPacket(packet, mTs);
			} catch (RtpException e) {
				sLogger.error("Fail to send RTP packet",e);
			}
			mTs+=160;
		}

		public void write(int arg0) throws IOException {
			
		}
		
	};

	public SendStream(RtpSession session) {
		mThread=new Thread(this,"SendStream thread");
		mSession=session;
		mRunning=false;
		mTs=0;
	}

	public void start() {
		mRunning=true;
		mThread.start();
	}

	public void stop() {
		mRunning=false;
		try {
			mThread.join();
		} catch (InterruptedException e) {
			
		}
	}

	public void run() {
		try {
			mPlayer = Manager.createPlayer("capture://audio?encoding=audio/amr&updateMethod=time&updateThreshold=20");
		
			mPlayer.addPlayerListener(this);
	
			mPlayer.realize();
	
			RecordControl recordControl = (RecordControl) mPlayer.getControl("RecordControl");
	
			recordControl.setRecordStream(mOutput);
	
			recordControl.startRecord();
	
			mPlayer.start();
	
			while (mRunning) {
				Thread.sleep(250);
			}
	
			recordControl.commit();
	
			mOutput.close();
	
			mPlayer.close();
		
		} catch (IOException e) {
			sLogger.error("IOException in SendStream !",e);
		} catch (MediaException e) {
			sLogger.error("MediaException in SendStream !",e);
		} catch (InterruptedException e) {
			sLogger.error("InterruptedException in SendStream !",e);
		}
		
	}

	public void playerUpdate(Player arg0, String event, Object eventData) {
		if (sLogger.isLevelEnabled(sLogger.Info))
			sLogger.info("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]");
	}

}
