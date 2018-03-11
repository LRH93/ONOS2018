#include <click/config.h>
#include "register.hh"
#include <iostream>
CLICK_DECLS

Register::Register() {
}

Register::~Register() {
}

Packet *Register::simple_action(Packet *p) {
    return p;
}

void
Register::push(int port, Packet *packet) {
    packet->push(sizeof(struct REG));
    static struct REG reg;
    reg.version_type = 0xA2;

    static int counter = 0;
    static int total = 0;
    if(counter <= 0){
        std::cout<<"Please input the counter: ";
        std::cin>>counter;

        std :: cout <<"Please input the SID:";
        std :: string a;
        std :: cin >> a;

        for (int i = 0; i < 36; i++) {
            reg.sid[i] = '_';
        }
        for (int i=0;i<a.length() && i<36;i++){
            reg.sid[i] = a[i];
        }
    }
    total++;
    std::cout << "send "<< total <<"| left " << counter <<std::endl;
    counter--;
    for (int i = 0; i < 16; i++) {
        reg.nid_s[i] = '_';
    }
    reg.nid_s[0]='S';
    reg.nid_s[1]='e';
    reg.nid_s[2]='r';
    reg.nid_s[3]='v';
    reg.nid_s[4]='e';
    reg.nid_s[5]='r';
    reg.nid_s[6]='1';

    memcpy((unsigned char *) packet->data(), &reg, sizeof(struct REG));
    output(0).push(packet);
}

CLICK_ENDDECLS
EXPORT_ELEMENT(Register)

ELEMENT_MT_SAFE(Register)
