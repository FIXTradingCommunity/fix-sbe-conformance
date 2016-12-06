package io.fixprotocol.sbe.conformance;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Inputs test data and outputs SBE representation
 * 
 * @author Don Mendelson
 *
 */
public interface Injector {

  /**
   * Inputs test data and outputs SBE representation to a file
   * 
   * @param values field values to populate in SBE messages
   * @param out stream to write SBE messages
   * @throws IOException if output file is not accessible
   */
  void inject(MessageValues values, OutputStream out) throws IOException;
}
