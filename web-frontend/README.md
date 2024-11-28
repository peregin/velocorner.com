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
# run from local connecting to remote backend (setup first)
# /etc/hosts 127.0.0.1 local.velocorner.com
# brew install mkcert
# mkcert -install
# mkdir ./.certs && cd ./.certs
# mkcert "*.velocorner.com"
caddy run --config Caddyfile
# then with or without reverse proxy
npm run proxy
# or connecting to localhost:9001
npm run local
```

### 'Live' environment
```shell
caddy file-server --root build --listen :3000
-- or
npx serve -s build
```

### Update dependencies
```shell script
# check audit report
npm audit
# check dependencies
npm outdated
npm update --save
```

## Dockerize
```shell script
# build and run local docker environment
docker build -t peregin/web-front:latest .
docker run --rm -e API_HOST=http://velocorner.com -p 3000:3000 peregin/web-front:latest
```