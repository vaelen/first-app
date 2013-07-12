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
#import <FacebookSDK/FacebookSDK.h>
#include <asl.h>

@interface FirstAppViewController ()
- (IBAction)signUpAction:(id)sender;
- (IBAction)toggleFacebook:(UISwitch*)sender;
- (void)authenticateWithFacebook;
- (void)createUser:(NSString*)username;
@property (weak, nonatomic) IBOutlet UITextField *usernameField;
@property (weak, nonatomic) IBOutlet UILabel *resultsLabel;
@property (weak, nonatomic) IBOutlet UISwitch *facebookSwitch;

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
   
    [self updateText:@"Working..."];

    
    if(self.facebookSwitch.on) {
        [self authenticateWithFacebook];
    } else {
        [self createUser:self.usernameField.text];
    }

}

- (void)createUser:(NSString*)username
{
    UIDevice *device = [UIDevice currentDevice];
    NSString *deviceId = [device.identifierForVendor UUIDString];
    NSString *deviceType = [NSString stringWithFormat:@"%@|%@|%@", device.model, device.systemName, device.systemVersion];
    
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
            } else if ([response statusCode] == 0) {
                NSLog(@"Could Not Connect to the Server.  Response Code: %ld, Error: %@", (long)[response statusCode], [error localizedDescription]);
                [self updateText:@"Could Not Connect To The Server" color:[UIColor redColor]];
            } else {
                NSLog(@"The Server Encountered An Error.  Response Code: %ld, Error: %@", (long)[response statusCode], [error localizedDescription]);
                [self updateText:@"The Server Encountered An Error" color:[UIColor redColor]];
            }
        }
    ];
    
    [operation start];
    
}

- (IBAction)toggleFacebook:(UISwitch*)sender {
    if(sender.on) {
        self.usernameField.text = @"";
    }
    self.usernameField.enabled = !(sender.on);
}

- (void)authenticateWithFacebook {
    NSLog(@"Authenticating With Facebook");
    [self updateText:@"Authenticating With Facebook..."];
    [FBSession openActiveSessionWithReadPermissions:@[@"basic_info", @"email"] allowLoginUI:YES completionHandler:
     ^(FBSession *session,
       FBSessionState state, NSError *error) {
         NSLog(@"Received Response From Facebook");
         [self sessionStateChanged:session state:state error:error];
     }];
}

- (void)sessionStateChanged:(FBSession *)session
                      state:(FBSessionState) state
                      error:(NSError *)error
{
    switch (state) {
        case FBSessionStateOpen: 
            if (FBSession.activeSession.isOpen) {
                [[FBRequest requestForMe] startWithCompletionHandler:
                 ^(FBRequestConnection *connection,
                   NSDictionary<FBGraphUser> *user,
                   NSError *error) {
                     if (!error) {
                         [self createUser:[user objectForKey:@"email"]];
                     } else {
                         [self updateText:@"Error Retrieving Email Address From Facebook" color:[UIColor redColor]];
                     }
                 }];      
            } else {
                NSLog(@"Session Not Open, Despite Changing To Open State");
                [self updateText:@"Facebook Login Failed" color:[UIColor redColor]];
            }
            break;
        case FBSessionStateClosedLoginFailed:
            NSLog(@"Facebook Login Failed");
            [self updateText:@"Facebook Login Failed" color:[UIColor redColor]];
            [FBSession.activeSession closeAndClearTokenInformation];
            break;
        case FBSessionStateClosed:
            NSLog(@"Facebook State Closed");
            [FBSession.activeSession closeAndClearTokenInformation];
            break;
        default:
            NSLog(@"Unknown Facebook State: %u", state);
            [self updateText:@"Unknown State" color:[UIColor redColor]];
            break;
    }
    
    if (error) {
        UIAlertView *alertView = [[UIAlertView alloc]
                                  initWithTitle:@"Error"
                                  message:error.localizedDescription
                                  delegate:nil
                                  cancelButtonTitle:@"OK"
                                  otherButtonTitles:nil];
        [alertView show];
    }    
}

@end
