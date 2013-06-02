package com.example.tcp_client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TCPClientService mService = null;
	private Button m_btSend;
	private Button m_btConnect;
	private EditText m_edIP;
	private EditText m_edPort;
	private EditText m_edSend;
	private TextView m_tvRecv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_edIP      = (EditText) findViewById(R.id.ed_IP);
		m_edPort    = (EditText) findViewById(R.id.ed_Port);
		m_edSend    = (EditText) findViewById(R.id.ed_Send);
		m_btConnect = (Button) findViewById(R.id.bt_Connect); 
		m_btSend    = (Button) findViewById(R.id.bt_Send);
		m_tvRecv    = (TextView) findViewById(R.id.tv_Recv);
		m_btSend.setEnabled(false);
		
		m_edIP.setText("192.168.99.1");
		m_edPort.setText("1234");
		m_edSend.setText("abcd");
		
		m_btConnect.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				String strIP   = m_edIP.getText().toString();
				String strPort = m_edPort.getText().toString();
				int nPort = 0;
				
				try {
					nPort = Integer.parseInt(strPort);
				} catch (NumberFormatException e) {
					Toast.makeText(getApplicationContext(), "Connect Fail", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(mService == null)
				{
					mService = new TCPClientService(mHandler);
				}
				mService.connect(strIP, nPort);
			}
		});
		
		m_btSend.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				String strSend = m_edSend.getText().toString();
				byte[] msg = strSend.getBytes(); 
				mService.send(msg);
			}
		});		
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{	
			switch (msg.what){
			case TCPClientService.MESSAGE_CONNECT:
				if(msg.arg1 == TCPClientService.CONNECT_FAIL){
					Toast.makeText(getApplicationContext(), "Connect Fail", 
							Toast.LENGTH_SHORT).show();
					m_btSend.setEnabled(false);
				}
				else{
					Toast.makeText(getApplicationContext(), "Connect OK", 
							Toast.LENGTH_SHORT).show();
					m_btSend.setEnabled(true);
				}
				break;
				
			case TCPClientService.MESSAGE_RECV:
                byte[] readBuf = (byte[]) msg.obj;

                String readMessage = new String(readBuf, 0, msg.arg1);
        		m_tvRecv.setText(readMessage);        
				break;
			}
		}
	};
	
}
