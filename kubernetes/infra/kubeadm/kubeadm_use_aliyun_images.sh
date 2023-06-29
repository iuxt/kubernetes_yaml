#!/bin/bash

ALIYUN_MIRROR=registry.cn-hangzhou.aliyuncs.com/iuxt

images=(
kube-apiserver:v1.18.20
kube-controller-manager:v1.18.20
kube-scheduler:v1.18.20
kube-proxy:v1.18.20
pause:3.2
etcd:3.4.3-0
coredns:1.6.7
)

for i in "${images[@]}"
do
  docker pull ${ALIYUN_MIRROR}/"$i"
  docker tag ${ALIYUN_MIRROR}/"$i" k8s.gcr.io/"$i"
done