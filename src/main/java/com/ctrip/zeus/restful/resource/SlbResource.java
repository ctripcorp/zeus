package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.impl.ResultHandler;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/")
public class SlbResource {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    private final int TIMEOUT = 1000;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbs")
    public Response list(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("type") final String type,
                         @QueryParam("tag") final List<String> tags,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue,
                         @TrimmedQueryParam("mode") final String mode) throws Exception {

        final SelectionMode selectionMode = SelectionMode.getMode(mode);
        final Long[] slbIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tags != null &&tags.size() > 0;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return new HashSet<>(tagService.query(tags, "slb"));
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return pname != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        if (pvalue != null)
                            return new HashSet<>(propertyService.query(pname, pvalue, "slb"));
                        else
                            return new HashSet<>(propertyService.query(pname, "slb"));
                    }
                }).build(Long.class).run(new ResultHandler<Long, Long>() {
                    @Override
                    public Long[] handle(Set<Long> result) throws Exception {
                        if (result == null) {
                            result = slbCriteriaQuery.queryAll();
                        }
                        return result.toArray(new Long[result.size()]);
                    }
                });

        QueryExecuter<IdVersion> executer = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbIds.length == 0 ? new HashSet<IdVersion>() : slbCriteriaQuery.queryByIdsAndMode(slbIds, selectionMode);
                    }
                })
                .build(IdVersion.class);

        final SlbList slbList = new SlbList();
        for (Slb slb : slbRepository.list(executer.run())) {
            slbList.addSlb(getSlbByType(slb, type));
        }
        slbList.setTotal(slbList.getSlbs().size());
        return responseHandler.handle(slbList, hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlb")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("slbId") final Long slbId,
                        @TrimmedQueryParam("slbName") final String slbName,
                        @TrimmedQueryParam("ip") final String serverIp,
                        @TrimmedQueryParam("type") String type,
                        @TrimmedQueryParam("mode") final String mode) throws Exception {
        final SelectionMode selectionMode = SelectionMode.getMode(mode);
        if (slbId == null && slbName == null && serverIp==null) {
            throw new Exception("Missing parameter.");
        }

        IdVersion[] keys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId==null && serverIp != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbCriteriaQuery.queryBySlbServerIp(serverIp);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return  slbId==null && slbName != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Long _slbId = slbCriteriaQuery.queryByName(slbName);
                        if (_slbId.longValue() == 0L) throw new ValidationException("Slb id cannot be found.");
                        return slbCriteriaQuery.queryByIdsAndMode(new Long[]{_slbId}, selectionMode);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId!=null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbCriteriaQuery.queryByIdsAndMode(new Long[]{slbId}, selectionMode);
                    }
                })
                .build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        return result.toArray(new IdVersion[result.size()]);
                    }
                });
        List<Slb> result = slbRepository.list(keys);
        if (result.size() == 0) throw new ValidationException("Slb cannot be found.");
        if (result.size() == 1) {
            return responseHandler.handle(getSlbByType(result.get(0), type), hh.getMediaType());
        }
        SlbList slbList = new SlbList();
        for (Slb slb :result) {
            slbList.addSlb(getSlbByType(slb, type));
        }
        slbList.setTotal(slbList.getSlbs().size());
        return responseHandler.handle(slbList, hh.getMediaType());
    }

    @POST
    @Path("/slb/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addSlb")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s = slbRepository.add(parseSlb(hh.getMediaType(), slb));
        return responseHandler.handle(s, hh.getMediaType());
    }

    @POST
    @Path("/slb/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateSlb")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String slb) throws Exception {
        Slb s = parseSlb(hh.getMediaType(), slb);
        DistLock lock = dbLockFactory.newLock(s.getName() + "_updateSlb");
        lock.lock(TIMEOUT);
        try {
            s = slbRepository.update(s);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle(s, hh.getMediaType());
    }

    @GET
    @Path("/slb/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteSlb")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) {
            throw new Exception("Query param - slbId is required.");
        }
        Slb archive = slbRepository.getById(slbId);
        if (archive == null) throw new ValidationException("Slb cannot be found with id " + slbId + ".");

        int count = slbRepository.delete(slbId);
        try {
            archiveRepository.archiveSlb(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted slb failed. " + GenericSerializer.writeJson(archive, false), ex);
        }
        String message = count == 1 ? "Delete slb successfully." : "No deletion is needed.";
        return responseHandler.handle(message, hh.getMediaType());
    }

    private Slb parseSlb(MediaType mediaType, String slb) throws Exception {
        Slb s;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            s = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else {
            try {
                s = DefaultJsonParser.parse(Slb.class, slb);
            } catch (Exception e) {
                throw new Exception("Slb cannot be parsed.");
            }
        }
        s.setName(trimIfNotNull(s.getName()));
        for (VirtualServer virtualServer : s.getVirtualServers()) {
            virtualServer.setName(trimIfNotNull(virtualServer.getName()));
            for (Domain domain : virtualServer.getDomains()) {
                domain.setName(trimIfNotNull(domain.getName()));
            }
        }
        for (SlbServer slbServer : s.getSlbServers()) {
            slbServer.setIp(trimIfNotNull(slbServer.getIp()));
            slbServer.setHostName(trimIfNotNull(slbServer.getHostName()));
        }
        return s;
    }

    private Slb getSlbByType(Slb slb, String type) {
        if ("INFO".equalsIgnoreCase(type)) {
            return new Slb().setId(slb.getId())
                    .setName(slb.getName());
        }
        if ("NORMAL".equalsIgnoreCase(type)) {
            Slb result = new Slb().setId(slb.getId())
                    .setName(slb.getName())
                    .setNginxBin(slb.getNginxBin())
                    .setNginxConf(slb.getNginxConf())
                    .setNginxWorkerProcesses(slb.getNginxWorkerProcesses())
                    .setStatus(slb.getStatus())
                    .setVersion(slb.getVersion());
            for (SlbServer slbServer : slb.getSlbServers()) {
                result.addSlbServer(slbServer);
            }
            return result;
        }
        return slb;
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
