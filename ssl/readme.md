# ingress-nginx

## 创建证书secret

```bash
kubectl create secret tls test.i.com \
    --cert tls/test.i.com_bundle.crt --key tls/test.i.com.key
```
