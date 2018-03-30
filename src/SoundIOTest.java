import AcousticNetwork.SoundIO;
import java.util.concurrent.ArrayBlockingQueue;

class SoundIOTest implements Runnable{

	private double[] wave;
	private SoundIO io;
	private ArrayBlockingQueue<Double> double_buf_;

	public SoundIOTest() throws Exception {
		double_buf_ = new ArrayBlockingQueue<Double>(300000); 
		io = new SoundIO(44100, double_buf_); 
		wave = new double[220000];
	}
	
	public void run(){
		try{
			wave = io.record(10.0);
		} catch (Exception e) {
			;
		}
	}

	private void testSin() throws Exception{
		double dur = 5;
		int sample_rate = 44100;
		int sample_cnt = (int) (dur * sample_rate);
		double[] wave = new double[sample_cnt];
		for (int i=0; i<sample_cnt; i++){
			double t = (double) i / sample_rate;
			// Weighted mean for better frequency responce.
			wave[i] = 
				0.3*Math.sin(2*Math.PI*1000*t) + 0.7*Math.sin(2*Math.PI*10000*t);
		}		
		io.sound(wave);
	}

	private double[] testReadFile() throws Exception{
		String path = "../wav/record1_41.wav";
		double[] wave = io.read_file(path);
		io.sound(wave);
		return wave;
	}

	private void testRecordAndPlayback(double dur) throws Exception{
		double[] wave = io.record(dur);
		io.save_file(wave, "./test.wav");
		io.sound(wave);
	}

	private void testSaveFile() throws Exception{
		double[] wave = testReadFile();
		io.save_file(wave, "./test.wav");
	}

	private void testConcurrent() throws Exception{
		Thread r = new Thread(io);
		r.start();
		// Main thread sleep for 5 second.
		Thread.sleep(2000);
//		Learn this. It is interesting.
//		Thread w = new Thread( new Runnable(){
//			public void run(){ 	io.sound(wave); }
//		})
		for (int i=0; i<wave.length; i++){
			// Wait for data.
			wave[i] = double_buf_.take();
		}
		io.sound(wave);
		io.stopConcurrentReadThread();
		r.join();
	}
	// Try modify this to test.
	public static void main(String[] args) throws Exception{
		SoundIOTest test = new SoundIOTest();
		if (args.length == 0){
			System.out.println("Please specify a function you want to test:\n" +
							" --sin: play a sin wave for 5s \n" +
							" --read: read from a wav file \n" +
							" --record: record for 10 seconds and playback \n" +
							" --save: save a file \n" +
							" --both: play a file while recording and then play back");
		} else if (args[0].equals("--sin")){
			test.testSin();
		} else if (args[0].equals("--read")){
			test.testReadFile();
		} else if (args[0].equals("--record")){
			double dur;
			if (args.length == 2){
				dur = Double.valueOf(args[1]).doubleValue();
			} else {
				dur = 10.0;
			}
			test.testRecordAndPlayback(dur);
		} else if (args[0].equals("--save")){
			test.testSaveFile();
		} else if (args[0].equals("--concurrent")) {
			test.testConcurrent();
		} else if (args[0].equals("--both")){
			Thread t = new Thread(test);

			t.start();
			test.testReadFile();
			t.join();
			test.io.save_file(test.wave, "./test.wav");
			test.io.sound(test.wave);
		} else {
			System.out.println("No such test.");
		}
	}
}