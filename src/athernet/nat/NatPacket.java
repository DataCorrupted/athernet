package athernet.nat;

public class NatPacket {
    private byte[] ip_;
    private byte[] content_;

    public NatPacket(int[] ip, byte[] content){
        ip_ = new byte[4];
        for (int i = 0; i < 4; i++){
            ip_[i] = (byte)ip[i];
        }
        content_ = content;
    }

    public NatPacket(byte[] encoded_data){
        ip_ = new byte[4];
        content_ = new byte[encoded_data.length - 4];

        System.arraycopy(encoded_data,0,ip_,0,ip_.length);
        System.arraycopy(encoded_data,4,content_,0,content_.length);
    }

    public byte[] Encode(){
        byte[] out = new byte[ip_.length + content_.length];
        System.arraycopy(ip_,0,out,0,ip_.length);
        System.arraycopy(content_,0,out,ip_.length,content_.length);
        return out;
    }

    public int[] GetIP(){
        int[] out = new int[4];
        for (int i = 0; i < 4; i++){
            out[i] = (ip_[i] & 0xff);
        }
        return out;
    }

    public byte[] GetContent(){
        return content_;
    }


    public static void main(String args[]){
        int[] ip_test = {192,168,1,2};
        byte[] content_test = {'6','7','U','I','7','8'};
        NatPacket test_packet = new NatPacket(ip_test,content_test);
        byte[] encoded_frame = test_packet.Encode();

        NatPacket recv_packet = new NatPacket(encoded_frame);
        int[] ip_recv = recv_packet.GetIP();
        for (int i = 0; i < ip_recv.length; i++){
            System.err.println(ip_recv[i]);
        }

        System.err.println();

        byte[] recv_content = recv_packet.GetContent();
        for (int i = 0; i < recv_content.length; i++){
            System.err.println(recv_content[i]);
        }
    }

}
