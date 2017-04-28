import {AppRegistry, NativeModules, PermissionsAndroid} from 'react-native';

const {RNRegionMonitor} = NativeModules;

let callbacks = [];

AppRegistry.registerHeadlessTask("region-monitor-transition", () => {
	return (geofenceEvent) => {
		return new Promise((resolve, reject) => {
			callbacks.forEach(callback => {
				callback(geofenceEvent);
			});
			resolve();
		});
	}
});

const permissionsCheck = () => {
	return PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION).then(permissionStatus => {
		switch (permissionStatus) {
			case true: // Pre-Marshmallow Android devices send true back
			case PermissionsAndroid.RESULTS.GRANTED: {
				return true;
			}
			case PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN:
			case PermissionsAndroid.RESULTS.DENIED:
			default: {
				throw new Error("Cannot obtain permissions to perform geofencing");
			}
		}
	});
};

export default {
	addCircularRegion: (center, radius, id) => {
		return permissionsCheck().then(() => {
			return RNRegionMonitor.addCircularRegion(center, radius, id);
		});
	},
	clearRegions: RNRegionMonitor.clearRegions,
	removeCircularRegion: RNRegionMonitor.removeCircularRegion,
	requestAuthorization: permissionsCheck,
	onRegionChange: (callback) => {
		callbacks.push(callback);
		return function off() {
			const idx = callbacks.indexOf(callback);
			if(idx >= 0) {
				callbacks.splice(i, 1);
			}
		};
	},
}
