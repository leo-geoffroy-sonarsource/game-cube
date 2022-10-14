FROM nginx:alpine
COPY frontend/game-qube/build /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./supervisord.conf /etc/supervisord.conf

RUN apk update && apk add --no-cache supervisor
RUN apk add gcompat
RUN mkdir -p /opt/game-qube

WORKDIR /opt/game-qube

COPY backend/game-qube/build/game-qube-1.0.0-SNAPSHOT-runner .
CMD /usr/bin/supervisord -c /etc/supervisord.conf
