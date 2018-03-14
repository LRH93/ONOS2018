#ifndef CLICK_Get_HH
#define CLICK_Get_HH
#include <click/element.hh>
CLICK_DECLS

class Data : public Element
{
public:
    Data();
    ~Data();
    const char *class_name() const
    {
        return "Data";
    }
    const char *port_count() const
    {
        return "1/1";
    }
    void push(int port, Packet *packet);
    Packet *simple_action(Packet *packet);
};



struct DATA
{
    uint8_t version_type;
    uint8_t ttl;
    uint16_t total_len;
    uint16_t port_no1;
    uint16_t port_no2;
    uint16_t minpid;
    uint8_t pids_o;
    uint8_t res;
    uint8_t pid_index;
    uint8_t reserved;
    uint16_t checksum;
    /*
    uint8_t nid_s[16];
    uint8_t l_sid[20];
    */
    uint8_t sid[36];
    uint8_t nid_c[16];
    uint32_t mac;
    uint32_t offset;
    uint32_t length;
    uint32_t pid1;
};

CLICK_ENDDECLS
#endif
