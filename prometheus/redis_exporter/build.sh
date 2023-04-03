apt update && apt install curl -y && apt clean all
cd /tmp/ 

case $(uname -i) in
x86_64)
    filename="redis_exporter-v1.37.0.linux-amd64.tar.gz"
    bin="redis_exporter-v1.37.0.linux-amd64/redis_exporter"
    ;;
aarch64)
    filename="redis_exporter-v1.37.0.linux-arm64.tar.gz"
    bin="redis_exporter-v1.37.0.linux-arm64/redis_exporter"
    ;;
*)
    echo "don't support $(uname -i)"
    ;;
esac

curl -OL https://github.com/oliver006/redis_exporter/releases/download/v1.37.0/$filename
tar xf $filename && mv $bin /
rm -rf /tmp/*
