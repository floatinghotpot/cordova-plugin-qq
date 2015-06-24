
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

qqExport.RESULT = {
    SUCCESS: 0,
    CANCELLED: 1,
    NOTINSTALLED: 2,
    BADAPPID: 3,
    BADAPPKEY: 4,
    BADAPI: 5,
    BADDATA: 6,
    FAILED: 7
};

qqExport.setOptions = function(options, successCallback, failureCallback) {
	  if(typeof options === 'object') {
		  cordova.exec( successCallback, failureCallback, 'QQ', 'setOptions', [ options ] );
	  } else {
		  if(typeof failureCallback === 'function') {
			  failureCallback('options should be specified.');
		  }
	  }
	};

/*
* share(message, subject, img, url, function(shared){}, function(err){});
*/
qqExport.share = function(message, subject, img, url, successCallback, failureCallback) {
	var options = {
        message: message,
        subject: subject,
        image: img,
        url: url,
        qqZone: false,
    };
    
	cordova.exec( successCallback, failureCallback, 'QQ', 'share', [ options ] );
};

/*
* shareQZone(message, subject, img, url, function(shared){}, function(err){});
*/
qqExport.shareQZone = function(message, subject, img, url, successCallback, failureCallback) {
	var options = {
        message: message,
        subject: subject,
        image: img,
        url: url,
        qqZone: true,
    };
    
	cordova.exec( successCallback, failureCallback, 'QQ', 'share', [ options ] );
};

module.exports = qqExport;

