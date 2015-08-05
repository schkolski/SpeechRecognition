package hmm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Codebook {
	private static final String CODEBOOK_FILENAME = "codebook.cbk";
	private static final int CODEBOOK_SIZE = 256;
	private static final double MIN_DISTORTION = 0.1;
	private Points points[];
	private double distorsion;

	private Centroid centroids[];
	
	private static final int KMEANS_ITERATIONS = 10;
	
	public Codebook(){
		// pri kreiranje na codebook bez parametri, se cita codebookot od file
		readFromFile();
	}
	
	private void readFromFile(){
		try {
			BufferedReader in = new BufferedReader(new FileReader(CODEBOOK_FILENAME));
			String line = "";
			int i = 0;
			centroids = new Centroid[CODEBOOK_SIZE];
			
			while((line = in.readLine()) != null && line.length() > 0){
				line = line.trim();
				String[] args = line.split(",");
				double[] coordinates = new double[args.length];
				
				for(int j = 0; j < args.length; i++){
					coordinates[j] = Double.parseDouble(args[j]);
				}
				centroids[i] = new Centroid(coordinates);
				++i;
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("Faild to find codebook!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Faild to read codebook!");
			e.printStackTrace();
		}
	}
	
	public Codebook(Points tmpPt[]) {
		this.points = tmpPt;

		// Ni trebaat poveke od CODEBOOK_SIZE tocki za da gi najdeme centroidite
		if (points.length >= CODEBOOK_SIZE) {
			distorsion = Double.MAX_VALUE;
			double tmp_distorsion = Double.MAX_VALUE;

			Centroid[] tmpCentroids = new Centroid[CODEBOOK_SIZE];
			
 			for(int i = 0; i < KMEANS_ITERATIONS; ++i){
 				tmp_distorsion = kMeans();
				if(distorsion > tmp_distorsion){
					for(int j = 0; j < centroids.length; i++){
						tmpCentroids[j] = centroids[j].clone();
					}
					tmpCentroids = centroids.clone();
					distorsion = tmp_distorsion;
				}
 			}
 			
		} else {
			System.err.println("Not enough training points");
		}
	}

	private void initializeCentroids() {
		double minimum[] = new double[Points.DIMENSION];
		double maximum[] = new double[Points.DIMENSION];

		for (int i = 0; i < Points.DIMENSION; ++i) {
			double MIN = Double.MAX_VALUE;
			double MAX = Double.MIN_VALUE;

			for (int j = 0; j < points.length; ++j) {
				if (MIN > points[j].getCoordinateAt(i)) {
					MIN = points[j].getCoordinateAt(i);
				}
				if (MAX < points[j].getCoordinateAt(i)) {
					MAX = points[j].getCoordinateAt(i);
				}
			}
			minimum[i] = MIN;
			maximum[i] = MAX;
		}

		// inicijalizacija na centroidi so random points vektor
		centroids = new Centroid[CODEBOOK_SIZE];

		Random r = new Random();
		for (int i = 0; i < CODEBOOK_SIZE; i++) {
			double[] tmp = new double[Points.DIMENSION];
			for (int j = 0; j < Points.DIMENSION; j++) {
				tmp[j] = r.nextDouble() * (maximum[j] - minimum[j])
						+ minimum[j];
			}
			centroids[i] = new Centroid(tmp);
		}

	}

	private double kMeans() {
		initializeCentroids();
		double distortion_before_update = 0.0; // distortion measure before
		double distortion_after_update = 0.0; // distortion measure after update
		
		groupPtoC();
		do {
			for (int i = 0; i < centroids.length; i++) {
				distortion_before_update += centroids[i].getDistortion();
				centroids[i].update();
			}

			// regroup
			groupPtoC();

			for (int i = 0; i < centroids.length; i++) {
				distortion_after_update += centroids[i].getDistortion();
			}

		} while (Math.abs(distortion_after_update - distortion_before_update) < MIN_DISTORTION);
		return 0.0;
	}

	private void groupPtoC() {
		// find closest Centroid and assign Points to it
		for (int i = 0; i < points.length; i++) {
			int index = closestCentroidToPoint(points[i]);
			centroids[index].add(points[i],
					getDistance(points[i], centroids[index]));
		}
	}

	private int closestCentroidToPoint(Points pt) {
		double tmp_dist = 0;
		double lowest_dist = 0; // = getDistance(pt, centroids[0]);
		int lowest_index = 0;

		for (int i = 0; i < centroids.length; i++) {
			tmp_dist = getDistance(pt, centroids[i]);
			if (tmp_dist < lowest_dist || i == 0) {
				lowest_dist = tmp_dist;
				lowest_index = i;
			}
		}
		return lowest_index;
	}

	// Evklidovo rastojanie na N-dim vektori
	private double getDistance(Points tPt, Centroid tC) {
		double distance = 0;
		double temp = 0;
		double[] centroid_coord = tC.getCoordinates();
		for (int i = 0; i < Points.DIMENSION; i++) {
			temp = tPt.getCoordinateAt(i) - centroid_coord[i];
			distance += temp * temp;
		}
		distance = Math.sqrt(distance);
		return distance;
	}
	
	public int[] quantize(Points pts[]) {
		int output[] = new int[pts.length];
		for (int i = 0; i < pts.length; i++) {
			output[i] = closestCentroidToPoint(pts[i]);
		}
		return output;
	}
	
	public void saveToFile(){		
		try {
			PrintWriter out = new PrintWriter(
					new BufferedWriter(
							new FileWriter(CODEBOOK_FILENAME)));
			for(Centroid elem : centroids){
				out.write(elem.toString() + "\n");
			}
			out.close();
			System.out.println("Succsessfuly written CODEBOOK into File:" + CODEBOOK_FILENAME);
		} catch (IOException e) {
			System.out.println("Can't write to file...");
			e.printStackTrace();
		}
	}
}
