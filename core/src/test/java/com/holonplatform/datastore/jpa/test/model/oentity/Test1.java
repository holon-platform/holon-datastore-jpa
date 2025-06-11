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
package com.holonplatform.datastore.jpa.test.model.oentity;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.holonplatform.datastore.jpa.test.model.TestData;
import com.holonplatform.datastore.jpa.test.model.TestEnum;

@Entity
@Table(name = "test1")
public class Test1 implements TestData {

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

	@Temporal(TemporalType.DATE)
	@Column(name = "datv2")
	private Date localDateValue;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "enmv")
	private TestEnum enumValue;

	@Column(name = "nbv")
	private int numericBooleanValue;

	private TestNested nested;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "tms")
	private Date datetimeValue;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "tms2")
	private Date localDatetimeValue;

	@Temporal(TemporalType.TIME)
	@Column(name = "tm")
	private Date localTimeValue;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "clb")
	private String clobValue;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "blb")
	private byte[] blobValue;

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

	public Date getDatetimeValue() {
		return datetimeValue;
	}

	public void setDatetimeValue(Date datetimeValue) {
		this.datetimeValue = datetimeValue;
	}

	public Date getLocalDateValue() {
		return localDateValue;
	}

	public void setLocalDateValue(Date localDateValue) {
		this.localDateValue = localDateValue;
	}

	public Date getLocalDatetimeValue() {
		return localDatetimeValue;
	}

	public void setLocalDatetimeValue(Date localDatetimeValue) {
		this.localDatetimeValue = localDatetimeValue;
	}

	public Date getLocalTimeValue() {
		return localTimeValue;
	}

	public void setLocalTimeValue(Date localTimeValue) {
		this.localTimeValue = localTimeValue;
	}

	public String getClobValue() {
		return clobValue;
	}

	public void setClobValue(String clobValue) {
		this.clobValue = clobValue;
	}

	public byte[] getBlobValue() {
		return blobValue;
	}

	public void setBlobValue(byte[] blobValue) {
		this.blobValue = blobValue;
	}

	public TestNested getNested() {
		return nested;
	}

	public void setNested(TestNested nested) {
		this.nested = nested;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Test1 [key=" + key + "]";
	}

}
