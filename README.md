# ShiroEXP

Shiro漏洞利用工具

## Environment

`JDK 8` | `Intellij 2024`

> ⚠️ _使用其他JDK版本可能出现未知的错误_

## TODO

- 爆破key及加密方式(已完成)
- 漏洞探测(已完成Shiro550 URLDNS探测)
- 探测回显链(已完成CB1+TomcatEcho、Spring、AllEcho回显链)
- 漏洞利用(已完成命令执行、Shell模式)
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

usage: java ShiroEXP.jar [-be] [-bk] [-c <arg>] [--cookie <arg>] [-e
       <arg>] [--gadget <arg>] [-h] [-k <arg>] [-rf <arg>] [-s] [--shell]
       [-u <arg>]
 -be,--brute-echo              漏洞扫描模块 | 爆破回显链
 -bk,--brute-key               key爆破模块 | 爆破key
 -c,--cmd <arg>                执行命令
    --cookie <arg>             携带Cookie
 -e,--exp <arg>                指定exp {Shiro550, Shiro721}
    --gadget <arg>             指定利用链
 -h,--help                     打印帮助
 -k,--key <arg>                漏洞扫描模块 | 指定key
 -rf,--rememberme-flag <arg>   自定义rememberMe字段名
 -s,--scan                     漏洞扫描模块 | 扫描漏洞
    --shell                    进入Shell模式
 -u,--url <arg>                目标地址
```

## Demonstrate

**爆破key及加密方式**

![brutekey.png](img/brutekey.png)

**漏洞验证**

![Shiro550scan.png](img/Shiro550scan.png)

**爆破回显链**

![bruteecho.png](img/bruteecho.png)

**命令执行**

![commandexcute.png](img/commandexcute.png)

**Shell模式**

![shellmode.png](img/shellmode.png)

## Thanks

@frohoff   https://github.com/frohoff/ysoserial

@SummerSec  https://github.com/SummerSec/ShiroAttack2