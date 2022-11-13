

# 1. コマンド一覧

https://kubernetes.io/ja/docs/reference/kubectl/cheatsheet/



## Kubectlコンテキストの設定

```bash
$ kubectl config view # マージされたkubeconfigの設定を表示します。

# 複数のkubeconfigファイルを同時に読み込む場合はこのように記述します。
KUBECONFIG=~/.kube/config:~/.kube/kubconfig2 

$ kubectl config view

# e2eユーザのパスワードを取得します。
$ kubectl config view -o jsonpath='{.users[?(@.name == "e2e")].user.password}'

$ kubectl config view -o jsonpath='{.users[].name}'    # 最初のユーザー名を表示します
$ kubectl config view -o jsonpath='{.users[*].name}'   # ユーザー名のリストを表示します
$ kubectl config get-contexts                          # コンテキストのリストを表示します
$ kubectl config current-context                       # 現在のコンテキストを表示します
$ kubectl config use-context my-cluster-name           # デフォルトのコンテキストをmy-cluster-nameに設定します

# basic認証をサポートする新たなユーザーをkubeconfigに追加します
$ kubectl config set-credentials kubeuser/foo.kubernetes.com --username=kubeuser --password=kubepassword

# 現在のコンテキストでkubectlのサブコマンドの名前空間を永続的に変更します
$ kubectl config set-context --current --namespace=ggckad-s2

# 特定のユーザー名と名前空間を使用してコンテキストを設定します
$ kubectl config set-context gce --user=cluster-admin --namespace=foo \
  && kubectl config use-context gce
 
$ kubectl config unset users.foo    # ユーザーfooを削除します
```



## Kubectl Apply

```bash
$ kubectl apply -f ./my-manifest.yaml            # リソースを作成します
$ kubectl apply -f ./my1.yaml -f ./my2.yaml      # 複数のファイルからリソースを作成します
$ kubectl apply -f ./dir                         # dirディレクトリ内のすべてのマニフェストファイルからリソースを作成します
$ kubectl apply -f https://git.io/vPieo          # urlで公開されているファイルからリソースを作成します
$ kubectl create deployment nginx --image=nginx  # 単一のnginx Deploymentを作成します
$ kubectl explain pods                           # Podマニフェストのドキュメントを取得します

```



## リソースの検索と閲覧

