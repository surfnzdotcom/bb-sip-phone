package org.linphone.sal;

public interface SalListener {
	public void onCallReceived(SalOp op);
	public void onCallRinging(SalOp op);
	public void onCallAccepted(SalOp op);
	public void onCallTerminated(SalOp op);
}
