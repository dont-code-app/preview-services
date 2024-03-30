package net.dontcode.preview;

import net.dontcode.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
