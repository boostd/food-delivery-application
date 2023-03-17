# Food Delivery App

This is a backend application for calculating the delivery fee for food delivery in different cities.
The fee depends on the selected vehicle and the weather conditions in the city at the time of delivery.

## Technologies Used
- Java 17
- Spring Boot
- H2 Database
- Gradle

## Requirements
- Java 17 or later installed
- Gradle installed

## Installation
1. Clone the repository
2. Open a terminal in the root directory of the project
3. Run `gradle build` to build the application
4. Run `java -jar build/libs/food-delivery-app-1.0.jar` to start the application

## Endpoints
The application exposes a single endpoint:

### POST /delivery/fee

Calculates the delivery fee based on the provided `FeeRequest` object, which contains the following fields:

- `city` - (case-insensitive) name of the city to which the delivery is being made
  - accepted values - Tallinn, Tartu, Pärnu
- `vehicleType` - (case-insensitive) type of vehicle being used for the delivery
  - accepted values - car, scooter, bike
- `timeStamp` - (optional) LocalDateTime of the time at which the delivery is being made
  - if unvalued, the latest data is used

Request example:
```json
{
    "city": "Tallinn",
    "vehicleType": "car",
    "timeStamp": "2023-03-17T14:30:00"
}
```

The endpoint returns a `FeeResponse` object, which contains the following fields:

- `fee` - the calculated delivery fee
- `errorMessage` - an error message in case an error occurs during the calculation

Response example:
```json
{
    "fee": 3.5,
    "errorMessage": null
}
```

## Error Handling

The endpoint can return the following error responses:

- `400 Bad Request` - in case of an invalid input
  - `Unknown city: {city}` - if the requested city is not found
  - `Unknown vehicle type: {vehicleType}` - if the requested vehicle type is not found
  - `Usage of selected vehicle type is forbidden` - if the selected vehicle type is not allowed for the requested weather conditions
- `404 Not Found` - in case no valid weather data was found for the requested city or timestamp
  - `No valid weather data for selected time for city: {city}` - if the requested time does not have valid weather data for requested city
  - `Database contains no weather data for city: {city}` - if there is no weather data available for the requested city
- `500 Internal Server Error` - in case the server encounters an unexpected error while processing the request
  - `An unexpected error occurred` - if there is an unexpected error while processing the request



## Architecture
The application follows the Model-View-Controller (MVC) architecture pattern, with the following components:

- Model: `WeatherData` - represents a weather observation made at a weather station
- View: `FeeRequest` and `FeeResponse` - represent the input and output of the delivery fee calculation endpoint, respectively
- Controller: `DeliveryFeeController` - handles requests to the delivery fee calculation endpoint and communicates with the service layer
- Service: `DeliveryFeeCalculator` and `WeatherDataService` - perform the business logic of the application, such as calculating the delivery fee and fetching weather data from the database, respectively
- Database: H2 database - stores the weather data used in the calculation of the delivery fee

## Contributors
This project was developed by Jürgen Tihanov as a part of the application for a Fujitsu software developer internship.
