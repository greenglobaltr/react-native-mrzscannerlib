//
//  MRZResultDataModel.h
//  MRZ
//
//  Created by Filip Siljavski on 12/25/17.
//  Copyright © 2017 Adamantus. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface MRZResultDataModel : NSObject

@property NSString * raw_result;
@property NSString * _id;
@property NSString * document_type_raw;
@property NSString * document_type_readable;
@property NSString * issuing_country;
@property NSArray<NSString*> * surnames;
@property NSArray<NSString*> * given_names;
@property (nonatomic) NSString * given_names_readable;
@property NSString * document_number;
@property NSString * nationality;
@property NSString * dob_raw;
@property NSString * dob_readable;
@property NSString * sex;
@property NSString * est_issuing_date_raw;
@property NSString * est_issuing_date_readable;
@property NSString * expiration_date_raw;
@property NSString * expiration_date_readable;
@property NSArray<NSString*> * optionals;
@property NSString * optionals_readable;
@property NSString * surnames_readable;
@property int dateScanned;
@property NSString * fullName;
@property UIImage* portrait;
@property UIImage* signature;
@property UIImage* fullImage;
@property UIImage* idBack;
@property UIImage* idFront;
@property NSString * master_check_digit;
@property NSString * document_number_with_check_digit;
@property NSString * expiration_date_with_check_digit;
@property NSString * dob_with_check_digit;
@property BOOL are_check_digits_valid;

- (void) parseRawDataToReadable;
- (NSString*) toJSON;
- (NSString*) toReadableString;
- (NSString*) toCommaSeparatedString;
- (NSString*) dob_check_digit;
- (NSString*) document_number_check_digit;
- (NSString*) expiration_date_check_digit;
- (BOOL) isExpired;
- (BOOL) isId;
- (BOOL) isPassport;
- (BOOL) isVisa;

- (id) initWithMRZText:(NSString *)text
          visaSubtype:(int)visaSubtype
          dateScanned:(int)dateScanned
are_check_digits_valid:(BOOL) are_check_digits_valid;

-(id) initWithCSVString:(NSString *) csvString;
-(id) initWithDocumentType:(NSString *) document_type
            issuingCountry:(NSString *) issuingCountry
                  surnames:(NSArray<NSString*> *) surnames
            documentNumber:(NSString *) documentNumber
               nationality:(NSString *) nationality
                       dob:(NSString *) dob
                       sex:(NSString *) sex
           expiration_date:(NSString *) expirationDate
            estIssuingDate:(NSString *) estIssuingDate
                 optionals:(NSArray<NSString*> *) optionals
                givenNames:(NSArray<NSString*> *) givenNames
               dateScanned:(int) dateScanned
          masterCheckDigit:(NSString*) masterCheckDigit
      dob_with_check_digit:(NSString*) dob_with_check_digit
document_number_with_check_digit:(NSString*) document_number_with_check_digit
expiration_date_with_check_digit:(NSString*) expiration_date_with_check_digit
    are_check_digits_valid:(BOOL) are_check_digits_valid;

@end
