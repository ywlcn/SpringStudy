課題：

- 現在認証情報はWorkerNodeのContainerdに設定していますが、K8sで管理できるようにするのは？



# 1. 環境概要

## 1.1 ソフトウェア

- CentOS版本

  http://ftp-srv2.kddilabs.jp/Linux/packages/CentOS/8-stream/isos/x86_64/CentOS-Stream-8-x86_64-20221005-dvd1.iso

- VirtualBox 版本

  VirtualBox 7.0.2 r154219 (Qt5.15.2)

## 1.2 クラスター構成

| PC名                                                    | 外部Ip         | 内部      |      |
| ------------------------------------------------------- | -------------- | --------- | ---- |
| k8s-master.vbox.local                                   | 192.168.56.100 | 10.0.2.20 |      |
| k8s-node1.vbox.local                                    | 192.168.56.101 | 10.0.2.21 |      |
| k8s-node2.vbox.local                                    | 192.168.56.102 | 10.0.2.22 |      |
| k8s-node3.vbox.local                                    | 192.168.56.103 | 10.0.2.23 |      |
| gitlab.vbox.local<br/>root/123456789<br/>user/12345678 | 192.168.56.111 | 10.0.2.31 |      |
| nexus.vbox.local                                        | 192.168.56.112 | 10.0.2.32 |      |

※ 認証情報user/123456  root/123456

※ DNS情報

```bash
sudo vi /etc/hosts

192.168.56.100    k8s-master.vbox.local   
192.168.56.101    k8s-node1.vbox.local   
192.168.56.102    k8s-node2.vbox.local
192.168.56.103    k8s-node3.vbox.local
192.168.56.111    gitlab.vbox.local gitlab.vbox.com
192.168.56.112    nexus.vbox.local nexus.vbox.com
```

## 1.3 参考メモ

### 1.3.1 VirtualBoxの設定

```bash
########################## 创建一个NAT网络
VBoxManage natnetwork add --netname "kubernetes" --network "10.0.2.0/24" --enable
# 查看
VBoxManage natnetwork list
# 结果如下
# NAT Networks:
#
# Name:        kubernetes
# Network:     10.0.2.0/24
# Gateway:     10.0.2.1
# IPv6:        No
# Enabled:     Yes
#
# 1 network found

########################## 创建仅主机网络
# 查看HostOnly的网络接口
VBoxManage list hostonlyifs
 
# 如果没有既存的网络接口，或者不希望使用既存的网络接口，可以自己创建一个仅主机的网络
VBoxManage hostonlyif create
# 结果会显示类似如下的消息
# Interface 'VirtualBox Host-Only Ethernet Adapter' was successfully created
 
# 配置IP和子网掩码
VBoxManage hostonlyif ipconfig "VirtualBox Host-Only Ethernet Adapter" --ip 192.168.56.1 --netmask 255.255.255.0
```

### 1.3.2 VMの設定

```bash
rem off

VBoxManage createvm --name base-os --groups /Kubernetes --ostype RedHat_64 --register

rem "描述信息为“CentOS 8.0最小安装”"
rem "CPU： 2核"
rem "内存： 2G"
rem "显卡驱动： VBoxSVGA"
rem "主板芯片组：ICH9"
rem "硬件时钟：使用UTC时间"
rem "启动项： DVD光驱 > 硬盘 > 无 > 无"
rem "声卡：无"
VBoxManage modifyvm base-os --description "CentOS 8.0 minimal installation" --cpus 2 --memory 2048 --rtcuseutc on --graphicscontroller vboxsvga --chipset ich9 --boot1 dvd --boot2 disk --boot3 none --boot4 none --audio none

rem "创建两个磁盘驱动器接口"
rem "使用芯片组：PIIX4"
VBoxManage storagectl base-os --name IDE --add ide --controller PIIX4 --portcount 2 --bootable on

rem "向光驱中装入盘片(Centos的ISO)"
rem "该光驱连接到IDE的第一接口（port 0）的主设备(device 0)"
VBoxManage storageattach base-os --storagectl IDE --port 0 --device 0 --type dvddrive --medium E:\Download\CentOS-Stream-8-x86_64-20221005-dvd1.iso

rem "创建硬盘"
rem "虚拟硬盘文件名k8s-master.vdi"
rem "大小：20480M = 20G"
VBoxManage createmedium disk --filename base-os.vdi --size 20480

rem "添加SATA磁盘驱动器"
rem "芯片组：Intel AHCI"
rem "提供4个插槽，最多连4个存储设备"
VBoxManage storagectl base-os --name SATA --add sata --controller IntelAhci --portcount 4 --bootable on

rem "挂载硬盘到磁盘驱动器"
VBoxManage storageattach base-os --storagectl SATA --port 0 --device 0 --type hdd --medium base-os.vdi
rem "向光驱中装入盘片(这里使用了kickstart的自动安装功能，在这个光盘内放置了安装配置，也有其他方法请参看https://docs.centos.org/en-US/centos/install-guide/Kickstart2/)"
VBoxManage storageattach base-os --storagectl SATA --port 1 --device 0 --type dvddrive --medium E:\output\OEMDRV.iso

rem "连接网络设备"
rem "第一个网卡连接到kubernetes的虚拟交换机"
rem "第二个网卡连接到仅主机网络的虚拟交换机"
VBoxManage modifyvm base-os  --nic1 natnetwork --nat-network1 kubernetes --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"

```

