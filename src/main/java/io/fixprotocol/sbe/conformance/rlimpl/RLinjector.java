package io.fixprotocol.sbe.conformance.rlimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.conformance.suite1.DecimalEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.MessageHeaderEncoder;
import io.fixprotocol.sbe.conformance.suite1.NewOrderSingleEncoder;
import io.fixprotocol.sbe.conformance.suite1.OrdTypeEnum;
import io.fixprotocol.sbe.conformance.suite1.QtyEncodingEncoder;
import io.fixprotocol.sbe.conformance.suite1.SideEnum;
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
   * @param args files names
   * <ol>
   * <li>File name of test plan</li>
   * <li>File name of message file to produce</li>
   * </ol>
   * @throws IOException if an IO error occurs
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      usage();
      System.exit(1);
    }
    RLinjector injector = new RLinjector();
    File in = new File(args[0]);
    File out = new File(args[1]);
    InputStream inputStream = new FileInputStream(in);
    JsonMessageSource jsonInjectorSource = new JsonMessageSource(inputStream);
    if (!jsonInjectorSource.getTestVersion().equals("2016.1")) {
      throw new IllegalArgumentException("Unexpected test version");
    }
    if (jsonInjectorSource.getTestNumber() != 1) {
      throw new IllegalArgumentException("Unexpected test number");
    }
    try (FileOutputStream outStream = new FileOutputStream(out)) {
      for (int index = 0; index < jsonInjectorSource.getInjectMessageCount(); index++) {
        Message message = jsonInjectorSource.getInjectMessage(index);
        injector.inject(message, outStream);
      }
    }
  }

  public static void usage() {
    System.out.println(
        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLinjector <input-test-file> <output-sbe-file>");
  }

  private void doInject1(MessageValues values, OutputStream outFile) throws IOException {
    int offset = 0;
    byte[] bytes = new byte[4096];
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);
    NewOrderSingleEncoder orderEncoder = new NewOrderSingleEncoder();
    MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    messageHeaderEncoder.wrap(buffer, offset);
    messageHeaderEncoder.blockLength(orderEncoder.sbeBlockLength())
        .templateId(orderEncoder.sbeTemplateId()).schemaId(orderEncoder.sbeSchemaId())
        .version(orderEncoder.sbeSchemaVersion());
    offset += messageHeaderEncoder.encodedLength();
    orderEncoder.wrap(buffer, offset);
    orderEncoder.clOrdId(values.getString("11"));
    orderEncoder.account(values.getString("1"));
    orderEncoder.symbol(values.getString("55"));
    orderEncoder.side(SideEnum.get(values.getChar("54", SideEnum.NULL_VAL.value())));
    orderEncoder.transactTime(values.getLong("60", NewOrderSingleEncoder.transactTimeNullValue()));
    QtyEncodingEncoder qtyEncoder = orderEncoder.orderQty();
    qtyEncoder.mantissa(values.getInt("38", QtyEncodingEncoder.exponentNullValue()));
    orderEncoder.ordType(OrdTypeEnum.get(values.getChar("37", OrdTypeEnum.NULL_VAL.value())));
    DecimalEncodingEncoder priceEncoder = orderEncoder.price();
    priceEncoder.mantissa(values.getLong("44", DecimalEncodingEncoder.mantissaNullValue()));
    DecimalEncodingEncoder stopPriceEncoder = orderEncoder.stopPx();
    stopPriceEncoder.mantissa(values.getLong("99", DecimalEncodingEncoder.mantissaNullValue()));

    outFile.write(bytes, 0, offset + orderEncoder.encodedLength());
  }

  public void inject(MessageValues values, OutputStream out) throws IOException {
    doInject1(values, out);
  }

}
