package athernet.mac;

import athernet.util.FileI;
import athernet.util.FileO;
import athernet.util.CheckIO;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

class TestMacLayer{

	private static final byte src_addr = 0x1;
	private static final byte dst_addr = 0x2;

	private static double tic;
	private static double toc;

	public static void main(String[] args) throws Exception{
		if (args.length == 0){
			System.err.println("No parameter given.");
		} else if (args[0].equals("--transmit-test")) {
			transmit_test();
		} else if (args[0].equals("--receive-test")) {
			receive_test();
		} else if (args[0].equals("--transmit-file")){
			transmit_file();
		} else if (args[0].equals("--receive-file")){
			receive_file();
		} else if (args[0].equals("--csma-test")) {
			if (args.length < 2) {
				System.err.println("A TEXT01 formatted file is required to test CSMA. ");
			} else if (args.length < 3) {
				System.err.println("You need to specify who is client.");
			} else { 
				csma_test(args[1], args[2].equals("--client") || args[2].equals("-C")); 
			}
		} else {
			System.err.println("No such option.");
		}
	}
	private static final String test_str1_ = "Hello world. ";
	private static final String test_str2_ = "I just sent 4 floating length packages,";
	private static final String test_str3_ = " including 2 sentences and a signature.";
	private static final String test_str4_ = " --P.R.";

	private static final String test_str_ = test_str1_ + test_str2_ + test_str3_ + test_str4_;

