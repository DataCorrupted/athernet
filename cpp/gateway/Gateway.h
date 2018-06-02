#ifndef UDP_GATEWAY_H
#define UDP_GATEWAY_H

// Implement gateway as udp client

#include <string>
#include <boost/asio.hpp>
#include "UDPServer.h"
#include "NatPacket.h"
#include "UDPClient.h"
#include "ICMPClient.h"

enum GatewayMode{
    UDP,
    TCP,
    ICMP,
};

class Gateway {

public:
    Gateway(bool is_icmp);
    ~Gateway();

    void icmp_init(boost::asio::io_service& io_service);

    // send out the nat frame by UDP after receiving it
    bool nat_send(std::string ip, int port, std::string content);

    ReceivedData nat_recv();
private:
    UDPClient * client_;
    UDPServer * server_;

    ICMPClient * icmp_client_;
    bool is_icmp_;
};


#endif //UDP_GATEWAY_H