### 1.3.3 インストール設定

使用下面的设置文件，在启动画面按下`Tab`输入`inst.ks=cdrom`参数，安装程序就会查找所有的光驱，找到下面的配置

```bash
#version=RHEL8
ignoredisk --only-use=sda

# System bootloader configuration
bootloader --location=mbr --boot-drive=sda
autopart --type=lvm --nohome --noswap

# Partition clearing information
clearpart --all --initlabel --drives=sda

# Shutdown after installation
shutdown

# Use graphical install
graphical

# Use CDROM installation media
cdrom

# Keyboard layouts
keyboard --vckeymap=us --xlayouts='us'

# System language
lang en_US.UTF-8

# Network information
network  --bootproto=static --device=enp0s3 --gateway=10.0.2.1 --ip=10.0.2.19 --nameserver=8.8.8.8,4.4.4.4 --netmask=255.255.255.0 --noipv6 --activate
network  --bootproto=static --device=enp0s8 --ip=192.168.56.99 --netmask=255.255.255.0 --noipv6 --activate
network  --hostname=base-os.vbox.local

# Root password
rootpw --iscrypted $6$jI5tly7EE.ibOTHa$0fwsMzcVFXIQ4JkYUIvD7PUdrreTmrBCfkHPvMY3GqkXDpwQi4HZFYQ72nqu2lDww1ANK5uApBaCi5Zm/4gNS.
# SELinux configuration
selinux --disabled
firewall --disabled
# Run the Setup Agent on first boot
firstboot --enable
# Do not configure the X Window System
skipx
# System services
services --enabled="chronyd"
# System timezone
timezone Asia/Tokyo --isUtc --ntpservers=2.centos.pool.ntp.org,2.centos.pool.ntp.org,2.centos.pool.ntp.org,2.centos.pool.ntp.org
user --groups=wheel --name=user --password=$6$jI5tly7EE.ibOTHa$0fwsMzcVFXIQ4JkYUIvD7PUdrreTmrBCfkHPvMY3GqkXDpwQi4HZFYQ72nqu2lDww1ANK5uApBaCi5Zm/4gNS. --iscrypted --uid=1001 --gid=100

%post --logfile=/root/ks-post.log
# wheel user setting sudo
sed -i -e 's/^%wheel/# %wheel/g' -e 's/^# %wheel\tALL=(ALL)\tNOPASSWD: ALL$/%wheel\tALL=(ALL)\tNOPASSWD: ALL/g' /etc/sudoers
%end

%packages
@^minimal-environment
emacs-nox
python3
python3-libselinux

%end

%addon com_redhat_kdump --disable --reserve-mb='auto'

%end

%anaconda
pwpolicy root --minlen=6 --minquality=1 --notstrict --nochanges --notempty
pwpolicy user --minlen=6 --minquality=1 --notstrict --nochanges --emptyok
pwpolicy luks --minlen=6 --minquality=1 --notstrict --nochanges --notempty
%end
```

# 2. Kubernetusクラスターのインストール

## 2.1 準備

### 2.1.1 OS設定



### 2.1.2 Dockerインストール

