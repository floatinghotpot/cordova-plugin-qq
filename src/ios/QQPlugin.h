//
//  GenericAdPlugin.h
//  TestAdMobCombo
//
//  Created by Xie Liming on 14-10-28.
//
//

#import "CDVPluginExt.h"

#define OPT_LICENSE         @"license"
#define OPT_IS_TESTING      @"isTesting"
#define OPT_LOG_VERBOSE     @"logVerbose"

#define OPT_APPID           @"appId"
#define OPT_APPKEY          @"appKey"
#define OPT_APPNAME         @"appName"

#define OPT_MESSAGE         @"message"
#define OPT_SUBJECT         @"subject"
#define OPT_IMAGE           @"image"
#define OPT_URL             @"url"
#define OPT_QQZONE          @"qqZone"

@interface QQPlugin : CDVPluginExt

- (void) setOptions:(CDVInvokedUrlCommand *)command;

- (void) share:(CDVInvokedUrlCommand *)command;

@property (assign) BOOL inited;

@property (assign) BOOL licenseValidated;
@property (assign) BOOL isTesting;
@property (assign) BOOL logVerbose;

@property (nonatomic, retain) NSString* appId;
@property (nonatomic, retain) NSString* appKey;
@property (nonatomic, retain) NSString* appName;

#pragma mark virtual methods

- (void)pluginInitialize;

- (void) parseOptions:(NSDictionary*) options;

- (NSString*) md5:(NSString*) s;

@end
