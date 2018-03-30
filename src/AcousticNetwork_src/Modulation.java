/* Description: A PSK modulation method */
package AcousticNetwork;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.DoubleStream;

/*
Implementation note:
    state_:     0       =>  initial receiving state (identify the sync header)
                1       =>  possible header identified, waiting for double confirmation (peak following?)
                2       =>  entering receiving state
    count_down_:        How many bits to wait till the confirmation of a sync_header
 */

public class Modulation{
    public static final int NOTHING = 0;
    public static final int RCVEDDAT = -1;
    public static final int RCVINGDAT = 2;
    public static final int CNFIRMING = 1;

    // hyper-parameters
    private int init_count_down_;            // The waiting windows for identifying the header

    // need to set in constructor
    private int bit_length_;            // the duration for a bit (in sample points, usually 44)
    private int header_length_;         // the length of the header (in samples points, usually 440)
    private int sample_rate_;           // usually 44100 Hz
    private int max_frame_length_;      // the length of a frame (in bytes), excluding the header

    private double[] carrier_;          // the carrier signal
    private double[] sync_header_;      // the sync header (for sync)

    // for debug only
    public List<Double> sync_power_debug;

    // for demodulation only
    private List<Double> processing_header_;               // used in identifying the header
    private List<Double> processing_data_;                 // used in receiving a single frame
    private List<Double> unconfirmed_data_;                // the buffer for unconfirmed data (when determining)
    private int state_;                                     // indicate whether the current data bit is part of the data
    private int count_down_;
    private double header_score_;
    private byte[] packet_;

    private double power_energy;
    private int bit_counter_ = 0;

    // dummy sin wave
    private double[] dummy_sin_wave_;
    private int dummy_sin_length_;

    public int getBitLength() { return bit_length_; }
    public int getHeaderLength() { return header_length_; }
    public int getDataLength() { return processing_data_.size(); }
    public double getHeaderScore() { return header_score_;}

    /* Someone want to overload this */
    public Modulation(int sample_rate){
        this(44,  440, sample_rate, 1100/8, 200, 10000, 88);
    }

    /*
        Params:
            carrier_freq: the frequency for carrier (usually 10^3Hz)
            dummy_length: in sample points (usually 100)
     */
    public Modulation(int bit_length, int header_length, int sample_rate, int max_frame_length, int init_count_down,
                      int carrier_freq, int dummy_sin_length){
        state_ = 0;
        power_energy = 0;
        header_score_ = 0;

        // save all parameters
        bit_length_ = bit_length;
        header_length_ = header_length;
        sample_rate_ = sample_rate;
        max_frame_length_ = max_frame_length;
        dummy_sin_length_ = dummy_sin_length;

        // count down related
        init_count_down_ = init_count_down;
        count_down_ = init_count_down_;

        // initialize
        processing_header_ = new ArrayList<>();
        processing_data_ = new ArrayList<>();
        unconfirmed_data_ = new ArrayList<>();
        sync_power_debug = new ArrayList<>();
        packet_ = new byte[0];

        // generate one standard frame unit (the waveform for max_frame_length)
        carrier_ = new double[bit_length * max_frame_length * 8];

        // the carrier is generated in 10kHZ
        for (int i = 0; i < carrier_.length; i++){
            carrier_[i] = Math.sin( 2 * Math.PI * carrier_freq * i/sample_rate);
        }

        // generate the sync_header
        generate_header();

        // generate dummy sin wave
        generate_dummy_sin();
    }

    // given a frame array (bits), return its modulated signal
    // 1 is the same as carrier, while 0 is -phrase
    public double[] modulate(byte[] frame_bytes){
        boolean[] frame_booleans = convert_bytes_to_booleans(frame_bytes);
        double[] output_data = new double[frame_booleans.length * bit_length_];

        int counter = 0;


        for (int i = 0 ; i < frame_booleans.length; i++){
            // convert each bits to a waveform
            int phrase = 1;
            if (!frame_booleans[i])          phrase = -1;
            for (int j = 0; j < bit_length_; j++){
                output_data[counter] = carrier_[bit_length_ * i + j] * phrase;
                counter ++;
            }
        }

        // add the sync header part to the frame and return modulated signal
        double[] output_frame = DoubleStream.concat(Arrays.stream(sync_header_),Arrays.stream(output_data)).toArray();

        // add a dummy sin wave to available
        double[] output_frame_with_dummy = DoubleStream.concat(Arrays.stream(dummy_sin_wave_),Arrays.stream(output_frame)).toArray();
        return output_frame_with_dummy;
        //return output_frame;
    }

