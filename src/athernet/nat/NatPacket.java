package athernet.nat;

public class NatPacket {
    // Byte:
    // -0- -1- -2- -3- -4- -5- -6- -7- -8- 
    // ip. ip. ip. ip: por por content....

    private byte[] ip_;
    private int port_;
    private byte[] content_;

    public NatPacket(int[] ip, int port, byte[] content){
        ip_ = new byte[4];
        for (int i = 0; i < 4; i++){
            ip_[i] = (byte)ip[i];
        }
        content_ = content;
        port_ = port;
    }

    public NatPacket(byte[] encoded_data){
        ip_ = new byte[4];
        port_ = (encoded_data[4] << 8) + (int) (encoded_data[5] & 0xff);
        content_ = new byte[encoded_data.length - 6];

        System.arraycopy(encoded_data,0,ip_,0,ip_.length);
        System.arraycopy(encoded_data,6,content_,0,content_.length);
    }

    public byte[] toArray(){
        byte[] out = new byte[ip_.length + content_.length + 2];
        System.arraycopy(ip_,0,out,0,ip_.length);
        out[4] = (byte) ((port_ & 0xff00) >> 8);
        out[5] = (byte) (port_ & 0x00ff);
        System.arraycopy(content_,0,out,ip_.length + 2,content_.length);
        return out;
    }

    public int[] getIPByteArray(){
        int[] out = new int[4];
        for (int i = 0; i < 4; i++){
            out[i] = (ip_[i] & 0xff);
        }
        return out;
    }

    public byte[] getContent(){
        return content_;
    }

    public int getPort(){ return port_; }

    public static void main(String args[]){
        int[] ip_test = {192, 168, 1, 2};
        int port = 8888;
        byte[] content_test = {'6', 0x25, (byte)0xf8, 0x02, 0x11,'8', (byte)0xff, 0x00};
        NatPacket test_packet = new NatPacket(ip_test, port, content_test);
        byte[] encoded_frame = test_packet.toArray();

        NatPacket recv_packet = new NatPacket(encoded_frame);
        int[] ip_recv = recv_packet.getIPByteArray();
        for (int i = 0; i < ip_recv.length; i++){
            System.err.print(ip_recv[i]);
            System.err.print((i == 3)? ":": ".");
        }
        System.err.print(recv_packet.getPort());

        System.err.println();

        byte[] recv_content = recv_packet.getContent();
        for (int i = 0; i < recv_content.length; i++){
            System.err.print((char) recv_content[i]);
        }
        System.out.println();
    }

}
