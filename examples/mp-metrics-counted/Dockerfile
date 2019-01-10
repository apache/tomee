FROM tomee:8-jre-8.0.0-M1-microprofile

RUN rm -Rf /usr/local/tomee/webapps/ROOT/
COPY target/mp-metrics-counted-8.0.0-SNAPSHOT.war /usr/local/tomee/webapps/ROOT.war

RUN groupadd --gid 1001 tomee
RUN useradd --uid 1001 -g tomee tomee

RUN chmod g=u /etc/passwd
RUN mkdir -p /home/tomee
RUN chown -R 1001:0 /home/tomee
RUN chown -R 1001:0 /usr/local/tomee

USER 1001
