package cn.wnhyang.coolguard.mapper;

import cn.wnhyang.coolguard.AdminApplication;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.constant.SceneType;
import cn.wnhyang.coolguard.decision.convert.IndicatorVersionConvert;
import cn.wnhyang.coolguard.decision.dto.IndicatorDTO;
import cn.wnhyang.coolguard.decision.entity.Indicator;
import cn.wnhyang.coolguard.decision.entity.IndicatorVersion;
import cn.wnhyang.coolguard.decision.mapper.ChainMapper;
import cn.wnhyang.coolguard.decision.mapper.IndicatorMapper;
import cn.wnhyang.coolguard.decision.vo.page.IndicatorByPolicySetPageVO;
import cn.wnhyang.coolguard.decision.vo.page.IndicatorPageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author wnhyang
 * @date 2024/5/6
 **/
@SpringBootTest(classes = AdminApplication.class)
@Slf4j
public class IndicatorMapperTest {

    @Resource
    private IndicatorMapper indicatorMapper;

    @Resource
    private ChainMapper chainMapper;

    @Test
    public void test() {
        List<Long> longs = indicatorMapper.selectIdListByScene(SceneType.APP, "phone");
        log.info("longs: {}", longs);
    }

    @Test
    public void test2() {
        IndicatorByPolicySetPageVO pageParam = new IndicatorByPolicySetPageVO();
        pageParam.setPolicySetCode("phone_login");
        pageParam.setPageNo(1).setPageSize(10);
        PageResult<Indicator> indicatorPageResult = indicatorMapper.selectPageByScene(pageParam, SceneType.POLICY_SET, "phone_login");
        log.info("indicatorPageResult: {}", indicatorPageResult);
    }

    @Test
    public void test3() {
        List<Indicator> resultList = indicatorMapper.selectListByScene(SceneType.APP, "phone");
        log.info("resultList: {}", resultList);

    }

    @Test
    public void test4() {
        List<Indicator> indicators = indicatorMapper.selectListByScenes("phone", "phone_login");
        log.info("indicators: {}", indicators);

    }

    @Test
    public void test5() {
        List<Indicator> indicatorList = indicatorMapper.selectList();
        log.info("indicatorList: {}", indicatorList);
    }

    @Test
    public void test6() {
        Indicator indicator = indicatorMapper.selectById(1L);
        log.info("indicator: {}", indicator);
        IndicatorVersion indicatorVersion = IndicatorVersionConvert.INSTANCE.convert(indicator);
        log.info("indicatorVersion: {}", indicatorVersion);
    }

    @Test
    public void test7() {
        IndicatorPageVO pageVO = new IndicatorPageVO();
        pageVO.setPageNo(1).setPageSize(10);
//        pageVO.setLatest(false);
        pageVO.setHasVersion(false);
        PageResult<IndicatorDTO> indicatorPageResult = indicatorMapper.selectPage(pageVO);
        System.out.println(indicatorPageResult.getList());
    }

}
