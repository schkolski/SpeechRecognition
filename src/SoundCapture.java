import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Input.WavReader;
import util.FeatureVector;
import util.Operations;

public class SoundCapture extends JPanel implements ActionListener {

	// variables for the sound
	private byte[] audioBytes;
	private float[] audioData;

	// ui
	private JButton playBtn, captBtn, checkBtn;
	private String errStr;
	private JPanel innerPanel;

	// recording and playback
	private Playback playback;
	private Capture capture;

	// streams
	private AudioInputStream audioInputStream;
	private FileOutputStream fos;
	private ByteArrayInputStream bis;
	private AudioFormat format;

	// variables for formating a wav file
	final int sampleSize = 16;
	final boolean bigEndian = false;
	final int channels = 1;
	final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
	final float SAMPLING_RATE = 22050.0f;
	final int BUFFER_SIZE = 16384;

	public SoundCapture() {
		audioBytes = null;
		audioData = null;
		format = new AudioFormat(encoding, SAMPLING_RATE, sampleSize, channels,
				(sampleSize / 8) * channels, SAMPLING_RATE, bigEndian);
		playback = new Playback();
		capture = new Capture();

		setLayout(new BorderLayout());
		innerPanel = new JPanel();
		playBtn = addButton("Play", innerPanel, false);
		captBtn = addButton("Record", innerPanel, true);
		checkBtn = addButton("Check", innerPanel, false);
		add(innerPanel);
	}

	private JButton addButton(String name, JPanel p, boolean state) {
		JButton b = new JButton(name);
		b.setPreferredSize(new Dimension(85, 24));
		b.addActionListener(this);
		b.setEnabled(state);
		b.setFocusable(false);
		p.add(b);
		return b;
	}

	// dali ima snimeno nesto
	public boolean isSoundDataAvailable() {
		if (audioBytes != null) {
			return (audioBytes.length > 100);
		}
		return false;
	}

	public void extractFloatDataFromAmplitudeByteArray() {
		// convert
		audioData = null;
		int nlengthInSamples = audioBytes.length / 2;
		audioData = new float[nlengthInSamples];
		for (int i = 0; i < nlengthInSamples; i++) {
			/* First byte is LSB (low order) */
			int LSB = audioBytes[2 * i];
			/* Second byte is MSB (high order) */
			int MSB = audioBytes[2 * i + 1];
			audioData[i] = MSB << 8 | (255 & LSB);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(playBtn)) {
			if (playBtn.getText().startsWith("Play")) {
				playCaptured();
			} else {
				stopPlaying();
			}
		} else if (obj.equals(captBtn)) {
			if (captBtn.getText().startsWith("Record")) {
				startRecord();
			} else {
				stopRecording();
			}
		} else if (obj.equals(checkBtn)) {
			FeatureVector featureVector;
			extractFloatDataFromAmplitudeByteArray();
			featureVector = Operations
					.extractFeatureFromExtractedAmplitureByteArray(audioData);
			System.out.println(featureVector.toString());
		}
	}

	public void playCaptured() {
		playback.start();
		captBtn.setEnabled(false);
		playBtn.setText("Stop");
	}

	public void stopPlaying() {
		playback.stop();
		captBtn.setEnabled(true);
		playBtn.setText("Play");
	}

	public void startRecord() {
		capture.start();
		playBtn.setEnabled(false);
		checkBtn.setEnabled(false);
		captBtn.setText("Stop");
	}

	public void stopRecording() {
		capture.stop();
		playBtn.setEnabled(true);
		checkBtn.setEnabled(true);
		captBtn.setText("Record");
	}

	public JButton getPlayBtn() {
		return playBtn;
	}

	public JButton getCaptBtn() {
		return captBtn;
	}

