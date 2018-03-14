//TODO:AR

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

//get包头
header_type get_t {
    fields {
        version_type : 8;
        ttl : 8;
        total_len : 16;
        port_no1 :16;
        port_no2 :16;
        minpid :16;
        pids_o :8;
        res : 8;
        mtu : 16;
        checksum :16;
        sid :288;
        nid_c :128;
        mac :32;
        offset :32;
        len :32;
        pid1 :32;
    }
}

header_type register_t{
	fields{
		sid: 288;
		nid_s: 120;
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

#define IPTYPE_COLOR_GET 0xa0
#define IPTYPE_COLOR_DATA 0xa1
#define IPTYPE_COLOR_REGISTER 0xa2

//依据对包头的判断执行不同的解析包
parser parse_ipv4 {
    extract(ipv4);
	return select(ipv4.protocol){
		IPTYPE_COLOR_REGISTER: parse_register;
		IPTYPE_COLOR_GET: parse_get;
		default: ingress;
	}
}

header register_t myregister;
parser parse_register{
	extract(myregister);
	return ingress;	
}

header get_t get;
parser parse_get{
    extract(get);
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

#define CPU_PORT 255
action send_to_cpu() {
    modify_field(standard_metadata.egress_spec, CPU_PORT);
}

table protocol_to_cpu {
	reads {
		ipv4.protocol : exact;
	}
	actions {
		send_to_cpu;
		_drop;
	}
	size : 1024;
}

//域内SID需要设置的所有动作
action inside_sid_set(srcAddr,dstAddr,res) {
    modify_field(ipv4.srcAddr, srcAddr);
    modify_field(ipv4.dstAddr, dstAddr);
    modify_field(get.res, res);
}


table sid_nid {
    reads {
        get.sid: exact;
    }
    actions {
        inside_sid_set;
        send_to_cpu;
        _drop;
    }
}


control ingress {
        if(ethernet.etherType == ETHERTYPE_IPV4 and ipv4.protocol == IPTYPE_COLOR_REGISTER){
            apply(protocol_to_cpu);
        }

        if(ethernet.etherType == ETHERTYPE_IPV4 and ipv4.protocol == IPTYPE_COLOR_GET){
            apply(sid_nid);
        }

        if( standard_metadata.egress_spec != CPU_PORT ){
            apply(dstAddr_port);
        }
}

control egress {

}


