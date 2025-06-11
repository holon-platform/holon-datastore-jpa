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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.AttributeConverter;

/**
 * Interface which may be used to identify the package containing JPA {@link AttributeConverter}s for auto-conversion of
 * <code>java.time.Local*</code> date/time types ({@link LocalDate}, {@link LocalTime} and {@link LocalDateTime}) into
 * and from standard SQL JDBC data types.
 * 
 * @since 5.0.0
 */
public interface LocalDateTimeConverters {

	// convenience interface to be used as package name reference

}
