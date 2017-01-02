package io.fixprotocol.sbe.conformance;

import java.math.BigDecimal;

/**
 * A source of values for encoding a message
 * @author Don Mendelson
 *
 */
public interface MessageValues {

  /**
   * Returns a character value for a field
   * 
   * @param id field tag
   * @param nullValue value to represent null
   * @return a character value
   */
  byte getChar(String id, byte nullValue);

  /**
   * Returns a decimal value for a field
   * 
   * @param id field tag
   * @param nullValue value to represent null 
   * @return a decimal value
   */
  BigDecimal getDecimal(String id, BigDecimal nullValue);

  /**
   * Returns field values for a nested group
   * @param name of the group
   * @param index instance of the group
   * @return values for the group
   */
  MessageValues getGroup(String name, int index);

  /**
   * Returns instances of a group
   * @param name of the group
   * @return number of instances
   */
  int getGroupCount(String name);
  
  /**
   * Returns an integer value for a field
   * 
   * @param id field tag
   * @param nullValue value to represent null
   * @return an integer value
   */
  int getInt(String id, int nullValue);

  /**
   * Returns a long value for a field
   * 
   * @param id field tag
   * @param nullValue value to represent null
   * @return a long value
   */
  long getLong(String id, long nullValue);
  
  /**
   * Returns a String value for a field
   * 
   * @param id field tag
   * @return a String value
   */
  String getString(String id);

}
