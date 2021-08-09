package net.dontcode.preview;

import io.smallrye.mutiny.Uni;
import net.dontcode.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.Date;

@Path("/sessions")
public class SessionResource {
    private static Logger log = LoggerFactory.getLogger(SessionResource.class);

    @Inject
    protected SessionService sessionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listSessions (@QueryParam("from") Date from, @QueryParam("to") Date to) {
        log.debug("Request all sessions from {} to {}", from, to);
        sessionService.listSessions (
                from.toInstant().atZone(ZoneId.systemDefault()),
                to.toInstant().atZone(ZoneId.systemDefault())).onFailure().call(throwable -> {
           log.error("Cannot load list of sessions because of {}", throwable.getMessage());
           return Uni.createFrom().failure(throwable);
        });
        return Response.ok().build();
    }
}
