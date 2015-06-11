
var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var qqExport = {};

/*
 * set options:
 *  {
 *      appName: string,
 *      appId: string,
 *      isTesting: boolean,	
 *   }
 */

qqExport.setOptions = function(options, successCallback, failureCallback) {
	  if(typeof options === 'object') {
		  cordova.exec( successCallback, failureCallback, 'QQ', 'setOptions', [ options ] );
	  } else {
		  if(typeof failureCallback === 'function') {
			  failureCallback('options should be specified.');
		  }
	  }
	};

qqExport.share = function(message, subject, img, url, successCallback, failureCallback) {
	var options = {
        message: message,
        subject: subject,
        img: img,
        url: url
    };
    
	cordova.exec( successCallback, failureCallback, 'QQ', 'share', [ options ] );
};

module.exports = qqExport;

