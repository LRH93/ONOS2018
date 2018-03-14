//TODO:IR

header_type ethernet_t {
    fields {
        dstAddr : 48;
        srcAddr : 48;
        etherType : 16;
    }
}

header_type ipv4_t {
    fields {
        version : 4;
        ihl : 4;
        diffserv : 8;
        totalLen : 16;
        identification : 16;
        flags : 3;
        fragOffset : 13;
        ttl : 8;
        protocol : 8;
        hdrChecksum : 16;
        srcAddr : 32;
        dstAddr: 32;
    }
}

parser start {
    return parse_ethernet;
}

#define ETHERTYPE_IPV4 0x0800

header ethernet_t ethernet;

parser parse_ethernet {
    extract(ethernet);
    return select(latest.etherType) {
        ETHERTYPE_IPV4 : parse_ipv4;
        default: ingress;
    }
}

header ipv4_t ipv4;

//依据对包头的判断执行不同的解析包
parser parse_ipv4 {
    extract(ipv4);
    return ingress;
}

action _drop() {
    drop();
}

action set_egress_port(port) {
    modify_field(standard_metadata.egress_spec, port);
}

table dstAddr_port {
    reads {
        ipv4.dstAddr : exact;
    }
    actions {
        set_egress_port;
        _drop;
    }
    size: 1024;
}

control ingress {
    apply(dstAddr_port);
}

control egress {

}
