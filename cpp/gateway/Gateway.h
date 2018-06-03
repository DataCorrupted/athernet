#ifndef UDP_GATEWAY_H
#define UDP_GATEWAY_H

// Implement gateway as udp client

#include <string>
#include <boost/asio.hpp>
#include "NatPacket.h"
#include "UDPClient.h"
#include "UDPServer.h"
#include "TCPClient.h"
#include "TCPServer.h"
#include "ICMPClient.h"

enum GatewayMode{
    UDP,
    TCP,
    ICMP,
};

class Gateway {

public:
    Gateway(bool is_icmp, bool is_tcp);
    ~Gateway();

    void icmp_init(boost::asio::io_service& io_service);

    // send out the nat frame by UDP after receiving it
    bool nat_send(std::string ip, int port, std::string content);

    ReceivedData nat_recv();
private:
    UDPClient * udp_client_;
    UDPServer * udp_server_;

    TCPClient * tcp_client_;
    TCPServer * tcp_server_;

    ICMPClient * icmp_client_;
    bool is_icmp_;
    bool is_tcp_;
};


#endif //UDP_GATEWAY_H
