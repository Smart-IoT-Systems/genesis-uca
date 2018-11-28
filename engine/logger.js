var winston = require('winston');

const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.printf(info => {
            return `${info.timestamp} - [${info.level}]: ${info.message}`;
        })
    ),
    transports: [new winston.transports.Console({
        handleExceptions: true,
        json: false,
        colorize: true,
      }), new winston.transports.File({filename: 'genesis.log'})]
});

module.exports = logger;