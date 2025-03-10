package vn.bxh.jobhunter.util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import vn.bxh.jobhunter.domain.response.RestResponse;
import vn.bxh.jobhunter.util.anotation.ApiMessage;

@ControllerAdvice // can thiep vao controller
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        // TODO Auto-generated method stub

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(status);

        if(body instanceof String){
            return body;
        }

        if (status >= 400) {
            return body;
        } else {
            res.setData(body);
            ApiMessage  apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
            res.setMessage(apiMessage==null?"":apiMessage.value());
        }
        return res;
    }

}
