package athernet.tools;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import java.util.ArrayList;

public class MacPing {
    private byte dest_addr_;
    private byte src_addr_;

    private MacLayer mac_layer_;
    private ArrayList<Byte> unconfirmed_ids;
    private ArrayList<Long> unconfirmed_time;

    private int counter = 0;

    public MacPing(byte src_addr, byte dest_addr){
        System.out.println("MacPing dest_addr: "+dest_addr);
        src_addr_ = src_addr;
        dest_addr_ = dest_addr;

        counter = 0;

        unconfirmed_ids = new ArrayList<Byte>();
        unconfirmed_time = new ArrayList<Long>();

        try{
            mac_layer_ = new MacLayer(src_addr_, dest_addr);
            mac_layer_.startMacLayer();
            // mac_layer_.turnEcho();
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }
    }

    // RequestSend(mac_pack)
    // GetOnePack
    public void start_ping(){
        while(true){
            if (counter % 10 == 0) {
                sendOnePacket();
                counter = 0;
            }
            counter ++;

            // receive one packet
            if (mac_layer_.countDataPack() != 0) {
                System.out.println("mac_layer countDataPack != 0");
                try {
                    MacPacket received_pack = mac_layer_.getOnePack();
                    long rtt = received_pack.getTimestampMacping();
                    System.out.printf("System Time: %ld", System.currentTimeMillis());
                    System.out.printf(
                        "Received packid: %d, RTT: %.9f\n",
                        received_pack.getPacketID(), (rtt/1e3));

                    // print timeout
                    while ((unconfirmed_ids.size() > 0) && unconfirmed_ids.get(0) != received_pack.getPacketID()){
                        System.out.printf("[MacPing1] Packet %d Timeout\n", unconfirmed_ids.get(0));
                        unconfirmed_ids.remove(0);
                        unconfirmed_time.remove(0);
                    }

                    // remove the received one
                    unconfirmed_ids.remove(0);
                    unconfirmed_time.remove(0);
                } catch (Exception exception) {
                    System.out.println("mac_layer_.getOnePack received one exception");
                }
            }

            // check timeout
            while((unconfirmed_time.size() > 0) && (unconfirmed_time.get(0) + 2e3) < System.currentTimeMillis()){
                System.out.printf("[MacPing2] Packet %d Timeout\n", unconfirmed_ids.get(0));
                unconfirmed_time.remove(0);
                unconfirmed_ids.remove(0);
            }

            try {
                Thread.sleep(100);
            }
            catch (Exception exception){
                System.out.println("Thread.sleep throw exception");
            }
        }
    }


    private void sendOnePacket(){
        long curr_time = 0;
        MacPacket packet = new MacPacket(dest_addr_,src_addr_,curr_time);
        packet.setPacketID((byte)0);

        try {
            mac_layer_.requestSend(packet);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }

        // while(!mac_layer_.isIdle()){
            try {
                Thread.sleep(0, 100);
            }
            catch (Exception exception){
                System.out.println("Thread throw exception");
            }
        // }

        // save the packet_id
        unconfirmed_ids.add((byte) packet.getPacketID());
        unconfirmed_time.add(packet.getTimestampMacping());
    }

    public static void main(String[] args) throws Exception{
        NodeConfig node_config = new NodeConfig(args);
        System.out.printf("Source Address: %d\n", node_config.get_src_addr());
        System.out.printf("Target Address: %d\n", node_config.get_dest_addr());

        if (args.length == 1){
            MacPing mac_ping = 
                new MacPing(node_config.get_src_addr(), node_config.get_dest_addr());
            mac_ping.start_ping();
        
        } else if (args[1].equals("-S") || args[1].equals("--server")){
            System.err.println("Server started.");
            MacLayer mac_layer_ = 
                new MacLayer(node_config.get_dest_addr(), node_config.get_src_addr());
            mac_layer_.startMacLayer();
        } else {
            System.err.println("No such option.");
        }
    }
}
