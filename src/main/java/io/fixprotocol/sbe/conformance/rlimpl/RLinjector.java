package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.conformance.Injector;
import io.fixprotocol.sbe.conformance.MessageValues;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;

/**
 * Uses the Real Logic implementation of SBE to inject test messages
 * 
 * @author Don Mendelson
 *
 */
public class RLinjector implements Injector {

  /**
   * Invokes the Real Logic implementation of SBE to inject test messages
   * 
   * @param args files names
   *        <ol>
   *        <li>File name of test plan</li>
   *        <li>File name of message file to produce</li>
   *        </ol>
   * @throws IOException if an IO error occurs
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      usage();
      System.exit(1);
    }
    RLinjector injector = new RLinjector(args);
    injector.injectAll();
  }

  public static void usage() {
    System.out.println(
        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLinjector <input-test-file> <output-sbe-file>");
  }

  private final String[] args;

  private int testNumber;

  public RLinjector(String[] args) {
    this.args = args;
  }

  public void inject(MessageValues values, OutputStream out) throws IOException {
    switch (testNumber) {
      case 1:
        doInject1(values, out);
        break;
      case 2:
        doInject2(values, out);
        break;
      default:
        throw new IllegalArgumentException("Unexpected test number");
    }
  }

  public void injectAll() throws FileNotFoundException, IllegalArgumentException, IOException {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File in = new File(classLoader.getResource(args[0]).getFile());
    final File out = new File(args[1]);
    final InputStream inputStream = new FileInputStream(in);
    final JsonMessageSource jsonInjectorSource = new JsonMessageSource(inputStream);
    if (!jsonInjectorSource.getTestVersion().equals("2016.1")) {
      throw new IllegalArgumentException("Unexpected test version");
    }
    this.testNumber = jsonInjectorSource.getTestNumber();

    try (final FileOutputStream outStream = new FileOutputStream(out)) {
      for (int index = 0; index < jsonInjectorSource.getInjectMessageCount(); index++) {
        final Message message = jsonInjectorSource.getInjectMessage(index);
        inject(message, outStream);
      }
    }
  }

  private void doInject1(MessageValues values, OutputStream outFile) throws IOException {
    int offset = 0;
    byte[] bytes = new byte[4096];
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);
    io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder orderEncoder =
        new io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder();
    io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder messageHeaderEncoder =
        new io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(buffer, offset);
    messageHeaderEncoder.blockLength(orderEncoder.sbeBlockLength())
        .templateId(orderEncoder.sbeTemplateId()).schemaId(orderEncoder.sbeSchemaId())
        .version(orderEncoder.sbeSchemaVersion());
    offset += messageHeaderEncoder.encodedLength();
    orderEncoder.wrap(buffer, offset);
    orderEncoder.clOrdId(values.getString("11"));
    orderEncoder.account(values.getString("1"));
    orderEncoder.symbol(values.getString("55"));
    orderEncoder.side(io.fixprotocol.sbe.conformance.schema1.SideEnum.get(
        values.getChar("54", io.fixprotocol.sbe.conformance.schema1.SideEnum.NULL_VAL.value())));
    orderEncoder.transactTime(values.getLong("60",
        io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder.transactTimeNullValue()));
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder qtyEncoder = orderEncoder.orderQty();
    qtyEncoder.mantissa(values
        .getDecimal("38",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    orderEncoder.ordType(io.fixprotocol.sbe.conformance.schema1.OrdTypeEnum.get(
        values.getChar("37", io.fixprotocol.sbe.conformance.schema1.OrdTypeEnum.NULL_VAL.value())));
    io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder priceEncoder =
        orderEncoder.price();
    BigDecimal price = values
        .getDecimal("44",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
    priceEncoder.mantissa(price
        .movePointRight(-priceEncoder.exponent()).longValue());
    io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder stopPriceEncoder =
        orderEncoder.stopPx();
    BigDecimal stopPx = values
        .getDecimal("99",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
    stopPriceEncoder.mantissa(stopPx
        .movePointRight(-priceEncoder.exponent()).longValue());

    outFile.write(bytes, 0, offset + orderEncoder.encodedLength());
  }

  private void doInject2(MessageValues values, OutputStream outFile) throws IOException {
    int offset = 0;
    byte[] bytes = new byte[4096];
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);
    io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder orderEncoder =
        new io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder();
    io.fixprotocol.sbe.conformance.schema2.MessageHeaderEncoder messageHeaderEncoder =
        new io.fixprotocol.sbe.conformance.schema2.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(buffer, offset);
    messageHeaderEncoder.blockLength(orderEncoder.sbeBlockLength())
        .templateId(orderEncoder.sbeTemplateId()).schemaId(orderEncoder.sbeSchemaId())
        .version(orderEncoder.sbeSchemaVersion());
    offset += messageHeaderEncoder.encodedLength();
    orderEncoder.wrap(buffer, offset);
    orderEncoder.clOrdId(values.getString("11"));
    orderEncoder.account(values.getString("1"));
    orderEncoder.symbol(values.getString("55"));
    orderEncoder.side(io.fixprotocol.sbe.conformance.schema2.SideEnum.get(
        values.getChar("54", io.fixprotocol.sbe.conformance.schema2.SideEnum.NULL_VAL.value())));
    orderEncoder.transactTime(values.getLong("60",
        io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder.transactTimeNullValue()));
    io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder qtyEncoder = orderEncoder.orderQty();
    qtyEncoder.mantissa(values
        .getDecimal("38",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());
    orderEncoder.ordType(io.fixprotocol.sbe.conformance.schema2.OrdTypeEnum.get(
        values.getChar("37", io.fixprotocol.sbe.conformance.schema2.OrdTypeEnum.NULL_VAL.value())));
    io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder priceEncoder =
        orderEncoder.price();
    BigDecimal price = values
        .getDecimal("44",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
    priceEncoder.mantissa(price
        .movePointRight(-priceEncoder.exponent()).longValue());
    io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder stopPriceEncoder =
        orderEncoder.stopPx();
    BigDecimal stopPx = values
        .getDecimal("99",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.exponentNullValue()));
    stopPriceEncoder.mantissa(stopPx
        .movePointRight(-priceEncoder.exponent()).longValue());
    io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder minQtyEncoder = orderEncoder.minQty();
    minQtyEncoder.mantissa(values
        .getDecimal("110",
            BigDecimal.valueOf(
                io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.mantissaNullValue(),
                -io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()))
        .intValue());

    outFile.write(bytes, 0, offset + orderEncoder.encodedLength());
  }

}
