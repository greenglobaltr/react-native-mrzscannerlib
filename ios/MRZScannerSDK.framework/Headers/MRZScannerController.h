
#import <UIKit/UIKit.h>
#import <CoreGraphics/CoreGraphics.h>
#import <CoreImage/CoreImage.h>
#import <AVFoundation/AVFoundation.h>
#import "MRZResultDataModel.h"

typedef enum {
    TYPE_MRZ = 0,
    TYPE_DOCUMENT_IMAGE_ID,
    TYPE_DOCUMENT_IMAGE_PASSPORT,
    TYPE_DRIVING_LICENCE,
    TYPE_DOCUMENT_IMAGE_ID_FRONT,
    TYPE_ID_SESSION
} MRZScannerType;

typedef enum {
    EFFORT_LEVEL_CASUAL = 0,
    EFFORT_LEVEL_TRY_HARDER,
    EFFORT_LEVEL_SWEATY
} MRZEffortLevel;


@protocol MRZScannerDelegate <NSObject>
@optional
- (void) successfulScanWithResult:(MRZResultDataModel*)result;
- (void) successfulDocumentScanWithImageResult:(UIImage*)resultImage;
- (void) scannerWasDismissed;
- (void) scanImageFailed;
- (void) permissionsWereDenied;
- (void) successfulIdFrontScanWithFullImage:(UIImage*)fullImage portrait:(UIImage*)portrait;
@end

@interface MRZScannerController : UIViewController <AVCaptureVideoDataOutputSampleBufferDelegate>

@property (nonatomic, weak) id <MRZScannerDelegate> delegate;

-(void) initUI:(UIViewController*)scannerPresenter;

/**
 Inintialize the user interface by specifying the frame of the scanner preview.
 
 @param partialRect the frame of the scanner preview. The CGRect represent percentages relative to the screen size [0 .. 100].
 */
-(void) initUI:(UIViewController*)scannerPresenter partialViewRect:(CGRect) partialRect;

/**
 Inintialize the user interface by specifying the frame of the scanner preview.
 
 @param portraitRect the frame of the scanner preview when the device is in portrait orientation. The CGRect represent percentages relative to the screen size [0 .. 100].
 @param landscapeRect the frame of the scanner preview when the device is in landscape orientation. The CGRect represent percentages relative to the screen size [0 .. 100].
 */
-(void) initUI:(UIViewController*)scannerPresenter partialViewRectPortrait:(CGRect) portraitRect partialViewRectLandscape:(CGRect) landscapeRect;

/**
 Specify the state of night mode. Night mode is used for increasing the ISO and brightness so the scanner performs better in dark environments.
 
 @param active [YES, NO]. Default value is NO.
 */
-(void) setNightModeActive:(BOOL)active;

/**
 Specify whether the scanner should detect and return result for IDs.
 
 @param active [YES, NO]. Default value is YES.
 */
+(void) setIDActive:(BOOL) active;

/**
 Specify whether the scanner should detect and return result for Passports.
 
 @param active [YES, NO]. Default value is YES.
 */
+(void) setPassportActive:(BOOL) active;

/**
 Specify whether the scanner should detect and return result for Visas.
 
 @param active [YES, NO]. Default value is YES.
 */
+(void) setVisaActive:(BOOL) active;

/**
 Specify the maximum number of CPU threads that the scanner can use during the scanning process.
 
 @param maxCPUCores Number of CPU threads. Default value is 2.
 */
-(void) setMaxCPUCores:(int) maxCPUCores;

/**
 Specify which scanner type you want to use. There are two options: "MRZ Scanner" and "Document Image scanner".
 The "MRZ Scanner" option is used to scan for MRZ.
 The "Document image scanner" is used for capturing front and back image of the ID documents.
 
 @param scannerType [SCANNER_TYPE_MRZ, SCANNER_TYPE_DOC_IMAGE_ID, SCANNER_TYPE_DOC_IMAGE_PASSPORT]. Default value is SCANNER_TYPE_MRZ
 */
-(void) setScannerType:(MRZScannerType) scannerType;

/**
 Set the date format in which the parsed dates are formatted.
 
 @param dateFormat The pattern describing the date format. Example: "dd.MM.yyyy"
 */
+(void) setDateFormat:(NSString *) dateFormat;

/**
 Resume scanning after the scanner has been paused/stopped. Usually after a successful scan.
 */
-(void) resumeScanner;

/**
 Stop and close the MRZ Scanner
 */
-(void) closeScanner;

/**
 Register with the licence key provided to remove the asterisks (*) from the result.
 
 @param key The provided licence key.
 @param registerResultHandler The result of licence registration.
 */
+(void) registerLicenseWithKey:(NSString*)key registerResultHandler:(void(^)(int result, NSError* error))registerResultHandler;

/**
Register with the licence key provided to remove the asterisks (*) from the result.

@param key The provided licence key.
@return Returns 0 for success, <0 if registration failed
*/
+(int) registerLicenseWithKey:(NSString*)key;

/**
 @return The current MRZScannerSDK Version.
 */
+(NSString*) getSDKVersion;

/**
 Scan a single image
 
 @param image The UIImage to be scanned.
 @return The result data model.
 */
+(MRZResultDataModel *) scanImage:(UIImage*) image;

/**
 Scan from gallery
 
 @param context The instance of the initializer controller.
 @param mrzDelegate MRZScannerDelegate
 */
+(void) scanFromGallery:(id)context delegate:(id<MRZScannerDelegate>)mrzDelegate;

