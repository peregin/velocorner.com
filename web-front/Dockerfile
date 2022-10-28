FROM node:16-alpine

# create a work directory and copy over our dependency manifest files.
RUN mkdir /app
WORKDIR /app
COPY /src /app/src
COPY /public /app/public
COPY ["package.json", "package-lock.json*", "./"]

# production mode
RUN npm install --omit=dev
# dev mode
#--RUN npm install

# build the application
RUN npm run build

# Set the env to "production"
ENV NODE_ENV production

# expose PORT 3000 on our virtual machine so we can run our server
EXPOSE 3000

# start app
CMD ["npx", "serve", "build"]
