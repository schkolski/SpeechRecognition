package hmm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import util.FeatureVector;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationIntegerReader;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesReader;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;

public class HMM {

	private Codebook codebook;
	private static final int NUMBER_OF_STATES = 7;
	public HMM() {
		this.codebook = new Codebook();
	}

	// WordSamples se feature vectorite za site snimki na eden zbor
	public void hmmTrain(String word) throws IOException, FileFormatException {
		// TODO: Da se napravi citanje na fajlovite
		// trainTestWavs = new TrainingTestingWaveFiles("train");
		// extract features
		// wavFiles = trainTestWavs.readWaveFilesList();
		// words = trainTestWavs.readWordWavFolder();

		// for each training word


		System.out.println("HMM for: " + word);

		Reader reader = new FileReader(word + ".obs");
		List<List<ObservationInteger>> seqs = ObservationSequencesReader
				.readSequences(new ObservationIntegerReader(), reader);

		reader.close();
		
		Hmm<ObservationInteger> hmm = new LeftRightHmm<ObservationInteger>(NUMBER_OF_STATES, 2);
		
		hmm.setPi(0, 1.0);
		for(int i = 1; i < NUMBER_OF_STATES; i++){
			hmm.setPi(i, 0);
		}
        
		BaumWelchLearner learner = new BaumWelchLearner();
        learner.learn(hmm, seqs);
        logHmm(hmm, word + ".dot");
        
        
		System.out.println("HMM Train Completed");
	}
	
	public void writeObs(ArrayList<FeatureVector> wordSamples, String word) throws IOException{

		int numberOfSamples = wordSamples.size();
		int quantized[][] = new int[numberOfSamples][];

		for (int i = 0; i < numberOfSamples; i++) {
			Points[] pts = getPointsFromFeatureVector(wordSamples.get(i));
			quantized[i] = codebook.quantize(pts);
		}
		
		Writer out = new FileWriter(new File(word + ".obs")); 
		for(int i = 0; i < numberOfSamples; i++){
			for(int j = 0; j < quantized[i].length; j++){
				out.write(quantized[i][j] + "; ");
			}
			out.write("\n");
		}
		out.close();
	}
	
	private Points[] getPointsFromFeatureVector(FeatureVector features) {
		// get Points object from all feature vector
		Points pts[] = new Points[features.getFeatureVector().length];
		for (int j = 0; j < features.getFeatureVector().length; j++) {
			pts[j] = new Points(features.getFeatureVector()[j]);
		}
		return pts;
	}
	
	private void logHmm(Hmm<ObservationInteger> hmm, String filename)
			throws IOException {
		System.out.println("------------------------------");
		(new GenericHmmDrawerDot()).write(hmm, filename);
		System.out.println(hmm);
	}
}
