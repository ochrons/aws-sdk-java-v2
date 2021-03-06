/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.core.http.async;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;
import software.amazon.awssdk.core.async.AsyncResponseHandler;
import software.amazon.awssdk.core.util.Throwables;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.SdkHttpResponseHandler;
import software.amazon.awssdk.utils.FunctionalUtils.UnsafeFunction;

/**
 * Adapts an {@link AsyncResponseHandler} to a {@link SdkHttpResponseHandler} by first performing unmarshalling
 * on the initial response (headers/status code).
 *
 * @param <ResponseT> Response POJO type.
 * @param <ReturnT>   Transformation result type. Returned by {@link #complete()}
 */
public class UnmarshallingAsyncResponseHandler<ResponseT, ReturnT> implements SdkHttpResponseHandler<ReturnT> {

    private final AsyncResponseHandler<ResponseT, ReturnT> asyncResponseHandler;
    private final UnsafeFunction<SdkHttpResponse, ResponseT> unmarshaller;

    /**
     * @param asyncResponseHandler Response handler being adapted.
     * @param unmarshaller         Unmarshaller that takes an {@link SdkHttpResponse} and returns the unmarshalled POJO.
     */
    public UnmarshallingAsyncResponseHandler(AsyncResponseHandler<ResponseT, ReturnT> asyncResponseHandler,
                                             UnsafeFunction<SdkHttpResponse, ResponseT> unmarshaller) {
        this.asyncResponseHandler = asyncResponseHandler;
        this.unmarshaller = unmarshaller;
    }

    @Override
    public void headersReceived(SdkHttpResponse response) {
        try {
            asyncResponseHandler.responseReceived(unmarshaller.apply(response));
        } catch (Exception e) {
            throw Throwables.failure(e);
        }
    }

    @Override
    public void onStream(Publisher<ByteBuffer> publisher) {
        asyncResponseHandler.onStream(publisher);
    }

    @Override
    public void exceptionOccurred(Throwable throwable) {
        asyncResponseHandler.exceptionOccurred(throwable);
    }

    @Override
    public ReturnT complete() {
        return asyncResponseHandler.complete();
    }
}
