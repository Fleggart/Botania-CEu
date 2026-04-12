package vazkii.botania.common.core;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import vazkii.botania.common.Botania;

public class MetadataFetcher {
    Thread fetcher;
    volatile Properties loadedProperties = null;
    volatile Properties contributors = null;
    final List<Consumer<MetadataFetcher>> callbacks = new ArrayList<>();

    public static MetadataFetcher INSTANCE = new MetadataFetcher();

    public MetadataFetcher() {
        this.fetcher = new Thread(new Runnable() {

            @Override
            public void run() {
                loadThings();
            }
            
        });
        this.fetcher.setName("Botania Metadata Fetcher");
        this.fetcher.setDaemon(true);
    }

    public synchronized void subscribe(Consumer<MetadataFetcher> callback) {
        System.out.println("New subscriber: " + callback);
        if (loadedProperties != null) {
            System.out.println("Starting immediately");
            callback.accept(this);
        } else {
            callbacks.add(callback);
        }
    }

    public void run() {
        this.fetcher.start();
    }

    public @Nullable String version() {
        Objects.requireNonNull(loadedProperties);
        return loadedProperties.getProperty("mod_version");
    }

    public Properties contributors() {
        Objects.requireNonNull(contributors);
        return contributors;
    }

    private void loadThings() {
        Botania.LOGGER.info("Loading various metadata things...");
		Properties prop = new Properties();
		Properties vanity = new Properties();
		try {
			URL url = new URL("https://raw.githubusercontent.com/TeamDimensional/Botania-CEu/1.12/gradle.properties");
			try (InputStreamReader is = new InputStreamReader(url.openStream())) {
				prop.load(is);
			}
            loadedProperties = prop;

			url = new URL("https://raw.githubusercontent.com/TeamDimensional/Botania-CEu/1.12/contributors.properties");
			try (InputStreamReader is = new InputStreamReader(url.openStream())) {
				vanity.load(is);
			}
            contributors = vanity;

            Botania.LOGGER.info("Loaded the network metadata");

            synchronized (this) {
                for (Consumer<MetadataFetcher> cb : callbacks) {
                    System.out.println("Starting callback " + cb);
                    cb.accept(this);
                }
                callbacks.clear();
            }
		} catch(Exception e) {
            Botania.LOGGER.warn("Unable to load network metadata!");
			e.printStackTrace();
		}
    }

}
