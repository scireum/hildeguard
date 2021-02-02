FROM hub.scireum.com/scireum/sirius-runtime:25

USER root

RUN apt-get update && \
  apt-get install -y openssh-client

ADD target/release-dir /home/sirius
RUN mkdir /home/sirius/data

RUN chown sirius:sirius -R /home/sirius
USER sirius

VOLUME /home/sirius/instance.conf
VOLUME /home/sirius/data

EXPOSE 2222
