get::Get;
TimedSource(INTERVAL 0.1,DATA PID1)->[0]get;
out1::Queue(200)->ToDevice(Client1-eth0);
get[0]->IPEncap(0xa0,192.111.111.111,192.222.222.222)->EtherEncap(0X0800,AA:AA:AA:AA:AA:AA,BB:BB:BB:BB:BB:BB)->out1;