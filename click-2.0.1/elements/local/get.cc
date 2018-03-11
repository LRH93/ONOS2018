#include <click/config.h>
#include "get.hh"
#include "register.hh"
#include <iostream>
CLICK_DECLS

Get::Get() {
}

Get::~Get() {
}

Packet *Get::simple_action(Packet *p) {
    return p;
}

void
Get::push(int port, Packet *packet) {

    packet->push(sizeof(struct GET));
    static struct GET get;

    get.version_type = 0xA0;

    static int counter = 0;
    static int total = 0;
    if(counter <= 0){
        std::cout<<"Please input the counter: ";
        std::cin>>counter;

        std :: cout <<"Please input the SID:";
        std :: string a;
        std :: cin >> a;

        for (int i = 0; i < 36; i++) {
            get.sid[i] = '_';
        }
        for (int i=0;i<a.length() && i<36;i++){
            get.sid[i] = a[i];
        }
    }
    total++;
    std::cout << "send "<< total <<"| left " << counter <<std::endl;
    counter--;
    for (int i = 0; i < 16; i++) {
        get.nid_c[i] = '_';
    }
    //TODO:表示没有被RM处理过
    get.res=0;

    get.nid_c[0]='C';
    get.nid_c[1]='l';
    get.nid_c[2]='i';
    get.nid_c[3]='e';
    get.nid_c[4]='n';
    get.nid_c[5]='t';
    get.nid_c[6]='1';

    memcpy((unsigned char *) packet->data(), &get, sizeof(struct GET));
    output(0).push(packet);
}

CLICK_ENDDECLS
EXPORT_ELEMENT(Get)

ELEMENT_MT_SAFE(Get)
