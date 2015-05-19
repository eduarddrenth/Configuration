This project is about configuration of applications and about parameterization of Objects.

This library offers annotations and processors, parsers, typing, observing changes, serialization, cloning and more when working with
settings and/or object parameters

An easy to use syntax allows you to specify settings and Object parameters in for example a file:

objects=Object1(num=1,date=01/12/2015,color=red,url=http://bla);Object2;Object3(array=1|2|3,colarray=red|green|#2200aa)

At runtime this library keeps track of keys that are not found in settings, for which a default is used. Also it tracks
keys that are present in settings but are not used.

Keys and values can be prepared before adding them to settings.

You can add features to settings such as readonlyness, threadsafety, helpsupport, caching, parsing, multiplicity. The library
uses the decoration pattern and has classes that wrap other settings classes.
