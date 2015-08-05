package hmm;

import java.util.ArrayList;

public class Centroid implements Cloneable{
	
	private int total_pts;
	private ArrayList<Points> points;
	private double distortion;
	private double[] coordinates;
	
	public Centroid(double coordinates[]) {
		this.total_pts = 0;
		this.points = new ArrayList<Points>();
		this.distortion = 0;
		this.coordinates = coordinates;
		
	}
	
	public void add(Points pt, double dist) {
		total_pts++;
		points.add(pt);
		distortion += dist;
	}
	
	public void update() {
		if (total_pts != 0){
			double sum_coordinates[] = new double[Points.DIMENSION];
	
			
			for(Points tmpPoint : points){
				for (int k = 0; k < Points.DIMENSION; k++) {
					sum_coordinates[k] += tmpPoint.getCoordinateAt(k);
				}
			}
	
	
	
			// divide sum of coordinates by total number points to get average
			for (int k = 0; k < Points.DIMENSION; k++) {
				coordinates[k] = sum_coordinates[k] / (double) total_pts;
			}
		}else{
			coordinates = new double[Points.DIMENSION];
		}

		// reset number of points
		total_pts = 0;
		points = new ArrayList<Points>();
		// reset distortion measure
		distortion = 0;
	}

	public int getTotal_pts() {
		return total_pts;
	}

	public ArrayList<Points> getPoints() {
		return points;
	}

	public double getDistortion() {
		return distortion;
	}

	public double[] getCoordinates() {
		return coordinates;
	}
	
	@Override
	protected Centroid clone()  {
		Centroid res = new Centroid(this.coordinates);
		
		res.distortion = this.distortion;
		res.total_pts = this.total_pts;
		res.points = (ArrayList<Points>) this.points.clone();

		return res;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		
		for(double elem : coordinates){
			sb.append(elem);
			sb.append(",");
		}
		return sb.substring(0, sb.length() - 1).toString();
	}
}
