[![Maven Central](https://img.shields.io/maven-central/v/com.vectorprint/Config.svg)](https://maven-badges.herokuapp.com/maven-central/com.vectorprint/Config)

# This project is about configuration of applications and about parameterization of Objects, most notably via CDI injection.

### Changes december 2024: removed xml based setup, removed Date support in favor of LocalDateTime!!

## code example
```xml
        <dependencies>
            <dependency>
                <!--only when working with CDI-->
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
### CDI, the preferred way
**NOTE** use `-parameters` when compiling or use the keys parameter in @Property
```java
import jakarta.enterprise.context.ApplicationScoped;

/** Configure property (re)loading */
@ApplicationScoped
public class PropertyLocationProvider {

    @Produces @ConfigFileUrls
    public String[] getUrl() {return new String[]{"/etc/stdw.properties"};}

    /** when true the url must be in the form "classpath:/pathto/my.properties" */
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
    
    /** A non required property that will not be reloaded */
    @Inject @Property(defaultValue = "30",required = false,updatable = false)
    private int timeoutSeconds;
    
    /** A required property that changes when the corresponding value in the properties file changes */
    private static int[] test;
    @Inject
    private void setTest(@Property(required=true, keys="testkey") int[] test) {
        MyBean.test=test;
    }
    
}

```

**NOTE** at the time reloading works only for Singleton beans, static properties and for `@inject @Properties EnhancedMap`

### Plain Java example
```java
   static class MyObserver implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {}
    }
    
    public class Example {

        private ObservableProperties mtp =
                new ObservableProperties(new ParsingProperties(new Settings(), "/etc/app.properties"));
        private MyObserver os = new MyObserver();
        private boolean prop;
        
        public Example() {
            mtp.addObserver(os);
            prop=mtp.getBooleanProperty(null,"prop");
        }
    }

```

### Java annotations example

```java
public class Fields {
    @Setting
    private boolean b;
    @SettingsField(observable = true, readonly = true, cache = true,....)
    private EnhancedMap settings;
}

public class Fields2 {
    @Setting
    private static boolean b;
}

public class Example {
    private final SettingsAnnotationProcessor sap = new SettingsAnnotationProcessorImpl();
    private final EnhancedMap eh = new ParsingProperties(new Settings(), "/etc/app.properties");
    private Fields f = new Fields();

    public Example() {
        sap.initSettings(f,eh);
        sap.initSettings(Fields2.class,eh);
    }

}

```
The library offers syntax support for settings and parameters in a loosely coupled manner. You are not restricted to the built-in syntaxes, you can provide your own.

At runtime this library tracks keys for which a default is used when no value is found. It tracks
unused keys as well.

You can stack features for settings such as caching, preparing keys and values, auto reloading, readonlyness, threadsafety, help for properties, reading / parsing from input. You can easily develop
your own features for settings.
