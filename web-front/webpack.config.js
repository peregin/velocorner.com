const { GitRevisionPlugin } = require('git-revision-webpack-plugin')

module.exports = {
  plugins: [new GitRevisionPlugin()],
  mode: 'production'
}