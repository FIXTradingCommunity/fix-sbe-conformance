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
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;
import io.fixprotocol.sbe.conformance.suite1.DecimalEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.ExecTypeEnum;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportEncoder;
import io.fixprotocol.sbe.conformance.suite1.MONTH_YEAREncoder;
import io.fixprotocol.sbe.conformance.suite1.MessageHeaderDecoder;
import io.fixprotocol.sbe.conformance.suite1.MessageHeaderEncoder;
import io.fixprotocol.sbe.conformance.suite1.NewOrderSingleDecoder;
import io.fixprotocol.sbe.conformance.suite1.OrdStatusEnum;
import io.fixprotocol.sbe.conformance.suite1.QtyEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.ExecutionReportEncoder.FillsGrpEncoder;

/**
 * Tests the Real Logic implementation of SBE
 * 
 * @author Don Mendelson
 *
 */
public class RLUnderTest implements Responder {

  /**
   * Invokes the Real Logic implementation of SBE to produce test results
   * 
   * @param args files names
   *        <ol>
   *        <li>File name of test plan</li>
   *        <li>File name of injected message file to read</li>
   *        <li>File name of message file to produce</li>
   *        </ol>
   * @throws IOException if an IO error occurs
   * @throws TestException if one or more encoding or decoding errors occur
   */
  public static void main(String[] args) throws IOException, TestException {
    if (args.length < 3) {
      usage();
      System.exit(1);
    }
    RLUnderTest tester = new RLUnderTest();
    File plan = new File(args[0]);
    File in = new File(args[1]);
    File out = new File(args[2]);
    try (InputStream inputStream = new FileInputStream(in)) {
      try (InputStream planInputStream = new FileInputStream(plan)) {
        JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
          throw new IllegalArgumentException("Unexpected test version");
        }
        if (jsonMessageSource.getTestNumber() != 1) {
          throw new IllegalArgumentException("Unexpected test number");
        }
        try (FileOutputStream outputStream = new FileOutputStream(out)) {
          for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
            Message message = jsonMessageSource.getResponseMessage(index);
            tester.respond(inputStream, message, outputStream);
          }
        }
      }
    }
  }

  public static void usage() {
    System.out.println(
        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLUnderTest <input-sbe-file> <input-test-file> <output-sbe-file>");
  }

  @Override
  public void respond(InputStream inputStream, MessageValues values, OutputStream outputStream) throws IOException, TestException {
    doTest1(inputStream, values, outputStream);
  }


  private void doTest1(InputStream in, MessageValues values, OutputStream outFile) throws IOException, TestException {
    TestException testException = new TestException();
    int inOffset = 0;
    byte[] inBytes = new byte[4096];
    in.read(inBytes, inOffset, inBytes.length);
    DirectBuffer inBuffer = new UnsafeBuffer(inBytes);
    MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    messageHeaderDecoder.wrap(inBuffer, inOffset);
    int templateId = messageHeaderDecoder.templateId();
    if (templateId != NewOrderSingleDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type", Integer.toString(NewOrderSingleDecoder.TEMPLATE_ID), 
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    NewOrderSingleDecoder orderDecoder = new NewOrderSingleDecoder();
    orderDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
    
    int outOffset = 0;
    byte[] outBytes = new byte[4096];
    MutableDirectBuffer outBuffer = new UnsafeBuffer(outBytes);
    ExecutionReportEncoder executionEncoder = new ExecutionReportEncoder();
    MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    messageHeaderEncoder.wrap(outBuffer, outOffset);
    messageHeaderEncoder.blockLength(executionEncoder.sbeBlockLength())
        .templateId(executionEncoder.sbeTemplateId()).schemaId(executionEncoder.sbeSchemaId())
        .version(executionEncoder.sbeSchemaVersion());
    outOffset += messageHeaderEncoder.encodedLength();
    executionEncoder.wrap(outBuffer, outOffset);
    executionEncoder.orderID(values.getString("37"));
    executionEncoder.execID(values.getString("17"));
    executionEncoder.execType(ExecTypeEnum.get(values.getChar("150", ExecTypeEnum.NULL_VAL.value())));
    executionEncoder.ordStatus(OrdStatusEnum.get(values.getChar("39", OrdStatusEnum.NULL_VAL.value())));  
    executionEncoder.symbol(orderDecoder.symbol());
    MONTH_YEAREncoder monthYearEncoder = executionEncoder.maturityMonthYear();
    monthYearEncoder.month(MONTH_YEAREncoder.monthNullValue());
    executionEncoder.side(orderDecoder.side());
    QtyEncodingEncoder leavesQtyEncoder = executionEncoder.leavesQty();
    leavesQtyEncoder.mantissa(values.getInt("151", QtyEncodingEncoder.mantissaNullValue()));
    QtyEncodingEncoder cumQtyEncoder = executionEncoder.cumQty();
    cumQtyEncoder.mantissa(values.getInt("14", QtyEncodingEncoder.mantissaNullValue()));
    executionEncoder.tradeDate(values.getInt("75", ExecutionReportEncoder.tradeDateNullValue()));

    int fillsGrpCount = values.getGroupCount("FillsGrp");
    FillsGrpEncoder fillsGrpEncoder = executionEncoder.fillsGrpCount(fillsGrpCount);
    for (int i=0; i < fillsGrpCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpEncoder.next();
      DecimalEncodingEncoder fillPxEncoder = fillsGrpEncoder.fillPx();
      fillPxEncoder.mantissa(fillGrpValues.getLong("1364", DecimalEncodingEncoder.mantissaNullValue())); 
      QtyEncodingEncoder fillQtyEncoder = fillsGrpEncoder.fillQty();
      fillQtyEncoder.mantissa(fillGrpValues.getInt("1365", QtyEncodingEncoder.mantissaNullValue()));
    }
    
    outFile.write(outBytes, 0, outOffset + executionEncoder.encodedLength());
  }

}