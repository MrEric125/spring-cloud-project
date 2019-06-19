package com.louis.server.service;

import com.google.common.collect.Lists;
import com.louis.common.api.dto.LoginAuthDto;
import com.louis.common.web.web.utils.RequestUtil;
import com.louis.core.redis.RedisOperate;
import com.louis.core.service.CRUDService;
import com.louis.oauth.dto.ClientMessageDto;
import com.louis.security.core.SecurityUser;
import com.louis.constant.RedisConstant;
import com.louis.server.entity.SysUser;
import com.louis.server.entity.UserLoginLog;
import com.louis.server.repository.SysUserRepository;
import com.louis.security.utils.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Eric
 * @date create in 2019/4/15
 */
@Service
@Slf4j
public class SysUserService extends CRUDService<SysUser, Long> {

    @Autowired
    private  PasswordService passwordService;

    @Autowired
    private  SysUserRepository sysUserRepository;

    @Autowired
    private  RedisOperate<SysUser> redisOperate;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private LoginLogService loginLogService;


    @Autowired
    RedisTemplate<Object ,Object> redisTemplate;



    public SysUser findByUserName(String userName) {

        log.info(" find user by user name ; username:{}", userName);
//        SysUser user =  getUserFromRedisCache(RedisConstant.SYS_USER + userName);
        SysUser user = null;
        if (user == null) {
            user = sysUserRepository.findByUsername(userName);
            if (user!=null) {
//                putUserToRedisCache(user);
            }
        }

        return user;

    }

    @Override
    public SysUser save(SysUser user) {
        if (user.getRegistryDate()==null) {
            user.setRegistryDate(new Date());
        }
        if (user.getIdentityNumber()==null) {
            user.setIdentityNumber(user.getUsername());
        }
        user.randomSalt();
        user.setPassword(passwordService.encryptPassword(user.getUsername(), user.getPassword(), user.getSalt()));
        return super.save(user);
    }

    public void putUserToRedisCache(SysUser user) {
        try {
            if( user == null ) return;
            String key = RedisConstant.SYS_USER+user.getIdToString();
            redisOperate.set(key, user, 12);
            redisTemplate.opsForValue().set(RedisConstant.SYS_USER+user.getUsername(), key, 12);
            redisTemplate.opsForValue().set(RedisConstant.SYS_USER+user.getEmail(), key, 12);
            redisTemplate.opsForValue().set(RedisConstant.SYS_USER+user.getPhone(), key, 12);
        }catch(Exception e) {
            log.error("save user to redis error:{}", e.getMessage());
        }
    }

    public SysUser getUserFromRedisCache(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if( obj instanceof SysUser)
                return (SysUser)obj;

            //username,phone,email,empid
            if( obj instanceof String) {
                Object obj2 = redisTemplate.opsForValue().get(String.valueOf(obj));
                if( obj2 instanceof SysUser)
                    return (SysUser)obj2;
                if( obj2 == null ) {
                    redisTemplate.delete(String.valueOf(obj));
                }
            }
        }catch(Exception e) {

            log.error("get user from redis error: key :{},{}", key, e.getMessage());
        }
        return null;
    }

    /**
     * TODO 暂时不查数据库，模拟一套数据出来
     * 查找用于的权限
     * @param userId
     * @return
     */
    public Collection<GrantedAuthority> loadUserAuthorities(Long userId){
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List<GrantedAuthority> authList = Lists.newArrayList();
        authList.add(grantedAuthority);
        return authList;
    }

    public SysUser findUserInfoByUserId(Long userId) {
        return null;
    }

    /**
     * 处理登录信息
     * @param token
     * @param principal
     * @param request
     */
    public void handlerLoginData(OAuth2AccessToken token, SecurityUser principal, HttpServletRequest request) {
        log.info("handle login data ;  token:{}", token);
         UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        //获取客户端操作系统
         String os = userAgent.getOperatingSystem().getName();
        //获取客户端浏览器
         String browser = userAgent.getBrowser().getName();
        String ipAddr = IpUtils.getIpAddr(request);
        // 根据IP获取位置信息
         String remoteLocation = RequestUtil.getLocationByIp(ipAddr);
         String requestURI = request.getRequestURI();
        ClientMessageDto messageDto = ClientMessageDto.builder().os(os)
                .browser(browser)
                .ip(ipAddr)
                .remoteLocation(remoteLocation)
                .requestURI(requestURI)
                .build();

        Long userId = principal.getUserId();
        LoginAuthDto loginAuthDto = new LoginAuthDto(userId, principal.getLoginName(), principal.getNickName(), principal.getGroupId(), principal.getGroupName());
        // 记录token日志

        userTokenService.saveUserToken(token,loginAuthDto);
        // 记录操作日志
        taskExecutor.execute(() -> loginLogService.saveLoginLog(messageDto,loginAuthDto));
    }

}
