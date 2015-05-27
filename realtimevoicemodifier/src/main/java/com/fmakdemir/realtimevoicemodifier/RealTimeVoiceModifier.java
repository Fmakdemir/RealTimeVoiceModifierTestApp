package com.fmakdemir.realtimevoicemodifier;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author fma
 * @date 5/26/15.
 */
public class RealTimeVoiceModifier {
	private static final String TAG = "RealTimeVoiceModifier";

	public static final int[] FREQ_SET = {11025, 16000, 22050, 44100};

	public static final int SAMPLE_RATE = FREQ_SET[3]; // 44.1 kHz
	public static final int NOTIFICATION_PERIOD = 100; // 100 ms??
	public static final int MIN_BUFFER_SIZE = SAMPLE_RATE*4; // ??
	public static final double[] DEFAULT_FILTER = {1}; // default filter has just 1 as its coef

	private AudioRecord mRecorder;
//	private AudioRecordListener listener;
	private Thread recordThread;

	private boolean recording;
	private double[] filter;

	public RealTimeVoiceModifier() {
		mRecorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE);
		mRecorder.setPositionNotificationPeriod(NOTIFICATION_PERIOD);

		filter = DEFAULT_FILTER;
		Log.d(TAG, "Init complete");
/*		listener = new AudioRecordListener(DEFAULT_FILTER);
		mRecorder.setRecordPositionUpdateListener(listener);*/
	}

	public void start() {
//		mRecorder.startRecording();
		Log.d(TAG, "Starting");
		recordThread = new Thread(new Runnable(){

			@Override
			public void run() {
				recording = true;
				Log.d(TAG, "Recording");
				startRecord();
			}

		});

		recordThread.start();
		Log.d(TAG, "Started");
	}

	private void startRecord() {
		File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

		try {
			boolean res = file.createNewFile();
			if (res) {
				throw new IOException("Couldn't get file");
			}

			OutputStream outputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

			int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			short[] audioData = new short[minBufferSize];

			AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					minBufferSize);

			audioRecord.startRecording();

			Log.d(TAG, "Come til now");
			while(recording){
				int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
//				Log.d(TAG, "Read: "+numberOfShort);
				for(int i = 0; i < numberOfShort; i++){
					dataOutputStream.writeShort(audioData[i]);
				}
			}

			Log.d(TAG, "Stopping");
			playRecord();
			audioRecord.stop();
			dataOutputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		recording = false;
	}

	public void setAudioFilter(double[] filter) {
		this.filter = filter;
		// listener.setFilter(filter);
	}

	void playRecord(){

		File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

		int shortSizeInBytes = Short.SIZE/Byte.SIZE;

		int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
		short[] audioData = new short[1000000];

		try {
			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

			int i = 0;
			while(dataInputStream.available() > 0){
				audioData[i] = dataInputStream.readShort();
				i++;
			}

			dataInputStream.close();

			AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSizeInBytes,
					AudioTrack.MODE_STREAM);

			audioTrack.play();
			audioTrack.write(audioData, 0, bufferSizeInBytes);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
