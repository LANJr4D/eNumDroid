/*
 * Copyright 2009 Nominet UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.nominet.DDDS;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ENUMTest {

	ENUM			mEnum = null;
	
	String			suffix = "e164.arpa";
	String			key = "+44-(1865)-332211";
	String			aus = "+441865332211";
	String			domain = "1.1.2.2.3.3.5.6.8.1.4.4.e164.arpa";
	
	@Before
	public void setUp() throws Exception {
		mEnum = new ENUM(suffix);
	}

	@Test
	public void testConvertKeyToAUS() {
		String res = mEnum.convertKeyToAUS(key);
		assertEquals(aus, res);
	}

	@Test
	public void testConvertAUSToDBKey() {
		String res = mEnum.convertAUSToDBKey(aus);
		assertEquals(domain, res);
	}

	@Test
	public void testLookup() {
		Rule[] rules = mEnum.lookup(key);
		for (Rule r: rules) {
			System.out.println(r);
		}
		assertEquals(2, rules.length);
		assertEquals("E2U+sip", rules[0].getService());
		assertEquals("sip:211@nominet.org.uk", rules[0].evaluate());
	}
}
