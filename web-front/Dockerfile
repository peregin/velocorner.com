FROM node:14.16.1-alpine

# create a work directory and copy over our dependency manifest files.
RUN mkdir /app
WORKDIR /app
COPY /src /app/src
COPY /public /app/public
COPY ["package.json", "package-lock.json*", "./"]

# build
# RUN npm install --production
RUN npm install

# expose PORT 3000 on our virtual machine so we can run our server
EXPOSE 3000

# start app
CMD ["npm", "start"]