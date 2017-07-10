# Android-Multi-Bluetooth-Library

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Multi%20Bluetooth%20Library-green.svg?style=flat)](https://android-arsenal.com/details/1/1954)

This library allows you to easily create a socket bluetooth connection for multiple android devices with one server and 7 clients max. This library is compatible with the Android SDK 2.3 to 8.0 

[![Youtube video](http://img.youtube.com/vi/svzu2qd_fOo/0.jpg)](http://www.youtube.com/watch?v=svzu2qd_fOo)

For documentation and additional information see [the website][1].


Download
--------
Download __[the latest JAR][2]__  or grab via Maven:
```xml
  <dependencies>
    <dependency>
      <groupId>com.ramimartin.multibluetooth</groupId>
      <artifactId>AndroidMultiBluetoothLibrary</artifactId>
      <version>2.0.4-SNAPSHOT</version>
    </dependency>
  </dependencies>

  <repositories>
    <repository>
      <id>drgames</id>
      <name>Drgames Ftp</name>
      <url>http://drgames.fr/maven2</url>
    </repository>
  </repositories>
```
or Gradle:
```groovy
repositories {
    maven {
        url "http://drgames.fr/maven2/"
    }
    mavenCentral()
}

dependencies {
    compile 'com.ramimartin.multibluetooth:AndroidMultiBluetoothLibrary:2.0.4-SNAPSHOT'
}

```

### This library is used in these applications :
[![googleplay](https://github.com/arissa34/Android-Multi-Bluetooth-Library/blob/gh-pages/images/domino.png?raw=true)](https://play.google.com/store/apps/details?id=com.drgames.domino)
[![googleplay](https://github.com/arissa34/Android-Multi-Bluetooth-Library/blob/gh-pages/images/P4.png?raw=true)](https://play.google.com/store/apps/details?id=com.drgames.puissance4)

If you like this library please download and give me 5 stars ;)

License
-------
    
    /*
    * ----------------------------------------------------------------------------
    * "THE BEER-WARE LICENSE" (Revision 42):
    * Rami Martin wrote this file.  As long as you retain this notice you
    * can do whatever you want with this stuff. If we meet some day, and you think
    * this stuff is worth it, you can buy me a beer in return.   Poul-Henning Kamp
    * ----------------------------------------------------------------------------
    */
    
[1]: http://arissa34.github.io/Android-Multi-Bluetooth-Library/
[2]: https://github.com/arissa34/Android-Multi-Bluetooth-Library/raw/master/Bluetooth/Bluetooth_lib/target/AndroidMultiBluetoothLibrary-2.0.4-SNAPSHOT.jar
