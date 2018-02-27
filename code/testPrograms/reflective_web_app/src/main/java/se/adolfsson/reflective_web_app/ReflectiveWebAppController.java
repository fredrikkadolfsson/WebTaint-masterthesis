package se.adolfsson.reflective_web_app;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReflectiveWebAppController {

  @RequestMapping("/")
  public String index(@RequestParam(name = "reflect") String reflect) {
    return
        "<div style='display:flex;justify-content:center;align-items:center;flex-direction:column;min-height:60%;'>" +
            "<h1>Reflected content:</h1>" +
            "<div>" + reflect + "</div>" +
            "</div>";
  }
}

