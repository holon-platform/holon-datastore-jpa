/*
 * Copyright 2000-2016 Holon TDCN.
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
package com.holonplatform.datastore.jpa.test.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.holonplatform.datastore.jpa.test.data.TestData;

@Entity
@Table(name = "test1bis")
public class TestJpaDomainBis implements TestData {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "keycode")
	private Long key;

	@Column(name = "strv")
	private String stringValue;

	@Column(name = "decv")
	private Double decimalValue;

	@Temporal(TemporalType.DATE)
	@Column(name = "datv")
	private Date dateValue;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "enmv")
	private TestEnum enumValue;

	@Column(name = "nbv")
	private int numericBooleanValue;

	private TestNested nested;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public Double getDecimalValue() {
		return decimalValue;
	}

	public void setDecimalValue(Double decimalValue) {
		this.decimalValue = decimalValue;
	}

	@Override
	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	@Override
	public TestEnum getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(TestEnum enumValue) {
		this.enumValue = enumValue;
	}

	@Override
	public int getNumericBooleanValue() {
		return numericBooleanValue;
	}

	public void setNumericBooleanValue(int numericBooleanValue) {
		this.numericBooleanValue = numericBooleanValue;
	}

	@Override
	public TestNested getNested() {
		return nested;
	}

	public void setNested(TestNested nested) {
		this.nested = nested;
	}

}
