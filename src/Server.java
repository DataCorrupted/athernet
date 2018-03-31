import AcousticNetwork.Receiver;
import AcousticNetwork.Transmitter;
import AcousticNetwork.FileI;
import AcousticNetwork.SoundO;
// import AcousticNetwork.Sender;

public class Server {

    private FileI i_file_;
    private SoundO o_sound_;
    private Receiver receiver_;
    private Transmitter transmitter_;

    private byte[] data_;

    public Server(String file, int file_format) throws Exception {
        receiver_ = new Receiver();
        transmitter_ = new Transmitter();
        i_file_ = new FileI(file, file_format);
        o_sound_ = new SoundO();
    }

    public int readFile(String path){
        // data_ = i_file_.blabla;
        // return bytes_read;
        return -1;
    }

    public int getPack(int pack_id){
        // return bytes read. -1 for no data.
        return -1;
    }

    public static void main(String[] args) throws Exception{
        // Parsing args.
        int i= 0;
        String file = "./I";
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
