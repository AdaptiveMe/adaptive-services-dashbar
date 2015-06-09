
/*
 * Copyright 2014-2015. Adaptive.me.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ge me.adaptive.che.infrastructure.dao;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.factory.FactoryImage;
import org.eclipse.che.api.factory.FactoryStore;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class InMemoryFactoryStore implements FactoryStore {
    private final Map<String, Set<FactoryImage>> images    = new HashMap<>();
    private final Map<String, Factory> factories = new HashMap<>();
    private final ReentrantReadWriteLock lock      = new ReentrantReadWriteLock();

    @Override
    public String saveFactory(Factory factoryUrl, Set<FactoryImage> images) throws ApiException {
        lock.writeLock().lock();
        try {
            final Factory newFactoryUrl = DtoFactory.getInstance().clone(factoryUrl);
            newFactoryUrl.setId(NameGenerator.generate("", 16));
            final Set<FactoryImage> newImages = new LinkedHashSet<>(images.size());
            for (FactoryImage image : images) {
                newImages.add(new FactoryImage(Arrays.copyOf(image.getImageData(), image.getImageData().length), image.getMediaType(),
                                               image.getName()));
            }

            factories.put(newFactoryUrl.getId(), newFactoryUrl);
            this.images.put(newFactoryUrl.getId(), newImages);

            return newFactoryUrl.getId();
        } catch (IOException e) {
            throw new ConflictException(e.getLocalizedMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeFactory(String id) throws ApiException {
        lock.writeLock().lock();
        try {
            factories.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Factory getFactory(String id) {
        lock.readLock().lock();
        try {
            return factories.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Factory> findByAttribute(Pair<String, String>... attributes) throws ApiException {
        final List<Factory> result = new LinkedList<>();
        lock.readLock().lock();
        try {
            for (Pair<String, String> attribute : attributes) {
                final String name = attribute.first;
                final String value = attribute.second;
                if (name == null || value == null) {
                    continue;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    @Override
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws ApiException {
        lock.readLock().lock();
        try {
            if (imageId == null) {
                return images.get(factoryId);
            }
            for (FactoryImage image : images.get(factoryId)) {
                if (image.getName().equals(imageId)) {
                    FactoryImage imageCopy;
                    try {
                        imageCopy = new FactoryImage(Arrays.copyOf(image.getImageData(), image.getImageData().length), image.getMediaType(),
                                                     image.getName());
                    } catch (IOException e) {
                        // Seems that error may happen just if media type isn't supported but since we just create copy of existed FactoryImage
                        // such error isn't excepted here.
                        throw new ConflictException(e.getMessage());
                    }
                    return Collections.singleton(imageCopy);
                }
            }
            return Collections.emptySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
   /**
     * Update factory at storage.
     *
     * @param factoryId - factory information
     * @param factory   - factory information
     * @return - if of stored factory
     * @throws org.eclipse.che.api.core.ApiException
     */verride
    public String updateFactory(String factoryId, Factory factory) throws ApiException {
        lock.writeLock().lock();
        try {
            final Factory clonedFactory = DtoFactory.getInstance().clone(factory);
            factories.put(factoryId, clonedFactory);
            return clonedFactory.getId();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
