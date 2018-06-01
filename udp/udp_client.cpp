#include <iostream>
#include "UDPClient.h"

int main() {
    UDPClient client("127.0.0.1",8888);

    for (int i = 0; i < 10; i++){
        std::string content = "Hello world";
        client.send_data(content);
        std::cerr << "[INFO] data sent" << std::endl;
        sleep(1);
    }

    return 0;
}