```bash
$ sudo yum install -y yum-utils

$ sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
    
    
    sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin
    
    
```

## 2.2 Masterノート



```bash
rm /etc/containerd/config.toml
systemctl restart containerd



kubeadm init --apiserver-advertise-address=192.168.56.100 --pod-network-cidr=10.244.0.0/16 --service-cidr=10.96.0.0/12 --cri-socket="/run/containerd/containerd.sock"


```

- 結果

```bash
$ sudo kubeadm init --apiserver-advertise-address=192.168.56.100 --pod-network-cidr=10.244.0.0/16 --service-cidr=10.96.0.0/12 --cri-socket="/run/containerd/containerd.sock"
W1120 21:25:14.088057    1683 initconfiguration.go:119] Usage of CRI endpoints without URL scheme is deprecated and can cause kubelet errors in the future. Automatically prepending scheme "unix" to the "criSocket" with value "/run/containerd/containerd.sock". Please update your configuration!
[init] Using Kubernetes version: v1.25.4
[preflight] Running pre-flight checks
        [WARNING FileExisting-tc]: tc not found in system path
[preflight] Pulling images required for setting up a Kubernetes cluster
[preflight] This might take a minute or two, depending on the speed of your internet connection
[preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
[certs] Using certificateDir folder "/etc/kubernetes/pki"
[certs] Generating "ca" certificate and key
[certs] Generating "apiserver" certificate and key
[certs] apiserver serving cert is signed for DNS names [k8s-master.vbox.local kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.96.0.1 192.168.56.100]
[certs] Generating "apiserver-kubelet-client" certificate and key
[certs] Generating "front-proxy-ca" certificate and key
[certs] Generating "front-proxy-client" certificate and key
[certs] Generating "etcd/ca" certificate and key
[certs] Generating "etcd/server" certificate and key
[certs] etcd/server serving cert is signed for DNS names [k8s-master.vbox.local localhost] and IPs [192.168.56.100 127.0.0.1 ::1]
[certs] Generating "etcd/peer" certificate and key
[certs] etcd/peer serving cert is signed for DNS names [k8s-master.vbox.local localhost] and IPs [192.168.56.100 127.0.0.1 ::1]
[certs] Generating "etcd/healthcheck-client" certificate and key
[certs] Generating "apiserver-etcd-client" certificate and key
[certs] Generating "sa" key and public key
[kubeconfig] Using kubeconfig folder "/etc/kubernetes"
[kubeconfig] Writing "admin.conf" kubeconfig file
[kubeconfig] Writing "kubelet.conf" kubeconfig file
[kubeconfig] Writing "controller-manager.conf" kubeconfig file
[kubeconfig] Writing "scheduler.conf" kubeconfig file
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Starting the kubelet
[control-plane] Using manifest folder "/etc/kubernetes/manifests"
[control-plane] Creating static Pod manifest for "kube-apiserver"
[control-plane] Creating static Pod manifest for "kube-controller-manager"
[control-plane] Creating static Pod manifest for "kube-scheduler"
[etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"
[wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests". This can take up to 4m0s
[apiclient] All control plane components are healthy after 8.003189 seconds
[upload-config] Storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace
[kubelet] Creating a ConfigMap "kubelet-config" in namespace kube-system with the configuration for the kubelets in the cluster
[upload-certs] Skipping phase. Please see --upload-certs
[mark-control-plane] Marking the node k8s-master.vbox.local as control-plane by adding the labels: [node-role.kubernetes.io/control-plane node.kubernetes.io/exclude-from-external-load-balancers]
[mark-control-plane] Marking the node k8s-master.vbox.local as control-plane by adding the taints [node-role.kubernetes.io/control-plane:NoSchedule]
[bootstrap-token] Using token: bz32si.xcpf471mqg1c6s9q
[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to get nodes
[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
[bootstrap-token] Configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
[bootstrap-token] Configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
[bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace
[kubelet-finalize] Updating "/etc/kubernetes/kubelet.conf" to point to a rotatable kubelet client certificate and key
[addons] Applied essential addon: CoreDNS
[addons] Applied essential addon: kube-proxy

Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.56.100:6443 --token bz32si.xcpf471mqg1c6s9q \
        --discovery-token-ca-cert-hash sha256:5921f1514155af783d023cbea2b7bd0a94c1a5c8eb2aa75149571353e91f39fe
```

