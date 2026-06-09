# Fuel Tracker App Product Spec

## 1. Product Summary

Fuel Tracker is an Android app for logging car fuel purchases and tracking driving costs over time. Version 1 is designed for a single user and a single car. The app should also support trip tracking so the user can measure the cost and fuel usage of a specific longer drive.

The app must work fully offline and store data locally on the device.

## 2. Goals

- Track fuel purchases accurately
- Show how much fuel the car uses over time
- Show how much driving costs
- Let the user track the cost and fuel usage of specific trips
- Keep the interface simple, minimal, and easy to use

## 3. Non-Goals For Version 1

- Multiple cars
- Multiple users
- Cloud sync
- Receipt scanning
- Photo attachments
- Export/import
- Multiple currencies
- Unit switching

These can be added later.

## 4. Target Platform

- Android phone
- Local-first app
- Offline use supported

## 5. User Type

- One primary user
- User tracks one personal car

## 6. Core User Flows

### 6.1 Car Setup

On first launch, the user creates a car profile with:

- car name
- tank size
- current odometer reading

This setup is required before fuel entries or trips can be created.

### 6.2 Add Fuel Entry

The user can add a fuel entry with:

- date
- distance input mode
- odometer reading or trip meter reading
- liters filled
- total cost
- price per liter
- full tank flag
- optional note

The app should allow either:

- entering liters and total cost, then calculating price per liter
- entering liters and price per liter, then calculating total cost

The final stored record should contain all required values.

Recommended UX behavior:

- the user can choose whether to enter the current odometer directly
- or enter the trip meter distance driven since the previous reset or refuel
- the app should normalize the result into a stored odometer value for calculations

### 6.3 View Fuel History

The user can view all fuel entries in chronological order and see:

- date
- odometer
- liters
- total cost
- full tank status

The user can open an entry to edit or delete it.

### 6.4 View Fuel Statistics

The app should provide:

- total fuel bought
- total money spent
- average fuel consumption
- cost per kilometer
- average fuel price

The app should clearly separate:

- values calculated from reliable full-tank data
- values that are estimated or based on incomplete data

### 6.5 Track A Trip

The user can create a trip by:

- starting a trip
- driving normally
- adding fuel entries during the trip if needed
- ending the trip

After ending a trip, the app should show:

- trip start and end points as names entered by the user
- start and end odometer
- start and end time
- kilometers driven
- total elapsed time, break time, and driving time
- a simple timeline of trip start, breaks, linked fuel entries, and trip end
- fuel bought during the trip
- total fuel cost during the trip
- average consumption for the trip when calculation is reliable

Fuel entries created during a trip must also appear in the normal fuel history.

## 7. Functional Requirements

### 7.1 Car

- The app must support creating one car in version 1.
- The car must include name, tank size, and current odometer.
- The user must be able to edit car details later.

### 7.2 Fuel Entries

- The app must let the user create, edit, and delete fuel entries.
- Liters must support decimal values.
- Currency in version 1 is EUR.
- Each fuel entry must be linked to the car.
- A fuel entry may optionally be linked to an active trip.
- The app must support entering distance either as direct odometer or as trip meter kilometers.
- The app must store a normalized odometer value for each fuel entry.
- If trip meter mode is used, the app must derive the odometer from previous known data.

### 7.3 Trips

- The app must let the user start a trip.
- Only one trip can be active at a time.
- The user must be able to end the active trip.
- The app must store trip summaries for later viewing.
- The app must support manually recording and correcting trip breaks.
- Trip summaries should show elapsed time, break time, driving time, and average speed when distance and valid time are available.

### 7.4 Calculations

- The app must calculate consumption using odometer and fuel-entry data.
- Full-tank entries must be used as the main source of reliable consumption calculations.
- Partial fills must still be stored and displayed.
- The app should avoid presenting misleading precision when data is incomplete.

### 7.5 Language

- The app must support English and Finnish.
- The user must be able to choose the language.

### 7.6 Design

- The app should use a simple and minimal visual style.
- Dark mode is required.
- The design direction should feel calm and polished, similar in spirit to Google Clock.

## 8. Data To Store

### 8.1 Car

- id
- name
- tank size liters
- current odometer
- created at
- updated at

### 8.2 Fuel Entry

- id
- car id
- trip id nullable
- date/time
- distance input mode
- odometer
- trip meter kilometers nullable
- liters
- total cost
- price per liter
- full tank
- note nullable
- created at
- updated at

### 8.3 Trip

- id
- car id
- name or route label
- start location name
- end location name nullable until trip ends
- start odometer
- end odometer nullable until trip ends
- start time
- end time nullable until trip ends
- status active/completed
- created at
- updated at

### 8.4 Trip Break

- id
- trip id
- start time
- end time nullable while the break is active
- note nullable
- created at
- updated at

## 9. Success Criteria For MVP

Version 1 is successful if the user can:

- set up one car
- log fuel entries reliably
- review history and basic stats
- start and end trips
- understand approximate and reliable fuel-cost information without confusion

## 10. Open Questions

- Should trip summaries use only fuel purchases made during the trip, or also estimate fuel already in the tank at trip start?
- Should the user manually name a trip, or should start and end locations be enough?
- Should the home screen focus more on fuel history or current stats?
