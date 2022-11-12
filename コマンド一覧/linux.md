# コンテイナ技術関連

unshare

```bash
$ unshare -h

Usage:
 unshare [options] <program> [<argument>...]

Run a program with some namespaces unshared from the parent.

オプション:
 -m, --mount               unshare mounts namespace
 -u, --uts                 unshare UTS namespace (hostname etc)
 -i, --ipc                 unshare System V IPC namespace
 -n, --net                 unshare network namespace
 -p, --pid                 unshare pid namespace
 -U, --user                unshare user namespace
 -f, --fork                fork before launching <program>
     --mount-proc[=<dir>]  mount proc filesystem first (implies --mount)
 -r, --map-root-user       map current user to root (implies --user)
     --propagation <slave|shared|private|unchanged>
                           modify mount propagation in mount namespace
 -s, --setgroups allow|deny  control the setgroups syscall in user namespaces

 -h, --help     display this help and exit
 -V, --version  output version information and exit
    
### sample 
$ sudo unshare --pid --mount-proc --fork /bin/bash
```





cgroup 





# ネットワーク関連











# 文字操作関連









# ファイル操作関連