package org.linphone.sal;

public interface Sal {
	public void setListener(SalListener listener);
	public void callSetMediaDescription(SalMediaDescription md);
	public void call(SalOp op);
	public void callTerminate(SalOp op);
	public void callAccept(SalOp op);
}
