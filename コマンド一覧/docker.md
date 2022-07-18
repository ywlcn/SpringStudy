## docker pull

```bash
$ docker pull nodered/node-red-docker:last
# 参数解析# [nodered/node-red-docker:last] 镜像名称，格式REPOSITORY:TAG（仓库：标签），如果指定标签自动默认为:last
```

## docker cp

```bash
$ docker cp [host パス] [コンテナーId]:[コンテナー内パス]
# 参数解析# [nodered/node-red-docker:last] 镜像名称，格式REPOSITORY:TAG（仓库：标签），如果指定标签自动默认为:last
```

## docker run

启动命令如果设置其它命令，会覆盖镜像中预设的命令，比如echo HelloWorld，那创建的容器就作用也就是echo HelloWorld，不会运行nodered服务了。

```bash
$ docker run -d -p 1888:1880 -v /local/workdir:/contianer/workdir --name docker-nodered nodered/node-red-docker npm start -- --userDir /data
# 参数解析
# [-d] 后台运行，如果不加-d，容器的输出会直接显示
# [-p 主机端口:容器端口] 映射端口，容器端口和容器服务相关，这里镜像的nodered服务用了1880端口，映射主机的1888，这样访问主机IP:1888就可以访问容器内的nodered服务了
# [-v /local/workdir:/contianer/workdir] 映射文件夹，将主机的/local/workdir目录映射到容器内的/contianer/workdir目录，容器内访问/contianer/workdir就可以访问主机/local/workdir的资源了
# [--name docker-nodered] 设置容器名称为docker-nodered，不指定会随机生成
# [nodered/node-red-docker] 镜像名称
# [npm start -- --userDir /data] 启动命令，位于镜像名称后面，是设置容器启动后执行的命令。这里的例子实际上是镜像中已经预设好的命令，目的是运行nodered。
# [--cap-add=NET_ADMIN] Iptables利用できるようにする
$ docker run -it --name my_centos docker.io/centos /bin/bash
-it表示启动终端交互界面
--name是自定义的容易名字   /bin/bash表示容器启动时候执行的命令

```

## docker ps

执行后可以看到CONTAINER ID（容器ID）,IMAGE（镜像名称）,COMMAND（启动命令）,CREATED（创建时间）,STATUS（运行状态）,PORTS（映射端口）,NAMES（容器名称）等信息，其中容器ID和容器名称比较有用。

```bash
$ docker ps -a
# 参数解析
# [-a] 查看所有容器，不带则只显示运行中的容器
```


## docker start/stop/restart/rm

```bash
$ docker start docker-nodered
# 参数解析
# [docker-nodered] 操作的容器名称/容器ID
```

## docker logs

```bash
$ docker logs -f docker-nodered
# 参数解析
# [-f] 跟随日志输出不断显示
# [docker-nodered] 操作的容器名称/容器ID
```


## docker exec

```bash
$ docker exec -it docker-nodered bash
# 参数解析
# [-it] 分配一个伪终端，并接管其stdin/stdout支持交互操作，这时候bash命令不会自动退出
# [docker-nodered] 操作的容器名称/容器ID
# [bash] shell程序，对于alpine内核的镜像则用sh
INFO：执行bash/sh，相当于运行了一个命令行终端（类似ssh），运行效果就是登陆进入容器的内部系统了。
INFO：当然你也可以执行echo HelloWorld、ps aux之类的命令。
```

## docker commit 

```bash
$ docker commit docker-nodered my-nodered:custom
# 参数解析
# [docker-nodered] 操作的容器名称/容器ID
# [my-nodered:custom] 镜像名称，格式REPOSITORY:TAG（仓库：标签）
结合docker exec命令使用，进入容器后修改文件、安装程序等操作后，保存成新新镜像；后面用新镜像创建的容器就会有先前操作产生的内容。

$ docker commit -m 'my python 2.7.5' my_centos python2:v0.1
-m:镜像描述
my_centos:上一步生成镜像的名称
python2:生成的镜像保存的仓库名
v0.1:生成镜像的版本号
```

##  docker images関連

