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
package com.holonplatform.datastore.jpa.test.model.dentity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TestNested implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "nst1")
	private String nestedStringValue;

	@Column(name = "nst2")
	private BigDecimal nestedDecimalValue;

	private SubNested subNested;

	public String getNestedStringValue() {
		return nestedStringValue;
	}

	public void setNestedStringValue(String nestedStringValue) {
		this.nestedStringValue = nestedStringValue;
	}

	public BigDecimal getNestedDecimalValue() {
		return nestedDecimalValue;
	}

	public void setNestedDecimalValue(BigDecimal nestedDecimalValue) {
		this.nestedDecimalValue = nestedDecimalValue;
	}

	public SubNested getSubNested() {
		return subNested;
	}

	public void setSubNested(SubNested subNested) {
		this.subNested = subNested;
	}

}
