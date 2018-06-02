#ifndef UDP_GATEWAY_H
#define UDP_GATEWAY_H

// Implement gateway as udp client

#include <string>
#include "NatPacket.h"
#include "UDPClient.h"


class Gateway {

public:
    Gateway(std::string ip, int port);

    // send out the nat frame by UDP after receiving it
    bool nat_send(std::string encoded_frame);

private:
    UDPClient client_;
    std::string server_ip_;
    int server_port_;

};


#endif //UDP_GATEWAY_H