	public static void receive_test() throws Exception{
		MacLayer mac_layer = new MacLayer(dst_addr, src_addr);
		mac_layer.startMacLayer();

		// Header first.
		MacPacket mac_pack = mac_layer.getOnePack();
		double tic = System.nanoTime() / 1e9;
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

		int total_len = 0;
		while (total_len < length){
			mac_pack = mac_layer.getOnePack();
			byte[] chunk = mac_pack.getData();
			// This shouldn't cause overflow error. 
			// But if so, let it be, so we can debug easier.
			System.arraycopy(chunk, 0, data, total_len, chunk.length);
			total_len += chunk.length;
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
	public static void transmit_test() throws Exception{
		final byte[] data1 = test_str1_.getBytes();
		final byte[] data2 = test_str2_.getBytes();
		final byte[] data3 = test_str3_.getBytes();
		final byte[] data4 = test_str4_.getBytes();

		final int pack_cnt = 4;
		final int data_length = 98;

		MacLayer mac_layer = new MacLayer(src_addr, dst_addr);

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

		mac_layer.requestSend(data1);
		mac_layer.requestSend(data2);
		mac_layer.requestSend(data3);
		mac_layer.requestSend(data4);

		Thread.sleep(3000);

		mac_layer.stopMacLayer();
	}
	public static void transmit_file(String file, MacLayer mac_layer) throws Exception{
		// Prepare file.
		FileI i_file_ = new FileI(file, FileI.TEXT01);
		int total_size = i_file_.getSize();

		tic = System.nanoTime() / 1e9;
		// Make sure that the init pack is sent.
		MacPacket init_pack 
			= new MacPacket(dst_addr, src_addr, total_size);
		mac_layer.requestSend(init_pack);
		while (init_pack.getStatus() != MacPacket.STATUS_ACKED) {
			if (mac_layer.getStatus() == MacLayer.LINKERR) {
				System.err.println("Link Error!");
				mac_layer.stopMacLayer();
				return;
			}
			Thread.sleep(20);
		}

		int pack_size = 125;
		byte[] out_data = new byte[pack_size];
		int r = 0;
		short pack_cnt = 0;

		r = i_file_.read(out_data, 0, pack_size);
		while (r != -1){
			// Send it.
			mac_layer.requestSend(out_data);
			pack_cnt ++;
			// Read next bunch of data.
			r = i_file_.read(out_data, 0, pack_size);
			Thread.sleep(20);
		}
		while (!mac_layer.isIdle()){ 
			Thread.sleep(100); 
			if (mac_layer.getStatus() == MacLayer.LINKERR){
				System.err.println("Link Error!");
				break;
			}
		}
		
		toc = System.nanoTime() / 1e9;
	}
	public static void transmit_file() throws Exception{
		// Start the mac layer.
		MacLayer mac_layer = new MacLayer(src_addr, dst_addr, 25);
		mac_layer.startMacLayer();

		transmit_file("./I", mac_layer);

		mac_layer.stopMacLayer();
		System.out.println("Time used for transmition: " + (toc - tic));
	}

	public static byte[] receive_file(MacLayer mac_layer) throws Exception{		
		// Receive head length.
		MacPacket mac_pack = mac_layer.getOnePack();
		tic = System.nanoTime() / 1e9;
		if (mac_pack.getType() != MacPacket.TYPE_INIT){
			System.err.println("Error, no init received.");
			return new byte[0];
		}
		int length = mac_pack.getTotalLength();
		System.out.printf(
			"Received sending request for %d bytes.\n", 
			length);
		// Receive each and every chunk of data.
		byte[] data = new byte[length];
		int total_len = 0;
		int pack_cnt = 0;
		while (total_len < length){
			mac_pack = mac_layer.getOnePack();
			byte[] chunk = mac_pack.getData();
			total_len += chunk.length;
			// This shouldn't cause overflow error. 
			// But if so, let it be, so we can debug easier.
			if (((pack_cnt+1) * chunk.length) < length){
				System.arraycopy(chunk, 0, data, pack_cnt*chunk.length, chunk.length);
			} else {
				// That the data is full yet more is transfered, then we ignore the rest.
				System.arraycopy(
					chunk, 0, data, pack_cnt*chunk.length, length - pack_cnt * chunk.length);
			}
			pack_cnt += 1;
		}
		toc = System.nanoTime() / 1e9;
		
		return data;
	}
	private static byte[] data = new byte[0];
	public static void receive_file() throws Exception{
		MacLayer mac_layer = new MacLayer(dst_addr, src_addr, 25);
		mac_layer.startMacLayer();
		
		data = receive_file(mac_layer);

		mac_layer.stopMacLayer();
		System.out.println("Time used for transmition: " + (toc - tic));
		saveAndCheckData();	
	}
	public static void csma_test(String file, boolean is_client) throws Exception{
		MacLayer mac_layer;
		if (is_client){
			mac_layer = new MacLayer(src_addr, dst_addr);
		} else {
			mac_layer = new MacLayer(dst_addr, src_addr);
		}
		
		Thread receive_thread = new Thread(new Runnable(){
			public void run(){
				try { data = receive_file(mac_layer); } catch (Exception e) { ; }
			}
		});
		Thread transmit_thread = new Thread(new Runnable(){
			public void run() { 
				try { transmit_file(file, mac_layer); } catch (Exception e) { ; }
			}
		});

		// Turn on csma.
		mac_layer.turnCSMA();
		mac_layer.startMacLayer();

		double tic = System.nanoTime() / 1e9;

		receive_thread.start();
		System.out.println("Receive thread started.");
		// Wait for the other side to start 
		Thread.sleep(1000);
		transmit_thread.start();
		System.out.println("Transmit thread started.");

		transmit_thread.join();
		System.out.println("Receive thread stoped.");
		receive_thread.join();
		System.out.println("Transmit thread stopped.");
		
		double toc = System.nanoTime() / 1e9;

		mac_layer.stopMacLayer();

		System.out.println("Time used for transmition: " + (toc - tic));
		saveAndCheckData();
	}

	private static void saveAndCheckData() throws Exception{
		// Received nothing.
		if (data.length == 0){ 
			System.err.println("Received nothing.");
			return; 
		}
		// Write the file and check correctness.
		FileO o_file = new FileO("./O", FileO.TEXT01);
		o_file.write(data, 0, data.length);
		CheckIO checker = new CheckIO();
		System.out.println(checker.summary());	
	}
}