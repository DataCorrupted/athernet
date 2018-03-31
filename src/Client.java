

public class Client {

    private Receiver receiver_;
    private Transmitter transmitter_;

    public static void main(String[] args){
        // parse parameters (if needed)

        // start receiving the data

        // while(has signal)
        // if data is CRC invalid, flag it as loss in ACK_generator
        // if data is correct, add it to ACK_generateor

        // (if no signal), generate NAK Report and send that out
        // if ACK_generator has no loss packet: write data to output and exist
        // else
        // while(has signal), repeat the receiving steps above


    }
}
