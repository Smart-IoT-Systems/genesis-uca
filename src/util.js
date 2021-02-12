/*
 * Provide a helper functions
 */


/*
 * Chain exceptions together for easier debugging.
 */
function chainError(message, cause) {
    var brokenLine = cause.stack
	.split("\n")
	.find(line => line.match(/^    at /));
    const newMessage =
	  `${message}\n` + 
	  `Caused by: ${cause.message.replace(/\n/g, "\n|\t")}\n` +
	  `${brokenLine}\n`;
    throw new Error(newMessage);
}


module.exports = {
    chainError: chainError
};
