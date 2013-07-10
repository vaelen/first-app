//
//  FirstAppViewController.m
//  First App
//
//  Created by Andrew Young on 7/9/13.
//  Copyright (c) 2013 Andrew Young. All rights reserved.
//

#define createUserURL [NSURL URLWithString:@"http://localhost:9000/users"]

#import "FirstAppViewController.h"
#import "AFNetworking.h"

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

- (void)updateText:(NSString *)text
{
    [self updateText:text color:[UIColor blackColor]];
}

- (void)updateText:(NSString *)text color:(UIColor *)color
{
    self.resultsLabel.text = text;
    self.resultsLabel.textColor = color;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (IBAction)signUpAction:(id)sender
{
    NSString *username = self.usernameField.text;
    NSString *deviceId = @"1234";
    NSString *deviceType = @"iOS";
    
    [self updateText:@"Working..."];
    
    NSError* error;
    
    NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:
                          username, @"username",
                          deviceId, @"deviceId",
                          deviceType, @"deviceType",
                          nil];

    
    NSData* bodyData = [NSJSONSerialization dataWithJSONObject:data
                        options:NSJSONWritingPrettyPrinted
                        error:&error];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:createUserURL];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setHTTPMethod:@"POST"];
    [request setHTTPBody:bodyData];
    
    NSString *bodyDataString = [[NSString alloc] initWithData:bodyData encoding:NSUTF8StringEncoding];
    
    NSLog(@"Request Data: %@", bodyDataString);
    
    AFJSONRequestOperation *operation = [AFJSONRequestOperation JSONRequestOperationWithRequest:request
        success:^(NSURLRequest *request, NSHTTPURLResponse *response, id JSON) {
            NSLog(@"Account Created.  Response Code: %ld, JSON: %@", (long)[response statusCode], JSON);
            [self updateText:@"Account Created" color:[UIColor greenColor]];
            
        }
        failure:^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON){
            if([response statusCode] == 409) {
                NSLog(@"Account Already Exists.");
                [self updateText:@"That Account Already Exists" color:[UIColor redColor]];
            } else {
                NSLog(@"An Error Occurred.  Response Code: %ld, Error: %@", (long)[response statusCode], [error localizedDescription]);
                [self updateText:@"An Error Occurred" color:[UIColor redColor]];
            }
        }
    ];
    
    [operation start];
    
}

@end
