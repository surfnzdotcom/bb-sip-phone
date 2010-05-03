package org.linphone.sal;





public interface SalListener {
	public void onCallReceived(SalOp op);
	public void onCallRinging(SalOp op);
	public void onCallAccepted(SalOp op);
	public void onCallTerminated(SalOp op);
	public void onAuthRequested(SalOp op, String parameter, String userName);
	public void onAuthSuccess(SalOp lSalOp, String realm, String username);
}
