package org.linphone.sal;

public class SalAuthInfo {
	private String mUsername;
	private String mPassword;
	private String mRealm;
	private String mUserid;
	
	public SalAuthInfo(String username, String password){
		mUsername=username;
		mPassword=password;
		mUserid=username;
	}
	public SalAuthInfo(String realm, String username, String password){
		mRealm=realm;
		mUsername=username;
		mPassword=password;
		mUserid=username;
	}
	public SalAuthInfo(){
		
	}
	public void setUsername(String username){
		mUsername=username;
	}
	public void setPassword(String passwd){
		mPassword=passwd;
	}
	public void setRealm(String realm){
		mRealm=realm;
	}
	public void setUserid(String userid){
		mUserid=userid;
	}
	public String getRealm(){
		return mRealm;
	}
	public String getPassword(){
		return mPassword;
	}
	public String getUsername(){
		return mUsername;
	}
	public String getUserid(){
		return mUserid;
	}
}
