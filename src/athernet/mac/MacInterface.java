package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

class MacReceiver{
	static public void main(String[] args){
		if (args.length == 0){
			System.out.println("No input. You have to give me something!");
		} else {
			for (int i=0; i<args.length; i++){
				System.out.print(args[i] + " ");
			}
			System.out.println();
		}
	}
}