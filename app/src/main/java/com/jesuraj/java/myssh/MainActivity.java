package com.jesuraj.java.myssh;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private String host = "";
    private String user = "";
    private String passwd = "";
    private String command1 = "";
    private TextView textView;
    private EditText editText;
    private Button button;
    private static final String TAG = "MainActivity";
    private InitConnect initConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.edittxt);
        button = findViewById(R.id.button);
        if (initConnect != null)
            initConnect = null;
        initConnect = new InitConnect();
        initConnect.start();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                if (initConnect != null) {

                    initConnect.write((editText.getText().toString() + "\r\n").getBytes());

                }
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        button.setEnabled(true);
//                    }
//                }, 5000);


            }
        });
    }

    class InitConnect extends Thread {
        private byte[] tmp;
        private OutputStream out;

        public InitConnect() {
        }

        public void write(byte[] bytes) {
            try {
                if (out != null)
                    out.write(bytes);
            } catch (Exception e) {
                Log.d(TAG, "write: " + e.toString());
            }
        }

        @Override
        public void run() {
            try {

                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                Session session = jsch.getSession(user, host, 22);
                session.setPassword(passwd);
                session.setConfig(config);
                session.connect();
                System.out.println("Connected");
                Log.d(TAG, "Session  connected ");

                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command1);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);
                InputStream in = channel.getInputStream();
                out = channel.getOutputStream();
                channel.connect();
                tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        final int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(new String(tmp, 0, i));
                            }
                        });
                        Log.d(TAG, "data received:" + new String(tmp, 0, i));
                    }
                    if (channel.isClosed()) {

                        Log.d(TAG, "exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                        Log.d(TAG, "run: " + ee.toString());
                    }
                }
                channel.disconnect();
                session.disconnect();
                Log.d(TAG, "DONE");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
