/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.piasy.biv.loader.fresco;

import android.content.Context;
import android.net.Uri;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.metadata.ImageInfoExtractor;
import com.hy.picker.utils.CommonUtils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Piasy{github.com/Piasy} on 08/11/2016.
 */

public final class FrescoImageLoader implements ImageLoader {

    private final Context mAppContext;
    private final DefaultExecutorSupplier mExecutorSupplier;

    private final ConcurrentHashMap<Integer, DataSource> mRequestSourceMap
            = new ConcurrentHashMap<>();

    private FrescoImageLoader(Context appContext) {
        mAppContext = appContext;
        mExecutorSupplier = new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors());
    }

    public static FrescoImageLoader with(Context appContext) {
        return with(appContext, null);
    }

    public static FrescoImageLoader with(Context appContext,
            ImagePipelineConfig imagePipelineConfig) {
        Fresco.initialize(appContext, imagePipelineConfig);
        return new FrescoImageLoader(appContext);
    }

//    public static FrescoImageLoader with(Context appContext,
//            ImagePipelineConfig imagePipelineConfig, DraweeConfig draweeConfig) {
//        Fresco.initialize(appContext, imagePipelineConfig, draweeConfig);
//        return new FrescoImageLoader(appContext);
//    }

    @Override
    public void loadImage(int requestId, Uri uri, final Callback callback) {
        ImageRequest request = ImageRequest.fromUri(uri);

        final File localCache = getCacheFile(request);
        if (localCache.exists()) {
            mExecutorSupplier.forLocalStorageRead().execute(new Runnable() {
                @Override
                public void run() {
                    callback.onCacheHit(ImageInfoExtractor.getImageType(localCache), localCache);
                    callback.onSuccess(localCache);
                }
            });
        } else {
            callback.onStart(); // ensure `onStart` is called before `onProgress` and `onFinish`
            callback.onProgress(0); // show 0 progress immediately

            ImagePipeline pipeline = Fresco.getImagePipeline();

            DataSource<CloseableReference<PooledByteBuffer>> source
                    = pipeline.fetchEncodedImage(request, true);

            source.subscribe(new ImageDownloadSubscriber(mAppContext) {
                @Override
                protected void onProgress(int progress) {
                    CommonUtils.post(() -> callback.onProgress(progress));

                }

                @Override
                protected void onSuccess(final File image) {
                    CommonUtils.post(()->{
                        callback.onFinish();
                        callback.onCacheMiss(ImageInfoExtractor.getImageType(image), image);
                        callback.onSuccess(image);
                    });

                }

                @Override
                protected void onFail(final Throwable t) {
                    t.printStackTrace();
                    CommonUtils.post(()-> callback.onFail((Exception) t));

                }
            }, mExecutorSupplier.forBackgroundTasks());

            closeSource(requestId);
            saveSource(requestId, source);
        }
    }

    @Override
    public void prefetch(Uri uri) {
        ImagePipeline pipeline = Fresco.getImagePipeline();
        pipeline.prefetchToDiskCache(ImageRequest.fromUri(uri),
                false); // we don't need context, but avoid null
    }

    @Override
    public void cancel(int requestId) {
        closeSource(requestId);
    }

    private void saveSource(int requestId, DataSource target) {
        mRequestSourceMap.put(requestId, target);
    }

    private void closeSource(int requestId) {
        DataSource source = mRequestSourceMap.remove(requestId);
        if (source != null) {
            source.close();
        }
    }

    private File getCacheFile(final ImageRequest request) {
        FileCache mainFileCache = ImagePipelineFactory
                .getInstance()
                .getMainFileCache();
        final CacheKey cacheKey = DefaultCacheKeyFactory
                .getInstance()
                .getEncodedCacheKey(request, false); // we don't need context, but avoid null
        File cacheFile = request.getSourceFile();
        // http://crashes.to/s/ee10638fb31
        if (mainFileCache.hasKey(cacheKey) && mainFileCache.getResource(cacheKey) != null) {
            cacheFile = ((FileBinaryResource) mainFileCache.getResource(cacheKey)).getFile();
        }
        return cacheFile;
    }
}