# ShiroEXP

Shiro漏洞利用工具

## TODO

- 爆破key及加密方式(已完成)
- 漏洞探测
- 探测回显链
- 漏洞利用
- 内存马

## Help
```zsh
C:\Tools\Red_Tools\ShiroEXP>java -jar ShiroEXP.jar -h

   _____    __      _                    ______   _  __    ____
  / ___/   / /_    (_)   _____  ____    / ____/  | |/ /   / __ \
  \__ \   / __ \  / /   / ___/ / __ \  / __/     |   /   / /_/ /
 ___/ /  / / / / / /   / /    / /_/ / / /___    /   |   / ____/
/____/  /_/ /_/ /_/   /_/     \____/ /_____/   /_/|_|  /_/
                                                       v1.0 by Y5neKO :)
                                                       GitHub: https://github.com/Y5neKO

usage: java ShiroEXP.jar [-e <arg>] [-h] [-k <arg>] [-rf <arg>] [-s] [-sk]
       [-u <arg>]
 -e,--exp <arg>                漏洞扫描模块 | 指定exp
 -h,--help                     打印帮助
 -k,--key <arg>                漏洞扫描模块 | 指定key
 -rf,--rememberme-flag <arg>   key爆破模块 | 自定义rememberMe字段名
 -s,--scan                     漏洞扫描模块 | 扫描漏洞
 -sk,--scan-key                key爆破模块 | 爆破key
 -u,--url <arg>                目标地址
```

## Demonstrate

**爆破key及加密方式**

![brutekey.png](./img/brutekey.png)