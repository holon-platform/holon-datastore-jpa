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
package com.holonplatform.datastore.jpa.converters.datetime;

import java.sql.Time;
import java.time.LocalTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * A JPA {@link AttributeConverter} to enable {@link LocalTime} data type for entity attributes.
 * <p>
 * This converter is automatically applyed if present in the ORM auto-scan package list.
 * </p>
 * <p>
 * Attribute values are expected to be backed by a {@link Time}-compatible SQL type in database.
 * </p>
 * 
 * @since 5.0.0
 */
@Converter(autoApply = true)
public class LocalTimeToTimeAttributeConverter implements AttributeConverter<LocalTime, Time> {

	/*
	 * (non-Javadoc)
	 * @see jakarta.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
	 */
	@Override
	public Time convertToDatabaseColumn(LocalTime attribute) {
		return (attribute == null) ? null : Time.valueOf(attribute);
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public LocalTime convertToEntityAttribute(Time dbData) {
		return (dbData == null) ? null : dbData.toLocalTime();
	}

}
