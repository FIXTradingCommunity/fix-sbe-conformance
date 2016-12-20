package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.conformance.MessageValues;
import io.fixprotocol.sbe.conformance.TestException;
import io.fixprotocol.sbe.conformance.Validator;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;

/**
 * Tests the Real Logic implementation of SBE
 * 
 * @author Don Mendelson
 *
 */
public class RLValidator implements Validator {

  private String[] args;
  private int testNumber;

  public RLValidator(String[] args) {
    this.args = args;
  }

  /**
   * Invokes the Real Logic implementation of SBE to produce test results
   * 
   * @param args files names
   *        <ol>
   *        <li>File name of test plan</li>
   *        <li>File name of response message file to read</li>
   *        </ol>
   * @throws IOException if an IO error occurs
   * @throws TestException if one or more encoding or decoding errors occur
   */
  public static void main(String[] args) throws IOException, TestException {
    if (args.length < 2) {
      usage();
      System.exit(1);
    }
    RLValidator tester = new RLValidator(args);
    tester.validateAll();
  }

  public void validateAll() throws IOException, TestException {
    File plan = new File(args[0]);
    File in = new File(args[1]);

    try (InputStream inputStream = new FileInputStream(in)) {
      try (InputStream planInputStream = new FileInputStream(plan)) {
        JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
          throw new IllegalArgumentException("Unexpected test version");
        }
        this.testNumber = jsonMessageSource.getTestNumber();

        for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
          Message message = jsonMessageSource.getResponseMessage(index);
          Message sourceMessage = jsonMessageSource.getInjectMessage(index);
          validate(inputStream, message, sourceMessage);
        }
      }
    }
  }

  public static void usage() {
    System.out.println(
        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLValidator <input-test-file> <input-sbe-file>");
  }

  @Override
  public void validate(InputStream inputStream, MessageValues values, MessageValues sourceValues)
      throws IOException, TestException {
    switch (testNumber) {
      case 1:
        doTest1(inputStream, values, sourceValues);
        break;
      case 2:
        doTest1(inputStream, values, sourceValues);
        break;        
      default:
        throw new IllegalArgumentException("Unexpected test number " + testNumber);
    }
  }


  private void compareInt(int actual, int nullValue, String id, MessageValues values,
      TestException testException) {
    int expected = values.getInt(id, nullValue);
    if (expected != actual) {
      testException.addDetail("Invalid field " + id, Integer.toString(expected),
          Integer.toString(actual));
    }
  }

  private void compareLong(long actual, long nullValue, String id, MessageValues values,
      TestException testException) {
    long expected = values.getLong(id, nullValue);
    if (expected != actual) {
      testException.addDetail("Invalid field " + id, Long.toString(expected),
          Long.toString(actual));
    }
  }

  private void compareString(String actual, String id, MessageValues values,
      TestException testException) {
    String expected = values.getString(id);
    if (!expected.equals(actual)) {
      testException.addDetail("Invalid field " + id, expected, actual);
    }
  }

  private void doTest1(InputStream in, MessageValues values, MessageValues sourceValues)
      throws IOException, TestException {
    TestException testException = new TestException();
    int inOffset = 0;
    byte[] inBytes = new byte[4096];
    in.read(inBytes, inOffset, inBytes.length);
    DirectBuffer inBuffer = new UnsafeBuffer(inBytes);
    io.fixprotocol.sbe.conformance.schema1.MessageHeaderDecoder messageHeaderDecoder =
        new io.fixprotocol.sbe.conformance.schema1.MessageHeaderDecoder();
    messageHeaderDecoder.wrap(inBuffer, inOffset);
    int templateId = messageHeaderDecoder.templateId();
    if (templateId != io.fixprotocol.sbe.conformance.schema1.ExecutionReportDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type",
          Integer
              .toString(io.fixprotocol.sbe.conformance.schema1.ExecutionReportDecoder.TEMPLATE_ID),
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportDecoder executionDecoder =
        new io.fixprotocol.sbe.conformance.schema1.ExecutionReportDecoder();
    executionDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(),
        messageHeaderDecoder.version());

    compareString(executionDecoder.orderID(), "37", values, testException);
    compareString(executionDecoder.execID(), "17", values, testException);
    io.fixprotocol.sbe.conformance.schema1.ExecTypeEnum execType = executionDecoder.execType();
    compareString(String.valueOf((char) execType.value()), "150", values, testException);
    io.fixprotocol.sbe.conformance.schema1.OrdStatusEnum ordStatus = executionDecoder.ordStatus();
    compareString(String.valueOf((char) ordStatus.value()), "39", values, testException);
    compareString(executionDecoder.symbol(), "55", sourceValues, testException);


    io.fixprotocol.sbe.conformance.schema1.MONTH_YEARDecoder monthYearDecoder =
        executionDecoder.maturityMonthYear();
    short month = monthYearDecoder.month();
    if (month != io.fixprotocol.sbe.conformance.schema1.MONTH_YEAREncoder.monthNullValue()) {
      testException.addDetail("Invalid field " + 200, "null", monthYearDecoder.toString());
    }

    io.fixprotocol.sbe.conformance.schema1.SideEnum side = executionDecoder.side();
    compareString(String.valueOf((char) side.value()), "54", sourceValues, testException);

    io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder leavesQtyDecoder =
        executionDecoder.leavesQty();
    compareInt(leavesQtyDecoder.mantissa(),
        io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(), "151",
        values, testException);

    io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder cumQtyDecoder =
        executionDecoder.cumQty();
    compareInt(cumQtyDecoder.mantissa(),
        io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(), "14", values,
        testException);

    compareInt(executionDecoder.tradeDate(),
        io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder.tradeDateNullValue(), "75",
        values, testException);

    int fillsGrpCount = values.getGroupCount("FillsGrp");
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportDecoder.FillsGrpDecoder fillsGrpDecoder =
        executionDecoder.fillsGrp();
    int actualCount = fillsGrpDecoder.count();
    if (fillsGrpCount != actualCount) {
      testException.addDetail("Invalid FillsGrp count", Integer.toString(fillsGrpCount),
          Integer.toString(actualCount));
    }

    for (int i = 0; i < fillsGrpCount && i < actualCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpDecoder.next();
      io.fixprotocol.sbe.conformance.schema1.DecimalEncodingDecoder fillPxEncoder =
          fillsGrpDecoder.fillPx();
      compareLong(fillPxEncoder.mantissa(),
          io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(), "1364",
          fillGrpValues, testException);
      io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder fillQtyEncoder =
          fillsGrpDecoder.fillQty();
      compareInt(fillQtyEncoder.mantissa(),
          io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(), "1365",
          fillGrpValues, testException);
    }

    if (testException.hasDetails()) {
      throw testException;
    }
  }
}
