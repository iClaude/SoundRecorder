package com.danielkim.soundrecorder.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.R;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // Constants.
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final int REQUEST_DANGEROUS_PERMISSION = 0;

    private boolean marshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private int position;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;
    private TextView mRecordingPrompt;

    private boolean isRecording = false;
    private boolean mPauseRecording;

    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private TextView tvChronometer;

    private ServiceOperationsListener serviceOperationsListener;


    /*
        Interface used to communicate with the Activity with regard to the connected Service.
     */
    public interface ServiceOperationsListener {
        void onStartRecord();

        void onStopRecord();

        boolean isConnected();

        boolean serviceIsRecording();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            serviceOperationsListener = (ServiceOperationsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ServiceOperationsListener");
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        tvChronometer = (TextView) recordView.findViewById(R.id.tvChronometer);
        tvChronometer.setText("00:00");
        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setEnabled(serviceOperationsListener.isConnected());
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!marshmallow) {
                    startStopRecording();
                } else {
                    checkPermissions();
                }
            }
        });

        mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });

        // Are we already recording? Check necessary if Service is connected to the Activity before the Fragment is created.
        checkRecording();

        return recordView;
    }

    // Check dangerous permissions for Android Marshmallow+.
    private void checkPermissions() {
        // Check permissions.
        boolean writePerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean audioPerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        String[] arrPermissions;
        if (!writePerm && !audioPerm) {
            arrPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        } else if (!writePerm && audioPerm) {
            arrPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else if (writePerm && !audioPerm) {
            arrPermissions = new String[]{Manifest.permission.RECORD_AUDIO};
        } else {
            startStopRecording();
            return;
        }

        // Request permissions.
        ActivityCompat.requestPermissions(getActivity(), arrPermissions, REQUEST_DANGEROUS_PERMISSION);
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void startStopRecording() {
        if (!isRecording) {
            // start recording
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();

            // Start RecordingService: send request to main Activity.
            if (serviceOperationsListener != null) {
                serviceOperationsListener.onStartRecord();
            }

            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep screen on while recording
            isRecording = true;
        } else {
            //stop recording
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            tvChronometer.setText("00:00");
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            // Stop RecordingService: send request to main Activity.
            if (serviceOperationsListener != null) {
                serviceOperationsListener.onStopRecord();
            }

            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //allow the screen to turn off again once recording is finished
            isRecording = false;
        }
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {

    }

    public void serviceConnection(boolean isConnected) {
        mRecordButton.setEnabled(isConnected);

        // Are we already recording?
        checkRecording();
    }

    private void checkRecording() {
        if (serviceOperationsListener == null) return;

        if (serviceOperationsListener.isConnected() && serviceOperationsListener.serviceIsRecording()) {
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
            isRecording = true;
        }
    }

    public void timerChanged(int seconds) {
        tvChronometer.setText(mTimerFormat.format(new Date(seconds * 1000L)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (serviceOperationsListener != null) serviceOperationsListener = null;
    }
}