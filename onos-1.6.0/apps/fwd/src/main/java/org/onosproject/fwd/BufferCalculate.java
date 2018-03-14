package org.onosproject.fwd;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.onlab.packet.IPv4;

import java.nio.ByteBuffer;

/**
 * Created by root on 18-2-24.
 */
public class BufferCalculate {

    ByteBuffer buf;
    byte[] bytes;

    public static final byte registerType=(byte) 0xA2;
    public static final byte getType=(byte) 0xA0;
    public static final byte dataType=(byte) 0xA1;

    byte Type;

    //外部可以访问
    public _CoLoRRegister CoLoRRegister;
    public _CoLoRGet CoLoRGet;
    public _CoLoRData ColoRData;

    public static final int len_sid = 36;
    public static final int len_nid = 16;

    BufferCalculate(ByteBuffer byteBuffer) {
        //todo:用这个方法把position复位到0
        byteBuffer.clear();

        this.buf = byteBuffer;
        this.bytes = byteBuffer.array();

        //TODO:越过Mac和Ip
        int macLen = 14;
        int ipLen = 20;
        for (int i = 0; i < macLen + ipLen; i++) {
            //弹出数据
            byteBuffer.get();
        }
        Type = byteBuffer.get();

        if (Type == registerType) {
            CoLoRRegister = new _CoLoRRegister(byteBuffer);
        } else if (Type == getType) {
            CoLoRGet = new _CoLoRGet(byteBuffer);
        } else if (Type == dataType) {
            ColoRData = new _CoLoRData(byteBuffer);
        } else {
            //TODO：never here
            System.out.println("---><-----");
        }

    }

    public void set__ipv4__dstAddr(int dstAddr) {
        byte[] dstAddr_bytes = IPv4.toIPv4AddressBytes(dstAddr);

        //ipv4目的地址偏移量
        int offset = 14 + 16;
        for (int i = 0; i < 4; i++) {
            bytes[i + offset] = dstAddr_bytes[i];
        }
    }

    public void set__ipv4__srcAddr(int srcAddr) {
        byte[] dstAddr_bytes = IPv4.toIPv4AddressBytes(srcAddr);

        //ipv4目的地址偏移量
        int offset = 14 + 12;
        for (int i = 0; i < 4; i++) {
            bytes[i + offset] = dstAddr_bytes[i];
        }
    }

    public void set__get__res(int res){
        bytes[14+20+11] = (byte)res;
    }

    public ByteBuffer getBuf() {
        buf = ByteBuffer.wrap(bytes);
        return buf;
    }

    //TODO:想定义Register包的格式
    public class _CoLoRRegister {
        public byte versionType;
        public byte ttl;
        public short totalLen;
        //36
        public byte[] sid;
        //16
        public byte[] nidS;

        _CoLoRRegister(ByteBuffer byteBuffer) {

            this.sid = new byte[len_sid];
            this.nidS = new byte[len_nid];

            versionType = registerType;
            System.out.printf("[Register packets]:");
            System.out.println("\n_______Begin__to__Analyse__to__Register_______");
            System.out.printf("versionType=%2X\n", versionType);

            ttl = byteBuffer.get();
            totalLen = byteBuffer.getShort();
            for (int i = 0; i < len_sid; i++) {
                sid[i] = byteBuffer.get();
            }
            for (int i = 0; i < len_nid; i++) {
                nidS[i] = byteBuffer.get();
            }
            System.out.printf("[SID]:");
            for (int i = 0; i < len_sid; i++) {
                System.out.printf("%c", sid[i]);
            }
            System.out.println();
            System.out.printf("[NIDs]:");
            for (int i = 0; i < len_nid; i++) {
                System.out.printf("%c", nidS[i]);
            }
            System.out.println();
        }
    }

