/**
 * Copyright 2016 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package io.fixprotocol.sbe.conformance.rlimpl;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol.sbe.conformance.TestException;

/**
 * This test is just to demonstrate the test framework. It uses Real Logic for both the test 
 * injector and system under test.
 * 
 * @author Don Mendelson
 *
 */
public class RLinjectorLoopbackTest {

  static final String INJECTOR_SBE_FILE = "test1inject.sbe";
  static final String RESPONDER_SBE_FILE = "test1respond.sbe";
  static final String TEST_FILE = "test1.json";

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    RLinjector.main(new String [] {TEST_FILE, INJECTOR_SBE_FILE});
  }


  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void test() throws IOException, TestException {
    RLUnderTest.main(new String [] {TEST_FILE, INJECTOR_SBE_FILE, RESPONDER_SBE_FILE});
    RLValidator.main(new String [] {TEST_FILE, RESPONDER_SBE_FILE});
  }

}
