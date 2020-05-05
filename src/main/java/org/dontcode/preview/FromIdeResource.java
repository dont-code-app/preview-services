package org.dontcode.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/updates")
public class FromIdeResource {
    private static Logger log = LoggerFactory.getLogger(PreviewSocket.class);

    @Inject
    protected PreviewSocket preview;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveUpdate (String update) {
        log.debug("From IDE {}",update);
        preview.broadcast(update);
        return Response.ok().build();
    }
}
