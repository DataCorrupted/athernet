#ifndef TCP_TCPSERVER_H
#define TCP_TCPSERVER_H


#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <exception>
#include <unistd.h>
#include <cstring>
#include <cerrno>
#include "ReceivedData.h"

class TCPServer {
public:
    TCPServer(int port);
    ~TCPServer();

    bool send_data(std::string data);
    ReceivedData recv_data();

private:
    int socket_;
    int socket_server_;

    std::string src_ip_;
    int src_port_;

    std::string reply_buffer_;
};


#endif //UDP_UDPSERVER_H
