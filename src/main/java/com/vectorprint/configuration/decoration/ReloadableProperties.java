package com.vectorprint.configuration.decoration;

import com.vectorprint.configuration.EnhancedMap;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Watches files (only those via constructor arguments) for changes and reloads settings in those files.
 */
public class ReloadableProperties extends ParsingProperties implements HiddenBy {

    public static final int POLL_INTERVAL = 10000;

    public ReloadableProperties(EnhancedMap properties, int interval, File... files) throws IOException {
        super(properties, files);
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
        Map<File,List<File>> toObserve = new HashMap<>();
        for (File f : files) {
            File dir = f.getParentFile();
            if (!toObserve.containsKey(dir)) {
                toObserve.put(dir,new ArrayList<>());
            }
            toObserve.get(dir).add(f);
        }
        for (Map.Entry<File,List<File>> entry: toObserve.entrySet()) {
            FileFilter ff = file -> entry.getValue().contains(file);
            FileAlterationObserver observer = new FileAlterationObserver(entry.getKey(),ff);
            FileAlterationListener listener = new FileAlterationListenerAdaptor() {
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
                        log.error("error reloading: " + file, e);
                    }
                }
            };
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
        this(properties, getFiles(files));
    }

    public ReloadableProperties(EnhancedMap properties, int interval, String... files) throws IOException {
        this(properties, interval, getFiles(files));
    }

    private static File[] getFiles(String[] files) {
        return Arrays.stream(files).map(File::new).toList().toArray(new File[files.length]);
    }

    public ReloadableProperties(EnhancedMap properties, File... files) throws IOException {
        this(properties, POLL_INTERVAL,files);
    }

    /**
     * This method does not clear existing settings, it just parses the changed property file.
     * Registered {@link java.beans.PropertyChangeListener Observers} will be notified of changes.
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
