package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

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
    final ClassLoader classLoader = getClass().getClassLoader();
    final File plan = new File(classLoader.getResource(args[0]).getFile());
    final File in = new File(args[1]);

    try (final InputStream inputStream = new FileInputStream(in)) {
      try (final InputStream planInputStream = new FileInputStream(plan)) {
        final JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
          throw new IllegalArgumentException("Unexpected test version");
        }
        this.testNumber = jsonMessageSource.getTestNumber();

        for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
          final Message message = jsonMessageSource.getResponseMessage(index);
          final Message sourceMessage = jsonMessageSource.getInjectMessage(index);
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
      case 3:
        doTest3(inputStream, values, sourceValues);
        break;
      default:
        throw new IllegalArgumentException("Unexpected test number " + testNumber);
    }
  }

  private void compareDecimal(BigDecimal actual, BigDecimal nullValue, String id,
      MessageValues values, TestException testException) {
    BigDecimal expected = values.getDecimal(id, nullValue);
    if (!expected.equals(actual)) {
      testException.addDetail("Invalid field " + id, expected.toString(), actual.toString());
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
    compareDecimal(BigDecimal.valueOf(leavesQtyDecoder.mantissa(), -leavesQtyDecoder.exponent()),
        BigDecimal.valueOf(
            io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.mantissaNullValue(),
            -io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.exponentNullValue()),
        "151", values, testException);

    io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder cumQtyDecoder =
        executionDecoder.cumQty();
    compareDecimal(BigDecimal.valueOf(cumQtyDecoder.mantissa(), -cumQtyDecoder.exponent()),
        BigDecimal.valueOf(
            io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.mantissaNullValue(),
            -io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.exponentNullValue()),
        "14", values, testException);

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
      io.fixprotocol.sbe.conformance.schema1.DecimalEncodingDecoder fillPxDecoder =
          fillsGrpDecoder.fillPx();
      BigDecimal actualPrice =
          BigDecimal.valueOf(fillPxDecoder.mantissa(), -fillPxDecoder.exponent());
      compareDecimal(actualPrice,
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema1.DecimalEncodingDecoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingDecoder.exponentNullValue()),
          "1364", fillGrpValues, testException);
      io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder fillQtyDecoder =
          fillsGrpDecoder.fillQty();
      compareDecimal(BigDecimal.valueOf(fillQtyDecoder.mantissa(), -fillQtyDecoder.exponent()),
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema1.QtyEncodingDecoder.exponentNullValue()),
          "1365", fillGrpValues, testException);
    }

    if (testException.hasDetails()) {
      throw testException;
    }
  }

  private void doTest3(InputStream in, MessageValues values, MessageValues sourceValues)
      throws IOException, TestException {
    TestException testException = new TestException();
    int inOffset = 0;
    byte[] inBytes = new byte[4096];
    in.read(inBytes, inOffset, inBytes.length);
    DirectBuffer inBuffer = new UnsafeBuffer(inBytes);
    io.fixprotocol.sbe.conformance.schema3.MessageHeaderDecoder messageHeaderDecoder =
        new io.fixprotocol.sbe.conformance.schema3.MessageHeaderDecoder();
    messageHeaderDecoder.wrap(inBuffer, inOffset);
    int templateId = messageHeaderDecoder.templateId();
    if (templateId != io.fixprotocol.sbe.conformance.schema3.ExecutionReportDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type",
          Integer
              .toString(io.fixprotocol.sbe.conformance.schema3.ExecutionReportDecoder.TEMPLATE_ID),
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    io.fixprotocol.sbe.conformance.schema3.ExecutionReportDecoder executionDecoder =
        new io.fixprotocol.sbe.conformance.schema3.ExecutionReportDecoder();
    executionDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(),
        messageHeaderDecoder.version());

    compareString(executionDecoder.orderID(), "37", values, testException);
    compareString(executionDecoder.execID(), "17", values, testException);
    io.fixprotocol.sbe.conformance.schema3.ExecTypeEnum execType = executionDecoder.execType();
    compareString(String.valueOf((char) execType.value()), "150", values, testException);
    io.fixprotocol.sbe.conformance.schema3.OrdStatusEnum ordStatus = executionDecoder.ordStatus();
    compareString(String.valueOf((char) ordStatus.value()), "39", values, testException);
    compareString(executionDecoder.symbol(), "55", sourceValues, testException);


    io.fixprotocol.sbe.conformance.schema3.MONTH_YEARDecoder monthYearDecoder =
        executionDecoder.maturityMonthYear();
    short month = monthYearDecoder.month();
    if (month != io.fixprotocol.sbe.conformance.schema3.MONTH_YEAREncoder.monthNullValue()) {
      testException.addDetail("Invalid field " + 200, "null", monthYearDecoder.toString());
    }

    io.fixprotocol.sbe.conformance.schema3.SideEnum side = executionDecoder.side();
    compareString(String.valueOf((char) side.value()), "54", sourceValues, testException);

    io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder leavesQtyDecoder =
        executionDecoder.leavesQty();
    compareDecimal(BigDecimal.valueOf(leavesQtyDecoder.mantissa(), -leavesQtyDecoder.exponent()),
        BigDecimal.valueOf(
            io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.mantissaNullValue(),
            -io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.exponentNullValue()),
        "151", values, testException);

    io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder cumQtyDecoder =
        executionDecoder.cumQty();
    compareDecimal(BigDecimal.valueOf(cumQtyDecoder.mantissa(), -cumQtyDecoder.exponent()),
        BigDecimal.valueOf(
            io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.mantissaNullValue(),
            -io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.exponentNullValue()),
        "14", values, testException);

    compareInt(executionDecoder.tradeDate(),
        io.fixprotocol.sbe.conformance.schema3.ExecutionReportEncoder.tradeDateNullValue(), "75",
        values, testException);

    compareString(executionDecoder.securityID(), "48", values, testException);

    int fillsGrpCount = values.getGroupCount("FillsGrp");
    io.fixprotocol.sbe.conformance.schema3.ExecutionReportDecoder.FillsGrpDecoder fillsGrpDecoder =
        executionDecoder.fillsGrp();
    int actualCount = fillsGrpDecoder.count();
    if (fillsGrpCount != actualCount) {
      testException.addDetail("Invalid FillsGrp count", Integer.toString(fillsGrpCount),
          Integer.toString(actualCount));
    }

    for (int i = 0; i < fillsGrpCount && i < actualCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpDecoder.next();
      io.fixprotocol.sbe.conformance.schema3.DecimalEncodingDecoder fillPxDecoder =
          fillsGrpDecoder.fillPx();
      BigDecimal actualPrice =
          BigDecimal.valueOf(fillPxDecoder.mantissa(), -fillPxDecoder.exponent());
      compareDecimal(actualPrice,
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema3.DecimalEncodingDecoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema3.DecimalEncodingDecoder.exponentNullValue()),
          "1364", fillGrpValues, testException);
      io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder fillQtyDecoder =
          fillsGrpDecoder.fillQty();
      compareDecimal(BigDecimal.valueOf(fillQtyDecoder.mantissa(), -fillQtyDecoder.exponent()),
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema3.QtyEncodingDecoder.exponentNullValue()),
          "1365", fillGrpValues, testException);
    }

    if (testException.hasDetails()) {
      throw testException;
    }
  }

}
