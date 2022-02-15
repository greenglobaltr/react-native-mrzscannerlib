
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import <React/RCTEventEmitter.h>

#import <MRZScannerSDK/MRZScannerSDK.h>

@interface RNMrzscannerlib : RCTEventEmitter <RCTBridgeModule, MRZScannerDelegate>

@end
  
