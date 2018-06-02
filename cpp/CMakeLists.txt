cmake_minimum_required(VERSION 2.8)
project(udp)

set(CMAKE_CXX_STANDARD 11)

file(GLOB UDP_SRC udp_lib/*(.h|.cpp))
file(GLOB NAT_SRC nat_packet/*(.h|.cpp))
file(GLOB GATE_SRC gateway/*(.h|.cpp))


include_directories(udp_lib)
include_directories(nat_packet)
include_directories(gateway)


add_executable(udp_client udp_client.cpp ${UDP_SRC} ${NAT_SRC})
add_executable(udp_server udp_server.cpp ${UDP_SRC} ${NAT_SRC})
add_executable(gateway gateway.cpp ${UDP_SRC} ${NAT_SRC} ${GATE_SRC})
add_executable(nat_client nat_client.cpp ${UDP_SRC})

add_executable(nat_test test/nat_test.cpp ${NAT_SRC})