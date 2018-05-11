package athernet.tools;

public class NodeConfig {
    private byte src_addr_;
    private byte dest_addr_;

    // parse node config
    public NodeConfig(String args[]){
        dest_addr_ = parse_address(args[0]);

        // infer the src_address from the dest_address
        if (dest_addr_ == 1){src_addr_ = 2;}
        else{src_addr_ = 1;}

        System.out.print("The src address: ");
        System.out.println(src_addr_);
        System.out.print("The dest address: ");
        System.out.println(dest_addr_);
    }

    public byte get_src_addr(){return src_addr_;}
    public byte get_dest_addr(){return dest_addr_;}

    // get the address from the input
    private byte parse_address(String arg){
        if (arg.toLowerCase().equals("node1")){
            return 1;
        }
        else if (arg.toLowerCase().equals("node2")){
            return 2;
        }
        else{
            throw new IllegalArgumentException("Invalid argument for destination address");
        }
    }
}
