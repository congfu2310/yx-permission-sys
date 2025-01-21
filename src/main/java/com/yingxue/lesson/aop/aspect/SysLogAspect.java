package com.yingxue.lesson.aop.aspect;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.mapper.SysLogMapper;
import com.yingxue.lesson.utils.HttpContextUtils;
import com.yingxue.lesson.utils.IPUtils;
import com.yingxue.lesson.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;


@Aspect
@Component
@Slf4j
//用于拦截和记录带有 @MyLog 注解的方法的执行情况
// 它使用 Spring AOP 实现环绕增强，在方法执行前后记录相关日志信息，并将这些信息保存到数据库中
public class SysLogAspect {
    //用于将日志信息保存到数据库中
    @Autowired
    private SysLogMapper sysLogMapper;


    //@Pointcut：定义一个切入点，匹配所有带有 @MyLog 注解的方法。
    @Pointcut("@annotation(com.yingxue.lesson.aop.annotation.MyLog)")
    public void logPointCut(){}

    //环绕增强，围绕方法执行前后执行自定义逻辑
    //参数 ProceedingJoinPoint point：允许控制目标方法的执行
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //记录方法执行开始时间
        long beginTime = System.currentTimeMillis();
        //执行目标方法并获取返回值
        Object result = point.proceed();
        //计算方法执行时间
        long time = System.currentTimeMillis() - beginTime;
        //调用 saveSysLog 方法保存日志
        try {
            saveSysLog(point, time);
        } catch (Exception e) {
            log.error("e={}",e);
        }
        return result;
    }

    //参数：
    //ProceedingJoinPoint joinPoint：当前连接点
    //long time：方法执行时间
    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        //获取目标方法的签名和 @MyLog 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        //创建并填充 SysLog 实体
        SysLog sysLog = new SysLog();
        //获取请求的类名和方法名
        MyLog myLog = method.getAnnotation(MyLog.class);
        if(myLog != null){
            //@MyLog 注解提供了 title 和 action 两个属性，用于描述用户操作的模块和动作
            sysLog.setOperation(myLog.title()+"-"+myLog.action());
        }
        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");
        //打印该方法耗时时间
        log.info("请求{}.{}耗时{}毫秒",className,methodName,time);
        try {
            //请求的参数
            Object[] args = joinPoint.getArgs();
            String params=null;
            if(args.length!=0){
                params= JSON.toJSONString(args);
            }
            sysLog.setParams(params);
        } catch (Exception e) {
        }
        //获取request
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        //设置IP地址
        sysLog.setIp(IPUtils.getIpAddr(request));
        log.info("Ip{}，接口地址{}，请求方式{}，入参：{}",sysLog.getIp(),request.getRequestURL(),request.getMethod(),sysLog.getParams());
        //用户名
        String  token = request.getHeader(Constant.ACCESS_TOKEN);
        String userId= JwtTokenUtil.getUserId(token);
        String username= JwtTokenUtil.getUserName(token);
        sysLog.setUsername(username);
        sysLog.setUserId(userId);
        sysLog.setTime((int) time);
        sysLog.setId(UUID.randomUUID().toString());
        sysLog.setCreateTime(new Date());
        log.info(sysLog.toString());
        sysLogMapper.insertSelective(sysLog);

    }
}
