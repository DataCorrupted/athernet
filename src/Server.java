// import AcousticNetwork.Receiver;
// import AcousticNetwork.Sender;

public class Server {

    private Receiver receiver_;
    private Transmitter transmitter_;

    public static void main(String[] args){
        // --------------- initial file transmission ------------------
        // parse the parameters (if needed)

        // open the file

        // send out a dummy (empty) packet

        // send out all data_packet

        // hold on less than a second (in case hearing yourself)

        // ---------------------NAK Flow Control------------------------
        // hear for NAK Report
        // if NAK report is broken, ignore it

        // while (NAK available)     resend lost package (remember to send out a dummy empty packet before)
        // if Client didn't speak for certain seconds, manually timeout, return 0
        // the Client should have got all data it needed
    }


}
