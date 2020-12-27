[![Maven Central](https://img.shields.io/maven-central/v/com.vectorprint/Config.svg)](https://maven-badges.herokuapp.com/maven-central/com.vectorprint/Config)

This project is about configuration of applications and about parameterization of Objects.

**New: CDI properties!!**

Use ```@Inject``` and ```@Property(default=..., keys=...)``` to Inject properties from urls (files).
When configured, changes in property files will immediately cause injected field and method parameters to change accordingly, **NOTE** at the time this works only for Singleton beans. Updating always works for static properties and for ```@inject @Properties EnhancedMap```

This library offers annotations (and annotation processors), parsers, typing, observing changes, serialization,
cloning and more when working with settings and/or object parameters. Settings and its features can also be declared using (external) xml, also in combination with annotations, in that case xml prevails.

The library offers syntax support for settings and parameters in a loosely coupled manner. You are not restricted to the built in syntaxes, you
can provide your own.

At runtime this library tracks keys for which a default is used because they are not found in settings. Also it tracks
unused keys.

You can stack features for settings such as caching, preparing keys and values, autoreloading, readonlyness, threadsafety, helpsupport, reading / parsing from input. You can easily develop
your own features for settings.
