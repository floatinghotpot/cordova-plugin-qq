//
//  GenericAdPlugin.m
//  TestAdMobCombo
//
//  Created by Xie Liming on 14-10-28.
//
//

#import <AdSupport/ASIdentifierManager.h>
#import <CommonCrypto/CommonDigest.h>
#import "QQPlugin.h"
#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/QQApiInterfaceObject.h>

@interface QQPlugin()

- (void) validateLicense:(NSString*) license;

@end

@implementation QQPlugin

- (void)pluginInitialize
{
    [super pluginInitialize];
    
    self.licenseValidated = false;
    self.isTesting = false;
    self.logVerbose = false;
    
    srand((unsigned int) time(NULL));
}

- (void)dealloc {

}

- (void) setOptions:(CDVInvokedUrlCommand *)command {
    if([command.arguments count] > 0) {
        NSDictionary* options = [command argumentAtIndex:0 withDefault:[NSNull null]];
        [self parseOptions:options];
    }
    
    [self sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] to:command.callbackId];
}

- (void) share:(CDVInvokedUrlCommand *)command {
    NSLog(@"share");
    
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
            NSString* genKey = [self md5:[NSString stringWithFormat:@"%@ licensed to %@ by floatinghotpot", [[self __getProductShortName] lowercaseString], userid]];
            self.licenseValidated = [genKey isEqualToString:key];
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

@end
