
#import "RNMrzscannerlib.h"
#import <React/RCTBridgeModule.h>

@interface RNMrzscannerlib()

typedef enum {
    BASE_64 = 0,
    FILE_STORAGE,
} DocumentReturnType;

@property MRZScannerController *mrzScannerController;

@end

@implementation RNMrzscannerlib

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

bool extractPassportFull = NO;
bool extractPortrait = NO;
bool extractSignature = NO;
bool extractIdBack = NO;

- (NSArray<NSString *> *)supportedEvents {
    return @[@"successfulScanEmittedEvent",
             @"successfulDocumentScanEmittedEvent",
             @"scannerWasDismissedEmittedEvent",
             @"permissionsWereDeniedEmittedEvent",
             @"successfulIdFrontScanEmittedEvent",
             @"scanImageFailedEmittedEvent"];
}

DocumentReturnType documentType = BASE_64;
MRZScannerType scannerTypeVar = TYPE_MRZ;
int maxThreadsVar = 1;
bool flashToggleActivated = false;
bool continuousScanning = NO;
bool ignoreDuplicates = YES;
bool showCloseButton = YES;
bool showFlasheButton = YES;


RCT_EXPORT_METHOD(startScanner){
    [self startScannerExec:nil];
}

RCT_EXPORT_METHOD(startPartialViewScanner:(int)x y:(int)y width:(int)width height:(int)height) {
    [self startScannerExec:nil partialRect:CGRectMake(x, y, width, height)];
}

RCT_EXPORT_METHOD(startScannerWithCustomOverlay : (NSString*) base64String){
    [self startScannerExec:base64String];
}

RCT_EXPORT_METHOD(setScannerType:(int)scannerType){
    scannerTypeVar = scannerType;
}

RCT_EXPORT_METHOD(setMaxThreads:(int)maxThreads){
    maxThreadsVar = maxThreads;
}

RCT_EXPORT_METHOD(resumeScanner ){
    [_mrzScannerController resumeScanner];
}

RCT_EXPORT_METHOD(closeScanner) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_mrzScannerController closeScanner];
    });
}

RCT_EXPORT_METHOD( setNightModeActive:(BOOL)active){
    [_mrzScannerController setNightModeActive:active];
}

RCT_EXPORT_METHOD( setVibrateOnSuccessfulScan:(BOOL)active){
    [MRZScannerController enableVibrationOnSuccess:active];
}

RCT_EXPORT_METHOD( getSdkVersion:(RCTPromiseResolveBlock)resolve){
    resolve(MRZScannerController.getSDKVersion);
}

RCT_EXPORT_METHOD(registerWithLicenseKey :(NSString *) licenceKey){
    if (licenceKey) {
        [MRZScannerController registerLicenseWithKey:licenceKey];
    }
}

RCT_EXPORT_METHOD(scanImage :(NSString *) base64Image){
    NSData *data = [[NSData alloc]initWithBase64EncodedString:base64Image options:NSDataBase64DecodingIgnoreUnknownCharacters];
    
    if(data) {
        UIImage *image = [UIImage imageWithData:data];
        
        if(image) {
            MRZResultDataModel *result = [MRZScannerController scanImageReactNative:image];
            
            if (result) {
                [self successfulScanWithResult:result];
            }
        }
    }
}

RCT_EXPORT_METHOD(scanFromGallery){
    dispatch_async(dispatch_get_main_queue(), ^{
        [MRZScannerController scanFromGallery:UIApplication.sharedApplication.keyWindow.rootViewController delegate:self];
    });
}

RCT_EXPORT_METHOD(setDateFormat :(NSString *) dateFormat){
    [MRZScannerController setDateFormat:dateFormat];
}

RCT_EXPORT_METHOD(setDocumentImageReturnType :(int) documentReturnType) {
    documentType = documentReturnType;
}

RCT_EXPORT_METHOD(setShowCloseButton :(BOOL) active) {
    showCloseButton = active;
    if (_mrzScannerController) {
        [self.mrzScannerController setShowCloseButton:active];
    }
}

RCT_EXPORT_METHOD(setShowFlashButton :(BOOL) active) {
    showFlasheButton = active;
    if (_mrzScannerController) {
        [self.mrzScannerController setShowFlashButton:active];
    }
}

RCT_EXPORT_METHOD(setPassportActive :(BOOL) active) {
    [MRZScannerController setPassportActive:active];
}

RCT_EXPORT_METHOD(setIDActive :(BOOL) active ){
    [MRZScannerController setIDActive:active];
}

RCT_EXPORT_METHOD(setExtractPortraitEnabled :(BOOL) active ){
    extractPortrait = active;
    [MRZScannerController setExtractPortraitEnabled:extractPortrait];
}

RCT_EXPORT_METHOD(setExtractFullPassportImageEnabled :(BOOL) active ){
    extractPassportFull = active;
    [MRZScannerController setExtractFullPassportImageEnabled:extractPassportFull];
}

