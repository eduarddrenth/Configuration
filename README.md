[![Maven Central](https://img.shields.io/maven-central/v/com.vectorprint/Config.svg)](https://maven-badges.herokuapp.com/maven-central/com.vectorprint/Config)

# This project is about configuration of applications and about parameterization of Objects, most notably via CDI injection.

### Changes december 2024: removed xml based setup, removed Date support in favor of LocalDateTime!!

## code example
```xml
        <dependencies>
            <dependency>
                <groupId>jakarta.platform</groupId>
                <artifactId>jakarta.jakartaee-api</artifactId>
                <version>10.0.0</version>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>com.vectorprint</groupId>
                <artifactId>Config</artifactId>
                <version>13.0</version>
            </dependency>
        </dependencies>
```

```java
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configure property (re)loading
 */
@ApplicationScoped
public class PropertyLocationProvider {

    @Produces @ConfigFileUrls
    public String[] getUrl() {return new String[]{"/etc/stdw.properties"};}

    /**
     * when true the url must be in the form "classpath:/pathto/my.properties"
     * @return
     */
    @Produces @FromJar
    public boolean fromJar() {return false;}

    @Produces @AutoReload
    public boolean autoRload() {return true;}

    @Produces @POLL_INTERVAL
    public int pollInterval() {return 10;}
}

/**
 * a bean with properties
 */
@ApplicationScoped
public class MyBean {
    /**
     * A non required property that will not ber reloaded
     */
    @Inject @Property(defaultValue = "30",required = false,updatable = false)
    private int timeoutSeconds;
    /**
     * A required property that changes when the corresponding value in the properties file changes
     */
    @Inject @Property(required="true", keys="testkey")
    private static int[] test;
    
}

```

Use ```@Inject``` and ```@Property(default=..., keys=...)``` to Inject properties from urls (files).
When configured, changes in property files will immediately cause injected fields and method parameters to change accordingly, **NOTE** at the time this works only for Singleton beans, static properties and for ```@inject @Properties EnhancedMap```



This library offers annotations (and annotation processors), parsers, typing, observing changes, serialization,
cloning and more when working with settings and/or object parameters. Settings and its features can be declared using annotations.

The library offers syntax support for settings and parameters in a loosely coupled manner. You are not restricted to the built in syntaxes, you
can provide your own.

At runtime this library tracks keys for which a default is used because they are not found in settings. Also it tracks
unused keys.

You can stack features for settings such as caching, preparing keys and values, autoreloading, readonlyness, threadsafety, helpsupport, reading / parsing from input. You can easily develop
your own features for settings.
