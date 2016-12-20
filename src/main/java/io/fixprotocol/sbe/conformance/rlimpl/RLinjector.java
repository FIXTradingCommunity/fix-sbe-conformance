package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    File in = new File(args[0]);
    File out = new File(args[1]);
    InputStream inputStream = new FileInputStream(in);
    JsonMessageSource jsonInjectorSource = new JsonMessageSource(inputStream);
    if (!jsonInjectorSource.getTestVersion().equals("2016.1")) {
      throw new IllegalArgumentException("Unexpected test version");
    }
    this.testNumber = jsonInjectorSource.getTestNumber();

    try (FileOutputStream outStream = new FileOutputStream(out)) {
      for (int index = 0; index < jsonInjectorSource.getInjectMessageCount(); index++) {
        Message message = jsonInjectorSource.getInjectMessage(index);
        inject(message, outStream);
      }
    }
  }

  private void doInject1(MessageValues values, OutputStream outFile) throws IOException {
    int offset = 0;
    byte[] bytes = new byte[4096];
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);
    io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder orderEncoder = new io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder();
    io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder messageHeaderEncoder = new io.fixprotocol.sbe.conformance.schema1.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(buffer, offset);
    messageHeaderEncoder.blockLength(orderEncoder.sbeBlockLength())
        .templateId(orderEncoder.sbeTemplateId()).schemaId(orderEncoder.sbeSchemaId())
        .version(orderEncoder.sbeSchemaVersion());
    offset += messageHeaderEncoder.encodedLength();
    orderEncoder.wrap(buffer, offset);
    orderEncoder.clOrdId(values.getString("11"));
    orderEncoder.account(values.getString("1"));
    orderEncoder.symbol(values.getString("55"));
    orderEncoder.side(io.fixprotocol.sbe.conformance.schema1.SideEnum.get(values.getChar("54", io.fixprotocol.sbe.conformance.schema1.SideEnum.NULL_VAL.value())));
    orderEncoder.transactTime(values.getLong("60", io.fixprotocol.sbe.conformance.schema1.NewOrderSingleEncoder.transactTimeNullValue()));
    io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder qtyEncoder = orderEncoder.orderQty();
    qtyEncoder.mantissa(values.getInt("38", io.fixprotocol.sbe.conformance.schema1.QtyEncodingEncoder.exponentNullValue()));
    orderEncoder.ordType(io.fixprotocol.sbe.conformance.schema1.OrdTypeEnum.get(values.getChar("37", io.fixprotocol.sbe.conformance.schema1.OrdTypeEnum.NULL_VAL.value())));
    io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder priceEncoder = orderEncoder.price();
    priceEncoder.mantissa(values.getLong("44", io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue()));
    io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder stopPriceEncoder = orderEncoder.stopPx();
    stopPriceEncoder.mantissa(values.getLong("99", io.fixprotocol.sbe.conformance.schema1.DecimalEncodingEncoder.mantissaNullValue()));

    outFile.write(bytes, 0, offset + orderEncoder.encodedLength());
  }

  private void doInject2(MessageValues values, OutputStream outFile) throws IOException {
    int offset = 0;
    byte[] bytes = new byte[4096];
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);
    io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder orderEncoder = new io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder();
    io.fixprotocol.sbe.conformance.schema2.MessageHeaderEncoder messageHeaderEncoder = new io.fixprotocol.sbe.conformance.schema2.MessageHeaderEncoder();
    messageHeaderEncoder.wrap(buffer, offset);
    messageHeaderEncoder.blockLength(orderEncoder.sbeBlockLength())
        .templateId(orderEncoder.sbeTemplateId()).schemaId(orderEncoder.sbeSchemaId())
        .version(orderEncoder.sbeSchemaVersion());
    offset += messageHeaderEncoder.encodedLength();
    orderEncoder.wrap(buffer, offset);
    orderEncoder.clOrdId(values.getString("11"));
    orderEncoder.account(values.getString("1"));
    orderEncoder.symbol(values.getString("55"));
    orderEncoder.side(io.fixprotocol.sbe.conformance.schema2.SideEnum.get(values.getChar("54", io.fixprotocol.sbe.conformance.schema2.SideEnum.NULL_VAL.value())));
    orderEncoder.transactTime(values.getLong("60", io.fixprotocol.sbe.conformance.schema2.NewOrderSingleEncoder.transactTimeNullValue()));
    io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder qtyEncoder = orderEncoder.orderQty();
    qtyEncoder.mantissa(values.getInt("38", io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder.exponentNullValue()));
    orderEncoder.ordType(io.fixprotocol.sbe.conformance.schema2.OrdTypeEnum.get(values.getChar("37", io.fixprotocol.sbe.conformance.schema2.OrdTypeEnum.NULL_VAL.value())));
    io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder priceEncoder = orderEncoder.price();
    priceEncoder.mantissa(values.getLong("44", io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder.mantissaNullValue()));
    io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder stopPriceEncoder = orderEncoder.stopPx();
    stopPriceEncoder.mantissa(values.getLong("99", io.fixprotocol.sbe.conformance.schema2.DecimalEncodingEncoder.mantissaNullValue()));
    io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder minQtyEncoder = orderEncoder.minQty();
    minQtyEncoder.mantissa(values.getInt("110", io.fixprotocol.sbe.conformance.schema2.QtyEncodingEncoder.exponentNullValue()));
    
    outFile.write(bytes, 0, offset + orderEncoder.encodedLength());
  }

}
