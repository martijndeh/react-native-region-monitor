#import "INVRegionMonitor.h"
#import <CoreLocation/CoreLocation.h>
#import <React/RCTLog.h>

// TODO: Export this event name
NSString* INVRegionMonitorDidChangeRegionEvent = @"regionMonitorDidChangeRegion";
NSString* INVRegionMonitorErrorDomain = @"INVRegionMonitorErrorDomain";

@implementation INVRegionMonitor

@synthesize locationManager;
@synthesize pendingRegions;
@synthesize pendingAuthorizations;
@synthesize isRequestingAuthorization;

RCT_EXPORT_MODULE()

- (void) dealloc {
    locationManager.delegate = nil;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[INVRegionMonitorDidChangeRegionEvent];
}

- (instancetype) init {
    self = [super init];

    if (self) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        pendingRegions = [[NSMutableDictionary alloc] init];
        pendingAuthorizations = [[NSMutableArray alloc] init];
        isRequestingAuthorization = NO;
    }

    return self;
}

- (void)_addCircularRegion:(NSDictionary *)center
                    radius:(CLLocationDistance)radius
                identifier:(NSString *)identifier {
    CLLocationCoordinate2D coordinate;
    coordinate.latitude = [center[@"latitude"] doubleValue];
    coordinate.longitude = [center[@"longitude"] doubleValue];

    RCTLogInfo(@"_addCircularRegion:radius:identifier: %f %f %@", coordinate.latitude, coordinate.longitude, identifier);

    CLCircularRegion* region = [[CLCircularRegion alloc]
                                   initWithCenter:coordinate
                                   radius:MIN(locationManager.maximumRegionMonitoringDistance, radius)
                                   identifier:identifier];
    [locationManager startMonitoringForRegion:region];
}

- (BOOL)_isRegionMonitoringPossible:(CLAuthorizationStatus)status {
    return (
        [CLLocationManager isMonitoringAvailableForClass:[CLCircularRegion class]] &&
        (
            status == kCLAuthorizationStatusAuthorized ||
            status == kCLAuthorizationStatusAuthorizedAlways ||
            status == kCLAuthorizationStatusNotDetermined
        )
    );
}

- (void)_sendRegionChangeEventWithIdentifier:(NSString *)identifier didEnter:(BOOL)didEnter didExit:(BOOL)didExit {
    [self sendEventWithName:INVRegionMonitorDidChangeRegionEvent body:@{
        @"region": @{
            @"identifier": identifier,
        },
        @"didEnter": @(didEnter),
        @"didExit": @(didExit),
    }];
}

- (CLCircularRegion *)_getMonitoredRegionWithIdentifier:(NSString *)identifier {
    NSSet *regions = [locationManager.monitoredRegions objectsPassingTest:^BOOL(CLCircularRegion *region, BOOL *stop) {
        return ([region.identifier isEqualToString:identifier]);
    }];
    return [regions anyObject];
}

RCT_EXPORT_METHOD(addCircularRegion:(nonnull NSDictionary *)center
                             radius:(CLLocationDistance)radius
                         identifier:(nonnull NSString *)identifier
                          resolver:(RCTPromiseResolveBlock)resolve
                          rejecter:(RCTPromiseRejectBlock)reject) {
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (![self _isRegionMonitoringPossible:status]) {
        NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:6 userInfo:nil];
        reject(@"monitoring_unavailable", @"Region monitoring unavailable, restricted or denied by the user.", error);
        return;
    }

    if (pendingRegions[identifier]) {
        // There is a pending region so we immediately fail adding this region.
        NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:1 userInfo:nil];
        reject(@"pending_region", @"Adding region failed because of a pending region with the same identifier.", error);
        return;
    }

    CLCircularRegion *existingRegion = [self _getMonitoredRegionWithIdentifier:identifier];
    if (existingRegion != nil) {
        NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:8 userInfo:nil];
        reject(@"region_already_exists", @"Adding region failed because a region with the same idenitifier already exists.", error);
        return;
    }

    pendingRegions[identifier] = @{
        @"center": center,
        @"radius": @(radius),
        @"resolve": resolve,
        @"reject": reject,
    };

    if (status == kCLAuthorizationStatusNotDetermined) {
        if (!isRequestingAuthorization) {
            isRequestingAuthorization = YES;

            [locationManager requestAlwaysAuthorization];
        }

        return;
    }

    [self _addCircularRegion:center radius:radius identifier:identifier];
}

RCT_EXPORT_METHOD(removeCircularRegion:(nonnull NSString *)identifier
                              resolver:(RCTPromiseResolveBlock)resolve
                              rejecter:(RCTPromiseRejectBlock)reject) {
    CLCircularRegion *region = [self _getMonitoredRegionWithIdentifier:identifier];

    if (region != nil) {
        RCTLogInfo(@"Stop monitoring region %@", region.identifier);

        [locationManager stopMonitoringForRegion:region];

        resolve(nil);
    }
    else {
        RCTLogInfo(@"Could not find region %@ in %@", region.identifier, locationManager.monitoredRegions);

        NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:2 userInfo:nil];
        reject(@"remove_region", @"Removing region failed because the region does not exist.", error);
    }
}

