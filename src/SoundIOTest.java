import AcousticNetwork.SoundIO;

class SoundIOTest implements Runnable{

	private double[] wave;
	private SoundIO io;

	public SoundIOTest() throws Exception { 
		io = new SoundIO(); 
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
			wave[i] = 
				0.5*(Math.sin(2*Math.PI*1000*t) + Math.sin(2*Math.PI*10000*t));
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
		io.sound(wave);
	}

	private void testSaveFile() throws Exception{
		double[] wave = testReadFile();
		io.save_file(wave, "./test.wav");
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
			test.testRecordAndPlayback(10);
		} else if (args[0].equals("--save")){
			test.testSaveFile();
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