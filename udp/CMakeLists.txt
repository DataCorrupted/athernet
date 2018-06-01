cmake_minimum_required(VERSION 2.8)
project(udp)

set(CMAKE_CXX_STANDARD 11)

add_executable(udp_client udp_client.cpp UDPSocket.cpp UDPSocket.h)
add_executable(udp_server udp_server.cpp)