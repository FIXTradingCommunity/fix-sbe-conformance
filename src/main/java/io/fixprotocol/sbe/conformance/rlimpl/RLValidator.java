package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.conformance.MessageValues;
import io.fixprotocol.sbe.conformance.Responder;
import io.fixprotocol.sbe.conformance.TestException;
import io.fixprotocol.sbe.conformance.Validator;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;
import io.fixprotocol.sbe.conformance.suite1.DecimalEncodingDecoder;
import io.fixprotocol.sbe.conformance.suite1.DecimalEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.ExecTypeEnum;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportDecoder;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportDecoder.FillsGrpDecoder;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportEncoder;
import io.fixprotocol.sbe.conformance.suite1.MONTH_YEAREncoder;
import io.fixprotocol.sbe.conformance.suite1.MessageHeaderDecoder;
import io.fixprotocol.sbe.conformance.suite1.MessageHeaderEncoder;
import io.fixprotocol.sbe.conformance.suite1.NewOrderSingleDecoder;
import io.fixprotocol.sbe.conformance.suite1.OrdStatusEnum;
import io.fixprotocol.sbe.conformance.suite1.QtyEncodingDecoder;
import io.fixprotocol.sbe.conformance.suite1.QtyEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.SideEnum;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportEncoder.FillsGrpEncoder;
import io.fixprotocol.sbe.conformance.suite1.MONTH_YEARDecoder;

/**
 * Tests the Real Logic implementation of SBE
 * 
 * @author Don Mendelson
 *
 */
public class RLValidator implements Validator {

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
    Validator tester = new RLValidator();
    File plan = new File(args[0]);
    File in = new File(args[1]);

    try (InputStream inputStream = new FileInputStream(in)) {
      try (InputStream planInputStream = new FileInputStream(plan)) {
        JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
          throw new IllegalArgumentException("Unexpected test version");
        }
        if (jsonMessageSource.getTestNumber() != 1) {
          throw new IllegalArgumentException("Unexpected test number");
        }
        for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
          Message message = jsonMessageSource.getResponseMessage(index);
          Message sourceMessage = jsonMessageSource.getInjectMessage(index);
          tester.validate(inputStream, message, sourceMessage);

        }
      }
    }
  }

  public static void usage() {
    System.out.println(
        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLValidator <input-test-file> <input-sbe-file>");
  }

  @Override
  public void validate(InputStream inputStream, MessageValues values, MessageValues sourceValues) throws IOException, TestException {
    doTest1(inputStream, values, sourceValues);
  }


  private void compareInt(int actual, int nullValue, String id, MessageValues values, TestException testException) {
    int expected = values.getInt(id, nullValue);
    if (expected != actual) {
      testException.addDetail("Invalid field " + id, Integer.toString(expected), Integer.toString(actual));
    }
  }

  private void compareLong(long actual, long nullValue, String id, MessageValues values, TestException testException) {
    long expected = values.getLong(id, nullValue);
    if (expected != actual) {
      testException.addDetail("Invalid field " + id, Long.toString(expected), Long.toString(actual));
    }
  }

  private void compareString(String actual, String id, MessageValues values, TestException testException) {
    String expected = values.getString(id);
    if (!expected.equals(actual)) {
      testException.addDetail("Invalid field " + id, expected, actual);
    }
  }
  
  private void doTest1(InputStream in, MessageValues values, MessageValues sourceValues) throws IOException, TestException {
    TestException testException = new TestException();
    int inOffset = 0;
    byte[] inBytes = new byte[4096];
    in.read(inBytes, inOffset, inBytes.length);
    DirectBuffer inBuffer = new UnsafeBuffer(inBytes);
    MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    messageHeaderDecoder.wrap(inBuffer, inOffset);
    int templateId = messageHeaderDecoder.templateId();
    if (templateId != ExecutionReportDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type", Integer.toString(ExecutionReportDecoder.TEMPLATE_ID), 
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    ExecutionReportDecoder executionDecoder = new ExecutionReportDecoder();
    executionDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());

    compareString(executionDecoder.orderID(), "37", values, testException);
    compareString(executionDecoder.execID(), "17", values, testException);
    ExecTypeEnum execType = executionDecoder.execType();
    compareString(String.valueOf((char)execType.value()), "150", values, testException); 
    OrdStatusEnum ordStatus = executionDecoder.ordStatus();
    compareString(String.valueOf((char)ordStatus.value()), "39", values, testException);
    compareString(executionDecoder.symbol(), "55", sourceValues, testException);
    
    
    MONTH_YEARDecoder monthYearDecoder = executionDecoder.maturityMonthYear();
    short month = monthYearDecoder.month();
    if (month != MONTH_YEAREncoder.monthNullValue()) {
      testException.addDetail("Invalid field " + 200, "null", monthYearDecoder.toString());
    }
    
    SideEnum side = executionDecoder.side();
    compareString(String.valueOf((char)side.value()), "54", sourceValues, testException); 
    
    QtyEncodingDecoder leavesQtyDecoder = executionDecoder.leavesQty();
    compareInt(leavesQtyDecoder.mantissa(), QtyEncodingEncoder.mantissaNullValue(),
        "151", values, testException);
    
    QtyEncodingDecoder cumQtyDecoder = executionDecoder.cumQty();
    compareInt(cumQtyDecoder.mantissa(), QtyEncodingEncoder.mantissaNullValue(),
        "14", values, testException);
    
    compareInt(executionDecoder.tradeDate(), ExecutionReportEncoder.tradeDateNullValue(),
        "75", values, testException);
    
    int fillsGrpCount = values.getGroupCount("FillsGrp");
    FillsGrpDecoder fillsGrpDecoder = executionDecoder.fillsGrp();
    int actualCount = fillsGrpDecoder.count();
    if (fillsGrpCount != actualCount) {
      testException.addDetail("Invalid FillsGrp count", Integer.toString(fillsGrpCount), 
          Integer.toString(actualCount));
    }
    
    for (int i=0; i < fillsGrpCount && i < actualCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpDecoder.next();
      DecimalEncodingDecoder fillPxEncoder = fillsGrpDecoder.fillPx();
      compareLong(fillPxEncoder.mantissa(), DecimalEncodingEncoder.mantissaNullValue(), "1364", fillGrpValues, testException);
      QtyEncodingDecoder fillQtyEncoder = fillsGrpDecoder.fillQty();
      compareInt(fillQtyEncoder.mantissa(), QtyEncodingEncoder.mantissaNullValue(), "1365", fillGrpValues, testException);
    }
    
    if (testException.hasDetails()) {
      throw testException;
    }
  }
}
