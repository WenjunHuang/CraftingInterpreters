const {merge} = require('webpack-merge');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const generatedConfig = require('./scalajs.webpack.config');
const commonConfig = require('./common.webpack.config');

const devConfig = {
  plugins: [
    new HtmlWebpackPlugin(
        {
          title: 'LoxWebUI',
          template: 'index-dev.html',
        },
    ),
  ],
};
module.exports = merge(generatedConfig, commonConfig, devConfig);


