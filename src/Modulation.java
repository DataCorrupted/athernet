/* Description: A PSK modulation method
   TODO;    Add sync head to the output
 */

class Modulation{
    private double[] carrier_;
    private double unit_duration_;
    private double sample_rate_;

    /*
        unit_duration: time duration for each bit (normally 1s)
        sample_rate: the sample rate in HZ
     */
    double[] Modulation(double unit_duration, double sample_rate){
        carrier_ = new double[unit_duration * sample_rate];
        // generate one standard frame unit
        // the carrier is generated in 10kHZ
        for (int i = 0; i < frame_unit_.length; i++){
            carrier_[i] = Math.sin( 2 * Math.pi * 10000 * i/sample_rate);
        }
    }

    // given a buffer, return its modulated signal
    public double[] modulate(int[] frame_in){
        double[] output_frame = double(frame_in.length * (unit_duration_ *sample_rate_));
        for (int i = 0 ; i < frame_in.length; i++){
            // convert each bits to a frame
            for (int j = 0; j < carrier_.length; j++){
                // phrase modulation
                output_frame[j + i * carrier_.length] = carrier_[j] * (frame_in[i] * 2 - 1);
            }
        }
        return frame_in;
    }
}