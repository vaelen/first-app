//
//  FirstAppViewController.m
//  First App
//
//  Created by Andrew Young on 7/9/13.
//  Copyright (c) 2013 Andrew Young. All rights reserved.
//

#import "FirstAppViewController.h"

@interface FirstAppViewController ()
- (IBAction)signUpAction:(id)sender;
@property (weak, nonatomic) IBOutlet UITextField *usernameField;
@property (weak, nonatomic) IBOutlet UILabel *resultsLabel;

@end

@implementation FirstAppViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)signUpAction:(id)sender {
    NSString *username = self.usernameField.text;
    NSString *deviceId = @"1234";
    NSString *deviceType = @"iOS";
    
    boolean_t accountCreated = false;
    boolean_t errorOccured = false;
    
    NSString *message = @"Working...";
    UIColor *textColor = [UIColor blackColor];
    
    self.resultsLabel.text = message;
    self.resultsLabel.textColor = textColor;
    
    NSError* error;
    
    NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:
                          @"username", username,
                          @"deviceId", deviceId,
                          @"deviceType", deviceType,
                          nil];
    
    NSData* bodyData = [NSJSONSerialization dataWithJSONObject:data
                        options:NSJSONWritingPrettyPrinted
                        error:&error];
    
    NSMutableURLRequest *postRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"https://www.apple.com"]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    [postRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    // Designate the request a POST request and specify its body data
    [postRequest setHTTPMethod:@"POST"];
    [postRequest setHTTPBody:[NSData dataWithBytes:[bodyData bytes] length:[bodyData length]]];
    
    
    
    
    NSDictionary* resultData = [NSJSONSerialization
                                JSONObjectWithData:responseData
                                options:kNilOptions
                                error:&error];
    
    if(accountCreated) {
        // Success
        message = @"Account Created";
        textColor = [UIColor greenColor];
    } else if(errorOccured) {
        // Error
        message = [[NSString alloc] initWithFormat:@"Error: %@!", message];
        textColor = [UIColor redColor];
    } else {
        // Account Already Exists
        message = @"Account Already Exists";
        textColor = [UIColor redColor];
    }
    
    self.resultsLabel.text = message;
    self.resultsLabel.textColor = textColor;
}
@end
