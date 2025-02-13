package cn.wnhyang.coolGuard.log.core.aop;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.wnhyang.coolGuard.log.core.annotation.OperateLog;
import cn.wnhyang.coolGuard.log.core.dto.LogCreateReqDTO;
import cn.wnhyang.coolGuard.log.core.enums.OperateType;
import cn.wnhyang.coolGuard.log.core.service.LogService;
import cn.wnhyang.coolGuard.pojo.CommonResult;
import cn.wnhyang.coolGuard.satoken.core.util.LoginUtil;
import cn.wnhyang.coolGuard.util.JsonUtil;
import cn.wnhyang.coolGuard.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static cn.wnhyang.coolGuard.exception.GlobalErrorCode.INTERNAL_SERVER_ERROR;
import static cn.wnhyang.coolGuard.exception.GlobalErrorCode.SUCCESS;


/**
 * 拦截使用 @OperateLog 注解，如果满足条件，则生成操作日志。
 * 满足如下任一条件，则会进行记录：
 * 1. 使用 @ApiOperation + 非 @GetMapping
 * 2. 使用 @OperateLog 注解
 * <p>
 * 但是，如果声明 @OperateLog 注解时，将 enable 属性设置为 false 时，强制不记录。
 *
 * @author 芋道源码
 */
@Aspect
@Slf4j
@Setter
public class OperateLogAspect {

    /**
     * 用于记录操作内容的上下文
     *
     * @see LogCreateReqDTO#getContent()
     */
    private static final ThreadLocal<String> CONTENT = new ThreadLocal<>();
    /**
     * 用于记录拓展字段的上下文
     *
     * @see LogCreateReqDTO#getExts()
     */
    private static final ThreadLocal<Map<String, Object>> EXTS = new ThreadLocal<>();

    private LogService logService;

    @Around("@annotation(operateLog) && @within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        return around0(joinPoint, operateLog);
    }

    @Around("!@annotation(cn.wnhyang.coolGuard.log.core.annotation.OperateLog) && @within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return around0(joinPoint, null);
    }

    public Object around0(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        Object result = null;
        LogCreateReqDTO operateLogObj = null;
        try {
            // 没有接入链路追踪，暂时使用uuid作为请求ID
            String traceId = IdUtil.simpleUUID();
            MDC.put("traceId", traceId);

            operateLogObj = new LogCreateReqDTO();
            // 补全通用字段
            operateLogObj.setStartTime(startTime);
            // 补充用户信息
            try {
                Long userId = LoginUtil.getUserId();
                operateLogObj.setUserId(userId);
                operateLogObj.setUserNickname(LoginUtil.getLoginUser().getNickname());
            } catch (Exception e) {
                operateLogObj.setUserId(0L);
            }
            // 补全模块信息
            fillModuleFields(operateLogObj, joinPoint, operateLog);

            log.info("userId:{}, module:{}, name:{}, type:{}, content:{}, exts:{}",
                    operateLogObj.getUserId(), operateLogObj.getModule(),
                    operateLogObj.getName(), operateLogObj.getType(),
                    operateLogObj.getContent(), operateLogObj.getExts());

            // 补全请求信息
            fillRequestFields(operateLogObj, joinPoint, operateLog);

            // 执行原有方法
            result = joinPoint.proceed();

            // 补全结果信息
            fillResultFields(operateLogObj, operateLog, startTime, result, null);

            // 判断不记录的情况
            if (!isLogEnable(joinPoint, operateLog)) {
                return result;
            }
            // 目前，只有管理员，才记录操作日志！所以非管理员，直接调用，不进行记录

            // 异步记录日志
            logService.createLog(operateLogObj);

            return result;
        } catch (Throwable exception) {
            log.error("[log][记录操作日志时，发生异常，其中参数是 joinPoint({}) operateLog({}) result({}) exception({}) ]",
                    joinPoint, operateLog, result, exception, exception);
            // 补全结果信息
            fillResultFields(operateLogObj, operateLog, startTime, result, exception);
            throw exception;
        } finally {
            clearThreadLocal();
            MDC.clear();
        }
    }

    public static void setContent(String content) {
        CONTENT.set(content);
    }

    public static void addExt(String key, Object value) {
        if (EXTS.get() == null) {
            EXTS.set(new HashMap<>());
        }
        EXTS.get().put(key, value);
    }

    private static void clearThreadLocal() {
        CONTENT.remove();
        EXTS.remove();
    }

    private static void fillModuleFields(LogCreateReqDTO operateLogObj,
                                         ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        // module 属性
        if (operateLog != null) {
            operateLogObj.setModule(operateLog.module());
        }
        // name 属性
        if (operateLog != null) {
            operateLogObj.setName(operateLog.name());
        }
        // type 属性
        if (operateLog != null && ArrayUtil.isNotEmpty(operateLog.type())) {
            operateLogObj.setType(operateLog.type()[0].getType());
        }
        if (operateLogObj.getType() == null) {
            RequestMethod requestMethod = obtainFirstMatchRequestMethod(obtainRequestMethod(joinPoint));
            OperateType operateLogType = convertOperateLogType(requestMethod);
            operateLogObj.setType(operateLogType != null ? operateLogType.getType() : null);
        }
        // content 和 exts 属性
        operateLogObj.setContent(CONTENT.get());
        operateLogObj.setExts(EXTS.get());
    }

