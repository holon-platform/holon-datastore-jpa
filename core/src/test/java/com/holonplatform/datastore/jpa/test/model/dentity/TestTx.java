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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.datastore.jpa.JpaTarget;

@Entity
@Table(name = "testtx")
public class TestTx {

	public final static DataTarget<?> TX_TARGET = JpaTarget.of(TestTx.class);

	public final static BeanPropertySet<TestTx> TEST_TX = BeanPropertySet.create(TestTx.class);

	public final static PathProperty<Long> TX_CODE = TEST_TX.property("code", Long.class);
	public final static PathProperty<String> TX_TEXT = TEST_TX.property("text", String.class);

	@Id
	@Column(name = "code")
	private Long code;

	@Column(name = "text")
	private String text;

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
