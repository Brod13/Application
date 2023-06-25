package com.example.scoreboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Logger-dbg";
    private static final String CHANNEL_ID = "1";
    private int mMaxChars = 50000;//Default//change this to string..........
    private UUID mDeviceUUID;
    public BluetoothSocket mBTSocket;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;
    private BluetoothDevice mDevice;


    private static Score team_a = new Score((byte) 0);
    private static Score team_b = new Score((byte) 0);
    private int score_2 = 0;


    private ProgressDialog progressDialog;
    TextView mTextView, mTextView1, startText;
    public String strInput, strInput2, x;
    public static boolean start = true;
    int type =0;
    static public boolean wasNotified = false;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(ConnectBoard.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(ConnectBoard.DEVICE_UUID));
        mMaxChars = b.getInt(ConnectBoard.BUFFER_SIZE);

        Button incrementScore_1 = findViewById(R.id.increase_button);
        Button decrementScore_1 = findViewById(R.id.decrease_button);
        Button incrementScore_2 = findViewById(R.id.increase_button_2);
        Button decrementScore_2 = findViewById(R.id.decrease_button_2);

        TextView score_a = findViewById(R.id.scoreA);
        TextView score_b = findViewById(R.id.scoreB);
        TextView team_a_label = findViewById(R.id.teamALabel);
        TextView team_b_label = findViewById(R.id.teamBLabel);
        EditText teamName_a = findViewById(R.id.teamAinput);
        EditText teamName_b = findViewById(R.id.teamBinput);

        Button addTeams = findViewById(R.id.addTeamsBtn);


        addTeams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team_a.teamName = String.valueOf(teamName_a.getText());
                team_a_label.setText(team_a.teamName);

                team_b.teamName = String.valueOf(teamName_b.getText());
                team_b_label.setText(team_b.teamName);

                sendNameToBoard(team_a.teamName, team_b.teamName);
            }
        });


        incrementScore_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                team_a.score++;
                sendScoreToBoard(team_a.score, 1);
                score_a.setText(Integer.toString(team_a.score));
            }
        });

        decrementScore_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(team_a.score == 0) return;
                team_a.score--;
                sendScoreToBoard(team_a.score , 1);
                score_a.setText(Integer.toString(team_a.score));

            }
        });

        incrementScore_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team_b.score++;
                sendScoreToBoard(team_b.score, 2);
                score_b.setText(Integer.toString(team_b.score));

            }
        });

        decrementScore_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(team_b.score == 0) return;
                team_b.score--;
                sendScoreToBoard(team_b.score, 2);
                score_b.setText(Integer.toString(team_b.score));

            }
        });


    }

    private void sendScoreToBoard(int score, int team) {

        byte[] frame = {0,0,0,0};
        frame[0] = (byte) 255;
        frame[1] = (byte) 2;
        frame[2] = (byte) team;
        frame[3] = (byte) score;

        Log.d(TAG, "frame : " + frame);


        try {
            mBTSocket.getOutputStream().write(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNameToBoard(String name_1, String name_2) {

        if(name_1.length() != 4 || name_2.length() != 4) return;

//        int id = 254;
//        try {
//            mBTSocket.getOutputStream().write(id);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        byte[] bytes = name_1.getBytes();
        byte[] bytes2 = name_2.getBytes();

        byte[] msg = new byte[10];
        msg[0] = (byte)254;
        msg[1] = (byte)8;
        msg[2] = bytes[0];
        msg[3] = bytes[1];
        msg[4] = bytes[2];
        msg[5] = bytes[3];
        msg[6] = bytes2[0];
        msg[7] = bytes2[1];
        msg[8] = bytes2[2];
        msg[9] = bytes2[3];

        try {
            mBTSocket.getOutputStream().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class DisConnectBT extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            if(start != true) {
                new DisConnectBT().execute();
            }
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {

        new ConnectBT().execute();
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");

//        String text = "2";
//        try {
//            mBTSocket.getOutputStream().write(text.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Hold on", "Connecting");
        }
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your sensor", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;

            }
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}