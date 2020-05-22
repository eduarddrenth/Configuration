package com.vectorprint.configuration.decoration;

import com.vectorprint.configuration.EnhancedMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Watches files (only those via constructor arguments) for changes reloads settings.
 */
public class ReloadableProperties extends ParsingProperties implements HiddenBy {

    private final transient WatchService watcher = FileSystems.getDefault().newWatchService();
    private final transient ExecutorService runner = Executors.newSingleThreadExecutor();

    public ReloadableProperties(EnhancedMap properties, File... files) throws IOException {
        super(properties, files);
        for (File f : files) {
            f.getParentFile().toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        }
        /*
        process events...
        watcher.take
        event.context
        reload props
        watchkey.reset
         */
        runner.submit(() -> {
            for (;;) {
                WatchKey watchKey = watcher.take();
                watchKey.pollEvents().forEach(pe -> {
                    WatchEvent<Path> we = (WatchEvent<Path>) pe;
                    Path file = we.context();
                    Path dir = (Path) watchKey.watchable();
                    try {
                        if (dir.resolve(file).toFile().exists()) {
                            reload(dir.resolve(file));
                        } else {
                            log.error("deleted? " + file);
                        }
                    } catch (IOException e) {
                        log.error("error reloading: " + file,e);
                    }
                    if (!watchKey.reset()) {
                        try {
                            file.register(watcher,StandardWatchEventKinds.ENTRY_MODIFY);
                        } catch (IOException e) {
                            log.error("unable to watch: " + file, e);
                        }
                    }
                });
            }
        });
    }

    public ReloadableProperties(EnhancedMap properties, String... files) throws IOException {
        this(properties, (File[]) Arrays.stream(files).map(u -> new File(u)).collect(Collectors.toList()).toArray());
    }

    /**
     * This method does not clear existing settings, it just parses the changed property file
     * @param file
     * @throws IOException
     */
    protected void reload(Path file) throws IOException {
        loadFromReader(new FileReader(file.toFile()));
    }
    @Override
    public boolean hiddenBy(Class<? extends AbstractPropertiesDecorator> settings) {
        return ObservableProperties.class.isAssignableFrom(settings);
    }
}
