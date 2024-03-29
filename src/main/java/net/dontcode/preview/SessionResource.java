package net.dontcode.preview;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import net.dontcode.common.session.SessionDetail;
import net.dontcode.common.session.SessionOverview;
import net.dontcode.common.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Path("/preview/sessions")
public class SessionResource {
    private static Logger log = LoggerFactory.getLogger(SessionResource.class);

    @Inject
    protected SessionService sessionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<SessionOverview> listSessions (@QueryParam("from") Date from, @QueryParam("to") Date to, @QueryParam("srcInfo") String srcInfo) {
        log.debug("Request session overviews from {} to {}", from, to);
        ZonedDateTime fromZoned = null;
        ZonedDateTime toZoned = null;
        if( from != null) fromZoned = from.toInstant().atZone(ZoneId.systemDefault());
        if( to != null) toZoned = to.toInstant().atZone(ZoneId.systemDefault());
        return sessionService.listSessionOverview (
            fromZoned, toZoned, srcInfo
        );
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SessionDetail> getSession (@PathParam("id") String id) {
        log.debug("Request session detail of {}", id);
        return sessionService.getSession (id);
    }
}
