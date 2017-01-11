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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.fixprotocol.sbe.conformance.TestException;

/**
 * This test is just to demonstrate the test framework. It uses Real Logic for both the test
 * injector and system under test.
 * 
 * @author Don Mendelson
 *
 */
@RunWith(value = Parameterized.class)
public class RLinjectorLoopbackTest {

  @Parameter
  public int testNumber;

  @Parameters
  public static Collection<Integer[]> data() {
    return Arrays.asList(new Integer[][] {{0}, {1}, {2}});
  }

  static final String[] injectorFiles = {"test1inject.sbe", "test2inject.sbe", "test3inject.sbe"};
  static final String[] responderFiles = {"test1respond.sbe", "test2respond.sbe", "test3respond.sbe"};
  static final String[] testFiles = {"test1.json", "test2.json", "test3.json"};


  @Test
  public void test() throws IOException, TestException {
    RLinjector.main(new String[] {testFiles[testNumber], injectorFiles[testNumber]});
    RLUnderTest.main(new String[] {testFiles[testNumber], injectorFiles[testNumber],
        responderFiles[testNumber]});
    RLValidator.main(new String[] {testFiles[testNumber], responderFiles[testNumber]});
  }

}
