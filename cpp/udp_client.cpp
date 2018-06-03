#include <iostream>
#include "UDPClient.h"

int main(int argc, const char * argv[]) {
    if (argc != 3){
        std::cerr << "[ERROR] invalid usage" << std::endl;
        return 1;
    }

    std::string server_ip = std::string(argv[1]);
    int server_port = atoi(argv[2]);

    UDPClient client(server_ip,server_port);

    for (int i = 0; i < 40; i++){
        std::string content = "Hello world";
        client.send_data(content);
        std::cerr << "[INFO] data sent" << std::endl;
        sleep(1);
    }

    return 0;
}