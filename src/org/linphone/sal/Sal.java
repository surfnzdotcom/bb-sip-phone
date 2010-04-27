package org.linphone.sal;

import org.linphone.jortp.SocketAddress;

public interface Sal {
	enum Reason{
		Declined,
		Busy,
		Redirect,
		TemporarilyUnavailable,
		NotFound,
		DoNotDisturb,
		Media,
		Forbidden,
		Unknown
	}
	enum Transport{
		Datagram,
		Stream
	}
	//global
	public void setListener(SalListener listener);
	public void listenPort(SocketAddress addr, Transport t, boolean isSecure );
	public void setUserAgent(String ua);
	public void authenticate(SalOp op, SalAuthInfo info);
	
	//Call management
	public void callSetLocalMediaDescription(SalMediaDescription md);
	public void call(SalOp op);
	public void callDecline(SalOp op, Reason r, String redirect_uri);
	public void callTerminate(SalOp op);
	public void callAccept(SalOp op);
	SalMediaDescription getFinalMediaDescription(SalOp h);
	
	//Registration
	public void register(SalOp op, String proxy, String from, int expires);
	public void unregister(SalOp op);
	
	//close
	public void close();
}