```bash
# Getコマンドで基本的な情報を確認します
$ kubectl get services                          # 現在の名前空間上にあるすべてのサービスのリストを表示します
$ kubectl get pods --all-namespaces             # すべての名前空間上にあるすべてのPodのリストを表示します
$ kubectl get pods -o wide                      # 現在の名前空間上にあるすべてのPodについてより詳細なリストを表示します
$ kubectl get deployment my-dep                 # 特定のDeploymentを表示します
$ kubectl get pods                              # 現在の名前空間上にあるすべてのPodのリストを表示します
$ kubectl get pod my-pod -o yaml                # PodのYAMLを表示します

# pod一覧を取得
$ kubectl get pod -A -o wide

# Describeコマンドで詳細な情報を確認します
$ kubectl describe nodes my-node
$ kubectl describe pods my-pod

# 名前順にソートしたServiceのリストを表示します
$ kubectl get services --sort-by=.metadata.name

# Restartカウント順にPodのリストを表示します
$ kubectl get pods --sort-by='.status.containerStatuses[0].restartCount'

# capacity順にソートしたPersistentVolumeのリストを表示します
$ kubectl get pv --sort-by=.spec.capacity.storage

# app=cassandraラベルのついたすべてのPodのversionラベルを表示します
$ kubectl get pods --selector=app=cassandra -o \
  jsonpath='{.items[*].metadata.labels.version}'

# 'ca.crt'のようなピリオドが含まれるキーの値を取得します
$ kubectl get configmap myconfig \
  -o jsonpath='{.data.ca\.crt}'

# fluent.confの内容をconfigmapとして作成します。
$ kubectl create configmap kafka.fluent.conf --from-file=fluent.conf
$ kubectl get configmap kafka.fluent.conf -o json/yaml

# すべてのワーカーノードを取得します（セレクターを使用して、
# 「node-role.kubernetes.io/master」という名前のラベルを持つ結果を除外します）
$ kubectl get node --selector='!node-role.kubernetes.io/master'

# 現在の名前空間でrunning状態のPodのリストを表示します
$ kubectl get pods --field-selector=status.phase=Running

# すべてのノードのExternal IPのリストを表示します
$ kubectl get nodes -o jsonpath='{.items[*].status.addresses[?(@.type=="ExternalIP")].address}'

# 特定のRCに属するPodの名前のリストを表示します
# `jq`コマンドは複雑なjsonpathを変換する場合に便利であり、https://stedolan.github.io/jq/で見つけることが可能です
sel=${$(kubectl get rc my-rc --output=json | jq -j '.spec.selector | to_entries | .[] | "\(.key)=\(.value),"')%?}
echo $(kubectl get pods --selector=$sel --output=jsonpath={.items..metadata.name})

# すべてのPod(またはラベル付けをサポートする他のKubernetesオブジェクト)のラベルのリストを表示します
$ kubectl get pods --show-labels

# どのノードがready状態か確認します
JSONPATH='{range .items[*]}{@.metadata.name}:{range @.status.conditions[*]}{@.type}={@.status};{end}{end}' \
 && kubectl get nodes -o jsonpath="$JSONPATH" | grep "Ready=True"

# Podで現在使用中のSecretをすべて表示します
$ kubectl get pods -o json | jq '.items[].spec.containers[].env[]?.valueFrom.secretKeyRef.name' | grep -v null | sort | uniq

# すべてのPodのInitContainerのコンテナIDのリストを表示します
# initContainerの削除を回避しながら、停止したコンテナを削除するときに役立つでしょう
$ kubectl get pods --all-namespaces -o jsonpath='{range .items[*].status.initContainerStatuses[*]}{.containerID}{"\n"}{end}' | cut -d/ -f3

# タイムスタンプでソートされたEventのリストを表示します
$ kubectl get events --sort-by=.metadata.creationTimestamp

# クラスターの現在の状態を、マニフェストが適用された場合のクラスターの状態と比較します。
$ kubectl diff -f ./my-manifest.yaml

# Nodeから返されるすべてのキーをピリオド区切りの階層表記で生成します。
# 複雑にネストされたJSON構造をもつキーを指定したい時に便利です
$ kubectl get nodes -o json | jq -c 'paths|join(".")'

# Pod等から返されるすべてのキーをピリオド区切り階層表記で生成します。
$ kubectl get pods -o json | jq -c 'paths|join(".")'
```



## リソースのアップデート

```bash
$ kubectl set image deployment/frontend www=image:v2               # frontend Deploymentのwwwコンテナイメージをv2にローリングアップデートします
$ kubectl rollout history deployment/frontend                      # frontend Deploymentの改訂履歴を確認します
$ kubectl rollout undo deployment/frontend                         # 1つ前のDeploymentにロールバックします
$ kubectl rollout undo deployment/frontend --to-revision=2         # 特定のバージョンにロールバックします
$ kubectl rollout status -w deployment/frontend                    # frontend Deploymentのローリングアップデートを状態をwatchします
$ kubectl rollout restart deployment/frontend                      # frontend Deployment を再起動します

cat pod.json | kubectl replace -f -                              # 標準入力から渡されたJSONに基づいてPodを置き換えます

# リソースを強制的に削除してから再生成し、置き換えます。サービスの停止が発生します
$ kubectl replace --force -f ./pod.json

# ReplicaSetリソースで作られたnginxについてServiceを作成します。これは、ポート80で提供され、コンテナへはポート8000で接続します
$ kubectl expose rc nginx --port=80 --target-port=8000

# 単一コンテナのPodイメージのバージョン(タグ)をv4に更新します
$ kubectl get pod mypod -o yaml | sed 's/\(image: myimage\):.*$/\1:v4/' | kubectl replace -f -

$ kubectl label pods my-pod new-label=awesome                      # ラベルを追加します
$ kubectl annotate pods my-pod icon-url=http://goo.gl/XXBTWq       # アノテーションを追加します
$ kubectl autoscale deployment foo --min=2 --max=10                # "foo" Deploymentのオートスケーリングを行います
```

