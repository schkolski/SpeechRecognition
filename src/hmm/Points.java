package hmm;

public class Points {
	private double coordinates[];

	// 12 MFCC + 12 delta MFCC + 12 delta-delta MFCC 
	// + 1 Energy + 1 delta energy + 1 delta-delta energy		
	public static final int DIMENSION = 39; 
		
	public Points(double co[]) {
		coordinates = co;
	}

	public double[] getCoordinates() {
		return coordinates;
	}
	
	public double getCoordinateAt(int i) {
		return coordinates[i];
	}
	
	public void setCoordinateAt(int i, double value) {
		coordinates[i] = value;
	}

}
