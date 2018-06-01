#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <cerrno>
#include <cstring>

int main() {
    int server_socket = socket(AF_INET,SOCK_DGRAM, IPPROTO_UDP);

    // get address
    struct sockaddr_in ip_addr, client_addr;
    ip_addr.sin_family = AF_INET;
    ip_addr.sin_addr.s_addr = INADDR_ANY;
    ip_addr.sin_port = htons(8888);

    // bind
    if (bind(server_socket,(struct sockaddr *)&ip_addr , sizeof(ip_addr)) == -1){
        std::cerr << "bind failed" << std::endl;
        return 1;
    }

//    if (listen(server_socket, 2) == -1){
//        std::cerr << "listen failed" << std::endl;
//        std::cout << strerror(errno) << std::endl;
//        return 1;
//    }
//
//    int socket_len = sizeof(client_addr);
//    int socket = accept(server_socket,(struct sockaddr *) &client_addr, (socklen_t*)&socket_len);
//    if (socket == -1){
//        std::cerr << "accept failed" << std::endl;
//        std::cout << strerror(errno) << std::endl;
//    }
//    std::cerr << "accepted" << std::endl;

    char recv_buffer[1024];
    while(1) {
        int recv_len = recv(server_socket, recv_buffer, sizeof(recv_buffer), 0);
        if (recv_len <= 0){
            continue;
        }
        std::string recv_content = std::string(recv_buffer, recv_len);
        std::cout << recv_content << std::endl;
    }

    return 0;
}