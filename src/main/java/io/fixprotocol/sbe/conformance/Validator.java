package io.fixprotocol.sbe.conformance;

import java.io.IOException;
import java.io.InputStream;

/**
 * Validates messages emited by the SBE implementation under test
 * @author Don Mendelson
 *
 */
public interface Validator {

  /**
   * Validate SBE messages
   * @param inputStream stream of SBE messages encoded by an {@link Responder}
   * @param responseValues field values to populate in SBE messages
   * @param injectValues field values that were populated in injected SBE messages
   * @throws IOException if an IO error occurs
   * @throws TestException if validation fails
   */
  void validate(InputStream inputStream, MessageValues responseValues, MessageValues injectValues) throws IOException, TestException;

}