## 2.3 ワーカーノード

- VMは複製で作成した原因かもしれないですが、下記のようなエラーがでたら、複数回再起動してください。

```bash
I1120 22:25:20.991456    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: protocol not available
```

- 実施結果

```bash
[user@k8s-node1 ~]$ sudo kubeadm join 192.168.56.100:6443 --token bz32si.xcpf471mqg1c6s9q --discovery-token-ca-cert-hash sha256:5921f1514155af783d023cbea2b7bd0a94c1a5c8eb2aa75149571353e91f39fe --v 5
I1120 22:25:20.930306    1853 join.go:416] [preflight] found NodeName empty; using OS hostname as NodeName
I1120 22:25:20.930422    1853 initconfiguration.go:116] detected and using CRI socket: unix:///var/run/containerd/containerd.sock
[preflight] Running pre-flight checks
I1120 22:25:20.930452    1853 preflight.go:92] [preflight] Running general checks
I1120 22:25:20.930469    1853 checks.go:280] validating the existence of file /etc/kubernetes/kubelet.conf
I1120 22:25:20.930474    1853 checks.go:280] validating the existence of file /etc/kubernetes/bootstrap-kubelet.conf
I1120 22:25:20.930479    1853 checks.go:104] validating the container runtime
I1120 22:25:20.945132    1853 checks.go:329] validating the contents of file /proc/sys/net/bridge/bridge-nf-call-iptables
I1120 22:25:20.945168    1853 checks.go:329] validating the contents of file /proc/sys/net/ipv4/ip_forward
I1120 22:25:20.945188    1853 checks.go:644] validating whether swap is enabled or not
I1120 22:25:20.945203    1853 checks.go:370] validating the presence of executable crictl
I1120 22:25:20.945216    1853 checks.go:370] validating the presence of executable conntrack
I1120 22:25:20.945220    1853 checks.go:370] validating the presence of executable ip
I1120 22:25:20.945223    1853 checks.go:370] validating the presence of executable iptables
I1120 22:25:20.945227    1853 checks.go:370] validating the presence of executable mount
I1120 22:25:20.945232    1853 checks.go:370] validating the presence of executable nsenter
I1120 22:25:20.945237    1853 checks.go:370] validating the presence of executable ebtables
I1120 22:25:20.945240    1853 checks.go:370] validating the presence of executable ethtool
I1120 22:25:20.945244    1853 checks.go:370] validating the presence of executable socat
I1120 22:25:20.945248    1853 checks.go:370] validating the presence of executable tc
        [WARNING FileExisting-tc]: tc not found in system path
I1120 22:25:20.945270    1853 checks.go:370] validating the presence of executable touch
I1120 22:25:20.945278    1853 checks.go:516] running all checks
I1120 22:25:20.950652    1853 checks.go:401] checking whether the given node name is valid and reachable using net.LookupHost
I1120 22:25:20.950745    1853 checks.go:610] validating kubelet version
I1120 22:25:20.983077    1853 checks.go:130] validating if the "kubelet" service is enabled and active
I1120 22:25:20.990432    1853 checks.go:203] validating availability of port 10250
I1120 22:25:20.990541    1853 checks.go:280] validating the existence of file /etc/kubernetes/pki/ca.crt
I1120 22:25:20.990552    1853 checks.go:430] validating if the connectivity type is via proxy or direct
I1120 22:25:20.990570    1853 join.go:533] [preflight] Discovering cluster-info
I1120 22:25:20.990581    1853 token.go:80] [discovery] Created cluster-info discovery client, requesting info from "192.168.56.100:6443"
I1120 22:25:20.991456    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: protocol not available
I1120 22:25:26.903220    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: protocol not available
I1120 22:25:33.316225    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: protocol not available
I1120 22:25:39.314951    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: connection refused
I1120 22:25:44.975837    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: connection refused
I1120 22:25:50.614562    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: connection refused
I1120 22:25:56.652357    1853 token.go:217] [discovery] Failed to request cluster-info, will try again: Get "https://192.168.56.100:6443/api/v1/namespaces/kube-public/configmaps/cluster-info?timeout=10s": dial tcp 192.168.56.100:6443: connect: connection refused
I1120 22:26:01.906699    1853 token.go:118] [discovery] Requesting info from "192.168.56.100:6443" again to validate TLS against the pinned public key
I1120 22:26:01.912840    1853 token.go:135] [discovery] Cluster info signature and contents are valid and TLS certificate validates against pinned roots, will use API Server "192.168.56.100:6443"
I1120 22:26:01.912861    1853 discovery.go:52] [discovery] Using provided TLSBootstrapToken as authentication credentials for the join process
I1120 22:26:01.912868    1853 join.go:547] [preflight] Fetching init configuration
I1120 22:26:01.912870    1853 join.go:593] [preflight] Retrieving KubeConfig objects
[preflight] Reading configuration from the cluster...
[preflight] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -o yaml'
I1120 22:26:01.916727    1853 kubelet.go:74] attempting to download the KubeletConfiguration from ConfigMap "kubelet-config"
I1120 22:26:01.919123    1853 interface.go:432] Looking for default routes with IPv4 addresses
I1120 22:26:01.919142    1853 interface.go:437] Default route transits interface "enp0s3"
I1120 22:26:01.919213    1853 interface.go:209] Interface enp0s3 is up
I1120 22:26:01.919237    1853 interface.go:257] Interface "enp0s3" has 2 addresses :[10.0.2.21/24 fe80::a00:27ff:fefc:7473/64].
I1120 22:26:01.919258    1853 interface.go:224] Checking addr  10.0.2.21/24.
I1120 22:26:01.919276    1853 interface.go:231] IP found 10.0.2.21
I1120 22:26:01.919279    1853 interface.go:263] Found valid IPv4 address 10.0.2.21 for interface "enp0s3".
I1120 22:26:01.919282    1853 interface.go:443] Found active IP 10.0.2.21
I1120 22:26:01.949592    1853 preflight.go:103] [preflight] Running configuration dependant checks
I1120 22:26:01.949607    1853 controlplaneprepare.go:220] [download-certs] Skipping certs download
I1120 22:26:01.949612    1853 kubelet.go:120] [kubelet-start] writing bootstrap kubelet config file at /etc/kubernetes/bootstrap-kubelet.conf
I1120 22:26:01.950154    1853 kubelet.go:135] [kubelet-start] writing CA certificate at /etc/kubernetes/pki/ca.crt
I1120 22:26:01.950814    1853 kubelet.go:156] [kubelet-start] Checking for an existing Node in the cluster with name "k8s-node1.vbox.local" and status "Ready"
I1120 22:26:01.953261    1853 kubelet.go:171] [kubelet-start] Stopping the kubelet
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Starting the kubelet
[kubelet-start] Waiting for the kubelet to perform the TLS Bootstrap...
I1120 22:26:32.041714    1853 kubelet.go:219] [kubelet-start] preserving the crisocket information for the node
I1120 22:26:32.041761    1853 patchnode.go:31] [patchnode] Uploading the CRI Socket information "unix:///var/run/containerd/containerd.sock" to the Node API object "k8s-node1.vbox.local" as an annotation
I1120 22:26:32.041796    1853 cert_rotation.go:137] Starting client certificate rotation controller

This node has joined the cluster:
* Certificate signing request was sent to apiserver and a response was received.
* The Kubelet was informed of the new secure connection details.

Run 'kubectl get nodes' on the control-plane to see this node join the cluster.


```