    //TODO:想定义GET包的格式
    public class _CoLoRGet {
        public byte versionType;
        public byte ttl;
        public short totalLen;
        public short portNo1;
        public short portNo2;
        public short minpid;
        public byte pidsO;
        public byte res;
        public short mtu;
        public short checksum;
        //36
        public byte[] sid;
        //16
        public byte[] nidC;
        public int mac;
        public int offset;
        public int length;
        public int pid1;

        _CoLoRGet(ByteBuffer byteBuffer) {

            sid = new byte[len_sid];
            nidC = new byte[len_nid];

            versionType = getType;
            System.out.printf("[GET packets]:");
            for (int i = 0; i < byteBuffer.capacity(); i++) {
                System.out.printf("%2X", byteBuffer.get(i));
            }

            System.out.println("\n_______Begin__to__Analyse__to__GET_______");
            System.out.printf("versionType=%2X\n", versionType);

            ttl = byteBuffer.get();
            totalLen = byteBuffer.getShort();
            portNo1 = byteBuffer.getShort();
            portNo2 = byteBuffer.getShort();
            minpid = byteBuffer.getShort();
            pidsO = byteBuffer.get();
            res = byteBuffer.get();
            mtu = byteBuffer.getShort();
            checksum = byteBuffer.getShort();
            for (int i = 0; i < 36; i++) {
                sid[i] = byteBuffer.get();
            }
            for (int i = 0; i < 16; i++) {
                nidC[i] = byteBuffer.get();
            }
            mac = byteBuffer.getInt();
            offset = byteBuffer.getInt();
            length = byteBuffer.getInt();
            pid1 = byteBuffer.getInt();

            System.out.printf("[SID]:");
            for (int i = 0; i < len_sid; i++) {
                System.out.printf("%s", (char)sid[i]);
            }
            System.out.println();
            System.out.printf("[NID]:");
            for (int j=0; j<len_nid;j++){
                System.out.printf("%s", (char)nidC[j]);
            }
            System.out.println();
        }
    }

    //TODO:定义data包包格式
    public class _CoLoRData {
        public byte versionType;
        public byte ttl;
        public short totalLen;
        public short portNo1;
        public short portNo2;
        public short minpid;
        public byte pidsO;
        public byte res;
        public byte pid_index;
        public byte reserved;
        public short checksum;
        //16
        public byte[] nidS;
        //20
        public byte[] lsid;
        //16
        public byte[] nidC;
        public int mac;
        public int offset;
        public int length;
        public int pid1;

        _CoLoRData(ByteBuffer byteBuffer) {

            nidS = new byte[16];
            lsid = new byte[20];
            nidC = new byte[16];

            versionType = dataType;
            System.out.printf("[DATA packet]:");
            for (int i = 0; i < byteBuffer.capacity(); i++) {
                System.out.printf("%2X", byteBuffer.get(i));
            }

            System.out.println("\n_______Begin__to__Analyse__to__DATA_______");
            System.out.printf("versionType=%2X\n", versionType);

            ttl = byteBuffer.get();
            totalLen = byteBuffer.getShort();
            portNo1 = byteBuffer.getShort();
            portNo2 = byteBuffer.getShort();
            minpid = byteBuffer.getShort();
            pidsO = byteBuffer.get();
            res = byteBuffer.get();
            pid_index = byteBuffer.get();
            reserved = byteBuffer.get();
            checksum = byteBuffer.getShort();

            for (int i = 0; i < 16; i++) {
                nidS[i] = byteBuffer.get();
            }
            for (int i = 0; i < 20; i++) {
                lsid[i] = byteBuffer.get();
            }
            for (int i = 0; i < 16; i++) {
                nidC[i] = byteBuffer.get();
            }
            mac = byteBuffer.getInt();
            offset = byteBuffer.getInt();
            length = byteBuffer.getInt();
            pid1 = byteBuffer.getInt();

            System.out.printf("[NID_c]:");
            for (int i = 0; i < 16; i++) {
                System.out.printf("%c", (char)nidC[i]);
            }
            System.out.println();

        }

    }

}