```bash
#查看镜像信息
$ docker images
# 重命名镜像
$ docker tag IMAGEID(镜像id) REPOSITORY:TAG（仓库：标签）
# 删除镜像
$ docker rmi IMAGEID(镜像id)



# ログイン
$ docker login -u user -p password nexus.vbox.local:9000
# タグ作成
$ docker tag grpc_server:latest nexus.vbox.local:9001/grpc_server:latest
# Imageをサーバーにプッシュ
$ docker push nexus.vbox.local:9001/grpc_server:latest
# イメージをExport
$ docker save [イメージ名] > XXXX.tar
$ docker save [イメージ名] | gzip > XXXX.tgz
$ docker load < XXXX.tar



```

## docker build

使用指定的Dockerfile创建镜像。Dockerfile是一个文件，类似脚本，build后会生成一个镜像。Dockerfile的执行效果类似于：创建一个容器、进容器安装新软件/服务、然后提交成为新镜像。

```bash
$ docker build -f /path/to/a/Dockerfile -t my-nodered:custom --no-cache .
# 参数解析
# [-f] 指定Dockerfile文件，不指定则默认上下文目录的Dockerfile文件
# [my-nodered:custom] 镜像名称，格式REPOSITORY:TAG（仓库：标签），不使用参数则默认生成None:None名称
# [--no-cache] 设置不使用缓存，不使用该参数则在build过程中会从上次出错的步骤继续执行
# [.] 指定上下文目录，.代表当前目录
在此不展开介绍Dockerfile了，可以从官方仓库看镜像的Dockerfile，重点学习理解其中的RUN、ENTRYPOINT、CMD指令。
```


## docker-compose

批量运行docker-compose.yml中预定义的容器。

```bash
$ docker-compose -f /path/to/a/docker-compose.yml -d up
# 参数解析
# [-f] 指定docker-compose.yml文件，不指定则默认当前目录docker-compose.yml文件
# [-d] 后台运行
# [up] 创建并运行作为服务的容器，如果已经存在服务的容器，且容器创建后服务的配置有变化，就重新创建容器。
docker-compose引入了服务管理模式，docker-compose start/stop是控制某个服务容器启停，docker-compose down是删除所有服务容器。
```

## docker-compose.yml样例

配置文件中的对容器的设置，实际上用docker run命令来设置也能达到相同的效果。

```yml
# docker-compose.yml
version: "3" # 指定语法的版本
services: # 定义服务
    nginx: # 服务的名称
        container_name: web-nginx # 容器的名称，不指定默认"项目名称_服务名称_序号"，其中项目名称默认为docker-compose.yaml所在目录的目录名称
        image: nginx:latest # 镜像名称
        restart: always # 重启方式
        ports: # 端口映射
          - 80:80
        volumes: # 主机与容器目录映射
          - ./webserver:/webserver
          - ./nginx/nginx.conf:/etc/nginx/nginx.conf

```



## 全体制御

```bash
$ docker run命令设置了容器的配置信息，如果需要修改配置信息，一般是删除容器，重新再docker run。但也可以通过修改容器的配置文件来达到修改的目的，以修改端口映射为例。
1. 停止容器
2. 停止docker服务（systemctl stop docker）
3. 修改这个容器的配置文件（hostconfig.json、config.v2.json）
    cd /var/lib/docker/containers/3b6ef264a040* # 这里是CONTAINER ID
    vi hostconfig.json # 在"PortBindings"属性下增加/修改映射记录："PortBindings":{"3306/tcp":[{"HostIp":"","HostPort":"3307"}]}，前一个数字是容器端口, 后一个是宿主机端口。
    vi config.v2.json  # 在"Ports"属性下增加/修改映射记录。
4. 启动止docker服务（systemctl start docker）
5. 启动容器
类似地可以更改目录映射，感觉通过docker run命令或者Dockerfile里命令设定的参数都可以通过修改hostconfig.json、config.v2.json来进行变更。


## 下記のコマンドでDockerの全部情報を出せる
$ docker info
## ネットワーク状況確認
$ docker network ls
$ docker network inspect briage

$ docker container prune
$ docker system prune

#############################
$ vi /etc/docker/daemon.json
{ "insecure-registries":[ "192.168.1.10:5000" ] }
#############################

```



