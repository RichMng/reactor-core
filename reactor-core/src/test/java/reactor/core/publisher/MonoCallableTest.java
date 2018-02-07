/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.publisher;

import java.io.IOException;

import org.junit.Test;
import reactor.test.subscriber.AssertSubscriber;

import static org.assertj.core.api.Assertions.*;

public class MonoCallableTest {

    @Test
    public void nullCallable() {
        assertThatNullPointerException()
                .isThrownBy(() -> Mono.<Integer>fromCallable(null));
    }

    @Test
    public void callableReturnsNull() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Mono.<Integer>fromCallable(() -> null).subscribe(ts);

        ts.assertNoValues()
          .assertNoError()
          .assertComplete();
    }

    @Test
    public void callableReturnsNullShortcircuitsBackpressure() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

        Mono.<Integer>fromCallable(() -> null).subscribe(ts);

        ts.assertNoValues()
          .assertNoError()
          .assertComplete();
    }

    @Test
    public void normal() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        Mono.fromCallable(() -> 1).subscribe(ts);

        ts.assertValues(1)
          .assertComplete()
          .assertNoError();
    }

    @Test
    public void normalBackpressured() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

        Mono.fromCallable(() -> 1).subscribe(ts);

        ts.assertNoValues()
          .assertNotComplete()
          .assertNoError();

        ts.request(1);

        ts.assertValues(1)
          .assertComplete()
          .assertNoError();
    }

    @Test
    public void callableThrows() {
        AssertSubscriber<Object> ts = AssertSubscriber.create();

        Mono.fromCallable(() -> {
            throw new IOException("forced failure");
        }).subscribe(ts);

        ts.assertNoValues()
          .assertNotComplete()
          .assertError(IOException.class)
          .assertErrorMessage("forced failure");
    }

    @Test
    public void onMonoSuccessCallableOnBlock() {
        assertThat(Mono.fromCallable(() -> "test")
                       .block()).isEqualToIgnoringCase("test");
    }

    @Test
    public void onMonoEmptyCallableOnBlock() {
        assertThat(Mono.fromCallable(() -> null)
                       .block()).isNull();
    }

    @Test
    public void onMonoErrorCallableOnBlock() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> Mono.fromCallable(() -> {
                    throw new Exception("test");
                }).block());
    }

    @Test
    public void delegateCall() throws Exception {
        MonoCallable<Integer> monoCallable = new MonoCallable<>(() -> 1);

        assertThat(monoCallable.call()).isEqualTo(1);
    }

    @Test
    public void delegateCallError() {
        MonoCallable<Integer> monoCallable = new MonoCallable<>(() -> {
            throw new IllegalStateException("boom");
        });

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(monoCallable::call)
                .withMessage("boom");
    }

    @Test
    public void delegateCallNull() throws Exception {
        MonoCallable<Integer> monoCallable = new MonoCallable<>(() -> null);

        assertThat(monoCallable.call()).isNull();
    }
}
