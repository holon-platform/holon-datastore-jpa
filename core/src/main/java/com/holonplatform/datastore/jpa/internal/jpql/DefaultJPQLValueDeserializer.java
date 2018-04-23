/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa.internal.jpql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.holonplatform.core.ConverterExpression;
import com.holonplatform.core.ExpressionValueConverter;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.internal.JpqlDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.JPQLValueDeserializer;

/**
 * Default {@link JPQLValueDeserializer}.
 *
 * @since 5.1.0
 */
public enum DefaultJPQLValueDeserializer implements JPQLValueDeserializer {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JpqlDatastoreLogger.create();

	/**
	 * Additional value processors
	 */
	private final List<ValueProcessor> valueProcessors = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.operation.JpaValueDeserializer#addValueProcessor(com.holonplatform.datastore.jpa.
	 * operation.JpaValueDeserializer.ValueProcessor)
	 */
	@Override
	public void addValueProcessor(ValueProcessor valueProcessor) {
		ObjectUtils.argumentNotNull(valueProcessor, "Value processor must be not null");
		valueProcessors.add(valueProcessor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaValueDeserializer#deserialize(com.holonplatform.datastore.jpa.
	 * context.JpaExecutionContext, com.holonplatform.core.TypedExpression, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(JpaExecutionContext context, TypedExpression<T> expression, Object valueToDeserialize)
			throws DataAccessException {

		ObjectUtils.argumentNotNull(expression, "Value deserialization expression must be not null");

		LOGGER.debug(() -> "<DefaultJpaValueDeserializer> Deserializing value [" + valueToDeserialize + "] of type ["
				+ ((valueToDeserialize == null) ? "NULL" : valueToDeserialize.getClass()) + "] for expression type ["
				+ expression.getType() + "]");

		Object value = valueToDeserialize;

		// apply processors
		for (ValueProcessor processor : valueProcessors) {
			value = processor.processValue(context, expression, value);
			LOGGER.debug(() -> "<DefaultJpaValueDeserializer> Value to deserialize processed by ValueProcessor ["
					+ processor.getClass() + "]");
		}

		// null always deserialized as null
		if (value == null) {
			LOGGER.debug(() -> "<DefaultJpaValueDeserializer> Value to deserialize is NULL, return it as NULL");
			return null;
		}

		// check converter
		final ExpressionValueConverter<?, ?> converter = (expression instanceof ConverterExpression)
				? ((ConverterExpression<?>) expression).getExpressionValueConverter().orElse(null)
				: null;

		// actual type to deserialize
		Class<?> targetType = (converter != null) ? converter.getModelType() : expression.getType();

		LOGGER.debug(() -> "<DefaultJpaValueDeserializer> ExpressionValueConverter "
				+ ((converter != null) ? "detected" : "not detected") + " - deserialization target type: [" + targetType
				+ "]");

		Object deserialized = deserialize(targetType, value);

		if (converter != null) {
			if (deserialized == null || TypeUtils.isAssignable(deserialized.getClass(), converter.getModelType())) {
				deserialized = ((ExpressionValueConverter<Object, Object>) converter).fromModel(deserialized);
			}
		}

		final Object deserializedValue = deserialized;
		LOGGER.debug(() -> "<DefaultJpaValueDeserializer> Deserialized value: [" + deserializedValue + "] - Type: ["
				+ ((deserializedValue == null) ? "NULL" : deserializedValue.getClass()) + "]");

		// check type
		if (TypeUtils.isAssignable(deserializedValue.getClass(), expression.getType())) {
			return (T) deserializedValue;
		} else {
			throw new DataAccessException("Failed to deserialize value [" + value + "] of type [" + value.getClass()
					+ "] for required type [" + expression.getType() + "]");
		}

	}

	/**
	 * Deserialize given <code>value</code> using supported value types.
	 * @param targetType Target type to obtain
	 * @param value Value to deserialize (not null)
	 * @return Optional deserialized value
	 * @throws DataAccessException If an error occurred
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object deserialize(Class<?> targetType, Object value) throws DataAccessException {
		// enum
		if (TypeUtils.isEnum(targetType)) {
			return ConversionUtils.convertEnumValue((Class<Enum>) targetType, value);
		}

		// number
		if (TypeUtils.isNumber(targetType) && TypeUtils.isNumber(value.getClass())) {
			return ConversionUtils.convertNumberToTargetClass((Number) value, (Class<Number>) targetType);
		}

		// date and times
		if (Date.class.isAssignableFrom(value.getClass())) {
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDate((Date) value);
			}
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDateTime((Date) value);
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalTime((Date) value);
			}
		}
		if (java.util.Date.class.isAssignableFrom(value.getClass())) {
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDate((java.util.Date) value);
			}
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalDateTime((java.util.Date) value);
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ConversionUtils.toLocalTime((java.util.Date) value);
			}
		}

		if (Timestamp.class.isAssignableFrom(value.getClass())) {
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime();
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime().toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((Timestamp) value).toLocalDateTime().toLocalTime();
			}
			if (java.util.Date.class.isAssignableFrom(targetType)) {
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(((Timestamp) value).getTime());
				return c.getTime();
			}
		}

		if (Time.class.isAssignableFrom(value.getClass())) {
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((Time) value).toLocalTime();
			}
		}

		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return Date.valueOf(((LocalDate) value));
			}
		}
		if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return new Date(Timestamp.valueOf(((LocalDateTime) value)).getTime());
			}
			if (Timestamp.class.isAssignableFrom(targetType)) {
				return Timestamp.valueOf(((LocalDateTime) value));
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((LocalDateTime) value).toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((LocalDateTime) value).toLocalTime();
			}
		}
		if (OffsetDateTime.class.isAssignableFrom(value.getClass())) {
			if (Date.class.isAssignableFrom(targetType) || java.util.Date.class.isAssignableFrom(targetType)) {
				return new Date(Timestamp.valueOf(((OffsetDateTime) value).toLocalDateTime()).getTime());
			}
			if (Timestamp.class.isAssignableFrom(targetType)) {
				return Timestamp.valueOf(((OffsetDateTime) value).toLocalDateTime());
			}
			if (LocalDateTime.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalDateTime();
			}
			if (LocalDate.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalDate();
			}
			if (LocalTime.class.isAssignableFrom(targetType)) {
				return ((OffsetDateTime) value).toLocalTime();
			}
		}

		// String to Reader
		if (TypeUtils.isString(value.getClass()) && Reader.class.isAssignableFrom(targetType)) {
			return new StringReader((String) value);
		}

		// Byte[] to InputStream
		if (value instanceof byte[] && InputStream.class.isAssignableFrom(targetType)) {
			return new ByteArrayInputStream((byte[]) value);
		}

		return value;
	}

}
