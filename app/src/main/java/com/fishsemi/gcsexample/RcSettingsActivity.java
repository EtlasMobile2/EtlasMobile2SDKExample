package com.fishsemi.gcsexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fishsemi.sdk.rcservice.RcListener;
import com.fishsemi.sdk.rcservice.RcService;

import java.util.ArrayList;

public class RcSettingsActivity extends Activity {
    private static final String TAG = "RcSettingsActivity";
    private RcService mRcService;
    RadioGroup mRadioGroup;
    private LinearLayout mSbus1Channels;
    private LinearLayout mSbus2Channels;
    private ArrayList<SeekBar> mChannelSeekBars = new ArrayList<>();
    private ArrayList<TextView> mChannelValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_settings);
        mRadioGroup = findViewById(R.id.mode_radio_group);
        mSbus1Channels = findViewById(R.id.sbus1_channels);
        mSbus2Channels = findViewById(R.id.sbus2_channels);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                int mode;
                if (id == R.id.jp_hand_radio) {
                    mode = RcService.JOYSTICK_MODE_JAPANESE_HAND;
                } else {
                    mode = RcService.JOYSTICK_MODE_AMERICAN_HAND;
                }
                if (!mRcService.setJoystickMode(mode)) {
                    updateJoystickMode();
                }
            }
        });
        for (int i = 1; i <= 32; i++) {
            LinearLayout channelView = (LinearLayout) View.inflate(this, R.layout.channel_value, null);
            SeekBar seekBar = channelView.findViewById(R.id.value_bar);
            seekBar.setMax(2200-800);
            seekBar.setEnabled(false);
            TextView title = channelView.findViewById(R.id.title);
            title.setText("CH"+ ((i <= 16) ? i : i - 16));
            TextView value = channelView.findViewById(R.id.value);
            value.setText("n/a");
            mChannelSeekBars.add(seekBar);
            mChannelValues.add(value);
            if (i <= 16) {
                mSbus1Channels.addView(channelView);
            } else {
                mSbus2Channels.addView(channelView);
            }

        }

        mRcService = new RcService(this, new RcListener() {
            @Override
            public void onRcServiceReady(boolean ready) {
                if (ready) {
                    updateJoystickMode();
                }
            }

            @Override
            public void onSbusChannelValueChanged(int sbusId, int channelId, int value) {
                int index = (sbusId-1) * 16 + channelId-1;
                Log.e("TAG","sbusId:  " + sbusId + "channelId:  " + sbusId + "value:  " + value);
                SeekBar seekBar = mChannelSeekBars.get(index);
                if (seekBar != null) {
                    seekBar.setEnabled(true);
                    int progress = value-800;
                    seekBar.setProgress(progress);
                }
                TextView valueView = mChannelValues.get(index);
                if (valueView != null) {
                    valueView.setText(String.valueOf(value));
                }
            }
        });
        mRcService.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRcService.stop();
    }

    private void updateJoystickMode() {
        int mode = mRcService.getJoystickMode();
        if (mode == RcService.JOYSTICK_MODE_JAPANESE_HAND) {
            mRadioGroup.check(R.id.jp_hand_radio);
        } else {
            mRadioGroup.check(R.id.am_hand_radio);
        }
    }
}
