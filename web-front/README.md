# Frontend for velocorner.com

## Available Scripts
In the project directory, you can run:

### `npm run build`
Builds the app for production to the `build` folder.<br />
It correctly bundles React in production mode and optimizes the build for the best performance.
The build is minified and the filenames include the hashes.<br />
Your app is ready to be deployed!

### `npm run version`
Retrieves the version of the package.

### `Local` environment
```shell script
# run from local connecting to remote backend
npm run remote
# or connecting to localhost:9001
npm run local
```

### Update dependencies
```shell script
npm outdated
npm update --save
```

## Dockerize
```shell script
# build and run local docker environment
docker build -t peregin/web-front:latest .
docker run --rm -e API_HOST=http://velocorner.com -p 3000:3000 peregin/web-front:latest
```