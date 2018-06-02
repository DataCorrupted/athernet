#ifndef ICMP_EXAMPLE_ICMPCLIENT_H
#define ICMP_EXAMPLE_ICMPCLIENT_H

#include <iostream>
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include "ReceivedData.h"
#include "icmp_header.hpp"
#include "ipv4_header.hpp"

using boost::asio::ip::icmp;
using boost::asio::deadline_timer;
namespace posix_time = boost::posix_time;

class ICMPClient {
public:
    // initialize ICMP socket and bind it to any IPv4
    ICMPClient(boost::asio::io_service& io_service);

    bool send_data(std::string ip, std::string content);

    ReceivedData recv_data();

private:

    void send_icmp(std::string ip, std::string content);
    ReceivedData recv_icmp();

    icmp::socket socket_;
    unsigned short sequence_number_;
    boost::asio::streambuf reply_buffer_;
};

static unsigned short get_identifier();

#endif //ICMP_EXAMPLE_ICMPCLIENT_H
