

#ifndef TCP_TCPCLIENT_H
#define TCP_TCPCLIENT_H

#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <cerrno>

#include "ReceivedData.h"

class TCPClient {
public:
    TCPClient(std::string ip, unsigned long port);
    ~TCPClient();

    bool send_data(std::string content);

    ReceivedData recv_data();

private:
    int socket_;

    std::string src_ip_;
    unsigned long src_port_;

    std::string reply_buffer_;
};


#endif //TCP_TCPCLIENT_H
