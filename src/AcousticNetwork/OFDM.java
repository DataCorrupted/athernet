package AcousticNetwork;

class OFDM{
	private int channel_cnt_;
	private double bandwidth_;
	private double freq_;
	private double[] freq_arr_;

	public final double MINFREQ = 20;
	public final double MAXFREQ = 20000;

	public OFDM(double f, double b, double c){
		freq_ = f;
		bandwidth_ = b;
		channel_cnt_ = c;
		if (c & 0x1 == 1){
			System.out.println("Warn[OFDM.OFDM(double, double, double)]: odd channel count given.");
		}
		if (f+b/2 > MAXFREQ || f-b/2 < MINFREQ){
			System.out.println("Warn[OFDM.OFDM(double, double, double)]: illegal bandwidth and frequency given.");
		} 
		freq_arr_ = new double[channel_cnt_];
		for(int i=0; i<channel_cnt_; i++){
			//;
		}
	}
}