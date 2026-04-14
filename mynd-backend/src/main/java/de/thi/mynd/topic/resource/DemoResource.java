package de.thi.mynd.topic.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/demo")
public class DemoResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String demo() {
    return "This is a demo controller";
  }
}
