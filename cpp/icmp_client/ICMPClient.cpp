//
// Created by Jianxiong Cai on 18-6-3.
//

#include "ICMPClient.h"


ICMPClient::ICMPClient(boost::asio::io_service &io_service):socket_(io_service, icmp::v4()),
                                                            sequence_number_(0) {
}

bool ICMPClient::send_data(std::string ip, std::string content) {
    send_icmp(ip,content);
    return true;
}

void ICMPClient::send_icmp(std::string ip, std::string body) {
    icmp::endpoint destination(boost::asio::ip::address::from_string(ip),0);

    icmp_header echo_request;
    echo_request.type(icmp_header::echo_request);
    echo_request.code(0);
    echo_request.identifier(get_identifier());
    echo_request.sequence_number(++sequence_number_);
    compute_checksum(echo_request, body.begin(), body.end());

    // Encode the request packet.
    boost::asio::streambuf request_buffer;
    std::ostream os(&request_buffer);
    os << echo_request << body;

    socket_.send_to(request_buffer.data(), destination);
}

ReceivedData ICMPClient::recv_data() {
    return recv_icmp();
}

ReceivedData ICMPClient::recv_icmp() {
    while(1) {
        // Discard any data already in the buffer.
        reply_buffer_.consume(reply_buffer_.size());

        ssize_t recv_len = socket_.receive(reply_buffer_.prepare(65536));
        reply_buffer_.commit(recv_len);

        // Decode the reply packet.
        std::istream is(&reply_buffer_);
        ipv4_header ipv4_hdr;
        icmp_header icmp_hdr;
        is >> ipv4_hdr >> icmp_hdr;

        if ((recv_len > 0) && (icmp_hdr.type() == icmp_header::echo_reply)) {

            // std::cerr << "[DEBUG] ICMP reply received" << std::endl;
            // std::cerr << "[DEBUG] ip: " << ipv4_hdr.source_address() << std::endl;

            // get ICMP content
            auto bufs = reply_buffer_.data();
            std::string content(boost::asio::buffers_begin(bufs), boost::asio::buffers_begin(bufs) + reply_buffer_.size());
            // std::cerr << "[DEBUG] payload: " << content << std::endl;

            ReceivedData recv_data;
            // for ICMP: port = 0
            recv_data.set_src_port(0);
            recv_data.set_src_ip(ipv4_hdr.source_address().to_string());
            recv_data.set_content(content);
            return recv_data;
        } else {
            // print out warning and wait for the next packet
            std::cerr << "[WARN] Received one ICMP packet" << std::endl;
            std::cerr << "[WARN] type(is echo_reply): " << (icmp_hdr.type() == icmp_header::echo_reply) << std::endl;
        }
    }

}


unsigned short get_identifier()
{
#if defined(BOOST_WINDOWS)
    return static_cast<unsigned short>(::GetCurrentProcessId());
#else
    return static_cast<unsigned short>(::getpid());
#endif
}