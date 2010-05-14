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

public class SendStream implements Runnable, PlayerListener{
	private static Logger sLogger=JOrtpFactory.instance().createLogger("SendStream");
	private Thread mThread;
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

			if (DeviceInfo.isSimulator() == false) { //only start record on real device
				recordControl.startRecord();
				mPlayer.start();
			}
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
		if (sLogger.isLevelEnabled(sLogger.Warn))
			sLogger.warn("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]");
	}

}
