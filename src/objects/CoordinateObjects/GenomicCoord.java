package objects.CoordinateObjects;

public interface GenomicCoord {
	public String getChrom();	
	public void setChrom(String chr);
	
	public long getStart();
	public void setStart(int sta);
	
	public long getStop();
	public void setStop(int sto);
	
	public double getScore();	
	public void setScore(double sco);
	
	public String getDir();	
	public void setDir(String di);
	
	public String getName();
	public void setName(String na);

	public String toString();
		
}
