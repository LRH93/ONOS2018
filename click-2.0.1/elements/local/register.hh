#ifndef CLICK_Register_HH
#define CLICK_Register_HH
#include <click/element.hh>
CLICK_DECLS

class Register : public Element
{
public:
    Register();
    ~Register();
    const char *class_name() const
    {
        return "Register";
    }
    const char *port_count() const
    {
        return "1/1";
    }
    void push(int port, Packet *packet);
    Packet *simple_action(Packet *packet);
};

//注册sid->nid的映射关系
struct REG
{
    uint8_t version_type;
    uint8_t ttl;
    uint16_t total_len;
    uint8_t sid[36];
    uint8_t nid_s[16];
}__attribute__((packed));

CLICK_ENDDECLS
#endif