## リソースへのパッチ適用

```bash
# ノードを部分的に更新します
$ kubectl patch node k8s-node-1 -p '{"spec":{"unschedulable":true}}'

# コンテナのイメージを更新します。spec.containers[*].nameはマージキーであるため必須です
$ kubectl patch pod valid-pod -p '{"spec":{"containers":[{"name":"kubernetes-serve-hostname","image":"new image"}]}}'

# ポテンシャル配列を含むJSONパッチを使用して、コンテナのイメージを更新します
$ kubectl patch pod valid-pod --type='json' -p='[{"op": "replace", "path": "/spec/containers/0/image", "value":"new image"}]'

# ポテンシャル配列のJSONパッチを使用してDeploymentのlivenessProbeを無効にします
$ kubectl patch deployment valid-deployment  --type json   -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'

# ポテンシャル配列に新たな要素を追加します
$ kubectl patch sa default --type='json' -p='[{"op": "add", "path": "/secrets/1", "value": {"name": "whatever" } }]'
```

## リソースの編集

任意のエディターでAPIリソースを編集します。

```bash
$ kubectl edit svc/docker-registry                      # docker-registryという名前のサービスを編集します
KUBE_EDITOR="nano" kubectl edit svc/docker-registry   # エディターを指定します
```

## リソースのスケーリング

```bash
$ kubectl scale --replicas=3 rs/foo                                 # 「foo」という名前のレプリカセットを3にスケーリングします
$ kubectl scale --replicas=3 -f foo.yaml                            # 「foo.yaml」で指定されたリソースを3にスケーリングします
$ kubectl scale --current-replicas=2 --replicas=3 deployment/mysql  # mysqlと名付けられたdeploymentの現在のサイズが2であれば、mysqlを3にスケーリングします
$ kubectl scale --replicas=5 rc/foo rc/bar rc/baz                   # 複数のReplication controllerをスケーリングします



$ kubectl scale --replicas=0 deployment/XXXXX

```

## リソースの削除

```bash
$ kubectl delete -f ./pod.json                                              # pod.jsonで指定されたタイプと名前を使用してPodを削除します
$ kubectl delete pod,service baz foo                                        # 「baz」と「foo」の名前を持つPodとServiceを削除します
$ kubectl delete pods,services -l name=myLabel                              # name=myLabelラベルを持つのPodとServiceを削除します
$ kubectl -n my-ns delete pod,svc --all                                     # 名前空間my-ns内のすべてのPodとServiceを削除します
# awkコマンドのpattern1またはpattern2に一致するすべてのPodを削除します。
$ kubectl get pods  -n mynamespace --no-headers=true | awk '/pattern1|pattern2/{print $1}' | xargs  kubectl delete -n mynamespace pod

### 強制Pod削除
$ kubectl delete pod kafka-zookeeper-6559d457d7-c4sgs --grace-period=0 --force
```

## Podとの対話処理

