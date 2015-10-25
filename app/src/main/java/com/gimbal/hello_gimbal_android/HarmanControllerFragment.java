package com.gimbal.hello_gimbal_android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import com.harman.hkwirelessapi.AudioCodecHandler;
import com.harman.hkwirelessapi.DeviceObj;
import com.harman.hkwirelessapi.HKDeviceStatusReason;
import com.harman.hkwirelessapi.HKPlayerState;
import com.harman.hkwirelessapi.HKWirelessHandler;
import com.harman.hkwirelessapi.HKWirelessListener;
import com.jasontoradler.harmankardoncontroller.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class HarmanControllerFragment extends Fragment implements HKWirelessListener {

    private static final String TAG = "HarmanController";
    private static final String KEY = "2FA8-2FD6-C27D-47E8-A256-D011-3751-2BD6";
    private static final String GIMBAL_API_KEY = "5f4ff778-27ab-4175-a475-1a14d7d11259";
    private HKWirelessHandler mHkWirelessHandler;
    private PlaceManager mPlaceManager;
    private PlaceEventListener mPlaceEventListener;
    private List<MusicUtil.Mp3Info> mMusicList;
    private List<DeviceData> mDeviceDataList;
    private AudioCodecHandler mAudioCodecHandler;
    private int mTimeElapsed;
    private String mCurrentMusicPath;
    private Button mPlaybackButton;

    public HarmanControllerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Set up the Gimbal beacon
        final Activity activity = getActivity();
        if (activity != null && activity.getApplication() != null) {
            Gimbal.setApiKey(activity.getApplication(), GIMBAL_API_KEY);
        } else {
            Log.e(TAG, "application is null, unable to setApiKey for Gimbal");
            throw new IllegalStateException("Could not Gimbal.seApiKey");
        }

        mDeviceDataList = new ArrayList<>();

        mAudioCodecHandler = new AudioCodecHandler();

        mPlaceEventListener = new PlaceEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
                super.onBeaconSighting(beaconSighting, list);
                Log.v(TAG, "onBeaconSighting: " + beaconSighting.getBeacon().toString());
                for (final Visit visit : list) {
                    Log.v(TAG, "  arrival: " + visit.getArrivalTimeInMillis() + ", dwell: " + visit.getDwellTimeInMillis() + ", departure: " + visit.getDepartureTimeInMillis());
                }
            }

            @Override
            public void onVisitEnd(Visit visit) {
                super.onVisitEnd(visit);
                Log.v(TAG, "onVisitEnd: " + visit);
                if (mCurrentMusicPath != null) {
                    togglePlayback(mCurrentMusicPath);
                }
            }

            @Override
            public void onVisitStart(Visit visit) {
                super.onVisitStart(visit);
                Log.v(TAG, "onVisitStart: " + visit);
                if (mCurrentMusicPath != null) {
                    togglePlayback(mCurrentMusicPath);
                }
            }
        };

        mPlaceManager = PlaceManager.getInstance();
        mPlaceManager.addListener(mPlaceEventListener);
        mPlaceManager.startMonitoring();

        CommunicationManager.getInstance().startReceivingCommunications();

        // Set up Harman/Kardon client
        // Create a HKWControlHandler instance
        mHkWirelessHandler = new HKWirelessHandler();

        // Initialize the HKWControlHandler and start wireless audio
        Log.v(TAG, "onActivityCreated: initializeHKWirelessController");
        mHkWirelessHandler.initializeHKWirelessController(KEY);

        mHkWirelessHandler.registerHKWirelessControllerListener(this);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getAvailableDevices();
                getMusicList();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "device and music list retrieved, ready to play/pause");
                        Toast.makeText(
                                getActivity(),
                                "Ready to play/pause music",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 3000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlaybackButton = (Button) view.findViewById(R.id.playbackButton);
        mPlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "playback button clicked: ");
                togglePlayback(mCurrentMusicPath);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mHkWirelessHandler != null) {
            Log.v(TAG, "onResume: startRefreshDeviceInfo");
            mHkWirelessHandler.startRefreshDeviceInfo();
        }
    }

    private void getAvailableDevices() {
        int deviceCount = mHkWirelessHandler.getDeviceCount();
        mDeviceDataList.clear();
        Log.v(TAG, "device count: " + deviceCount);
        for (int ii = 0; ii < deviceCount; ++ii) {
            final DeviceObj deviceObj = mHkWirelessHandler.getDeviceInfoByIndex(ii);
            final DeviceData deviceData = new DeviceData(deviceObj, true);
            mDeviceDataList.add(deviceData);
            Log.v(TAG, "device: " + deviceData);
        }

        // TODO remove kludge -- onDeviceStateUpdated should handle adding device to session
        mHkWirelessHandler.addDeviceToSession(23983452403888L);
    }

    private void getMusicList() {
        mMusicList = MusicUtil.getMusicList(getActivity());

        // TODO change this to allow user to select which music to play
        if (mMusicList.size() > 0) {
            mCurrentMusicPath = mMusicList.get(0).getUrl();
        }
    }

    private void togglePlayback(final String path) {
        Log.v(TAG, "togglePlayback: " + path);

        String songName = path.substring(path.lastIndexOf("/") + 1);
        Log.v(TAG, "  songName=" + songName);

        if (mAudioCodecHandler.isPlaying()) {
            Log.v(TAG, "pausing music");
            mAudioCodecHandler.pause();
        } else {
            Log.v(TAG, "playing music from: " + mTimeElapsed);
            mAudioCodecHandler.playCAFFromCertainTime(path, songName, mTimeElapsed);
        }

//        File file = new File("/storage/emulated/legacy/amazonmp3/The_Submarines/Declare_A_New_State/B0045EBNGW_(disc_1)_03_-_Vote.mp3");
//        Log.v(TAG, "file: " + file.getAbsolutePath() + " exists: " + file.exists());
//        audioCodecHandler.playCAF(
//                "/mnt/shell/emulated/0/amazonmp3/The_Submarines/Declare_A_New_State/B0045EBNGW_(disc_1)_03_-_Vote.mp3",
//                "Vote",
//                false);
//        audioCodecHandler.playCAF(
//                "/mnt/shell/emulated/0/amazonmp3/Kate_Earl/Kate_Earl/B002L4BQWE_(disc_1)_07_-_Golden_Street.mp3",
//                "B002L4BQWE_(disc_1)_07_-_Golden_Street.mp3",
//                false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mHkWirelessHandler != null) {
            Log.v(TAG, "onPause: startRefreshDeviceInfo");
            mHkWirelessHandler.stopRefreshDeviceInfo();
//            mHkWirelessHandler.removeDeviceFromSession(23983452403888L);
        }
    }

    @Override
    public void onDeviceStateUpdated(long deviceId, int reason) {
        Log.v(TAG, "onDeviceStateUpdated: deviceId=" + deviceId + ", reason=" + reason);
        final DeviceObj deviceObj = mHkWirelessHandler.findDeviceFromList(deviceId);
        final DeviceData deviceData = new DeviceData(deviceObj,
                (reason == HKDeviceStatusReason.HKDeviceStatusReasonDeviceAvailable.ordinal()));
        Log.v(TAG, "device: " + deviceData.toString());
    }

    @Override
    public void onPlaybackStateChanged(int playState) {
        if (playState >= 0 && playState < HKPlayerState.values().length) {
            final HKPlayerState playerState = HKPlayerState.values()[playState];
            switch (playerState) {
                case EPlayerState_Init:
                    Log.v(TAG, "onPlaybackStateChanged:  INIT");
                    break;
                case EPlayerState_Play:
                    Log.v(TAG, "onPlaybackStateChanged:  PLAY");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPlaybackButton.setText(R.string.pause);
                        }
                    });
                    break;
                case EPlayerState_Preparing:
                    Log.v(TAG, "onPlaybackStateChanged:  PREPARING");
                    break;
                case EPlayerState_Pause:
                    Log.v(TAG, "onPlaybackStateChanged:  PAUSE");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPlaybackButton.setText(R.string.play);
                        }
                    });
                    break;
                case EPlayerState_Stop:
                    Log.v(TAG, "onPlaybackStateChanged:  STOP");
                    break;
            }
        } else {
            Log.v(TAG, "onPlaybackStateChanged: INVALID=" + playState);
        }
    }

    @Override
    public void onVolumeLevelChanged(long deviceId, int deviceVolume, int avgVolume) {
        Log.v(TAG, "onVolumeLevelChanged: deviceId=" + deviceId + " deviceVolume=" + deviceVolume + ", avgVolume=" + avgVolume);
    }

    @Override
    public void onPlayEnded() {
        Log.v(TAG, "onPlayEnded");
        mTimeElapsed = 0;
    }

    @Override
    public void onPlaybackTimeChanged(int timeElapsed) {
        Log.v(TAG, "onPlaybackTimeChanged: timeElapsed=" + timeElapsed);
        mTimeElapsed = timeElapsed;
    }

    @Override
    public void onErrorOccurred(int errorCode, String errorMsg) {
        Log.v(TAG, "onErrorOccurred: errorCode=" + errorCode + ", errorMsg=" + errorMsg);
    }
}