/**
 Set the scanning rectangle to limit the scanning area. The parameters' values are representing percentages of the scanning preview.
 
 @param x the top left point of the scanning rectangle. [0,...,100] Default value: 1.
 @param y the top left point of the scanning rectangle. [0,...,100] Default value: 30.
 @param width the width of the scanning rectangle. [0,...,100] Default value: 98.
 @param height the height of the scanning rectangle. [0,...,100] Default value: 40.
 */
+(void) setScanningRectangleWithX:(float)x
                                y:(float)y
                            width:(float)width
                           height:(float)height;

/**
 Set valid issuing country codes. Successful scans with country codes not included in the array will be ignored.
 
 @param validIssuingCountries array with active country codes. Example value: @[@"D", @"USA"].
 */
+(void) setValidIssuingCountries:(NSArray<NSString*>*) validIssuingCountries;

/**
 Specify whether validation of country codes is enabled.
 
 @param enabled indicates whether validating is enabled. Default: YES.
 */
+(void) setValidateCountryCodesEnabled:(BOOL) enabled;

/**
 Successful scans with documents that are expired will be ignored and the scanning process will continue until a valid document is found.
 
 @param ignore whether the result should be ignored. Default value: false.
 */
+(void) ignoreResultIfDocumentIsExpired:(BOOL) ignore;

/**
 Set custom overlay image for the scanner.
 
 @param customImage the image to be shown on top of the scanner preview.
 */
-(void) setCustomOverlayImage:(UIImage *) customImage;

/**
 Enable minimizing of the scanner preview
 
 @param enableMinimized indicates whether the minimizing is enabled. Default value: false.
 */
-(void) enableMinimizedView:(BOOL) enableMinimized;

/**
 Enable scan from gallery. If enabled, a "scan from gallery" button will appear in the scanner preview.
 
 @param enabled indicates whether the "scan from gallery"  is enabled. Default value: false.
 */
-(void) enableScanFromGallery:(BOOL) enabled;

/**
 Enable upside-down scanning. If enabled, the scanner will also try the upside-down format of the preview.
 
 @param enabled indicates whether the "upside-down" feature  is enabled. Default value: NO.
 */
+(void) enableUpsideDownScanning:(BOOL) enabled;

/**
 * Turn flash on or off.
 *
 * @param on true = on, false = off. Default value: false.
 */
-(void) toggleFlash:(BOOL) on;

/**
 Enable vibration on successful scan.
 
 @param enabled indicates whether the vibration feature  is enabled. Default value: YES.
 */
+(void) enableVibrationOnSuccess:(BOOL) enabled;

/**
 If enabled, after successful scan, the scanner view will not be paused.
 
 See setIgnoreDuplicates for further continuous scanning behaviour changes
 
 @param enabled indicates whether the scanner should stay active after a successful scan. Default value: NO.
 */
- (void) setContinuousScanningEnabled:(BOOL) enabled;

/**
 Ignore duplicates when scanning continuously.
 
 @param ignore indicates whether the scanner should repeat the last successful scan. Default value: YES.
 */
- (void) setIgnoreDuplicates:(BOOL) ignore;

/**
  Choose the effort level that the scanner should apply for detecting a MRZ.
 
  @param effortLevel available options [EFFORT_LEVEL_CASUAL, EFFORT_LEVEL_TRY_HARDER, EFFORT_LEVEL_SWEATY]. Default value: EFFORT_LEVEL_TRY_HARDER.
 */
- (void) setEffortLevel:(MRZEffortLevel) effortLevel;

/**
 Perform face detection when scanning a passport to locate the portrait image and return it as part of MRZResultModel if detected.
 This feature is only available for Passport document types.
 
 @param enabled indicates whether or not to perform this action. Default value: YES.
 */
+ (void) setExtractPortraitEnabled:(BOOL) enabled;

/**
 Find and return an image of the signature as part of MRZResultModel.
 This feature is only available for Passport document types.
 
 @param enabled indicates whether or not to perform this action. Default value: YES.
 */
+ (void) setExtractSignatureEnabled:(BOOL) enabled;


/**
 Find and return a full image of the document as part of MRZResultModel.
 This feature is only available for Passport document types.
 
 @param enabled indicates whether or not to perform this action. Default value: YES.
 */
+ (void) setExtractFullPassportImageEnabled:(BOOL) enabled;

/**
 If enabled, the scanner will ignore results if check digits are not valid.
 Disabling it increases the chances of misread.
 
 @param enabled Default value: YES.
 */
+ (void) setIngoreInvalidCheckDigits:(BOOL) enabled;

/**
 Find and return a full image of ID's back side as part of MRZResultModel.
 This feature is only available for ID document types.
 
 @param enabled indicates whether or not to perform this action. Default value: YES.
 */
+ (void) setExtractIdBackEnabled:(BOOL) enabled;

/**
 Show close button.
 
 @param enabled indicates whether or not to show close button. Default value: YES.
 */
- (void) setShowCloseButton:(BOOL) enabled;

/**
 Show flash button.
 
 @param enabled indicates whether or not to show flash button. Default value: YES.
 */
- (void) setShowFlashButton:(BOOL) enabled;


+ (MRZResultDataModel*) scanImageReactNative:(UIImage*) image;

/**
 Use front camera.
 
 @param enabled indicates whether or not to use the front facing camera. Default value: NO.
 */
+ (void) setUseFrontCamera:(BOOL) enabled;

@end
