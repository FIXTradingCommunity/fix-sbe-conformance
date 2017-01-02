package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.conformance.MessageValues;
import io.fixprotocol.sbe.conformance.Responder;
import io.fixprotocol.sbe.conformance.TestException;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;

/**
 * Tests the Real Logic implementation of SBE
 * 
 * @author Don Mendelson
 *
 */
public class RLUnderTest implements Responder {

  private final String[] args;
  private int testNumber;


  public RLUnderTest(String[] args) {
    this.args = args;
  }

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
    RLUnderTest tester = new RLUnderTest(args);
    tester.respondAll();
  }

  private void respondAll()
      throws IllegalArgumentException, IOException, TestException, FileNotFoundException {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File plan = new File(classLoader.getResource(args[0]).getFile());
    final File in = new File(args[1]);
    final File out = new File(args[2]);
    try (final InputStream inputStream = new FileInputStream(in)) {
      try (final InputStream planInputStream = new FileInputStream(plan)) {
        final JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
          throw new IllegalArgumentException("Unexpected test version");
        }
        this.testNumber = jsonMessageSource.getTestNumber();

        try (final FileOutputStream outputStream = new FileOutputStream(out)) {
          for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
            final Message message = jsonMessageSource.getResponseMessage(index);
            respond(inputStream, message, outputStream);
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
  public void respond(InputStream inputStream, MessageValues values, OutputStream outputStream)
      throws IOException, TestException {
    switch (testNumber) {
      case 1:
        doTest1(inputStream, values, outputStream);
        break;
      case 2:
        doTest2(inputStream, values, outputStream);
        break;
      default:
        throw new IllegalArgumentException("Unexpected test number " + testNumber);
    }
  }

  private void doTest1(InputStream in, MessageValues values, OutputStream outFile)
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
    if (templateId != io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type",
          Integer
              .toString(io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID),
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder orderDecoder =
        new io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder();
    orderDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(),
        messageHeaderDecoder.version());

    int outOffset = 0;
    byte[] outBytes = new byte[4096];
    MutableDirectBuffer outBuffer = new UnsafeBuffer(outBytes);
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder executionEncoder =
        new io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder();
    io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder messageHeaderEncoder =
        new io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(outBuffer, outOffset);
    messageHeaderEncoder.blockLength(executionEncoder.sbeBlockLength())
        .templateId(executionEncoder.sbeTemplateId()).schemaId(executionEncoder.sbeSchemaId())
        .version(executionEncoder.sbeSchemaVersion());
    outOffset += messageHeaderEncoder.encodedLength();
    executionEncoder.wrap(outBuffer, outOffset);
    executionEncoder.orderID(values.getString("37"));
    executionEncoder.execID(values.getString("17"));
    executionEncoder.execType(io.fixprotocol.sbe.conformance.schema1.ExecTypeEnum.get(values
        .getChar("150", io.fixprotocol.sbe.conformance.schema1.ExecTypeEnum.NULL_VAL.value())));
    executionEncoder.ordStatus(io.fixprotocol.sbe.conformance.schema1.OrdStatusEnum.get(values
        .getChar("39", io.fixprotocol.sbe.conformance.schema1.OrdStatusEnum.NULL_VAL.value())));
    executionEncoder.symbol(orderDecoder.symbol());
    io.fixprotocol.sbe.conformance.schema1.MONTH_YEAREncoder monthYearEncoder =
        executionEncoder.maturityMonthYear();
    monthYearEncoder
        .month(io.fixprotocol.sbe.conformance.schema1.MONTH_YEAREncoder.monthNullValue());
    executionEncoder.side(orderDecoder.side());
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder leavesQtyEncoder =
        executionEncoder.leavesQty();
    leavesQtyEncoder.mantissa(values
        .getDecimal("151",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder cumQtyEncoder =
        executionEncoder.cumQty();
    cumQtyEncoder.mantissa(values
        .getDecimal("14",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    executionEncoder.tradeDate(values.getInt("75",
        io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder.tradeDateNullValue()));

    int fillsGrpCount = values.getGroupCount("FillsGrp");
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder.FillsGrpEncoder fillsGrpEncoder =
        executionEncoder.fillsGrpCount(fillsGrpCount);
    for (int i = 0; i < fillsGrpCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpEncoder.next();
      io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder fillPxEncoder =
          fillsGrpEncoder.fillPx();
      BigDecimal fillPx = fillGrpValues.getDecimal("1364",
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
      fillPxEncoder.mantissa(fillPx
          .movePointRight(-fillPxEncoder.exponent()).longValue());
      io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder fillQtyEncoder =
          fillsGrpEncoder.fillQty();
      fillQtyEncoder.mantissa(fillGrpValues
          .getDecimal("1365",
              BigDecimal.valueOf(
                  io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                  -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
          .intValue());
    }

    outFile.write(outBytes, 0, outOffset + executionEncoder.encodedLength());
  }

  private void doTest2(InputStream in, MessageValues values, OutputStream outFile)
      throws IOException, TestException {
    TestException testException = new TestException();
    int inOffset = 0;
    byte[] inBytes = new byte[4096];
    in.read(inBytes, inOffset, inBytes.length);
    DirectBuffer inBuffer = new UnsafeBuffer(inBytes);
    io.fixprotocol.sbe.conformance.schema2.MessageHeaderDecoder messageHeaderDecoder =
        new io.fixprotocol.sbe.conformance.schema2.MessageHeaderDecoder();
    messageHeaderDecoder.wrap(inBuffer, inOffset);
    int templateId = messageHeaderDecoder.templateId();
    if (templateId != io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID) {
      testException.addDetail("Unexpected message type",
          Integer
              .toString(io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID),
          Integer.toString(templateId));
      throw testException;
    }
    inOffset += messageHeaderDecoder.encodedLength();
    io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder orderDecoder =
        new io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder();
    orderDecoder.wrap(inBuffer, inOffset, messageHeaderDecoder.blockLength(),
        messageHeaderDecoder.version());

    int outOffset = 0;
    byte[] outBytes = new byte[4096];
    MutableDirectBuffer outBuffer = new UnsafeBuffer(outBytes);
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder executionEncoder =
        new io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder();
    io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder messageHeaderEncoder =
        new io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(outBuffer, outOffset);
    messageHeaderEncoder.blockLength(executionEncoder.sbeBlockLength())
        .templateId(executionEncoder.sbeTemplateId()).schemaId(executionEncoder.sbeSchemaId())
        .version(executionEncoder.sbeSchemaVersion());
    outOffset += messageHeaderEncoder.encodedLength();
    executionEncoder.wrap(outBuffer, outOffset);
    executionEncoder.orderID(values.getString("37"));
    executionEncoder.execID(values.getString("17"));
    executionEncoder.execType(io.fixprotocol.sbe.conformance.schema1.ExecTypeEnum.get(values
        .getChar("150", io.fixprotocol.sbe.conformance.schema1.ExecTypeEnum.NULL_VAL.value())));
    executionEncoder.ordStatus(io.fixprotocol.sbe.conformance.schema1.OrdStatusEnum.get(values
        .getChar("39", io.fixprotocol.sbe.conformance.schema1.OrdStatusEnum.NULL_VAL.value())));
    executionEncoder.symbol(orderDecoder.symbol());
    io.fixprotocol.sbe.conformance.schema1.MONTH_YEAREncoder monthYearEncoder =
        executionEncoder.maturityMonthYear();
    monthYearEncoder
        .month(io.fixprotocol.sbe.conformance.schema1.MONTH_YEAREncoder.monthNullValue());
    executionEncoder.side(orderDecoder.side());
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder leavesQtyEncoder =
        executionEncoder.leavesQty();
    leavesQtyEncoder.mantissa(values
        .getDecimal("151",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder cumQtyEncoder =
        executionEncoder.cumQty();
    cumQtyEncoder.mantissa(values
        .getDecimal("14",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    executionEncoder.tradeDate(values.getInt("75",
        io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder.tradeDateNullValue()));

    int fillsGrpCount = values.getGroupCount("FillsGrp");
    io.fixprotocol.sbe.conformance.schema1.ExecutionReportEncoder.FillsGrpEncoder fillsGrpEncoder =
        executionEncoder.fillsGrpCount(fillsGrpCount);
    for (int i = 0; i < fillsGrpCount; i++) {
      MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
      fillsGrpEncoder.next();
      io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder fillPxEncoder =
          fillsGrpEncoder.fillPx();
      BigDecimal fillPx = fillGrpValues.getDecimal("1364",
          BigDecimal.valueOf(
              io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
              -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
      fillPxEncoder.mantissa(fillPx
          .movePointRight(-fillPxEncoder.exponent()).longValue());
      io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder fillQtyEncoder =
          fillsGrpEncoder.fillQty();
      fillQtyEncoder.mantissa(fillGrpValues
          .getDecimal("1365",
              BigDecimal.valueOf(
                  io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                  -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
          .intValue());
    }

    outFile.write(outBytes, 0, outOffset + executionEncoder.encodedLength());
  }
}
