package net.dontcode.preview;

import io.smallrye.mutiny.Multi;
import net.dontcode.session.SessionOverview;
import net.dontcode.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
    public Multi<SessionOverview> listSessions (@QueryParam("from") Date from, @QueryParam("to") Date to) {
        log.debug("Request session overviews from {} to {}", from, to);
        ZonedDateTime fromZoned = null;
        ZonedDateTime toZoned = null;
        if( from != null) fromZoned = from.toInstant().atZone(ZoneId.systemDefault());
        if( to != null) toZoned = to.toInstant().atZone(ZoneId.systemDefault());
        return sessionService.listSessionOverview (
            fromZoned, toZoned
        );
    }
}
