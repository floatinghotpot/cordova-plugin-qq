//
//  GenericAdPlugin.m
//  TestAdMobCombo
//
//  Created by Xie Liming on 14-10-28.
//
//

#import <AdSupport/ASIdentifierManager.h>
#import <CommonCrypto/CommonDigest.h>

#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/TencentOAuthObject.h>

#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/QQApiInterfaceObject.h>

#import "QQPlugin.h"

@interface QQPlugin()<TencentSessionDelegate, QQApiInterfaceDelegate>

@property (nonatomic, retain) TencentOAuth* tencentOAuth;

- (void) validateLicense:(NSString*) license;

@end

@implementation QQPlugin

- (void)pluginInitialize
{
    [super pluginInitialize];
    
    self.inited = NO;
    self.licenseValidated = false;
    self.isTesting = false;
    self.logVerbose = false;
    
    srand((unsigned int) time(NULL));
}

- (void)dealloc {
    
}

- (void) setOptions:(CDVInvokedUrlCommand *)command {
    NSLog(@"setOptions");
    
    if([command.arguments count] > 0) {
        NSDictionary* options = [command argumentAtIndex:0 withDefault:[NSNull null]];
        [self parseOptions:options];
    }
    
    [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] to:command.callbackId];
}

- (void) share:(CDVInvokedUrlCommand *)command {
    NSLog(@"share");
    
    if(! [QQApiInterface isQQInstalled]) {
        [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                    messageAsInt:EQQAPIQQNOTINSTALLED]
                            to:command.callbackId];
        [self fireQQEvent:ERR_NOTINSTALLED withStr:@"QQNOTINSTALLED"];
        return;
    }
    
    if(! [QQApiInterface isQQSupportApi]) {
        [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                    messageAsInt:EQQAPIQQNOTSUPPORTAPI]
                            to:command.callbackId];
        [self fireQQEvent:ERR_API withStr:@"QQNOTSUPPORTAPI"];
        return;
    }
    
    if(! self.inited) {
        self.tencentOAuth = [[TencentOAuth alloc] initWithAppId:self.appId
                                                    andDelegate:self];
        self.inited = true;
    }
    
    if([command.arguments count] > 0) {
        NSDictionary* options = [command argumentAtIndex:0 withDefault:[NSNull null]];
        if([options count] > 1) {
            [self parseOptions:options];
        }
        
        NSString* message = [options objectForKey:OPT_MESSAGE];
        NSString* subject = [options objectForKey:OPT_SUBJECT];
        NSString* image = [options objectForKey:OPT_IMAGE];
        NSString* url = [options objectForKey:OPT_URL];
        
        NSString* str = [options objectForKey:OPT_QQZONE];
        BOOL qqZone = (str) ? [str boolValue] : NO;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            QQApiNewsObject *newsObj = [QQApiNewsObject
                                        objectWithURL:[NSURL URLWithString:url]
                                        title: subject
                                        description: message
                                        previewImageURL:[NSURL URLWithString:image]];
            SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:newsObj];

            QQApiSendResultCode sent = qqZone ? [QQApiInterface SendReqToQZone:req] : [QQApiInterface sendReq:req];
            
            if(sent == EQQAPISENDSUCESS) {
                [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
                                    to:command.callbackId];
            } else {
                [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                            messageAsInt:sent]
                                    to:command.callbackId];
            }
        });
        
    } else {
        [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                 messageAsString:@"param needed"]
                            to:command.callbackId];
    }
}

- (int) errCodeFromQQ: (int) retCode
{
    switch( retCode ) {
        case -4: return ERR_CANCELLED;
        case EQQAPISENDSUCESS: return ERR_SUCCESS;
        case EQQAPIQQNOTINSTALLED: return ERR_NOTINSTALLED;
        case EQQAPIQQNOTSUPPORTAPI: return ERR_API;
        case EQQAPIMESSAGETYPEINVALID: return ERR_DATA;
        case EQQAPIMESSAGECONTENTNULL: return ERR_DATA;
        case EQQAPIMESSAGECONTENTINVALID: return ERR_DATA;
        case EQQAPIAPPNOTREGISTED: return ERR_APPID;
        case EQQAPIAPPSHAREASYNC: return ERR_FAILED;
        case EQQAPIQQNOTSUPPORTAPI_WITH_ERRORSHOW: return ERR_API;
        case EQQAPIQZONENOTSUPPORTTEXT: return ERR_DATA;
        case EQQAPIQZONENOTSUPPORTIMAGE: return ERR_DATA;
        default: return ERR_FAILED;
    }
}

