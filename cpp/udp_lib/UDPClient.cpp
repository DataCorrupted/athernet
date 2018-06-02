#include "UDPClient.h"


UDPClient::UDPClient(std::string ip, int port) {
    socket_ = socket(AF_INET,SOCK_DGRAM, IPPROTO_UDP);

    // get address
    struct sockaddr_in ip_addr;
    ip_addr.sin_family = AF_INET;
    ip_addr.sin_addr.s_addr = inet_addr(ip.c_str());
    ip_addr.sin_port = htons(port);

    // connect
    if (connect(socket_ , (struct sockaddr *)&ip_addr , sizeof(ip_addr)) == -1) {
        std::cerr << "connect error" << std::endl;
        throw std::runtime_error("connect error");
    }
}

UDPClient::~UDPClient() {
    close(socket_);
}

bool UDPClient::send_data(std::string content) {
    if (send(socket_, content.c_str(), content.length(), 0) == -1) {
        std::cerr << "send failed" << std::endl;
        std::cerr << strerror(errno) << std::endl;
        return false;
    }
    return true;
}