    /*
    Params:
        sample: an single sample from SoundIO
        expected_frame_length: (in bytes)
            the frame length to decode after catching a header.
            When this one is set to 0, it means infinity. However, that function will not be supported now.
            TODO: support expected_frame_length infinity.
    Return value:
        True: when a complete datapacket is ready.
        False: Otherwise
    */
    public int demodulation(double sample, int expected_length){
        // given a recved signal. Demodulate it.
        // Whether current signals are data or just nothing important.
        bit_counter_ ++;

        // calculate the power
        power_energy = power_energy * (1-1.0/64.0) + (sample * sample) / 64;

        if (state_ == 0){
            // identify the header
            if (processing_header_.size() == header_length_){
                // if header is already full, remove the first one
                processing_header_.remove(0);
            }
            processing_header_.add(sample);
            // skip the following check as this would be checked in check_sync_header and good for storing debug info.
            /*if (processing_header_.size() < header_length_){
                return NOTHING;               // return when not adequate data
            }
            */
            if (check_sync_header()){
                state_ ++;                  // next state
//                System.out.println("sync_header check passed once, entering confirming state. at bit: " + bit_counter_);
                return CNFIRMING;
            }
            return NOTHING;
        }
        else if (state_ == 1){
            // waiting for the counter down to be 0, make sure the sync_header is the local maximum
            // add the data into unconfirmed buffer, and recheck the header value
            unconfirmed_data_.add(sample);
            processing_header_.remove(0);
            processing_header_.add(sample);
            count_down_  = count_down_ - 1;

            // call for recheck
            if (check_sync_header()){
//                System.out.println("Header reconfirmed at bit: " + bit_counter_);
                unconfirmed_data_.clear();
                // TODO: This was wrong...
                /*
                while (unconfirmed_data_.size() > 0){
                    processing_header_.remove(0);
                    processing_header_.add(unconfirmed_data_.get(0));
                    unconfirmed_data_.remove(0);
                }
                */
                count_down_ = init_count_down_;
            }

            // if count_down completed, turn the unconfirmed data into actual data
            if (count_down_ == 0){
                // convert data
                while(unconfirmed_data_.size()>0){
                    processing_data_.add(unconfirmed_data_.get(0));
                    unconfirmed_data_.remove(0);
                }

                // reset initial value
                count_down_ = init_count_down_;
                header_score_ = 0;
                processing_header_.clear();

                // change state
//                System.out.println("Header confirmed, goto receiving data");
                state_ ++;
            }
            return (state_ == CNFIRMING)? CNFIRMING: RCVINGDAT;
        }
        else if (state_ == 2){
            // add the data to the buffer
            processing_data_.add(sample);
            if (processing_data_.size() < expected_length * 8 * bit_length_) {
                return RCVINGDAT;               // not enough data to decode
            }

//            System.out.println("Get all data, start demodulation");
            // decode the waveform to get the bits
            // process and clear the buffer
            state_ = 0;
            // TODO: convert double queue buffer to data
            boolean[] packet_boolean = convert_processing_data(expected_length);
            processing_data_.clear();
            packet_ = convert_booleans_to_bytes(packet_boolean);
            return RCVEDDAT;                // new data packet is ready
        }
        else {
            throw new RuntimeException(new String("Invalid state"));
        }
	}

	/*
	Return value:
		Return an empty byte[] when no packet is valid
	*/
    public byte[] getPacket(){
       // convert packet_ to byte[]
        if (packet_.length == 0){
            return new byte[0];
        }

        byte[] array_out = packet_.clone();

        // clear packet_ on return
        packet_ = new byte[0];

        return array_out;
    }

