package cn.wnhyang.coolGuard.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.wnhyang.coolGuard.context.PolicyContext;
import cn.wnhyang.coolGuard.convert.PolicyConvert;
import cn.wnhyang.coolGuard.entity.Chain;
import cn.wnhyang.coolGuard.entity.Policy;
import cn.wnhyang.coolGuard.entity.Rule;
import cn.wnhyang.coolGuard.mapper.ChainMapper;
import cn.wnhyang.coolGuard.mapper.PolicyMapper;
import cn.wnhyang.coolGuard.mapper.PolicySetMapper;
import cn.wnhyang.coolGuard.mapper.RuleMapper;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.service.PolicyService;
import cn.wnhyang.coolGuard.service.RuleService;
import cn.wnhyang.coolGuard.util.CollectionUtils;
import cn.wnhyang.coolGuard.util.LFUtil;
import cn.wnhyang.coolGuard.vo.PolicyVO;
import cn.wnhyang.coolGuard.vo.create.PolicyCreateVO;
import cn.wnhyang.coolGuard.vo.page.PolicyPageVO;
import cn.wnhyang.coolGuard.vo.update.PolicyUpdateVO;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.wnhyang.coolGuard.exception.ErrorCodes.*;
import static cn.wnhyang.coolGuard.exception.util.ServiceExceptionUtil.exception;

/**
 * 策略表 服务实现类
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Slf4j
@Service
@LiteflowComponent
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyMapper policyMapper;

    private final PolicySetMapper policySetMapper;

    private final ChainMapper chainMapper;

    private final RuleMapper ruleMapper;

    private final RuleService ruleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPolicy(PolicyCreateVO createVO) {
        validateForCreateOrUpdate(null, createVO.getCode());
        Policy policy = PolicyConvert.INSTANCE.convert(createVO);
        policyMapper.insert(policy);

        String psChain = StrUtil.format(LFUtil.POLICY_SET_CHAIN, policy.getPolicySetId());
        if (!chainMapper.selectByChainName(psChain)) {
            Chain chain = new Chain().setChainName(psChain)
                    .setElData(StrUtil.format(LFUtil.WHEN_EMPTY_NODE_EL,
                            LFUtil.getNodeWithTag(LFUtil.POLICY_COMMON_NODE, policy.getId())));
            chainMapper.insert(chain);
        } else {
            Chain chain = chainMapper.getByChainName(psChain);
            chain.setElData(LFUtil.elAdd(chain.getElData(),
                    LFUtil.getNodeWithTag(LFUtil.POLICY_COMMON_NODE, policy.getId())));
            chainMapper.updateByChainName(psChain, chain);
        }
        // TODO 创建chain
        String pChain = StrUtil.format(LFUtil.POLICY_CHAIN, policy.getId());
        chainMapper.insert(new Chain().setChainName(pChain));
        return policy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicy(PolicyUpdateVO updateVO) {
        Policy policy = PolicyConvert.INSTANCE.convert(updateVO);
        policyMapper.updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(Long id) {
        validateExists(id);
        deletePolicy(Collections.singleton(id));
    }

    @Override
    public void deletePolicy(Collection<Long> ids) {
        ids.forEach(id -> {
            // 1、确认是否还有运行的规则
            List<Rule> ruleList = ruleMapper.selectRunningListByPolicyId(id);
            if (CollUtil.isNotEmpty(ruleList)) {
                throw exception(POLICY_REFERENCE);
            }
            Policy policy = policyMapper.selectById(id);
            // 2、没有运行的规则就可以删除策略了
            // 3、删除策略下的所有规则
            ruleList = ruleMapper.selectByPolicyId(id);
            ruleService.deleteRule(CollectionUtils.convertSet(ruleList, Rule::getId));
            // 4、删除chain
            String psChain = StrUtil.format(LFUtil.POLICY_SET_CHAIN, policy.getPolicySetId());
            Chain chain = chainMapper.getByChainName(psChain);
            chain.setElData(LFUtil.removeEl(chain.getElData(),
                    LFUtil.getNodeWithTag(LFUtil.POLICY_COMMON_NODE, id)));
            chainMapper.updateByChainName(psChain, chain);
            policyMapper.deleteById(id);
            chainMapper.deleteByChainName(StrUtil.format(LFUtil.POLICY_CHAIN, id));
        });
    }

    @Override
    public Policy getPolicy(Long id) {
        return policyMapper.selectById(id);
    }

    @Override
    public PageResult<Policy> pagePolicy(PolicyPageVO pageVO) {
        return policyMapper.selectPage(pageVO);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.IS_ACCESS, nodeId = LFUtil.POLICY_COMMON_NODE, nodeType = NodeTypeEnum.COMMON)
    public boolean policyAccess(NodeComponent bindCmp) {
        Policy policy = policyMapper.selectById(bindCmp.getTag());
        return policy.getStatus();
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = LFUtil.POLICY_COMMON_NODE, nodeType = NodeTypeEnum.COMMON)
    public void policy(NodeComponent bindCmp) {
        PolicyContext policyContext = bindCmp.getContextBean(PolicyContext.class);
        Policy policy = policyMapper.selectById(bindCmp.getTag());
        PolicyVO policyVO = PolicyConvert.INSTANCE.convert(policy);
        policyContext.addPolicy(policyVO.getId(), policyVO);

        log.info("当前策略(id:{}, name:{}, code:{})", policy.getId(), policy.getName(), policy.getCode());

        policyContext.initRuleList(policy.getId());

        bindCmp.invoke2Resp(StrUtil.format(LFUtil.POLICY_CHAIN, policy.getId()), null);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.IS_END, nodeId = LFUtil.POLICY_COMMON_NODE, nodeType = NodeTypeEnum.COMMON)
    public boolean policyEnd(NodeComponent bindCmp) {
        // isEnd 用于顺序模式，提前终止流程
        log.info("终止");
        return false;
    }

    private void validateForCreateOrUpdate(Long id, String name) {
        // 校验存在
        validateExists(id);
        // 校验名唯一
        validateCodeUnique(id, name);
    }

    private void validateExists(Long id) {
        if (id == null) {
            return;
        }
        Policy policy = policyMapper.selectById(id);
        if (policy == null) {
            throw exception(POLICY_NOT_EXIST);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        Policy policy = policyMapper.selectByCode(code);
        if (policy == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(POLICY_CODE_EXIST);
        }
        if (!policy.getId().equals(id)) {
            throw exception(POLICY_CODE_EXIST);
        }
    }

}