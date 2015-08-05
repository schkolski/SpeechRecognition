package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import preProcessing.PreProcess;

public class Operations {

	private static FormatControlConf fc = new FormatControlConf();
	private static int samplingRate = (int) fc.getRate();
	// int samplePerFrame = 256;//16ms for 8 khz
	private static int samplePerFrame = 512; // 23.22ms

	public static FeatureVector extractFeatureFromExtractedAmplitureByteArray(
			float[] arrAmp) {
		PreProcess prp = new PreProcess(arrAmp, samplePerFrame, samplingRate);
		FeatureExtract fExt = new FeatureExtract(prp.framedSignal,
				samplingRate, samplePerFrame);
		fExt.makeMfccFeatureVector();
		return fExt.getFeatureVector();
	}

//	public static FeatureVector extractFeatureFromFile(File speechFile) throws IOException {
//		float[] arrAmp;
//		arrAmp = extractAmplitudeFromFile(speechFile);
//		return extractFeatureFromExtractedAmplitureByteArray(arrAmp);
//	}
//
//	private static float[] extractAmplitudeFromFile(File wavFile) throws IOException {
//		// create file input stream
//		FileInputStream fis = new FileInputStream(wavFile);
//		// create bytearray from file
//		byte[] arrFile = new byte[(int) wavFile.length()];
//		fis.read(arrFile);
//		fis.close();
//		return extractAmplitudeFromFileByteArray(arrFile);
//	}
//	private static float[] extractAmplitudeFromFileByteArray(byte[] audioBytes) {
//		// convert
//		float[] audioData = null;
//		int nlengthInSamples = audioBytes.length / 2;
//		audioData = new float[nlengthInSamples];
//		for (int i = 0; i < nlengthInSamples; i++) {
//			/* First byte is LSB (low order) */
//			int LSB = audioBytes[2 * i];
//			/* Second byte is MSB (high order) */
//			int MSB = audioBytes[2 * i + 1];
//			audioData[i] = MSB << 8 | (255 & LSB);
//		}
//		return audioData;
//	}

}