    /*  Description:
            return true only when all of the following condition are met:
            1. a possible sync header is meet
            2. the score for this match > header_score (previous match)
        Return Value:
            true: when the current sample is identified as the end of a header (based on current knowledge)
      */
    private boolean check_sync_header(){
        if (processing_header_.size() < header_length_){
            sync_power_debug.add(0.0);
            return false;
        }
        // calculate the dot product
        double sync_power = 0;
        for (int i = 0; i < header_length_; i++){
            sync_power = sync_power + sync_header_[i] * processing_header_.get(i);
        }
        // TODO(jianxiong cai): for some reason, reference program said divided by 200
        sync_power = sync_power / 200;
        sync_power_debug.add(sync_power);
        // TODO: enforce other condition
        // TODO: normalize header in detection maybe?
        /*
        if (bit_counter_ == 540){
            boolean debug = true;
        }
        */
        if ( (sync_power > (power_energy * power_energy)) && (sync_power > header_score_) && (sync_power > 0.05)){
            header_score_ = sync_power;
            return true;
        }
        else{
            return false;
        }
    }

    private boolean[] convert_bytes_to_booleans(byte[] array_in){
        boolean[] array_out = new boolean[array_in.length * 8];

        int counter = 0;

        for (int i = 0; i < array_in.length; i++){

            byte tmp_char = array_in[i];
            int mask = 0x80;

            for (int j = 0; j < 8; j++){
                array_out[counter] = ((tmp_char & mask) == mask);
                mask = mask >>> 1;              // unsigned shift
                counter ++;
            }
        }

        return array_out;
    }

    private byte[] convert_booleans_to_bytes(boolean[] array_in){
        // check for safety
        if (array_in.length/8*8 != array_in.length){
            throw new RuntimeException("Trying to convert misaligned array to bytes[]");
        }
        byte[] array_out = new byte[array_in.length/8];

        int counter = 0;

        for (int i = 0; i < array_out.length; i++){
            int tmp_char = 0;
            for (int j = 0; j < 8; j++){
                // append a new boolean into the byte
                tmp_char = tmp_char << 1;
                // if the boolean is true, change the last bit to 1
                if (array_in[counter]){
                    tmp_char = (tmp_char | 0x01);
                }
                counter ++;
            }
            array_out[i] = (byte)tmp_char;
        }

        return array_out;
    }

    /*  Description:
            decode the processing_data (receiver side)
            convert sound to boolean[]
    */

    private boolean[] convert_processing_data(int expected_length){
        // normalize processing_data such that they are in the range of [-1,1]
        normalize_data();

        // array for storing the result
        boolean[] array_out = new boolean[expected_length*8];

        for (int i = 0; i < expected_length*8; i += 1){
            double tmp_sum = 0;
            // get a chunk
            for (int j = 0; j < bit_length_; j++){
                // for robustness, only use the 25%-75% data.
                if (((j%bit_length_) > bit_length_/4) && ((j%bit_length_) < bit_length_*3/4)){
                    tmp_sum += carrier_[bit_length_*i+j] * processing_data_.get(bit_length_ * i + j);
                }
            }
            // determine if it is 0 or 1
            array_out[i] = (tmp_sum>0) ? true: false;
        }

        return array_out;
    }

    // Test: test passed
    private void generate_header(){
        // TODO: change the header generation function for optimization?
        // generate the base function
        int start_freq = 2000;
        int end_freq = 10000;

        double[] fp = new double[header_length_];
        fp[0] = start_freq;
        for (int i = 1; i < header_length_/2; i++){
            fp[i] = fp[i-1] + ((end_freq-start_freq)+0.0)/(header_length_/2);
        }
        fp[header_length_/2] = end_freq;
        for (int i = header_length_/2+1; i < header_length_; i++){
            fp[i] = fp[i-1] - ((end_freq-start_freq)+0.0)/(header_length_/2);
        }

        // use numerical cumulative integral to generate omega
        double[] omega = new double[header_length_];
        omega[0] = 0;
        for (int i = 1; i < header_length_; i++){
            // TODO: from reference program, using t (sample rate) instead of header_length?
            omega[i] = omega[i-1] + (fp[i]+fp[i-1])/2.0*(1.0/sample_rate_);
        }

        sync_header_ = new double[header_length_];
        for (int i = 0; i < header_length_; i++){
            sync_header_[i] = Math.sin(2*Math.PI*omega[i]);
        }
    }

