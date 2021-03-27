package com.vectorprint.configuration.decoration;

import com.vectorprint.configuration.EnhancedMap;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Watches files (only those via constructor arguments) for changes and reloads settings in those files.
 */
public class ReloadableProperties extends ParsingProperties implements HiddenBy {

    private final transient FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
    private final transient FileAlterationListener listener = new FileAlterationListenerAdaptor() {
        @Override
        public void onFileCreate(File file) {
        }

        @Override
        public void onFileDelete(File file) {
        }

        @Override
        public void onFileChange(File file) {
            try {
                reload(file.toPath());
            } catch (IOException e) {
                log.error("error reloading: " + file,e);
            }
        }
    };

    public ReloadableProperties(EnhancedMap properties, File... files) throws IOException {
        super(properties, files);
        for (File f : files) {
            FileAlterationObserver observer = new FileAlterationObserver(f.getParentFile());
            observer.addListener(listener);
            monitor.addObserver(observer);
        }
        try {
            monitor.start();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public ReloadableProperties(EnhancedMap properties, String... files) throws IOException {
        this(properties, Arrays.stream(files).map(u -> new File(u)).collect(Collectors.toList()).toArray(new File[files.length]));
    }

    /**
     * This method does not clear existing settings, it just parses the changed property file.
     * Registered {@link Observer Observers} will be notified of changes.
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
