import AcousticNetwork.Receiver;
import AcousticNetwork.Transmitter;
import AcousticNetwork.FileI;
import AcousticNetwork.SoundO;
import AcousticNetwork.NAKPacket;

import java.util.ArrayList;
import java.util.List;
// import AcousticNetwork.Sender;

public class Server {

    private FileI i_file_;
    private SoundO o_sound_;
    private Receiver receiver_;
    private Transmitter transmitter_;
    private List<Integer> loss_lists_;          // store all packages need resend

    private byte[] data_;

    public Server(String file, int file_format) throws Exception {
        receiver_ = new Receiver();
        transmitter_ = new Transmitter();
        i_file_ = new FileI(file, file_format);
        o_sound_ = new SoundO();

        loss_lists_ = new ArrayList<>();
    }

    public int readFile() throws Exception{
        data_ = i_file_.readAllData();
        return data_.length;
    }
    public int readNewFile(String path) throws Exception{
        i_file_.updateFile(path);
        return this.readFile();
    }

    // Given packet's id and an array to place that packet,
    // move data to arr and return bytes read.
    public int getPack(int pack_id, byte[] arr){
        int start_pos = transmitter_.getDataSize() * pack_id;
        int cnt = 0;
        for (int i=0; i<transmitter_.getDataSize(); i++){
            if (start_pos+i < data_.length){
                arr[i] = data_[start_pos+i];
                cnt++;
            }
        }
        // There is no need to specify a -1, as 0 already means nothing read,
        // which indicates that an invalid pack id is given.
        return cnt;
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
        Server server_ = new Server(file, FileI.TEXT01);

        // send out a dummy (empty) packet
        byte[] packet = new byte[server_.transmitter_.getPackSize()];
        packet[1] = (byte)253;
        server_.transmitter_.transmitOnePack(packet);
        System.out.println("Empty Packet sent.");

        // Read data from a file.
        int r = server_.readFile();
        System.out.printf("%d bytes read from file.", r);

        // send out all data_packet
        int pack_cnt = 0;
        while (server_.getPack(pack_cnt, packet) != -1){
            server_.transmitter_.transmitOnePack(packet);
            pack_cnt ++;
        }
        System.out.println("All data transmitted.");

        // hold on 0.5s (in case hearing yourself)
        // Thread.sleep(100);

        // ---------------------NAK Flow Control------------------------
        while(true){
            // hear for NAK Report (perhaps more than one packet)
            // if NAK report is broken, ignore it
            int packet_received = 0;                // count how many package received (including CRC invalid)
            while(true){
                byte[] nak_report = new byte[server_.receiver_.getPackSize()];
                int flag = server_.receiver_.receiveOnePacket(nak_report);
                // decode the data packet
                if (flag == server_.receiver_.RECEVED){
                    if (((int)nak_report[1] & 0xff) != 253){
                        continue;
                    }
                    if (((int)nak_report[1] & 0xff) != 254){
                        System.out.println("[WARNING] NAK Report is invalid, packet_id != 254");
                    }
                    // add all lost data packet to loss_list
                    for (int j = 2; j < nak_report.length; j++){
                        int loss_id = (int)nak_report[j] & 0xff;
                        server_.loss_lists_.add(loss_id);
                    }
                    packet_received ++;
                }
                else if (flag == server_.receiver_.TIMEOUT){
                    System.out.println("Timeout reached, transmission done");
                    break;
                }
                else{
                    // CRC8 Invalid
                    packet_received++;
                }
            }

            // if Client didn't speak for certain seconds, manually timeout, return 0
            // the Client should have got all data it needed
            if (packet_received == 0){
                break;
            }

            // resend lost package (remember to send out a dummy empty packet before)
            if (server_.loss_lists_.size() > 0){
                byte[] dummy_pack = new byte[16];
                dummy_pack[1] = (byte)253;
                server_.transmitter_.transmitOnePack(dummy_pack);
            }
            while(server_.loss_lists_.size() > 0){
                int packet_id = server_.loss_lists_.get(0);
                server_.loss_lists_.remove(0);
                byte[] data_to_resend = new byte[16];
                if (server_.getPack(packet_id, data_to_resend) > 0){
                    server_.transmitter_.transmitOnePack(data_to_resend);
                }
            }
        }


    }


}
