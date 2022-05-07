# MB2 Anti-VPN
MB2 Anti-VPN is a plugin made for the [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader). The goal of this plugin is to deny VPN users access to the servers and prevent abuse.

# Prerequisites
1. [JRE](https://java.com/en/download/manual.jsp) that can run Java 11 or higher
2. [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader)
3. [MariaDB](https://mariadb.org/) database
4. [IPHub](https://iphub.info/apiKey/newFree) api key

# Running
```
java -jar mb2-plugin-antivpn-VERSION.jar
```
After your first run a settings file will be generated next to the jar. Fill your credentials there and run again.

## License
MB2 Anti-VPN is licensed under GPLv2 as free software. You are free to use, modify and redistribute MB2 Anti-VPN following the terms in LICENSE.txt