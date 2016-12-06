# FIX Simple Binary Conformance Test

Simple Binary Encoding (SBE) is a [standard](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) for binary message encoding. SBE is part of a family of protocols created by the High Performance Working Group of the FIX Trading Community. SBE is a presentation layer protocol (layer 6). 

This project provides a conformance test suite to verify interoperability of SBE implementations.

## Test Framework
This project provides these features:
* A file format for specifying test plans. See [SBE Conformance Test Plan Format](TestPlanFormat.md)
* A test injector to create SBE messages that conform to a test plan
* A test validator that compares the response messages written by an implementation under test to expected values.

The test injector and validator use the Java SBE implementation developed by [Real Logic](https://github.com/real-logic/simple-binary-encoding).


## Implementations Under Test
Implementors are responsible for developing an application that ...
* Decodes injected SBE messages that conform to a provided message schema.
* Reads the test plan file for expected values to set.
* Encodes response messages and writes the buffer to a file.

If developed in Java, realize the Responder interface. However, all data is passed through files, and all that really matters is behavior. Any programming language or method can be used. 

After writing the response file, invoke the Validator to verify the response messages meet expectations. If not, the a report of differences is produced.

## License
© Copyright 2016 FIX Protocol Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Prerequisites
This project requires Java 8. It should run on any platform for which the JVM is supported. 

Note that SBE implementations under test may be written in any programming language. All data to and from a tested implementation is passed through files, which should be platform independent.

## Build
The project is built with Maven. 

