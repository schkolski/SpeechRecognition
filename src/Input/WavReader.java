package Input;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import util.FeatureVector;
import util.Operations;

public class WavReader {
	private static final String PATH= "wavs";
	private File wavPath;
	
	public WavReader(){
		wavPath = new File(PATH);
	}
	
	private String[] getFolderNames() {

		String[] folderNames = new String[wavPath.list().length];
		folderNames = wavPath.list();
		return folderNames;
	}
	
	public ArrayList<ArrayList<FeatureVector>> getFeatureVectors() throws IOException{
		File[][] waveFiles = new File[getFolderNames().length][];
		String[] folderNames = getFolderNames();
		
		waveFiles = new File[folderNames.length][];
		for (int i = 0; i < folderNames.length; i++) {
			System.out.println(folderNames[i]);
			
			File wordDir = new File(wavPath.getPath() + "\\" + folderNames[i]+"\\");
			waveFiles[i] = wordDir.listFiles();
		}
		ArrayList<ArrayList<FeatureVector>> features = new ArrayList<ArrayList<FeatureVector>>();
		System.out.println("      Sodrzina vo dir:        ");
		for (int i = 0; i < waveFiles.length; i++) {
			ArrayList<FeatureVector> tmp = new ArrayList<FeatureVector>();
			for (int j = 0; j < waveFiles[i].length; j++) {
				System.out.print(waveFiles[i][j].getName() + "\t\t");
				// TODO: ovde ni treba medota so koja sto go zemame feature vectorot za daden file
				tmp.add(Operations.extractFeatureFromFile(waveFiles[i][j]));
			}
			System.out.println();
			features.add(tmp);
		}
		
		return features;
	}
	
}
