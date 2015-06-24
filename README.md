## Cordova Plugin for Tencent QQ Open SDK ##


## Features ##

* Share to QQ

TODO:

* Auth with QQ Id
* Retrieve QQ user info
* Change QQ avatar

## APIs ##

```javascript
// set app id before share
setOptions({
    appId: 'nnn',
    appName: 'TestQQ',
    appKey: 'xxx',
});

// share to QQ chat
share(message, subject, img, url, success_callback, fail_callback);

// share to QQZone
shareQZone(message, subject, img, url, success_callback, fail_callback);

success_callback = function(shared) {
    if(shared) console.log('shared');
    else console.log('cancelled');
};

fail_callback = function(err) {
    console.log('share fail, error code: ' + err);
};

// TODO:
// login(success_callback, fail_callback);
// logout()

```

## Example Code ##

```javascript
    QQ.setOptions({
        appId: '1234567',
        appName: 'Demo for QQPlugin',
        appKey: 'xxx'
    });

    QQ.share('这是QQ分享的内容',
             '标题',
             'http://img3.cache.netease.com/photo/0005/2013-03-07/8PBKS8G400BV0005.jpg',
             'https://github.com/floatinghotpot/cordova-plugin-qq',
        function(ok){
            alert('share: ' + (ok ? 'okay':'cancelled'));
        }, function(err){
            alert('share fail, err:' + err);
        });

```

## Credits ##

Created by Raymond Xie. All rights reserved.


