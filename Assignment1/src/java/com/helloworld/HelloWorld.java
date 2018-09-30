
package com.helloworld;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("helloworld")
public class HelloWorld
{
  @Context
  private UriInfo context;
  
  public HelloWorld() {}
  
  @GET
  @Produces({"text/plain"})
  public String getText()
  {
    return "alive";
  }
  
  @GET
  @Path("/sayHello")
  public String sayHello() {
    return "Hello";
  }
  


  @PUT
  @Consumes({"text/plain"})
  public int postText(String content)
  {
    return content.length();
  }
}