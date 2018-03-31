import AcousticNetwork.Receiver;
import AcousticNetwork.Transmitter;

public class Client {

    private Receiver receiver_;
    private Transmitter transmitter_;

    public Client(){
        this(16);
    }
    public Client(int pack_size){
        receiver_ = new Receiver(pack_size);
        transmitter_ = new Transmitter(pack_size);
    }

    public static void main(String[] args){

        // parse parameters (if needed)

        // Startup a client
        Client client = new Client();

        // Start to receive data
        client.receiver_.startReceive();

        byte[] packet = new byte[client.receiver_.getPackSize()];

        // Recv data as long as there is signal.
        while(client.receiver_.hasSignal()){

            // Tries to receive a packet.
            int recv_status = client.receiver_.receiveOnePacket();
            
            // Determine recv status
            if (recv_status == client.receiver_.RECEVED){
                // Add to ACK_generator.
            } else (recv_status == client.receiver_.CRCINVL){
                // Flag it loss in ACK_generator.
            } else (recv_status == client.receiver_.TIMEOUT){
                // Does nothing and move on to next.
                ;
            }
        }
        // (if no signal), generate NAK Report and send that out
        // if ACK_generator has no loss packet: write data to output and exist
        // else
        // while(has signal), repeat the receiving steps above

        // Stop receiving data
        client.receiver_.stopReceive();
    }
}