- (NSString*) errStrFromQQ: (int) retCode
{
    switch( retCode ) {
        case -4: return @"Cancelled";
        case EQQAPISENDSUCESS: return @"Success";
        case EQQAPIQQNOTINSTALLED: return @"QQNOTINSTALLED";
        case EQQAPIQQNOTSUPPORTAPI: return @"QQNOTSUPPORTAPI";
        case EQQAPIMESSAGETYPEINVALID: return @"MESSAGETYPEINVALID";
        case EQQAPIMESSAGECONTENTNULL: return @"MESSAGECONTENTNULL";
        case EQQAPIMESSAGECONTENTINVALID: return @"MESSAGECONTENTINVALID";
        case EQQAPIAPPNOTREGISTED: return @"APPNOTREGISTED";
        case EQQAPIAPPSHAREASYNC: return @"APPSHAREASYNC";
        case EQQAPIQQNOTSUPPORTAPI_WITH_ERRORSHOW: return @"QQNOTSUPPORTAPI_WITH_ERRORSHOW";
        case EQQAPIQZONENOTSUPPORTTEXT: return @"QZONENOTSUPPORTTEXT";
        case EQQAPIQZONENOTSUPPORTIMAGE: return @"QZONENOTSUPPORTIMAGE";
        default: return @"Unknown";
    }
}

- (void) fireQQSendEvent:(QQApiSendResultCode) retCode
{
    [self fireQQEvent:[self errCodeFromQQ:retCode] withStr:[self errStrFromQQ:retCode]];
}

- (void) fireQQEvent:(int)errCode withStr:(NSString*)errStr
{
    NSString* obj = [self __getProductShortName];
    NSString* json = [NSString stringWithFormat:@"{'errCode':%d,'errStr':'%@'}", errCode, errStr];
    [self fireEvent:obj event:@"QQEvent" withData:json];
}

- (void) parseOptions:(NSDictionary*) options
{
    if ((NSNull *)options == [NSNull null]) return;
    
    NSString* str = nil;
    
    str = [options objectForKey:OPT_LICENSE];
    if(str && [str length]>0) {
        [self validateLicense:str];
    }
    
    str = [options objectForKey:OPT_APPID];
    if(str) self.appId = str;
    
    str = [options objectForKey:OPT_APPKEY];
    if(str) self.appKey = str;
    
    str = [options objectForKey:OPT_APPNAME];
    if(str) self.appName = str;
}

- (void) validateLicense:(NSString*) license
{
    if(license && [license length] > 0) {
        NSArray* fields = [license componentsSeparatedByString:@"/"];
        if([fields count] >= 2) {
            NSString* userid = [fields objectAtIndex:0];
            NSString* key = [fields objectAtIndex:1];
            self.licenseValidated = [key isEqualToString:userid];
        }
    }

    if(self.licenseValidated) NSLog(@"valid license");
}

- (NSString*) md5:(NSString*) str
{
    const char *cStr = [str UTF8String];
    unsigned char digest[16];
    CC_MD5( cStr, (CC_LONG)strlen(cStr), digest );
    
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    
    return  output;
}

#pragma mark pure virtual methods

- (NSString*) __getProductShortName { return @"QQ"; }

#pragma mark TencentSessionDelegate methods

- (void) tencentDidLogin
{
    NSLog(@"tencentDidLogin");
}

- (void) tencentDidNotLogin:(BOOL)cancelled
{
    NSLog(@"tencentDidNotLogin");
}

- (void) tencentDidNotNetWork
{
    NSLog(@"tencentDidNotNetWork");
}

#pragma mark CDVPlugin overrides

- (void) handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];

    if([url isKindOfClass:[NSURL class]]) {
        [QQApiInterface handleOpenURL:url delegate:self];
    }
}

- (void)onReq:(QQBaseReq *)req
{
    NSLog( @"req" );
}

- (void)onResp:(QQBaseResp *)resp
{
    NSLog( @"resp" );

    if(resp != nil) {
        NSLog( @"%@, %@, %d, %@", resp.result, resp.description, resp.type, resp.extendInfo );
        [self fireQQSendEvent:[resp.result intValue]];
    }
}

- (void)isOnlineResponse:(NSDictionary *)response
{
    NSLog( @"response" );
}


@end
