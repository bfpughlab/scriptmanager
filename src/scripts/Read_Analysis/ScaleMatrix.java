package scripts.Read_Analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class ScaleMatrix {
	
	private File MATRIX = null;
	private File OUTFILE = null;
	private double SCALE = 0;

	private int ROWINDEX = 0;
	private int COLINDEX = 0;
	
	public ScaleMatrix(File m, File o, double s, int r, int c) {
		MATRIX = m;
		OUTFILE = o;
		SCALE = s;
		
		ROWINDEX = r;
		COLINDEX = c;
	}
	
	public void run() throws IOException {
		//Open output file
		PrintStream OUT = new PrintStream(OUTFILE);
		
		System.err.println(getTimeStamp());
		System.err.println("Processing file:\t" + MATRIX.getName());
		System.err.println("Scaling factor:\t" + SCALE);
		System.err.println("Starting row index:\t" + ROWINDEX);
		System.err.println("Starting column index:\t" + COLINDEX);
		
		try {
			//Parse, scale, and output tab-delimited matrix on the fly
			Scanner SCAN = new Scanner(MATRIX);
			int counter = 0;
			while (SCAN.hasNextLine()) {
				String[] temp = SCAN.nextLine().split("\t");
				if(counter < ROWINDEX) { OUT.println(String.join("\t", temp)); }
				else {
					for(int x = 0; x < temp.length; x++) {
						if(x < COLINDEX) { OUT.print(temp[x]); }
						else { OUT.print(Double.parseDouble(temp[x]) * SCALE); }
						if(x < temp.length - 1) { OUT.print("\t"); }
					}
					OUT.println();
				}			
				counter++;
				if(counter % 1000 == 0) { System.err.println("Rows processed: " + counter); }
		    }
			SCAN.close();
		} catch(NumberFormatException e) {
    		JOptionPane.showMessageDialog(null, MATRIX.getName() + " contains non-numbers in indexes selected!!!");
    	}
		OUT.close();
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}