    private void generate_dummy_sin(){
        dummy_sin_wave_ = new double[dummy_sin_length_];
        for (int i=0; i< dummy_sin_length_; i++){
            double t = (i +0.0)/ sample_rate_;
            dummy_sin_wave_[i] = 0.5*(Math.sin(2*Math.PI*1000*t));
        }
    }

    // This function should only be called before converting the processing_data to boolean[]
    private void normalize_data(){
        // find the max abs value
        double max_tmp = 0;
        for (int i = 0; i < processing_data_.size(); i++){
            // replace max_tmp with the largest abs so far
             if ((processing_data_.get(i) > max_tmp) || (processing_data_.get(i) < (-1*max_tmp))){
                 max_tmp = processing_data_.get(i);
                 if (max_tmp < 0)           max_tmp = -1 * max_tmp;
             }
        }

        // store everything to normalized_data
        List<Double> normalized_data = new ArrayList<>();
        for (int i = 0; i < processing_data_.size(); i++){
            normalized_data.add(processing_data_.get(i) / max_tmp);
        }

        // replace the processing_data with normalized_data
        processing_data_ = normalized_data;
    }

    // A unit testcase for modulation
    public static void main(String[] args){
        Modulation modulator = new Modulation(44100);
        /*
        System.out.println("----------------------------------");
        for (int i = 0; i < 10; i++){
            System.out.println(modulator.sync_header_[i]);
        }
        */

        // converting byte[] <-> boolean[]
        /*
        byte[] test_array = new byte[3];
        test_array[0] = 0x08;
        test_array[1] = (byte)0xff;
        test_array[2] = 0x16;
        boolean[] test_out = modulator.convert_bytes_to_booleans(test_array);
        for (int i = 0; i < test_out.length; i++){
            System.out.println(test_out[i]);
        }
        */

        /*
        boolean[] test_array = new boolean[16];
        test_array[0] = false;
        test_array[1] = false;
        test_array[2] = false;
        test_array[3] = true;
        test_array[4] = false;
        test_array[5] = true;
        test_array[6] = true;
        test_array[7] = false;
        test_array[8] = false;
        test_array[9] = false;
        test_array[10] = false;
        test_array[11] = false;
        test_array[12] = false;
        test_array[13] = true;
        test_array[14] = false;
        test_array[15] = true;
        byte[] test_out = modulator.convert_booleans_to_bytes(test_array);
        System.out.println((int)test_out[0]);
        System.out.println((int)test_out[1]);
        */

        // test demodulation (header detection)
        /*
        byte[] test_input = new byte[2];
        test_input[0] = (byte)0x99;
        test_input[1] = (byte)0x1f;
        double[] test_output = modulator.modulate(test_input);
        System.out.println("Modulation completed");
        for (int i = 0; i < test_output.length; i++) {
            boolean flag = modulator.demodulation(test_output[i],2);
            System.out.println(flag);
        }*/
        // test demodulate a packet.
        // There is a "hello world" in this packet.
        byte[] helloworld = {0x7f, 0x0, 0x1, 0xc, 0x48, 0x65, 0x6c, 0x6c,
                        0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64, 0x2e};
        double[] wave = modulator.modulate(helloworld);
        int byte_cnt = helloworld.length;
        int i=0;
        while (modulator.demodulation(wave[i], byte_cnt) != Modulation.RCVEDDAT) {
            i = (i+1) % wave.length; 
        }
        byte[] recv_helloworld = modulator.getPacket(); 
        for (i=0; i<byte_cnt; i++){
            System.out.printf("Byte# %d matched: %d. Decode result: %d\n", 
                    i, (helloworld[i] == (recv_helloworld[i]))? 1: 0, recv_helloworld[i]);
        }

    }

}