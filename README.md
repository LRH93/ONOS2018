2018年3月11日于在北京航空航天大学，创建第一版本。

以下是 .bashrc 的内容

		export JAVA_HOME=/root/onos/Applications/jdk1.8.0_121
		export JRE_HOME=$JAVA_HOME/jre
		export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JRE_HOME/lib
		export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
		echo “Java has been installed”

		export KARAF_ROOT=/root/onos/Applications/apache-karaf-3.0.5
		export PATH=$KARAF_ROOT/bin:$PATH
		echo “karaf has been installed”

		export M2_HOME=/root/onos/Applications/apache-maven-3.3.9
		export PATH=$PATH:$M2_HOME/bin
		echo “maven has been installed”

		export ONOS_ROOT=/root/onos/onos-1.6.0/
		source $ONOS_ROOT/tools/dev/bash_profile
		echo "ONOS has been installed"

		export onos_p4_dev_ROOT=/root/onos/onos-p4-dev

		source /root/onos/onos-p4-dev/tools/bash_profile
		echo "bmv2 has been installed"

