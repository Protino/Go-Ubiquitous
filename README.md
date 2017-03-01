[![Codacy Badge](https://api.codacy.com/project/badge/Grade/def8b5a8b1dd416ea8e4878b8af239b5)](https://www.codacy.com/app/gurupadmamadapur/Go-Ubiquitous?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Protino/Go-Ubiquitous&amp;utm_campaign=Badge_Grade)

Go-Ubiquitous
=============
* [About](#about)
* [Build Configuration](#build-configuration)
* [Screenshots](#screenshots)
* [Checklist](#checklist)
* [License](#license)

### About
This project is part of the [Udacity Android Developer Nanodegree].


```
Project Overview
 In this project, you will build a wearable watch face for Sunshine to run on an Android Wear device.

Why this Project?
 Android Wear is an exciting way to integrate your app more directly into users’ lives. As a new developer, it will be important for you to understand how to perform this integration. This project gives you an opportunity to design a companion app for Sunshine, tying it to a watch face in order to enrich the experience.

What Will I Learn?
 Through this project, you will:

 • Understand the fundamentals of Android Wear.
 • Design for multiple watch form factors.
 • Communicate between a mobile device and a wearable device.
```


### Build Configuration

 1. Clone the repository.
 2. Add google-services.json created by firebase console. Make sure the package is correctly set.
 3. Create gradle.properties in the root folder.
 4. Initialize the following variables in the file.

    ```
    MyOpenWeatherMapApiKey = "[YOUR_API_KEY_HERE]"
    MyGeoApiKey = "[YOUR_API_KEY_HERE]"
    ```

    You can get an API_KEY from <a href="http://openweathermap.org" target="_blank">Open Weather Map</a>

 To test the wearable app you can use refresh menu item in the main activity, which sends data item to the wearable.
 By default, it phone sends data every 3 hours and when there is a change in the weather data.


### Screenshots

<p align="center">
<img src="https://drive.google.com/uc?id=0B7HoD_UwfapHcG13LTNHaE9fZ2c" width="821" height="540" alt="Cover">
</p>

<p align="center">
<img src="https://drive.google.com/uc?id=0B7HoD_UwfapHWFEtbml3MTNwY1E" alt="App Gif">
</p>

### Checklist

 - [x] Refactor the initial code.
 - [x] Create mocks for watchFace.
 - [x] Code UI reflecting mocks.
 - [x] Display dummy data.
 - [x] Fetch real data from phone and display it.
 - [ ] Add configuration to change background. (`Partially complete`).
 - [x] Make battery saving optimizations.
 - [x] Test for rubric specifications.
 - [x] Implement listener on phone and make wearable request data on startup.
 - [ ] Add simple animations.

### LICENSE

```
Copyright 2016 Gurupad Mamadapur

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 ```


  [Udacity Android Developer Nanodegree]:https://www.udacity.com/degrees/android-developer-nanodegree-by-google--nd801

