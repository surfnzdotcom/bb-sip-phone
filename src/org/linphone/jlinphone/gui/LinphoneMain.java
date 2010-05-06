package org.linphone.jlinphone.gui;


import net.rim.device.api.ui.UiApplication;

public class LinphoneMain extends UiApplication{
	 /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
    	LinphoneMain theApp = new LinphoneMain();       
    	theApp.enterEventDispatcher();
    }
    

    /**
     * Creates a new HelloWorldDemo object
     */
    public LinphoneMain()
    {        
        // Push a screen onto the UI stack for rendering.
        pushScreen(new LinphoneScreen());
    }    

}
