# Frontend for velocorner.com

## Available Scripts
In the project directory, you can run:

### `API_HOST=http://velocorner.com npm run start`
Runs the app in the development mode.<br />
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.<br />
You will also see any lint errors in the console.

### `npm run build`
Builds the app for production to the `build` folder.<br />
It correctly bundles React in production mode and optimizes the build for the best performance.
The build is minified and the filenames include the hashes.<br />
Your app is ready to be deployed!

### `npm run version`
Retrieves the version of the package.

## Dockerize

```shell script
# build and run local docker environment
docker build -t peregin/web-front:latest .
docker run --rm -e API_HOST=http://velocorner.com -p 3000:3000 peregin/web-front:latest

# run from local dev environment
API_HOST=http://velocorner.com npm run start
```
