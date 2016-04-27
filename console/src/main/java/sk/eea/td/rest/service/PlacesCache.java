package sk.eea.td.rest.service;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.dto.PlacesResponseDTO;
import sk.eea.td.util.GeoUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Repository
public class PlacesCache {

    private static final Logger LOG = LoggerFactory.getLogger(PlacesCache.class);

    @Value("${places.cache.expire.time}")
    private Long expireAfterWrite;

    @Value("${places.cache.max.size}")
    private Long maxSize;

    @Value("${places.cache.cleanup.interval}")
    private Long cleanupInterval;

    @Autowired
    private HPClient hpClient;

    private LoadingCache<String, Optional<Location>> placesCache;

    private ScheduledExecutorService cleanUpService;

    @PostConstruct
    public void init() {
        placesCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build(new CacheLoader<String, Optional<Location>>() {
            @Override
            public Optional<Location> load(String s) throws Exception {
                final PlacesResponseDTO places = hpClient.getPlaces(s);
                final Location ne = places.getBounds().getNe();
                final Location sw = places.getBounds().getSw();

                final Long radius = GeoUtils.calculateDistance(
                        new GeoUtils.Location(ne.getLat(), ne.getLng()),
                        new GeoUtils.Location(sw.getLat(), sw.getLng())
                ) / 2;

                final GeoUtils.Location midpoint = GeoUtils.calculateMidpoint(
                        new GeoUtils.Location(ne.getLat(), ne.getLng()),
                        new GeoUtils.Location(sw.getLat(), sw.getLng())
                );

                return Optional.of(new Location(midpoint.getLat(), midpoint.getLng(), radius));
            }
        });

        // cache does not remove expired value automatically, we need to perform cleanup procedure periodically
        cleanUpService = Executors.newScheduledThreadPool(1);
        cleanUpService.scheduleAtFixedRate((Runnable) () -> {
            LOG.info("Performing scheduled cache cleanup.");
            placesCache.cleanUp();
        },cleanupInterval,cleanupInterval, TimeUnit.MINUTES);
    }

    /**
     * Gets location using cache.
     *
     * Cache is loaded from HP Places API. If there was problem during location retrieval, cache will return empty (not null !!!) location for period of time set by property "places.cache.expire.time" in properties file.
     *
     * @param countrySlug
     * @return location from given country slug, or will return empty (not null!) location if exeption occured during value retrieval.
     */
    public Location getLocation(String countrySlug) {
        try {
            return placesCache.get(countrySlug).get();
        } catch (Exception e) {
            LOG.error("Exception at determining pin location through Historypin Places API.", e);
            // put empty location into cache
            Location emptyLocation = new Location();
            placesCache.put(countrySlug, Optional.of(emptyLocation));
            return emptyLocation;
        }
    }

    @PreDestroy
    public void destroy() {
        if (cleanUpService != null) {
            cleanUpService.shutdown();
            try {
                // Wait a while for existing tasks to terminate
                if (!cleanUpService.awaitTermination(3, TimeUnit.SECONDS)) {
                    cleanUpService.shutdownNow(); // Cancel currently executing tasks
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                cleanUpService.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }
}
