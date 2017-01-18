import { NativeModules } from 'react-native';

const { RNRegionMonitor } = NativeModules;

export default {
	addCircularRegion: () => {
		console.log("addCircularRegion");
		RNRegionMonitor.addCircularRegion();
	},
	removeCircularRegion: () => {
		console.log("removeCircularRegion");
	},
	requestAuthorization: () => {
		console.log("requestAuthorization");
	},
	onRegionChange: (callback) => {
		console.log("onRegionChange");
	},
}
