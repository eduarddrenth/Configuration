[![Maven Central](https://img.shields.io/maven-central/v/com.vectorprint/Config.svg)](https://maven-badges.herokuapp.com/maven-central/com.vectorprint/Config)

# This project is about configuration of applications, most notably via CDI injection, and about parameterization of Objects.

### Changes december 2024: removed xml based setup, removed Date support in favor of LocalDateTime and improved docs and logging

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
**NOTE** use `-parameters` when compiling or use the `keys` parameter in `@Property`
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

/** a bean with properties */
@ApplicationScoped
public class MyBean {
    
    /** A non required property that will not be reloaded */
    @Inject @Property(defaultValue = "30",required = false,updatable = false)
    private int timeoutSeconds;

    /** workaround: using a static field makes reloading work */
    private static int[] test;
    /** A required property that changes when the corresponding value in the properties file changes */
    @Inject
    public void setTest(@Property(required=true, keys="testkey") int[] test) {
        MyBean.test=test;
    }
    
}

```

**NOTE** at the time reloading works only for Singleton beans, static properties and for `@inject @Properties EnhancedMap`

### Plain Java example with an Observer
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
The library offers syntax support for settings and parameters in a loosely coupled manner. An enhanced java properties syntax and json are built-in. You are not restricted to the built-in syntaxes, you can provide your own, using `javacc` and `spi`, see `SettinsBindingService.getFactory()` and the junit tests.

At runtime this library tracks keys for which a default is used when no value is found. It tracks
unused keys as well.

You can declare multiple features for settings such as caching, preparing keys and values, auto reloading, readonlyness, threadsafety, help for properties, reading / parsing from input. You can also develop your own features for settings.

