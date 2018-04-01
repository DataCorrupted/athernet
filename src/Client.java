import AcousticNetwork.Receiver;
import AcousticNetwork.Transmitter;
import AcousticNetwork.ACKChecker;
import AcousticNetwork.NAKPacket;
import AcousticNetwork.FileO;

import java.util.List;

// TODO: the dummy bag should have packet_id = 255

public class Client {

    private Receiver receiver_;
    private Transmitter transmitter_;
    private ACKChecker ack_checker_;
    private FileO o_file_;

    public Client() throws Exception{
        this(16);
    }
    public Client(int pack_size) throws Exception{
        receiver_ = new Receiver(pack_size);
        transmitter_ = new Transmitter(pack_size);
        // TODO: Recheck if the last package has packet_id == 89
        ack_checker_ = new ACKChecker(89);
        o_file_ = new FileO();
    }

    public static void main(String[] args) throws Exception{

        // parse parameters (if needed)

        // Startup a client
        Client client = new Client();

        // Start to receive data
        client.receiver_.startReceive();

        byte[] packet = new byte[client.receiver_.getPackSize()];
        byte[] data = new byte[1250];

        while(true) {
            int packet_received = 0;            // how many packages has received, including CRC Invalid
            // Recv data as long as there is signal.
            while (true) {
                // Tries to receive a packet.
                int recv_status = client.receiver_.receiveOnePacket(packet);

                // Determine recv status
                int recv_pack_id = (int) packet[1] & 0xff;
                if (recv_status == client.receiver_.RECEVED) {
                    packet_received ++;
                    // Add to ACK_generator.
                    client.ack_checker_.on_ack(recv_pack_id);
                    System.arraycopy(
                        packet, client.receiver_.getPackSize() - client.receiver_.getDataSize(),
                        data, 0,
                        client.receiver_.getDataSize()
                    );
                } else if (recv_status == client.receiver_.CRCINVL) {
                    // Skip CRC invalid packet
                    ;
                } else if (recv_status == client.receiver_.TIMEOUT) {
                    // Does nothing and move on to next;
                    packet_received ++;
                    break;
                }
            }

            // if server didn't response for certain time
            if (packet_received == 0){
                System.out.println("No further pack receive, goto writing data.");
                break;
            }

            // NAK control: send back NAK Report
            int packet_size = client.receiver_.getPackSize();
            // if all packets received, return now
            if (!client.ack_checker_.has_loss_packet()){
                System.out.println("No loss pack, goto writing data.");
                break;
            }
            else{
                // send an empty packet
                byte[] dummy_buffer = new byte[packet_size];
                dummy_buffer[1] = (byte)253;
                client.transmitter_.transmitOnePack(dummy_buffer);
            }
            // generate loss packet list and NAK
            List<Integer> loss_packet_list = client.ack_checker_.get_loss_list();
            int loss_pack_num = loss_packet_list.size();
            NAKPacket nak_pack = new NAKPacket(packet_size-2);
            for (int i = 0; i < loss_pack_num; i++){
                int loss_id = loss_packet_list.get(i);
                nak_pack.add_loss_packet(loss_id);

                // send out the nak_pack
                // either the packet is full, or its the last one
                if ((((i + 1) % (packet_size-2)) == 0) || (i == loss_pack_num-1)){
                    client.transmitter_.transmitOnePack(nak_pack.toArray(packet_size));
                    nak_pack.clear();
                }
            }

            // (if no signal), generate NAK Report and send that out
            // if ACK_generator has no loss packet: write data to output and exist
            // else
            // while(has signal), repeat the receiving steps above
        }

        // Write to file.
        client.o_file_.write(data, 0, 1250);
        // Stop receiving data
        client.receiver_.stopReceive();
    }
}