RCT_EXPORT_METHOD(setExtractSignatureEnabled :(BOOL) active ){
    extractSignature = active;
    [MRZScannerController setExtractSignatureEnabled:extractSignature];
}

RCT_EXPORT_METHOD(setExtractIdBackEnabled :(BOOL) active ){
    extractIdBack = active;
    [MRZScannerController setExtractIdBackEnabled:extractIdBack];
}

RCT_EXPORT_METHOD(toggleFlash :(BOOL) active) {
    flashToggleActivated = active;
    dispatch_async(dispatch_get_main_queue(), ^{
        if(self->_mrzScannerController) {
            [self->_mrzScannerController toggleFlash :active];
        }
    });
}

RCT_EXPORT_METHOD(setVisaActive :(BOOL) active ){
    [MRZScannerController setVisaActive:active];
}

RCT_EXPORT_METHOD(setContinuousScanningEnabled :(BOOL) enabled) {
    continuousScanning = enabled;
    
    if(_mrzScannerController)
        [_mrzScannerController setContinuousScanningEnabled:continuousScanning];
}

RCT_EXPORT_METHOD(setIgnoreDuplicatesEnabled :(BOOL) enabled) {
    ignoreDuplicates = enabled;
    
    if(_mrzScannerController)
        [_mrzScannerController setIgnoreDuplicates:ignoreDuplicates];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

-(void) startScannerExec:(NSString*) base64String {
    [self startScannerExec:base64String partialRect:CGRectMake(0, 0, 100, 100)];
}

-(void) startScannerExec:(NSString*) base64String partialRect:(CGRect)partialRect {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController* currentVC = UIApplication.sharedApplication.keyWindow.rootViewController;
        self->_mrzScannerController = [MRZScannerController new];
        if(flashToggleActivated){
            [self->_mrzScannerController toggleFlash:YES];
        }
        self->_mrzScannerController.delegate = self;
        [currentVC addChildViewController:self->_mrzScannerController];
        [self->_mrzScannerController setMaxCPUCores:maxThreadsVar];
        [self->_mrzScannerController setScannerType:scannerTypeVar];
        [self.mrzScannerController setContinuousScanningEnabled:continuousScanning];
        [self.mrzScannerController setIgnoreDuplicates:ignoreDuplicates];
        [self.mrzScannerController setShowCloseButton:showCloseButton];
        [self.mrzScannerController setShowFlashButton:showFlasheButton];
        [MRZScannerController setExtractFullPassportImageEnabled:extractPassportFull];
        [MRZScannerController setExtractPortraitEnabled:extractPortrait];
        [MRZScannerController setExtractSignatureEnabled:extractSignature];
        [MRZScannerController setExtractIdBackEnabled:extractIdBack];
        
        [self->_mrzScannerController initUI:currentVC partialViewRect:partialRect];
        
        if (base64String && ![base64String isEqualToString:@""]) {
            NSData *data = [[NSData alloc]initWithBase64EncodedString:base64String options:NSDataBase64DecodingIgnoreUnknownCharacters];
            
            [self->_mrzScannerController setCustomOverlayImage:[UIImage imageWithData:data]];
        }
    });
}

-(void)successfulIdFrontScanWithFullImage:(UIImage *)fullImage portrait:(UIImage *)portrait{
    NSMutableDictionary* jsonDictionary = [NSMutableDictionary new];
    
    if (documentType == FILE_STORAGE) {
        long timestamp = NSDate.date.timeIntervalSince1970;
        if (fullImage) {
            NSString *filePath = [self saveAsFile:fullImage name:[NSString stringWithFormat:@"fullImage%ld.png", timestamp]];
            if (filePath) {
                [jsonDictionary setObject:filePath forKey:@"fullImagePath"];
            }
        }
        
        if (portrait) {
            NSString *filePath = [self saveAsFile:portrait name:[NSString stringWithFormat:@"portraitImage%ld.png", timestamp]];
            if (filePath) {
                [jsonDictionary setObject: filePath forKey:@"portraitImagePath"];
            }
        }
    } else {
        if (fullImage) {
            NSData * dataFullImage = UIImagePNGRepresentation(fullImage);
            if (dataFullImage) {
                [jsonDictionary setObject:[dataFullImage base64EncodedStringWithOptions:0] forKey:@"fullImage"];
            }
        }
        
        if (portrait) {
            NSData * dataPortrait = UIImagePNGRepresentation(portrait);
            if (dataPortrait) {
                [jsonDictionary setObject:[dataPortrait base64EncodedStringWithOptions:0] forKey:@"portrait"];
            }
        }
    }
    
    [self sendEventWithName:@"successfulIdFrontScanEmittedEvent" body:jsonDictionary];
    [_mrzScannerController closeScanner];
}

-(NSString*)saveAsFile:(UIImage *)image name:(NSString *)imageName {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *mrzPath = [[paths objectAtIndex:0]stringByAppendingPathComponent:@"mrzimages"];
        
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [fileManager createDirectoryAtPath:mrzPath withIntermediateDirectories:YES attributes:nil error:nil];
    
    NSString *filePath = [mrzPath stringByAppendingPathComponent:imageName];
    
    [UIImagePNGRepresentation(image) writeToFile:filePath atomically:YES];
    
    return filePath;
}

