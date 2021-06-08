package net.dontcode.preview;

import net.dontcode.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/messages")
public class FromIdeResource {
    private static Logger log = LoggerFactory.getLogger(PreviewSocket.class);

    @Inject
    protected PreviewSocket preview;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveUpdate (Message update) {
        log.debug("Received from IDE");
        log.trace("{}",update);
        preview.broadcast(update);
        return Response.ok().build();
    }
}
