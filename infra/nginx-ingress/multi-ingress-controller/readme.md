## 创建两个ingressclass

```bash
kubectl apply -f ./clusterrole.yaml
kubectl apply -f ./intranet.yaml
kubectl apply -f ./internet.yaml
```

## 查看安装的ingressclass

```bash
kubectl get ingressclass
```

## 指定 ingress class

```yml
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: prometheus
  namespace: monitor
  annotations:
    kubernetes.io/ingress.class: "intranet"       # 这里指定ingress class
    nginx.ingress.kubernetes.io/ssl-redirect: "true"

spec:
  tls:
  - hosts:
    - prometheus.i.com
    secretName: i-com
  rules:
  - host: prometheus.i.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: prometheus
            port:
              number: 9090
```

## 使用模板创建多个ingress controller

```bash
sed 's#{namespace}#dev#g'  template.yaml |kubectl apply -f -
sed 's#{namespace}#test#g'  template.yaml |kubectl apply -f -
sed 's#{namespace}#prod#g'  template.yaml |kubectl apply -f -
```
