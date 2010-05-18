package org.linphone.sal;



import org.linphone.jortp.SocketAddress;

public interface Sal {
	static public class  Reason{
		public static Reason Declined = new Reason("Declined");
		public static Reason Busy = new Reason("Busy");
		public static Reason Redirect = new Reason("Redirect");
		public static Reason TemporarilyUnavailable = new Reason("TemporarilyUnavailable");
		public static Reason NotFound = new Reason("NotFound");
		public static Reason DoNotDisturb = new Reason("DoNotDisturb");
		public static Reason Media = new Reason("Media");
		public static Reason Forbidden = new Reason("Forbidden");
		public static Reason Unknown = new Reason("Unknown");
		private String mStringValue;
		private Reason(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	}

	static public class Transport{
		public static Transport Datagram = new Transport("Datagram");
		public static Transport Stream = new Transport("Stream");
		private String mStringValue;
		private Transport(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	}

	//global
	public void setListener(SalListener listener);
	public void listenPort(SocketAddress addr, Transport t, boolean isSecure )throws SalException;
	public void setUserAgent(String ua);
	public void authenticate(SalOp op, SalAuthInfo info) throws SalException;
	public SalOp createSalOp();
	
	//Call management
	public void callSetLocalMediaDescription(SalOp op, SalMediaDescription md);
	public void call(SalOp op) throws SalException;
	public void callDecline(SalOp op, Reason r, String redirect_uri);
	public void callTerminate(SalOp op);
	public void callAccept(SalOp op) throws SalException;
	public void callRinging(SalOp op) throws SalException;
	
	SalMediaDescription getFinalMediaDescription(SalOp h);
	
	//Registration
	public void register(SalOp op, String proxy, String from, int expires) throws SalException;
	public void unregister(SalOp op);
	
	//close
	public void close();
	public String getLocalAddr() throws SalException;
}