```bash
$ kubectl logs my-pod                                 # Podのログをダンプします(標準出力)
$ kubectl logs -l name=myLabel                        # name=myLabelラベルの持つPodのログをダンプします(標準出力)
$ kubectl logs my-pod --previous                      # 以前に存在したコンテナのPodログをダンプします(標準出力)
$ kubectl logs my-pod -c my-container                 # 複数コンテナがあるPodで、特定のコンテナのログをダンプします(標準出力)
$ kubectl logs -l name=myLabel -c my-container        # name=mylabelラベルを持つPodのログをダンプします(標準出力) 
$ kubectl logs my-pod -c my-container --previous      # 複数コンテナがあるPodで、以前に作成した特定のコンテナのログをダンプします(標準出力)
$ kubectl logs -f my-pod                              # Podのログをストリームで確認します(標準出力)
$ kubectl logs -f my-pod -c my-container              # 複数のコンテナがあるPodで、特定のコンテナのログをストリームで確認します(標準出力)
$ kubectl logs -f -l name=myLabel --all-containers    # name-myLabelラベルを持つすべてのコンテナのログをストリームで確認します(標準出力)

$ kubectl run -i --tty busybox --image=busybox -- sh  # Podをインタラクティブシェルとして実行します
$ kubectl run nginx --image=nginx -n mynamespace      # 特定の名前空間でnginx Podを実行します
$ kubectl run nginx --image=nginx                     # nginx Podを実行し、マニフェストファイルをpod.yamlという名前で書き込みます
--dry-run=client -o yaml > pod.yaml
$ kubectl attach my-pod -i                            # 実行中のコンテナに接続します
$ kubectl port-forward my-pod 5000:6000               # ローカルマシンのポート5000を、my-podのポート6000に転送します
$ kubectl exec my-pod -- ls /                         # 既存のPodでコマンドを実行(単一コンテナの場合)
$ kubectl exec my-pod -c my-container -- ls /         # 既存のPodでコマンドを実行(複数コンテナがある場合)
$ kubectl top pod POD_NAME --containers               # 特定のPodとそのコンテナのメトリクスを表示します

### Pod起動　　※--dry-run：本当は実施しない　 -o yaml：定義ファイル生成
$ kubectl run my-db --restart=Never --image=postgres:12-alpine --port=5432 --env=POSTGRES_PASSWORD=example --namespace=my-ns --requests=`cpu=0.1,memory=50Mi` --limits=`cpu=0.2,memory=100Mi` --dry-run -o yaml
$ kubectl expose deploy/my-nginx --type=ClusterIP --port=8080 --target-port=80 --dry-run -o yaml



### ファイルコピー
$ kubectl cp [Hostパス] [Pod名]:[コンテナ内パス] -c[コンテナー名]

### コンテナ内コマンド実施
$ kubectl exec -it [Pod名] -c[コンテナー名] -- [コマンド]

```

## ノード&クラスターの操作

```bash
$ kubectl cordon my-node                                                # my-nodeをスケジューリング不能に設定します
$ kubectl drain my-node                                                 # メンテナンスの準備としてmy-nodeで動作中のPodを空にします
$ kubectl uncordon my-node                                              # my-nodeをスケジューリング可能に設定します
$ kubectl top node my-node                                              # 特定のノードのメトリクスを表示します
$ kubectl cluster-info                                                  # Kubernetesクラスターのマスターとサービスのアドレスを表示します
$ kubectl cluster-info dump                                             # 現在のクラスター状態を標準出力にダンプします
$ kubectl cluster-info dump --output-directory=/path/to/cluster-state   # 現在のクラスター状態を/path/to/cluster-stateにダンプします

# special-userキーとNoScheduleエフェクトを持つTaintがすでに存在する場合、その値は指定されたとおりに置き換えられます
$ kubectl taint nodes foo dedicated=special-user:NoSchedule


$ kubectl label nodes [node名前] runType=Server


```

## リソースタイプ確認

```bash
$ kubectl api-resources
$ kubectl api-resources --namespaced=true      # 名前空間付きのすべてのリソースを表示します
$ kubectl api-resources --namespaced=false     # 名前空間のないすべてのリソースを表示します
$ kubectl api-resources -o name                # すべてのリソースを単純な出力(リソース名のみ)で表示します
$ kubectl api-resources -o wide                # すべてのリソースを拡張された形(別名 "wide")で表示します
$ kubectl api-resources --verbs=list,get       # "list"および"get"操作をサポートするすべてのリソースを表示します
$ kubectl api-resources --api-group=extensions # "extensions" APIグループのすべてのリソースを表示します
```



