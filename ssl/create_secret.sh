#!/bin/bash
set -euo pipefail
# 用作给所有命名空间创建证书
for ns in `kubectl get ns|grep -v NAME|awk '{print $1}'`;do
    echo -e "\033[32m--------------------- $ns -----------------------\033[0m"
    # kubectl create secret tls ingeek-com --cert=ingeek.com.crt --key=ingeek.com.key -n $ns
    kubectl create secret tls i-com --cert=demo.crt --key=demo.key -n $ns --dry-run -o yaml |kubectl apply -f -
done
