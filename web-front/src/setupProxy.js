const proxy = require('http-proxy-middleware');
const pkg = require('../package.json')
const apiHost = process.env.API_HOST || pkg.proxy

console.info("connecting to" + apiHost);

module.exports = function(app) {
  app.use(
    '/api',
    proxy({
      target: apiHost,
      changeOrigin: true,
    })
  );
};