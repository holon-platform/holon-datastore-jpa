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

public class TestMapped {

	private final long key;
	private final String value;
	private final int ordinal;

	public TestMapped(long key, String value, int ordinal) {
		super();
		this.key = key;
		this.value = value;
		this.ordinal = ordinal;
	}

	public long getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public int getOrdinal() {
		return ordinal;
	}

}
