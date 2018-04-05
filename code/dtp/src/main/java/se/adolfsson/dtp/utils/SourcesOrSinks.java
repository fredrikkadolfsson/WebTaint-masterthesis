package se.adolfsson.dtp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Value
public class SourcesOrSinks {
  private List<SourceOrSink> classes;
  private List<SourceOrSink> interfaces;

  public static SourcesOrSinks getSources() throws IOException {
    return getSourcesOrSinks("sources.json");
  }

  public static SourcesOrSinks getSinks() throws IOException {
    return getSourcesOrSinks("sinks.json");
  }

  private static SourcesOrSinks getSourcesOrSinks(String fileName) throws IOException {
    URL fileUrl = ClassLoader.getSystemClassLoader().getResource(fileName);
    ObjectMapper mapper = new ObjectMapper();

    return mapper.readValue(fileUrl, SourcesOrSinks.class);
  }

  static boolean isSourceOrSink(List<SourceOrSink> sourcesOrSinks, String className) {
    for (SourceOrSink source : sourcesOrSinks) {
      if (className.equals(source.getClazz())) return true;
    }

    return false;
  }
}

