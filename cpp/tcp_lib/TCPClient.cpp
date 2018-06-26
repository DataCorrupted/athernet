#include "TCPClient.h"


TCPClient::TCPClient(std::string ip, int port) {
    socket_ = socket(AF_INET,SOCK_STREAM, 0);

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

TCPClient::~TCPClient() {
    close(socket_);
}

bool TCPClient::send_data(std::string content_raw) {
    // encode the content length to the first bit
//    unsigned int content_len = content_raw.size();
    std::string content;
    content = content_raw;
//    content.push_back((char)content_len);
//    content = content + content_raw;

    if (send(socket_, content.c_str(), content.length(), 0) == -1) {
        std::cerr << "send failed" << std::endl;
        std::cerr << strerror(errno) << std::endl;
        return false;
    }
    return true;
}

ReceivedData TCPClient::recv_data() {
    ReceivedData received_data;
    while(1) {
        char recv_buffer[1024];

        // receive data
        struct sockaddr_in client_addr;
        int client_len = sizeof(client_addr);
        int recv_len = recvfrom(socket_, recv_buffer, sizeof(recv_buffer),
                                0, (struct sockaddr *) &client_addr,
                                (socklen_t *) &client_len);

        // get the content
        std::string raw_content = std::string(recv_buffer, recv_len);


        // save the content to the buffer
        // reply_buffer_ = reply_buffer_ + raw_content;

        if (raw_content.length() > 0) {
            // std::cerr << "[DEBUG] TCP buffer size: " << reply_buffer_.size() << std::endl;
            // verify data length
//            unsigned int data_length = (unsigned int)reply_buffer_[0];
//            if (data_length < (reply_buffer_.length() - 1)){
//                // not enough data, wait for the next packet
//                std::cerr << "[DEBUG, TCPClient] received data_length < required, continue listening" << std::endl;
//                continue;
//            }

            // prepare the returning packet
            // get the content
//            std::string recv_content = std::string(reply_buffer_,reply_buffer_.length());
//            received_data.set_content(recv_content);
            // empty the buffer
            // std::cerr << "[DEBUG] length before empty: " << reply_buffer_.length() << std::endl;
//            reply_buffer_ = std::string(reply_buffer_,data_length+1,reply_buffer_.length() - data_length - 1);
//            reply_buffer_.clear();
            // std::cerr << "[DEBUG] length after empty: " << reply_buffer_.length() << std::endl;

            received_data.set_content(raw_content);
            // set src_ip and port
            received_data.set_src_ip(src_ip_);
            received_data.set_src_port(src_port_);
            return received_data;
        }
    }
}