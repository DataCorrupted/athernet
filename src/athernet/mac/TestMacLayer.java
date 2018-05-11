package athernet.mac;

import athernet.util.FileI;
import athernet.util.FileO;
import athernet.util.CheckIO;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

class TestMacLayer{

	private static final byte src_addr = 0x1;
	private static final byte dst_addr = 0x2;

	public static void main(String[] args) throws Exception{
		if (args.length == 0){
			System.err.println("No parameter given.");
		} else if (args[0].equals("--test-send")) {
			test_send();
		} else if (args[0].equals("--test-receive")) {
			test_receive();
//		} else if (args[0].equals("--transmit-file")){
//			transmit_file();
//		} else if (args[0].equals("--receive-file")){
//			receive_file();
		}
	}
	private static final String test_str1_ = "Hello world. ";
	private static final String test_str2_ = "I just sent 4 floating length packages,";
	private static final String test_str3_ = " including 2 sentences and a signature.";
	private static final String test_str4_ = " --P.R.";

	private static final String test_str_ = test_str1_ + test_str2_ + test_str3_ + test_str4_;

	public static void test_receive() throws Exception{
		MacLayer mac_layer = new MacLayer(dst_addr);
		mac_layer.startMacLayer();

		double tic = System.nanoTime() / 1e9;

		// Header first.
		MacPacket mac_pack = mac_layer.getOnePack();
		if (mac_pack.getType() != MacPacket.TYPE_INIT){
			System.err.println("Error, no init received.");
			return;
		}
		int length = mac_pack.getTotalLength();
		System.out.printf(
			"Received sending request for %d bytes.\n", 
			length);

		int pack_cnt = 4;
		byte[] data = new byte[length];
		for (int i=0; i<pack_cnt; i++){
			mac_pack = mac_layer.getOnePack();
			int offset = mac_pack.getOffset();
			byte[] chunk = mac_pack.getData();

			// This shouldn't cause overflow error. 
			// But if so, let it be, so we can debug easier.
			System.arraycopy(chunk, 0, data, offset, chunk.length);
		}

		double toc = System.nanoTime() / 1e9;

		String received_str = new String(data);
		System.out.println(
			"Receiving completed. You should receive the following sentence: \n");
		System.out.println(test_str_);
		System.out.println("\nYou received: \n");
		System.out.println(received_str + "\n");

		Thread.sleep(3000);
		System.out.printf("Transmition took: %3.3fs\n", (toc - tic));
		mac_layer.stopMacLayer();
	}
	public static void test_send() throws Exception{
		final byte[] data1 = test_str1_.getBytes();
		final byte[] data2 = test_str2_.getBytes();
		final byte[] data3 = test_str3_.getBytes();
		final byte[] data4 = test_str4_.getBytes();

		final int pack_cnt = 4;
		final int data_length = 98;

		MacLayer mac_layer = new MacLayer(src_addr);

		mac_layer.startMacLayer();
		
		// Make sure that init is recived.
		MacPacket init_pack 
			= new MacPacket(dst_addr, src_addr, data_length);
		mac_layer.requestSend(init_pack);
		while (init_pack.getStatus() != MacPacket.STATUS_ACKED) {
			if (mac_layer.getStatus() == MacLayer.LINKERR) {
				System.err.println("Link Error!");
				mac_layer.stopMacLayer();
				return;
			}
			Thread.sleep(20);
		}

		mac_layer.requestSend(dst_addr, 0, data1);
		mac_layer.requestSend(dst_addr, 13, data2);
		mac_layer.requestSend(dst_addr, 52, data3);
		mac_layer.requestSend(dst_addr, 91, data4);

		Thread.sleep(3000);

		mac_layer.stopMacLayer();
	}
/*
	public static void transmit_file() throws Exception{

		// Start the mac layer.
		MacLayer mac_layer = new MacLayer(src_addr);
		mac_layer.startMacLayer();

		double start_time = System.nanoTime() / 1e9;

		FileI i_file_ = new FileI("./I", FileI.TEXT01);
		int total_size = i_file_.getSize();

		// Make sure that the init pack is sent.
		MacPacket init_pack 
			= new MacPacket(dst_addr, src_addr, 0, 0);
		mac_layer.requestSend(init_pack);
		while (init_pack.getStatus() != MacPacket.STATUS_ACKED) {
			if (mac_layer.getStatus() == MacLayer.LINKERR) {
				System.err.println("Link Error!");
				mac_layer.stopMacLayer();
				return;
			}
			Thread.sleep(20);
		}

		int pack_size = 128;
		byte[] out_data = new byte[pack_size];
		int r = 0;
		short pack_cnt = 0;

		while (r != -1){
			// Read next bunch of data.
			r = i_file_.read(out_data, 0, pack_size);
			// Send it.
			mac_layer.requestSend(dst_addr, pack_cnt*pack_size, out_data);
			pack_cnt ++;
		}

		double end_time = System.nanoTime() / 1e9;
		System.out.println("Time used for transmition: " + (end_time - start_time));
		// Remember to stop it.
		mac_layer.stopMacLayer();
	}

	public static void receive_file() throws Exception{
		MacLayer mac_layer = new MacLayer(dst_addr);
		mac_layer.startMacLayer();

		// Receive head length.
		MacPacket mac_pack = mac_layer.getOnePack();
		if (mac_pack.getType() != MacPacket.TYPE_INIT){
			System.err.println("Error, no init received.");
			return;
		}
		int length = mac_pack.getTotalLength();
		int pack_cnt = mac_pack.getTotalPack();
		System.out.printf(
			"Received sending request for %d bytes.\n", 
			length);

		// Receive each and every chunk of data.
		byte[] data = new byte[length];
		for (int i=0; i<pack_cnt; i++){
			mac_pack = mac_layer.getOnePack();
			int offset = mac_pack.getOffset();
			byte[] chunk = mac_pack.getData();
			System.arraycopy(chunk, 0, data, offset, chunk.length);
		}

		// Write the file and check correctness.
		FileO o_file = new FileO("./O", FileO.TEXT01);
		o_file.write(data, 0, data.length);
		CheckIO checker = new CheckIO();
		System.out.println(checker.summary());		

		mac_layer.stopMacLayer();
	}
*/

}