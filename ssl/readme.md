# ingress-nginx

## 创建证书secret

```bash
kubectl create secret tls test.babudiu.com \
    --cert tls/test.babudiu.com_bundle.crt --key tls/test.babudiu.com.key
```
