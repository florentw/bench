## Bench
[![Build Status](https://travis-ci.org/florentw/bench.svg?branch=master)](https://travis-ci.org/florentw/bench)
[![Quality gate](https://sonarqube.com/api/badges/gate?key=io.amaze%3Abench)](https://sonarqube.com/dashboard?id=io.amaze%3Abench)
[![Code coverage](https://sonarqube.com/api/badges/measure?key=io.amaze%3Abench&metric=coverage)](https://sonarqube.com/dashboard?id=io.amaze%3Abench)
[![Lines of code](https://sonarqube.com/api/badges/measure?key=io.amaze%3Abench&metric=ncloc)](https://sonarqube.com/dashboard?id=io.amaze%3Abench)

Bench is an actor framework to help write performance benchmarks in Java for distributed applications.

It uses Java 8 and the network communication layer within the actor's cluster can be switched to either:
- Using a lightweight embedded JMS server: [FFMQ](http://timewalker74.github.io/ffmq/), all actor communication then goes through a single master node.
- Using [Jgroups](http://www.jgroups.org/) to form a peer to peer network where actors can communicate directly with one another.

Bench includes actors to watch the host system and processes using the excellent [OSHI library](https://github.com/oshi/oshi):
- [SystemWatcherActor](./agent/src/main/java/io/amaze/bench/actor/SystemWatcherActor.java)
- [ProcessWatcherActor](./agent/src/main/java/io/amaze/bench/actor/ProcessWatcherActor.java)

## Installation
On linux, the Jgroups cluster implementation displays warnings when the following kernel properties are not set to the following values:

System property  |Value 
-----------------|-------
net.core.rmem_max|5242880
net.core.wmem_max|5242880

**Quick runtime fix:**
```bash
sysctl -w net.core.rmem_max=5242880
sysctl -w net.core.wmem_max=5242880
```

**Permanent fix**
Configure /etc/sysctl.conf with correct max mem size:
```
net.core.rmem_max=5242880
net.core.wmem_max=5242880
```

Then reload sysctl parameters
```bash
sudo sysctl -p /etc/sysctl.conf
```

## Contributors
* Florent Weber <florent.weber@gmail.com>

## License
The source code is licensed under the Apache v2 license, see [LICENSE](./LICENSE) file.
