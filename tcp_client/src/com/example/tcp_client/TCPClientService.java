package com.example.tcp_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.Handler;
import android.util.Log;

public class TCPClientService {
	private static final String TAG = "TCPClientService";
	private static final int PACKET_LENGTH = 1024;
	
	public static final int MESSAGE_RECV    = 1;
	public static final int MESSAGE_SEND    = 2;
	public static final int MESSAGE_CONNECT = 3;
	
	public static final int CONNECT_FAIL    = 0;
	public static final int CONNECT_OK      = 1;
	
	public static final int CONNECT_TIMEOUT = 5000; // 5 secs
	
	private Handler 	    mHandler = null;	
	private ConnectedThread mThread  = null;
	
	public TCPClientService(Handler handler)
	{
		mHandler = handler;
	}
	
	public int connect(String strIP, int port)
	{	
		if(mThread != null)
		{
			mThread.close();
			mThread = null;
		}
		
		mThread = new ConnectedThread(strIP, port);
		mThread.start();
		
		return 0;	
	}
	
	public void send(byte[] msg)
	{
		if(mThread != null)
		{
			mThread.send(msg);
		}
	}
	
	// Class: ConnectedThread
	private class ConnectedThread extends Thread {
		private Socket mSocket 			= null;
		private InputStream mInStream 	= null;
		private OutputStream mOutStream = null;
		private String m_strIP;
		private int m_port;
		
		public ConnectedThread(String strIP, int port)
		{
			m_strIP = strIP;
			m_port  = port;
		}
		
		public void run() 
		{
			byte[] buffer = new byte[PACKET_LENGTH];
			int bytes;
			
			try {
				SocketAddress remoteAddr = new InetSocketAddress(m_strIP, m_port);
				mSocket = new Socket();
				mSocket.connect(remoteAddr, CONNECT_TIMEOUT);
				mHandler.obtainMessage(MESSAGE_CONNECT, CONNECT_OK, 
						0, null).sendToTarget();							
			} catch (Exception e) {
				Log.e(TAG, mSocket.toString());
				mHandler.obtainMessage(MESSAGE_CONNECT, CONNECT_FAIL, 
						0, null).sendToTarget();	
				return;
			}

			if(mSocket.isConnected())
			{
				try {
					mInStream 	= mSocket.getInputStream();
					mOutStream 	= mSocket.getOutputStream();
				} catch (Exception e) {
					Log.e(TAG, mInStream.toString());
					Log.e(TAG, mOutStream.toString());
				}
			}
			
			// Forever loop to receive packets
			while(mSocket != null && mSocket.isConnected())
			{
				try {
					bytes = mInStream.read(buffer);
					mHandler.obtainMessage(MESSAGE_RECV, bytes, -1, 
							buffer).sendToTarget();					
				} catch (Exception e) {
					Log.e(TAG, mInStream.toString());
					break;
				}
			}
		}
		
		public void send(byte[] buffer)
		{
			try {
				mOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Send Fail");
			}
		}
		
		public void close()
		{
			try {
				mSocket.close();
				mSocket = null;
			} catch (IOException e) {
				Log.e(TAG, "Socket close failed");
			}
		}
		
	}
}
