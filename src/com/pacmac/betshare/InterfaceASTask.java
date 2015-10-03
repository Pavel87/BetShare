package com.pacmac.betshare;

public interface InterfaceASTask {

	public void updateMainView(String mEmail, String mName, String mGroupid);
	
	public void showDialog(Boolean mSwitch);
	
	public void registerDialog(Boolean mSwitch, String text);
	
	public void finishRegistration();
	
	public void showDialogNoConn();

}
