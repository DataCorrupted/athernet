//
// Created by ernest on 18-6-2.
//

#include "Gateway.h"

// create long socket
Gateway::Gateway(std::string ip, int port) : client_(ip,port), server_ip_(ip), server_port_(port) {
}

bool Gateway::nat_send(std::string encoded_frame) {

    NatPacket nat_pack(encoded_frame);

    // TODO: by pass the check to udp port

    client_.send_data(nat_pack.get_content());

}