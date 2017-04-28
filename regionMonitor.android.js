import {AppRegistry, NativeModules} from 'react-native';

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

export default {
	addCircularRegion: RNRegionMonitor.addCircularRegion,
	clearRegions: RNRegionMonitor.clearRegions,
	removeCircularRegion: RNRegionMonitor.removeCircularRegion,
	requestAuthorization: () => {
		// noop
	},
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
