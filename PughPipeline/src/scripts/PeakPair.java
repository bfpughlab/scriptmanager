package scripts;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import objects.Peak;

@SuppressWarnings("serial")
public class PeakPair extends JFrame {
	private JTextArea textArea;
	
	private File INPUT = null;

	private int MODE;
	
	private int UP = 50;
	private int DOWN = 100;
	private int BIN_SIZE = 1;
	
	//Cutoffs
	private int ABS;
	private int REL;
			
	//Array to contain genetrack peaks separated by strand
	private ArrayList<Peak> FPEAKS = null;
	private ArrayList<Peak> RPEAKS = null;
	
	public PeakPair(File in, int mode, int up, int down, int bin, int abs, int rel) {
		setTitle("Peak Pairing Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		INPUT = in;

		MODE = mode;
		UP = up;
		DOWN = down;
		BIN_SIZE = bin;
		
		REL = rel;
		ABS = abs;
		
	}
	
	public void run() throws IOException {

		System.out.println(getTimeStamp());

		String TIME = getTimeStamp();
		textArea.append(TIME + "\n" + INPUT.getName().split("\\.")[0] + "\nPeak Pairing Parameters:\n");
		textArea.append("Upstream Distance: " + UP + "\nDownstream Distance: " + DOWN + "\nBin Size: " + BIN_SIZE);

		FPEAKS = new ArrayList<Peak>();
		RPEAKS = new ArrayList<Peak>();
		
		//Load genetrack peaks
		loadPeaks(INPUT);
		
		//generate directory to put matched peaks
		String NAME = "cwpair_output_";
		if(MODE == 0) NAME += "mode_";
		else if(MODE == 1) NAME += "closest_";
		else if(MODE == 2) NAME += "largest_";
		if(ABS != -999) NAME += "f" + ABS;
		else if(REL != -999) NAME += "fa" + REL;
		
		NAME += "u" + UP + "d" + DOWN + "b" + BIN_SIZE;
		new File(NAME).mkdirs();
		   
		//create output files for called peak pairs
	    PrintStream DOUT = null;
		PrintStream SOUT = null;
		PrintStream OOUT = null;
		String DETAIL = NAME + File.separator + "D_" + INPUT.getName().split("\\.")[0] + ".txt";
		String SUMMARY = NAME + File.separator + "S_" + INPUT.getName().split("\\.")[0] + ".gff";
		String ORPHAN = NAME + File.separator + "O_" + INPUT.getName().split("\\.")[0] + ".txt";
		String PDF = NAME + File.separator + "P_" + INPUT.getName().split("\\.")[0] + ".pdf";

		try { DOUT = new PrintStream(new File(DETAIL)); }
		catch (FileNotFoundException e) { e.printStackTrace(); }
		try { SOUT = new PrintStream(new File(SUMMARY)); }
		catch (FileNotFoundException e) { e.printStackTrace(); }
		try { OOUT = new PrintStream(new File(ORPHAN)); }
		catch (FileNotFoundException e) { e.printStackTrace(); }

		
		
		
		
		
		
		
		
		
		DOUT.close();
		SOUT.close();
		OOUT.close();
		
		System.out.println(getTimeStamp());
		
		dispose();
	}
	
	private void loadPeaks(File in) {
//		chr1	genetrack	.	1	21	114	+	.	stddev=5.23604125646
//		chr1	genetrack	.	29	49	248	+	.	stddev=4.14744351633
//		chr1	genetrack	.	68	88	250	+	.	stddev=6.33545578471
		
		Scanner scan = null;
		try { scan = new Scanner(in);
		} catch (FileNotFoundException e) {	e.printStackTrace(); }
		
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if(temp.length > 2) { 
				if(!temp[0].contains("track") && !temp[0].contains("#")) {
					if(temp[6].equals("+")) {
						FPEAKS.add(new Peak(temp[0], Integer.parseInt(temp[3]), Integer.parseInt(temp[4]), "+", Integer.parseInt(temp[5])));
					}
					else {
						RPEAKS.add(new Peak(temp[0], Integer.parseInt(temp[3]), Integer.parseInt(temp[4]), "-", Integer.parseInt(temp[5])));
					}
				}
			}
		}
		scan.close();
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}