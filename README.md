This project is about configuration of applications and about parameterization of Objects.

This library offers annotations and processors, parsers, typing, observing changes, serialization, cloning and more when working with settings and/or object parameters

The library offers syntax support for settings and parameters. You are not restricted to the built in syntaxes, you
can provide your own.

At runtime this library keeps track of keys that are not found in settings, for which a default is used. Also it tracks
keys that are present in settings but are not used.

Keys and values can be prepared before adding them to settings.

You can add features to settings such as readonlyness, threadsafety, helpsupport, caching, parsing, multiplicity. The library
uses the decoration pattern and has classes that wrap other settings classes.
