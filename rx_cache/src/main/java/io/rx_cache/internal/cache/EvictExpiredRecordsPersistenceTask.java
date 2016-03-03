/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache.internal.cache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache.Persistence;
import io.rx_cache.Record;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

@Singleton
public final class EvictExpiredRecordsPersistenceTask {
    private final HasRecordExpired hasRecordExpired;
    private final Persistence persistence;

    public @Inject EvictExpiredRecordsPersistenceTask(HasRecordExpired hasRecordExpired, Persistence persistence) {
        this.hasRecordExpired = hasRecordExpired;
        this.persistence = persistence;
    }

    public Observable<String> startEvictingExpiredRecords() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                List<String> allKeys = persistence.allKeys();

                for (String key : allKeys) {
                    Record record = persistence.retrieveRecord(key);
                    if (record != null && hasRecordExpired.hasRecordExpired(record)) {
                        persistence.evict(key);
                        subscriber.onNext(key);
                    }
                }

                subscriber.onCompleted();
            }
        }).subscribeOn((Schedulers.io())).observeOn(Schedulers.io());
    }
}
