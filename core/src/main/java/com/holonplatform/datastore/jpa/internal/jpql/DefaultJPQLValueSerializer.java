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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.jpql.JPQLValueSerializer;

/**
 * Default {@link JPQLValueSerializer}.
 *
 * @since 5.1.0
 */
public enum DefaultJPQLValueSerializer implements JPQLValueSerializer {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
	private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance(Locale.US);

	public static final String ANSI_DATE_FORMAT = "yyyy-MM-dd";
	public static final String ANSI_TIME_FORMAT = "HH:mm:ss";
	public static final String ANSI_DATETIME_FORMAT = ANSI_DATE_FORMAT + " " + ANSI_TIME_FORMAT;

	private static final DateTimeFormatter ANSI_LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(ANSI_DATE_FORMAT,
			Locale.US);
	private static final DateTimeFormatter ANSI_LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern(ANSI_TIME_FORMAT,
			Locale.US);

	static {
		INTEGER_FORMAT.setGroupingUsed(false);
		INTEGER_FORMAT.setParseIntegerOnly(true);
		DECIMAL_FORMAT.setGroupingUsed(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaValueSerializer#serialize(java.lang.Object,
	 * com.holonplatform.core.temporal.TemporalType)
	 */
	@Override
	public String serialize(Object value, TemporalType temporalType) {

		// null
		if (value == null) {
			return "NULL";
		}

		// boolean
		if (TypeUtils.isBoolean(value.getClass())) {
			return (Boolean) value ? "TRUE" : "FALSE";
		}

		// enums
		if (TypeUtils.isEnum(value.getClass())) {
			return value.getClass().getName() + "." + ((Enum<?>) value).name();
		}

		// CharSequence
		if (TypeUtils.isCharSequence(value.getClass())) {
			return "'" + value.toString() + "'";
		}

		// numbers
		if (TypeUtils.isNumber(value.getClass())) {
			if (TypeUtils.isLong(value.getClass())) {
				return INTEGER_FORMAT.format(value) + "L";
			}
			if (TypeUtils.isFloat(value.getClass())) {
				return DECIMAL_FORMAT.format(value) + "f";
			}
			if (TypeUtils.isDecimalNumber(value.getClass())) {
				return DECIMAL_FORMAT.format(value) + "d";
			}
			return INTEGER_FORMAT.format(value);
		}

		// date and times
		Optional<String> asTemporal = serializeTemporal(value, temporalType);
		if (asTemporal.isPresent()) {
			return asTemporal.get();
		}

		// defaults to unqualified class name
		return value.getClass().getSimpleName();
	}

	/**
	 * Serialize given value as a temporal type.
	 * @param value Value to serialize
	 * @param temporalType Temporal type
	 * @return Serialized value, empty if it was not a temporal type
	 */
	private static Optional<String> serializeTemporal(Object value, TemporalType temporalType) {

		if (TypeUtils.isDate(value.getClass()) || TypeUtils.isCalendar(value.getClass())) {
			final Date date = TypeUtils.isCalendar(value.getClass()) ? ((Calendar) value).getTime() : (Date) value;
			TemporalType tt = (temporalType != null) ? temporalType : TemporalType.DATE;

			LocalDate datePart = null;
			LocalTime timePart = null;

			switch (tt) {
			case DATE_TIME:
				datePart = ConversionUtils.toLocalDate(date);
				timePart = ConversionUtils.toLocalTime(date);
				break;
			case TIME:
				timePart = ConversionUtils.toLocalTime(date);
				break;
			case DATE:
			default:
				datePart = ConversionUtils.toLocalDate(date);
				break;
			}

			return Optional.of(serializeDateTimeValue(datePart, timePart));
		}

		if (TemporalAccessor.class.isAssignableFrom(value.getClass())) {

			LocalDate datePart = null;
			LocalTime timePart = null;

			if (value instanceof LocalDate) {
				datePart = (LocalDate) value;
			} else if (value instanceof LocalTime) {
				timePart = (LocalTime) value;
			} else if (value instanceof LocalDateTime) {
				datePart = ((LocalDateTime) value).toLocalDate();
				timePart = ((LocalDateTime) value).toLocalTime();
			} else if (value instanceof OffsetTime) {
				timePart = ((OffsetTime) value).toLocalTime();
			} else if (value instanceof OffsetDateTime) {
				datePart = ((OffsetDateTime) value).toLocalDate();
				timePart = ((OffsetDateTime) value).toLocalTime();
			} else if (value instanceof ZonedDateTime) {
				datePart = ((ZonedDateTime) value).toLocalDate();
				timePart = ((ZonedDateTime) value).toLocalTime();
			}

			if (datePart != null || timePart != null) {
				LocalDate serializeDate = datePart;
				LocalTime serializeTime = timePart;

				if (temporalType != null) {
					if (temporalType == TemporalType.DATE) {
						serializeTime = null;
					} else if (temporalType == TemporalType.TIME) {
						serializeDate = null;
					}
				}

				return Optional.of(serializeDateTimeValue(serializeDate, serializeTime));
			}
		}

		return Optional.empty();
	}

	/**
	 * Serialize a date/time value using given {@link LocalDate} part, {@link LocalTime} part and zone offset.
	 * @param datePart Date part
	 * @param timePart Time part
	 * @return Serialized date/time value
	 */
	private static String serializeDateTimeValue(LocalDate datePart, LocalTime timePart) {
		final StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (datePart != null && timePart != null) {
			sb.append("ts");
		} else if (datePart != null) {
			sb.append("d");
		} else if (timePart != null) {
			sb.append("t");
		}

		sb.append(" '");

		boolean appendSpace = false;
		if (datePart != null) {
			sb.append(ANSI_LOCAL_DATE_FORMATTER.format(datePart));
			appendSpace = true;
		}
		if (timePart != null) {
			if (appendSpace) {
				sb.append(" ");
			}
			sb.append(ANSI_LOCAL_TIME_FORMATTER.format(timePart));
		}

		sb.append("'}");

		return sb.toString();
	}

}
