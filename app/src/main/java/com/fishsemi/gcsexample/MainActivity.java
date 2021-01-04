package com.fishsemi.gcsexample;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fishsemi.sdk.aircontrol.AirControlListener;
import com.fishsemi.sdk.aircontrol.AirController;
import com.fishsemi.sdk.aircontrol.DataStream;
import com.fishsemi.sdk.aircontrol.DataStreamListener;
import com.fishsemi.sdk.aircontrol.Types;
import com.fishsemi.sdk.aircontrol.VideoCapture;
import com.fishsemi.sdk.aircontrol.VideoCaptureListener;
import com.fishsemi.sdk.aircontrol.VideoStream;
import com.fishsemi.sdk.aircontrol.VideoStreamListener;
import com.fishsemi.sdk.d2dcontrol.D2dControlListener;
import com.fishsemi.sdk.d2dcontrol.D2dController;
import com.fishsemi.sdk.utils.SystemPropertyUtil;

import java.io.File;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    private TextView mVideoStreamIdInfo;
    private TextView mVideoReslutionInfo;
    private TextView mCaptureStateInfo;
    private TextView mCaptureModeInfo;
    private TextView mVideoRecordingInfo;
    private TextView mFreeSpaceInfo;

    private TextView mD2dConnectionInfo;
    private TextView mD2dSignalAirInfo;
    private TextView mD2dSignalControllerInfo;
    private TextView mD2dBandwidthInfo;
    private TextView mD2dSpeedInfo;

    private Button mPairButton;
    private Button mToggleModeButton;
    private Button mPhotoButton;
    private Button mStartVideoButton;
    private Button mStopVideoButton;
    private Button mSetVideoStreamButton;

    private Button mPlayButton;
    private TextureView mTextureView;

    private TextView mPairPrompt;
    private TextView mToggleModePrompt;
    private TextView mPhotoPrompt;
    private TextView mStartVideoPrompt;
    private TextView mStopVideoPrompt;

    private TextView mUart1DataReceivedInfo;
    private Button mSendDataUart1Button;
    private EditText mUart1DataEdit;
    private TextView mSendDataUart1Prompt;
    private TextView mUart1BaudInfo;
    private Button mSetUart1Button;
    private EditText mUart1BaudEdit;
    private TextView mSetUart1Prompt;

    private TextView mUart2DataReceivedInfo;
    private Button mSendDataUart2Button;
    private EditText mUart2DataEdit;
    private TextView mSendDataUart2Prompt;
    private TextView mUart2BaudInfo;
    private Button mSetUart2Button;
    private EditText mUart2BaudEdit;
    private TextView mSetUart2Prompt;
    private LinearLayout mData2Layout;


    private AirController mAirController;
    private D2dController mD2dController;
    private VideoStream mVideoStream;
    private VideoCapture mVideoCapture;
    private DataStream mDataStream1;
    private DataStream mDataStream2;
    private EditText mServerUrl;

    private int mStreamId = 0;
    private Types.CaptureMode mCaptureMode = Types.CaptureMode.UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create air controller at first
        mAirController = new AirController(this, new AirControlListener() {
            @Override
            public void onCameraReset() {
                // operation should be done to reset rtsp video stream
            }
        });
        mAirController.start();

        // For video stream
        if (new File("/system/lib64/libgstreamer_android.so").exists()) {
            mVideoStream = new VideoStream(mAirController, new VideoStreamListener() {
                @Override
                public void onPlayReady(boolean ready) {
                    if (ready) {
                        mPlayButton.setEnabled(true);
                        if (mAirController.getVideoStreamCount() > 1) {
                            mSetVideoStreamButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mPlayButton.setEnabled(false);
                    }
                }

                @Override
                public void onPlayStateChanged(boolean playing) {
                    if (playing) {
                        mPlayButton.setText(R.string.action_stop);
                    } else {
                        mPlayButton.setText(R.string.action_play);
                    }
                }

                @Override
                public void onVideoStreamIdChanged(int id) {
                    mVideoStreamIdInfo.setText(getString(R.string.info_video_stream_id)+id);
                }

                @Override
                public void onVideoStreamResolutionChanged(Types.Resolution resolution) {
                    mVideoReslutionInfo.setText(getString(R.string.info_video_stream_resolution)+resolution);
                }
            });

            if (mStreamId > 0) {
                mVideoStream.setStreamId(mStreamId);
            }

        }




        mVideoStreamIdInfo = (TextView)findViewById(R.id.info_video_stream_id);
        mVideoStreamIdInfo.setText(getString(R.string.info_video_stream_id)+mVideoStream.getStreamId());
        mVideoReslutionInfo = (TextView)findViewById(R.id.info_video_stream_resolution);
        mTextureView = (TextureView) this.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                if (mVideoStream != null) {
                    mVideoStream.setSurface(new Surface(surfaceTexture));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
        mPlayButton = (Button) this.findViewById(R.id.switch_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoStream == null){
                    return;
                }
                if (!mVideoStream.isPlaying()) {
                    // set hardcoded rtsp url for slave controller
                    if (SystemPropertyUtil.getBoolean("persist.song.d2r.mode", false)) {
                        if (SystemPropertyUtil.getInt("persist.fpv.device.id", 0) == 2) {
                            mVideoStream.setStreamUrl("rtsp://192.168.0.254:8554/H264VideoSub");
                        }
                    }

                    // setup push server
                    if (!mServerUrl.getText().toString().isEmpty()) {
                        mVideoStream.setRtmpPushServerUrl(mServerUrl.getText().toString());
                    }

                    mVideoStream.play();
                } else {
                    mVideoStream.stop();
                }
            }
        });

        mSetVideoStreamButton = (Button)findViewById(R.id.switch_stream);
        mSetVideoStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoStream == null){
                    return;
                }
                mStreamId = (mVideoStream.getStreamId() == 1) ? 2 : 1;
                mVideoStream.setStreamId(mStreamId);
            }
        });

        // rtmp server for video stream push
        mServerUrl = (EditText)findViewById(R.id.input_server_url);

        // For video capture
        if (mVideoStream != null) {
            mVideoCapture = new VideoCapture(mAirController, mVideoStream, new VideoCaptureListener() {
                @Override
                public void onCaptureReady(boolean ready) {
                    // camera capture is ready, photo and video capture can work now
                    String str = ready ? getString(R.string.info_ready) : getString(R.string.info_not_ready);
                    mCaptureStateInfo.setText(getString(R.string.info_capture_state) + str);
                }

                @Override
                public void onToggleCaptureModeDone(Types.Result result) {
                    Log.d(TAG, "toggle capture mode done :" + result);
                    showResult(result, mToggleModePrompt, getString(R.string.prompt_toggle_mode));
                }

                @Override
                public void onTakePhotoDone(Types.Result result) {
                    Log.d(TAG, "take photo done :" + result);
                    showResult(result, mPhotoPrompt, getString(R.string.prompt_taking_photo));
                }

                @Override
                public void onStartVideoRecordingDone(Types.Result result) {
                    Log.d(TAG, "start video recording done :" + result);
                    showResult(result, mStartVideoPrompt, getString(R.string.prompt_starting_video_record));
                }

                @Override
                public void onStopVideoRecordingDone(Types.Result result) {
                    Log.d(TAG, "stop video recording done :" + result);
                    showResult(result, mStopVideoPrompt, getString(R.string.prompt_stopping_video_record));
                }

                @Override
                public void onVideoRecordingStatusChanged(boolean inVideoRecording) {
                    String info = inVideoRecording ? getString(R.string.info_on)
                            : getString(R.string.info_off);
                    mVideoRecordingInfo.setText(getString(R.string.info_video_recording) + info);
                }

                @Override
                public void onCaptureModeChanged(Types.CaptureMode captureMode) {
                    mCaptureMode = captureMode;
                    String info;
                    if (captureMode == Types.CaptureMode.PHOTO) {
                        info = getString(R.string.info_cap_mode_photo);
                    } else if (captureMode == Types.CaptureMode.VIDEO) {
                        info = getString(R.string.info_cap_mode_video);
                    } else {
                        info = "";
                    }
                    mCaptureModeInfo.setText(getString(R.string.info_capture_mode) + info);
                }

                @Override
                public void onFreeSpaceChanged(int i) {
                    String info = (i == -1) ? "" : String.format(getString(R.string.info_free_mb), i);
                    mFreeSpaceInfo.setText(getString(R.string.info_free_space) + info);
                }

                @Override
                public void onVideoStreamIdChanged(int i) {

                }
            });
        }

        mCaptureStateInfo = (TextView)findViewById(R.id.info_capture_state);
        mCaptureModeInfo = (TextView)findViewById(R.id.info_capture_mode);
        mVideoRecordingInfo = (TextView)findViewById(R.id.info_video_recording_status);
        mFreeSpaceInfo = (TextView)findViewById(R.id.info_free_space);
        mPhotoPrompt = (TextView)findViewById(R.id.photo_prompt);
        mToggleModePrompt = (TextView)findViewById(R.id.toggle_mode_prompt);
        mToggleModeButton = (Button)findViewById(R.id.toggle_mode);
        mToggleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Types.CaptureMode mode =Types.CaptureMode.PHOTO;
                if (mCaptureMode ==Types.CaptureMode.PHOTO) {
                    mode =Types.CaptureMode.VIDEO;
                }
                Types.RetValue ret = mVideoCapture.toggleCaptureMode(mode);
                showRetValue(ret, mToggleModePrompt, getString(R.string.prompt_toggle_mode));
            }
        });
        mPhotoButton = (Button)findViewById(R.id.photo);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Types.RetValue ret = mVideoCapture.takePhoto();
                showRetValue(ret, mPhotoPrompt, getString(R.string.prompt_taking_photo));
            }
        });

        mStartVideoPrompt = (TextView)findViewById(R.id.start_video_prompt);
        mStartVideoButton = (Button)findViewById(R.id.start_video);
        mStartVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Types.RetValue ret = mVideoCapture.startVideoRecording();
                showRetValue(ret, mStartVideoPrompt, getString(R.string.prompt_starting_video_record));
            }
        });

        mStopVideoPrompt = (TextView)findViewById(R.id.stop_video_prompt);
        mStopVideoButton = (Button)findViewById(R.id.stop_video);
        mStopVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Types.RetValue ret = mVideoCapture.stopVideoRecording();
                showRetValue(ret, mStopVideoPrompt, getString(R.string.prompt_stopping_video_record));
            }
        });

        // For data stream 1
        mDataStream1 = new DataStream(mAirController, 1, new DataStreamListener() {
            private int len = 0;
            @Override
            public void onDataReceived(byte[] data) {
                len += data.length;
                mUart1DataReceivedInfo.setText(getString(R.string.info_uart1_data_received)+len+"Bytes");
                // TODO: further process the data
            }

            @Override
            public void onUartReady(boolean b) {
                int count = mAirController.getUartCount();
                if (count > 0) {
                    mUart1BaudInfo.setText(getString(R.string.info_uart1_baudrate)
                            +mDataStream1.getUartBaudrate());
                }
            }

            @Override
            public void onSetUartBaudrateDone(Types.Result result) {
                showResult(result, mSetUart1Prompt, getString(R.string.prompt_setting_uart));
                int count = mAirController.getUartCount();
                if (count > 0) {
                    mUart1BaudInfo.setText(getString(R.string.info_uart1_baudrate)
                            +mDataStream1.getUartBaudrate());
                }
            }
        });
        mDataStream1.start();

        mUart1DataReceivedInfo = (TextView)findViewById(R.id.info_uart1_data_received);
        mSendDataUart1Prompt = (TextView)findViewById(R.id.send_data_uart1_prompt);
        mUart1DataEdit = (EditText)findViewById(R.id.input_text_to_send_uart1);
        mSendDataUart1Button = (Button)findViewById(R.id.send_data_uart1);
        mSendDataUart1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUart1DataEdit.getText().toString().isEmpty()) {
                    mSendDataUart1Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                };
                byte[] bytes = mUart1DataEdit.getText().toString().getBytes();
                if (mDataStream1.sendData(bytes, bytes.length) == bytes.length) {
                    mSendDataUart1Prompt.setText(getString(R.string.prompt_send_data_done));
                } else {
                    mSendDataUart1Prompt.setText(getString(R.string.prompt_send_data_failed));
                }
            }
        });

        mUart1BaudInfo = (TextView)findViewById(R.id.info_uart1_baud);
        mSetUart1Prompt = (TextView)findViewById(R.id.set_uart1_prompt);
        mUart1BaudEdit = (EditText)findViewById(R.id.input_uart1_baud);
        mSetUart1Button = (Button)findViewById(R.id.set_uart1);
        mSetUart1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUart1BaudEdit.getText().toString().isEmpty()) {
                    mSetUart1Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                };
                try {
                    int id = mDataStream1.getUartId();
                    int baud = Integer.valueOf(mUart1BaudEdit.getText().toString());
                    Types.RetValue ret = mDataStream1.setUartBaudrate(baud);
                    showRetValue(ret, mSetUart1Prompt, getString(R.string.prompt_setting_uart));
                } catch (Exception e) {
                    mSetUart1Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                }
            }
        });

        // For data stream 2
        mDataStream2 = new DataStream(mAirController, 2, new DataStreamListener() {
            private int len = 0;
            @Override
            public void onDataReceived(byte[] data) {
                len += data.length;
                mUart2DataReceivedInfo.setText(getString(R.string.info_uart2_data_received)+len+"Bytes");
                // TODO: further process the data
            }

            @Override
            public void onUartReady(boolean ready) {
                if (!ready) {
                    return;
                }
                int count = mAirController.getUartCount();
                if (count > 1) {
                    mData2Layout.setVisibility(View.VISIBLE);
                    mUart2BaudInfo.setText(getString(R.string.info_uart2_baudrate)
                            +mDataStream2.getUartBaudrate());
                } else {
                    mDataStream2.stop();
                }
            }

            @Override
            public void onSetUartBaudrateDone(Types.Result result) {
                showResult(result, mSetUart2Prompt, getString(R.string.prompt_setting_uart));
                int count = mAirController.getUartCount();
                if (count > 0) {
                    mUart2BaudInfo.setText(getString(R.string.info_uart2_baudrate)
                            +mDataStream2.getUartBaudrate());
                }
            }
        });
        mDataStream2.start();

        mData2Layout = (LinearLayout)findViewById(R.id.data2);
        mUart2DataReceivedInfo = (TextView)findViewById(R.id.info_uart2_data_received);
        mSendDataUart2Prompt = (TextView)findViewById(R.id.send_data_uart2_prompt);
        mUart2DataEdit = (EditText)findViewById(R.id.input_text_to_send_uart2);
        mSendDataUart2Button = (Button)findViewById(R.id.send_data_uart2);
        mSendDataUart2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUart2DataEdit.getText().toString().isEmpty()) {
                    mSendDataUart2Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                };
                byte[] bytes = mUart2DataEdit.getText().toString().getBytes();
                if (mDataStream2.sendData(bytes, bytes.length) == bytes.length) {
                    mSendDataUart2Prompt.setText(getString(R.string.prompt_send_data_done));
                } else {
                    mSendDataUart2Prompt.setText(getString(R.string.prompt_send_data_failed));
                }
            }
        });

        mUart2BaudInfo = (TextView)findViewById(R.id.info_uart2_baud);
        mSetUart2Prompt = (TextView)findViewById(R.id.set_uart2_prompt);
        mUart2BaudEdit = (EditText)findViewById(R.id.input_uart2_baud);
        mSetUart2Button = (Button)findViewById(R.id.set_uart2);
        mSetUart2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUart2BaudEdit.getText().toString().isEmpty()) {
                    mSetUart2Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                };
                try {
                    int id = mDataStream2.getUartId();
                    int baud = Integer.valueOf(mUart2BaudEdit.getText().toString());
                    Types.RetValue ret = mDataStream2.setUartBaudrate(baud);
                    showRetValue(ret, mSetUart2Prompt, getString(R.string.prompt_setting_uart));
                } catch (Exception e) {
                    mSetUart2Prompt.setText(getString(R.string.prompt_input_valid));
                    return;
                }
            }
        });

        // For D2d
        mD2dController = new D2dController(this, new D2dControlListener() {
            @Override
            public void onLinkConnectionChanged(boolean connected) {
                String info = connected ? getString(R.string.info_connected)
                        : getString(R.string.info_disconnected);
                mD2dConnectionInfo.setText(getString(R.string.info_d2d_connection)+info);
            }

            @Override
            public void onAirSignalChanged(int masterRsrp, int slaveRsrp) {
                String info = String.format(getString(R.string.info_dbm), masterRsrp);
                mD2dSignalAirInfo.setText(getString(R.string.info_d2d_signal_air)+info);
            }

            @Override
            public void onControllerSignalChanged(int masterRsrp, int slaveRsrp) {
                String info = String.format(getString(R.string.info_dbm), masterRsrp);
                mD2dSignalControllerInfo.setText(getString(R.string.info_d2d_signal_controller)+info);
            }

            @Override
            public void onAirSnrChanged(int masterSnr, int slaveSnr) {

            }

            @Override
            public void onControllerSnrChanged(int masterSnr, int slaveSnr) {

            }

            @Override
            public void onSpeedChanged(int bandwidth, int speed) {
                String info = String.format(getString(R.string.info_kbps), bandwidth);
                mD2dBandwidthInfo.setText(getString(R.string.info_d2d_bandwidth)+info);
                info = String.format(getString(R.string.info_kbps), speed);
                mD2dSpeedInfo.setText(getString(R.string.info_d2d_speed)+info);
            }

            @Override
            public void onPairAirDone(boolean success) {
                if (success) {
                    mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_succeed));
                } else {
                    mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_fail));
                }
            }
        });
        mD2dController.start();

        mPairPrompt = (TextView)findViewById(R.id.pair_prompt);
        mPairButton = (Button)findViewById(R.id.pair);
        mPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mD2dController.requestPairAir();
                mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_waiting));
            }
        });

        mD2dConnectionInfo = (TextView)findViewById(R.id.d2d_connection);
        mD2dSignalAirInfo = (TextView)findViewById(R.id.d2d_signal_air);
        mD2dSignalControllerInfo = (TextView)findViewById(R.id.d2d_signal_controller);
        mD2dBandwidthInfo = (TextView)findViewById(R.id.d2d_bandwidth);
        mD2dSpeedInfo = (TextView)findViewById(R.id.d2d_speed);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataStream1.stop();
        mDataStream2.stop();
        if (mVideoStream != null && mVideoStream.isPlaying()) {
            mVideoStream.stop();
        }
        mAirController.stop();
        mD2dController.stop();
    }

    private void showResult(Types.Result r, TextView v, String baseString) {
        switch (r) {
            case SUCCESS:
                v.setText(baseString+getString(R.string.prompt_result_success));
                break;
            case FAIL:
                v.setText(baseString+getString(R.string.prompt_result_fail));
                break;
            case TIMEOUT:
                v.setText(baseString+getString(R.string.prompt_result_timeout));
                break;
        }
    }

    private void showRetValue(Types.RetValue ret, TextView v, String string) {
        switch (ret) {
            case OK:
                v.setText(string+getString(R.string.prompt_return_ok));
                break;
            case NOT_READY:
                v.setText(string+getString(R.string.prompt_return_not_ready));
                break;
            case WAIT_PENDING:
                v.setText(string+getString(R.string.prompt_return_wait_pending));
                break;
            case CAP_NOT_READY:
                v.setText(string+getString(R.string.prompt_return_capture_not_ready));
                break;
            case NO_FREE_MEM:
                v.setText(string+getString(R.string.prompt_return_capture_no_free_mem));
                break;
            case REMOTE_ERROR:
            case UNKNOWN:
                v.setText(string+getString(R.string.prompt_result_fail));
                break;
        }
    }
}
