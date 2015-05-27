package com.fmakdemir.realtimevoicemodifier;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * @author fma
 * @date 5/27/15.
 */
public class AudioRecordListener implements AudioRecord.OnRecordPositionUpdateListener {
	private double[] filter;
	private final ByteBuffer buffer;
	private final ByteBuffer resBuffer;
	private static final String TAG = "AudioListener";
	AudioTrack track;

	public AudioRecordListener(double[] filter) {
		this.filter = filter;

		int buffSize = AudioRecord.getMinBufferSize(RealTimeVoiceModifier.SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

		buffer = ByteBuffer.allocate(buffSize);
		resBuffer = ByteBuffer.allocate(buffSize);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, RealTimeVoiceModifier.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.capacity(), AudioTrack.MODE_STREAM);
	}

	@Override
	public void onMarkerReached(AudioRecord audioRecord) {
		int markerPos = audioRecord.getNotificationMarkerPosition();
		Log.d(this.getClass().getSimpleName(), "onMarkerReached: "+markerPos);
	}

	@Override
	public void onPeriodicNotification(AudioRecord audioRecord) {
		int read = 0;
		do {
			read = audioRecord.read(buffer, buffer.capacity());
			Log.i(TAG, "Read, Cap: " + read + ", " + buffer.capacity());
			track.write(buffer.array(), 0, read);
			buffer.clear();
		} while (read > 0);
	}

	public void setFilter(double[] filter) {
		this.filter = filter;
	}
}
