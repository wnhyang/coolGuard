package cn.wnhyang.coolguard.mapper;

import cn.wnhyang.coolguard.AdminApplication;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.dto.PolicySetDTO;
import cn.wnhyang.coolguard.decision.mapper.PolicySetMapper;
import cn.wnhyang.coolguard.decision.vo.page.PolicySetPageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wnhyang
 * @date 2024/7/21
 **/
@SpringBootTest(classes = AdminApplication.class)
@Slf4j
public class PolicySetMapperTest {

    @Resource
    private PolicySetMapper policySetMapper;

    @Test
    public void test() {
        PolicySetPageVO pageVO = new PolicySetPageVO();
        pageVO.setPageNo(1).setPageSize(10);
//        pageVO.setLatest(true);
        pageVO.setHasVersion(true);
        PageResult<PolicySetDTO> policySetDTOPageResult = policySetMapper.selectPage(pageVO);


    }

}
