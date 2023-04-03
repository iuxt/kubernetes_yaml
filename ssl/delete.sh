#!/bin/bash
# 用作给所有命名空间创建证书
for ns in `kubectl get ns|grep -v NAME|awk '{print $1}'`;do
    echo -e "\033[32m--------------------- $ns -----------------------\033[0m"
    # kubectl create secret tls ingeek-com --cert=ingeek.com.crt --key=ingeek.com.key -n $ns
    kubectl delete secret i-com -n $ns
done
