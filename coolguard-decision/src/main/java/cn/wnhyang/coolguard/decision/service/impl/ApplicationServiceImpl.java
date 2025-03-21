package cn.wnhyang.coolguard.decision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.wnhyang.coolguard.common.entity.LabelValue;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.common.util.CollectionUtils;
import cn.wnhyang.coolguard.decision.constant.SceneType;
import cn.wnhyang.coolguard.decision.convert.ApplicationConvert;
import cn.wnhyang.coolguard.decision.entity.Application;
import cn.wnhyang.coolguard.decision.entity.Indicator;
import cn.wnhyang.coolguard.decision.entity.PolicySet;
import cn.wnhyang.coolguard.decision.mapper.ApplicationMapper;
import cn.wnhyang.coolguard.decision.mapper.IndicatorMapper;
import cn.wnhyang.coolguard.decision.mapper.PolicySetMapper;
import cn.wnhyang.coolguard.decision.service.ApplicationService;
import cn.wnhyang.coolguard.decision.vo.create.ApplicationCreateVO;
import cn.wnhyang.coolguard.decision.vo.page.ApplicationPageVO;
import cn.wnhyang.coolguard.decision.vo.update.ApplicationUpdateVO;
import cn.wnhyang.coolguard.redis.constant.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.wnhyang.coolguard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.wnhyang.coolguard.decision.error.DecisionErrorCode.*;

/**
 * 应用表 服务实现类
 *
 * @author wnhyang
 * @since 2024/04/03
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;

    private final IndicatorMapper indicatorMapper;

    private final PolicySetMapper policySetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKey.APPLICATION, allEntries = true)
    public Long createApplication(ApplicationCreateVO createVO) {
        if (applicationMapper.selectByCode(createVO.getCode()) != null) {
            throw exception(APPLICATION_CODE_EXIST);
        }
        if (applicationMapper.selectByCode(createVO.getName()) != null) {
            throw exception(APPLICATION_NAME_EXIST);
        }
        Application application = ApplicationConvert.INSTANCE.convert(createVO);
        applicationMapper.insert(application);
        return application.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKey.APPLICATION, allEntries = true)
    public void updateApplication(ApplicationUpdateVO updateVO) {
        Application application = applicationMapper.selectById(updateVO.getId());
        if (application == null) {
            throw exception(APPLICATION_NOT_EXIST);
        }
        Application byName = applicationMapper.selectByName(updateVO.getName());
        if (byName != null && !application.getId().equals(byName.getId())) {
            throw exception(APPLICATION_NAME_EXIST);
        }
        Application convert = ApplicationConvert.INSTANCE.convert(updateVO);
        applicationMapper.updateById(convert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKey.APPLICATION, allEntries = true)
    public void deleteApplication(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw exception(APPLICATION_NOT_EXIST);
        }
        // 确认是否有指标引用
        List<Indicator> indicatorList = indicatorMapper.selectListByScene(SceneType.APP, application.getCode());
        if (CollUtil.isNotEmpty(indicatorList)) {
            throw exception(APPLICATION_REFERENCE_DELETE);
        }
        // 确认是否有策略集引用
        List<PolicySet> policySets = policySetMapper.selectList(null, application.getCode(), null, null);
        if (CollUtil.isNotEmpty(policySets)) {
            throw exception(APPLICATION_REFERENCE_DELETE);
        }
        applicationMapper.deleteById(id);
    }

    @Override
    public Application getApplication(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw exception(APPLICATION_NOT_EXIST);
        }
        return application;
    }

    @Override
    public PageResult<Application> pageApplication(ApplicationPageVO pageVO) {
        return applicationMapper.selectPage(pageVO);
    }

    @Override
    public List<LabelValue> getLabelValueList() {
        return CollectionUtils.convertList(applicationMapper.selectList(), Application::getLabelValue);
    }

}
