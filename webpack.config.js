const path = require("path");
const webpack = require("webpack");

module.exports = {
  entry: __dirname+"/src/ui/index.js",
  mode: "development",
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /(node_modules|bower_components)/,
        loader: "babel-loader",
        options: { presets: ["@babel/env"] }
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"]
      }
    ]
  },
  resolve: { extensions: ["*", ".js", ".jsx"] },
  output: {
    path: path.resolve(__dirname, "dist/"),
    publicPath: "/dist/",
    filename: "bundle.js"
  },
  devServer: {
    contentBase: path.join(__dirname, "src/public/"),
    port: 8880,
    host: '0.0.0.0',
    publicPath: "http://0.0.0.0:8880/dist/",
    hotOnly: true,
    proxy: {
        '/genesis/*': {
            target: 'http://0.0.0.0:8000',
                secure: false,
                changeOrigin: true,
        }
    }
  },
  plugins: [new webpack.HotModuleReplacementPlugin()]
};