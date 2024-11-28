FROM node:18-alpine as builder

# create a work directory and copy over our dependency manifest files.
RUN mkdir /app
WORKDIR /app
COPY /src /app/src
COPY /public /app/public
COPY ["*.json", "*.js", "./"]

# production mode
RUN npm install --omit=dev
# dev mode
#--RUN npm install

# build the application
RUN npm run build

# RUN THE APP
FROM caddy:2.6.4-alpine

# expose PORT 3000 on our virtual machine so we can run our server
EXPOSE 3000

#ENV NODE_ENV production
#CMD ["npx", "serve", "-s", "build"]

COPY --from=builder /app/build /usr/share/caddy/html
COPY Caddyfile.live /etc/Caddyfile
CMD ["caddy", "run", "--config", "/etc/Caddyfile"]