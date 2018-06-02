#ifndef UDP_GATEWAY_H
#define UDP_GATEWAY_H

// Implement gateway as udp client

#include <string>
#include <UDPServer.h>
#include "NatPacket.h"
#include "UDPClient.h"

class Gateway {

public:
    Gateway();
    ~Gateway();

    // send out the nat frame by UDP after receiving it
    bool nat_send(std::string ip, int port, std::string content);

    ReceivedData nat_recv();
private:
    UDPClient * client_;
    UDPServer * server_;

};


#endif //UDP_GATEWAY_H
