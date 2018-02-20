package se.tardell.simon.bajs.sample;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(value = "/hi", name = "hello-servlet")
public class HelloServlet extends GenericServlet {

  public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
    res.getWriter().println("Hi World!");
  }
}