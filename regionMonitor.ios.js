import { NativeModules, NativeEventEmitter } from 'react-native';

const { INVRegionMonitor } = NativeModules;
const RegionMonitorEventEmitter = new NativeEventEmitter(INVRegionMonitor);

export default {
	addCircularRegion: INVRegionMonitor.addCircularRegion,
	clearRegions: INVRegionMonitor.clearRegions,
	removeCircularRegion: INVRegionMonitor.removeCircularRegion,
	requestAuthorization: INVRegionMonitor.requestAuthorization,
	onRegionChange: (callback) => {
		const subscription = RegionMonitorEventEmitter.addListener(INVRegionMonitor.regionMonitorDidChangeRegion, callback);

		return function off() {
			subscription.remove();
		};
	},
}
