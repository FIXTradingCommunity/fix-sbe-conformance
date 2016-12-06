package io.fixprotocol.sbe.conformance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Invokes an SBE implementation under test
 * 
 * @author Don Mendelson
 *
 */
public interface Responder {

  /**
   * Tests an SBE implementation by invoking to decode input messages and encode responses
   * @param in stream of SBE messages encoded by an {@link Injector}
   * @param values field values to populate in SBE messages
   * @param out stream of SBE messages encoded by an SBE implementation under test
   * @throws IOException if an IO error occurs
   * @throws TestException if one or more encoding or decoding errors occur
   */
  void respond(InputStream in, MessageValues values, OutputStream out) throws IOException, TestException;

}