	public static void main(String[] args) {
		SoundCapture sound = new SoundCapture();
		JFrame f = new JFrame("Timska rabota");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add("Center", sound);
		f.setSize(400, 100);
		f.setVisible(true);
		
//		WavReader wr = new WavReader();
//		try {
//			ArrayList<ArrayList<FeatureVector>> features = wr.getFeatureVectors();
//		} catch (IOException e) {
//			System.out.println("GRESHKA!");
//			e.printStackTrace();
//		}
	}

	class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;

		public void start() {
			thread = new Thread(this);
			thread.setName("Playback");
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if (message != null) {
				System.err.println(message);
			}
			if (thread != null) {
				thread = null;
				captBtn.setEnabled(true);
				playBtn.setText("Play");
			}
		}

		public void run() {

			// make sure we have something to play
			if (audioInputStream == null) {
				shutDown("No loaded audio to play back");
				return;
			}
			// reset to the beginnning of the stream
			try {
				audioInputStream.reset();
			} catch (Exception e) {
				shutDown("Unable to reset the stream\n" + e);
				return;
			}

			// get an AudioInputStream of the desired format for playback
			AudioInputStream playbackInputStream = AudioSystem
					.getAudioInputStream(format, audioInputStream);

			if (playbackInputStream == null) {
				shutDown("Unable to convert stream of format "
						+ audioInputStream + " to format " + format);
				return;
			}

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				shutDown("Line matching " + info + " not supported.");
				return;
			}

			// get and open the source data line for playback.

			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format, BUFFER_SIZE);
			} catch (LineUnavailableException ex) {
				shutDown("Unable to open the line: " + ex);
				return;
			}

			// play the captured audio data

			int frameSizeInBytes = format.getFrameSize();
			int bufferLengthInFrames = line.getBufferSize() / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[bufferLengthInBytes];
			int numBytesRead = 0;

			// start the source data line
			line.start();

			while (thread != null) {
				try {
					if ((numBytesRead = playbackInputStream.read(data)) == -1) {
						break;
					}
					int numBytesRemaining = numBytesRead;
					while (numBytesRemaining > 0) {
						numBytesRemaining -= line.write(data, 0,
								numBytesRemaining);
					}
				} catch (Exception e) {
					shutDown("Error during playback: " + e);
					break;
				}
			}
			// we reached the end of the stream. let the data play out, then
			// stop and close the line.
			if (thread != null) {
				line.drain();
			}
			line.stop();
			line.close();
			line = null;
			shutDown(null);
		}
	}

	class Capture implements Runnable {

		TargetDataLine line;
		Thread thread;

		public void start() {
			errStr = null;
			thread = new Thread(this);
			thread.setName("Capture");
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if ((errStr = message) != null && thread != null) {
				thread = null;

				playBtn.setEnabled(true);
				captBtn.setText("Record");
			}
		}

		public void run() {

			audioInputStream = null;

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			if (!AudioSystem.isLineSupported(info)) {
				shutDown("Line matching " + info + " not supported.");
				return;
			}

			// get and open the target data line for capture.

			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, line.getBufferSize());
			} catch (Exception ex) {
				shutDown(ex.toString());
				return;
			}

			// read the captured audio data
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int frameSizeInBytes = format.getFrameSize();
			int bufferLengthInFrames = line.getBufferSize() / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[bufferLengthInBytes];
			int numBytesRead;

			line.start();

			while (thread != null) {
				if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
					break;
				}
				out.write(data, 0, numBytesRead);
			}

			// we reached the end of the stream. stop and close the line.
			line.stop();
			line.close();
			line = null;

			// stop and close the output stream
			try {
				out.flush();
				out.close();
			} catch (IOException ex) {
				System.out.println("Error flushing the line");
			}

			// load bytes into the audio input stream for playback

			audioBytes = out.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
			audioInputStream = new AudioInputStream(bais, format,
					audioBytes.length / frameSizeInBytes);

			try {
				audioInputStream.reset();
			} catch (Exception ex) {
				System.out.println("Error in reseting audioInputStream");
			}

		}
	}

}
