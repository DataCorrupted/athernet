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

ReceivedData UDPClient::recv_data() {
    ReceivedData received_data;
    char recv_buffer[1024];

    // receive data
    struct sockaddr_in client_addr;
    int client_len = sizeof(client_addr);
    unsigned long recv_len = (unsigned long) recvfrom(socket_, recv_buffer, sizeof(recv_buffer),
                                                      0,(struct sockaddr *) &client_addr,(socklen_t *)&client_len);

    // get the content
    std::string recv_content = std::string(recv_buffer, recv_len);
    received_data.set_content(recv_content);

    // get the client ip
    char client_ip[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &(client_addr.sin_addr), client_ip, INET_ADDRSTRLEN);
    received_data.set_src_ip(client_ip);

    // get the client port
    received_data.set_src_port(ntohs(client_addr.sin_port));

    return received_data;
}