# react-native-region-monitor

A simple and easy to use geographical region monitoring API for React Native. This only works for iOS.

## Getting started

```
$ npm install react-native-region-monitor --save
$ react-native link
```

## Info.plist

It's important that you set `Privacy - Location Always Usage Description` and `Required background modes` in your Info.plist or region monitoring will not function properly.

## Usage

Simply add a region and the library will automatically request the correct authorization and add the region afterwards.

```js
import regionMonitor from 'react-native-region-monitor';

const center = {
	latitude: 52.0834365,
	longitude: 4.3121346,
};
const radius = 1000;
const identifier = 'MyRegionIdentifier';

regionMonitor.addCircularRegion(center, radius, identifier)
	.then(() => {
		// Added.
	})
	.catch((error) => {
		// Something failed.
	});
```

## API Documentation

#### `regionMonitor#addCircularRegion(center, radius, identifier)`
- `center` `Object` The coordinate which defines the center location of the circular region.
- `radius` `Integer` The radius of the region in meters. If the radius exceeds the maximum as defined by iOS, the radius is automatically clamped to the maximum value.
- `identifier` `String` A unique identifier of the region.

This method creates a new region object and starts monitoring the region. Monitored regions are persisted between app launches. This means you don't have to re-add regions every time your app starts.

If region monitoring authorization is not requested yet, this method automatically requests authorization which display the permission prompt to the user, see [regionMonitor#requestAuthorization](#regionMonitor-requestAuthorization). This only works if the Info.plist is configured correctly. See [Info.plist](#info-plist) section.

This method returns a promise which resolves when the region was monitored successfully or rejects when this fails. In case location authorization is not requested, authorization is first requested, and if it allowed, the region is added. This might take a while as it requires user interaction.

Once a region is successfully added and the user enters or exits a region, the region changed callback is invoked. See `regionMonitor#onRegionChange` on how to set and use this callback. When adding a region and the user is already inside the region, the region changed callback is immediately invoked.

If a region with the same identifier is already being monitored, or is in the process of being added, adding the region fails. If you want to edit a region, you first have to remove it and then add it again.

You can add up to 20 regions. This is a limit set by iOS. If you need to add more regions, there are alternative libraries which workaround this limitation.

##### Example

```js
import regionMonitor from 'react-native-region-monitor';

const center = {
	latitude: 52.0834365,
	longitude: 4.3121346,
};
const radius = 1000;
const identifier = 'MyRegionIdentifier';

regionMonitor.addCircularRegion(center, radius, identifier)
	.then(() => {
		// Added.
	})
	.catch((error) => {
		// Something failed.
	});
```

#### `regionMonitor#onRegionChange(callback)`
- `callback` `Function` The callback to invoke when the user enters or exits a region.

Registers a callback to be invoked whenever a user enters or exits a region. The callback is invoked with an `event` argument which contains `didEnter` and `didExit` booleans and a `region` object which contains an `identifier` property. The radius and center location are not available on the region object.

Both in the simulator and on an actual device it might take some seconds up till some minutes before a region change is noticed by iOS.

This method returns a function which you should invoke when you want to unregister the callback, for example, in your apps `componentWillUnmount`.

#### Example
```js
const off = regionMonitor.onRegionChange((event) => {
	const { didEnter, didExit, region } = event;

	if (didEnter) {
		console.log(`Enter region ${region.identifier}.`);
	}
	else if (didExit) {
		console.log(`Exit region ${region.identifier}.`);
	}
});

// Whenever you want to unregister.
off();
```

#### `regionMonitor#removeCircularRegion(identifier)`
- `identifier` `String` The identifier of the region to remove.

Removes a monitored region with `identifier`. This method returns a promise which resolves if the region was found and scheduled to be removed, and rejects if the region could not be found (if it was not monitored).

#### Example

```js
const identifier = 'MyRegionIdentifier';

regionMonitor.removeCircularRegion(identifier)
	.then(() => {
		// Region scheduled to be removed.
	})
	.catch((error) => {
		// Removing failed likely because the region is not monitored.
	});
```

#### `regionMonitor#requestAuthorization()`

Requests the authorization required to initiate region monitoring. This method returns a promise which resolves if the authorization is allowed (or was already allowed) or rejects if region monitoring is not possible (either denied, restricted or not supported by the hardware).

Please note: the returned promise might take a while to resolve. This is because, if authorization is requested and a prompt is shown to the user, this promise resolves after the user allows the authorization (or rejects if the user denies the authorization).

If the user denied the authorization, the user can allow the authorization by going to the settings and changing the location permissions for the given app. It's not possible to prompt the user with an authorization prompt (again).

You don't have to call this method manually. When adding your first region by invoking `regionMonitor#addCircularRegion(center, radius, identifier)` authorization is automatically requested if it has not been requested yet.

If you do not configure your Info.plist correctly the authorization prompt will not show. See [Info.plist](#info-plist) for more information.

#### Example

```js
regionMonitor.requestAuthorization()
	.then(() => {
		// Allowed or already allowed.
	})
	.catch((error) => {
		// Authorization denied, restricted or region monitoring not supported.
	});
```
