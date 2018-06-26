#include <iostream>
#include <thread>
#include "TCPClient.h"

// 163.22.12.51 21

TCPClient * client = NULL;
bool is_shutdown = false;

int receiving_and_disp(){
    while(!is_shutdown){
        std::string recv_reply = client->recv_data().get_content();
        std::cerr << "[INFO] received: " << recv_reply << std::endl;
    }
    return 0;
}

int main(int argc, const char * argv[]) {
    if (argc != 3){
        std::cerr << "[ERROR] invalid usage" << std::endl;
        return 1;
    }

    std::string server_ip = std::string(argv[1]);
    int server_port = atoi(argv[2]);

    client = new TCPClient(server_ip,server_port);

    std::thread child(receiving_and_disp);

    sleep(1);
    std::string content = "USER\n";
    client->send_data(content);
    std::cerr << "[INFO] data sent" << std::endl;
    // sleep(5);

    child.join();


    return 0;
}