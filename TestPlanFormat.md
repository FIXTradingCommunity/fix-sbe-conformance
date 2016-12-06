# SBE Conformance Test Plan Format

## Overview
A test file supplies expected values to test injectors and SBE implementations under test. It is formatted as a JSON document. See [The JSON Data Interchange Format ](http://www.ecma-international.org/publications/standards/Ecma-262.htm)

**Note:** this test file format does not conform to the usage guide for JSON encoding of FIX.

## Metadata

The JSON `version` object provides metadata including the test version, test number and SBE message schema filename. The metadata can be used by test implementations to validate that the test file matches an expectation.

Metadata example:

```json
	"version": {
	"testVersion": "2016.1",
	"testNumber": 1,
	"messageSchema": "TestSchema1.xml"
	}
```

## Field values
A field value is represented in the test file as a JSON name/value pair where the name is the FIX field ID (tag). The value is JSON text but should be encoded as the proper SBE data type, with the exception of special values listed below. All numeric encodings should formatted as a JSON number, and all character types are JSON text.

A field value defined in an SBE enum  type should be represented its literal code value rather than symbolic name.

Field example:
```json
	"11": "CL000001"
```

### Special values
A JSON value of `null` indicates that the SBE field should be encoded using the SBE null value for the data type of the field. See the [SBE specification](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) for a listing of those values.

A JSON value of `true` indicates that a field in a response message should echo a value received on an injected message.

Use of JSON value `false` is currently undefined for this test format.

## Messages

### Sections
A test file is divided into these sections:

 The `inject` JSON object contains definitions of messages to be injected into the system under test by a reference implementation.

The `respond` JSON object contains messages to be encoded by the system under test.

### Message values

Each of the sections contains a `messages` array of one or more message objects.

Each message object contains a JSON pair with name `template`, giving the SBE template ID value for that message to be encoded. After that, field values are given for the root of the message.

Example of a message to be injected with literal field values:

```json
		"messages": [
			{
			"template": 99,
			"11": "CL000001",
		    "1": "ACCT0001",
		    "55": "SYMBOL.A",
		    "54": "2",
		    "60": 1480936563000000,
		    "38": 700,
		    "37": "2",
		    "44": 17.56,
		    "99": null
			}
		]
```

### Repeating groups

Repeating groups are represented by a JSON array within a message object. The name of the array should be the name of the repeating group as shown in FIX Repository.  Each entry of the repeating group is a JSON object containing fields. Fields within a repeating group are JSON name/value pairs in the same format as in the root of a message.

Example of a message with a repeating group:

```json
		"messages": [
			{
			"template": 98,
			"11": "OR000001",
			"17": "EX000001",
			"150": "F"
			"39": "1"
			"55": true,
			"200", null,
			"54": true,
			"151": 400,
			"1": 0,
			"75", 17140,
			"FillsGrp": [
				{
					"1364": 17.56
					"1365": 300
				}
			]
			}
		]
    }
```