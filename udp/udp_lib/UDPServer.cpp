#include "UDPServer.h"

UDPServer::UDPServer(int port) {
    // create a new socket
    socket_ = socket(AF_INET,SOCK_DGRAM, IPPROTO_UDP);

    // bind address
    struct sockaddr_in ip_addr;
    ip_addr.sin_family = AF_INET;
    ip_addr.sin_addr.s_addr = INADDR_ANY;
    ip_addr.sin_port = htons(port);

    if (bind(socket_,(struct sockaddr *)&ip_addr , sizeof(ip_addr)) == -1){
        std::cerr << "bind failed" << std::endl;
        throw std::runtime_error("bind failed");
    }
}

UDPServer::~UDPServer() {
    close(socket_);
}

ReceivedData UDPServer::recv_data() {
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