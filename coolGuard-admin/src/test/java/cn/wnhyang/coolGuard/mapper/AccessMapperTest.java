package cn.wnhyang.coolGuard.mapper;

import cn.wnhyang.coolGuard.AdminApplication;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wnhyang
 * @date 2024/11/28
 **/
@SpringBootTest(classes = AdminApplication.class)
@Slf4j
public class AccessMapperTest {

    @Resource
    private AccessMapper accessMapper;


}
