package org.linphone.jlinphone.media.jsr135;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;

public class RecvStream implements Runnable, PlayerListener {
	private Player mPlayer;
	private Thread mThread;
	private RtpSession mSession;
	private boolean mRunning;
	private int mTs;
	private static Logger sLogger=JOrtpFactory.instance().createLogger("RecvStream");
	
	private InputStream mInput= new InputStream(){
		public int available() throws IOException {
			return 1;
		}

		public void close() throws IOException {
		}

		public void mark(int readlimit) {
		}

		public boolean markSupported() {
			return false;
		}

		public int read(byte[] b, int offset, int length) throws IOException {
			sLogger.warn("Called in read(data, offset, length) !",null);
			return read(b);
		}

		public int read(byte[] b) throws IOException {
			RtpPacket packet=mSession.recvPacket(mTs);
			mTs+=160;
			if (packet!=null){
				//+1 because we need to skip the CMR bytes
				int datalen=packet.getRealLength()-packet.getDataOffset()-1;
				System.arraycopy(packet.getBytes(),packet.getDataOffset()+1, b, 0, datalen );
				return datalen;
			}
			
			return 0;
		}

		public int read() throws IOException {
			return 0;
		}
		
	};

	public RecvStream(RtpSession session) {
		mThread=new Thread(this,"RecvStream thread");
		mSession=session;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		
		try {
			mPlayer = Manager.createPlayer(mInput, "audio/amr");
		
			mPlayer.addPlayerListener(this);
			mPlayer.realize();
			mPlayer.prefetch();
			
	/*
			AudioPathControl  lPathCtr = (AudioPathControl) mPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
			lPathCtr.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSET);
	*/	
			//((VolumeControl)mPlayer.getControl("VolumeControl")).setLevel(10);
			mPlayer.start();
	
			while (mRunning) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	
			mPlayer.stop();
	
			mPlayer.close();
		
		} catch (IOException e) {
			sLogger.error("IOException",e);
		} catch (MediaException e) {
			sLogger.error("MediaException",e);
		}

	}

	public void playerUpdate(Player arg0, String event, Object eventData) {
		if (sLogger.isLevelEnabled(sLogger.Info))
				sLogger.info("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]");
	}

}
