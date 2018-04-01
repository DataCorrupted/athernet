package AcousticNetwork;
import AcousticNetwork.FileI;
import AcousticNetwork.FileO;

import java.util.ArrayList;

public class CheckIO{
	private FileI i_file_;
	private FileI o_file_;
	private byte[] ground_truth_;
	private byte[] reality_;
	private ArrayList<Integer> diff_list_;
	private final int MAXLEN = 56;

	public CheckIO() throws Exception{
		this("./I", "./O");
	}
	public CheckIO(String i, String o) throws Exception{
		i_file_ = new FileI(i, FileI.BIN);
		o_file_ = new FileI(o, FileO.BIN);
		diff_list_ = new ArrayList<Integer>();
		updateFiles();
	}
	public void updateFiles() throws Exception{
		diff_list_.clear();
		ground_truth_ = i_file_.readAllData();
		reality_ = o_file_.readAllData();
	}
	private int largeSize() { return Math.max(ground_truth_.length, reality_.length); }
	private int smallSize() { return Math.min(ground_truth_.length, reality_.length); }
	public boolean isSameLength(){ return largeSize() == smallSize();}
	public double accuracy(){ return (1 - ((double)countDiff()) / ((double)smallSize())); }
	private int countDiff(){
		for (int i=0; i<smallSize(); i++){
			if (ground_truth_[i] != reality_[i]){
				diff_list_.add(i);
			}
		}
		return diff_list_.size();
	}
	public String genGram(){ return genGram(MAXLEN); }
	public String genGram(int length){
		if (length > MAXLEN) { length = MAXLEN;}
		String gram = "    A1234567B1234567C1234567D1234567E1234567F1234567G1234567";
		int k = 0;
		int j = 0;
		for (int i = 0; i<largeSize(); i++){
			if (i % length == 0){
				gram = gram + "\n" + String.format("%03d ", j);
				j++;
			}
			if (k < diff_list_.size() && diff_list_.get(k) == i){
				gram = gram + "x";
				k ++;
			} else if (i > smallSize()){
				gram = gram + "l";
			} else {
				gram = gram + " ";
			}
		}
		return gram + "\n";
	}
	public String summary(){
		String s = new String();
		s = s + "Summary: \n";
		s = s + "The length of two files are " + ((isSameLength())? "the same": "differnet")  + "\n";
		s = s + "The accuracy is of " + accuracy() + "\n";
		s = s + "Below is a detailed gram of where they are differnet: \n";
		s = s + genGram();
		if (countDiff() == 0){
			s = s + "Congratulations! Your packet is safe and SOUND.";
		}
		return s;
	}
	public static void main(String args[]) throws Exception{
		int i=0;
		String i_file = "./I";
		String o_file = "./O";
		while (i<args.length){
			if (args[i].equals("-i")){
				i++;
				if (i==args.length){
					System.out.println("No file given. Quit.");
				}
				i_file = args[i];
			} else if (args[i].equals("-o")){
				i++;
				if (i==args.length){
					System.out.println("No file given. Quit.");
				}
				o_file = args[i];
			} else {
				System.out.println("No such command.\n -i <ground-truth>\n -o <your-file>");
			}
			i++;
		}
		CheckIO c = new CheckIO(i_file, o_file);
		System.out.println(c.summary());
	}
}