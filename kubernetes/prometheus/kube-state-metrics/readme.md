这个在k8s版本1.21.10上验证过

版本对应关系可以查看官方GitHub
官方地址： <https://github.com/kubernetes/kube-state-metrics/tree/main/examples/standard>


service.yml 里面需要增加几条注解才能被Prometheus自动发现

```yml
annotations:
    prometheus.io/path: /metrics
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
```