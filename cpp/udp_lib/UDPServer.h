#ifndef UDP_UDPSERVER_H
#define UDP_UDPSERVER_H


#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <exception>
#include <unistd.h>
#include <cstring>
#include <cerrno>
#include "ReceivedData.h"

class UDPServer {
public:
    UDPServer(int port);
    ~UDPServer();

    bool send_data(std::string data);
    ReceivedData recv_data();

private:
    int socket_;

};


#endif //UDP_UDPSERVER_H