    private static void fillRequestFields(LogCreateReqDTO operateLogObj, ProceedingJoinPoint joinPoint,
                                          OperateLog operateLog) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        operateLogObj.setRequestMethod(request.getMethod());
        operateLogObj.setRequestUrl(request.getRequestURI());
        operateLogObj.setUserIp(ServletUtils.getClientIP(request));
        operateLogObj.setUserAgent(ServletUtils.getUserAgent(request));
        log.info("requestMethod:{}, requestUrl:{}, userIp:{}, userAgent:{}, javaMethod:{}",
                operateLogObj.getRequestMethod(), operateLogObj.getRequestUrl(),
                operateLogObj.getUserIp(), operateLogObj.getUserAgent(),
                operateLogObj.getJavaMethod());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        operateLogObj.setJavaMethod(methodSignature.toString());
        if (operateLog == null || operateLog.logArgs()) {
            operateLogObj.setJavaMethodArgs(obtainMethodArgs(joinPoint));
        }
    }

    private static void fillResultFields(LogCreateReqDTO operateLogObj,
                                         OperateLog operateLog,
                                         LocalDateTime startTime, Object result, Throwable exception) {
        LocalDateTime endTime = LocalDateTime.now();
        operateLogObj.setEndTime(endTime);
        operateLogObj.setDuration((int) (LocalDateTimeUtil.between(startTime, endTime).toMillis()));
        log.info("startTime:{}, duration:{}, resultCode:{}, resultMsg:{}",
                startTime, operateLogObj.getDuration(), operateLogObj.getResultCode(), operateLogObj.getResultMsg());

        // （正常）处理 resultCode 和 resultMsg 字段
        if (result instanceof CommonResult<?> commonResult) {
            operateLogObj.setResultCode(commonResult.getCode());
            operateLogObj.setResultMsg(commonResult.getMsg());
        } else {
            operateLogObj.setResultCode(SUCCESS.getCode());
        }
        // （异常）处理 resultCode 和 resultMsg 字段
        if (exception != null) {
            operateLogObj.setResultCode(INTERNAL_SERVER_ERROR.getCode());
            operateLogObj.setResultMsg(ExceptionUtil.getRootCauseMessage(exception));
        }
        if (operateLog == null || operateLog.logResultData()) {
            operateLogObj.setResultData(obtainResultData(result));
        }
    }

    private static boolean isLogEnable(ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        return operateLog != null && operateLog.enable();
    }

    private static RequestMethod obtainFirstLogRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtil.isEmpty(requestMethods)) {
            return null;
        }
        return Arrays.stream(requestMethods).filter(requestMethod ->
                        requestMethod == RequestMethod.POST
                                || requestMethod == RequestMethod.PUT
                                || requestMethod == RequestMethod.DELETE)
                .findFirst().orElse(null);
    }

    private static RequestMethod obtainFirstMatchRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtil.isEmpty(requestMethods)) {
            return null;
        }
        // 优先，匹配最优的 POST、PUT、DELETE
        RequestMethod result = obtainFirstLogRequestMethod(requestMethods);
        if (result != null) {
            return result;
        }
        // 然后，匹配次优的 GET
        result = Arrays.stream(requestMethods).filter(requestMethod -> requestMethod == RequestMethod.GET)
                .findFirst().orElse(null);
        if (result != null) {
            return result;
        }
        // 兜底，获得第一个
        return requestMethods[0];
    }

    private static OperateType convertOperateLogType(RequestMethod requestMethod) {
        if (requestMethod == null) {
            return null;
        }
        return switch (requestMethod) {
            case GET -> OperateType.GET;
            case POST -> OperateType.CREATE;
            case PUT -> OperateType.UPDATE;
            case DELETE -> OperateType.DELETE;
            default -> OperateType.OTHER;
        };
    }

    private static RequestMethod[] obtainRequestMethod(ProceedingJoinPoint joinPoint) {
        // 使用 Spring 的工具类，可以处理 @RequestMapping 别名注解
        RequestMapping requestMapping = AnnotationUtils.getAnnotation(
                ((MethodSignature) joinPoint.getSignature()).getMethod(), RequestMapping.class);
        return requestMapping != null ? requestMapping.method() : new RequestMethod[]{};
    }

    private static String obtainMethodArgs(ProceedingJoinPoint joinPoint) {
        // TODO 提升：参数脱敏和忽略
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] argNames = methodSignature.getParameterNames();
        Object[] argValues = joinPoint.getArgs();
        // 拼接参数
        Map<String, Object> args = new HashMap<>();
        for (int i = 0; i < argNames.length; i++) {
            String argName = argNames[i];
            Object argValue = argValues[i];
            // 被忽略时，标记为 ignore 字符串，避免和 null 混在一起
            args.put(argName, !isIgnoreArgs(argValue) ? argValue : "[ignore]");
        }
        log.info("javaMethodArgs:{}", args);
        return JsonUtil.toJsonString(args);
    }

    private static String obtainResultData(Object result) {
        // TODO 提升：结果脱敏和忽略
        if (result instanceof CommonResult) {
            result = ((CommonResult<?>) result).getData();
        }
        log.info("resultData:{}", result);
        return JsonUtil.toJsonString(result);
    }

    private static boolean isIgnoreArgs(Object object) {
        Class<?> clazz = object.getClass();
        // 处理数组的情况
        if (clazz.isArray()) {
            return IntStream.range(0, Array.getLength(object))
                    .anyMatch(index -> isIgnoreArgs(Array.get(object, index)));
        }
        // 递归，处理数组、Collection、Map 的情况
        if (Collection.class.isAssignableFrom(clazz)) {
            return ((Collection<?>) object).stream()
                    .anyMatch((Predicate<Object>) OperateLogAspect::isIgnoreArgs);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return isIgnoreArgs(((Map<?, ?>) object).values());
        }
        // obj
        return object instanceof MultipartFile
                || object instanceof HttpServletRequest
                || object instanceof HttpServletResponse
                || object instanceof BindingResult;
    }

}
