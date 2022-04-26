package utils;

import constant.Constants;
import exception.DtmException;
import feign.Response;
import interfaces.feign.IURIParser;
import model.response.ResponseBase;
import model.feign.ServiceMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FeignUtils {
    private static final Logger log = LoggerFactory.getLogger(FeignUtils.class);

    private static IURIParser uriParser;

    public static void setUriParser(IURIParser uriParser) {
        FeignUtils.uriParser = uriParser;
    }

    public static String parseGid(ResponseBase base) throws Exception {
        if (base == null || !Constants.SUCCESS_RESULT.equals(base.getDtmResult())) {
            throw new Exception("get new gid from dtm server fail.");
        }
        return base.getGid();
    }

    public static String generatorURI(ServiceMessage serviceMessage, boolean httpType) throws Exception {
        return uriParser.generatorURI(serviceMessage, httpType);
    }

    public static void checkResult(Response response) throws DtmException {
        if (response.status() >= Constants.RESP_ERR_CODE){
            if (response.reason() != null) {
                throw new DtmException(response.reason());
            }
            try {
                log.error("response code is {}, but unknown reason, response body is {}", response.status(),
                        IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8)));
            } finally {
                throw new DtmException("response code is " + response.status());
            }
        }
        String result = "";
        try {
            InputStream inputStream = response.body().asInputStream();
            if (inputStream != null) {
                result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new DtmException("response is null");
        }
        if (StringUtils.isBlank(result)) {
            throw new DtmException("response is null");
        }
        if (result.contains(Constants.FAILURE_RESULT)){
            throw new DtmException("Service returned failed");
        }
    }
}