-(void)successfulScanWithResult:(MRZResultDataModel *)result {
    NSString* jsonString = result.toJSON;
    
    if (![jsonString containsString:@"given_names_readable"]) {
        jsonString = [jsonString stringByReplacingOccurrencesOfString:@"given_names" withString:@"given_names_readable"];
    }
    
    NSData* data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError* err;
    NSMutableDictionary* jsonDictionary = [NSMutableDictionary dictionaryWithDictionary:[NSJSONSerialization JSONObjectWithData:data options:0 error:&err]];
    
    if (!err) {
        if (documentType == FILE_STORAGE) {
            long timestamp = NSDate.date.timeIntervalSince1970;
            if (result.idBack) {
                NSString * filePath = [self saveAsFile:result.idBack name:[NSString stringWithFormat:@"idBack%ld.png", timestamp]];
                if (filePath) {
                    [jsonDictionary setObject:filePath forKey:@"idBackImagePath"];
                }
            }
            
            if(result.idFront) {
                NSString * filePath = [self saveAsFile:result.idFront name:[NSString stringWithFormat:@"idFront%ld.png", timestamp]];
                if (filePath) {
                    [jsonDictionary setObject:filePath forKey:@"idFrontImagePath"];
                }
            }
            
            if (result.portrait) {
                NSString * filePath = [self saveAsFile:result.portrait name:[NSString stringWithFormat:@"portrait%ld.png", timestamp]];
                if (filePath) {
                    [jsonDictionary setObject:filePath forKey:@"portraitImagePath"];
                }
            }
            
            if (result.signature) {
                NSString * filePath = [self saveAsFile:result.signature name:[NSString stringWithFormat:@"signature%ld.png", timestamp]];
                if (filePath) {
                    [jsonDictionary setObject:filePath forKey:@"signatureImagePath"];
                }
                
            }
            
            if (result.fullImage) {
                NSString * filePath = [self saveAsFile:result.fullImage name:[NSString stringWithFormat:@"passportImage%ld.png", timestamp]];
                if (filePath) {
                    [jsonDictionary setObject:filePath forKey:@"passportImagePath"];
                }
            }
        } else {
            if (result.idBack) {
                NSData * data = UIImagePNGRepresentation(result.idBack);
                NSString *base64String = [data base64EncodedStringWithOptions:0];
                [jsonDictionary setObject:base64String forKey:@"idBack"];
            }
            
            if(result.idFront) {
                NSData * data = UIImagePNGRepresentation(result.idFront);
                NSString *base64String = [data base64EncodedStringWithOptions:0];
                [jsonDictionary setObject:base64String forKey:@"idFront"];
            }
            
            if (result.portrait) {
                NSData * imgData = UIImagePNGRepresentation(result.portrait);
                NSString *base64image = [imgData base64EncodedStringWithOptions:0];
                [jsonDictionary setObject:base64image forKey:@"portrait"];
            }
            
            if (result.signature) {
                NSData * imgData = UIImagePNGRepresentation(result.signature);
                NSString *base64image = [imgData base64EncodedStringWithOptions:0];
                [jsonDictionary setObject:base64image forKey:@"signature"];
            }
            
            if (result.fullImage) {
                NSData * imgData = UIImagePNGRepresentation(result.fullImage);
                NSString *base64image = [imgData base64EncodedStringWithOptions:0];
                [jsonDictionary setObject:base64image forKey:@"passportImage"];
            }
        }

        
        [self sendEventWithName:@"successfulScanEmittedEvent" body:jsonDictionary];
    }
    
    if (!continuousScanning) {
        [_mrzScannerController closeScanner];
    }
}

-(void)successfulDocumentScanWithImageResult:(UIImage *)resultImage {
    if (documentType == FILE_STORAGE) {
        long timestamp = NSDate.date.timeIntervalSince1970;
        if (resultImage) {
            NSString *filePath = [self saveAsFile:resultImage name:[NSString stringWithFormat:@"fullImage%ld.png", timestamp]];
            if (filePath) {
                [self sendEventWithName:@"successfulDocumentScanEmittedEvent" body:filePath];
            }
        }
    } else {
        NSData * data = UIImagePNGRepresentation(resultImage);
        NSString *base64image = [data base64EncodedStringWithOptions:0];
        [self sendEventWithName:@"successfulDocumentScanEmittedEvent" body:base64image];
    }
    [_mrzScannerController closeScanner];
}

-(void)scannerWasDismissed {
    [self sendEventWithName:@"scannerWasDismissedEmittedEvent" body:nil];
}

-(void)permissionsWereDenied {
    [self sendEventWithName:@"permissionsWereDeniedEmittedEvent" body:nil];
}

-(void)scanImageFailed {
    [self sendEventWithName:@"scanImageFailedEmittedEvent" body:nil];
}

@end

