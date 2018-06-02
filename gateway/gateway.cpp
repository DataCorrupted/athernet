#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>

using namespace std;

int getUnsignedByte(){
	return (int)(getchar() & 0xff);
}
void send(){
	// Receive a pack from athernet.
	int len = getUnsignedByte() << 8 + getUnsignedByte();
	int data[len];
	for (int i=0; i<len; i++){
		data[len] = getUnsignedByte();
	}

	// Make a UDP Packet.
	// IP: data[0].data[1].data[2].data[3]
	// Port: data[4] << 8 + data[5]
	// Data: data[6:]

	// Change Source and port.
	; 

	// Send the UDP Packet.
	;
}	

int main(int argc, char *argv[]){
	string str;
	putchar(argc);
	char c;
	cerr << "I am cpp. I received a str from Java:\n  ";
	for (int i=0; i<3; i++){
		cerr << getchar() << " ";
	}
	cerr << endl;

	while (1){
		send();
	}
	return 0;
}