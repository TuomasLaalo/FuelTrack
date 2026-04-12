Fuel Tracker App Planning
1. App Goal

What is the main goal of the app?
The main goal of the app is to track a car’s fuel consumption, fuel costs, and trip costs over time.

What problem should it solve for you?
The app should help me understand:

how much fuel my car uses

how much driving costs me

how much specific longer trips cost

I want to be able to log normal fuel entries and also create trips.
For example:

start a trip in Helsinki with a full tank

refill once during the trip

end the trip in Rovaniemi

After ending the trip, the app should show:

kilometers driven

fuel used

average consumption

total cost

This same fuel data should also be included in the normal fuel history view.

2. App Scope

Is this app for one car or multiple cars?
Version 1 is for one car only.
Later, support for multiple cars can be added.

Is it only for your own use, or for other users too?
Version 1 is for one user only.
Support for multiple users can be added later.

3. Fuel Entry Data

Each fuel entry should contain:

Date

Odometer reading

Liters filled

Total cost

Price per liter

Full tank: yes / no

Note

This is a good starting point for version 1.

4. Units

Version 1 uses kilometers and liters

Liters must support decimals, for example 33.3

Currency is mainly EUR

Sometimes fuel may be bought in Sweden using SEK, but version 1 can use only euros

Support for changing units later (miles / gallons / other currencies) can be added to the backlog

5. Core Features For Version 1

Must-have features in version 1:

Create car

Add fuel entry

View fuel history

Edit entry

Delete entry

Average fuel consumption

Cost per kilometer

Trip tracking

Trip summary

Car setup in version 1

When creating a car, the user should enter:

car name

tank size

current odometer reading

This is important because the app should understand the fuel situation in the tank and use previous entries when calculating consumption.

Important calculation logic

The app should support this kind of situation:

Car tank size = 40 liters

First fill: add 21 liters and the pump stops automatically, so tank is considered full

Next fill: add 15 liters, but user stops filling manually, so tank is not full

The app should use tank state and previous entries to calculate fuel usage as accurately as possible.
Logic is calulated by the entries and the known values

6. Nice-To-Have Features Later

Possible backlog features:

Multiple cars

Multiple users

Charts

Export to CSV

Backup / restore

Cloud sync

Receipt photo

Receipt scanning

Reminders

Multiple currencies

Unit selection (km/liters or miles/gallons)

7. Storage

Version 1 should work locally on the phone only.
Cloud sync can be added later.

8. Design Preferences

Simple and minimal design

Dark mode

Visually pleasing

Similar feeling to the Google Clock app

9. Language

The app should support:

English

Finnish

The user should be able to choose the language.