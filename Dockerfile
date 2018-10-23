FROM scireum/sirius-runtime:3

USER root

ADD target/release-dir /home/sirius
RUN mkdir /home/sirius/data

RUN chown sirius:sirius -R /home/sirius
USER sirius

VOLUME /home/sirius/instance.conf
VOLUME /home/sirius/data

EXPOSE 2222
