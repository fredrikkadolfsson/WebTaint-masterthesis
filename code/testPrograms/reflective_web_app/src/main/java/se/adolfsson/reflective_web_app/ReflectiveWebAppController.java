package se.adolfsson.reflective_web_app;


import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ReflectiveWebAppController {

  /**
   * Disables Springs built in XSS protection header that some browsers support.
   * <p>
   * https://docs.spring.io/spring-security/site/docs/current/reference/html/headers.html#headers-xss-protection
   *
   * @param response contains function to set header -> X-XSS-Protection: Disabled
   */
  @ModelAttribute
  public void disableXSSProtection(HttpServletResponse response) {
    response.setHeader("X-XSS-Protection", "0");
  }

  @RequestMapping("/")
  public String index(@RequestParam(name = "reflect", required = false) String reflect) {
    return
        "<div style='display:flex;justify-content:center;align-items:center;flex-direction:column;min-height:60%;'>" +
            "<h1>Reflected content:</h1>" +
            "<div>" + reflect + "</div>" +
            "</div>";


  }
}

