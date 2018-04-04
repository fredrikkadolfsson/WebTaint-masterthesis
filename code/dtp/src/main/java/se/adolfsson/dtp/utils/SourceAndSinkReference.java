package se.adolfsson.dtp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Value;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Value
public class SourceAndSinkReference {
  private String clazz;
  private String[] methods;
  private String descriptor;


  public static List<SourceAndSinkReference> getSources() throws IOException {
    return getSourcesOrSinks("sources.json");
  }

  public static List<SourceAndSinkReference> getSinks() throws IOException {
    return getSourcesOrSinks("sinks.json");
  }

  private static List<SourceAndSinkReference> getSourcesOrSinks(String fileName) throws IOException {
    URL fileUrl = ClassLoader.getSystemClassLoader().getResource(fileName);
    ObjectMapper mapper = new ObjectMapper();
    CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, SourceAndSinkReference.class);

    return mapper.readValue(fileUrl, type);
  }
}
