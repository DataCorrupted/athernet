import AcousticNetwork.Receiver;
import AcousticNetwork.Transmitter;
import AcousticNetwork.FileI;
import AcousticNetwork.SoundO;
// import AcousticNetwork.Sender;

public class Server {

    private CRC8 crc8_;
    private FileI = i_file_;
    private Receiver receiver_;
    private Transmitter transmitter_;

    public Server(String file, int file_format){
        receiver_ = new Receiver();
        transmitter_ = new Transmitter();
        i_file_ = FileI(file, file_format);
        o_sound_ = SoundO();
    }

    public static void main(String[] args){
        // Parsing args.
        int i= 0;
        String file = "./I"
        while (i<args.length){
            if (args[i].equals("-f")){
                i++;
                if (i == args.length){
                    System.out.println("No file given, using default.");
                } else {
                    file = args[i];
                }
            } else {
                System.out.println("No such command.");
            }
            i++;
        }
        // Setup a server.
        Server server = new Server(file, FileI.TEXT01);

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
