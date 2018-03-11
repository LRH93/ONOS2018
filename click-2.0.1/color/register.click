register::Register;
TimedSource(INTERVAL 1,DATA 1_2_3_4)->[0]register;
out1::Queue(200)->ToDevice(Server1-eth0);
register[0]->IPEncap(0xa2,0.0.0.12,0.0.0.13)->EtherEncap(0X0800,AA:AA:AA:AA:AA:AA,BB:BB:BB:BB:BB:BB)->out1;
