#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import <CoreLocation/CoreLocation.h>

@interface INVRegionMonitor : RCTEventEmitter <RCTBridgeModule, CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager* locationManager;
@property (nonatomic, strong) NSMutableDictionary* pendingRegions;
@property (nonatomic, strong) NSMutableArray* pendingAuthorizations;
@property (nonatomic, assign) BOOL isRequestingAuthorization;

@end