RCT_EXPORT_METHOD(requestAuthorization:(RCTPromiseResolveBlock)resolve
                              rejecter:(RCTPromiseRejectBlock)reject) {
    if (isRequestingAuthorization) {
        // Requesting authorization is in progress.
        [pendingAuthorizations addObject:@{
            @"resolve": resolve,
            @"reject": reject,
        }];

        return;
    }

    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if ([self _isRegionMonitoringPossible:status]) {
        if (status == kCLAuthorizationStatusNotDetermined) {
            RCTLogInfo(@"Status not determined, requesting authorization");

            isRequestingAuthorization = YES;

            [pendingAuthorizations addObject:@{
                @"resolve": resolve,
                @"reject": reject,
            }];

            [locationManager requestAlwaysAuthorization];
        }
        else if (status == kCLAuthorizationStatusAuthorizedAlways ||
                 status == kCLAuthorizationStatusAuthorized) {
            // We are already authorized.
            resolve(nil);
        }
    }
    else {
        NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:6 userInfo:@{
            @"status": @(status),
        }];

        reject(@"request_authorization", @"Region monitoring unavailable, restricted or denied by the user.", error);
    }
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    RCTLogInfo(@"Auth is %d", status);

    isRequestingAuthorization = NO;

    if (pendingRegions.count > 0) {
        if (status == kCLAuthorizationStatusAuthorizedAlways ||
            status == kCLAuthorizationStatusAuthorized) {
            // We add all regions and take it from there.
            for (NSString *identifier in pendingRegions.keyEnumerator) {
                NSDictionary *pendingRegion = pendingRegions[identifier];
                NSDictionary *center = pendingRegion[@"center"];
                NSNumber *radius = pendingRegion[@"radius"];

                [self _addCircularRegion:center radius:radius.doubleValue identifier:identifier];
            }
        }
        else {
            NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:5 userInfo:@{
                @"status": @(status),
            }];

            for (NSString *identifier in pendingRegions.keyEnumerator) {
                NSDictionary *pendingRegion = pendingRegions[identifier];
                RCTPromiseRejectBlock reject = pendingRegion[@"reject"];
                reject(@"monitoring_failed", @"Failed to start region monitoring.", error);
            }

            [pendingRegions removeAllObjects];
        }
    }

    if (pendingAuthorizations.count > 0) {
        if (status == kCLAuthorizationStatusAuthorizedAlways ||
            status == kCLAuthorizationStatusAuthorized) {
            for (NSDictionary *pendingAuthorization in pendingAuthorizations) {
                RCTPromiseResolveBlock resolve = pendingAuthorization[@"resolve"];
                resolve(nil);
            }
        }
        else {
            NSError *error = [[NSError alloc] initWithDomain:INVRegionMonitorErrorDomain code:5 userInfo:@{
                @"status": @(status),
            }];

            for (NSDictionary *pendingAuthorization in pendingAuthorizations) {
                RCTPromiseRejectBlock reject = pendingAuthorization[@"reject"];
                reject(@"authorization_failed", @"Requesting region monitoring authorization failed.", error);
            }
        }

        [pendingAuthorizations removeAllObjects];
    }
}

- (void)locationManager:(CLLocationManager *)manager
         didEnterRegion:(CLRegion *)region {
    RCTLogInfo(@"Did enter region %@", region.identifier);

    [self _sendRegionChangeEventWithIdentifier:region.identifier didEnter:YES didExit:NO];
}

- (void)locationManager:(CLLocationManager *)manager
          didExitRegion:(CLRegion *)region {
    RCTLogInfo(@"Did exit region %@", region.identifier);

    [self _sendRegionChangeEventWithIdentifier:region.identifier didEnter:NO didExit:YES];
}

- (void)locationManager:(CLLocationManager *)manager
monitoringDidFailForRegion:(CLRegion *)region
              withError:(NSError *)error {
    RCTLogInfo(@"Region failed to monitor %@ %@!", region.identifier, error);

    // TODO: Check if we already have this region in the monitoredRegions?
    RCTLogInfo(@"%@", locationManager.monitoredRegions);

    NSString *identifier = region.identifier;
    NSDictionary *pendingRegion = pendingRegions[identifier];

    if (pendingRegion != nil) {
        RCTPromiseRejectBlock reject = pendingRegion[@"reject"];
        reject(@"monitoring_failed", @"Failed to start region monitoring.", error);

        [pendingRegions removeObjectForKey:identifier];
    }
}

- (void)locationManager:(CLLocationManager *)manager
didStartMonitoringForRegion:(CLRegion *)region {
    RCTLogInfo(@"Yes start monitor %@!", region.identifier);

    NSString *identifier = region.identifier;
    NSDictionary *pendingRegion = pendingRegions[identifier];

    if (pendingRegion != nil) {
        RCTPromiseResolveBlock resolve = pendingRegion[@"resolve"];
        resolve(nil);

        [pendingRegions removeObjectForKey:identifier];

        // We request the state so we can immediatelly emit an event if we're already inside this region.
        [locationManager requestStateForRegion:region];
    }
}

@end
