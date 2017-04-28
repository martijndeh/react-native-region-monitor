import {AppRegistry, NativeModules} from 'react-native';

const {RNRegionMonitor} = NativeModules;

/**
 * So that we can use the same onRegionChange interface as iOS, along with a headless task, we queue events that arrive before any callbacks are present
 *
 * Those events are sent to newly attached callbacks when they arrive.
 */
let queuedEvents = [];
let callbacks = [];

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
	clearRegions: RNRegionMonitor.clearRegions,
	removeCircularRegion: RNRegionMonitor.removeCircularRegion,
	requestAuthorization: () => {
		// noop
	},
	onRegionChange: (callback) => {
		queuedEvents.forEach(queuedEvent => {
			callback(queuedEvent);
		});
		queuedEvents = [];

		callbacks.push(callback);
	},
}
