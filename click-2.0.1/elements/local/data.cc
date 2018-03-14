#include <click/config.h>
#include "data.hh"
#include "register.hh"
#include <iostream>
CLICK_DECLS

Data::Data() {
}

Data::~Data() {
}

Packet *Data::simple_action(Packet *p) {
    return p;
}

void
Data::push(int port, Packet *packet) {

    packet->push(sizeof(struct DATA));
    static struct DATA data;

    data.version_type = 0xA1;

    static int counter = 0;
    static int total = 0;
    if(counter <= 0){
        std::cout<<"Please input the counter: ";
        std::cin>>counter;

        std :: cout <<"Please input the SID:";
        std :: string a;
        std :: cin >> a;

        for (int i = 0; i < 36; i++) {
            data.sid[i] = '_';
        }
        for (int i=0;i<a.length() && i<36;i++){
            data.sid[i] = a[i];
        }
    }
    total++;
    std::cout << "send "<< total <<"| left " << counter <<std::endl;
    counter--;
    for (int i = 0; i < 16; i++) {
        data.nid_c[i] = '_';
    }

    data.nid_c[0]='C';
    data.nid_c[1]='l';
    data.nid_c[2]='i';
    data.nid_c[3]='e';
    data.nid_c[4]='n';
    data.nid_c[5]='t';
    data.nid_c[6]='1';

    memcpy((unsigned char *) packet->data(), &data, sizeof(struct DATA));
    output(0).push(packet);
}

CLICK_ENDDECLS
EXPORT_ELEMENT(Data)

ELEMENT_MT_SAFE(Data)
