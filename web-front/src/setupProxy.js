const proxy = require('http-proxy-middleware');
const pkg = require('../package.json')

const apiHost = process.env.API_HOST || pkg.proxy

console.info(`CONNECTING TO [${apiHost}]`)

module.exports = function(app) {
  app.use('/api', proxy({
    target: apiHost, 
    changeOrigin: true
  }))
}