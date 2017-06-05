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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class SubNested implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "nss1")
	private String subnestedStringValue;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "nss2")
	private Date subnestedDateValue;

	public String getSubnestedStringValue() {
		return subnestedStringValue;
	}

	public void setSubnestedStringValue(String subnestedStringValue) {
		this.subnestedStringValue = subnestedStringValue;
	}

	public Date getSubnestedDateValue() {
		return subnestedDateValue;
	}

	public void setSubnestedDateValue(Date subnestedDateValue) {
		this.subnestedDateValue = subnestedDateValue;
	}

}
