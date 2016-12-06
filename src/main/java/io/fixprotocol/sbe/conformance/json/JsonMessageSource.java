package io.fixprotocol.sbe.conformance.json;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.fixprotocol.sbe.conformance.MessageValues;

public class JsonMessageSource {

  public interface Message extends MessageValues {

  }

  private class MessageSource implements Message {
    private final JsonObject jsonObject;

    public MessageSource(JsonObject jsonObject) {
      this.jsonObject = jsonObject;
    }

    @Override
    public byte getChar(String id, byte nullValue) {
      if (jsonObject.isNull(id)) {
        return nullValue;
      } else {
        return (byte) jsonObject.getJsonString(id).getString().charAt(0);
      }
    }

    @Override
    public int getInt(String id, int nullValue) {
      if (jsonObject.isNull(id)) {
        return nullValue;
      } else {
        return jsonObject.getInt(id);
      }
    }

    @Override
    public long getLong(String id, long nullValue) {
      if (jsonObject.isNull(id)) {
        return nullValue;
      } else {
        return jsonObject.getJsonNumber(id).longValue();
      }
    }

    @Override
    public String getString(String id) {
      return jsonObject.getJsonString(id).getString();
    }

    @Override
    public MessageValues getGroup(String name, int index) {
      return new MessageSource((JsonObject) jsonObject.getJsonArray(name).get(index));
    }

    @Override
    public int getGroupCount(String name) {
      return jsonObject.getJsonArray(name).size();
    }


  }

  private JsonObject jsonObject;



  public JsonMessageSource(InputStream inputStream) {
    JsonReader reader = Json.createReader(inputStream);
    jsonObject = reader.readObject();
  }

  public Message getInjectMessage(int index) {
    return new MessageSource((JsonObject) getInjectMessages().get(index));
  }

  public int getInjectMessageCount() {
    return getInjectMessages().size();
  }

  public Message getResponseMessage(int index) {
    return new MessageSource((JsonObject) getResponseMessages().get(index));
  }

  public int getResponseMessageCount() {
    return getResponseMessages().size();
  }
  
  private JsonArray getInjectMessages() {
    return jsonObject.getJsonObject("inject").getJsonArray("messages");
  }

  private JsonArray getResponseMessages() {
    return jsonObject.getJsonObject("respond").getJsonArray("messages");
  }
  
  public int getTestNumber() {
    return jsonObject.getJsonObject("version").getInt("testNumber");
  }

  public String getTestVersion() {
    return jsonObject.getJsonObject("version").getJsonString("testVersion").getString();
  }

}
