/*
 * Copyright 2017 The OpenDSP Project
 *
 * The OpenDSP Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package mobi.f2time.dorado.rest;

import java.io.InputStream;
import java.lang.reflect.Method;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import io.netty.util.CharsetUtil;
import mobi.f2time.dorado.exception.DoradoException;
import mobi.f2time.dorado.rest.util.IOUtils;
import mobi.f2time.dorado.rest.util.SerializeUtils;

/**
 * 
 * @author wangwp
 */
public interface MessageBodyConverter<T> {
	byte[] writeMessageBody(T t);

	T readMessageBody(InputStream in, Class<T> clazz);

	MessageBodyConverter<? extends Object> JSON = new MessageBodyConverter<Object>() {
		@Override
		public byte[] writeMessageBody(Object t) {
			return com.alibaba.fastjson.JSON.toJSONBytes(t);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object readMessageBody(InputStream in, Class clazz) {
			return JSONObject.parseObject(IOUtils.toString(in, "utf-8"), clazz);
		}
	};

	@SuppressWarnings("rawtypes")
	MessageBodyConverter TEXT_PLAIN = new MessageBodyConverter<Object>() {
		@Override
		public byte[] writeMessageBody(Object t) {
			return t.toString().getBytes(CharsetUtil.UTF_8);
		}

		@Override
		public Object readMessageBody(InputStream in, Class<Object> clazz) {
			return IOUtils.toString(in, "utf-8");
		}
	};

	@SuppressWarnings("rawtypes")
	MessageBodyConverter PROTOBUF = new MessageBodyConverter<Message>() {
		@Override
		public byte[] writeMessageBody(Message t) {
			return t.toByteArray();
		}

		@Override
		public Message readMessageBody(InputStream in, Class<Message> clazz) {
			try {
				Method newBuilder = clazz.getMethod("newBuilder");
				GeneratedMessage.Builder builder = (GeneratedMessage.Builder) newBuilder.invoke(clazz);

				return builder.mergeFrom(in).build();
			} catch (Throwable ex) {
				throw new DoradoException(ex);
			}
		}
	};

	@SuppressWarnings("rawtypes")
	MessageBodyConverter DEFAULT = new MessageBodyConverter<Object>() {
		@Override
		public byte[] writeMessageBody(Object t) {
			return SerializeUtils.serialize(t);
		}

		@Override
		public Object readMessageBody(InputStream in, Class<Object> clazz) {
			return SerializeUtils.deserialize(in, clazz);
		}
	};
}
