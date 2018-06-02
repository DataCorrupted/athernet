package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;
import java.io.*;

class MacInterface{
	static public void main(String[] args) throws Exception{
		if (args.length == 0){
			System.out.println("No input. You have to give me something!");
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			line = in.readLine();
			System.err.println("I am Java, I received a str from cpp:\n  " + line);
			for (int i=0; i<args.length; i++){
				System.out.print(args[i] + " ");
			}
			System.out.println();
		}
	}
}