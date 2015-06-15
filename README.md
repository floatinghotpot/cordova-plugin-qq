## Cordova Plugin for Tencent QQ Open SDK ##


## Features ##

* Share to QQ

TODO:

* Auth with QQ Id
* Retrieve QQ user info
* Change QQ avatar

## APIs ##

```javascript
setOptions({
    appId: 'nnn',
    appName: 'TestQQ',
    appKey: 'xxx',
});

// share to QQ chat
share(message, subject, img, url, success_callback, fail_callback);

// share to QQZone
shareQZone(message, subject, img, url, success_callback, fail_callback);

// TODO:
// login(success_callback, fail_callback);
// logout()

```

## Example Code ##

```javascript
QQ.share('这是QQ分享的内容',
         '标题',
         'http://mat1.gtimg.com/www/icon/favicon2.ico',
         'https://github.com/floatinghotpot/cordova-plugin-qq',
    function(data){
        console.log('share okay');
    }, function(data){
        alert('share fail:' + data);
    });

```

## Credits ##

Created by Raymond Xie. All rights reserved.


