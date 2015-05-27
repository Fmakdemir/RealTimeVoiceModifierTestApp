package com.fmakdemir.realtimevoicemodifier;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * @author fma
 * @date 5/26/15.
 */
public class RealTimeVoiceModifier {
	private static final String TAG = "RealTimeVoiceModifier";

	public static final int[] FREQ_SET = {11025, 16000, 22050, 44100};

	public static final int SAMPLE_RATE = FREQ_SET[3]; // 44.1 kHz
	public final int MIN_BUFFER_SIZE; // = SAMPLE_RATE*4; // ??
	public static final double[] DEFAULT_FILTER = {1}; // default filter has just 1 as its coef

	private Thread recordThread;

	private boolean recording;
	private double[] filter;

	public RealTimeVoiceModifier() {
		// Keep the buffer to min possible value to prevent delay between record and play
		MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		// default filter is {1} which gives the record itself
		filter = DEFAULT_FILTER;
		Log.d(TAG, "Init complete");
	}

	public void start() {
		Log.d(TAG, "Starting");
		if (recordThread == null) {
			recordThread = new Thread(new Runnable() {

				@Override
				public void run() {
					recording = true;
					Log.d(TAG, "Recording");
					startRecord();
				}

			});

			recordThread.start();
		}
		Log.d(TAG, "Started");
	}

	private void startRecord() {
		Log.d(TAG, "minBufferSize: "+MIN_BUFFER_SIZE);

		short[] audioData = new short[MIN_BUFFER_SIZE]; //
		int bufferSizeInBytes = MIN_BUFFER_SIZE*Short.SIZE/Byte.SIZE;

		// AudioTrack object for playing sound
		AudioTrack audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC,
				SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufferSizeInBytes,
				AudioTrack.MODE_STREAM);

		// AudioRecord object for recording from mic
		AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				MIN_BUFFER_SIZE);

		// Start recording
		audioRecord.startRecording();
		// Start playing
		audioTrack.play();

		Log.d(TAG, "Not it should start playing");
		double val;
		int i, j;
		while(recording){
			// get data from mic
			int numberOfShort = audioRecord.read(audioData, 0, MIN_BUFFER_SIZE);
			// apply linear filter
			for (i = numberOfShort-1; i >= filter.length-1; --i) {
				for (j = 0, val = 0; j < filter.length; ++j) {
					val += filter[j]*audioData[i-filter.length+j+1];
				}
				audioData[i] = (short)val;
			}
			// write to play
			audioTrack.write(audioData, 0, numberOfShort);
		}

		Log.d(TAG, "Stopping");

		// cleanup
		audioRecord.stop();
		audioTrack.release();
		recordThread = null;
	}

	public void stop() {
		// this will break loop of record
		recording = false;
	}

	public void setAudioFilter(double[] filter) {
		this.filter = filter;
	}
}
