

#ifndef UDP_UDPCLIENT_H
#define UDP_UDPCLIENT_H

#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <cerrno>

class UDPClient {
public:
    UDPClient(std::string ip, int port);
    ~UDPClient();

    bool send_data(std::string content);

    std::string recv_data();

private:
    int socket_;
};


#endif //UDP_UDPCLIENT_H
