module.exports = {
  "/api": {
    target: "http://localhost:8080",
    secure: false,
    logLevel: "debug",
    changeOrigin: true,
    bypass: function (req, res, proxyOptions) {
      req.headers["Origin"] = "http://localhost:8080";
    }
  }
}; 