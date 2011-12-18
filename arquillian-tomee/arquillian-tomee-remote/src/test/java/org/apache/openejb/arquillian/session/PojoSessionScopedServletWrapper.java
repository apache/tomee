package org.apache.openejb.arquillian.session;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PojoSessionScopedServletWrapper extends HttpServlet {
   @Inject private PojoSessionScoped pojo;

   @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.setContentType("text/plain");
      resp.getWriter().println("ms=" + pojo.getMs());
      resp.getWriter().println("id=" + pojo.getId());
   }

}
