This project is about configuration of applications and about parameterization of Objects in a declarative way.

This library offers annotations (and annotation processors), parsers, observing changes, serialization,
cloning and more when working with settings and/or object parameters. Settings and its features can as well be declared using an xml format.

The library offers syntax support (both parsing and serialization) for settings and parameters in a loosely coupled manner. You are not restricted to the built in syntaxes, you can provide your own.

At runtime this library tracks keys for which a default is used because they are not found in settings. Also it tracks
unused keys.

You can stack features for settings such as caching, preparing keys and values, readonlyness, threadsafety, helpsupport, reading / parsing from input. You can easily develop your own features.
