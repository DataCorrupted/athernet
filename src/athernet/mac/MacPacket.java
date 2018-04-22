package athernet.mac;

public class MacPacket {
    public final byte TYPE_ACK = 0;
    public final byte TYPE_DATA = 1;
    public final byte TYPE_INIT = 2;

    private byte mac_addr_;             // 2 bits
    private byte type_;                 // 6 bits
    private byte pack_id_;              // one byte
    private byte[] data_field_;               // 2^n - 3 bytes

    public MacPacket(byte mac_addr, byte type, byte pack_id, byte[] data_field){
        mac_addr_ = mac_addr;
        type_ = type;
        pack_id_ = pack_id;
        data_field_ = data_field;
    }

    public MacPacket(byte[] frame){
        mac_addr_ = (byte)(frame[0] >> 6);
    }
}
