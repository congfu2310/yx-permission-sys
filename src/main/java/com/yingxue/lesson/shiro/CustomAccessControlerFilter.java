package com.yingxue.lesson.shiro;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.utils.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

//这个类主要是拦截需求认证的请求，首先验证客户端 header 是否携带了 token ，如果没有携带直接响应客户端，引导客户端到登录界面进行登录操作
// 如果客户端 header 已经携带有 token 放开进入 shiro SecurityManager 验证
@Slf4j
public class CustomAccessControlerFilter extends AccessControlFilter {
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;// 始终返回 false，所有请求都会进入 onAccessDenied 处理
    }

    //如果 token 验证成功，允许访问，否则返回相应的错误信息。
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        log.info(request.getMethod());
        log.info(request.getRequestURL().toString());
        //判断客户端是否携带accessToken
        try {
            //检查 accessToken 是否为空或 null。如果为空，抛出自定义异常
            String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
            if(StringUtils.isEmpty(accessToken)){
                throw new BusinessException(BaseResponseCode.TOKEN_NOT_NULL);
            }
            //使用获取到的 accessToken 创建自定义的 CustomUsernamePasswordToken 实例
            CustomUsernamePasswordToken customUsernamePasswordToken=new CustomUsernamePasswordToken(accessToken);
            //通过 Shiro 的 Subject 执行登录操作，验证 accessToken 的合法性
            getSubject(servletRequest,servletResponse).login(customUsernamePasswordToken);
        } catch (BusinessException e) {
            //捕获 BusinessException，调用 customResponse 方法返回错误响应，错误代码和信息从 BusinessException 中获取，并返回 false 表示访问被拒绝
            customResponse(e.getCode(),e.getDefaultMessage(),servletResponse);
            return false;
        } catch (AuthenticationException e) {
            //捕获 AuthenticationException，检查其 cause 是否为 BusinessException 实例。
            // 如果是，调用 customResponse 方法返回错误响应，错误代码和信息从 cause 中的 BusinessException 获取。
            if(e.getCause() instanceof BusinessException){
                BusinessException exception= (BusinessException) e.getCause();
                customResponse(exception.getCode(),exception.getDefaultMessage(),servletResponse);
            }else {
                customResponse(BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getCode(),BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getMsg(),servletResponse);
            }
           return false;
        }
        return true;
    }

    //自定义的错误代码和消息转换为 JSON 格式，并将其写入 HTTP 响应中
    private void customResponse(int code, String msg, ServletResponse response){
        try {
            DataResult result=DataResult.getResult(code,msg);
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            String userJson = JSON.toJSONString(result);
            OutputStream out = response.getOutputStream();
            out.write(userJson.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            log.error("eror={}",e);
        }
    }
}
