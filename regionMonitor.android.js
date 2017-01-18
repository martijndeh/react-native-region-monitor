import {AppRegistry, NativeModules} from 'react-native';

const {RNRegionMonitor} = NativeModules;

const queuedEvents = [];
const callbacks = [];

AppRegistry.registerHeadlessTask("region-monitor-transition", () => {
	return (geofenceEvent) => {
		return new Promise((resolve, reject) => {
			if (callbacks.length == 0) {
				queuedEvents.push(geofenceEvent);
			} else {
				callbacks.forEach(callback => {
					callback(geofenceEvent);
				});
			}

			resolve();
		});
	}
});

export default {
	addCircularRegion: RNRegionMonitor.addCircularRegion,
	removeCircularRegion: RNRegionMonitor.removeCircularRegion,
	requestAuthorization: () => {
		// noop
	},
	onRegionChange: (callback) => {
		queuedEvents.forEach(queuedEvent => {
			callback(queuedEvent);
		});

		callbacks.push(callback);
	},
}
