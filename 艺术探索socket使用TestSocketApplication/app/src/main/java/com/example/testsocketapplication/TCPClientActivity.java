package com.example.testsocketapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testsocketapplication.service.TcpService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MESSAGE_RECEIVE_NEW_MSG =1;
    private static final int MESSAGE_SOCKET_CONNECTED =2;

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:
                    mMessageTextView.setText(mMessageTextView.getText().toString()+(String)msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMessageTextView = findViewById(R.id.msg_container);
        mSendButton = findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mMessageEditText = findViewById(R.id.msg);
        Intent service = new Intent(this, TcpService.class);
        startService(service);
        new Thread(){
            @Override
            public void run() {
                super.run();
                connetTCPService();
            }
        }.start();
    }

    private void connetTCPService() {
        Socket socket = null;
        while (socket ==null){
            try {
                socket = new Socket("localhost",8688);
                mClientSocket = socket;
                mPrintWriter =new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("connnet tcp service failed, retry ... ");
            }

        }
        //接受服务器端的消息
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            while (!TCPClientActivity.this.isFinishing()){
                String msg = br.readLine();
                if (msg!=null){
                    String time = formatDateTime(System.currentTimeMillis());
                    String showMsg = "servcie "+time+": "+msg+"\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showMsg).sendToTarget();
                }

            }
            System.out.println("quit ... ");
            mPrintWriter.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {

        if (mClientSocket!=null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();

    }

    @Override
    public void onClick(View view) {
        if (view == mSendButton){
            final String msg = mMessageEditText.getText().toString();
            if (!TextUtils.isEmpty(msg)&&mPrintWriter!=null) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPrintWriter.println(msg);
                    }
                }).start();
                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                String showedMsg = "self "+time +":"+msg+"\n";
                mMessageTextView.setText(mMessageTextView.getText().toString()+showedMsg);

            }
        }
    }

    private String formatDateTime(long currentTimeMillis) {

        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(currentTimeMillis));
    }
 /* private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG: {
                    mMessageTextView.setText(mMessageTextView.getText()
                            + (String) msg.obj);
                    break;
                }
                case MESSAGE_SOCKET_CONNECTED: {
                    mSendButton.setEnabled(true);
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMessageTextView = (TextView) findViewById(R.id.msg_container);
        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mMessageEditText = (EditText) findViewById(R.id.msg);
        Intent service = new Intent(this, TcpService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == mSendButton) {
            final String msg = mMessageEditText.getText().toString();
            if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPrintWriter.println(msg);
                    }
                }).start();

                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showedMsg = "self " + time + ":" + msg + "\n";
                mMessageTextView.setText(mMessageTextView.getText() + showedMsg);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket ==null){
            try {
                socket = new Socket("localhost",8688);
                mClientSocket = socket;
                mPrintWriter =new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("connnet tcp service failed, retry ... ");
            }
        }

        //接受服务器端的消息
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            while (!TCPClientActivity.this.isFinishing()){
                String msg = br.readLine();
                if (msg!=null){
                    String time = formatDateTime(System.currentTimeMillis());
                    String showMsg = "servcie "+time+": "+msg+"\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showMsg).sendToTarget();
                }

            }
            System.out.println("quit ... ");
            mPrintWriter.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
}
