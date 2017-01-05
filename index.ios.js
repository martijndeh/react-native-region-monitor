import { NativeModules, NativeEventEmitter } from 'react-native';

const { INVRegionMonitor } = NativeModules;
const RegionMonitorEventEmitter = new NativeEventEmitter(INVRegionMonitor);

export default {
	addCircularRegion: INVRegionMonitor.addCircularRegion,
	removeCircularRegion: INVRegionMonitor.removeCircularRegion,
	requestAuthorization: INVRegionMonitor.requestAuthorization,
	onRegionChange: (callback) => {
		const subscription = RegionMonitorEventEmitter.addListener('regionMonitorDidChangeRegion', callback);

		return function off() {
			subscription.remove();
		};
	},
}
