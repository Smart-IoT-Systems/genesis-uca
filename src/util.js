/*
 * Provide a helper functions
 */


/*
 * Chain exceptions together for easier debugging.
 */
function chainError(message, cause) {
    var newMessage = `${message} (no given cause)`;
    if (cause.stack) {
        var brokenLine = cause.stack
            .split("\n")
            .find(line => line.match(/^    at /));
        newMessage =
          `${message}\n` +
              `Caused by: ${cause.message.replace(/\n/g, "\n|\t").trim()}\n` +
              `${brokenLine}\n`;
    }
    throw new Error(newMessage);
}


module.exports = {
    chainError: chainError
};
