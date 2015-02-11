package window_interface.Data_Analysis;

import file_filters.BAMFilter;
import file_filters.BEDFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.SwingWorker;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

import objects.BEDCoord;
import objects.PileupParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import scripts.TagPileup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("serial")
public class TagPileupWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	
	final DefaultListModel expList;
	Vector<File> BAMFiles = new Vector<File>();
	Vector<BEDCoord> COORD = null;
	private File INPUT = null;
	private File OUTPUT = null;
	
	private JButton btnPileup;
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JRadioButton rdbtnRead1;
	private JRadioButton rdbtnRead2;
	private JRadioButton rdbtnCombined;
	private JRadioButton rdbtnSeperate;
	private JRadioButton rdbtnComb;
	private JRadioButton rdbtnNone;
	private JRadioButton rdbtnGaussianSmooth;
	private JRadioButton rdbtnSlidingWindow;
	private JRadioButton rdbtnTabdelimited;
	private JRadioButton rdbtnCdt;

	private JLabel lblPleaseSelectWhich_1;
	private JLabel lblWindowSizebin;
	private JLabel lblTagShift;
	private JLabel lblStdDevSize;
	private JLabel lblNumStd;
	private JLabel lblBEDFile;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrentOutput;
	private JLabel lblPleaseSelectOutput;
	private JTextField txtShift;
	private JTextField txtBin;
	private JTextField txtSmooth;
	private JTextField txtStdSize;
	private JTextField txtNumStd;
	private JCheckBox chckbxOutputData;
	private JCheckBox chckbxTagStandard;
	
	JProgressBar progressBar;
	public Task task;
	private JLabel lblCpusToUse;
	private JTextField txtCPU;

	class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() throws IOException, InterruptedException {
        	try {
        		if(Double.parseDouble(txtBin.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Bin Size!!! Must be larger than 0 bp");
        		} else if(rdbtnSlidingWindow.isSelected() && Double.parseDouble(txtSmooth.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Smoothing Window Size!!! Must be larger than 0 bp");
        		} else if(rdbtnGaussianSmooth.isSelected() && Double.parseDouble(txtStdSize.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Standard Deviation Size!!! Must be larger than 0 bp");
        		} else if(rdbtnGaussianSmooth.isSelected() && Double.parseDouble(txtNumStd.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Number of Standard Deviations!!! Must be larger than 0");
        		} else if(Integer.parseInt(txtCPU.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Number of CPU's!!! Must use at least 1");
        		} else if(INPUT == null) {
        			JOptionPane.showMessageDialog(null, "BED File Not Loaded!!!");
        		} else if(BAMFiles.size() < 1) {
        			JOptionPane.showMessageDialog(null, "No BAM Files Loaded!!!");
        		} else {
		        	setProgress(0);
		        	
		        	//Load up parameters for the pileup into single object
		        	PileupParameters param = new PileupParameters();
		        	if(rdbtnSeperate.isSelected()) { param.setStrand(0); }
		        	else if(rdbtnComb.isSelected()) { param.setStrand(1); }
		        	
		        	if(rdbtnRead1.isSelected()) { param.setRead(0); }
		        	else if(rdbtnRead2.isSelected()) { param.setRead(1); }
		        	else if(rdbtnCombined.isSelected()) { param.setRead(2); }
		        	
		        	if(rdbtnNone.isSelected()) { param.setTrans(0); }
		        	else if(rdbtnSlidingWindow.isSelected()) { param.setTrans(1); }
		        	else if(rdbtnGaussianSmooth.isSelected()) { param.setTrans(2); }
		        			        	
		        	if(!chckbxOutputData.isSelected()) { 
		        		param.setOutput(null);
		        		param.setOutputType(0);
		        	}
		        	else if(OUTPUT == null) { param.setOutput(new File(System.getProperty("user.dir"))); }
		        	else { param.setOutput(OUTPUT); }
		        	
		        	if(chckbxOutputData.isSelected() && rdbtnTabdelimited.isSelected()) param.setOutputType(1);
		        	else if(chckbxOutputData.isSelected() && rdbtnCdt.isSelected()) param.setOutputType(2);
		        	
		        	if(chckbxTagStandard.isSelected()) param.setStandard(true);
		        	else param.setStandard(false);
		        	
		        	//SHIFT can be negative
		        	param.setShift(Integer.parseInt(txtShift.getText()));
		        	param.setBin(Integer.parseInt(txtBin.getText()));
		        	param.setSmooth(Integer.parseInt(txtSmooth.getText()));
		        	param.setStdSize(Integer.parseInt(txtStdSize.getText()));
		        	param.setStdNum(Integer.parseInt(txtNumStd.getText()));
		        	param.setCPU(Integer.parseInt(txtCPU.getText()));
		        	
		        	loadCoord();
		        	
	        		TagPileup pile = new TagPileup(COORD, BAMFiles, param);
	        		
	        		pile.addPropertyChangeListener("tag", new PropertyChangeListener() {
					    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
					    	int temp = (Integer) propertyChangeEvent.getNewValue();
					    	int percentComplete = (int)(((double)(temp) / BAMFiles.size()) * 100);
				        	setProgress(percentComplete);
					     }
					 });
	        		
	        		pile.setVisible(true);
					pile.run();

		        	setProgress(100);
		        	return null;
        		}
        	} catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			}
			return null;
        }
        
        public void done() {
    		massXable(contentPane, true);
            setCursor(null); //turn off the wait cursor
        }
	}
	
	public TagPileupWindow() {
		setTitle("Tag Pileup");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 600, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -368, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
		btnLoad = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 11, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				File[] newBAMFiles = getBAMFiles();
				if(newBAMFiles != null) {
					for(int x = 0; x < newBAMFiles.length; x++) { 
						BAMFiles.add(newBAMFiles[x]);
						expList.addElement(newBAMFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);
		
      	expList = new DefaultListModel();
		final JList listExp = new JList(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);
		
		btnRemoveBam = new JButton("Remove BAM");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, btnRemoveBam);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveBam);
		
		btnPileup = new JButton("Pile Tags");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnPileup, 250, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnPileup, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnPileup, -250, SpringLayout.EAST, contentPane);
		contentPane.add(btnPileup);
		
		rdbtnRead1 = new JRadioButton("Read 1");
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnRead1, 89, SpringLayout.WEST, contentPane);
		contentPane.add(rdbtnRead1);
		
		rdbtnRead2 = new JRadioButton("Read 2");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnRead2, 0, SpringLayout.NORTH, rdbtnRead1);
		contentPane.add(rdbtnRead2);
		
		rdbtnCombined = new JRadioButton("Combined");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnCombined, 0, SpringLayout.NORTH, rdbtnRead1);
		sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnCombined, -10, SpringLayout.EAST, contentPane);
		contentPane.add(rdbtnCombined);
		
		ButtonGroup OutputRead = new ButtonGroup();
        OutputRead.add(rdbtnRead1);
        OutputRead.add(rdbtnRead2);
        OutputRead.add(rdbtnCombined);
        rdbtnRead1.setSelected(true);
        
        JLabel lblPleaseSelectWhich = new JLabel("Please Select Which Read to Output:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnRead1, 6, SpringLayout.SOUTH, lblPleaseSelectWhich);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblPleaseSelectWhich, 6, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblPleaseSelectWhich, 0, SpringLayout.WEST, scrollPane);
        lblPleaseSelectWhich.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblPleaseSelectWhich);

        lblDefaultToLocal = new JLabel("Default to Local Directory");
        lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDefaultToLocal, -6, SpringLayout.NORTH, btnPileup);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblDefaultToLocal, -15, SpringLayout.EAST, contentPane);
        lblDefaultToLocal.setBackground(Color.WHITE);
        contentPane.add(lblDefaultToLocal);
        
        lblCurrentOutput = new JLabel("Current Output:");
        sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrentOutput);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, -1, SpringLayout.NORTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, scrollPane);
        lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblCurrentOutput);
		
        btnOutputDirectory = new JButton("Output Directory");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 507, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnOutputDirectory, -225, SpringLayout.EAST, contentPane);
        contentPane.add(btnOutputDirectory);
        
        progressBar = new JProgressBar();
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnCombined, 0, SpringLayout.WEST, progressBar);
        sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, 94, SpringLayout.EAST, btnPileup);
        sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -15, SpringLayout.EAST, contentPane);
        progressBar.setStringPainted(true);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -4, SpringLayout.SOUTH, contentPane);
        contentPane.add(progressBar);
        
        btnPileup.setActionCommand("start");
        
        lblPleaseSelectWhich_1 = new JLabel("Please Select How to Output Strands:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblPleaseSelectWhich_1, 6, SpringLayout.SOUTH, rdbtnRead1);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblPleaseSelectWhich_1, 0, SpringLayout.WEST, scrollPane);
        lblPleaseSelectWhich_1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblPleaseSelectWhich_1);
        
        rdbtnSeperate = new JRadioButton("Seperate");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnSeperate, 6, SpringLayout.SOUTH, lblPleaseSelectWhich_1);
        sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnSeperate, 0, SpringLayout.EAST, lblPleaseSelectWhich);
        rdbtnSeperate.setSelected(true);
        contentPane.add(rdbtnSeperate);
        
        rdbtnComb = new JRadioButton("Combined");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnComb, 0, SpringLayout.NORTH, rdbtnSeperate);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnComb, 89, SpringLayout.EAST, rdbtnSeperate);
        contentPane.add(rdbtnComb);
        
        ButtonGroup ReadStrand = new ButtonGroup();
        ReadStrand.add(rdbtnSeperate);
        ReadStrand.add(rdbtnComb);
        rdbtnSeperate.setSelected(true);
        
        lblTagShift = new JLabel("Tag Shift (bp):");
        sl_contentPane.putConstraint(SpringLayout.WEST, lblTagShift, 0, SpringLayout.WEST, scrollPane);
        lblTagShift.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblTagShift);
        
        txtShift = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtShift, -1, SpringLayout.NORTH, lblTagShift);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtShift, 6, SpringLayout.EAST, lblTagShift);
        txtShift.setHorizontalAlignment(SwingConstants.CENTER);
        txtShift.setText("0");
        contentPane.add(txtShift);
        txtShift.setColumns(10);
        
        lblStdDevSize = new JLabel("Std Dev Size (Bin #):");
        sl_contentPane.putConstraint(SpringLayout.EAST, lblStdDevSize, -307, SpringLayout.EAST, contentPane);
        lblStdDevSize.setEnabled(false);
        lblStdDevSize.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblStdDevSize);
        
        lblNumStd = new JLabel("# of Std Deviations:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblNumStd, 0, SpringLayout.NORTH, lblStdDevSize);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblNumStd, 0, SpringLayout.WEST, rdbtnComb);
        lblNumStd.setEnabled(false);
        lblNumStd.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblNumStd);
        
        JLabel lblBinSizebp = new JLabel("Bin Size (bp):");
        sl_contentPane.putConstraint(SpringLayout.EAST, txtShift, -56, SpringLayout.WEST, lblBinSizebp);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblBinSizebp, 0, SpringLayout.NORTH, lblTagShift);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblBinSizebp, 0, SpringLayout.WEST, btnOutputDirectory);
        lblBinSizebp.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblBinSizebp);
        
        txtBin = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtBin, -1, SpringLayout.NORTH, lblTagShift);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtBin, 6, SpringLayout.EAST, lblBinSizebp);
        txtBin.setText("1");
        txtBin.setHorizontalAlignment(SwingConstants.CENTER);
        txtBin.setColumns(10);
        contentPane.add(txtBin);
        
        txtStdSize = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtStdSize, -1, SpringLayout.NORTH, lblStdDevSize);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtStdSize, 6, SpringLayout.EAST, lblStdDevSize);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtStdSize, -11, SpringLayout.WEST, lblNumStd);
        txtStdSize.setEnabled(false);
        txtStdSize.setHorizontalAlignment(SwingConstants.CENTER);
        txtStdSize.setText("5");
        contentPane.add(txtStdSize);
        txtStdSize.setColumns(10);
        
        txtNumStd = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtNumStd, -1, SpringLayout.NORTH, lblStdDevSize);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtNumStd, 6, SpringLayout.EAST, lblNumStd);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtNumStd, -66, SpringLayout.EAST, contentPane);
        txtNumStd.setEnabled(false);
        txtNumStd.setHorizontalAlignment(SwingConstants.CENTER);
        txtNumStd.setText("3");
        contentPane.add(txtNumStd);
        txtNumStd.setColumns(10);
        
        JButton btnLoadBedFile = new JButton("Load BED File");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBedFile, 10, SpringLayout.WEST, contentPane);
		contentPane.add(btnLoadBedFile);
        
        lblBEDFile = new JLabel("No BED File Loaded");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 14, SpringLayout.SOUTH, lblBEDFile);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadBedFile, -6, SpringLayout.NORTH, lblBEDFile);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblBEDFile, 5, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblBEDFile, 12, SpringLayout.EAST, btnLoadBedFile);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblBEDFile, 0, SpringLayout.EAST, contentPane);
        contentPane.add(lblBEDFile);
        
        chckbxOutputData = new JCheckBox("Output Data");
        sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputData, 15, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxOutputData, -87, SpringLayout.SOUTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnOutputDirectory, 102, SpringLayout.EAST, chckbxOutputData);
        chckbxOutputData.setSelected(true);
        contentPane.add(chckbxOutputData);
        
        rdbtnNone = new JRadioButton("None");
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnNone, 10, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnNone, -149, SpringLayout.SOUTH, contentPane);
        contentPane.add(rdbtnNone);
        
        JLabel lblPleaseSelectComposite = new JLabel("Please Select Composite Transformation:");
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblTagShift, -42, SpringLayout.NORTH, lblPleaseSelectComposite);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblPleaseSelectComposite, 0, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblPleaseSelectComposite, -6, SpringLayout.NORTH, rdbtnNone);
        lblPleaseSelectComposite.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblPleaseSelectComposite);
        
        rdbtnGaussianSmooth = new JRadioButton("Gaussian Smooth");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblStdDevSize, 3, SpringLayout.NORTH, rdbtnGaussianSmooth);
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnGaussianSmooth, 6, SpringLayout.SOUTH, rdbtnNone);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnGaussianSmooth, 0, SpringLayout.WEST, scrollPane);
        contentPane.add(rdbtnGaussianSmooth);
        
        rdbtnSlidingWindow = new JRadioButton("Sliding Window");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnSlidingWindow, 0, SpringLayout.NORTH, rdbtnNone);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnSlidingWindow, 56, SpringLayout.EAST, rdbtnNone);
        contentPane.add(rdbtnSlidingWindow);
               
        ButtonGroup trans = new ButtonGroup();
        trans.add(rdbtnNone);
        trans.add(rdbtnSlidingWindow);
        trans.add(rdbtnGaussianSmooth);
        rdbtnNone.setSelected(true);
        
        lblWindowSizebin = new JLabel("Window Size (Bin #):");
        sl_contentPane.putConstraint(SpringLayout.EAST, lblWindowSizebin, -189, SpringLayout.EAST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnRead2, 0, SpringLayout.WEST, lblWindowSizebin);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblWindowSizebin, 4, SpringLayout.NORTH, rdbtnNone);
        lblWindowSizebin.setEnabled(false);
        lblWindowSizebin.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblWindowSizebin);
        
        txtSmooth = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtSmooth, 2, SpringLayout.NORTH, rdbtnNone);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtSmooth, 6, SpringLayout.EAST, lblWindowSizebin);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtSmooth, -136, SpringLayout.EAST, contentPane);
        txtSmooth.setHorizontalAlignment(SwingConstants.CENTER);
        txtSmooth.setEnabled(false);
        txtSmooth.setText("3");
        contentPane.add(txtSmooth);
        txtSmooth.setColumns(10);
        
        lblCpusToUse = new JLabel("CPU's to Use:");
        sl_contentPane.putConstraint(SpringLayout.EAST, txtBin, -36, SpringLayout.WEST, lblCpusToUse);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblCpusToUse, -104, SpringLayout.EAST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblCpusToUse, 0, SpringLayout.NORTH, lblTagShift);
        lblCpusToUse.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblCpusToUse);
        
        txtCPU = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtCPU, -1, SpringLayout.NORTH, lblTagShift);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtCPU, 6, SpringLayout.EAST, lblCpusToUse);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtCPU, -46, SpringLayout.EAST, contentPane);
        txtCPU.setHorizontalAlignment(SwingConstants.CENTER);
        txtCPU.setText("1");
        contentPane.add(txtCPU);
        txtCPU.setColumns(10);
        
        rdbtnTabdelimited = new JRadioButton("TAB-Delimited");
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnTabdelimited, 0, SpringLayout.WEST, rdbtnRead2);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnTabdelimited, -11, SpringLayout.NORTH, lblDefaultToLocal);
        contentPane.add(rdbtnTabdelimited);
        rdbtnCdt = new JRadioButton("CDT");
        sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnCdt, -11, SpringLayout.NORTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnCdt, -104, SpringLayout.EAST, contentPane);
        contentPane.add(rdbtnCdt);
        
        ButtonGroup output = new ButtonGroup();
        output.add(rdbtnTabdelimited);
        output.add(rdbtnCdt);
        rdbtnTabdelimited.setSelected(true);
        
        lblPleaseSelectOutput = new JLabel("Please select Output File Format:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblPleaseSelectOutput, 3, SpringLayout.NORTH, rdbtnTabdelimited);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblPleaseSelectOutput, 0, SpringLayout.WEST, scrollPane);
        lblPleaseSelectOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblPleaseSelectOutput);
        
        chckbxTagStandard = new JCheckBox("Set Tags to Be Equal");
        sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxTagStandard, -6, SpringLayout.NORTH, lblPleaseSelectComposite);
        sl_contentPane.putConstraint(SpringLayout.EAST, chckbxTagStandard, 0, SpringLayout.EAST, btnOutputDirectory);
        contentPane.add(chckbxTagStandard);
        
        rdbtnNone.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		    	  if(rdbtnNone.isSelected()) {
		    		  lblWindowSizebin.setEnabled(false);
		    		  lblStdDevSize.setEnabled(false);
		    		  lblNumStd.setEnabled(false);
		    		  txtSmooth.setEnabled(false);
		    		  txtStdSize.setEnabled(false);
		    		  txtNumStd.setEnabled(false);
		    	  }
		      }
        });
        rdbtnSlidingWindow.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		    	  if(rdbtnSlidingWindow.isSelected()) {
		    		  lblWindowSizebin.setEnabled(true);
		    		  lblStdDevSize.setEnabled(false);
		    		  lblNumStd.setEnabled(false);
		    		  txtSmooth.setEnabled(true);
		    		  txtStdSize.setEnabled(false);
		    		  txtNumStd.setEnabled(false);  		  
		    	  }
		      }
        });
        rdbtnGaussianSmooth.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		    	  if(rdbtnGaussianSmooth.isSelected()) {
		    		  lblWindowSizebin.setEnabled(false);
		    		  lblStdDevSize.setEnabled(true);
		    		  lblNumStd.setEnabled(true);
		    		  txtSmooth.setEnabled(false);
		    		  txtStdSize.setEnabled(true);
		    		  txtNumStd.setEnabled(true); 		  
		    	  }
		      }
        });;
        
        chckbxOutputData.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
			        if(chckbxOutputData.isSelected()) {
			        	btnOutputDirectory.setEnabled(true);
			        	lblDefaultToLocal.setEnabled(true);
			        	lblCurrentOutput.setEnabled(true);
			        	lblPleaseSelectOutput.setEnabled(true);
						rdbtnTabdelimited.setEnabled(true);
						rdbtnCdt.setEnabled(true);

			        } else {
			        	btnOutputDirectory.setEnabled(false);
			        	lblDefaultToLocal.setEnabled(false);
			        	lblCurrentOutput.setEnabled(false);
			        	lblPleaseSelectOutput.setEnabled(false);
						rdbtnTabdelimited.setEnabled(false);
						rdbtnCdt.setEnabled(false);
			        }
			      }
			    });
        
        btnLoadBedFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				File temp = getBEDFile();
				if(temp != null) {
					INPUT = temp;
					lblBEDFile.setText(INPUT.getName());
				}
			}
		});   
              
        btnOutputDirectory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				OUTPUT = getOutputDir();
				if(OUTPUT != null) {
					lblDefaultToLocal.setText(OUTPUT.getAbsolutePath());
				}
			}
		});
        
        btnPileup.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		massXable(contentPane, false);
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
	}
	
	public void massXable(Container con, boolean status) {
		Component[] components = con.getComponents();
		for (Component component : components) {
			component.setEnabled(status);
			if (component instanceof Container) {
				massXable((Container)component, status);
			}
		}
		if(status) {
			if(rdbtnNone.isSelected()) {
				lblWindowSizebin.setEnabled(false);
	    		lblStdDevSize.setEnabled(false);
	    		lblNumStd.setEnabled(false);
	    		txtSmooth.setEnabled(false);
	    		txtStdSize.setEnabled(false);
	    		txtNumStd.setEnabled(false);
			}
			if(rdbtnGaussianSmooth.isSelected()) {
				lblWindowSizebin.setEnabled(false);
	    		lblStdDevSize.setEnabled(true);
	    		lblNumStd.setEnabled(true);
	    		txtSmooth.setEnabled(false);
	    		txtStdSize.setEnabled(true);
	    		txtNumStd.setEnabled(true);
			}
			if(rdbtnSlidingWindow.isSelected()) {
				lblWindowSizebin.setEnabled(true);
	    		lblStdDevSize.setEnabled(false);
	    		lblNumStd.setEnabled(false);
	    		txtSmooth.setEnabled(true);
	    		txtStdSize.setEnabled(false);
	    		txtNumStd.setEnabled(false);
			}
			if(!chckbxOutputData.isSelected()) {
				btnOutputDirectory.setEnabled(false);
				lblPleaseSelectOutput.setEnabled(false);
				lblCurrentOutput.setEnabled(false);
				lblDefaultToLocal.setEnabled(false);
				rdbtnTabdelimited.setEnabled(false);
				rdbtnCdt.setEnabled(false);
			}
		}
	}
	
	/**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
	
    public void loadCoord() throws FileNotFoundException {
		Scanner scan = new Scanner(INPUT);
		COORD = new Vector<BEDCoord>();
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if(temp.length > 2) { 
				if(!temp[0].contains("track") && !temp[0].contains("#")) {
					String name = "";
					if(temp.length > 3) { name = temp[3]; }
					else { name = temp[0] + "_" + temp[1] + "_" + temp[2]; }
					if(Integer.parseInt(temp[1]) >= 0) {
						if(temp[5].equals("+")) { COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "+", name)); }
						else { COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "-", name)); }
					} else {
						System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
		}
		scan.close();
    }
    
	public File[] getBAMFiles() {
		fc.setDialogTitle("BAM File Selection");
		fc.setFileFilter(new BAMFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		
		File[] bamFiles = null;
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			bamFiles = fc.getSelectedFiles();
		}
		return bamFiles;
	}
	
	public File getBEDFile() {
		fc.setDialogTitle("BED File Selection");
		fc.setFileFilter(new BEDFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		
		File bedFile = null;
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			bedFile = fc.getSelectedFile();
		}
		return bedFile;
	}
	
	public File getOutputDir() {
		fc.setDialogTitle("Output Directory");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		
		if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) { 
			return fc.getSelectedFile();
		} else {
			return null;
		}	
	}
}


	