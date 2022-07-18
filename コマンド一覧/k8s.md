

# 1. コマンド一覧

```bash
### 強制Pod削除
$ kubectl delete pod kafka-zookeeper-6559d457d7-c4sgs --grace-period=0 --force

### ファイルコピー
$ kubectl cp [Hostパス] [Pod名]:[コンテナ内パス] -c[コンテナー名]
### コンテナ内コマンド実施
$ kubectl exec -it [Pod名] -c[コンテナー名] -- [コマンド]
### Pod起動　　※--dry-run：本当は実施しない　 -o yaml：定義ファイル生成
$ kubectl run my-db --restart=Never --image=postgres:12-alpine --port=5432 --env=POSTGRES_PASSWORD=example --namespace=my-ns --requests=`cpu=0.1,memory=50Mi` --limits=`cpu=0.2,memory=100Mi` --dry-run -o yaml
$ kubectl expose deploy/my-nginx --type=ClusterIP --port=8080 --target-port=80 --dry-run -o yaml
# pod一覧を取得
$ kubectl get pod -A -o wide

#　secret作成
$ kubectl create secret generic db-user-pass --from-file=username=./username.txt --from-file=password=./password.txt
$ kubectl create secret docker-registry [secret名前] --docker-server=https://index.docker.io/v1/  \
  --docker-username=tunaclouser --docker-password=<token> --docker-email=fj-tunaclo-support@dl.jp.fujitsu.com
適用方法A　PodのYamlで指定
#Yamlでは 下記の内容が必要です。
# imagePullSecrets:
#  - name: local-docker-nexus
適用方法B　imagePullSecretsをserviceaccountに追加
#　kubectl patch serviceaccount default -p '{"imagePullSecrets": [{"name": "myregistrykey"}]}'


$ kubectl label nodes [node名前] runType=Server

$ kubectl scale --replicas=0 deployment/XXXXX

####################################################
$ cat /etc/kubernetes/kubelet/kubelet-config.json

```














# 2. k8s環境通過DNS名稱訪問POD

https://www.gushiciku.cn/pl/gzEA/zh-tw









kubectl delete -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml





kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml





kubectl get events







```bash


Error registering network: failed to acquire lease: node "k8s-master.vbox.local" pod cidr not assigned



I know this is old but I wanted to comment here as I too had this issue, but in my case it was a symptom to a different issue. In my case, there was no subnet.env file but it was not getting created because my flannel daemonset was failing. The error from the pod (kubectl --namespace=kube-system logs <POD_NAME>) showed "Error registering network: failed to acquire lease: node "<NODE_NAME>" pod cidr not assigned". The node was missing a spec for podCIDR, so I ran "kubectl patch node <NODE_NAME> -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'" for each node and the issue went away.

```







X.1 flannelインストール後、corednsは下記のようにContainerCreatingとなっている

```bash
$ kubectl get pod -A 
NAMESPACE     NAME                                            READY   STATUS              RESTARTS   AGE
kube-system   coredns-74ff55c5b-7h2mg                         0/1     ContainerCreating   0          9s
kube-system   coredns-74ff55c5b-g54rg                         0/1     ContainerCreating   0          9s

$ kubectl describe pod -n kube-system coredns-74ff55c5b-7h2mg
  Warning  FailedCreatePodSandBox  17s   kubelet            Failed to create pod sandbox: rpc error: code = Unknown desc = failed to create pod network sandbox k8s_coredns-74ff55c5b-7h2mg_kube-system_a3dad125-bcda-4eb2-8709-ba80394247f7_0(1eda5091171d555dcf2ef8ea44be638407d2d611b2b8fca15dd6ba4a976fcff0): error adding pod kube-system_coredns-74ff55c5b-7h2mg to CNI network "cbr0": open /run/flannel/subnet.env: no such file or directory
  
  
一応下記の内容を/run/flannel/subnet.envに書き込んで、起動できる。
FLANNEL_NETWORK=10.244.0.0/16
FLANNEL_SUBNET=10.244.0.1/24
FLANNEL_MTU=1450
FLANNEL_IPMASQ=true
  
```







X.2 kube-flannel-ds-XXXXは起動できない

```bash
$ kubectl logs -n kube-system kube-flannel-ds-XXXX
Error registering network: failed to acquire lease: node "k8s-master.vbox.local" pod cidr not assigned


kubectl patch node <NODE_NAME> -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'

kubectl patch node k8s-master.vbox.local -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'

The node was missing a spec for podCIDR, so I ran "kubectl patch node <NODE_NAME> -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'" for each node and the issue went away.

```

