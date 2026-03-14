# Asset Service

The Asset Service is responsible for managing the assets within the system. It provides CRUD operations for assets.

## What is an Asset?

An asset is a digital representation of a physical entity. It can be a device or a vehicle. The Asset Service allows you to create, read, update and delete assets. Each asset has a unique identifier and can have many attributes associated with it.

## Device vs Vehicle

The Asset Service distinguishes between two types of assets: devices and vehicles. A device is a physical entity that can be connected to the system, such as a sensor or a gateway. A vehicle is a physical entity that can be tracked, such as a car or a truck. The Asset Service provides different attributes for devices and vehicles.

The vehicle should be associated with a device, which is responsible for tracking the vehicle, but the device can exist without a vehicle. This allows for flexibility in managing assets, as some devices may not be associated with a vehicle, while others may be associated with multiple vehicles over time.
This way, an administrator can configure the devices, and then the user can associate the vehicle with the device when needed. The user can also change the association between the device and the vehicle as needed; for example, if the user sells the vehicle, they can associate the device with a new vehicle.