```bash
$ kubectl get node -o wide
NAME                    STATUS     ROLES           AGE     VERSION   INTERNAL-IP      EXTERNAL-IP   OS-IMAGE          KERNEL-VERSION          CONTAINER-RUNTIME
k8s-master.vbox.local   Ready      control-plane   2d11h   v1.25.3   192.168.56.100   <none>        CentOS Stream 8   4.18.0-408.el8.x86_64   containerd://1.6.8
k8s-node1.vbox.local    Ready      <none>          2d10h   v1.25.3   192.168.56.101   <none>        CentOS Stream 8   4.18.0-408.el8.x86_64   containerd://1.6.8
k8s-node2.vbox.local    Ready      <none>          2d10h   v1.25.3   192.168.56.102   <none>        CentOS Stream 8   4.18.0-408.el8.x86_64   containerd://1.6.8
```



## 2.4 リポジトリの設定

https://kubernetes.io/zh-cn/docs/tasks/configure-pod-container/pull-image-private-registry/



- Dockerの設定

  ```bash
  sudo cat /etc/docker/daemon.json
  {
         "insecure-registries":[ "nexus.vbox.local:8443"]
  }
  
  
  # デーモン再起動して設定読み直し
  sudo systemctl daemon-reload
  sudo systemctl restart docker 
  ```

