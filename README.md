## Bench
[![Build Status](https://travis-ci.org/florentw/bench.svg?branch=master)](https://travis-ci.org/florentw/bench)

Bench is an actor framework to help write performance benchmarks in Java for distributed applications.
It uses Java 8 and the network communication layer within the actor's cluster can be switched to either:
- Using a lightweight embedded JMS server: [FFMQ](http://timewalker74.github.io/ffmq/), all actor communication then goes through a single master node.
- Using [Jgroups](http://www.jgroups.org/) to form a peer to peer network where actors can communicate directly with one another.

## Installation
On linux, the Jgroups cluster implementation displays warnings when the following kernel properties are not set to the following values:
| System property | Value |
|-----------------|-------|
|net.core.rmem_max|5242880|
|net.core.wmem_max|5242880|

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
The source code is licensed under the Apache v2 license, see [LICENSE](../blob/master/LICENSE) file.
