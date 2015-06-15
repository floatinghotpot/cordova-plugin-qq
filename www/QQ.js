
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

qqExport.ResultCode = {
    EQQAPISENDSUCESS : 0,
    EQQAPIQQNOTINSTALLED : 1,
    EQQAPIQQNOTSUPPORTAPI : 2,
    EQQAPIMESSAGETYPEINVALID : 3,
    EQQAPIMESSAGECONTENTNULL : 4,
    EQQAPIMESSAGECONTENTINVALID : 5,
    EQQAPIAPPNOTREGISTED : 6,
    EQQAPIAPPSHAREASYNC : 7,
    EQQAPIQQNOTSUPPORTAPI_WITH_ERRORSHOW : 8,
    EQQAPISENDFAILD : -1,
    //qzone分享不支持text类型分享
    EQQAPIQZONENOTSUPPORTTEXT : 10000,
    //qzone分享不支持image类型分享
    EQQAPIQZONENOTSUPPORTIMAGE : 10001,
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

qqExport.share = function(message, subject, img, url, successCallback, failureCallback) {
	var options = {
        message: message,
        subject: subject,
        img: img,
        url: url,
        qqZone: false,
    };
    
	cordova.exec( successCallback, failureCallback, 'QQ', 'share', [ options ] );
};

qqExport.shareQZone = function(message, subject, img, url, successCallback, failureCallback) {
	var options = {
        message: message,
        subject: subject,
        img: img,
        url: url,
        qqZone: true,
    };
    
	cordova.exec( successCallback, failureCallback, 'QQ', 'share', [ options ] );
};

module.exports = qqExport;

