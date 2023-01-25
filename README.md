# ONEstore Application License Checker Samples
Sample applications for Application License Checker.

## Application License Checker SDK
### How to download
Now(ALC API v2), You can download Application License Checker SDK using the maven system.

Add the maven url to root gradle.

```
repositories {
    ...
    maven { url 'https://repo.onestore.co.kr/repository/onestore-sdk-public' }
}
```

And, add the dependency to your project gradle.

```
dependencies {
    def onestore_alc_version = "2.0.0"
    def onestore_configuration_version = "1.0.0"
    def onestore_configuration_region = "sdk-configuration-kr"
    
    implementation "com.onestorecorp.sdk:sdk-licensing:$onestore_alc_version"
    implementation "com.onestorecorp.sdk:$onestore_configuration_region:$onestore_configuration_version"
}
```

Next, enter your license key to your project gradle. The method below is used only in the test app.

***Cause***<br/>
For license keys, it is recommended to use it after receiving it through a server rather than saving it as an in-app code to ensure security.

```
android {
  ...
  defaultConfig {
      buildConfigField "String", "PUBLIC_KEY", "\"INSERT YOUR LICENSE KEY\""
  }
  ...
}
```

If you want to download older SDK(v1), click [This Link](https://github.com/ONE-store/app_license_checker/releases/tag/release%2Falc-1.0.0)

### Changed the function of the Application License Checker v2 SDK
* Changed instance of AppLicenseChecker calling method 
    * In version 1, the instance was created directly.
    * In version 2, call get() static function for instance of AppLicenseChecker 
* Deprecated the error code class
    * Deprecated Enumeration.HandleError and Enumeration.HandleException and made new class for AppLicenseChecker.ResponseCode.
* Changed the deployment flow of json file(global-appstore.json)
    * Can download json file for korea region using the maven.

## Change Note

* 2022-09-29
    * Uploaded samples for ONEstore Application License Checker Library v2. 


# License
```
Copyright 2023 One store Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, 
software distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and
limitations under the License.
```	
