package com.MAVLink;

import java.util.Enumeration;
import java.util.Hashtable;

import com.bvcode.ncopter.comms.CommunicationClient;

import android.os.Handler;
import android.util.Log;

public class VerifiedMAVLink {

	private Hashtable<String, byte[]> queuedPackets = new Hashtable<String, byte[]>(); 
	private Handler saveHandler = new Handler();
	private SaveRunnable r = new SaveRunnable();
	
	CommunicationClient ba;
	
	public VerifiedMAVLink(CommunicationClient b) {
		ba = b;
		
	}

	public void put(String valueName, byte[] createMessage) {
		queuedPackets.put(valueName, createMessage);
		
	}

	public void start(int retries) {
		r.retryCount  = retries;
		saveHandler.postDelayed(r, 0);
		
	}

	public void verifyReceipt(String name) {
		// Remove the entry from the queue list
		queuedPackets.remove(name);
		
		// And if there is another packet, send it right away
		if(queuedPackets.size() != 0){
			Enumeration<byte[]> elements = queuedPackets.elements();
			ba.sendBytesToComm( elements.nextElement());
			
		}		
	}
	
	private class SaveRunnable implements Runnable {
		public int retryCount = 0;

		public void run() { 
			// If there is something available, send it over.
			if(queuedPackets.size() != 0){
				Enumeration<byte[]> elements = queuedPackets.elements();
				ba.sendBytesToComm( elements.nextElement());
				Log.d("sd", "sending");
				
				if( retryCount-- > 0){
					// In 500msec, try again. 
					saveHandler.postDelayed(r, 500);
					
				}else{
					queuedPackets.clear();
					Log.d("SaveRunnable", "Giving up with saving PIDs");
					
				}
			}
		}
	}

	public boolean isDone() {
		return queuedPackets.size()==0;
		
	}
}
