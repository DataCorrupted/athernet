#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>


int main() {
    std::string ip = "127.0.0.1";

    int socket_ = socket(AF_INET,SOCK_DGRAM, IPPROTO_UDP);

    // get address
    struct sockaddr_in ip_addr;
    ip_addr.sin_family = AF_INET;
    ip_addr.sin_addr.s_addr = inet_addr(ip.c_str());
    ip_addr.sin_port = htons(8888);

    // connect
    if (connect(socket_ , (struct sockaddr *)&ip_addr , sizeof(ip_addr)) == -1) {
        std::cerr << "connect error" << std::endl;
        return 1;
    }

    std::string content = "Hello world";

    for (int i = 0; i < 10; i++) {
        content = content + "!";
        if (send(socket_, content.c_str(), content.length(), 0) < 0) {
            std::cerr << "Send failed" << std::endl;
            return 1;
        }
        else{
            std::cout << "Data sent" << std::endl;
        }
    }

    return 0;
}