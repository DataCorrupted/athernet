#include "TCPServer.h"

TCPServer::TCPServer(int port) {
    // create a new socket
    socket_server_ = socket(AF_INET,SOCK_STREAM, 0);

    // bind address
    struct sockaddr_in ip_addr, client_addr;
    ip_addr.sin_family = AF_INET;
    ip_addr.sin_addr.s_addr = INADDR_ANY;
    ip_addr.sin_port = htons(port);

    if (bind(socket_server_,(struct sockaddr *)&ip_addr , sizeof(ip_addr)) == -1){
        std::cerr << "bind failed" << std::endl;
        throw std::runtime_error("bind failed");
    }

    if (listen(socket_server_,3) == -1){
        std::cerr << "[ERROR, TCPServer]: listen failed" << std::endl;
    }

    int client_addr_len = sizeof(client_addr);
    socket_ = accept(socket_server_, (struct sockaddr *)&client_addr, (socklen_t *)&client_addr_len);
    if (socket_ == -1){
        std::cerr << "[ERROR, TCPServer] accept failed" << std::endl;
    }

    // set src_ip and port
    // get the client ip
    char client_ip[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &(client_addr.sin_addr), client_ip, INET_ADDRSTRLEN);
    src_ip_ = client_ip;

    // get the client port
    src_port_ = ntohs(client_addr.sin_port);
}

TCPServer::~TCPServer() {
    close(socket_);
}

bool TCPServer::send_data(std::string content_raw) {
    // encode the content length to the first bit
    unsigned int content_len = content_raw.size();
    std::string content;
    content.push_back((char)content_len);
    content = content + content_raw;

    if (send(socket_, content.c_str(), content.length(), 0) == -1) {
        std::cerr << "send failed" << std::endl;
        std::cerr << strerror(errno) << std::endl;
        return false;
    }
    return true;
}

ReceivedData TCPServer::recv_data() {
    ReceivedData received_data;
    while(1) {
        char recv_buffer[1024];

        // receive data
        struct sockaddr_in client_addr;
        int client_len = sizeof(client_addr);


        bool wait_flag = true;
        if (reply_buffer_.length() > 0) {
            unsigned int data_length = (unsigned int)reply_buffer_[0];
            if (data_length <= (reply_buffer_.length() - 1)){
                // no need to wait
                wait_flag = false;
            }
        }

        // wait on necessary
        if (wait_flag) {
            // sync recv
            int recv_len = recvfrom(socket_, recv_buffer, sizeof(recv_buffer),
                                    0, (struct sockaddr *) &client_addr,
                                    (socklen_t *) &client_len);

            // get the content
            std::string raw_content = std::string(recv_buffer, recv_len);
            // save the content to the buffer
            reply_buffer_ = reply_buffer_ + raw_content;
        }

        if (reply_buffer_.length() > 0) {
            // std::cerr << "[DEBUG] TCP buffer size: " << reply_buffer_.size() << std::endl;
            // verify data length
            unsigned int data_length = (unsigned int)reply_buffer_[0];
            if (data_length > (reply_buffer_.length() - 1)){
                std::cerr << "[DEBUG, TCPClient] required data_length:" << data_length << std::endl;
                // not enough data, wait for the next packet
                std::cerr << "[DEBUG, TCPClient] received data_length:" << reply_buffer_.length() - 1 << std::endl;
                std::cerr << "[DEBUG, TCPClient] received data_length < required, continue listening" << std::endl;
                continue;
            }

            // prepare the returning packet
            // get the content
            std::string recv_content = std::string(reply_buffer_,1,data_length);
            received_data.set_content(recv_content);
            // empty the buffer
            // std::cerr << "[DEBUG] length before empty: " << reply_buffer_.length() << std::endl;
            // reply_buffer_ = "";
            // std::cerr << "[DEBUG, TCPServer] reply_buffer_.length() - data_length - 1: " << reply_buffer_.length() - data_length - 1 << std::endl;
            reply_buffer_ = std::string(reply_buffer_,data_length+1,reply_buffer_.length() - data_length - 1);
            std::cerr << "[DEBUG, TCPServer] length after empty: " << reply_buffer_.length() << std::endl;

            // set src_ip and port
            received_data.set_src_ip(src_ip_);
            received_data.set_src_port(src_port_);
            return received_data;
        }
    }
}