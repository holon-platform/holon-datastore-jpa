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
package com.holonplatform.datastore.jpa.test.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.core.property.VirtualProperty;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.test.model.entity.Test1;
import com.holonplatform.datastore.jpa.test.model.entity.Test3;

public interface TestDataModel {

	public final static JpaTarget<Test1> JPA_TARGET = JpaTarget.of(Test1.class);

	public final static NumericProperty<Long> KEY = NumericProperty.create("key", long.class);
	public final static StringProperty STR = StringProperty.create("stringValue");
	public final static NumericProperty<Double> DBL = NumericProperty.doubleType("decimalValue");
	public final static TemporalProperty<Date> DAT = TemporalProperty.create("dateValue", Date.class)
			.temporalType(TemporalType.DATE);
	public final static TemporalProperty<LocalDate> LDAT = TemporalProperty.localDate("localDateValue");
	public final static PathProperty<TestEnum> ENM = PathProperty.create("enumValue", TestEnum.class);
	public final static PathProperty<Boolean> NBOOL = PathProperty.create("numericBooleanValue", boolean.class)
			.converter(PropertyValueConverter.numericBoolean(Integer.class));
	public final static PathProperty<String> NST_STR = PathProperty.create("nested.nestedStringValue", String.class);
	public final static PathProperty<BigDecimal> NST_DEC = PathProperty.create("nested.nestedDecimalValue",
			BigDecimal.class);
	public final static PathProperty<String> SNST_STR = PathProperty.create("nested.subNested.subnestedStringValue",
			String.class);

	public final static TemporalProperty<Date> TMS = TemporalProperty.create("datetimeValue", Date.class)
			.temporalType(TemporalType.DATE_TIME);
	public final static TemporalProperty<LocalDateTime> LTMS = TemporalProperty.localDateTime("localDatetimeValue");

	public final static TemporalProperty<LocalTime> TIME = TemporalProperty.localTime("localTimeValue");

	public final static PropertySet<?> PROPERTIES = PropertySet
			.builderOf(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME).identifier(KEY).build();

	public final static PropertySet<?> PROPERTIES_NOID = PropertySet.of(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR,
			NST_DEC, TMS, LTMS, TIME);

	// virtual

	public static final VirtualProperty<String> VIRTUAL_STR = VirtualProperty.create(String.class, pb -> {
		return pb.getValueIfPresent(STR).map(str -> "[" + str + "]").orElse("NONE");
	});

	public final static PropertySet<?> PROPERTIES_V = PropertySet
			.builderOf(KEY, STR, DBL, DAT, LDAT, ENM, NBOOL, NST_STR, NST_DEC, TMS, LTMS, TIME, VIRTUAL_STR)
			.identifier(KEY).build();

	// lobs

	public final static PathProperty<String> CLOB_STR = PathProperty.create("clobValue", String.class);

	public final static PathProperty<byte[]> BLOB_BYS = PathProperty.create("blobValue", byte[].class);

	public final static byte[] DEFAULT_BLOB_VALUE = hexStringToByteArray("C9CBBBCCCEB9C8CABCCCCEB9C9CBBB");

	// with parent
	public final static PathProperty<Long> KEY_P = JPA_TARGET.property(KEY);
	public final static PathProperty<String> STR_P = JPA_TARGET.property(STR);

	// recur

	public static final DataTarget<?> R_TARGET = DataTarget.named("TestRec");
	public static final PathProperty<String> R_NAME = PathProperty.create("name", String.class);
	public static final PathProperty<String> R_PARENT = PathProperty.create("parent", String.class);

	// test2

	public final static DataTarget<String> TEST2 = DataTarget.named("test2_entity");

	public final static PathProperty<Long> TEST2_CODE = PathProperty.create("code", long.class);
	public final static PathProperty<String> TEST2_TEXT = PathProperty.create("value", String.class);

	public final static PropertySet<?> TEST2_PROPERTIES = PropertySet.of(TEST2_CODE, TEST2_TEXT);

	// test3

	public final static JpaTarget<Test3> TEST3 = JpaTarget.of(Test3.class);

	public final static PathProperty<Long> TEST3_CODE = PathProperty.create("pk.code", long.class);
	public final static PathProperty<String> TEST3_TEXT = PathProperty.create("text", String.class);

	public final static PathProperty<Long> TEST3_CODE_P = PathProperty.create("pk.code", long.class).parent(TEST3);
	public final static PathProperty<String> TEST3_TEXT_P = PathProperty.create("text", String.class).parent(TEST3);

	public final static PropertySet<?> TEST3_SET = PropertySet.builderOf(TEST3_CODE, TEST3_TEXT).identifier(TEST3_CODE)
			.build();

	// utils

	static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

}
