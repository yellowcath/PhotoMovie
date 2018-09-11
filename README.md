### Gradle
``` groovy 
    allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```

``` groovy 
	dependencies {
	        implementation 'com.github.yellowcath:PhotoMovie:1.4.6'
	}
```
![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/filter.gif)
![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/transfer.gif)