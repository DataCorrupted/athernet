#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>

using namespace std;	
int main(int argc, char *argv[]){
	string str;
	cout << argc << endl;
	getline(cin, str);
	cerr << "I am cpp. I received a str from Java:\n  " << str << endl;
	return 0;
}