## シークレット操作

```bash
#　secret作成
$ kubectl create secret generic db-user-pass --from-file=username=./username.txt --from-file=password=./password.txt
$ kubectl create secret docker-registry [secret名前] --docker-server=https://index.docker.io/v1/  \
  --docker-username=tunaclouser --docker-password=<token> --docker-email=fj-tunaclo-support@dl.jp.fujitsu.com

```

- 作成したシークレットを利用する方法

  - 方法①．PodのYamlで指定

    - ```Yaml
      apiVersion: v1
      kind: Pod
      metadata:
        name: private-reg
      spec:
        containers:
        - name: private-reg-container
          image: <your-private-image>
        imagePullSecrets:
        - name: local-docker-nexus
      ```

  - 方法②．imagePullSecretsをserviceaccountに追加

    - ```bash
      $ kubectl patch serviceaccount default -p '{"imagePullSecrets": [{"name": "myregistrykey"}]}'
      ```

      

## ConfigMapの操作

環境変数としての利用　　　　　実験が必要！！！！！！！！

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: game-demo
data:
  # プロパティーに似たキー。各キーは単純な値にマッピングされている
  player_initial_lives: "3"
  ui_properties_file_name: "user-interface.properties"

  # ファイルに似たキー
  game.properties: |
    enemy.types=aliens,monsters
    player.maximum-lives=5    
  user-interface.properties: |
    color.good=purple
    color.bad=yellow
    allow.textmode=true    
```



```yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-demo-pod
spec:
  containers:
    - name: demo
      image: alpine
      command: ["sleep", "3600"]
      env:
        # 環境変数を定義します。
        - name: PLAYER_INITIAL_LIVES # ここではConfigMap内のキーの名前とは違い
                                     # 大文字が使われていることに着目してください。
          valueFrom:
            configMapKeyRef:
              name: game-demo           # この値を取得するConfigMap。
              key: player_initial_lives # 取得するキー。
        - name: UI_PROPERTIES_FILE_NAME
          valueFrom:
            configMapKeyRef:
              name: game-demo
              key: ui_properties_file_name
      volumeMounts:
      - name: config
        mountPath: "/config"
        readOnly: true
  volumes:
    # Podレベルでボリュームを設定し、Pod内のコンテナにマウントします。
    - name: config
      configMap:
        # マウントしたいConfigMapの名前を指定します。
        name: game-demo
        # ファイルとして作成するConfigMapのキーの配列
        items:
        - key: "game.properties"
          path: "game.properties"
        - key: "user-interface.properties"
          path: "user-interface.properties"
```



```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: index-html
data:
  index.html: |-
    <!DOCTYPE html>
    <html lang="ja">
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="background-color: green">
      <h2>
        Hello World !
      </h2>
    </body>
    </html>
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: web
        image: nginx
        ports:
          - containerPort: 80
        volumeMounts:
        - name: html-volume # ConfigMapから作成したボリューム
          mountPath: /usr/share/nginx/html # ConfigMapのファイルをマウントするパス
      volumes:
        - name: html-volume # ConfigMapからボリュームを作成
          configMap: # 作成したConfigMapを指定
            name: index-html # ConfigMap名
```



```bash







####################################################
$ cat /etc/kubernetes/kubelet/kubelet-config.json

```














# 2. k8s環境通過DNS名稱訪問POD

https://www.gushiciku.cn/pl/gzEA/zh-tw









$ kubectl delete -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

$ kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml





$ kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml





$ kubectl get events







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


$ kubectl patch node <NODE_NAME> -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'

$ kubectl patch node k8s-master.vbox.local -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'

The node was missing a spec for podCIDR, so I ran "kubectl patch node <NODE_NAME> -p '{"spec":{"podCIDR":"10.244.0.0/16"}}'" for each node and the issue went away.

```

