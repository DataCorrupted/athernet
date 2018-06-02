//
// Created by ernest on 18-6-2.
//

#include <NatPacket.h>

int main(int argc, const char * argv[]){
    // create one nat packet
    std::string ip_test = "192.0.0.1";
    int port_test = 8888;
    std::string content_test = "Hello world";
    NatPacket natpack(ip_test,port_test,content_test);

    // encode
    std::string nat_encoded = natpack.encode_frame();

    // decode
    NatPacket nat_decoded(nat_encoded);
    std::cout << "ip: " << nat_decoded.get_ip() << std::endl;
    std::cout << "port: " << nat_decoded.get_port() << std::endl;
    std::cout << "content: " << nat_decoded.get_content() << std::endl;
}