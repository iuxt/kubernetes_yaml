FROM ubuntu:latest
ADD build.sh /
RUN /build.sh

ENV REDIS_ADDR=127.0.0.1:6379
ENV REDIS_PASSWORD=111111

CMD /redis_exporter -redis.addr ${REDIS_ADDR} -redis.password ${REDIS_PASSWORD}