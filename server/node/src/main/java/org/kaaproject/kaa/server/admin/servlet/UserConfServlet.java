package org.kaaproject.kaa.server.admin.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationService;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationService;
import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Created by pyshankov on 08.09.16.
 */
public class UserConfServlet extends HttpServlet implements Servlet, ServletParams {

    private static final long serialVersionUID = 1584721028432234643L;

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(UserConfServlet.class);

    private static final int BUFFER = 1024 * 100;

    private static final String JSON = "application/json";

    private static final ObjectMapper FORMATTER = new ObjectMapper();

    /** The Constant PROFILE_FILE_NAME_PATTERN. */
    private static final String USER_CONFIG_NAME_PATTERN = "UserConfig_{}.v{}.json";

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String externalUId = URLDecoder.decode(request.getParameter(USER_EXTERNAL_ID_PARAMETER), "UTF-8");
        String appId = URLDecoder.decode(request.getParameter(APPLICATION_ID_PARAMETER), "UTF-8");
        int schemaVersion = Integer.parseInt(URLDecoder.decode(request.getParameter(CONFIGURATION_SCHEMA_ID), "UTF-8"));

        try {
            EndpointUserConfigurationDto endpointUserConfigurationDto = configurationService.findUserConfigurationByExternalUIdAndAppIdAndSchemaVersion(externalUId,appId,schemaVersion);
            String json;
            String fileName = MessageFormatter.arrayFormat(USER_CONFIG_NAME_PATTERN, new Object[] {  externalUId,appId } ).getMessage();

                json = endpointUserConfigurationDto.getBody();

            Object jsonObject = FORMATTER.readValue(json, Object.class);
            byte[] body = FORMATTER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject).getBytes("UTF-8");
            ServletUtils.prepareDisposition(request, response, fileName);
            response.setContentType(JSON);
            response.setContentLength(body.length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(body);
            response.flushBuffer();
        } catch (Exception e) {
            LOG.error("Unexpected error in ProfileDownloadServlet.doGet: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get file: " + e.getMessage());
        }
    }
}