- containerdの設定方法

  https://github.com/containerd/containerd/blob/main/docs/cri/config.md#registry-configuration

  ```bash
  ### デフォルト設定生成
  $ containerd config default  > /etc/containerd/config.toml
  ### 設定ファイルにリポジトリの部分を下記のように変更
      [plugins."io.containerd.grpc.v1.cri".registry]
        config_path = "/etc/containerd/certs.d"
  
  ### リポジトリの設定
  [root@k8s-node1 containerd]# pwd
  /etc/containerd
  [root@k8s-node1 containerd]# tree
  .
  ┣━ certs.d
  ┃ 　┣━ docker.io
  ┃ 　┃ 　　┗ hosts.toml
  ┃ 　┗ nexus.vbox.local:8443
  ┃    　　 ┣━ hosts.toml
  ┃    　　 ┗ nexus.vbox.download.crt　　　　　　・・・ブラウザからttpsでNexusサーバにアクセス後、ダウンロードした証明書
  ┗ config.toml
  
  ###  hosts.tomlの内容
  server = "https://nexus.vbox.local:8443"
  
  [host."https://nexus.vbox.local:8443"]
    capabilities = ["pull", "resolve", "push"]
    ca = "/etc/containerd/certs.d/nexus.vbox.local:8443/nexus.vbox.download.crt" 
  
  
  sudo systemctl restart containerd 
  
  
  sudo crictl config --set image-endpoint=unix:///run/containerd/containerd.sock
  sudo crictl config --set runtime-endpoint=unix:///run/containerd/containerd.sock
  ###　デフォルトでは下記のファイルがないです。　上記の二つの設定によって生成します。
  $ cat /etc/crictl.yaml
  runtime-endpoint: "unix:///run/containerd/containerd.sock"
  image-endpoint: "unix:///run/containerd/containerd.sock"
  timeout: 0
  debug: false
  pull-image-on-create: false
  disable-pull-on-run: false
  
  ```

  

```bash







sudo crictl info




```



# 3. PV PVCのインストール

nfsサーバーをストレージ前提でPVを作成します。

## 3.1 nfsサーバーの構築

MasterNodeにインストール、各ワークノートにもインストール必要です。

```bash
# 
sudo dnf install -y rpcbind nfs-utils
# フォルダ作成
sudo mkdir -p -m 777 /tmp/k8svp

# mountするディレクトリ /k8s-vp はWin10のホストにMount済み
#sudo echo "/k8s-vp *(rw,async,no_root_squash)" >> /etc/exports
sudo echo "/tmp/k8svp *(rw,async,no_root_squash)" >> /etc/exports
sudo exportfs -r

# 起動
sudo systemctl start nfs-server
sudo systemctl enable nfs-server
sudo systemctl start rpcbind
sudo systemctl enable rpcbind

# firewalldの設定　起動しない場合設定不要
sudo firewall-cmd --permanent --zone=public --add-service=nfs
sudo firewall-cmd --reload
```

## 3.2 PV PVCの作成

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
    name: kafka-broker-logs-001
    labels:
      type: local-hdd
spec:
    capacity:
        storage: 1Gi
    storageClassName: kafka-broker-logs-001
    volumeMode: Filesystem
    accessModes:
        - ReadWriteMany
    persistentVolumeReclaimPolicy: Retain
    mountOptions:
      - hard
    nfs:
      path: /tmp/k8svp/kafka-broker-logs-1
      server: 192.168.56.100

---

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: kafka-broker-logs-001
spec:
  selector: 
    matchLabels:
      type: local-hdd
  storageClassName: kafka-broker-logs-001
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 100Mi

```



# 4 Helm

```sh
$ ./get_helm.sh
Downloading https://get.helm.sh/helm-v3.10.2-linux-amd64.tar.gz
Verifying checksum... Done.
Preparing to install helm into /usr/local/bin
helm installed into /usr/local/bin/helm
```

